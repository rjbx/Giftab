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
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;

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

// TODO: Add record and giving objects implementing ORM for remote persistence and Parcelable for bundling
/**
 * Handles asynchronous task requests in a service on a separate handler thread.
 */
public class DatabaseService extends IntentService {

    private static final Executor DISK_IO = AppExecutors.getInstance().getDiskIO();
    private static final Executor NETWORK_IO = AppExecutors.getInstance().getNetworkIO();

    private static final String ACTION_FETCH_SEARCH = "com.github.rjbx.givetrack.data.action.FETCH_SEARCH";
    private static final String ACTION_FETCH_GIVING = "com.github.rjbx.givetrack.data.action.FETCH_GIVING";
    private static final String ACTION_FETCH_RECORD = "com.github.rjbx.givetrack.data.action.FETCH_RECORD";
    private static final String ACTION_REMOVE_SEARCH = "com.github.rjbx.givetrack.data.action.REMOVE_SEARCH";
    private static final String ACTION_REMOVE_GIVING = "com.github.rjbx.givetrack.data.action.REMOVE_GIVING";
    private static final String ACTION_REMOVE_RECORD = "com.github.rjbx.givetrack.data.action.REMOVE_RECORD";
    private static final String ACTION_RESET_SEARCH = "com.github.rjbx.givetrack.data.action.RESET_SEARCH";
    private static final String ACTION_RESET_GIVING = "com.github.rjbx.givetrack.data.action.RESET_GIVING";
    private static final String ACTION_RESET_RECORD = "com.github.rjbx.givetrack.data.action.RESET_RECORD";
    private static final String ACTION_GIVE_SEARCH = "com.github.rjbx.givetrack.data.action.GIVE_SEARCH";
    private static final String ACTION_GIVE_RECORD = "com.github.rjbx.givetrack.data.action.GIVE_RECORD";
    private static final String ACTION_UPDATE_CONTACT = "com.github.rjbx.givetrack.data.action.UPDATE_CONTACT";
    private static final String ACTION_UPDATE_FREQUENCY = "com.github.rjbx.givetrack.data.action.UPDATE_FREQUENCY";
    private static final String ACTION_UPDATE_PERCENTAGES = "com.github.rjbx.givetrack.data.action.UPDATE_PERCENTAGES";
    private static final String ACTION_UPDATE_TIME = "com.github.rjbx.givetrack.data.action.UPDATE_TIME";
    private static final String ACTION_UPDATE_AMOUNT = "com.github.rjbx.givetrack.data.action.UPDATE_AMOUNT";
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
    public DatabaseService() { super(DatabaseService.class.getSimpleName()); }

    /**
     * Starts this service to perform action FetchSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchSearch(Context context, HashMap<String, String> apiRequest) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_SEARCH);
        intent.putExtra(EXTRA_API_REQUEST, apiRequest);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action FetchGiving with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchGiving(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_GIVING);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action FetchRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchRecord(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_RECORD);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action GiveSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGiveSearch(Context context, String charityId) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GIVE_SEARCH);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action GiveSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGiveRecord(Context context, String charityId) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GIVE_RECORD);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveSearch(Context context, String charityId) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_SEARCH);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveGiving with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveGiving(Context context, String charityId) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_GIVING);
        intent.putExtra(EXTRA_ITEM_ID, charityId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveRecord(Context context, long recordTime) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_RECORD);
        intent.putExtra(EXTRA_ITEM_ID, recordTime);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetSearch(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_SEARCH);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetGiving with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetGiving(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_GIVING);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetRecord(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_RECORD);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateContact with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateContact(Context context, ContentValues charityValues) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_CONTACT);
        intent.putExtra(EXTRA_ITEM_VALUES, charityValues);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateFrequency with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateFrequency(Context context, ContentValues charityValues) {
        Intent intent = new Intent(context, DatabaseService.class);
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
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_PERCENTAGES);
        if (charityValues.length > 1) {
            ArrayList<ContentValues> charityValuesArrayList = new ArrayList<>(Arrays.asList(charityValues));
            intent.putParcelableArrayListExtra(EXTRA_LIST_VALUES, charityValuesArrayList);
        } else intent.putExtra(EXTRA_ITEM_VALUES, charityValues[0]);
        context.startService(intent);
    }

    public static void startActionUpdateTime(Context context, long oldTime, long newTime) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_TIME);
        intent.putExtra(EXTRA_ITEM_ID, oldTime);
        intent.putExtra(EXTRA_ITEM_VALUES, newTime);
        context.startService(intent);
    }

    public static void startActionUpdateAmount(Context context, long id, float amount) {
        Intent intent = new Intent(context, DatabaseService.class);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, amount);
        intent.setAction(ACTION_UPDATE_AMOUNT);
        intent.putExtra(EXTRA_ITEM_ID, id);
        intent.putExtra(EXTRA_ITEM_VALUES, amount);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetData with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetData(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_DATA);
        context.startService(intent);
    }

    /**
     * Syncs data inside a worker thread on requests to process {@link Intent}.
     * @param intent launches this {@link IntentService}.
     */
    @Override protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        final String action = intent.getAction();
        switch (action) {
            case ACTION_FETCH_SEARCH:
                final HashMap fetchSearchMap = (HashMap) intent.getSerializableExtra(EXTRA_API_REQUEST);
                handleActionFetchSearch(fetchSearchMap);
                break;
            case ACTION_FETCH_GIVING:
                handleActionFetchGiving();
                break;
            case ACTION_FETCH_RECORD:
                handleActionFetchRecord();
                break;
            case ACTION_GIVE_SEARCH:
                final String collectSearchString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionGiveSearch(collectSearchString);
                break;
            case ACTION_GIVE_RECORD:
                final String collectRecordString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionGiveRecord(collectRecordString);
                break;
            case ACTION_REMOVE_SEARCH:
                final String removeSearchString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionRemoveSearch(removeSearchString);
                break;
            case ACTION_REMOVE_GIVING:
                final String removeGivingString = intent.getStringExtra(EXTRA_ITEM_ID);
                handleActionRemoveGiving(removeGivingString);
                break;
            case ACTION_REMOVE_RECORD:
                final long removeRecordLong = intent.getLongExtra(EXTRA_ITEM_ID, -1);
                handleActionRemoveRecord(removeRecordLong);
                break;
            case ACTION_RESET_SEARCH:
                handleActionResetSearch();
                break;
            case ACTION_RESET_GIVING:
                handleActionResetGiving();
                break;
            case ACTION_RESET_RECORD:
                handleActionResetRecord();
                break;
            case ACTION_UPDATE_CONTACT:
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
            case ACTION_UPDATE_TIME:
                long timeId = intent.getLongExtra(EXTRA_ITEM_ID, 0);
                long newTime = intent.getLongExtra(EXTRA_ITEM_VALUES, 0);
                handleActionUpdateTime(timeId, newTime);
                break;
            case ACTION_UPDATE_AMOUNT:
                long amountId = intent.getLongExtra(EXTRA_ITEM_ID, 0);
                float amount = intent.getFloatExtra(EXTRA_ITEM_VALUES, 0f);
                handleActionUpdateAmount(amountId, amount);
                break;
            case ACTION_RESET_DATA: handleActionResetData();
        }
    }

    /**
     * Handles action FetchSearch in the provided background thread with the provided parameters.
     */
    private void handleActionFetchSearch(HashMap apiRequest) {

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
            Search[] parsedResponse = parseSearches(response, single);

            // Store data
            DatabaseRepository.removeSearch(this, null);
            DatabaseRepository.addSearch(this, parsedResponse);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action FetchGiving in the provided background thread.
     */
    private void handleActionFetchGiving() {

        Uri.Builder templateBuilder = Uri.parse(FetchContract.BASE_URL).buildUpon();
        templateBuilder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);
        Uri template = templateBuilder.build();

        List<String> charities = UserPreferences.getCharities(this);
        if (charities.isEmpty() || charities.get(0).isEmpty()) return;

        int charityCount = charities.size();
        Giving[] givings = new Giving[charityCount];

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
                Timber.e("Giving Fetched URL: %s", url);
                String response = requestResponseFromUrl(url);
                Timber.e("Giving Fetched Response: %s", response);
                Search search = parseSearches(response, true)[0];
                search.setPhone(charityData[1]);
                search.setEmail(charityData[2]);
                search.setImpact(charityData[4]);
                Giving giving = new Giving(search, Integer.parseInt(charityData[5]), charityData[3]);
                givings[i] = giving;
            }

            DatabaseRepository.removeGiving(this, null);
            DatabaseRepository.addGiving(this, givings);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action FetchRecord in the provided background thread.
     */
    private void handleActionFetchRecord() {

        Uri.Builder templateBuilder = Uri.parse(FetchContract.BASE_URL).buildUpon();
        templateBuilder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);
        Uri template = templateBuilder.build();

        List<String> charities = UserPreferences.getCharities(this);
        if (charities.isEmpty() || charities.get(0).isEmpty()) return;

        int charityCount = charities.size();
        Record[] records = new Record[charityCount];

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
                Timber.e("Record Fetched URL: %s", url);
                String response = requestResponseFromUrl(url);
                Timber.e("Record Fetched Response: %s", response);
                Search search = parseSearches(response, true)[0];
                search.setPhone(charityData[1]);
                search.setEmail(charityData[2]);
                search.setImpact(charityData[4]);
                Record record = new Record(search, "", 0);
                records[i] = record;
            }

            DatabaseRepository.removeSearch(this, null);
            DatabaseRepository.addRecord(this, records);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action GiveSearch in the provided background thread with the provided parameters.
     */
    private void handleActionGiveSearch(String charityId) {

        NETWORK_IO.execute(() -> {

            Search search = DatabaseRepository.getSearch(this, charityId).get(0);

            List<String> charities = UserPreferences.getCharities(this);

            String ein = search.getEin();
            float impact = 0;
            int frequency = 0;

            for (String record : UserPreferences.getRecords(this)) {
                String[] recordFields = record.split(":");
                if (recordFields[3].equals(ein)) {
                    impact += Float.parseFloat(recordFields[1]);
                    frequency++;
                }
            }

            String percentage = charities.isEmpty() || charities.get(0).isEmpty() ? "1" : "0";
            Giving giving = new Giving(search, frequency, percentage);
            giving.setImpact(String.valueOf(impact));

            String phoneNumber = urlToPhoneNumber(giving.getNavigatorUrl());
            giving.setPhone(phoneNumber);

            String emailAddress = urlToEmailAddress(giving.getHomepageUrl());
            giving.setEmail(emailAddress);

            if (charities.isEmpty() || charities.get(0).isEmpty()) charities = new ArrayList<>();

            charities.add(String.format(Locale.getDefault(),"%s:%s:%s:%s:%f:%d", ein, phoneNumber, emailAddress, percentage, 0f, 0));

            UserPreferences.setCharities(this, charities);
            UserPreferences.updateFirebaseUser(this);
            DatabaseRepository.addGiving(this, giving);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action GiveRecord in the provided background thread with the provided parameters.
     */
    private void handleActionGiveRecord(String charityId) {

         NETWORK_IO.execute(() -> {

            List<String> charities = UserPreferences.getCharities(this);

            Record record = DatabaseRepository.getRecord(this, charityId).get(0);

            float impact = 0;
            int frequency = 0;

            for (String r : UserPreferences.getRecords(this)) {
                String[] recordFields = r.split(":");
                if (recordFields[3].equals(record.getEin())) {
                    impact += Float.parseFloat(recordFields[1]);
                    frequency++;
                }
            }
            
            String percentage = charities.isEmpty() || charities.get(0).isEmpty() ? "1" : "0";
            Giving giving = new Giving(record.getSearch(), frequency, String.valueOf(impact));

            String phoneNumber = urlToPhoneNumber(giving.getNavigatorUrl());
            giving.setPhone(phoneNumber);

            String emailAddress = urlToEmailAddress(giving.getHomepageUrl());
            giving.setEmail(emailAddress);

            if (charities.isEmpty() || charities.get(0).isEmpty()) charities = new ArrayList<>();
            charities.add(String.format(Locale.getDefault(),"%s:%s:%s:%s:%f:%d", giving.getEin(), phoneNumber, emailAddress, percentage, 0f, 0));

            UserPreferences.setCharities(this, charities);
            UserPreferences.updateFirebaseUser(this);
            DatabaseRepository.addGiving(this, giving);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action RemoveSearch in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveSearch(String charityId) {

        DISK_IO.execute(() -> DatabaseRepository.removeSearch(this, charityId);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action RemoveGiving in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveGiving(String charityId) {

        DISK_IO.execute(() -> DatabaseRepository.removeGiving(this, charityId);

        Cursor cursor = getContentResolver().query(DatabaseContract.Entry.CONTENT_URI_GIVING,
                null, null, null, null);
        if (cursor == null) return;

        List<String> charities = UserPreferences.getCharities(this);
        int removeIndex = 0;
        boolean notFound = true;
        for (int i = 0; i < charities.size(); i++) {
            if (charities.get(i).split(":")[0].contains(charityId)) {
                removeIndex = i;
                notFound = false;
                break;
            }
        } if (notFound) return;
        charities.remove(charities.get(removeIndex));
        UserPreferences.setCharities(this, charities);
        UserPreferences.updateFirebaseUser(this);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action RemoveRecord in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveRecord(long time) {

        String formattedTime = String.valueOf(time);
        Uri recordUri = DatabaseContract.Entry.CONTENT_URI_RECORD.buildUpon().appendPath(formattedTime).build();
        DISK_IO.execute(() -> {
            Record record = DatabaseRepository.getRecord(this, formattedTime).get(0);
            String ein = record.getEin();
            float rI = Float.parseFloat(record.getImpact());


            Giving giving =DatabaseRepository.getGiving(this, ein).get(0);

            giving.setFrequency(giving.getFrequency() - 1);
            giving.setImpact(String.valueOf(Float.parseFloat(giving.getImpact()) - rI));
            DatabaseRepository.addGiving(this, giving);

            DatabaseRepository.removeRecord(this, formattedTime);

            List<String> records = UserPreferences.getRecords(this);
            if (records.isEmpty() || records.get(0).isEmpty()) records = new ArrayList<>();
            int removeIndex = 0;
            boolean found = false;
            for (int i = 0; i < records.size(); i++) {
                if (Float.parseFloat(records.get(i).split(":")[0]) == time) {
                    removeIndex = i;
                    found = true;
                    break;
                }
            }
            if (found) {
                records.remove(records.get(removeIndex));
                UserPreferences.setRecords(this, records);
            }

            UserPreferences.updateFirebaseUser(this);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action ResetSearch in the provided background thread with the provided parameters.
     */
    private void handleActionResetSearch() {
        DISK_IO.execute(() -> DatabaseRepository.removeSearch(this, null));

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action ResetGiving in the provided background thread with the provided parameters.
     */
    private void handleActionResetGiving() {
        DISK_IO.execute(() -> DatabaseRepository.removeGiving(this, null));

        UserPreferences.setCharities(this, new ArrayList<>());
        UserPreferences.updateFirebaseUser(this);
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action ResetRecord in the provided background thread with the provided parameters.
     */
    private void handleActionResetRecord() {
        DISK_IO.execute(() -> DatabaseRepository.removeRecord(this, null));

        UserPreferences.setRecords(this, new ArrayList<>());
        UserPreferences.updateFirebaseUser(this);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, "0");
        values.put(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY, 0);
        getContentResolver().update(DatabaseContract.Entry.CONTENT_URI_GIVING, values, null, null);
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action UpdatePercentages in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateContact(ContentValues... charityValues) {}

    /**
     * Handles action UpdatePercentages in the provided background thread with the provided parameters.
     */
    private void handleActionUpdatePercentages(ContentValues... charityValues) {
        DISK_IO.execute(() -> {
            Cursor cursor = getContentResolver().query(DatabaseContract.Entry.CONTENT_URI_GIVING,
                    null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) return;

            boolean recalibrate = charityValues[0].get(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE) == null;
            if (recalibrate) charityValues[0].put(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE, String.valueOf(1f / cursor.getCount()));
            int i = 0;

            List<String> charities = new ArrayList<>();

            do {
                ContentValues values = recalibrate ? charityValues[0] : charityValues[i++];
                String ein = cursor.getString(DatabaseContract.Entry.INDEX_EIN);
                String phone = cursor.getString(DatabaseContract.Entry.INDEX_PHONE_NUMBER);
                String email = cursor.getString(DatabaseContract.Entry.INDEX_EMAIL_ADDRESS);
                float percentage = Float.parseFloat(values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE));
                float impact = Float.parseFloat(cursor.getString(DatabaseContract.Entry.INDEX_DONATION_IMPACT));
                int frequency = cursor.getInt(DatabaseContract.Entry.INDEX_DONATION_FREQUENCY);
                charities.add(String.format(Locale.getDefault(),"%s:%s:%s:%f:%f:%d", ein, phone, email, percentage, impact, frequency));

                Uri uri = DatabaseContract.Entry.CONTENT_URI_GIVING.buildUpon().appendPath(ein).build();
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
        if (charityValues.containsKey(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY)) {
            affectedColumn = DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY;
            affectedIndex = DatabaseContract.Entry.INDEX_DONATION_FREQUENCY;
        } else return;

        DISK_IO.execute(() -> {
            Cursor cursor = getContentResolver().query(DatabaseContract.Entry.CONTENT_URI_GIVING, null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) return;

            long anchorTime = UserPreferences.getAnchor(this);

            int f = charityValues.getAsInteger(affectedColumn);
            List<String> charities = new ArrayList<>();
            List<String> records = UserPreferences.getRecords(this);
            if (records.isEmpty() || records.get(0).isEmpty()) records = new ArrayList<>();

            float amount = Float.parseFloat(UserPreferences.getDonation(this)) * f;
            do {
                String ein = cursor.getString(DatabaseContract.Entry.INDEX_EIN);
                String name = cursor.getString(DatabaseContract.Entry.INDEX_CHARITY_NAME);
                String phone = cursor.getString(DatabaseContract.Entry.INDEX_PHONE_NUMBER);
                String email = cursor.getString(DatabaseContract.Entry.INDEX_EMAIL_ADDRESS);
                float percentage = Float.parseFloat(cursor.getString(DatabaseContract.Entry.INDEX_DONATION_PERCENTAGE));
                float transactionImpact = amount * percentage;
                float totalImpact = Float.parseFloat(cursor.getString(DatabaseContract.Entry.INDEX_DONATION_IMPACT)) + transactionImpact;
                int affectedFrequency = cursor.getInt(affectedIndex) + (percentage < .01f ? 0 : f);

                ContentValues values = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, values);
                values.put(DatabaseContract.Entry.COLUMN_EIN, ein);
                values.put(affectedColumn, affectedFrequency);
                values.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, String.format(Locale.getDefault(), "%.2f", totalImpact));
                charities.add(String.format(Locale.getDefault(), "%s:%s:%s:%f:%.2f:%d", ein, phone, email, percentage, totalImpact, affectedFrequency));

                if (transactionImpact != 0) records.add(String.format(Locale.getDefault(), "%d:%s:%s:%s", anchorTime, transactionImpact, name, ein));

                Uri uri = DatabaseContract.Entry.CONTENT_URI_GIVING.buildUpon().appendPath(ein).build();
                getContentResolver().update(uri, values, null, null);

                if (percentage < .01f) continue;
                values.remove(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY);
                values.remove(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE);
                values.remove(DatabaseContract.Entry.COLUMN_DONATION_IMPACT);
                values.put(DatabaseContract.Entry.COLUMN_DONATION_TIME, anchorTime++);
                values.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, String.format(Locale.getDefault(), "%.2f", transactionImpact));
                values.put(DatabaseContract.Entry.COLUMN_DONATION_MEMO, "");
                getContentResolver().insert(DatabaseContract.Entry.CONTENT_URI_RECORD, values);

            } while (cursor.moveToNext());
            cursor.close();

            updateTimePreferences(anchorTime, amount);

            UserPreferences.setRecords(this, records);
            UserPreferences.setCharities(this, charities);
            UserPreferences.updateFirebaseUser(this);
        });

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action UpdateTime in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateTime(long oldTime, long newTime) {
        String formattedTime = String.valueOf(oldTime);

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Entry.COLUMN_DONATION_TIME, newTime);
        Uri entryUri = DatabaseContract.Entry.CONTENT_URI_RECORD.buildUpon().appendPath(formattedTime).build();
        DISK_IO.execute(() -> getContentResolver().update(entryUri, values, null, null));

        List<String> records = UserPreferences.getRecords(this);
        for (String record : records) {
            String[] recordFields = record.split(":");
            if (recordFields[0].equals(formattedTime)) {
                String newRecord = record.replaceFirst(formattedTime, String.valueOf(newTime));
                int index = records.indexOf(record);
                records.set(index, newRecord);
            }
        }
        UserPreferences.setRecords(this, records);

        updateTimePreferences(UserPreferences.getAnchor(this), 0);
        UserPreferences.updateFirebaseUser(this);

        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action UpdateAmount in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateAmount(long id, float amount) {
        String formattedTime = String.valueOf(id);

        String ein = "";
        float oldAmount = 0;
        List<String> records = UserPreferences.getRecords(this);
        for (String record : records) {
            String[] recordFields = record.split(":");
            if(recordFields[0].equals(formattedTime)) {
                String oldAmountStr = recordFields[1];
                String newAmountStr = String.format(Locale.getDefault(), "%.2f", amount);
                String newRecord = String.format("%s:%s:%s:%s",
                        recordFields[0], newAmountStr, recordFields[2], recordFields[3]);
                int index = records.indexOf(record);
                records.set(index, newRecord);
                oldAmount = Float.parseFloat(oldAmountStr);
                ein = recordFields[3];
            }
        }
        UserPreferences.setRecords(this, records);
        float amountChange = amount - oldAmount;

        String newGivingAmountStr = "";
        List<String> charities = UserPreferences.getCharities(this);
        for (String charity : charities) {
            String[] charityFields = charity.split(":");
            if (charityFields[0].equals(ein)) {
                String givingAmountStr = charityFields[4];
                float newGivingAmount = Float.parseFloat(givingAmountStr) + amountChange;
                newGivingAmountStr = String.format(Locale.getDefault(), "%.2f", newGivingAmount);
                String newCharity = String.format("%s:%s:%s:%s:%s:%s",
                        charityFields[0], charityFields[1], charityFields[2], charityFields[3], newGivingAmountStr, charityFields[5]);
                int index = charities.indexOf(charity);
                charities.set(index, newCharity);
            }
        }
        UserPreferences.setCharities(this, charities);

        String recordAmountStr = String.format(Locale.getDefault(), "%.2f", amount);

        ContentValues recordValues = new ContentValues();
        ContentValues givingValues = new ContentValues();
        recordValues.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, recordAmountStr);
        givingValues.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, newGivingAmountStr);
        Uri recordUri = DatabaseContract.Entry.CONTENT_URI_RECORD.buildUpon().appendPath(formattedTime).build();
        Uri givingUri = DatabaseContract.Entry.CONTENT_URI_GIVING.buildUpon().appendEncodedPath(ein).build();
        DISK_IO.execute(() -> {
            getContentResolver().update(recordUri, recordValues, null, null);
            getContentResolver().update(givingUri, givingValues, null, null);
        });

        updateTimePreferences(UserPreferences.getAnchor(this), amountChange);
        UserPreferences.updateFirebaseUser(this);

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
            DatabaseRepository.removeSearch(this, null);
            DatabaseRepository.removeGiving(this, null);
            DatabaseRepository.removeRecord(this, null);
        });
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    private void updateTimePreferences(long anchorTime, float amount) {

        if (!UserPreferences.getHistorical(this)) UserPreferences.setAnchor(this, System.currentTimeMillis());
        else UserPreferences.setAnchor(this, anchorTime);
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
        values.put(DatabaseContract.Entry.COLUMN_EIN, ein);
        values.put(DatabaseContract.Entry.COLUMN_CHARITY_NAME, nullToDefaultStr(charityName));
        values.put(DatabaseContract.Entry.COLUMN_LOCATION_STREET, nullToDefaultStr(street));
        values.put(DatabaseContract.Entry.COLUMN_LOCATION_DETAIL, nullToDefaultStr(detail));
        values.put(DatabaseContract.Entry.COLUMN_LOCATION_CITY, nullToDefaultStr(city));
        values.put(DatabaseContract.Entry.COLUMN_LOCATION_STATE, nullToDefaultStr(state));
        values.put(DatabaseContract.Entry.COLUMN_LOCATION_ZIP, nullToDefaultStr(zip));
        values.put(DatabaseContract.Entry.COLUMN_HOMEPAGE_URL, nullToDefaultStr(homepageUrl));
        values.put(DatabaseContract.Entry.COLUMN_NAVIGATOR_URL, nullToDefaultStr(navigatorUrl));
        values.put(DatabaseContract.Entry.COLUMN_PHONE_NUMBER, "");
        values.put(DatabaseContract.Entry.COLUMN_EMAIL_ADDRESS, "");
        values.put(DatabaseContract.Entry.COLUMN_DONATION_IMPACT, "0");
        values.put(DatabaseContract.Entry.COLUMN_DONATION_TYPE, 0);

        return values;
    }

    /**
     * This method parses JSON String of data API response and returns array of {@link ContentValues}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static Search[] parseSearches(@NonNull String jsonResponse, boolean single) {

        Search[] searches = null;
        try {
            if (single) {
                searches = new Search[1];
                searches[0] = parseSearch(new JSONObject(jsonResponse));
                Timber.v("Parsed Response: %s", searches[0].toString());
            } else {
                JSONArray charityArray = new JSONArray(jsonResponse);
                searches = new Search[charityArray.length()];
                for (int i = 0; i < charityArray.length(); i++) {
                    JSONObject charityObject = charityArray.getJSONObject(i);
                    Search search = parseSearch(charityObject);
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
     * This method parses JSONObject of JSONArray and returns {@link ContentValues}.
     * @throws JSONException if JSON data cannot be properly parsed.
     */
    private static Search parseSearch(JSONObject charityObject) throws JSONException {

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

        return new Search(ein, name, street, detail, city, state, zip, homepageUrl, navigatorUrl, "", "", "0", 0);
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