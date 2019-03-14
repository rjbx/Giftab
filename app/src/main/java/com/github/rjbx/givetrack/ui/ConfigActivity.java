package com.github.rjbx.givetrack.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
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
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;

// TODO: Add change email option to UserPreferenceFragment
// TODO: Add anchor reset option for HomePreferenceFragment
// TODO: Add option to disable remote persistence, converting users to guests and deleting data
// TODO: Fully implement removed options
/**
 * Presents a set of application settings.
 */
public class ConfigActivity
        extends PreferenceActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_USER = "com.github.rjbx.givetrack.ui.arg.ITEM_USER";
    private static User mUser;

    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_USER: return new CursorLoader(this, DatabaseContract.UserEntry.CONTENT_URI_USER, null, null, null, null);
            default: throw new RuntimeException(this.getString(R.string.loader_error_message, id));
        }
    }

    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            do {
                User user = User.getDefault();
                DatabaseAccessor.cursorRowToEntry(data, user);
                if (user.getUserActive()) {
                    mUser = user;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    if (sharedPreferences.getAll().isEmpty())
                        mapToSharedPreferences(mUser.toParameterMap(), sharedPreferences);
                    break;
                }
            } while (data.moveToNext());
        }
    }

    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) { mUser = null; }

    /**
     * Constructs the Settings UI.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        getLoaderManager().initLoader(DatabaseContract.LOADER_ID_USER, null, this);
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
                || IndexPreferenceFragment.class.getName().equals(fragmentName)
                || HomePreferenceFragment.class.getName().equals(fragmentName)
                || JournalPreferenceFragment.class.getName().equals(fragmentName)
                || AdvancedPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * Sets up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Settings");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary, null)));
        }
    }

    /**
     * Fragment bound to preference header for updating advanced settings.
     */
    private static void changeUser(Preference changedPreference, Object newValue) {
        if (mUser == null || newValue == null) return;
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

        String preferenceKey = preference.getKey();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());

        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference, sharedPreferences.getAll().get(preferenceKey));
    }

    private static void handlePreferenceClick(Preference preference, Preference.OnPreferenceClickListener listener) {
        preference.setOnPreferenceClickListener(listener);
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            String action = getIntent().getAction();
            if (action != null) {
                switch (action) {
                    case JournalActivity.ACTION_RECORD_INTENT:
                        startActivity(new Intent(this, JournalActivity.class));
                        return true;
                    case IndexActivity.ACTION_SPAWN_INTENT:
                        startActivity(new Intent(this, IndexActivity.class));
                        return true;
                    case HomeActivity.ACTION_MAIN_INTENT:
                        startActivity(new Intent(this, HomeActivity.class));
                        return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Fragment bound to preference header for updating user settings.
     */
    public static class UserPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener,
            DatePickerDialog.OnDateSetListener {

        private Calendar mCalendar;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_user);
            setHasOptionsMenu(true);
       }

        @Override
        public void onResume() {
            super.onResume();
//            handlePreferenceChange(findPreference("example_text"), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_userGender_key)), this);
//            handlePreferenceChange(findPreference("example_list"), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_userBirthdate_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_userBirthdate_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_show_key)), this);
        }

        /**
         * Updates the DatePicker with the date selected from the Dialog.
         */
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(year, month, dayOfMonth);
            String birthdate = String.format("%s/%s/%s", month + 1, dayOfMonth, year);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(getString(R.string.pref_userBirthdate_key), birthdate).apply();
            handlePreferenceChange(findPreference(getString(R.string.pref_userBirthdate_key)), this);
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
            if (getString(R.string.pref_userBirthdate_key).equals(preferenceKey)) {
                mCalendar = Calendar.getInstance();
                String birthdate = mUser.getUserBirthdate();
                String[] birthdateParams = birthdate.split("/");
                mCalendar.set(Integer.parseInt(birthdateParams[2]), Integer.parseInt(birthdateParams[0]) - 1, Integer.parseInt(birthdateParams[1]));
                DatePickerDialog datePicker = new DatePickerDialog(
                        getActivity(),
                        UserPreferenceFragment.this,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
                return true;
            } else if (getString(R.string.pref_show_key).equals(preferenceKey)) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return true;
            } return false;
        }
    }

    /**
     * Fragment bound to preference header for updating spawn settings.
     */
    public static class IndexPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener,
            Dialog.OnClickListener {

        AlertDialog mClearDialog;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_index);
            setHasOptionsMenu(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            ListPreference statePref = (ListPreference) findPreference(getString(R.string.pref_indexState_key));
            if (statePref.getValue() == null)
                statePref.setValueIndex(statePref.getEntries().length - 1);

            ListPreference ratingPref = (ListPreference) findPreference(getString(R.string.pref_indexMinrating_key));
            if (ratingPref.getValue() == null)
                ratingPref.setValueIndex(ratingPref.getEntries().length - 1);

            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_indexSort_key));
            if (sortPref.getValue() == null)
                sortPref.setValueIndex(sortPref.getEntries().length - 1);

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_indexOrder_key));
            if (orderPref.getValue() == null)
                orderPref.setValueIndex(orderPref.getEntries().length - 1);

            handlePreferenceChange(findPreference(getString(R.string.pref_indexFilter_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexTerm_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexCity_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexState_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexZip_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexMinrating_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexPages_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexSize_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexSort_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexOrder_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_indexCompany_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_indexFocus_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_clear_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_show_key)), this);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String preferenceKey = preference.getKey();
            if (getString(R.string.pref_clear_key).equals(preferenceKey)) {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_record, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark, null));
                return true;
            } else if (getString(R.string.pref_indexFocus_key).equals(preferenceKey)) {
                if ((boolean) newValue) {
                    preference.setEnabled(true);
                } else preference.setEnabled(false);
                return true;
            }
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
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark, null));
                return true;
            } else if (getString(R.string.pref_show_key).equals(preferenceKey)) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return true;
            } return false;
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
                        DatabaseService.startActionResetSpawn(getActivity());
                        startActivity(new Intent(getActivity(), IndexActivity.class));
                        break;
                    default:
                }
            }
        }
    }

    /**
     * Fragment bound to preference header for updating target settings.
     */
    public static class HomePreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            Preference.OnPreferenceClickListener,
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
            addPreferencesFromResource(R.xml.pref_home);
            setHasOptionsMenu(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            handlePreferenceChange(findPreference(getString(R.string.pref_giveMagnitude_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_giveMagnitude_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_giveReset_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_clear_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_show_key)), this);
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
            if (getString(R.string.pref_giveMagnitude_key).equals(preferenceKey)) {
                String magnitudeStr = mUser.getGiveMagnitude();
                mSeekProgress = Math.round(Float.parseFloat(magnitudeStr) * 1000f);
                View view = getActivity().getLayoutInflater().inflate(R.layout.seekbar_home, new LinearLayout(getActivity()));
                SeekBar seekbar = view.findViewById(R.id.main_seekbar);
                mMagnitudeDialog = new AlertDialog.Builder(getActivity()).create();
                mSeekReadout = view.findViewById(R.id.main_readout);
                mSeekReadout.setText(percentIntToDecimalString(mSeekProgress));
                seekbar.setOnSeekBarChangeListener(this);
                seekbar.setProgress(mSeekProgress);
                mMagnitudeDialog.setView(view);
                mMagnitudeDialog.setMessage(getString(R.string.dialog_description_magnitude_adjustment, percentIntToDecimalString(mSeekProgress)));
                mMagnitudeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
                mMagnitudeDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
                mMagnitudeDialog.show();
                mMagnitudeDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mMagnitudeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark, null));
                return true;
            } else if (getString(R.string.pref_giveReset_key).equals(preferenceKey)) {
                mRecalibrateDialog = new AlertDialog.Builder(getActivity()).create();
                mRecalibrateDialog.setMessage(getActivity().getString(R.string.dialog_message_recalibrate));
                mRecalibrateDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
                mRecalibrateDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
                mRecalibrateDialog.show();
                mRecalibrateDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mRecalibrateDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark, null));
                return true;
            } else if (getString(R.string.pref_clear_key).equals(preferenceKey)) {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark, null));
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
         * Updates dialog readout to reflect adjustment.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mSeekProgress = progress;
            mSeekReadout.setText(percentIntToDecimalString(progress));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}

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
                        mUser.setGiveMagnitude(percentIntToDecimalString(mSeekProgress));
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(getString(R.string.pref_giveMagnitude_key), mUser.getGiveMagnitude()).apply();
                        Preference magnitudePreference = findPreference(getString(R.string.pref_giveMagnitude_key));
                        handlePreferenceChange(magnitudePreference, this);
                        break;
                    default:
                }
            } else if (dialog == mRecalibrateDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_POSITIVE:
                        mUser.setGiveReset(true);
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
                        DatabaseService.startActionResetTarget(getActivity());
                        startActivity(new Intent(getActivity(), HomeActivity.class));
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
     * Fragment bound to preference header for updating target settings.
     */
    public static class JournalPreferenceFragment extends PreferenceFragment implements
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
            addPreferencesFromResource(R.xml.pref_journal);
            setHasOptionsMenu(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_journalSort_key));
            if (sortPref.getValue() == null) {
                sortPref.setValueIndex(sortPref.getEntries().length - 1);
            }

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_journalOrder_key));
            if (orderPref.getValue() == null) {
                orderPref.setValueIndex(orderPref.getEntries().length - 1);
            }

            handlePreferenceChange(findPreference(getString(R.string.pref_journalSort_key)), this);
            handlePreferenceChange(findPreference(getString(R.string.pref_journalOrder_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_clear_key)), this);
            handlePreferenceClick(findPreference(getString(R.string.pref_show_key)), this);
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
                mClearDialog.setMessage(getString(R.string.dialog_removal_record, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark, null));
                return true;
            } else if (getString(R.string.pref_show_key).equals(preferenceKey)) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return false;
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
                        startActivity(new Intent(getActivity(), JournalActivity.class));
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
        }

        @Override
        public void onResume() {
            super.onResume();
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
            Preference.OnPreferenceClickListener,
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
            handlePreferenceClick(findPreference(getString(R.string.pref_show_key)), this);
//            handlePreferenceChange(findPreference("sync_frequency"), this);

            Preference deletePreference = findPreference(getString(R.string.pref_delete_key));
            deletePreference.setOnPreferenceClickListener(clickedPreference -> {
                mDeleteDialog = new AlertDialog.Builder(getActivity()).create();
                mDeleteDialog.setMessage(getActivity().getString(R.string.dialog_description_account_deletion));
                mDeleteDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mDeleteDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_erase), this);
                mDeleteDialog.show();
                mDeleteDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                mDeleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark, null));
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

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (getString(R.string.pref_show_key).equals(preference.getKey())) {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return true;
            } return false;
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
    
    private static void mapToSharedPreferences(Map<String, Object> map, SharedPreferences sp) {

        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            if (value instanceof String) sp.edit().putString(entry.getKey(), (String) value).apply();
            if (value instanceof Boolean) sp.edit().putBoolean(entry.getKey(), (Boolean) value).apply();
            if (value instanceof Integer) sp.edit().putInt(entry.getKey(), (Integer) value).apply();
            if (value instanceof Long) sp.edit().putLong(entry.getKey(), (Long) value).apply();
            if (value instanceof Float) sp.edit().putFloat(entry.getKey(), (Float) value).apply();
        }
    }
}