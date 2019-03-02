package com.github.rjbx.givetrack;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.preference.PreferenceActivity;

import com.github.rjbx.givetrack.data.entry.Company;
import com.github.rjbx.givetrack.data.entry.User;
import com.github.rjbx.givetrack.ui.ConfigActivity;
import com.github.rjbx.givetrack.ui.MainActivity;
import com.github.rjbx.givetrack.ui.RecordActivity;
import com.github.rjbx.givetrack.ui.SearchActivity;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.NumberFormat;

public class AppUtilities {

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
            case MainActivity.ACTION_MAIN_INTENT:
                return ConfigActivity.GivingPreferenceFragment.class.getName();
            case SearchActivity.ACTION_SEARCH_INTENT:
                return ConfigActivity.SearchPreferenceFragment.class.getName();
            case RecordActivity.ACTION_RECORD_INTENT:
                return ConfigActivity.RecordPreferenceFragment.class.getName();
            default: throw new IllegalArgumentException(
                    String.format("Action must derive from %s, %s or %s",
                            MainActivity.ACTION_MAIN_INTENT,
                            SearchActivity.ACTION_SEARCH_INTENT,
                            RecordActivity.ACTION_RECORD_INTENT
                    ));
        }
    }
}
