package com.github.rjbx.givetrack.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Company;
import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public final class DatabaseAccessor {

    static void fetchSearch(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        Uri contentUri = UserEntry.CONTENT_URI_USER;
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<User> entries = getEntryListFromCursor(cursor, User.class);
        
        User user = null;
        for (User u : entries) if (u.getActive()) user = u;
        
        Map<String, String> request = new HashMap<>();
        if (user.getFocus()) request.put(DatabaseAccessor.FetchContract.PARAM_EIN, user.getCompany());
        else {
            request.put(DatabaseAccessor.FetchContract.PARAM_SEARCH, user.getTerm());
            request.put(DatabaseAccessor.FetchContract.PARAM_CITY, user.getCity());
            request.put(DatabaseAccessor.FetchContract.PARAM_STATE, user.getState());
            request.put(DatabaseAccessor.FetchContract.PARAM_ZIP, user.getZip());
            request.put(DatabaseAccessor.FetchContract.PARAM_MIN_RATING, user.getMinrating());
            request.put(DatabaseAccessor.FetchContract.PARAM_FILTER, user.getFilter() ? "1" : "0");
            request.put(DatabaseAccessor.FetchContract.PARAM_SORT, user.getSearchSort() + ":" + user.getSearchOrder());
            request.put(DatabaseAccessor.FetchContract.PARAM_PAGE_NUM, user.getPages());
            request.put(DatabaseAccessor.FetchContract.PARAM_PAGE_SIZE, user.getRows());
        }

        Uri.Builder builder = Uri.parse(FetchContract.BASE_URL).buildUpon();
        builder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);

        // Append required parameters
        builder.appendQueryParameter(FetchContract.PARAM_APP_ID, context.getString(R.string.cn_app_id));
        builder.appendQueryParameter(FetchContract.PARAM_APP_KEY, context.getString(R.string.cn_app_key));

        boolean single = request.containsKey(FetchContract.PARAM_EIN);
        if (single) builder.appendPath((String) request.get(FetchContract.PARAM_EIN));
        else {
            // Append optional parameters
            for (String param : FetchContract.OPTIONAL_PARAMS) {
                if (request.containsKey(param)) {
                    String value = (String) request.get(param);
                    if (value != null && !value.equals(""))
                        builder.appendQueryParameter(param, value);
                }
            }
        }
        URL url = getUrl(builder.build());

        // Retrieve data
        String response = requestResponseFromUrl(url);
        if (response == null) return;
        Search[] parsedResponse = parseSearches(response, user.getUid(), single);

        // Store data
        removeEntriesFromLocal(local, Search.class, null);
        addEntriesToLocal(local, Search.class, parsedResponse);
        addEntriesToRemote(remote, Search.class, parsedResponse);
    }

    static List<Search> getSearch(Context context) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Search.class);
        
        Uri contentUri = CompanyEntry.CONTENT_URI_SEARCH;
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<Search> entries = getEntryListFromCursor(cursor, Search.class);
        cursor.close();
        return entries;
    }

    static void addSearch(Context context, Search... entries) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Search.class);
        
        addEntriesToLocal(local, Search.class, entries);
    }

    static void removeSearch(Context context, @Nullable Search... search) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Search.class);

        removeEntriesFromLocal(local, Search.class, search);
     }

    static void fetchGiving(Context context) {
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        context.getContentResolver().delete(contentUri, null, null);
        pullRemoteToLocalEntries(context.getContentResolver(), Giving.class);
    }

    static List<Giving> getGiving(Context context) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Giving.class);
        
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<Giving> entries = getEntryListFromCursor(cursor, Giving.class);
        cursor.close();
        return entries;
    }

    static void addGiving(Context context, Giving... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, remote, Giving.class);
        
        addEntriesToLocal(local, Giving.class, entries);
        addEntriesToRemote(remote, Giving.class, entries);
    }

    static void removeGiving(Context context, Giving... giving) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, FirebaseDatabase.getInstance(), Giving.class);

        removeEntriesFromLocal(local, Giving.class, giving);
        removeEntriesFromRemote(remote, Giving.class, giving);
    }

    static void fetchRecord(Context context) {
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        context.getContentResolver().delete(contentUri, null, null);
        pullRemoteToLocalEntries(context.getContentResolver(), Record.class);
    }

    static List<Record> getRecord(Context context) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Record.class);

        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<Record> entries = getEntryListFromCursor(cursor, Record.class);
        cursor.close();
        return entries;
    }

    static void addRecord(Context context, Record... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, remote, Record.class);

        addEntriesToLocal(local, Record.class, entries);
        addEntriesToRemote(remote, Record.class, entries);
    }

    static void removeRecord(Context context, @Nullable Record... record) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, FirebaseDatabase.getInstance(), Record.class);

        removeEntriesFromLocal(local, Record.class, record);
        removeEntriesFromRemote(remote, Record.class, record);
    }

    static void fetchUser(Context context) {
        Uri contentUri = UserEntry.CONTENT_URI_USER;
        context.getContentResolver().delete(contentUri, null, null);
        pullRemoteToLocalEntries(context.getContentResolver(), User.class);
    }

    static List<User> getUser(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), User.class);

        Uri contentUri = UserEntry.CONTENT_URI_USER;
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<User> entries = getEntryListFromCursor(cursor, User.class);
        cursor.close();
        return entries;
    }

    static void addUser(Context context, User... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, remote, User.class);

        addEntriesToLocal(local, User.class, entries);
        addEntriesToRemote(remote, User.class, entries);
    }

    static void removeUser(Context context, @Nullable User... user) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, FirebaseDatabase.getInstance(), User.class);

        removeEntriesFromLocal(local, User.class, user);
        removeEntriesFromRemote(remote, User.class, user);
    }

    public static <T extends Entry> void cursorRowToEntry(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    public static <T extends Entry> List<T> getEntryListFromCursor(@NonNull Cursor cursor, Class<T> type) {
        List<T> entries = new ArrayList<>();
        if (!cursor.moveToFirst()) return entries;
        entries.clear();
        int i = 0;
        do {
            try { entries.add(type.newInstance());
            } catch (InstantiationException|IllegalAccessException e) { Timber.e(e); }
            cursorRowToEntry(cursor, entries.get(i++));
        } while (cursor.moveToNext());
        return entries;
    }

    public static <T extends Entry> void addEntriesToLocal(ContentResolver local, Class<T> entryType, T... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < values.length; i++) {values[i] = entries[i].toContentValues(); }
        local.bulkInsert(DatabaseContract.getContentUri(entryType), values);
    }

    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    static <T extends Entry> /*Task<Void>*/void addEntriesToRemote(FirebaseDatabase remote, Class<T> entryType, T... entries) {

        String rootPath = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(rootPath);

//        if (entries.length == 1) {
//            T entry = entries[0];
//            pathReference = pathReference.child(entry.getUid());
//            if (entry instanceof Company) pathReference = pathReference.child((entry.getId()));
//            pathReference.updateChildren(entry.toParameterMap());
//        } else {
//             TODO: Handle multiple entries with single update
            for (T entry: entries) {
                DatabaseReference childReference = pathReference.child(entry.getUid());
                if (entry instanceof Company) childReference = childReference.child(entry.getId());
                childReference.updateChildren(entry.toParameterMap());
            }
//            Map<String, Object> entryMap = new HashMap<>();
//            entryMap.put(entry.getId(), entry);
//            pathReference.updateChildren(entryMap);
//        }
    }

    static <T extends Entry> void removeEntriesFromLocal(ContentResolver local, Class<T> entryType, @Nullable T... entries) {

        Uri contentUri = DatabaseContract.getContentUri(entryType);
        if (entries == null || entries.length == 0) {
            local.delete(contentUri, null, null);
            return;
        }
        for (Entry entry : entries) {
            contentUri = contentUri.buildUpon().appendPath(String.valueOf(entry.getId())).build();
            local.delete(contentUri, null, null);
        }
    }

    static <T extends Entry> void removeEntriesFromRemote(FirebaseDatabase remote, Class<T> entryType, @Nullable T... entries) {

        DatabaseReference reference = remote.getReference(entryType.getSimpleName().toLowerCase());

        if (entries == null || entries.length == 0) {
            reference.removeValue();
            return;
        }
        for (T entry : entries) reference.child(entry.getUid()).child(entry.getId()).removeValue();
    }

    static <T extends Entry> void pullRemoteToLocalEntries(ContentResolver local, Class<T> entryType) {

        Uri uri = DatabaseContract.getContentUri(entryType);
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        String path = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(path);

        pathReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Giving giving = iterator.next().getValue(Giving.class);
                    local.insert(uri, giving.toContentValues());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

// TODO: Consider adding entry parameter to all fetch methods to prevent additional cursor query
    static <T extends Entry> void validateEntries(ContentResolver local, FirebaseDatabase remote, Class<T> entryType) {

        DatabaseReference reference = remote.getReference(User.class.getSimpleName().toLowerCase());
        reference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long localUpdateTime = 0;
                long remoteUpdateTime = 0;

                Cursor cursor = local.query(UserEntry.CONTENT_URI_USER, null, null, null, null);
                if (cursor != null) {
                    List<User> localUsers = getEntryListFromCursor(cursor, User.class);
                    cursor.close();
                    for (User user : localUsers)
                        if (user.getActive())
                            localUpdateTime = DatabaseContract.getTableTime(entryType, user);
                }

                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    User user = iterator.next().getValue(User.class);
                    if (user != null && user.getActive()) remoteUpdateTime = DatabaseContract.getTableTime(entryType, user);
                    if (localUpdateTime < remoteUpdateTime) {
                        pullRemoteToLocalEntries(local, entryType);
                    } else if (localUpdateTime > remoteUpdateTime) {
                        remote.getReference(entryType.getSimpleName().toLowerCase()).removeValue();
                        cursor = local.query(DatabaseContract.getContentUri(entryType), null, null, null, null);
                        if (cursor != null) {
                            List<T> entryList = getEntryListFromCursor(cursor, entryType);
                            cursor.close();
                            T[] entries = (T[]) Array.newInstance(entryType, entryList.size());
                            for (int i = 0; i < entries.length; i++) entries[i] = entryList.get(i);
                            addEntriesToRemote(FirebaseDatabase.getInstance(), entryType, entries);
                        } else remote.getReference(entryType.getSimpleName().toLowerCase()).removeValue();
                    } else return;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Generates a {@link User} from {@link SharedPreferences} and {@link FirebaseUser} attributes.
     */
    public static User convertRemoteToLocalUser(FirebaseUser firebaseUser) {

        User user = User.getDefault();
        user.setUid(firebaseUser == null ? "" : firebaseUser.getUid());
        user.setEmail(firebaseUser == null ? "" : firebaseUser.getEmail());
        user.setActive(true);
        return user;
    }


    /**
     * Builds the proper {@link Uri} for requesting movie data.
     * Users must register and reference a unique API key.
     * API keys are available at http://api.charitynavigator.org/
     * @return {@link Uri} for requesting data from the API service.
     */
    private static URL getUrl(Uri uri) {
        URL url = null;
        try {
            String urlStr = URLDecoder.decode(uri.toString(), "UTF-8");
            url = new URL(urlStr);
            Timber.v("Fetch URL: %s", url.toString());
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            Timber.e("Unable to convert Uri of %s to URL:", e.getMessage());
        }
        return url;
    }

    /**
     * Returns the result of the HTTP request.
     * @param url address from which to fetch the HTTP response.
     * @return the result of the HTTP request; null if none received.
     * @throws IOException caused by network and stream reading.
     */
    private static String requestResponseFromUrl(URL url) {

        HttpURLConnection urlConnection = null;
        String response = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) response = scanner.next();
            scanner.close();
            Timber.v("Fetched Response: %s", response);
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return response;
    }

    /**
     * This method parses JSON String of data API response and returns array of {@link Search}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static Search[] parseSearches(@NonNull String jsonResponse, String uid, boolean single) {

        Search[] searches = null;
        try {
            if (single) {
                searches = new Search[1];
                searches[0] = parseSearch(new JSONObject(jsonResponse),uid);
                Timber.v("Parsed Response: %s", searches[0].toString());
            } else {
                JSONArray charityArray = new JSONArray(jsonResponse);
                searches = new Search[charityArray.length()];
                for (int i = 0; i < charityArray.length(); i++) {
                    JSONObject charityObject = charityArray.getJSONObject(i);
                    Search search = parseSearch(charityObject, uid);
                    searches[i] = search;
                    Timber.v("Parsed Response: %s", search.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e);
        }
        return searches;
    }

    /**
     * This method parses JSONObject of JSONArray and returns {@link Search}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static Search parseSearch(JSONObject charityObject, String uid) throws JSONException {

        JSONObject locationObject = charityObject.getJSONObject(FetchContract.KEY_LOCATION);
        String ein = charityObject.getString(FetchContract.KEY_EIN);
        String name = charityObject.getString(FetchContract.KEY_CHARITY_NAME);
        String street = locationObject.getString(FetchContract.KEY_STREET_ADDRESS);
        String detail = locationObject.getString(FetchContract.KEY_ADDRESS_DETAIL);
        String city = locationObject.getString(FetchContract.KEY_CITY);
        String state = locationObject.getString(FetchContract.KEY_STATE);
        String zip = locationObject.getString(FetchContract.KEY_POSTAL_CODE);
        String homepageUrl = charityObject.getString(FetchContract.KEY_WEBSITE_URL);
        String navigatorUrl = charityObject.getString(FetchContract.KEY_CHARITY_NAVIGATOR_URL);

        return new Search(uid, ein, System.currentTimeMillis(), name, street, detail, city, state, zip, homepageUrl, navigatorUrl, "", "", "0", 0);
    }

    /**
     * Converts a null value returned from API response to default value.
     */
    private static String nullToDefaultStr(String str) {
        return (str.equals("null")) ? DEFAULT_VALUE_STR : str;
    }

    public static final String DEFAULT_VALUE_STR = "";
    public static final int DEFAULT_VALUE_INT = -1;

    /**
     * Defines the request and response path and parameters for the data API service.
     */
    public static final class FetchContract {

        private static final String BASE_URL = "https://api.data.charitynavigator.org/v2";
        static final String API_PATH_ORGANIZATIONS = "Organizations";

        // Query parameters
        public static final String PARAM_APP_ID = "app_id";
        public static final String PARAM_APP_KEY = "app_key";
        public static final String PARAM_EIN = "ein";
        public static final String PARAM_PAGE_NUM = "pageNum";
        public static final String PARAM_PAGE_SIZE = "pageSize";
        public static final String PARAM_SEARCH = "search";
        public static final String PARAM_SEARCH_TYPE ="searchType";
        public static final String PARAM_RATED = "rated";
        public static final String PARAM_CATEGORY_ID = "categoryID";
        public static final String PARAM_CAUSE_ID = "causeID";
        public static final String PARAM_FILTER = "fundraisingOrgs";
        public static final String PARAM_STATE = "state";
        public static final String PARAM_CITY = "city";
        public static final String PARAM_ZIP = "zip";
        public static final String PARAM_MIN_RATING = "minRating";
        public static final String PARAM_MAX_RATING = "maxRating";
        public static final String PARAM_SIZE_RANGE = "sizeRange";
        public static final String PARAM_DONOR_PRIVACY = "donorPrivacy";
        public static final String PARAM_SCOPE_OF_WORK = "scopeOfWork";
        public static final String PARAM_CFC_CHARITIES = "cfcCharities";
        public static final String PARAM_NO_GOV_SUPPORT = "noGovSupport";
        public static final String PARAM_SORT = "sort";

        // Response keys
        private static final String KEY_EIN = "ein";
        private static final String KEY_CHARITY_NAME = "charityName";
        private static final String KEY_LOCATION = "mailingAddress";
        private static final String KEY_STREET_ADDRESS = "streetAddress1";
        private static final String KEY_ADDRESS_DETAIL = "streetAddress2";
        private static final String KEY_CITY = "city";
        private static final String KEY_STATE = "stateOrProvince";
        private static final String KEY_POSTAL_CODE = "postalCode";
        private static final String KEY_WEBSITE_URL = "websiteURL";
        private static final String KEY_PHONE_NUMBER = "phoneNumber";
        private static final String KEY_EMAIL_ADDRESS = "generalEmail";
        private static final String KEY_CURRENT_RATING = "currentRating";
        private static final String KEY_RATING = "rating";
        private static final String KEY_ADVISORIES = "advisories";
        private static final String KEY_SEVERITY = "severity";
        private static final String KEY_CHARITY_NAVIGATOR_URL = "charityNavigatorURL";
        private static final String KEY_ERROR_MESSAGE = "errorMessage";

        private static final String[] OPTIONAL_PARAMS = {
                PARAM_PAGE_NUM,
                PARAM_PAGE_SIZE,
                PARAM_SEARCH,
                PARAM_SEARCH_TYPE,
                PARAM_RATED,
                PARAM_CATEGORY_ID,
                PARAM_CAUSE_ID,
                PARAM_FILTER,
                PARAM_STATE,
                PARAM_CITY,
                PARAM_ZIP,
                PARAM_MIN_RATING,
                PARAM_MAX_RATING,
                PARAM_SIZE_RANGE,
                PARAM_DONOR_PRIVACY,
                PARAM_SCOPE_OF_WORK,
                PARAM_CFC_CHARITIES,
                PARAM_NO_GOV_SUPPORT,
                PARAM_SORT
        };
    }
}