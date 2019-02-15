package com.github.rjbx.givetrack.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.entry.User;
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
    public static final String KEY_VIEWTRACK = "viewtrack";
    public static final String KEY_RECORDS = "records";
    public static final String KEY_HISTORICAL = "historical";
    public static final String KEY_ANCHOR = "anchor";
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
        return sp.getString(KEY_MINRATING, context.getResources().getStringArray(R.array.list_preference_minrating_values)[0]);
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
        return sp.getString(KEY_SEARCHSORT, context.getResources().getStringArray(R.array.list_preference_sortSearch_values)[0]);
    }

    public static void setSearchSort(Context context, String sort) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_SEARCHSORT, sort).apply();
    }

    public static String getSearchOrder(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_SEARCHORDER, context.getResources().getStringArray(R.array.list_preference_orderSearch_values)[0]);
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

    public static boolean getViewtrack(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(KEY_VIEWTRACK, true);
    }

    public static void setViewtrack(Context context, boolean viewtrack) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(KEY_VIEWTRACK, viewtrack).apply();
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

    public static long getTimetrack(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(KEY_TIMETRACK, System.currentTimeMillis());
    }

    public static void setTimetrack(Context context, long timetrack) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(KEY_TIMETRACK, timetrack).apply();
    }

    /**
     * Replaces {@link SharedPreferences} with attributes of the given {@link User}.
     */
    public static void replaceSharedPreferences(@NonNull Context context, User user) {

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
        setViewtrack(context, user.getViewtrack());
        setRecords(context, user.getRecords());
        setHistorical(context, user.getHistorical());
        setAnchor(context, user.getAnchor());
        setTimetrack(context, user.getTimetrack());
    }

    /**
     * Generates a {@link User} from {@link SharedPreferences} and {@link FirebaseUser} attributes.
     */
    public static User generateUserProfile(@NonNull Context context) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return new User(
                firebaseUser == null ? "" : firebaseUser.getUid(),
                firebaseUser == null ? "" : firebaseUser.getEmail(),
                getBirthdate(context),
                getGender(context),
                getTheme(context),
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
                getViewtrack(context),
                getRecords(context),
                getHistorical(context),
                getAnchor(context),
                getTimetrack(context));
    }

    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    public static void updateFirebaseUser(@NonNull Context context) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final User user = UserPreferences.generateUserProfile(context);
        firebaseDatabase.getReference("users").child(user.getUid())
                .updateChildren(user.toParameterMap());
    }
}