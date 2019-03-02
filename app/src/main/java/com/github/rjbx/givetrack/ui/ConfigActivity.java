package com.github.rjbx.givetrack.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseAccessor;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.github.rjbx.givetrack.data.entry.User;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import static com.github.rjbx.givetrack.AppUtilities.DATE_FORMATTER;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_GIVING;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_RECORD;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_SEARCH;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;


// TODO: Add option to disable remote persistence, converting users to guests and deleting data
/**
 * Presents a set of application settings.
 */
public class ConfigActivity
        extends PreferenceActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_USER = "com.github.rjbx.givetrack.ui.arg.ITEM_USER";
    private static User mUser;

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_SEARCH: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_SEARCH, null, null, null, null);
            case LOADER_ID_GIVING: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_GIVING, null, null, null, null);
            case LOADER_ID_RECORD: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_RECORD, null, null, null, null);
            case LOADER_ID_USER: return new CursorLoader(this, DatabaseContract.UserEntry.CONTENT_URI_USER, null, null, null, null);
            default: throw new RuntimeException(this.getString(R.string.loader_error_message, id));
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            do {
                User user = User.getDefault();
                DatabaseAccessor.cursorRowToEntry(data, user);
                if (user.getActive()) mUser = user;
            } while (data.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) { mUser = null; }

    /**
     * Constructs the Settings UI.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mUser = getIntent().getParcelableExtra(ARG_ITEM_USER);
    }

    /**
     * Renders preference headers and related Fragments in dual panes
     * when device orientation is landscape.
     */
    @Override
    public boolean onIsMultiPane() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * Stops fragment injection in malicious applications.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || UserPreferenceFragment.class.getName().equals(fragmentName)
                || SearchPreferenceFragment.class.getName().equals(fragmentName)
                || GivingPreferenceFragment.class.getName().equals(fragmentName)
                || RecordPreferenceFragment.class.getName().equals(fragmentName)
                || AdvancedPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * Sets up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }
    }

    /**
     * Fragment bound to preference header for updating advanced settings.
     */
    private static void changeUser(Preference changedPreference, Object newValue) {

        if (newValue == null) return;
        String preferenceKey = changedPreference.getKey();
        Map<String, Object> map = mUser.toParameterMap();
        if (!map.containsKey(preferenceKey)) return;
        map.put(preferenceKey, newValue);
        mUser.fromParameterMap(map);
        DatabaseService.startActionUpdateUser(changedPreference.getContext(), mUser);
    }

    /**
     * Updates the preference summary to reflect its new value.
     */
    private static void changeSummary(Preference changedPreference, Object newValue) {

        if (newValue instanceof String) {
            String stringValue = newValue.toString();

            if (changedPreference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) changedPreference;
                int index = listPreference.findIndexOfValue(stringValue);

                changedPreference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (changedPreference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    changedPreference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            changedPreference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        changedPreference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(changedPreference.getContext());
                        changedPreference.setSummary(name);
                    }
                }
            } else changedPreference.setSummary(stringValue);
        }
    }

    /**
     * Binds preference summary to its value; the exact display format is dependent on preference type.
     *
     * @see #PreferenceActivity
     */
    private static void handlePreferenceChange(Preference preference, Preference.OnPreferenceChangeListener listener) {
        preference.setOnPreferenceChangeListener(listener);

        listener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getAll().get(preference.getKey()));
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            String action = getIntent().getAction();
            switch (action) {
                case RecordActivity.ACTION_RECORD_INTENT:
                    startActivity(new Intent(this, RecordActivity.class));
                    return true;
                case SearchActivity.ACTION_SEARCH_INTENT:
                    startActivity(new Intent(this, SearchActivity.class));
                    return true;
                case MainActivity.ACTION_MAIN_INTENT:
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Fragment bound to preference header for updating user settings.
     */
    public static class UserPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            DatePickerDialog.OnDateSetListener {


        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_user);
            setHasOptionsMenu(true);

            handlePreferenceChange(findPreference("example_text"), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_gender_key)), this);
            handlePreferenceChange(findPreference("example_list"), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_birthdate_key)), this);
        }

        /**
         * Updates the DatePicker with the date selected from the Dialog.
         */
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String birthdate = String.format("%s/%s/%s", year, month, dayOfMonth);
//            mUser.setBirthdate(birthdate);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            if (getString(R.string.pref_birthdate_key).equals(preference.getKey())) {
                Calendar calendar = Calendar.getInstance();
                String birthdate = mUser.getBirthdate();
                String[] birthdateParams = birthdate.split("/");
                calendar.set(Integer.parseInt(birthdateParams[0]), Integer.parseInt(birthdateParams[1]), Integer.parseInt(birthdateParams[2]));
                preference.setSummary(DATE_FORMATTER.format(calendar.getTime()));
                preference.setOnPreferenceClickListener(clickedPreference -> {
                    DatePickerDialog datePicker = new DatePickerDialog(
                            getActivity(),
                            UserPreferenceFragment.this,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    datePicker.show();
                    return false;
                });
            }

            ConfigActivity.changeSummary(preference, newValue);
            ConfigActivity.changeUser(preference, newValue);
            return true;
        }
    }
    /**
     * Fragment bound to preference header for updating search settings.
     */
    public static class SearchPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Dialog.OnClickListener {


        AlertDialog mClearDialog;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_search);
            setHasOptionsMenu(true);

            ListPreference statePref = (ListPreference) findPreference(getString(R.string.pref_state_key));
            if (statePref.getValue() == null)
                statePref.setValueIndex(statePref.getEntries().length - 1);

            ListPreference ratingPref = (ListPreference) findPreference(getString(R.string.pref_minrating_key));
            if (ratingPref.getValue() == null)
                ratingPref.setValueIndex(ratingPref.getEntries().length - 1);

            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_searchSort_key));
            if (sortPref.getValue() == null)
                sortPref.setValueIndex(sortPref.getEntries().length - 1);

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_searchOrder_key));
            if (orderPref.getValue() == null)
                orderPref.setValueIndex(orderPref.getEntries().length - 1);

            handlePreferenceChange(findPreference(getString(R.string.pref_filter_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_term_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_city_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_state_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_zip_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_minrating_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_pages_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_size_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_searchSort_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_searchOrder_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_company_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_focus_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_show_key)), this);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            String preferenceKey = preference.getKey();
            if (getString(R.string.pref_focus_key).equals(preferenceKey)) {
                if ((boolean) newValue) {
                    preference.setEnabled(true);
                } else preference.setEnabled(false);
                return true;
            } else if (getString(R.string.pref_clear_key).equals(preferenceKey)) {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return false;
            } else if (getString(R.string.pref_show_key).equals(preferenceKey)) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return false;
            }
            ConfigActivity.changeSummary(preference, newValue);
            ConfigActivity.changeUser(preference, newValue);
            return true;
        }
        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog == mClearDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        DatabaseService.startActionResetSearch(getActivity());
                        startActivity(new Intent(getActivity(), SearchActivity.class));
                        break;
                    default:
                }
            }
        }

    }
    /**
     * Fragment bound to preference header for updating giving settings.
     */
    public static class GivingPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            DialogInterface.OnClickListener,
            SeekBar.OnSeekBarChangeListener {

        AlertDialog mMagnitudeDialog;
        AlertDialog mRecalibrateDialog;
        AlertDialog mClearDialog;
        TextView mSeekReadout;

        int mSeekProgress;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_giving);
            setHasOptionsMenu(true);

            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_searchSort_key));
            if (sortPref.getValue() == null) {
                sortPref.setValueIndex(sortPref.getEntries().length - 1);
            }

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_searchOrder_key));
            if (orderPref.getValue() == null) {
                orderPref.setValueIndex(orderPref.getEntries().length - 1);
            }

            handlePreferenceChange(findPreference(getString(R.string.pref_recalibrate_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_magnitude_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_clear_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_show_key)), this);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            String preferenceKey = preference.getKey();
            if (getString(R.string.pref_magnitude_key).equals(preferenceKey)) {
                mSeekProgress = Math.round(Float.parseFloat(mUser.getMagnitude()) * 1000f);
                preference.setSummary(String.format("Change the magnitude of increments and decrements.\nThe current magnitude is %s", percentIntToDecimalString(mSeekProgress)));
                View view = getActivity().getLayoutInflater().inflate(R.layout.seekbar_main, new LinearLayout(getActivity()));
                SeekBar seekbar = view.findViewById(R.id.main_seekbar);
                mMagnitudeDialog = new AlertDialog.Builder(getActivity()).create();
                mSeekReadout = view.findViewById(R.id.main_readout);
                mSeekReadout.setText(percentIntToDecimalString(mSeekProgress));
                seekbar.setOnSeekBarChangeListener(this);
                seekbar.setProgress(mSeekProgress);
                mMagnitudeDialog.setView(view);
                mMagnitudeDialog.setMessage(this.getString(R.string.dialog_description_magnitude_adjustment));
                mMagnitudeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
                mMagnitudeDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
                mMagnitudeDialog.show();
                mMagnitudeDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mMagnitudeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
                return false;
            } else if (getString(R.string.pref_magnitude_key).equals(preferenceKey)) {
                mRecalibrateDialog = new AlertDialog.Builder(getActivity()).create();
                mRecalibrateDialog.setMessage(getActivity().getString(R.string.dialog_message_recalibrate));
                mRecalibrateDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
                mRecalibrateDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
                mRecalibrateDialog.show();
                mRecalibrateDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mRecalibrateDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
                return false;
            } else if (getString(R.string.pref_clear_key).equals(preferenceKey)) {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return false;
            } else if (getString(R.string.pref_show_key).equals(preferenceKey)) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return false;
            }
            ConfigActivity.changeSummary(preference, newValue);
            ConfigActivity.changeUser(preference, newValue);
            return true;
        }

        /**
         * Updates dialog readout to reflect adjustment.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mSeekProgress = progress;
            mSeekReadout.setText(percentIntToDecimalString(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog == mMagnitudeDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_POSITIVE:
                        mUser.setMagnitude(percentIntToDecimalString(mSeekProgress));
                        DatabaseService.startActionUpdateUser(getContext(), mUser);
                        Preference magnitudePreference = findPreference(getString(R.string.pref_magnitude_key));
                        magnitudePreference.setSummary(String.format("Change the magnitude of increments and decrements.\nThe current magnitude is %s", percentIntToDecimalString(mSeekProgress)));
                        break;
                    default:
                }
            } else if (dialog == mRecalibrateDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_POSITIVE:
                        mUser.setRatingReset(true);
                        DatabaseService.startActionUpdateUser(getActivity(), mUser);
                        break;
                    default:
                }
            } else if (dialog == mClearDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        DatabaseService.startActionResetGiving(getActivity());
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        break;
                    default:
                }
            }
        }
        /**
         * Converts whole number percentage to its decimal equivalent,
         * formatted as a String to preserve its precision.
         */
        private static String percentIntToDecimalString(int percentInt) {
            return String.format(Locale.getDefault(), "%.2f", percentInt / 1000f);
        }

    }

    /**
     * Fragment bound to preference header for updating giving settings.
     */
    public static class RecordPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener,
            DialogInterface.OnClickListener {


        AlertDialog mClearDialog;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_record);
            setHasOptionsMenu(true);

            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_recordSort_key));
            if (sortPref.getValue() == null) {
                sortPref.setValueIndex(sortPref.getEntries().length - 1);
            }

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_recordOrder_key));
            if (orderPref.getValue() == null) {
                orderPref.setValueIndex(orderPref.getEntries().length - 1);
            }

            findPreference(getString(R.string.pref_recordSort_key)).setOnPreferenceClickListener(this);
            findPreference(getString(R.string.pref_recordOrder_key)).setOnPreferenceClickListener(this);
            handlePreferenceChange(findPreference(getString(R.string.pref_clear_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_show_key)), this);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            ConfigActivity.changeSummary(preference, newValue);
            ConfigActivity.changeUser(preference, newValue);
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String preferenceKey = preference.getKey();
            if (getString(R.string.pref_clear_key).equals(preferenceKey)) {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return true;
            } else if (getString(R.string.pref_show_key).equals(preferenceKey)) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return true;
            }
            return false;
        }

        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog == mClearDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        DatabaseService.startActionResetRecord(getActivity());
                        startActivity(new Intent(getActivity(), RecordActivity.class));
                        break;
                    default:
                }
            }
        }

    }
    /**
     * Fragment bound to preference header for updating notification settings.
     */
    public static class NotificationPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);
            handlePreferenceChange(findPreference("notifications_new_message_ringtone"), this);
        }
        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ConfigActivity.changeSummary(preference, newValue);
            ConfigActivity.changeUser(preference, newValue);
            return true;
        }

    }
    public static class AdvancedPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            DialogInterface.OnClickListener {


        AlertDialog mDeleteDialog;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_advanced);
            setHasOptionsMenu(true);
            handlePreferenceChange(findPreference("sync_frequency"), this);

            Preference deletePreference = findPreference(getString(R.string.pref_delete_key));
            deletePreference.setOnPreferenceClickListener(clickedPreference -> {
                mDeleteDialog = new AlertDialog.Builder(getActivity()).create();
                mDeleteDialog.setMessage(getActivity().getString(R.string.dialog_description_account_deletion));
                mDeleteDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mDeleteDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_erase), this);
                mDeleteDialog.show();
                mDeleteDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mDeleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return false;
            });
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ConfigActivity.changeSummary(preference, newValue);
            ConfigActivity.changeUser(preference, newValue);
            return true;
        }
        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog == mDeleteDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        mDeleteDialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        startActivity(new Intent(getActivity(), AuthActivity.class).setAction(AuthActivity.ACTION_DELETE_ACCOUNT));
                        break;
                    default:
                }
            }
        }

    }
}