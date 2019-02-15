package com.github.rjbx.givetrack.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.UserPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// TODO: Add option to disable remote persistence, converting users to guests and deleting data
/**
 * Presents a set of application settings.
 */
public class ConfigActivity extends PreferenceActivity {

    /**
     * Constructs the Settings UI.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Renders preference headers and related Fragments in dual panes
     * when device orientation is landscape.
     */
    @Override public boolean onIsMultiPane() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * Stops fragment injection in malicious applications.
     */
    @Override protected boolean isValidFragment(String fragmentName) {
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
     * Updates the preference summary to reflect its new value.
     */
    private static boolean changePreference(Preference changedPreference, Object newValue) {
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
        UserPreferences.updateFirebaseUser(changedPreference.getContext());
        return true;
    }

    /**
     * Binds preference summary to its value; the exact display format is dependent on preference type.
     *
     * @see #PreferenceActivity
     */
    private static void bindPreferenceSummaryToValue(Preference preference, Preference.OnPreferenceChangeListener listener) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
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
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_user);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("example_text"), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gender_key)), this);
            bindPreferenceSummaryToValue(findPreference("example_list"), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_birthdate_key)), this);

            Preference datePreference = findPreference(getString(R.string.pref_birthdate_key));
            Calendar calendar = Calendar.getInstance();
            String birthdate = UserPreferences.getBirthdate(getActivity());
            String[] birthdateParams = birthdate.split("/");
            calendar.set(Integer.parseInt(birthdateParams[0]), Integer.parseInt(birthdateParams[1]), Integer.parseInt(birthdateParams[2]));
            datePreference.setSummary(SimpleDateFormat.getDateInstance().format(calendar.getTime()));
            datePreference.setOnPreferenceClickListener(clickedPreference -> {
                    DatePickerDialog datePicker = new DatePickerDialog(
                            getActivity(),
                            UserPreferenceFragment.this,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    datePicker.show();
                    return false;
                });
            datePreference.setOnPreferenceChangeListener((changedPreference, newValue) -> false);
        }

        /**
         * Updates the DatePicker with the date selected from the Dialog.
         */
        @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String birthdate = String.format("%s/%s/%s", year, month, dayOfMonth);
            UserPreferences.setBirthdate(getActivity(), birthdate);
            UserPreferences.updateFirebaseUser(getActivity());
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
            return ConfigActivity.changePreference(preference, newValue);
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
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_search);
            setHasOptionsMenu(true);

            ListPreference statePref = (ListPreference) findPreference(getString(R.string.pref_state_key));
            if (statePref.getValue() == null) statePref.setValueIndex(statePref.getEntries().length - 1);

            ListPreference ratingPref = (ListPreference) findPreference(getString(R.string.pref_minrating_key));
            if (ratingPref.getValue() == null) ratingPref.setValueIndex(ratingPref.getEntries().length - 1);

            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_sortSearch_key));
            if (sortPref.getValue() == null) sortPref.setValueIndex(sortPref.getEntries().length - 1);

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_orderSearch_key));
            if (orderPref.getValue() == null) orderPref.setValueIndex(orderPref.getEntries().length - 1);

            EditTextPreference einPref = (EditTextPreference) findPreference(getString(R.string.pref_ein_key));

            SwitchPreference focusPref = (SwitchPreference) findPreference(getString(R.string.pref_focus_key));
            focusPref.setOnPreferenceChangeListener((changedPreference, newValue) -> {
                    if ((boolean) newValue) {
                        einPref.setEnabled(true);
                    } else einPref.setEnabled(false);
                    return true;
            });

            Preference unsavePreference = findPreference(getString(R.string.pref_clear_key));
            unsavePreference.setOnPreferenceClickListener(clickedPreference -> {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return false;
            });

            Preference showPreference = findPreference("showAll");
            showPreference.setOnPreferenceClickListener(clickedPreference -> {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return false;
            });

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_term_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_city_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_state_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_zip_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_minrating_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_term_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_pages_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_size_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sortSearch_key)), this);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_orderSearch_key)), this);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
            return ConfigActivity.changePreference(preference, newValue);
        }

        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override public void onClick(DialogInterface dialog, int which) {
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
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_giving);
            setHasOptionsMenu(true);

            Preference magnitudePreference = findPreference(getString(R.string.pref_magnitude_key));
            mSeekProgress = Math.round(Float.parseFloat(UserPreferences.getMagnitude(getActivity())) * 1000f);
            magnitudePreference.setSummary(String.format("Change the magnitude of increments and decrements.\nThe current magnitude is %s", percentIntToDecimalString(mSeekProgress)));
            magnitudePreference.setOnPreferenceClickListener(clickedPreference -> {
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
            });

            Preference recalibratePreference = findPreference(getString(R.string.pref_recalibrate_key));
            recalibratePreference.setOnPreferenceClickListener(clickedPreference -> {
                mRecalibrateDialog = new AlertDialog.Builder(getActivity()).create();
                mRecalibrateDialog.setMessage(getActivity().getString(R.string.dialog_message_recalibrate));
                mRecalibrateDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
                mRecalibrateDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
                mRecalibrateDialog.show();
                mRecalibrateDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mRecalibrateDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
                return false;
            });

            Preference unsavePreference = findPreference(getString(R.string.pref_clear_key));
            unsavePreference.setOnPreferenceClickListener(clickedPreference -> {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return false;
            });

            Preference showPreference = findPreference("showAll");
            showPreference.setOnPreferenceClickListener(clickedPreference -> {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return false;
            });
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
            return ConfigActivity.changePreference(preference, newValue);
        }

        /**
         * Updates dialog readout to reflect adjustment.
         */
        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mSeekProgress = progress;
            mSeekReadout.setText(percentIntToDecimalString(progress));
        }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}

        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override public void onClick(DialogInterface dialog, int which) {
            if (dialog == mMagnitudeDialog) {
                switch (which) {
                    case AlertDialog.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case AlertDialog.BUTTON_POSITIVE:
                        UserPreferences.setMagnitude(getActivity(), percentIntToDecimalString(mSeekProgress));
                        UserPreferences.updateFirebaseUser(getActivity());
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
                        ContentValues values = new ContentValues();
                        values.putNull(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE);
                        DatabaseService.startActionUpdatePercentages(getActivity(), values);
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
            DialogInterface.OnClickListener {

        AlertDialog mClearDialog;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_record);
            setHasOptionsMenu(true);

            ListPreference sortPref = (ListPreference) findPreference(getString(R.string.pref_sortRecord_key));
            if (sortPref.getValue() == null) sortPref.setValueIndex(sortPref.getEntries().length - 1);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sortRecord_key)), this);

            ListPreference orderPref = (ListPreference) findPreference(getString(R.string.pref_orderRecord_key));
            if (orderPref.getValue() == null) orderPref.setValueIndex(orderPref.getEntries().length - 1);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_orderRecord_key)), this);
            
            Preference unsavePreference = findPreference(getString(R.string.pref_clear_key));
            unsavePreference.setOnPreferenceClickListener(clickedPreference -> {
                mClearDialog = new AlertDialog.Builder(getActivity()).create();
                mClearDialog.setMessage(getString(R.string.dialog_removal_charity, getString(R.string.snippet_all_charities)));
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mClearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mClearDialog.show();
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mClearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                return false;
            });

            Preference showPreference = findPreference("showAll");
            showPreference.setOnPreferenceClickListener(clickedPreference -> {
                String action = getActivity().getIntent().getAction();
                Intent intent = new Intent(getActivity(), ConfigActivity.class).setAction(action);
                startActivity(intent);
                return false;
            });
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
            return ConfigActivity.changePreference(preference, newValue);
        }

        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override public void onClick(DialogInterface dialog, int which) {
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
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"), this);
        }

        /**
         * Invokes helper method for setting preference summary to new preference value.
         */
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
            return ConfigActivity.changePreference(preference, newValue);
        }
    }

    /**
     * Fragment bound to preference header for updating advanced settings.
     */
    public static class AdvancedPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener,
            DialogInterface.OnClickListener {

        AlertDialog mDeleteDialog;

        /**
         * Inflates and provides logic for updating values of preference.
         */
        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_advanced);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("sync_frequency"), this);

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
        @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
            return ConfigActivity.changePreference(preference, newValue);
        }

        /**
         * Defines behavior onClick of each DialogInterface option.
         */
        @Override public void onClick(DialogInterface dialog, int which) {
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