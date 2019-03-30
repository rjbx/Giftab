package com.github.rjbx.givetrack;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcelable;
import android.preference.PreferenceActivity;

import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.User;
import com.github.rjbx.givetrack.view.ConfigActivity;
import com.github.rjbx.givetrack.view.JournalActivity;
import com.github.rjbx.givetrack.view.HomeActivity;
import com.github.rjbx.givetrack.view.IndexActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public final class AppUtilities {

    public static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    public static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();
    public static final DateFormat DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.SHORT);

    public static <T extends Parcelable> T[] getTypedArrayFromParcelables(Parcelable[] parcelables, Class<T> arrayType) {
        T[] typedArray = (T[]) Array.newInstance(arrayType, parcelables.length);
        System.arraycopy(parcelables, 0, typedArray, 0, parcelables.length);
        return typedArray;
    }

    /**
     * Defines and launches Intent for displaying a {@link android.preference.PreferenceFragment}.
     */
    public static void launchPreferenceFragment(Context context, String action) {
        Intent filterIntent = new Intent(context, ConfigActivity.class);
        filterIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, getPreferenceFragmentName(action));
        filterIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        filterIntent.setAction(action);
        context.startActivity(filterIntent);
    }

    public static String getPreferenceFragmentName(String action) {
        switch (action) {
            case HomeActivity.ACTION_HOME_INTENT:
                return ConfigActivity.HomePreferenceFragment.class.getName();
            case IndexActivity.ACTION_INDEX_INTENT:
                return ConfigActivity.IndexPreferenceFragment.class.getName();
            case JournalActivity.ACTION_JOURNAL_INTENT:
                return ConfigActivity.JournalPreferenceFragment.class.getName();
            default:
                throw new IllegalArgumentException(
                        String.format("Action must derive from %s, %s or %s",
                                HomeActivity.ACTION_HOME_INTENT,
                                IndexActivity.ACTION_INDEX_INTENT,
                                JournalActivity.ACTION_JOURNAL_INTENT
                        ));
        }
    }

    public static void mapToSharedPreferences(Map<String, Object> map, SharedPreferences sp) {

        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            if (value instanceof String) sp.edit().putString(entry.getKey(), (String) value).apply();
            else if (value instanceof Boolean) sp.edit().putBoolean(entry.getKey(), (Boolean) value).apply();
            else if (value instanceof Integer) sp.edit().putString(entry.getKey(), String.valueOf(value)).apply();
            else if (value instanceof Long) sp.edit().putLong(entry.getKey(), (Long) value).apply();
            else if (value instanceof Float) sp.edit().putFloat(entry.getKey(), (Float) value).apply();
        }
    }

    public static String[] getArgs(String... strings) {
        int arrayLength = strings.length;
        String[] stringArray = new String[arrayLength];
        for (int i = 0; i < arrayLength; i++) stringArray[i] = strings[i];
        return stringArray;
    }

    /**
     * Generates a {@link User} from {@link SharedPreferences} and {@link FirebaseUser} attributes.
     */
    public static User convertRemoteToLocalUser(FirebaseUser firebaseUser) {

        String uid = "";
        String email = "";
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
            String firebaseEmail = firebaseUser.getEmail();
            if (firebaseEmail != null) email = firebaseUser.getEmail();
        }

        User user = User.getDefault();
        user.setUid(uid);
        user.setUserEmail(email);
        user.setUserActive(true);
        return user;
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
            try {
                entries.add(type.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Timber.e(e);
            }
            cursorRowToEntry(cursor, entries.get(i++));
        } while (cursor.moveToNext());
        return entries;
    }

    public static boolean dateIsCurrent(long dateStamp) {
        Calendar anchorCalendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        anchorCalendar.setTimeInMillis(dateStamp);
        currentCalendar.setTimeInMillis(System.currentTimeMillis());
        return anchorCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                anchorCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH) &&
                anchorCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
    }
}