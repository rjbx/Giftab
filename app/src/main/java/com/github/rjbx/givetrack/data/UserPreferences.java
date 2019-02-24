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

// TODO: Replace with direct changes to User objects synced with DatabaseService and accessed with LoaderCallbacks
/**
 * Provides constants and static helpers for retrieving and setting {@link SharedPreferences}.
 */
public class UserPreferences {

    public static final String KEY_ACTIVE = "active";
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
    public static final String KEY_COMPANY = "company";
    public static final String KEY_VIEWTRACK = "viewtrack";
    public static final String KEY_RECORDS = "records";
    public static final String KEY_SEARCHGUIDE = "searchguide";
    public static final String KEY_HISTORICAL = "historical";
    public static final String KEY_ANCHOR = "anchor";
    public static final String KEY_TIMETRACK = "timetrack";

    public static final String LAST_PREFERENCE = KEY_TIMETRACK;

    // TODO: Replace by comparing last update time and either pushing to or pulling from remote based on more recently updated database
    public static List<String> getCharities(Context context) {
        Set<String> defaultValue = new LinkedHashSet<>();
        defaultValue.add("");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return new ArrayList<>(sp.getStringSet(KEY_CHARITIES, defaultValue));
    }

    public static String getMagnitude(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_MAGNITUDE, "0.01");
    }

    public static String getDonation(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(KEY_DONATION, "0");
    }

    public static void setDonation(Context context, String donation) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(KEY_DONATION, donation).apply();
    }

    public static void setTheme(Context context, int theme) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(KEY_THEME, theme).apply();
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
}