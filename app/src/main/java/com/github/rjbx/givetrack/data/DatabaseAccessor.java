package com.github.rjbx.givetrack.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.concurrent.ExecutionException;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Company;
import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Spawn;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
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
import androidx.core.util.Pair;
import timber.log.Timber;

public final class DatabaseAccessor {

    static void fetchSpawn(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        User user =  getActiveUserFromLocal(local);
        
        Map<String, String> request = new HashMap<>();
        if (user.getIndexFocus()) request.put(DatabaseAccessor.FetchContract.PARAM_EIN, user.getIndexCompany());
        else {
            request.put(DatabaseAccessor.FetchContract.PARAM_SPAWN, user.getIndexTerm());
            request.put(DatabaseAccessor.FetchContract.PARAM_CITY, user.getIndexCity());
            request.put(DatabaseAccessor.FetchContract.PARAM_STATE, user.getIndexState());
            request.put(DatabaseAccessor.FetchContract.PARAM_ZIP, user.getIndexZip());
            request.put(DatabaseAccessor.FetchContract.PARAM_MIN_RATING, user.getIndexMinrating());
            request.put(DatabaseAccessor.FetchContract.PARAM_FILTER, user.getIndexFilter() ? "1" : "0");
            request.put(DatabaseAccessor.FetchContract.PARAM_SORT, user.getIndexSort() + ":" + user.getIndexOrder());
            request.put(DatabaseAccessor.FetchContract.PARAM_PAGE_NUM, user.getIndexPages());
            request.put(DatabaseAccessor.FetchContract.PARAM_PAGE_SIZE, user.getIndexRows());
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
        String response = requestResponseFromUrl(url, null);
        if (response == null) return;
        Spawn[] parsedResponse = parseSpawns(response, user.getUid(), single);

        // Store data
        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, Spawn.class, stamp);
        addEntriesToLocal(local, Spawn.class, stamp, false, parsedResponse);
        addEntriesToRemote(remote, Spawn.class, stamp, false, parsedResponse);
    }

    @SafeVarargs static List<Spawn> getSpawn(Context context, Pair<String, String>... where) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = CompanyEntry.CONTENT_URI_SPAWN;

        String selection = CompanyEntry.COLUMN_UID + " = ? ";
        List<String> selectionArgList = new ArrayList<>();
        selectionArgList.add(activeUser.getUid());

        if (where != null) {
            for (Pair<String, String> p : where) {
                selection = selection.concat(", " + p.first);
                selectionArgList.add(p.second);
            }
        }
        String[] selectionArgs = selectionArgList.toArray(new String[0]);

        Cursor cursor = local.query(
                contentUri, null, selection, selectionArgs, null
        );
        List<Spawn> entries = getEntryListFromCursor(cursor, Spawn.class);
        if (cursor != null) cursor.close();
        return entries;
    }

    static void addSpawn(Context context, Spawn... entries) {
        ContentResolver local = context.getContentResolver();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, Spawn.class, stamp, false, entries);
    }

    static void removeSpawn(Context context, Spawn... spawns) {
        ContentResolver local = context.getContentResolver();

        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, Spawn.class, stamp, spawns);
    }

    static void fetchTarget(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        validateEntries(local, remote, Target.class);
    }

    @SafeVarargs static List<Target> getTarget(Context context, Pair<String, String>... where) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = CompanyEntry.CONTENT_URI_TARGET;

        String selection = CompanyEntry.COLUMN_UID + " = ? ";
        List<String> selectionArgList = new ArrayList<>();
        selectionArgList.add(activeUser.getUid());

        if (where != null) {
            for (Pair<String, String> p : where) {
                selection = selection.concat("AND " + p.first);
                selectionArgList.add(p.second);
            }
        }
        String[] selectionArgs = selectionArgList.toArray(new String[0]);

        Cursor cursor = local.query(
                contentUri, null, selection, selectionArgs, null
        );
        List<Target> entries = getEntryListFromCursor(cursor, Target.class);
        if (cursor != null) cursor.close();
        return entries;
    }

    static void addTarget(Context context, Target... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, Target.class, stamp, false, entries);
        addEntriesToRemote(remote, Target.class, stamp, false, entries);
    }

    static void removeTarget(Context context, Target... target) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();

        // TODO Update recalibration with Rateraid
        List<Target> targetList = getTarget(context);
        int[] removalIndeces = new int[targetList.size()];
        for (Target t1 : target) for (Target t2 : targetList)
            if (t2.getId().equals(t1.getId()))
                removalIndeces[targetList.indexOf(t2)] = 1;
        for (int i = 0; i < removalIndeces.length; i++)
            if (removalIndeces[i] == 1) targetList.remove(i);

        if (!targetList.isEmpty()) {
            Iterator<Target> iterator = targetList.iterator();
            do iterator.next().setPercent(1d / targetList.size());
            while (iterator.hasNext());
            target = targetList.toArray(new Target[0]);
        }

        addEntriesToLocal(local, Target.class, stamp, true, target);
        addEntriesToRemote(remote, Target.class, stamp, true, target);
    }

    static void fetchRecord(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        validateEntries(local, remote, Record.class);
    }

    @SafeVarargs static List<Record> getRecord(Context context, Pair<String, String>... where) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;

        String selection = CompanyEntry.COLUMN_UID + " = ? ";
        List<String> selectionArgList = new ArrayList<>();
        selectionArgList.add(activeUser.getUid());

        if (where != null) {
            for (Pair<String, String> p : where) {
                selection = selection.concat(", " + p.first);
                selectionArgList.add(p.second);
            }
        }
        String[] selectionArgs = selectionArgList.toArray(new String[0]);

        Cursor cursor = local.query(
                contentUri, null, selection, selectionArgs, null
        );
        List<Record> entries = getEntryListFromCursor(cursor, Record.class);

        if (cursor != null) cursor.close();
        return entries;
    }

    static void addRecord(Context context, Record... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, Record.class, stamp, false, entries);
        addEntriesToRemote(remote, Record.class, stamp, false, entries);
    }

    static void removeRecord(Context context, Record... record) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, Record.class, stamp, record);
        removeEntriesFromRemote(remote, Record.class, stamp,  record);
    }

    static void fetchUser(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        validateEntries(local, remote, User.class);
    }

    static List<User> getUser(Context context) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = UserEntry.CONTENT_URI_USER;
        Cursor cursor = local.query(
                contentUri, null, CompanyEntry.COLUMN_UID + " = ? ", new String[] { activeUser.getUid() }, null
        );
        List<User> entries = getEntryListFromCursor(cursor, User.class);
        if (cursor != null) cursor.close();
        return entries;
    }

    static void addUser(Context context, User... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, User.class, stamp, false, entries);
        addEntriesToRemote(remote, User.class, stamp, false, entries);
    }

    static void removeUser(Context context, User... user) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, User.class, stamp, user);
        removeEntriesFromRemote(remote, User.class, stamp, user);
    }

    public static <T extends Entry> void cursorRowToEntry(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    public static <T extends Entry> List<T> getEntryListFromCursor(Cursor cursor, Class<T> type) {
        List<T> entries = new ArrayList<>();
        if (cursor == null || !cursor.moveToFirst()) return entries;
        entries.clear();
        int i = 0;
        do {
            try { entries.add(type.newInstance());
            } catch (InstantiationException|IllegalAccessException e) { Timber.e(e); }
            cursorRowToEntry(cursor, entries.get(i++));
        } while (cursor.moveToNext());
        return entries;
    }

    @SafeVarargs private static <T extends Entry> void addEntriesToLocal(ContentResolver local, Class<T> entryType, long stamp, boolean reset, T... entries) {

        Uri contentUri = DatabaseContract.getContentUri(entryType);

        String uid =  entries == null || entries.length == 0 ?
                getActiveUserFromLocal(local).getUid() : entries[0].getUid();

        if (reset) local.delete(contentUri, UserEntry.COLUMN_UID + " = ? ", new String[] { uid });

        if (entries != null) {
            ContentValues[] values = new ContentValues[entries.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = entries[i].toContentValues();
            }
            local.bulkInsert(contentUri, values);
        }

        updateLocalTableTime(local, entryType, stamp, uid);
    }

    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    @SafeVarargs private static <T extends Entry> void addEntriesToRemote(FirebaseDatabase remote, Class<T> entryType, long stamp,  boolean reset, T... entries) {

        if (entryType.equals(Spawn.class)) return;

        String entryPath = entryType.getSimpleName().toLowerCase();
        DatabaseReference entryReference = remote.getReference(entryPath);

        String uid = entries == null || entries.length == 0 ?
                getActiveUserFromRemote(remote).getUid() : entries[0].getUid();

        DatabaseReference childReference = entryReference.child(uid);

        if (reset) childReference.removeValue();

        if (entries != null) {
            for (T entry : entries) {
                if (entry instanceof Company) childReference = childReference.child(entry.getId());
                childReference.updateChildren(entry.toParameterMap());
            }
        }

        updateRemoteTableTime(remote, entryType, stamp, uid);
    }

    @SafeVarargs private static <T extends Entry> void removeEntriesFromLocal(ContentResolver local, Class<T> entryType, long stamp, T... entries) {

        Uri contentUri = DatabaseContract.getContentUri(entryType);
        String uid;
        if (entries == null || entries.length == 0) {
            uid = getActiveUserFromLocal(local).getUid();
            local.delete(contentUri, UserEntry.COLUMN_UID + " = ?", new String[] { uid });
        } else {
            uid = entries[0].getUid();
            for (Entry entry : entries) {
                Uri rowUri = contentUri.buildUpon().appendPath(String.valueOf(entry.getId())).build();
                local.delete(rowUri, null, null);
            }
        } updateLocalTableTime(local, entryType, stamp, uid);
    }

    @SafeVarargs private static <T extends Entry> void removeEntriesFromRemote(FirebaseDatabase remote, Class<T> entryType, long stamp, T... entries) {

        if (entryType.equals(Spawn.class)) return;

        String entryPath = entryType.getSimpleName().toLowerCase();
        DatabaseReference entryReference = remote.getReference(entryPath);
        String uid;
        if (entries == null || entries.length == 0) {
            User user = getActiveUserFromRemote(remote);
            uid = user.getUid();
            DatabaseReference childReference = entryReference.child(uid);
            childReference.removeValue();
        } else {
            uid = entries[0].getUid();
            for (T entry : entries) {
                DatabaseReference childReference = entryReference.child(uid);
                if (entry instanceof Company) childReference = childReference.child(entry.getId());
                childReference.removeValue();
            }
        } updateRemoteTableTime(remote, entryType, stamp, uid);
    }

    private static User getActiveUserFromLocal(ContentResolver local) {
        User u = User.getDefault();
        u.setUserStamp(-1);
        u.setTargetStamp(-1);
        u.setRecordStamp(-1);
        Cursor data = local.query(UserEntry.CONTENT_URI_USER, null, null, null, null);
        if (data == null) return u;
        if (data.moveToFirst()) {
            do {
                User user = User.getDefault();
                DatabaseAccessor.cursorRowToEntry(data, user);
                if (user.getUserActive()) return user;
            } while (data.moveToNext());
        } return u;
    }

    private static User getActiveUserFromRemote(FirebaseDatabase remote) {

        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();

        DatabaseReference entryReference = remote.getReference(User.class.getSimpleName().toLowerCase());
        entryReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : iterable) {
                    User u = snapshot.getValue(User.class);
                    if (u != null && u.getUserActive()) taskSource.trySetResult(u);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        Task<User> task = taskSource.getTask();
        try { Tasks.await(task); }
        catch (ExecutionException|InterruptedException e) { task = Tasks.forException(e); }

        User u = User.getDefault();
        u.setUserStamp(-1);
        u.setTargetStamp(-1);
        u.setRecordStamp(-1);
        if (task.isSuccessful()) u = task.getResult();
        return u;
    }

    private static <T extends Entry> void updateLocalTableTime(ContentResolver local, Class<T> entryType, long stamp, String uid) {

        if (entryType.equals(Spawn.class)) return;

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.getTimeTableColumn(entryType), stamp);

        Uri uri = UserEntry.CONTENT_URI_USER.buildUpon().appendPath(uid).build();
        local.update(uri, values, null,null);
    }

    private static <T extends Entry> void updateRemoteTableTime(FirebaseDatabase remote, Class<T> entryType, long stamp, String uid) {

        Map<String, Object> map = new HashMap<>();
        map.put(DatabaseContract.getTimeTableColumn(entryType), stamp);

        DatabaseReference userReference = remote.getReference(User.class.getSimpleName().toLowerCase());
        userReference.child(uid).updateChildren(map);
    }

    private static <T extends Entry> void pullLocalToRemoteEntries(ContentResolver local, FirebaseDatabase remote, Class<T> entryType, long stamp) {
        Uri contentUri = DatabaseContract.getContentUri(entryType);
        Cursor cursor = local.query(contentUri, null, null, null, null);
        if (cursor == null) return;
        List<T> entryList = getEntryListFromCursor(cursor, entryType);
        cursor.close();
        removeEntriesFromRemote(remote, entryType, stamp);
        if (entryList.isEmpty()) return;
        addEntriesToRemote(remote, entryType, stamp, false, entryList.toArray((T[]) Array.newInstance(entryType, entryList.size())));
    }

    private static <T extends Entry> void pullRemoteToLocalEntries(ContentResolver local, FirebaseDatabase remote, Class<T> entryType, long stamp, String uid) {

        String path = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(path);
        if (entryType == Record.class || entryType == Target.class) pathReference = pathReference.child(uid);
        pathReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                List<T> entryList = new ArrayList<>();
                for (DataSnapshot snapshot : iterable) {
                    T entry = snapshot.getValue(entryType);
                    if (entry instanceof User) {
                        ((User) entry).setRecordStamp(0);
                        ((User) entry).setTargetStamp(0);
                    }
                    entryList.add(entry);
                }
                removeEntriesFromLocal(local, entryType, stamp);
                if (entryList.isEmpty()) return;
                addEntriesToLocal(local, entryType, stamp, false, entryList.toArray((T[]) Array.newInstance(entryType, entryList.size())));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private static <T extends Entry> void validateEntries(@NonNull ContentResolver local, @NonNull FirebaseDatabase remote, Class<T> entryType) {

        User localUser = getActiveUserFromLocal(local);
        User remoteUser = getActiveUserFromRemote(remote);

        long localTableStamp = DatabaseContract.getTableTime(entryType, localUser);
        long remoteTableStamp = DatabaseContract.getTableTime(entryType, remoteUser);
        int compareLocalToRemote = Long.compare(localTableStamp, remoteTableStamp);

        if (compareLocalToRemote > 0) pullLocalToRemoteEntries(local, remote, entryType, localTableStamp);
        else if (compareLocalToRemote < 0) pullRemoteToLocalEntries(local, remote, entryType, remoteTableStamp, remoteUser.getUid());
        else local.notifyChange(DatabaseContract.getContentUri(entryType), null);
    }

    /**
     * Generates a {@link User} from {@link SharedPreferences} and {@link FirebaseUser} attributes.
     */
    public static User convertRemoteToLocalUser(FirebaseUser firebaseUser) {

        User user = User.getDefault();
        user.setUid(firebaseUser == null ? "" : firebaseUser.getUid());
        user.setUserEmail(firebaseUser == null ? "" : firebaseUser.getEmail());
        user.setUserActive(true);
        return user;
    }

//    private static String urlToCompanyData(Context context, String homepageUrlStr) {
//        String formattedUrl = parsedResponse[0].getHomepageUrl().split("www.")[1].replace("/", "");
//        String clearbitUrlStr = "https://api.com/v1/enrichment/domain=" + homepageUrlStr
//        URL clearbitURL = getUrl(new Uri.Builder().path(clearbitUrlStr).build());
//        return requestResponseFromUrl(clearbitURL, context.getString(R.string.cb_api_key));
//    }


    // TODO Consider moving below methods

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
     */
    private static String requestResponseFromUrl(URL url, @Nullable String password) {

        HttpURLConnection urlConnection = null;
        String response = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
//            TODO: Replace network API with Retrofit
//
//            if (password != null) {
//                urlConnection.setRequestProperty("Content-Type", "application/json");
//                urlConnection.setDoOutput(true);
//                urlConnection.setRequestMethod("PUT");
//
//                String credential = "Basic " + new String(Base64.encode(password.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
//                urlConnection.setRequestProperty("Authorization", credential);
//
//                String format = "{\"format\":\"json\",\"pattern\":\"#\"}";
//                OutputStreamWriter oStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
//                oStreamWriter.write(format);
//                oStreamWriter.close();
//            }
            InputStreamReader in = new InputStreamReader(urlConnection.getInputStream());

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
     * This method parses JSON String of data API response and returns array of {@link Spawn}.
     */
    private static Spawn[] parseSpawns(@NonNull String jsonResponse, String uid, boolean single) {

        Spawn[] spawns = null;
        try {
            if (single) {
                spawns = new Spawn[1];
                spawns[0] = parseSpawn(new JSONObject(jsonResponse),uid);
                Timber.v("Parsed Response: %s", spawns[0].toString());
            } else {
                JSONArray charityArray = new JSONArray(jsonResponse);
                spawns = new Spawn[charityArray.length()];
                for (int i = 0; i < charityArray.length(); i++) {
                    JSONObject charityObject = charityArray.getJSONObject(i);
                    Spawn spawn = parseSpawn(charityObject, uid);
                    spawns[i] = spawn;
                    Timber.v("Parsed Response: %s", spawn.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e);
        }
        return spawns;
    }

    /**
     * This method parses JSONObject of JSONArray and returns {@link Spawn}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static Spawn parseSpawn(JSONObject charityObject, String uid) throws JSONException {

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

        return new Spawn(uid, ein, System.currentTimeMillis(), name, street, detail, city, state, zip, homepageUrl, navigatorUrl, "", "", "", "0", 0);
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
        public static final String PARAM_SPAWN = "spawn";
        public static final String PARAM_SPAWN_TYPE ="spawnType";
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
                PARAM_SPAWN,
                PARAM_SPAWN_TYPE,
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