package com.github.rjbx.givetrack.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides constants and static helpers for retrieving and setting {@link SharedPreferences}.
 */
public class UserPreferences {

    public static final String KEY_BIRTHDATE = "birthdate";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_THEME = "theme";
    public static final String KEY_TRACKED = "tracked";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_DONATION = "donation";
    public static final String KEY_CHARITIES = "charities";
    public static final String KEY_TERM = "term";
    public static final String KEY_CITY = "city";
    public static final String KEY_STATE = "state";
    public static final String KEY_ZIP = "zip";
    public static final String KEY_MINRATING = "minrating";
    public static final String KEY_FILTER = "filter";
    public static final String KEY_RECORDSORT = "sortRecord";
    public static final String KEY_SEARCHSORT = "sortSearch";
    public static final String KEY_RECORDORDER = "orderRecord";
    public static final String KEY_SEARCHORDER = "orderSearch";
    public static final String KEY_PAGES = "pages";
    public static final String KEY_ROWS = "rows";
    public static final String KEY_FOCUS = "focus";
    public static final String KEY_EIN = "ein";
    public static final String KEY_RECORDS = "records";
    public static final String KEY_HIGH = "high";
    public static final String KEY_TODAY = "today";
    public static final String KEY_HISTORICAL = "historical";
    public static final String KEY_ANCHOR = "anchor";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_TIMETRACK = "timetrack";

    public static final String LAST_PREFERENCE = KEY_TIMETRACK;
    
    public static List<String> getCharities(Context context) {
        Set<String> defaultValue = new LinkedHashSet<>();
        defaultValue.add("");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return new ArrayList<>(sp.getStringSet(KEY_CHARITIES, defaultValue));
    }

    public static void setCharities(Context context, List<String> charities) {
        Set<String> value = new LinkedHashSet<>(charities);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putStringSet(KEY_CHARITIES, value).apply();
    }

    public static String getTerm(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_TERM, "");
    }

    public static void setTerm(Context context, String term) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_TERM, term).apply();
    }

    public static String getCity(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_CITY, "");
    }

    public static void setCity(Context context, String city) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_CITY, city).apply();
    }

    public static String getState(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_STATE, "");
    }

    public static void setState(Context context, String state) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_STATE, state).apply();
    }

    public static String getZip(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_ZIP, "");
    }

    public static void setZip(Context context, String zip) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_ZIP, zip).apply();
    }

    public static String getMinrating(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_MINRATING, "");
    }

    public static void setMinrating(Context context, String minrating) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_MINRATING, minrating).apply();
    }

    public static boolean getFilter(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_FILTER, true);
    }

    public static void setFilter(Context context, boolean filter) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_FILTER, filter).apply();
    }

    public static String getSearchSort(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_SEARCHSORT, "NAME");
    }

    public static void setSearchSort(Context context, String sort) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_SEARCHSORT, sort).apply();
    }

    public static String getSearchOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_SEARCHORDER, "ASC");
    }

    public static void setSearchOrder(Context context, String order) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_SEARCHORDER, order).apply();
    }
    
    public static String getRecordSort(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_RECORDSORT, DatabaseContract.Entry.COLUMN_DONATION_TIME);
    }

    public static void setRecordSort(Context context, String sort) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_RECORDSORT, sort).apply();
    }

    public static String getRecordOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_RECORDORDER, "ASC");
    }

    public static void setRecordOrder(Context context, String order) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_RECORDORDER, order).apply();
    }

    public static String getPages(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_PAGES, "");
    }

    public static void setPages(Context context, String pages) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_PAGES, pages).apply();
    }

    public static String getRows(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_ROWS, "");
    }

    public static void setRows(Context context, String rows) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_ROWS, rows).apply();
    }

    public static String getGender(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_GENDER, "0");
    }

    public static void setGender(Context context, String gender) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_GENDER, gender).apply();
    }

    public static String getTracked(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_TRACKED, "0");
    }

    public static void setTracked(Context context, String tracked) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_TRACKED, tracked).apply();
    }

    public static String getMagnitude(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_MAGNITUDE, "0.01");
    }

    public static void setMagnitude(Context context, String magnitude) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_MAGNITUDE, magnitude).apply();
    }

    public static String getDonation(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_DONATION, "0");
    }

    public static void setDonation(Context context, String donation) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_DONATION, donation).apply();
    }
    
    public static String getHigh(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_HIGH, "0");
    }

    public static void setHigh(Context context, String high) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_HIGH, high).apply();
    }

    public static String getToday(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_TODAY, "0");
    }

    public static void setToday(Context context, String today) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_TODAY, today).apply();
    }

    public static String getBirthdate(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_BIRTHDATE, "0/0/2000");
    }

    public static void setBirthdate(Context context, String birthday) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_BIRTHDATE, birthday).apply();
    }

    public static List<String> getRecords(Context context) {
        Set<String> defaultValue = new LinkedHashSet<>();
        defaultValue.add("");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return new ArrayList<>(sp.getStringSet(KEY_RECORDS, defaultValue));
    }

    public static void setRecords(Context context, List<String> records) {
        Set<String> value = new LinkedHashSet<>(records);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putStringSet(KEY_RECORDS, value).apply();
    }

    public static int getTheme(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_THEME, 0);
    }

    public static void setTheme(Context context, int theme) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_THEME, theme).apply();
    }

    public static boolean getFocus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_FOCUS, false);
    }

    public static void setFocus(Context context, boolean focus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_FOCUS, focus).apply();
    }

    public static boolean getHistorical(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_HISTORICAL, false);
    }

    public static void setHistorical(Context context, boolean historical) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_HISTORICAL, historical).apply();
    }

    public static String getEin(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_EIN, "");
    }

    public static void setEin(Context context, String ein) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_EIN, ein).apply();
    }

    public static long getAnchor(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(KEY_ANCHOR, System.currentTimeMillis());
    }

    public static void setAnchor(Context context, long anchor) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(KEY_ANCHOR, anchor).apply();
    }

    public static long getTimestamp(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(KEY_TIMESTAMP, System.currentTimeMillis());
    }

    public static void setTimestamp(Context context, long timestamp) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(KEY_TIMESTAMP, timestamp).apply();
    }

    public static long getTimetrack(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(KEY_TIMETRACK, System.currentTimeMillis());
    }

    public static void setTimetrack(Context context, long timetrack) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(KEY_TIMETRACK, timetrack).apply();
    }

    /**
     * Replaces {@link SharedPreferences} with attributes of the given {@link UserProfile}.
     */
    public static void replaceSharedPreferences(@NonNull Context context, UserProfile user) {

        setCharities(context, user.getCharities());
        setBirthdate(context, user.getBirthdate());
        setGender(context, user.getGender());
        setTheme(context, user.getTheme());
        setDonation(context, user.getDonation());
        setCharities(context, user.getCharities());
        setTerm(context, user.getTerm());
        setCity(context, user.getCity());
        setState(context, user.getState());
        setZip(context, user.getZip());
        setMinrating(context, user.getMinrating());
        setFilter(context, user.getFilter());
        setSearchSort(context, user.getSearchSort());
        setSearchOrder(context, user.getSearchOrder());
        setPages(context, user.getPages());
        setRows(context, user.getRows());
        setFocus(context, user.getFocus());
        setEin(context, user.getEin());
        setRecords(context, user.getRecords());
        setHigh(context, user.getHigh());
        setHistorical(context, user.getHistorical());
        setAnchor(context, user.getAnchor());
        setTimestamp(context, user.getTimestamp());
        setTimetrack(context, user.getTimetrack());
    }

    /**
     * Generates a {@link UserProfile} from {@link SharedPreferences} and {@link FirebaseUser} attributes.
     */
    public static UserProfile generateUserProfile(@NonNull Context context) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return new UserProfile(
                firebaseUser == null ? "" : firebaseUser.getUid(),
                firebaseUser == null ? "" : firebaseUser.getEmail(),
                getBirthdate(context),
                getGender(context),
                getTheme(context),
                getTracked(context),
                getDonation(context),
                getMagnitude(context),
                getCharities(context),
                getTerm(context),
                getCity(context),
                getState(context),
                getZip(context),
                getMinrating(context),
                getFilter(context),
                getSearchSort(context),
                getSearchOrder(context),
                getRecordSort(context),
                getRecordOrder(context),
                getPages(context),
                getRows(context),
                getFocus(context),
                getEin(context),
                getRecords(context),
                getHigh(context),
                getToday(context),
                getHistorical(context),
                getAnchor(context),
                getTimestamp(context),
                getTimetrack(context));
    }

    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    public static void updateFirebaseUser(@NonNull Context context) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final UserProfile user = UserPreferences.generateUserProfile(context);
        firebaseDatabase.getReference("users").child(user.getUid())
                .updateChildren(user.toParameterMap());
    }
}