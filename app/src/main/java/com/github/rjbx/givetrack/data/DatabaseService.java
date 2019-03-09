package com.github.rjbx.givetrack.data;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import com.github.rjbx.givetrack.AppExecutors;
import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.AppWidget;
import com.github.rjbx.givetrack.data.entry.Spawn;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.User;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import static com.github.rjbx.givetrack.data.DatabaseAccessor.DEFAULT_VALUE_STR;

// TODO: Extrapolate executors from service thread if possible or consolidate logic into the former or the latter
/**
 * Handles asynchronous task requests in a service on a separate handler thread.
 */
public class DatabaseService extends IntentService {

    private static final Executor DISK_IO = AppExecutors.getInstance().getDiskIO();
    private static final Executor NETWORK_IO = AppExecutors.getInstance().getNetworkIO();
    private static final Executor MAIN_THREAD = AppExecutors.getInstance().getMainThread();

    private static final String ACTION_FETCH_SPAWN = "com.github.rjbx.givetrack.data.action.FETCH_SPAWN";
    private static final String ACTION_FETCH_TARGET = "com.github.rjbx.givetrack.data.action.FETCH_TARGET";
    private static final String ACTION_FETCH_RECORD = "com.github.rjbx.givetrack.data.action.FETCH_RECORD";
    private static final String ACTION_FETCH_USER = "com.github.rjbx.givetrack.data.action.FETCH_USER";
    private static final String ACTION_REMOVE_SPAWN = "com.github.rjbx.givetrack.data.action.REMOVE_SPAWN";
    private static final String ACTION_REMOVE_TARGET = "com.github.rjbx.givetrack.data.action.REMOVE_TARGET";
    private static final String ACTION_REMOVE_RECORD = "com.github.rjbx.givetrack.data.action.REMOVE_RECORD";
    private static final String ACTION_REMOVE_USER = "com.github.rjbx.givetrack.data.action.REMOVE_USER";
    private static final String ACTION_RESET_SPAWN = "com.github.rjbx.givetrack.data.action.RESET_SPAWN";
    private static final String ACTION_RESET_TARGET = "com.github.rjbx.givetrack.data.action.RESET_TARGET";
    private static final String ACTION_RESET_RECORD = "com.github.rjbx.givetrack.data.action.RESET_RECORD";
    private static final String ACTION_RESET_USER = "com.github.rjbx.givetrack.data.action.RESET_USER";
    private static final String ACTION_TARGET_SPAWN = "com.github.rjbx.givetrack.data.action.GIVE_SPAWN";
    private static final String ACTION_RECORD_TARGET = "com.github.rjbx.givetrack.data.action.RECORD_TARGET";
    private static final String ACTION_UPDATE_TARGET = "com.github.rjbx.givetrack.data.action.UPDATE_TARGET";
    private static final String ACTION_UPDATE_CONTACT = "com.github.rjbx.givetrack.data.action.UPDATE_CONTACT";
    private static final String ACTION_UPDATE_RECORD = "com.github.rjbx.givetrack.data.action.UPDATE_RECORD";
    private static final String ACTION_UPDATE_USER = "com.github.rjbx.givetrack.data.action.UPDATE_USER";
    private static final String ACTION_RESET_DATA = "com.github.rjbx.givetrack.data.action.RESET_DATA";
    private static final String EXTRA_API_REQUEST = "com.github.rjbx.givetrack.data.extra.API_REQUEST";
    private static final String EXTRA_ITEM_VALUES = "com.github.rjbx.givetrack.data.extra.ITEM_VALUES";
    private static final String EXTRA_LIST_VALUES = "com.github.rjbx.givetrack.data.extra.LIST_VALUES";
    private static final String EXTRA_ITEM_ID = "com.github.rjbx.givetrack.data.extra.ITEM_ID";


    /**
     * Creates an {@link IntentService} instance.
     */
    public DatabaseService() {
        super(DatabaseService.class.getSimpleName());
    }

    // TODO: Add boolean returns for launching error message

    /**
     * Starts this service to perform action FetchSpawn with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchSpawn(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_SPAWN);
//        intent.putExtra(EXTRA_API_REQUEST, apiRequest);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action FetchTarget with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchTarget(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_TARGET);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action FetchRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFetchRecord(Context context) {
        if (context == null) return;
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
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_FETCH_USER);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action GiveSpawn with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGiveSpawn(Context context, Spawn spawn) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_TARGET_SPAWN);
        intent.putExtra(EXTRA_ITEM_VALUES, spawn);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RecordTarget with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRecordTarget(Context context, Target... target) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RECORD_TARGET);
        if (target.length > 1) intent.putExtra(EXTRA_LIST_VALUES, target);
        intent.putExtra(EXTRA_ITEM_VALUES, target[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveSpawn with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveSpawn(Context context, Spawn... spawns) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_SPAWN);
        if (spawns.length > 1) intent.putExtra(EXTRA_LIST_VALUES, spawns);
        else intent.putExtra(EXTRA_ITEM_VALUES, spawns[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveTarget with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveTarget(Context context, Target... target) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_TARGET);
        if (target.length > 1) intent.putExtra(EXTRA_LIST_VALUES, target);
        else intent.putExtra(EXTRA_ITEM_VALUES, target[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action RemoveRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveRecord(Context context, Record... record) {
        if (context == null) return;
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
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_REMOVE_USER);
        if (user.length > 1) intent.putExtra(EXTRA_LIST_VALUES, user);
        else intent.putExtra(EXTRA_ITEM_VALUES, user[0]);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetSpawn with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetSpawn(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_SPAWN);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetTargetwith the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetTarget(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_TARGET);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetRecord(Context context) {
        if (context == null) return;
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
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_USER);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateTargetwith the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateTarget(Context context, Target... target) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_TARGET);
        intent.putExtra(EXTRA_ITEM_VALUES, target);
        if (target.length > 1) intent.putExtra(EXTRA_LIST_VALUES, target);
        else intent.putExtra(EXTRA_ITEM_VALUES, target[0]);

        context.startService(intent);
    }

    /**
     * Starts this service to perform action UpdateRecord with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateRecord(Context context, Record... record) {
        if (context == null) return;
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
        if (context == null) return;
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
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_UPDATE_TARGET);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ResetData with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResetData(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, DatabaseService.class);
        intent.setAction(ACTION_RESET_DATA);
        context.startService(intent);
    }

    /**
     * Syncs data inside a worker thread on requests to process {@link Intent}.
     *
     * @param intent launches this {@link IntentService}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        final String action = intent.getAction();
        switch (action) {
            case ACTION_FETCH_SPAWN:
//                final HashMap fetchSpawnMap = (HashMap) intent.getSerializableExtra(EXTRA_API_REQUEST);
                handleActionFetchSpawn();
                break;
            case ACTION_FETCH_TARGET:
                handleActionFetchTarget();
                break;
            case ACTION_FETCH_RECORD:
                handleActionFetchRecord();
                break;
            case ACTION_FETCH_USER:
                handleActionFetchUser();
                break;
            case ACTION_TARGET_SPAWN:
                handleActionGiveSpawn(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_RECORD_TARGET:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRecordTarget(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Target.class));
                else handleActionRecordTarget(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_REMOVE_SPAWN:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRemoveSpawn(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Spawn.class));
                else handleActionRemoveSpawn(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
                break;
            case ACTION_REMOVE_TARGET:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionRemoveTarget(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Target.class));
                else handleActionRemoveTarget(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
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
            case ACTION_RESET_SPAWN:
                handleActionResetSpawn();
                break;
            case ACTION_RESET_TARGET:
                handleActionResetTarget();
                break;
            case ACTION_RESET_RECORD:
                handleActionResetRecord();
                break;
            case ACTION_RESET_USER:
                handleActionResetUser();
                break;
            case ACTION_UPDATE_CONTACT:
                break;
            case ACTION_UPDATE_TARGET:
                if (intent.hasExtra(EXTRA_LIST_VALUES))
                    handleActionUpdateTarget(AppUtilities.getTypedArrayFromParcelables(intent.getParcelableArrayExtra(EXTRA_LIST_VALUES), Target.class));
                else handleActionUpdateTarget(intent.getParcelableExtra(EXTRA_ITEM_VALUES));
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
            case ACTION_RESET_DATA:
                handleActionResetData();
        }
        // TODO: Decide whether AppWidget refresh should occur here, inside accessor local update helpers or ContentProvider notify helper
        AppWidget.refresh(this);
    }

    /**
     * Handles action FetchSpawn in the provided background thread with the provided parameters.
     */
    private void handleActionFetchSpawn() {
        NETWORK_IO.execute(() -> DatabaseAccessor.fetchSpawn(this));
    }

    /**
     * Handles action FetchTargetin the provided background thread.
     */
    private void handleActionFetchTarget() {
        NETWORK_IO.execute(() -> DatabaseAccessor.fetchTarget(this));
    }

    /**
     * Handles action FetchRecord in the provided background thread.
     */
    private void handleActionFetchRecord() {
        NETWORK_IO.execute(() -> DatabaseAccessor.fetchRecord(this));
    }

    private void handleActionFetchUser() {
        NETWORK_IO.execute(() -> DatabaseAccessor.fetchUser(this));
    }

    /**
     * Handles action GiveSpawn in the provided background thread with the provided parameters.
     */
    private void handleActionGiveSpawn(Spawn spawn) {

        NETWORK_IO.execute(() -> {

            float impact = 0f;
            int frequency = 0;

            List<Record> records = DatabaseAccessor.getRecord(this);
            for (Record record : records) {
                if (record.getEin().equals(spawn.getEin())) {
                    impact += Float.parseFloat(record.getImpact());
                    frequency++;
                }
            }

            List<Target> targets = DatabaseAccessor.getTarget(this);
            int size = targets.size();
            double percent = size == 1 ? size : 0d;
            Target target = Target.fromSuper(spawn);
            target.setFrequency(frequency);
            target.setPercent(percent);
            target.setImpact(String.format(Locale.getDefault(), "%.2f", impact));

            String phoneNumber = urlToPhoneNumber(target);
            target.setPhone(phoneNumber);

            String emailAddress = urlToEmailAddress(target);
            target.setEmail(emailAddress);

            String socialHandle = urlToSocialHandle(target);
            target.setSocial(socialHandle);

            DatabaseAccessor.addTarget(this, target);
        });
    }

    private void handleActionRecordTarget(Target... target) {

        Record[] record = new Record[target.length];
        for (int i = 0; i < target.length; i++) {
            long time = System.currentTimeMillis();
            record[i] = Record.fromSuper(target[i].getSuper());
            record[i].setStamp(time);
            record[i].setTime(time);
        }
        DatabaseAccessor.addRecord(this, record);
    }

    /**
     * Handles action RemoveSpawn in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveSpawn(Spawn... spawns) {
        DISK_IO.execute(() -> DatabaseAccessor.removeSpawn(this, spawns));
    }

    /**
     * Handles action RemoveTargetin the provided background thread with the provided parameters.
     */
    private void handleActionRemoveTarget(Target... targets) {
        DISK_IO.execute(() -> DatabaseAccessor.removeTarget(this, targets));
    }

    /**
     * Handles action RemoveRecord in the provided background thread with the provided parameters.
     */
    private void handleActionRemoveRecord(Record... records) {

        DISK_IO.execute(() -> {
            DatabaseAccessor.removeRecord(this, records);

            List<Target> targets = DatabaseAccessor.getTarget(this);
            for (Target target : targets) {
                for (Record record : records) {
                    if (record.getEin().equals(target.getEin())) {
                        target.setFrequency(target.getFrequency() - 1);
                        float impact = Float.parseFloat(target.getImpact()) - Float.parseFloat(record.getImpact());
                        target.setImpact(String.format(Locale.getDefault(), "%.2f", impact));
                        DatabaseAccessor.addTarget(this, target);
                        break;
                    }
                }
            }
        });
    }

    private void handleActionRemoveUser(User... users) {

        DISK_IO.execute(() -> {

            List<Spawn> spawns = DatabaseAccessor.getSpawn(this);
            List<Target> targets = DatabaseAccessor.getTarget(this);
            List<Record> records = DatabaseAccessor.getRecord(this);

            for (User user : users) {
                for (Spawn spawn : spawns)
                    if (!spawn.getUid().equals(user.getUid()))
                        DatabaseAccessor.removeSpawn(this, spawn);
                for (Target target : targets)
                    if (!target.getUid().equals(user.getUid()))
                        DatabaseAccessor.removeTarget(this, target);
                for (Record record : records)
                    if (!record.getUid().equals(user.getUid()))
                        DatabaseAccessor.removeRecord(this, record);
            }
            DatabaseAccessor.removeUser(this, users);
        });
    }

    /**
     * Handles action ResetSpawn in the provided background thread with the provided parameters.
     */
    private void handleActionResetSpawn() {
        DISK_IO.execute(() -> DatabaseAccessor.removeSpawn(this));
    }

    /**
     * Handles action ResetTargetin the provided background thread with the provided parameters.
     */
    private void handleActionResetTarget() {
        DISK_IO.execute(() -> DatabaseAccessor.removeTarget(this));
    }

    /**
     * Handles action ResetRecord in the provided background thread with the provided parameters.
     */
    private void handleActionResetRecord() {

        DISK_IO.execute(() -> {
            DatabaseAccessor.removeRecord(this);
            List<Target> targets = DatabaseAccessor.getTarget(this);
            for (Target target : targets) {
                target.setImpact("0");
                target.setFrequency(0);
            }
            DatabaseAccessor.addTarget(this, targets.toArray(new Target[targets.size()]));
        });
    }

    private void handleActionResetUser() {
        DISK_IO.execute(() -> {
            DatabaseAccessor.removeSpawn(this);
            DatabaseAccessor.removeTarget(this);
            DatabaseAccessor.removeRecord(this);
            DatabaseAccessor.removeUser(this);
        });
    }

    /**
     * Handles action UpdatePercent in the provided background thread with the provided parameters.
     */
    private void handleActionUpdateTarget(Target... targets) {
        DISK_IO.execute(() -> DatabaseAccessor.addTarget(this, targets));
    }

    private void handleActionUpdateRecord(Record... records) {
        DISK_IO.execute(() -> DatabaseAccessor.addRecord(this, records));
    }

    private void handleActionUpdateUser(User... user) {
        DISK_IO.execute(() -> DatabaseAccessor.addUser(this, user));
    }

    /**
     * Handles action ResetData in the provided background thread.
     */
    private void handleActionResetData() {

        DISK_IO.execute(() -> {
            DatabaseAccessor.removeSpawn(this);
            DatabaseAccessor.removeTarget(this);
            DatabaseAccessor.removeRecord(this);
            DatabaseAccessor.removeUser(this);
        });
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();

    }

//          TODO: Impelement retrieval from additional sources; alternative: Clearbit Enrichment API
    private String urlToSocialHandle(Target target) {
        String socialHandle = DEFAULT_VALUE_STR;
        String url = target.getHomepageUrl();
        if (url == null || url.isEmpty()) return socialHandle;
        try {
            List<String> socialHandles = urlToElementContent(url, "a", "twitter.com/", null, null, " ");
//            if (socialHandles.isEmpty()) {
//                String thirdPartyEngineUrl  = String.format(
//                        "https://site.org/profile/%s-%s",
//                        give.getEin().substring(0, 2),
//                        give.getEin().substring(2));
//                socialHandles = urlToElementContent(thirdPartyEngineUrl, "a", "/twitter.com/", null, null, " ");
//            }
//           if (socialHandles.isEmpty())) {
//                String spawnEngineUrl  = String.format(
//                        "https://webcache.googleusercontent.com/spawn?q=cache:%s",
//                        url);
//                socialHandles = urlToElementContent(spawnEngineUrl, "twitter.com/", null, null, null);
//            }
            if (!socialHandles.isEmpty()) {
                for (String handle : socialHandles) Timber.v("Social: @%s", handle);
                socialHandle = socialHandles.get(0);
            }
        } catch (IOException e) { Timber.e(e); }
        return socialHandle;
    }

    private String urlToEmailAddress(Target target) {
        String emailAddress = DEFAULT_VALUE_STR;
        String url = target.getHomepageUrl();
        if (url == null || url.isEmpty()) return DEFAULT_VALUE_STR;
        try {
            List<String> emailAddresses = urlToElementContent(url, "a", "mailto:", new String[] { "Donate", "Contact" }, null, " ");
//            if (emailAddresses.isEmpty()) {
//                String thirdPartyUrl = "";
//                if (!url.equals(thirdPartyUrl)) emailAddress = urlToElementContent();
//            }
//            if (emailAddress.equals(DEFAULT_VALUE_STR)) {
//                url.replace("http://", "").replace("https://", "").replace("www.", "");
//                String spawnEngineUrl  = String.format(
//                        "https://www.google.com/spawn?q=site%%3A%s+contact+OR+support+\"*%%40%s\"",
//                        url,
//                        url);
//                if (!url.equals(spawnEngineUrl)) emailAddress = (spawnEngineUrl, "mailto:", null, null, " ");
//            }
            if (!emailAddresses.isEmpty()) {
                for (String address : emailAddresses) Timber.v("Email: %s", address);
                emailAddress = emailAddresses.get(0);
            }
        } catch (IOException e) { Timber.e(e); }
        return emailAddress;
    }

    private String urlToPhoneNumber(Target target) {
        String phoneNumber = DEFAULT_VALUE_STR;
        String url = target.getNavigatorUrl();
        if (url == null || url.isEmpty()) return phoneNumber;
        try {
            List<String> phoneNumbers = urlToElementContent(url, "div[class=cn-appear]", "tel:", null, 15, "[^0-9]");
            if (!phoneNumbers.isEmpty()) {
                for (String number : phoneNumbers) Timber.v("Phone: %s", number);
                phoneNumber = phoneNumbers.get(0);
            }
        } catch (IOException e) { Timber.e(e);
        } return phoneNumber;
    }

    private List<String> urlToElementContent(@NonNull String url, String cssQuery, String key, @Nullable String[] pageNames, @Nullable Integer endIndex, @Nullable String removeRegex) throws IOException {

        Elements homeInfo = parseElements(url, cssQuery);
        List<String> infoList = new ArrayList<>();
        List<String> visitedLinks = new ArrayList<>();
        if (pageNames != null) {
            for (String pageName : pageNames) {
                infoList.addAll(parseKeysFromPages(url, homeInfo, pageName, visitedLinks, key));
            }
        } infoList.addAll(parseKeys(homeInfo, key, endIndex, removeRegex));
        return infoList;
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

    private Elements parseElements(String url, String cssQuery) throws IOException {
        Document homepage = Jsoup.connect(url).get();
        return homepage.select(cssQuery);
    }
}