package com.github.rjbx.givetrack.data;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import com.github.rjbx.givetrack.AppExecutors;
import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.AppWidget;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;
import com.github.rjbx.givetrack.data.entry.User;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executor;

// TODO: Extrapolate executors from service thread if possible or consolidate logic into the former or the latter
/**
 * Handles asynchronous task requests in a service on a separate handler thread.
 */
public class DatabaseService extends IntentService {

    private static final Executor DISK_IO = AppExecutors.getInstance().getDiskIO();
    private static final Executor NETWORK_IO = AppExecutors.getInstance().getNetworkIO();

    private static final String ACTION_FETCH_SEARCH = "com.github.rjbx.givetrack.data.action.FETCH_SEARCH";
    private static final String ACTION_FETCH_GIVING = "com.github.rjbx.givetrack.data.action.FETCH_GIVING";
    private static final String ACTION_FETCH_RECORD = "com.github.rjbx.givetrack.data.action.FETCH_RECORD";
    private static final String ACTION_FETCH_USER = "com.github.rjbx.givetrack.data.action.FETCH_USER";
    private static final String ACTION_REMOVE_SEARCH = "com.github.rjbx.givetrack.data.action.REMOVE_SEARCH";
    private static final String ACTION_REMOVE_GIVING = "com.github.rjbx.givetrack.data.action.REMOVE_GIVING";
    private static final String ACTION_REMOVE_RECORD = "com.github.rjbx.givetrack.data.action.REMOVE_RECORD";
    private static final String ACTION_REMOVE_USER = "com.github.rjbx.givetrack.data.action.REMOVE_USER";     
    private static final String ACTION_RESET_SEARCH = "com.github.rjbx.givetrack.data.action.RESET_SEARCH";
    private static final String ACTION_RESET_GIVING = "com.github.rjbx.givetrack.data.action.RESET_GIVING";
    private static final String ACTION_RESET_RECORD = "com.github.rjbx.givetrack.data.action.RESET_RECORD";
    private static final String ACTION_RESET_USER = "com.github.rjbx.givetrack.data.action.RESET_USER";
    private static final String ACTION_GIVE_SEARCH = "com.github.rjbx.givetrack.data.action.GIVE_SEARCH";
    private static final String ACTION_GIVE_RECORD = "com.github.rjbx.givetrack.data.action.GIVE_RECORD";
    private static final String ACTION_RECORD_GIVE = "com.github.rjbx.givetrack.data.action.RECORD_GIVE";
    private static final String ACTION_UPDATE_GIVING = "com.github.rjbx.givetrack.data.action.UPDATE_GIVING";
    private static final String ACTION_UPDATE_CONTACT = "com.github.rjbx.givetrack.data.action.UPDATE_CONTACT";
    private static final String ACTION_UPDATE_RECORD = "com.github.rjbx.givetrack.data.action.UPDATE_RECORD";
    private static final String ACTION_UPDATE_USER = "com.github.rjbx.givetrack.data.action.UPDATE_USER";
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
     * Starts this service to perform action FetchUser with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchUser(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_USER);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action GiveSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGiveSearch(Context context, Search search) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GIVE_SEARCH);
        intent.putExtra(EXTRA_ITEM_VALUES, search);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action GiveRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGiveRecord(Context context, Record record) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_GIVE_RECORD);
        intent.putExtra(EXTRA_ITEM_VALUES, record);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RecordGive with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRecordGive(Context context, Giving giving) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RECORD_GIVE);
        intent.putExtra(EXTRA_ITEM_VALUES, giving);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveSearch with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveSearch(Context context, Search... search) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_SEARCH);
        if (search.length > 1) intent.putExtra(EXTRA_LIST_VALUES, search);
        else intent.putExtra(EXTRA_ITEM_VALUES, search[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveGiving with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveGiving(Context context, Giving... giving) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_GIVING);
        if (giving.length > 1) intent.putExtra(EXTRA_LIST_VALUES, giving);
        else intent.putExtra(EXTRA_ITEM_VALUES, giving[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveRecord(Context context, Record... record) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_RECORD);
        if (record.length > 1) intent.putExtra(EXTRA_LIST_VALUES, record);
        else intent.putExtra(EXTRA_ITEM_VALUES, record[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveUser with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveUser(Context context, User... user) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_USER);
        if (user.length > 1) intent.putExtra(EXTRA_LIST_VALUES, user);
        else intent.putExtra(EXTRA_ITEM_VALUES, user[0]);
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
     * Starts this service to perform action ResetUser with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetUser(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_USER);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateGiving with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateGiving(Context context, Giving... giving) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_GIVING);
        intent.putExtra(EXTRA_ITEM_VALUES, giving);
        if (giving.length > 1) intent.putExtra(EXTRA_LIST_VALUES, giving);
        else intent.putExtra(EXTRA_ITEM_VALUES, giving[0]);

        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateRecord(Context context, Record... record) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_RECORD);
        intent.putExtra(EXTRA_ITEM_VALUES, record);
        if (record.length > 1) intent.putExtra(EXTRA_LIST_VALUES, record);
        else intent.putExtra(EXTRA_ITEM_VALUES, record[0]);

        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateUser with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateUser(Context context, User... user) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_USER);
        intent.putExtra(EXTRA_ITEM_VALUES, user);
        if (user.length > 1) intent.putExtra(EXTRA_LIST_VALUES, user);
        else intent.putExtra(EXTRA_ITEM_VALUES, user[0]);

        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateFrequency with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateFrequency(Context context) {
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_GIVING);
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
                handleActionGiveSearch(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_GIVE_RECORD:
                handleActionGiveRecord(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_RECORD_GIVE:
                handleActionRecordGive(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_REMOVE_SEARCH:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRemoveSearch(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Search.class));
                else handleActionRemoveSearch(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_REMOVE_GIVING:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRemoveGiving(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Giving.class));
                else handleActionRemoveGiving(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_REMOVE_RECORD:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRemoveRecord(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Record.class));
                else handleActionRemoveRecord(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_REMOVE_USER:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRemoveUser(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), User.class));
                else handleActionRemoveUser(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
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
            case ACTION_RESET_USER:
                handleActionResetUser();
                break;
            case ACTION_UPDATE_CONTACT:
                break;
            case ACTION_UPDATE_GIVING:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionUpdateGiving(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Giving.class));
                else handleActionUpdateGiving(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_UPDATE_RECORD:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionUpdateRecord(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Record.class));
                else handleActionUpdateRecord(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_UPDATE_USER:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionUpdateUser(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), User.class));
                else handleActionUpdateUser(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_RESET_DATA: handleActionResetData();
        }
        // TODO: Decide whether AppWidget refresh should occur here, inside accessor local update helpers or ContentProvider notify helper
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
            String uid = "";
            for (User user : DatabaseAccessor.getUser(this, null)) if (user.getActive()) uid = user.getUid();

            // Retrieve data
            String response = requestResponseFromUrl(url);
            if (response == null) return;
            Search[] parsedResponse = parseSearches(response, uid, single);

            // Store data
            DatabaseAccessor.removeSearch(this, (Search) null);
            DatabaseAccessor.addSearch(this, parsedResponse);
        });

        AppWidget.refresh(this);
    }

    /**
     * Handles action FetchGiving in the provided background thread.
     */
    private void handleActionFetchGiving() {

        // TODO: Implement

//        Uri.Builder templateBuilder = Uri.parse(FetchContract.BASE_URL).buildUpon();
//        templateBuilder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);
//        Uri template = templateBuilder.build();
//
//        List<String> charities = UserPreferences.getCharities(this);
//        if (charities.isEmpty() || charities.get(0).isEmpty()) return;
//
//        int charityCount = charities.size();
//        Giving[] givings = new Giving[charityCount];
//
//        NETWORK_IO.execute(() -> {
//
//            for (int i = 0; i < charityCount; i++) {
//
//                String[] charityData = charities.get(i).split(":");
//                Uri.Builder charityBuilder = Uri.parse(template.toString()).buildUpon();
//
//                charityBuilder.appendPath(charityData[0]);
//
//                // Append required parameters
//                charityBuilder.appendQueryParameter(FetchContract.PARAM_APP_ID, getString(R.string.cn_app_id));
//                charityBuilder.appendQueryParameter(FetchContract.PARAM_APP_KEY, getString(R.string.cn_app_key));
//
//                Uri charityUri = charityBuilder.build();
//
//                URL url = getUrl(charityUri);
//                Timber.e("Giving Fetched URL: %s", url);
//                String response = requestResponseFromUrl(url);
//                Timber.e("Giving Fetched Response: %s", response);
//                Search search = parseSearches(response, true)[0];
//                search.setPhone(charityData[1]);
//                search.setEmail(charityData[2]);
//                search.setImpact(charityData[4]);
//                Giving giving = new Giving(search, Integer.parseInt(charityData[5]), charityData[3]);
//                givings[i] = giving;
//            }
//
//            DatabaseAccessor.removeGiving(this, null);
//            DatabaseAccessor.addGiving(this, givings);
//        });
//
//        AppWidgetManager awm = AppWidgetManager.getInstance(this);
//        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
//        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    /**
     * Handles action FetchRecord in the provided background thread.
     */
    private void handleActionFetchRecord() {

        // TODO: Implement

//        Uri.Builder templateBuilder = Uri.parse(FetchContract.BASE_URL).buildUpon();
//        templateBuilder.appendPath(FetchContract.API_PATH_ORGANIZATIONS);
//        Uri template = templateBuilder.build();
//
//        List<String> charities = UserPreferences.getCharities(this);
//        if (charities.isEmpty() || charities.get(0).isEmpty()) return;
//
//        int charityCount = charities.size();
//        Record[] records = new Record[charityCount];
//
//        NETWORK_IO.execute(() -> {
//
//            for (int i = 0; i < charityCount; i++) {
//
//                String[] charityData = charities.get(i).split(":");
//                Uri.Builder charityBuilder = Uri.parse(template.toString()).buildUpon();
//
//                charityBuilder.appendPath(charityData[0]);
//
//                // Append required parameters
//                charityBuilder.appendQueryParameter(FetchContract.PARAM_APP_ID, getString(R.string.cn_app_id));
//                charityBuilder.appendQueryParameter(FetchContract.PARAM_APP_KEY, getString(R.string.cn_app_key));
//
//                Uri charityUri = charityBuilder.build();
//
//                URL url = getUrl(charityUri);
//                Timber.e("Record Fetched URL: %s", url);
//                String response = requestResponseFromUrl(url);
//                Timber.e("Record Fetched Response: %s", response);
//                Search search = parseSearches(response, true)[0];
//                search.setPhone(charityData[1]);
//                search.setEmail(charityData[2]);
//                search.setImpact(charityData[4]);
//                Record record = new Record(search, "", 0);
//                records[i] = record;
//            }
//
//            DatabaseAccessor.removeRecord(this, null);
//            DatabaseAccessor.addRecord(this, records);
//        });
//
//        AppWidgetManager awm = AppWidgetManager.getInstance(this);
//        int[] ids = awm.getAppWidgetIds(new ComponentName(this, AppWidget.class));
//        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    private void handleActionFetchUser() {}

    /**
     * Handles action GiveSearch in the provided background thread with the provided parameters.
     */
    private void handleActionGiveSearch(Search search) {

        NETWORK_IO.execute(() -> {

            float impact = 0f;
            int frequency = 0;

            List<Record> records = DatabaseAccessor.getRecord(this, null);
            for (Record record : records) {
                if (record.getEin().equals(search.getEin())) {
                    impact += Float.parseFloat(record.getImpact());
                    frequency++;
                }
            }

            List<Giving> givings = DatabaseAccessor.getGiving(this, null);
            int size = givings.size();
            double percent = size == 1 ? size : 0d;
            Giving giving = Giving.fromSuper(search);
            giving.setFrequency(frequency);
            giving.setPercent(percent);
            giving.setImpact(String.format(Locale.getDefault(), "%.2f", impact));

            String phoneNumber = urlToPhoneNumber(giving.getNavigatorUrl());
            giving.setPhone(phoneNumber);

            String emailAddress = urlToEmailAddress(giving.getHomepageUrl());
            giving.setEmail(emailAddress);

            DatabaseAccessor.addGiving(this, giving);
        });

        AppWidget.refresh(this);
    }

    private void handleActionGiveRecord(Record record) {
        int code = record.hashCode();
    }

    private void handleActionRecordGive(Giving giving) {

         long time = System.currentTimeMillis();
         Record record = Record.fromSuper(giving.getSuper());
         record.setStamp(time);
         record.setTime(time);
         DatabaseAccessor.addRecord(this, record);
    }

    /**
     * Handles action RemoveSearch in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveSearch(Search... searches) {

        DISK_IO.execute(() -> DatabaseAccessor.removeSearch(this, searches));

        AppWidget.refresh(this);
    }

    /**
     * Handles action RemoveGiving in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveGiving(Giving... givings) {

        DISK_IO.execute(() -> DatabaseAccessor.removeGiving(this, givings));

        AppWidget.refresh(this);
    }

    /**
     * Handles action RemoveRecord in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveRecord(Record... records) {

        DISK_IO.execute(() -> {
            DatabaseAccessor.removeRecord(this, records);

            List<Giving> givings = DatabaseAccessor.getGiving(this, null);
            for (Giving giving : givings) {
                for (Record record : records) {
                    if (record.getEin().equals(giving.getEin())) {
                        giving.setFrequency(giving.getFrequency() - 1);
                        float impact = Float.parseFloat(giving.getImpact()) - Float.parseFloat(record.getImpact());
                        giving.setImpact(String.format(Locale.getDefault(), "%.2f", impact));
                        DatabaseAccessor.addGiving(this, giving);
                        break;
                    }
                }
            }
        });

        AppWidget.refresh(this);
    }

    private void handleActionRemoveUser(User... users) {
        
        DISK_IO.execute(() -> {
               
            List<Search> searches = DatabaseAccessor.getSearch(this, null);
            List<Giving> givings = DatabaseAccessor.getGiving(this, null);
            List<Record> records = DatabaseAccessor.getRecord(this, null);
            
            for (User user : users) {
                for (Search search : searches) if (!search.getUid().equals(user.getUid())) DatabaseAccessor.removeSearch(this, search);
                for (Giving giving : givings) if (!giving.getUid().equals(user.getUid())) DatabaseAccessor.removeGiving(this, giving);
                for (Record record : records) if (!record.getUid().equals(user.getUid())) DatabaseAccessor.removeRecord(this, record);
            } DatabaseAccessor.removeUser(this, users);
        });
        
    }

    /**
     * Handles action ResetSearch in the provided background thread with the provided parameters.
     */
    private void handleActionResetSearch() {

        DISK_IO.execute(() -> DatabaseAccessor.removeSearch(this, (Search) null));

        AppWidget.refresh(this);
    }

    /**
     * Handles action ResetGiving in the provided background thread with the provided parameters.
     */
    private void handleActionResetGiving() {

        DISK_IO.execute(() -> DatabaseAccessor.removeGiving(this, (Giving) null));

        AppWidget.refresh(this);
    }

    /**
     * Handles action ResetRecord in the provided background thread with the provided parameters.
     */
    private void handleActionResetRecord() {

        DISK_IO.execute(() -> {
            DatabaseAccessor.removeRecord(this, (Record) null);
            List<Giving> givings = DatabaseAccessor.getGiving(this, null);
            for (Giving giving : givings) {
                giving.setImpact("0");
                giving.setFrequency(0);
            } DatabaseAccessor.addGiving(this, givings.toArray(new Giving[givings.size()]));
        });

        AppWidget.refresh(this);
    }

    private void handleActionResetUser() {
        DISK_IO.execute(() -> {
            DatabaseAccessor.removeSearch(this, (Search) null);
            DatabaseAccessor.removeGiving(this, (Giving) null);
            DatabaseAccessor.removeRecord(this, (Record) null);
            DatabaseAccessor.removeUser(this, (User) null);
        });
    }

    /**
     * Handles action UpdatePercent in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateGiving(Giving... givings) {

        boolean recalibrate = givings[0].getPercent() == -1d;
        // TODO: Dereference array element to update
        if (recalibrate) for (Giving giving : givings) giving.setPercent(1d / givings.length);

        DISK_IO.execute(() -> DatabaseAccessor.addGiving(this, givings));


        AppWidget.refresh(this);
    }

    private void handleActionUpdateRecord(Record... records) {}

    private void handleActionUpdateUser(User... user) {
        DISK_IO.execute(() -> DatabaseAccessor.addUser(this, user));

        AppWidget.refresh(this);
    }


    /**
     * Handles action UpdateTime in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateTime(long oldTime, long newTime) {

//        DISK_IO.execute(() -> {
//            String formattedTime = String.valueOf(oldTime);
//            Record record = DatabaseAccessor.getRecord(this, formattedTime).get(0);
//            record.setTime(newTime);
//            DatabaseAccessor.removeRecord(this, String.valueOf(oldTime));
//            DatabaseAccessor.addRecord(this, record);
//
//            updateTimePreferences(UserPreferences.getAnchor(this));
//        });

        AppWidget.refresh(this);
    }


    /**
     * Handles action ResetData in the provided background thread.
     */
    private void handleActionResetData() {

       DISK_IO.execute(() -> {
            DatabaseAccessor.removeSearch(this, (Search) null);
            DatabaseAccessor.removeGiving(this, (Giving) null);
            DatabaseAccessor.removeRecord(this, (Record) null);
            DatabaseAccessor.removeUser(this, (User) null);
        });
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        AppWidget.refresh(this);
    }

    private void updateTimePreferences(long anchorTime) {

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