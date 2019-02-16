package com.github.rjbx.givetrack.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

import timber.log.Timber;

import com.github.rjbx.calibrater.Calibrater;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.github.rjbx.rateraid.Rateraid;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


// TODO: Implement OnTouchListeners for repeating actions on button long presses

/**
 * Provides the logic and views for a donation management screen.
 * Presents a list of collected items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class GivingFragment extends Fragment implements
        DetailFragment.MasterDetailFlow,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TextView.OnEditorActionListener {

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final String STATE_PANE = "com.github.rjbx.givetrack.ui.state.RECORD_PANE";
    private static final String STATE_ADJUST = "com.github.rjbx.givetrack.ui.state.RECORD_ADJUST";
    private static final String STATE_POSITION = "com.github.rjbx.givetrack.ui.state.RECORD_POSITION";
    private static ContentValues[] sValuesArray;
    private static boolean sDualPane;
    private static boolean sPercentagesAdjusted;
    private static double[] sPercentages;
    private InputMethodManager mMethodManager;
    private MainActivity mParentActivity;
    private DetailFragment mDetailFragment;
    private ListAdapter mListAdapter;
    private Unbinder mUnbinder;
    private Timer mTimer;
    private int mPanePosition;
    private float mAmountTotal;
    private float mMagnitude;
    @BindView(R.id.save_progress_bar) ProgressBar mProgress;
    @BindView(R.id.action_bar) ImageButton mActionBar;
    @BindView(R.id.action_bar_wrapper) View mActionWrapper;
    @BindView(R.id.donation_wrapper) View mDonationWrapper;
    @BindView(R.id.donation_amount_text) EditText mTotalText;
    @BindView(R.id.donation_amount_label) View mTotalLabel;
    @BindView(R.id.donation_detail_container) View mDetailContainer;
    @BindView(R.id.donation_list) RecyclerView mRecyclerView;

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public GivingFragment() {}

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    public static GivingFragment newInstance(@Nullable Bundle args) {
        GivingFragment fragment = new GivingFragment();
        if (args != null) fragment.setArguments(args);
        return fragment;
    }

    /**
     * Generates a Layout for the Fragment.
     */
    @Override public @Nullable
    View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_donor, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mAmountTotal = Float.parseFloat(UserPreferences.getDonation(getContext()));
        mMagnitude = Float.parseFloat(UserPreferences.getMagnitude(getContext()));
        sPercentagesAdjusted = false;

        Bundle args = getArguments();
        if (args != null) {
            Parcelable[] parcelableArray = args.getParcelableArray(MainActivity.ARGS_GIVING_ATTRIBUTES);
            if (parcelableArray != null) {
                ContentValues[] valuesArray = (ContentValues[]) parcelableArray;
                if (sValuesArray != null && sValuesArray.length != valuesArray.length) {
                    sPercentagesAdjusted = true;
                }
                sValuesArray = valuesArray;
            }
        }

        mTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
        mTotalText.setOnEditorActionListener(this);
        mTotalLabel.setContentDescription(getString(R.string.description_donation_text, CURRENCY_FORMATTER.format(mAmountTotal)));

        if (savedInstanceState != null) {
            sDualPane = savedInstanceState.getBoolean(STATE_PANE);
            sPercentagesAdjusted = savedInstanceState.getBoolean(STATE_ADJUST);
            mPanePosition = savedInstanceState.getInt(STATE_POSITION);
        } else sDualPane = mDetailContainer.getVisibility() == View.VISIBLE;

        if (mParentActivity != null && sDualPane) showDualPane(getArguments());

        if (mListAdapter == null) mListAdapter = new ListAdapter();
        else if (getFragmentManager() != null) getFragmentManager().popBackStack();
        mRecyclerView.setAdapter(mListAdapter);

        renderActionBar();

        return rootView;
    }

    /**
     * Saves reference to parent Activity, initializes Loader and updates Layout configuration.
     */
    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        mParentActivity = (MainActivity) getActivity();
        if (sDualPane) showDualPane(getArguments());
    }

    /**
     * Ensures the parent Activity has been created and data has been retrieved before
     * invoking the method that references them in order to populate the UI.
     */
    @Override public void onResume() {
        super.onResume();

        if (mParentActivity != null) {
            View contentView = mParentActivity.getWindow().getDecorView().findViewById(R.id.main_drawer);
            contentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                if (mTotalText == null || mTotalText.hasFocus()) return;

                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);

                if (100 < (contentView.getHeight() - (r.bottom - r.top))) {
                    mDonationWrapper.setVisibility(View.GONE);
                    mActionWrapper.setVisibility(View.GONE);
                } else {
                    mDonationWrapper.setVisibility(View.VISIBLE);
                    mActionWrapper.setVisibility(View.VISIBLE);
                }

            });
            mMethodManager = (InputMethodManager) mParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (mListAdapter != null) mListAdapter.swapValues();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Registers this Activity to listen for magnitude preference changes generated from selecting related
     * {@link #mParentActivity} options {@link android.view.MenuItem}, as well as save item percentage changes.
     */
    @Override public void onPause() {
        super.onPause();
        if (sPercentagesAdjusted) syncPercentages();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Unbinds Butterknife from this Fragment.
     */
    @Override public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Saves Layout configuration state.
     */
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PANE, sDualPane);
        outState.putBoolean(STATE_ADJUST, sPercentagesAdjusted);
        outState.putInt(STATE_POSITION, mPanePosition);
    }

    /**
     * Applies magnitude preference changes generated from selecting related
     * {@link #mParentActivity} options {@link android.view.MenuItem}.
     */
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(UserPreferences.KEY_MAGNITUDE)) {
            mMagnitude = Float.parseFloat(UserPreferences.getMagnitude(getContext()));
        }
    }

    /**
     * Presents the list of items and item details side-by-side using two vertical panes.
     */
    @Override public void showDualPane(Bundle args) {

        mDetailFragment = DetailFragment.newInstance(args);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.donation_detail_container, mDetailFragment)
                .commit();

        DisplayMetrics metrics = new DisplayMetrics();
        mParentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (width * .5f), ViewGroup.LayoutParams.MATCH_PARENT);
        mRecyclerView.setLayoutParams(params);
        mDetailContainer.setVisibility(View.VISIBLE);
        mDetailContainer.setLayoutParams(params);
    }

    /**
     * Presents the list of items in a single vertical pane, hiding the item details.
     */
    @Override public void showSinglePane() {
        getChildFragmentManager().beginTransaction().remove(mDetailFragment).commit();
        mRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sDualPane = false;
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * Listens for and persists changes to text editor value.
     */
    @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_DONE:
                try {
                    String viewText = mTotalText.getText().toString();
                    if (viewText.contains("$")) mAmountTotal = CURRENCY_FORMATTER.parse(viewText).floatValue();
                    else if (!viewText.isEmpty()) mAmountTotal = Float.parseFloat(viewText);
                    else {
                        mTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
                        return false;
                    }
                    UserPreferences.setDonation(getContext(), String.valueOf(mAmountTotal));
                    UserPreferences.updateFirebaseUser(getContext());
                } catch (ParseException e) {
                    Timber.e(e);
                    return false;
                }
                mTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
                mTotalLabel.setContentDescription(getString(R.string.description_donation_text, CURRENCY_FORMATTER.format(mAmountTotal)));
                updateAmounts();
                if (mMethodManager == null) return false;
                mMethodManager.toggleSoftInput(0, 0);
                return true;
            default:
                return false;
        }
    }

    /**
     * Defines behavior on click of decrement amount button.
     */
    @OnClick(R.id.donation_decrement_button) void decrementAmount() {
        if (mAmountTotal > 0f) {
            mAmountTotal -= mMagnitude;
            UserPreferences.setDonation(getContext(), String.valueOf(mAmountTotal));
            UserPreferences.updateFirebaseUser(getContext());
        }
        String formattedTotal = CURRENCY_FORMATTER.format(mAmountTotal);
        mTotalText.setText(formattedTotal);
        mTotalLabel.setContentDescription(getString(R.string.description_donation_text, formattedTotal));
        updateAmounts();
    }

    /**
     * Defines behavior on click of increment amount button.
     */
    @OnClick(R.id.donation_increment_button) void incrementAmount() {
        mAmountTotal += mMagnitude;
        UserPreferences.setDonation(getContext(), String.valueOf(mAmountTotal));
        UserPreferences.updateFirebaseUser(getContext());
        String formattedTotal = CURRENCY_FORMATTER.format(mAmountTotal);
        mTotalText.setText(formattedTotal);
        mTotalLabel.setContentDescription(getString(R.string.description_donation_text, formattedTotal));
        updateAmounts();
    }

    /**
     * Defines behavior on click of sync adjustments button.
     */
    @OnClick(R.id.action_bar) void syncAdjustments() {
        // Prevents multithreading issues on simultaneous sync operations due to constant stream of database updates.
        if (sPercentagesAdjusted) {
            syncPercentages();
            mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentDark)));
            mActionBar.setImageResource(R.drawable.action_sync);
        } else if (mAmountTotal > 0) {
            syncDonations();
            mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorConversionDark)));
            mActionBar.setImageResource(R.drawable.action_sync);
        }
    }

    /**
     * Syncs donation percentage and amount mValues to database from which table is repopulated.
     */
    private void syncPercentages() {
        if (sPercentages == null || sPercentages.length == 0) return;
        ContentValues[] valuesArray = new ContentValues[sPercentages.length];
        for (int i = 0; i < sPercentages.length; i++) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE, String.valueOf(sPercentages[i]));
            Timber.d(sPercentages[i] + " " + mAmountTotal + " " + i + " " + sPercentages.length);
            valuesArray[i] = values;
        }
        DatabaseService.startActionUpdatePercentages(getContext(), valuesArray);
        sPercentagesAdjusted = false;
    }

    /**
     * Syncs donations to database.
     */
    private void syncDonations() {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY, 1);
        DatabaseService.startActionUpdateFrequency(getContext(), values);
        UserPreferences.updateFirebaseUser(mParentActivity);
    }

    /**
     * Schedules syncing of percentages a specified time interval after the last adjustment.
     */
    private void scheduleSyncPercentages() {
        if (!sPercentagesAdjusted) return;
        if (mTimer != null) {  // Where last adjustment occurs before the set time interval
            mTimer.cancel(); // Prevent scheduling multiple invocations
            mTimer.purge();
        }
        mTimer = new Timer();
        final Handler handler = new Handler();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    if (sPercentagesAdjusted) syncPercentages();
                    sPercentagesAdjusted = false;
                });
            }
        }, 2000); // Wait until this time interval has elapsed to sync percentages
    }

    /**
     * Updates the amounts allocated to each charity on increment or decrement
     * of total donation amount.
     */
    private void updateAmounts() {
        renderActionBar();
        if (sValuesArray != null) mListAdapter.notifyDataSetChanged();
    }

    /**
     * Contextually applies visual attributes to action bar.
     */
    private void renderActionBar() {

        int barWrapperColor;
        int actionBarColor;
        int actionBarIcon;
        int progressBarVisibility;

        if (sPercentagesAdjusted) {
            barWrapperColor = R.color.colorAccentDark;
            actionBarColor = R.color.colorAccent;
            actionBarIcon = R.drawable.action_save;
            progressBarVisibility = View.VISIBLE;
        } else if (mAmountTotal == 0f) {
            barWrapperColor = R.color.colorAttentionDark;
            actionBarColor = R.color.colorAttention;
            actionBarIcon = android.R.drawable.stat_sys_warning;
            progressBarVisibility = View.GONE;
        } else {
            actionBarColor = R.color.colorConversion;
            barWrapperColor = R.color.colorConversionDark;
            actionBarIcon = R.drawable.action_download;
            progressBarVisibility = View.GONE;
        }

        mActionWrapper.setBackgroundColor(getResources().getColor(barWrapperColor));
        mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(actionBarColor)));
        mActionBar.setImageResource(actionBarIcon);
        mProgress.setVisibility(progressBarVisibility);
    }

    /**
     * Indicates whether the MasterDetailFlow is in dual pane mode.
     */
    public boolean isDualPane() {
        return sDualPane;
    }

    /**
     * Populates {@link GivingFragment} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private static final int VIEW_TYPE_CHARITY = 0;
        private static final int VIEW_TYPE_BUTTON = 1;
        private ImageButton mLastClicked;
        private Rateraid.Arrays mRateraidArrays;

        /**
         * Initializes percentage array and percentage button click handler and view updater.
         */
        ListAdapter() {
            sPercentages = new double[sValuesArray.length];
            mRateraidArrays = Rateraid.with(sPercentages, mMagnitude, Calibrater.STANDARD_PRECISION, clickedView -> {
                float sum = 0;
                for (double percentage : sPercentages) sum += percentage;
                Timber.d("List[%s] : Sum[%s]", Arrays.asList(sPercentages).toString(), sum);
                sPercentagesAdjusted = true;
                scheduleSyncPercentages();
                renderActionBar();
                mProgress.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            });
        }

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_CHARITY) view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_donor, parent, false);
            else view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.button_collect, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Reserves the last element in the list to an add button for launching {@link SearchActivity}.
         */
        @Override public int getItemViewType(int position) {
            if (position == getItemCount() - 1) return VIEW_TYPE_BUTTON;
            else return VIEW_TYPE_CHARITY;
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            if (position == getItemCount() - 1) {
                holder.mAddButton.setOnClickListener(clickedView -> {
                    Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                    startActivity(searchIntent);
                });
                return;
            }

            if (sValuesArray == null || sValuesArray.length == 0 || sValuesArray[position] == null
                    || sPercentages == null || sPercentages.length == 0)
                return;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    && position == 0) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.donation_item_height));
                params.setMargins(
                        (int) getResources().getDimension(R.dimen.item_horizontal_margins),
                        (int) getResources().getDimension(R.dimen.item_initial_top_margin),
                        (int) getResources().getDimension(R.dimen.item_horizontal_margins),
                        (int) getResources().getDimension(R.dimen.item_default_vertical_margin));
                holder.itemView.setLayoutParams(params);
            }

            final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
            final NumberFormat percentInstance = NumberFormat.getPercentInstance();

            ContentValues values = sValuesArray[position];
            final String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
            final int frequency =
                    values.getAsInteger(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY);
            final float impact = Float.parseFloat(values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_IMPACT));

            String mutableName = name;
            if (mutableName.length() > 30) {
                mutableName = mutableName.substring(0, 30);
                mutableName = mutableName.substring(0, mutableName.lastIndexOf(" ")).concat("...");
            }
            holder.mNameView.setText(mutableName);

            String impactStr = NumberFormat.getCurrencyInstance().format(impact);
            int impactLength = impactStr.length();
            if (impactLength > 12) impactStr = String.format("%s%sM", impactStr.substring(0, impactLength - 11),
                    impactLength > 14 ? "" : "." + impactStr.substring(impactLength - 9, impactLength - 7));
            else if (impactLength > 6) impactStr = impactStr.substring(0, impactLength - 3);

            holder.mFrequencyView.setText(getString(R.string.indicator_donation_frequency, String.valueOf(frequency)));
            holder.mImpactView.setText(String.format(Locale.US, getString(R.string.indicator_donation_impact), impactStr));
            if (impact > 10)
                if (Build.VERSION.SDK_INT > 23) holder.mImpactView.setTextAppearance(R.style.AppTheme_TextEmphasis);
                else holder.mImpactView.setTextAppearance(getContext(), R.style.AppTheme_TextEmphasis);

            for (View view : holder.itemView.getTouchables()) view.setTag(position);

            double amount = sPercentages[position] * mAmountTotal;
            String amountStr = NumberFormat.getCurrencyInstance().format(amount);
            int amountLength = amountStr.length();
            if (amountLength > 12) amountStr = String.format("%s%sM", amountStr.substring(0, amountLength - 11),
                    amountLength > 14 ? "" : "." + amountStr.substring(amountLength - 9, amountLength - 7));
            else if (amountLength > 6) amountStr = amountStr.substring(0, amountLength - 3);

            holder.mPercentageView.setText(percentInstance.format(sPercentages[position]));
            holder.mAmountView.setText(amountStr);

            if (!sDualPane) holder.mInspectButton.setImageResource(R.drawable.ic_baseline_expand_more_24px);
            else if (sDualPane && mPanePosition == position) {
                mLastClicked = holder.mInspectButton;
                mLastClicked.setImageResource(R.drawable.ic_baseline_expand_less_24px);
            }

            final int adapterPosition = holder.getAdapterPosition();

            mRateraidArrays.addShifters(holder.mIncrementButton, holder.mDecrementButton, adapterPosition)
                           .addEditor(holder.mPercentageView, adapterPosition, mMethodManager, null);
        }

        /**
         * Returns the number of items to display.
         */
        @Override public int getItemCount() {
            return sValuesArray != null ? sValuesArray.length + 1 : 1;
        }

        /**
         * Swaps the Cursor after completing a load or resetting Loader.
         */
        private void swapValues() {
            if (sPercentages.length != sValuesArray.length)
                sPercentages = Arrays.copyOf(sPercentages, sValuesArray.length);
            for (int i = 0; i < sPercentages.length; i++) {
                sPercentages[i] = Float.parseFloat(sValuesArray[i].getAsString(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE));
            }
            boolean adjusted = Calibrater.recalibrateRatings(sPercentages, false, Calibrater.STANDARD_PRECISION);
            if (adjusted) syncPercentages();
            notifyDataSetChanged();
        }

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder implements DialogInterface.OnClickListener {

            @BindView(R.id.charity_primary) @Nullable TextView mNameView;
            @BindView(R.id.charity_secondary) @Nullable TextView mFrequencyView;
            @BindView(R.id.charity_tertiary) @Nullable TextView mImpactView;
            @BindView(R.id.donation_percentage_text) @Nullable EditText mPercentageView;
            @BindView(R.id.donation_amount_text) @Nullable TextView mAmountView;
            @BindView(R.id.donation_increment_button) @Nullable TextView mIncrementButton;
            @BindView(R.id.donation_decrement_button) @Nullable TextView mDecrementButton;
            @BindView(R.id.collection_add_button) @Nullable Button mAddButton;
            @BindView(R.id.share_button) @Nullable ImageButton mShareButton;
            @BindView(R.id.contact_button) @Nullable ImageButton mContactButton;
            @BindView(R.id.inspect_button) @Nullable ImageButton mInspectButton;
            private AlertDialog mContactDialog;
            private AlertDialog mRemoveDialog;
            private String mEin;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            /**
             * Defines behaviors on click of DialogInterface buttons.
             */
            @Override public void onClick(DialogInterface dialog, int which) {
                if (dialog == mRemoveDialog) {
                    switch (which) {
                        case AlertDialog.BUTTON_NEUTRAL:
                            dialog.dismiss();
                            break;
                        case AlertDialog.BUTTON_NEGATIVE:
                            if (sDualPane) showSinglePane();
//                                if (sValuesArray.length == 1) onDestroy();
                            DatabaseService.startActionRemoveGiving(getContext(), mEin);
                            break;
                        default:
                    }
                }
            }

            /**
             * Defines behavior on click of remove button.
             */
            @Optional @OnClick(R.id.collection_remove_button) void removeGiving(View v) {

                ContentValues values = sValuesArray[(int) v.getTag()];
                String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
                mEin = values.getAsString(DatabaseContract.Entry.COLUMN_EIN);

                mRemoveDialog = new AlertDialog.Builder(getContext()).create();
                mRemoveDialog.setMessage(mParentActivity.getString(R.string.dialog_removal_charity, name));
                mRemoveDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mRemoveDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mRemoveDialog.show();
                mRemoveDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mRemoveDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
            }

            /**
             * Defines behavior on click of inspect button.
             */
            @Optional @OnClick(R.id.inspect_button) void inspectGiving(View v) {

                int position = (int) v.getTag();
                ContentValues values = sValuesArray[position];
                String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
                String ein = values.getAsString(DatabaseContract.Entry.COLUMN_EIN);
                String navUrl = values.getAsString(DatabaseContract.Entry.COLUMN_NAVIGATOR_URL);
                if (mLastClicked != null && mLastClicked.equals(v)) sDualPane = !sDualPane;
                else sDualPane = true;

                if (mLastClicked != null) mLastClicked.setImageResource(R.drawable.ic_baseline_expand_more_24px);
                mLastClicked = (ImageButton) v;
                mPanePosition = position;

                int resId = sDualPane ? R.drawable.ic_baseline_expand_less_24px : R.drawable.ic_baseline_expand_more_24px;
                mLastClicked.setImageResource(resId);
                mLastClicked.invalidate();

                Bundle arguments = new Bundle();
                arguments.putString(DetailFragment.ARG_ITEM_NAME, name);
                arguments.putString(DetailFragment.ARG_ITEM_EIN, ein);
                arguments.putString(DetailFragment.ARG_ITEM_URL, navUrl);
                if (sDualPane) showDualPane(arguments);
                else showSinglePane();
            }

            /**
             * Defines behavior on click of share button.
             */
            @Optional @OnClick(R.id.share_button) void shareGiving(View v) {

                ContentValues values = sValuesArray[(int) v.getTag()];
                String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
                int frequency = values.getAsInteger(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY);
                float impact = values.getAsFloat(DatabaseContract.Entry.COLUMN_DONATION_IMPACT);

                Intent shareIntent = ShareCompat.IntentBuilder.from(mParentActivity)
                        .setType("text/plain")
                        .setText(String.format("My %s donations totaling %s to %s have been added to my personal record with #%s App!",
                                frequency,
                                CURRENCY_FORMATTER.format(impact),
                                name,
                                getString(R.string.app_name)))
                        .getIntent();
                startActivity(shareIntent);
            }

            /**
             * Defines behavior on click of contact button.
             */
            @Optional @OnClick(R.id.contact_button) void viewContacts(View v) {
                mContactDialog = new AlertDialog.Builder(getContext()).create();
                ContactDialogLayout alertLayout = ContactDialogLayout.getInstance(mContactDialog, sValuesArray[(int) v.getTag()]);
                mContactDialog.setView(alertLayout);
                mContactDialog.show();
            }
        }
    }

    /**
     * Provides an inflated layout populated with contact method buttons and associated
     * listeners predefined.
     */
    static class ContactDialogLayout extends LinearLayout {

        private Context mContext;
        private static AlertDialog mAlertDialog;
        private static String mPhone;
        private static String mEmail;
        private static String mWebsite;
        private static String mLocation;
        @BindView(R.id.email_button) @Nullable Button mEmailButton;
        @BindView(R.id.phone_button) @Nullable Button mPhoneButton;
        @BindView(R.id.location_button) @Nullable Button mLocationButton;
        @BindView(R.id.website_button) @Nullable Button mWebsiteButton;

        /**
         * Defines visibility and appearance of button according to associated content value.
         */
        private ContactDialogLayout(Context context) {
            super(context);
            mContext = context;
            LayoutInflater.from(mContext).inflate(R.layout.dialog_contact, this, true);
            ButterKnife.bind(this);

            if (mEmailButton != null)
                if (mEmail.isEmpty()) mEmailButton.setVisibility(View.GONE);
                else mEmailButton.setText(mEmail.toLowerCase());

            if (mPhoneButton != null)
                if (mPhone.isEmpty()) mPhoneButton.setVisibility(View.GONE);
                else mPhoneButton.setText(String.format("+%s", mPhone));

            if (mWebsiteButton != null)
                if (mWebsite.isEmpty()) mWebsiteButton.setVisibility(View.GONE);
                else mWebsiteButton.setText(mWebsite.toLowerCase());

            if (mLocationButton != null)
                if (mLocation.isEmpty()) mLocationButton.setVisibility(View.GONE);
                else mLocationButton.setText(mLocation);
        }

        /**
         * Initializes value instance fields and generates an instance of this layout.
         */
        public static ContactDialogLayout getInstance(AlertDialog alertDialog, ContentValues values) {
            mAlertDialog = alertDialog;
            mEmail = values.getAsString(DatabaseContract.Entry.COLUMN_EMAIL_ADDRESS);
            mPhone = values.getAsString(DatabaseContract.Entry.COLUMN_PHONE_NUMBER);
            mWebsite = values.getAsString(DatabaseContract.Entry.COLUMN_HOMEPAGE_URL);
            mLocation = valuesToAddress(values);
            return new ContactDialogLayout(mAlertDialog.getContext());
        }

        /**
         * Converts a set of ContentValues to a single formatted String.
         */
        private static String valuesToAddress(ContentValues values) {
            String street = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_STREET);
            String detail = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_DETAIL);
            String city = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_CITY);
            String state = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_STATE);
            String zip = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_ZIP);
            return street + (detail.isEmpty() ? "" : '\n' + detail) + '\n' + city + ", " + state.toUpperCase() + " " + zip;
        }

        /**
         * Defines behavior on click of email launch button.
         */
        @Optional @OnClick(R.id.email_button) void launchEmail() {
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(Uri.parse("mailto:"));
            mailIntent.putExtra(Intent.EXTRA_EMAIL, mEmail);
            if (mailIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(mailIntent);
            }
        }

        /**
         * Defines behavior on click of phone launch button.
         */
        @Optional @OnClick(R.id.phone_button) void launchPhone() {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + mPhone));
            if (phoneIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(phoneIntent);
            }
        }

        /**
         * Defines behavior on click of website launch button.
         */
        @Optional @OnClick(R.id.website_button) void launchWebsite() {
            new CustomTabsIntent.Builder()
                    .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                    .build()
                    .launchUrl(mContext, Uri.parse(mWebsite));
        }

        /**
         * Defines behavior on click of map launch button.
         */
        @Optional @OnClick(R.id.location_button) void launchMap() {
            Uri intentUri = Uri.parse("geo:0,0?q=" + mLocation);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(mapIntent);
        }
    }
}