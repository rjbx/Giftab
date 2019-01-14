package com.github.rjbx.givetrack.data;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import com.github.rjbx.givetrack.AppExecutors;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.AppWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Handles asynchronous task requests in a service on a separate handler thread.
 */
public class DataService extends IntentService {

    private static final Executor DISK_IO = AppExecutors.getInstance().getDiskIO();
    private static final Executor NETWORK_IO = AppExecutors.getInstance().getNetworkIO();

    private static final String ACTION_FETCH_GENERATED = "com.github.rjbx.givetrack.data.action.FETCH_GENERATED";
    private static final String ACTION_FETCH_COLLECTED = "com.github.rjbx.givetrack.data.action.FETCH_COLLECTED";
    private static final String ACTION_COLLECT_GENERATED = "com.github.rjbx.givetrack.data.action.COLLECT_GENERATED";
    private static final String ACTION_REMOVE_GENERATED = "com.github.rjbx.givetrack.data.action.REMOVE_GENERATED";
    private static final String ACTION_REMOVE_COLLECTED = "com.github.rjbx.givetrack.data.action.REMOVE_COLLECTED";
    private static final String ACTION_RESET_GENERATED = "com.github.rjbx.givetrack.data.action.RESET_GENERATED";
    private static final String ACTION_RESET_COLLECTED = "com.github.rjbx.givetrack.data.action.RESET_COLLECTED";
    private static final String ACTION_UPDATE_FREQUENCY = "com.github.rjbx.givetrack.data.action.UPDATE_FREQUENCY";
    private static final String ACTION_UPDATE_PERCENTAGES = "com.github.rjbx.givetrack.data.action.UPDATE_PERCENTAGES";
    private static final String ACTION_RESET_DATA = "com.github.rjbx.givetrack.data.action.RESET_DATA";

    private static final String EXTRA_API_REQUEST = "com.github.rjbx.givetrack.data.extra.API_REQUEST";
    private static final String EXTRA_ITEM_VALUES = "com.github.rjbx.givetrack.data.extra.ITEM_VALUES";
    private static final String EXTRA_LIST_VALUES = "com.github.rjbx.givetrack.data.extra.LIST_VALUES";
    private static final String EXTRA_ITEM_ID = "com.github.rjbx.givetrack.data.extra.ITEM_ID";

    public static final String DEFAULT_VALUE_STR = "";
    public static final int DEFAULT_VALUE_INT = -1;

    /**
     * Creates an {@link IntentService} instance.
     */
    public DataService() { super(DataService.class.getSimpleName()); }

    /**
     * Starts this service to perform action FetchGenerated with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchGenerated(Context context, HashMap<String, String> apiRequest) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_FETCH_GENERATED);
        intent.putExtra(EXTRA_API_REQUEST, apiRequest);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action FetchCollected with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchCollected(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_FETCH_COLLECTED);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action CollectGenerated with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCollectGenerated(Context context, String charityId) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_COLLECT_GENERATED);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveGenerated with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveGenerated(Context context, String charityId) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_REMOVE_GENERATED);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveCollected with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveCollected(Context context, String charityId) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_REMOVE_COLLECTED);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetGenerated with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetGenerated(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_RESET_GENERATED);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetCollected with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetCollected(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_RESET_COLLECTED);
        context.startService(intent);
    }
    
    /**
     * Starts this service to perform action UpdateFrequency with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateFrequency(Context context, ContentValues charityValues) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_UPDATE_FREQUENCY);
        intent.putExtra(EXTRA_ITEM_VALUES, charityValues);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdatePercentages with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdatePercentages(Context context, ContentValues... charityValues) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_UPDATE_PERCENTAGES);
        if (charityValues.length > 1) {
            ArrayList<ContentValues> charityValuesArrayList = new ArrayList<>(Arrays.asList(charityValues));
            intent.putParcelableArrayListExtra(EXTRA_LIST_VALUES, charityValuesArrayList);
        } else intent.putExtra(EXTRA_ITEM_VALUES, charityValues[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetData with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetData(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_RESET_DATA);
        context.startService(intent);
    }

    /**
     * Syncs data inside a worker thread on requests to process {@link Intent}.
     * @param intent launches this {@link IntentService}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        final String action = intent.getAction();
        switch (action) {
            case ACTION_FETCH_GENERATED:
                final HashMap fetchGeneratedMap = (HashMap) intent.getSerializableExtra(EXTRA_API_REQUEST);
                handleActionFetchGenerated(fetchGeneratedMap);
                break;
            case ACTION_FETCH_COLLECTED:
                handleActionFetchCollected();
                break;
            case ACTION_COLLECT_GENERATED:
                final String collectGeneratedString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionCollectGenerated(collectGeneratedString);
                break;
            case ACTION_REMOVE_GENERATED:
                final String removeGeneratedString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionRemoveGenerated(removeGeneratedString);
                break;
            case ACTION_REMOVE_COLLECTED:
                final String removeCollectedString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionRemoveCollected(removeCollectedString);
                break;
            case ACTION_RESET_GENERATED:
                handleActionResetGenerated();
                break;
            case ACTION_RESET_COLLECTED:
                handleActionResetCollected();
                break;
            case ACTION_UPDATE_FREQUENCY:
                final ContentValues updateFrequencyValues = intent.getParcelableExtra(EXTRA_ITEM_VALUES);
                handleActionUpdateFrequency(updateFrequencyValues);
                break;
            case ACTION_UPDATE_PERCENTAGES:
                if (intent.hasExtra(EXTRA_LIST_VALUES)) {
                    final ArrayList<ContentValues> updatePercentagesValuesArray = intent.getParcelableArrayListExtra(EXTRA_LIST_VALUES);
                    handleActionUpdatePercentages(updatePercentagesValuesArray.toArray(new ContentValues[updatePercentagesValuesArray.size()]));
                } else {
                    ContentValues updatePercentagesValues = intent.getParcelableExtra(EXTRA_ITEM_VALUES);
                    handleActionUpdatePercentages(updatePercentagesValues);
                }
                break;
            case ACTION_RESET_DATA: handleActionResetData();
        }
    }

    /**
     * Handles action FetchGenerated in the provided background thread with the provided parameters.
     */
    private void handleActionFetchGenerated(HashMap apiRequest) {

        Uri.Builder builder = Uri.parse(FetchContract.BASE_URL).buildUpon();
        builder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);

        // Append required parameters
        builder.appendQueryParameter(FetchContract.PARAM_APP_ID, getString(R.string.cn_app_id));
        builder.appendQueryParameter(FetchContract.PARAM_APP_KEY, getString(R.string.cn_app_key));

        boolean single = apiRequest.containsKey(FetchContract.PARAM_EIN);
        if (single) builder.appendPath((String) apiRequest.get(FetchContract.PARAM_EIN));
        else {
            // Append optional parameters
            for (String param : FetchContract.OPTIONAL_PARAMS) {
                if (apiRequest.containsKey(param)) {
                    String value = (String) apiRequest.get(param);
                    if (value != null && !value.equals(""))
                        builder.appendQueryParameter(param, value);
                }
            }
        }

        URL url = getUrl(builder.build());
        NETWORK_IO.execute(() -> {
            // Retrieve data
            String response = requestResponseFromUrl(url);
            if (response == null) return;
            ContentValues[] parsedResponse = parseJsonResponse(response, single);

            // Store data
            getContentResolver().delete(GivetrackContract.Entry.CONTENT_URI_GENERATION, null, null);
            getContentResolver().bulkInsert(GivetrackContract.Entry.CONTENT_URI_GENERATION, parsedResponse);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action FetchCollected in the provided background thread.
     */
    private void handleActionFetchCollected() {

        Uri.Builder templateBuilder = Uri.parse(FetchContract.BASE_URL).buildUpon();
        templateBuilder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);
        Uri template = templateBuilder.build();

        List<String> charities = UserPreferences.getCharities(this);
        if (charities.get(0).isEmpty()) return;

        int charityCount = charities.size();
        ContentValues[] contentValuesArray = new ContentValues[charityCount];

        NETWORK_IO.execute(() -> {
            for (int i = 0; i < charityCount; i++) {

                String[] charityData = charities.get(i).split(":");
                Uri.Builder charityBuilder = Uri.parse(template.toString()).buildUpon();

                charityBuilder.appendPath(charityData[0]);

                // Append required parameters
                charityBuilder.appendQueryParameter(FetchContract.PARAM_APP_ID, getString(R.string.cn_app_id));
                charityBuilder.appendQueryParameter(FetchContract.PARAM_APP_KEY, getString(R.string.cn_app_key));

                Uri charityUri = charityBuilder.build();

                URL url = getUrl(charityUri);
                Timber.e("Collection Fetched URL: %s", url);
                String response = requestResponseFromUrl(url);
                Timber.e("Collection Fetched Response: %s", response);
                ContentValues[] parsedResponse = parseJsonResponse(response, true);
                parsedResponse[0].put(GivetrackContract.Entry.COLUMN_PHONE_NUMBER, charityData[1]);
                parsedResponse[0].put(GivetrackContract.Entry.COLUMN_EMAIL_ADDRESS, charityData[2]);
                parsedResponse[0].put(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE, charityData[3]);
                parsedResponse[0].put(GivetrackContract.Entry.COLUMN_DONATION_IMPACT, charityData[4]);
                parsedResponse[0].put(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY, charityData[5]);
                contentValuesArray[i] = parsedResponse[0];
            }

            getContentResolver().delete(GivetrackContract.Entry.CONTENT_URI_COLLECTION, null, null);
            getContentResolver().bulkInsert(GivetrackContract.Entry.CONTENT_URI_COLLECTION, contentValuesArray);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action CollectGenerated in the provided background thread with the provided parameters.
     */
    private void handleActionCollectGenerated(String charityId) {
        Uri charityUri = GivetrackContract.Entry.CONTENT_URI_GENERATION.buildUpon().appendPath(charityId).build();

        NETWORK_IO.execute(() -> {

            Cursor cursor = getContentResolver().query(charityUri, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) return;
            cursor.moveToFirst();
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, values);

            List<String> charities = UserPreferences.getCharities(this);

            values.put(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE, charities.isEmpty() ? "1" : "0");
            values.put(GivetrackContract.Entry.COLUMN_DONATION_IMPACT, "0");
            values.put(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY, 0);

            String navUrl = cursor.getString(GivetrackContract.Entry.INDEX_NAVIGATOR_URL);
            String phoneNumber = urlToPhoneNumber(navUrl);
            values.put(GivetrackContract.Entry.COLUMN_PHONE_NUMBER, phoneNumber);

            String orgUrl = cursor.getString(GivetrackContract.Entry.INDEX_HOMEPAGE_URL);
            String emailAddress = urlToEmailAddress(orgUrl);
            values.put(GivetrackContract.Entry.COLUMN_EMAIL_ADDRESS, emailAddress);

            if (charities.get(0).isEmpty()) charities = new ArrayList<>();
            String ein = cursor.getString(GivetrackContract.Entry.INDEX_EIN);
            charities.add(String.format(Locale.getDefault(),"%s:%s:%s:%f:%f:%d", ein, phoneNumber, emailAddress, 0f, 0f, 0));

            UserPreferences.setCharities(this, charities);
            UserPreferences.updateFirebaseUser(this);
            getContentResolver().insert(GivetrackContract.Entry.CONTENT_URI_COLLECTION, values);
            cursor.close();

        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action RemoveGenerated in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveGenerated(String charityId) {
        Uri charityUri = GivetrackContract.Entry.CONTENT_URI_GENERATION.buildUpon().appendPath(charityId).build();
        DISK_IO.execute(() -> getContentResolver().delete(charityUri, null, null));

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action RemoveCollected in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveCollected(String charityId) {

        Uri charityUri = GivetrackContract.Entry.CONTENT_URI_COLLECTION.buildUpon().appendPath(charityId).build();
        DISK_IO.execute(() -> getContentResolver().delete(charityUri, null, null));

        Cursor cursor = getContentResolver().query(GivetrackContract.Entry.CONTENT_URI_COLLECTION,
                null, null, null, null);
        if (cursor == null) return;

        List<String> charities = new ArrayList<>();
        if (!cursor.moveToFirst()) charities.add("");
        else {
            do {
                String ein = cursor.getString(GivetrackContract.Entry.INDEX_EIN);
                String phone = cursor.getString(GivetrackContract.Entry.INDEX_PHONE_NUMBER);
                String email = cursor.getString(GivetrackContract.Entry.INDEX_EMAIL_ADDRESS);
                float percentage = Float.parseFloat(cursor.getString(GivetrackContract.Entry.INDEX_DONATION_PERCENTAGE));
                float impact = Float.parseFloat(cursor.getString(GivetrackContract.Entry.INDEX_DONATION_IMPACT));
                int frequency = cursor.getInt(GivetrackContract.Entry.INDEX_DONATION_FREQUENCY);
                charities.add(String.format(Locale.getDefault(), "%s:%s:%s:%f:%f:%d", ein, phone, email, percentage, impact, frequency));
            } while (cursor.moveToNext());
            cursor.close();
        }

        UserPreferences.setCharities(this, charities);
        UserPreferences.updateFirebaseUser(this);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action ResetGenerated in the provided background thread with the provided parameters.
     */
    private void handleActionResetGenerated() {
        DISK_IO.execute(() -> getContentResolver().delete(GivetrackContract.Entry.CONTENT_URI_GENERATION, null, null));

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action ResetCollected in the provided background thread with the provided parameters.
     */
    private void handleActionResetCollected() {
        DISK_IO.execute(() -> getContentResolver().delete(GivetrackContract.Entry.CONTENT_URI_COLLECTION, null, null));

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }
    
    /**
     * Handles action UpdatePercentages in the provided background thread with the provided parameters.
     */
    private void handleActionUpdatePercentages(ContentValues... charityValues) {
        DISK_IO.execute(() -> {
            Cursor cursor = getContentResolver().query(GivetrackContract.Entry.CONTENT_URI_COLLECTION,
            null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) return;

            boolean recalibrate = charityValues[0].get(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE) == null;
            if (recalibrate) charityValues[0].put(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE, String.valueOf(1f / cursor.getCount()));
            int i = 0;

            List<String> charities = new ArrayList<>();

            do {
                ContentValues values = recalibrate ? charityValues[0] : charityValues[i++];
                String ein = cursor.getString(GivetrackContract.Entry.INDEX_EIN);
                String phone = cursor.getString(GivetrackContract.Entry.INDEX_PHONE_NUMBER);
                String email = cursor.getString(GivetrackContract.Entry.INDEX_EMAIL_ADDRESS);
                float percentage = Float.parseFloat(values.getAsString(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE));
                float impact = Float.parseFloat(cursor.getString(GivetrackContract.Entry.INDEX_DONATION_IMPACT));
                int frequency = cursor.getInt(GivetrackContract.Entry.INDEX_DONATION_FREQUENCY);
                charities.add(String.format(Locale.getDefault(),"%s:%s:%s:%f:%f:%d", ein, phone, email, percentage, impact, frequency));

                Uri uri = GivetrackContract.Entry.CONTENT_URI_COLLECTION.buildUpon().appendPath(ein).build();
                getContentResolver().update(uri, values, null, null);

            } while (cursor.moveToNext());
            cursor.close();

            UserPreferences.setCharities(this, charities);
            UserPreferences.updateFirebaseUser(this);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action UpdateFrequency in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateFrequency(ContentValues charityValues) {

        String affectedColumn;
        int affectedIndex;
        if (charityValues.containsKey(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY)) {
            affectedColumn = GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY;
            affectedIndex = GivetrackContract.Entry.INDEX_DONATION_FREQUENCY;
        } else return;

        DISK_IO.execute(() -> {
            Cursor cursor = getContentResolver().query(GivetrackContract.Entry.CONTENT_URI_COLLECTION, null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) return;

            int f = charityValues.getAsInteger(affectedColumn);
            List<String> charities = new ArrayList<>();

            long currentTime = System.currentTimeMillis();
            long lastConversionTime = UserPreferences.getTimestamp(this);
            long timeBetweenConversions = currentTime - lastConversionTime;

            long daysBetweenConversions =
                    TimeUnit.DAYS.convert(
                            timeBetweenConversions,
                            TimeUnit.MILLISECONDS
                    );

            float todaysImpact = daysBetweenConversions > 0 ? 0 : Float.valueOf(UserPreferences.getToday(this));
            float totalTracked = Float.parseFloat(UserPreferences.getTracked(this));
            float amount = Float.parseFloat(UserPreferences.getDonation(this)) * f;
            do {
                String ein = cursor.getString(GivetrackContract.Entry.INDEX_EIN);
                String phone = cursor.getString(GivetrackContract.Entry.INDEX_PHONE_NUMBER);
                String email = cursor.getString(GivetrackContract.Entry.INDEX_EMAIL_ADDRESS);
                float percentage = Float.parseFloat(cursor.getString(GivetrackContract.Entry.INDEX_DONATION_PERCENTAGE));
                float transactionImpact = amount * percentage;
                float totalImpact = Float.parseFloat(cursor.getString(GivetrackContract.Entry.INDEX_DONATION_IMPACT)) + transactionImpact;
                todaysImpact += transactionImpact * 100f / 100f;
                totalTracked += transactionImpact * 100f / 100f;

                int affectedFrequency = cursor.getInt(affectedIndex) + (percentage == 0f ? 0 : f);

                ContentValues values = new ContentValues();
                values.put(GivetrackContract.Entry.COLUMN_EIN, ein);
                values.put(affectedColumn, affectedFrequency);
                values.put(GivetrackContract.Entry.COLUMN_DONATION_IMPACT, String.format(Locale.getDefault(), "%.2f", totalImpact));

                charities.add(String.format(Locale.getDefault(), "%s:%s:%s:%f:%.2f:%d", ein, phone, email, percentage, totalImpact, affectedFrequency));

                Uri uri = GivetrackContract.Entry.CONTENT_URI_COLLECTION.buildUpon().appendPath(ein).build();
                getContentResolver().update(uri, values, null, null);
            } while (cursor.moveToNext());
            cursor.close();

            String[] tallyArray = UserPreferences.getTally(this).split(":");
            tallyArray[0] = String.format(Locale.getDefault(), "%.2f", todaysImpact);
            UserPreferences.setToday(this, String.format(Locale.getDefault(), "%.2f", todaysImpact));
            UserPreferences.setTracked(this, String.format(Locale.getDefault(), "%.2f", totalTracked));
            UserPreferences.setTally(this, Arrays.asList(tallyArray).toString().replace("[","").replace("]","").replace(", ", ":"));
            UserPreferences.setTimestamp(this, System.currentTimeMillis());
            UserPreferences.setCharities(this, charities);
            UserPreferences.updateFirebaseUser(this);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action ResetData in the provided background thread.
     */
    private void handleActionResetData() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        DISK_IO.execute(() -> {
            getContentResolver().delete(GivetrackContract.Entry.CONTENT_URI_GENERATION, null, null);
            getContentResolver().delete(GivetrackContract.Entry.CONTENT_URI_COLLECTION, null, null);
        });
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    private String urlToEmailAddress(String url) {
        String emailAddress = DEFAULT_VALUE_STR;
        try {
            if (url.isEmpty()) return DEFAULT_VALUE_STR;
            Document homepage = Jsoup.connect(url).get();
            Elements homeInfo = homepage.select("a");
            List<String> emailAddresses;
            List<String> visitedLinks = new ArrayList<>();
            emailAddresses = parseKeysFromPages(url, homeInfo, "Donate", visitedLinks, "mailto:");
            if (emailAddresses.isEmpty())
                emailAddresses = parseKeysFromPages(url, homeInfo, "Contact", visitedLinks, "mailto:");
            if (emailAddresses.isEmpty())
                emailAddresses = parseKeys(homeInfo, "mailto:", null, " ");
            if (!emailAddresses.isEmpty()) {
                for (String address : emailAddresses) Timber.v("Email: %s", address);
                emailAddress = emailAddresses.get(0);
            }
        } catch (IOException e) { Timber.e(e);
        } return emailAddress;
    }

    private String urlToPhoneNumber(String url) {
        String phoneNumber = DEFAULT_VALUE_STR;
        try {
            Document webpage = Jsoup.connect(url).get();
            Elements info = webpage.select("div[class=cn-appear]");
            List<String> phoneNumbers;
            phoneNumbers = parseKeys(info, "tel:", 15, "[^0-9]");
            if (!phoneNumbers.isEmpty()) {
                for (String number : phoneNumbers) Timber.v("Phone: %s", number);
                phoneNumber = phoneNumbers.get(0);
            }
        } catch (IOException e) { Timber.e(e);
        } return phoneNumber;
    }

    private List<String> parseKeysFromPages(String homeUrl, Elements anchors, String pageName, List<String> visitedLinks, String key) throws IOException {
        List<String> emails = new ArrayList<>();
        for (int i = 0; i < anchors.size(); i++) {
            Element anchor = anchors.get(i);
            if (anchor.text().contains(pageName)) {
                if (!anchor.hasAttr("href")) continue;
                String pageLink = anchors.get(i).attr("href");
                if (pageLink.startsWith("/")) pageLink = homeUrl + pageLink.substring(1);
                if (visitedLinks.contains(pageLink)) continue;
                else visitedLinks.add(pageLink);
                Document page = Jsoup.connect(pageLink).get();
                Elements pageAnchors = page.select("a");

                emails.addAll(parseKeys(pageAnchors, key, null, " "));
            }
        }
        return emails;
    }

    private List<String> parseKeys(Elements anchors, String key, @Nullable Integer endIndex, @Nullable String removeRegex) {
        List<String> values = new ArrayList<>();
        for (int j = 0; j < anchors.size(); j++) {
            Element anchor = anchors.get(j);
            if (anchor.hasAttr("href")) {
                if (anchor.attr("href").contains(key))
                    values.add(anchor.attr("href").split(key)[1].trim());
            } else if (anchor.text().contains(key)) {
                String text = anchor.text();
                String value = text.split(key)[1].trim();
                if (endIndex != null) value = value.substring(0, endIndex);
                if (removeRegex != null) value = value.replaceAll(removeRegex, "");
                values.add(value);
            }
        }
        return values;
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
        } catch (MalformedURLException|UnsupportedEncodingException e) {
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
     * This method parses JSON String of data API response and returns array of {@link ContentValues}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static ContentValues[] parseJsonResponse(@NonNull String jsonResponse, boolean single) {

        ContentValues[] valuesArray = null;
        try {
            if (single) {
                valuesArray = new ContentValues[1];
                valuesArray[0] = parseContentValues(new JSONObject(jsonResponse));
                Timber.v("Parsed Response: %s", valuesArray[0].toString());
            } else {
                JSONArray charityArray = new JSONArray(jsonResponse);
                valuesArray = new ContentValues[charityArray.length()];
                for (int i = 0; i < charityArray.length(); i++) {
                    JSONObject charityObject = charityArray.getJSONObject(i);
                    ContentValues values = parseContentValues(charityObject);
                    valuesArray[i] = values;
                    Timber.v("Parsed Response: %s", values.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.e(e);
        }
        return valuesArray;
    }

    /**
     * This method parses JSONObject of JSONArray and returns {@link ContentValues}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static ContentValues parseContentValues(JSONObject charityObject) throws JSONException {

        JSONObject locationObject = charityObject.getJSONObject(FetchContract.KEY_LOCATION);
        String ein = charityObject.getString(FetchContract.KEY_EIN);
        String charityName = charityObject.getString(FetchContract.KEY_CHARITY_NAME);
        String street = locationObject.getString(FetchContract.KEY_STREET_ADDRESS);
        String detail = locationObject.getString(FetchContract.KEY_ADDRESS_DETAIL);
        String city = locationObject.getString(FetchContract.KEY_CITY);
        String state = locationObject.getString(FetchContract.KEY_STATE);
        String zip = locationObject.getString(FetchContract.KEY_POSTAL_CODE);
        String homepageUrl = charityObject.getString(FetchContract.KEY_WEBSITE_URL);
        String navigatorUrl = charityObject.getString(FetchContract.KEY_CHARITY_NAVIGATOR_URL);

        ContentValues values = new ContentValues();
        values.put(GivetrackContract.Entry.COLUMN_EIN, ein);
        values.put(GivetrackContract.Entry.COLUMN_CHARITY_NAME, nullToDefaultStr(charityName));
        values.put(GivetrackContract.Entry.COLUMN_LOCATION_STREET, nullToDefaultStr(street));
        values.put(GivetrackContract.Entry.COLUMN_LOCATION_DETAIL, nullToDefaultStr(detail));
        values.put(GivetrackContract.Entry.COLUMN_LOCATION_CITY, nullToDefaultStr(city));
        values.put(GivetrackContract.Entry.COLUMN_LOCATION_STATE, nullToDefaultStr(state));
        values.put(GivetrackContract.Entry.COLUMN_LOCATION_ZIP, nullToDefaultStr(zip));
        values.put(GivetrackContract.Entry.COLUMN_HOMEPAGE_URL, nullToDefaultStr(homepageUrl));
        values.put(GivetrackContract.Entry.COLUMN_NAVIGATOR_URL, nullToDefaultStr(navigatorUrl));

        return values;
    }

    /**
     * Converts a null value returned from API response to default value.
     */
    private static String nullToDefaultStr(String str) {
        return (str.equals("null")) ? DEFAULT_VALUE_STR : str;
    }

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