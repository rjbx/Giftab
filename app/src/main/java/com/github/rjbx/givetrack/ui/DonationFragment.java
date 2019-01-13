package com.github.rjbx.givetrack.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import timber.log.Timber;

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

import com.github.rjbx.calibrater.Calibrater;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.DataService;
import com.github.rjbx.rateraid.Rateraid;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.rjbx.givetrack.ui.DonationFragment.ContactDialogLayout.mLocation;
import static com.github.rjbx.givetrack.ui.DonationFragment.ContactDialogLayout.mWebsite;

// TODO: Implement OnTouchListeners for repeating actions on button long presses

/**
 * Provides the logic and views for a donation management screen.
 * Presents a list of collected items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class DonationFragment extends Fragment
        implements CharityFragment.MasterDetailFlow,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TextView.OnEditorActionListener {

    private static final String STATE_PANE = "pane_state_donation";
    private static final String STATE_ADJUST = "adjust_state_donation";
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static ContentValues[] mValuesArray;
    private static boolean mDonationsAdjusted;
    private MainActivity mParentActivity;
    private CharityFragment mCharityFragment;
    private ListAdapter mListAdapter;
    private Unbinder unbinder;
    private float mAmountTotal;
    private float mMagnitude;
    private boolean mDualPane;
    @BindView(R.id.action_bar)
    ImageButton mActionBar;
    @BindView(R.id.action_bar_wrapper)
    View mBarWrapper;
    @BindView(R.id.save_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.donation_amount_text)
    EditText donationTotalText;
    @BindView(R.id.donation_amount_label)
    View donationTotalLabel;

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public DonationFragment() {
    }

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    public static DonationFragment newInstance(@Nullable Bundle args) {
        DonationFragment fragment = new DonationFragment();
        if (args != null) fragment.setArguments(args);
        return fragment;
    }

    /**
     * Generates a Layout for the Fragment.
     */
    @Override
    public @Nullable
    View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_donation, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        mAmountTotal = Float.parseFloat(UserPreferences.getDonation(getContext()));
        mMagnitude = Float.parseFloat(UserPreferences.getMagnitude(getContext()));
        mDonationsAdjusted = false;

        Bundle args = getArguments();
        if (args != null) {
            Parcelable[] parcelableArray = args.getParcelableArray(MainActivity.ARGS_VALUES_ARRAY);
            if (parcelableArray != null) {
                ContentValues[] valuesArray = (ContentValues[]) parcelableArray;
                if (mValuesArray != null && mValuesArray.length != valuesArray.length) {
                    mDonationsAdjusted = true;
                }
                mValuesArray = valuesArray;
            }
        }

        final EditText donationTotalText = rootView.findViewById(R.id.donation_amount_text);
        final View donationTotalLabel = rootView.findViewById(R.id.donation_amount_label);

        donationTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
        donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, CURRENCY_FORMATTER.format(mAmountTotal)));

        if (savedInstanceState != null) {
            mDualPane = savedInstanceState.getBoolean(STATE_PANE);
            mDonationsAdjusted = savedInstanceState.getBoolean(STATE_ADJUST);
        } else
            mDualPane = rootView.findViewById(R.id.donation_detail_container).getVisibility() == View.VISIBLE;

        if (mParentActivity != null && mDualPane) showDualPane(getArguments());

        if (mListAdapter == null) mListAdapter = new ListAdapter();
        else if (getFragmentManager() != null) getFragmentManager().popBackStack();

        RecyclerView recyclerView = rootView.findViewById(R.id.donation_list);
        recyclerView.setAdapter(mListAdapter);

        mBarWrapper = rootView.findViewById(R.id.action_bar_wrapper);
        mActionBar = rootView.findViewById(R.id.action_bar);
        mProgressBar = rootView.findViewById(R.id.save_progress_bar);

        renderActionBar();

        return rootView;
    }

    /**
     * Saves reference to parent Activity, initializes Loader and updates Layout configuration.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        mParentActivity = (MainActivity) getActivity();
        if (mDualPane) showDualPane(getArguments());
    }

    /**
     * Ensures the parent Activity has been created and data has been retrieved before
     * invoking the method that references them in order to populate the UI.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mListAdapter != null) mListAdapter.swapValues();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * Saves Layout configuration state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PANE, mDualPane);
        outState.putBoolean(STATE_ADJUST, mDonationsAdjusted);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(UserPreferences.KEY_MAGNITUDE)) {
            mMagnitude = Float.parseFloat(UserPreferences.getMagnitude(getContext()));
        }
    }

    /**
     * Presents the list of items and item details side-by-side using two vertical panes.
     */
    @Override
    public void showDualPane(Bundle args) {

        mCharityFragment = CharityFragment.newInstance(args);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.donation_detail_container, mCharityFragment)
                .commit();

        DisplayMetrics metrics = new DisplayMetrics();
        mParentActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (width * .5f), ViewGroup.LayoutParams.MATCH_PARENT);
        mParentActivity.findViewById(R.id.donation_list).setLayoutParams(params);
        View container = mParentActivity.findViewById(R.id.donation_detail_container);
        container.setVisibility(View.VISIBLE);
        container.setLayoutParams(params);
    }

    /**
     * Presents the list of items in a single vertical pane, hiding the item details.
     */
    @Override
    public void showSinglePane() {
        getChildFragmentManager().beginTransaction().remove(mCharityFragment).commit();
        mParentActivity.findViewById(R.id.donation_list).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDualPane = false;
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_DONE:
                try {
                    mAmountTotal = CURRENCY_FORMATTER.parse(donationTotalText.getText().toString()).floatValue();
                    UserPreferences.setDonation(getContext(), String.valueOf(mAmountTotal));
                    UserPreferences.updateFirebaseUser(getContext());
                } catch (ParseException e) {
                    Timber.e(e);
                    return false;
                }
                donationTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
                donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, CURRENCY_FORMATTER.format(mAmountTotal)));
                updateAmounts();
                InputMethodManager inputMethodManager = mParentActivity != null ?
                        (InputMethodManager) mParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE) : null;
                if (inputMethodManager == null) return false;
                inputMethodManager.toggleSoftInput(0, 0);
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.donation_decrement_button)
    void decrementPercentage() {
        mAmountTotal += mMagnitude;
        UserPreferences.setDonation(getContext(), String.valueOf(mAmountTotal));
        UserPreferences.updateFirebaseUser(getContext());
        String formattedTotal = CURRENCY_FORMATTER.format(mAmountTotal);
        donationTotalText.setText(formattedTotal);
        donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, formattedTotal));
        updateAmounts();
    }

    @OnClick(R.id.donation_increment_button)
    void incrementPercentage() {
        if (mAmountTotal > 0f) {
            mAmountTotal -= mMagnitude;
            UserPreferences.setDonation(getContext(), String.valueOf(mAmountTotal));
            UserPreferences.updateFirebaseUser(getContext());
        }
        String formattedTotal = CURRENCY_FORMATTER.format(mAmountTotal);
        donationTotalText.setText(formattedTotal);
        donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, formattedTotal));
        updateAmounts();
    }

    @OnClick(R.id.action_bar)
    void syncAdjustments() {
        // Prevents multithreading issues on simultaneous sync operations due to constant stream of database updates.
        if (mDonationsAdjusted) {
            mListAdapter.syncDonations();
            mDonationsAdjusted = false;
            mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentDark)));
            mActionBar.setImageResource(R.drawable.action_sync);
        } else if (mAmountTotal > 0) {
            ContentValues values = new ContentValues();
            values.put(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY, 1);
            DataService.startActionUpdateFrequency(getContext(), values);
            UserPreferences.updateFirebaseUser(mParentActivity);
            mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorConversionDark)));
            mActionBar.setImageResource(R.drawable.action_sync);
        }
    }

    /**
     * Updates the amounts allocated to each charity on increment or decrement
     * of total donation amount.
     */
    private void updateAmounts() {
        renderActionBar();
        if (mValuesArray != null) mListAdapter.notifyDataSetChanged();
    }

    /**
     * Indicates whether the MasterDetailFlow is in dual pane mode.
     */
    public boolean isDualPane() {
        return mDualPane;
    }

    public static void resetDonationsAdjusted() {
        mDonationsAdjusted = false;
    }

    private void renderActionBar() {

        int barWrapperColor;
        int actionBarColor;
        int actionBarIcon;
        int progressBarVisibility;

        if (mDonationsAdjusted) {
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

        mBarWrapper.setBackgroundColor(getResources().getColor(barWrapperColor));
        mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(actionBarColor)));
        mActionBar.setImageResource(actionBarIcon);
        mProgressBar.setVisibility(progressBarVisibility);
    }

    /**
     * Populates {@link DonationFragment} {@link RecyclerView}.
     */
    public class ListAdapter
            extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private static final int VIEW_TYPE_CHARITY = 0;
        private static final int VIEW_TYPE_BUTTON = 1;

        private Float[] mPercentages;
        private ImageButton mLastClicked;
        private Rateraid.Builder mWeightsBuilder;

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.charity_primary)
            @Nullable
            TextView mNameView;
            @BindView(R.id.charity_secondary)
            @Nullable
            TextView mFrequencyView;
            @BindView(R.id.charity_tertiary)
            @Nullable
            TextView mImpactView;
            @BindView(R.id.donation_percentage_text)
            @Nullable
            EditText mPercentageView;
            @BindView(R.id.donation_amount_text)
            @Nullable
            TextView mAmountView;
            @BindView(R.id.donation_increment_button)
            @Nullable
            TextView mIncrementButton;
            @BindView(R.id.donation_decrement_button)
            @Nullable
            TextView mDecrementButton;
            @BindView(R.id.collection_add_button)
            @Nullable
            Button mAddButton;
            @BindView(R.id.share_button)
            @Nullable
            ImageButton mShareButton;
            @BindView(R.id.contact_button)
            @Nullable
            ImageButton mContactButton;
            @BindView(R.id.inspect_button)
            @Nullable
            ImageButton mInspectButton;
            private AlertDialog mAlertDialog;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @Optional @OnClick(R.id.collection_remove_button)
            void removeCollected(View v) {
                ContentValues values = mValuesArray[(int) v.getTag()];
                String name = values.getAsString(GivetrackContract.Entry.COLUMN_CHARITY_NAME);
                String ein = values.getAsString(GivetrackContract.Entry.COLUMN_EIN);
                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setMessage(mParentActivity.getString(R.string.dialog_removal_alert, name));
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep),
                        (neutralButtonOnClickDialog, neutralButtonOnClickPosition) -> dialog.dismiss());
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove),
                        (negativeButtonOnClickDialog, negativeButtonOnClickPosition) -> {
                            if (mDualPane) showSinglePane();
//                                if (mValuesArray.length == 1) onDestroy();
                            DataService.startActionRemoveCollected(getContext(), ein);
                        });
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            }

            @Optional @OnClick(R.id.inspect_button)
            void inspectCollected(View v) {

                ContentValues values = mValuesArray[(int) v.getTag()];
                String name = values.getAsString(GivetrackContract.Entry.COLUMN_CHARITY_NAME);
                String ein = values.getAsString(GivetrackContract.Entry.COLUMN_EIN);
                String navUrl = values.getAsString(GivetrackContract.Entry.COLUMN_NAVIGATOR_URL);
                if (mLastClicked != null && mLastClicked.equals(v)) mDualPane = !mDualPane;
                else mDualPane = true;

                if (mLastClicked != null)
                    mLastClicked.setImageResource(R.drawable.ic_baseline_expand_more_24px);
                mLastClicked = (ImageButton) v;

                int resId = mDualPane ? R.drawable.ic_baseline_expand_less_24px : R.drawable.ic_baseline_expand_more_24px;
                mLastClicked.setImageResource(resId);
                mLastClicked.invalidate();

                Bundle arguments = new Bundle();
                arguments.putString(CharityFragment.ARG_ITEM_NAME, name);
                arguments.putString(CharityFragment.ARG_ITEM_EIN, ein);
                arguments.putString(CharityFragment.ARG_ITEM_URL, navUrl);
                if (mDualPane) showDualPane(arguments);
                else showSinglePane();
            }

            @Optional @OnClick(R.id.share_button)
            void shareCollected(View v) {

                ContentValues values = mValuesArray[(int) v.getTag()];
                String name = values.getAsString(GivetrackContract.Entry.COLUMN_CHARITY_NAME);
                int frequency = values.getAsInteger(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY);
                float impact = values.getAsFloat(GivetrackContract.Entry.COLUMN_DONATION_IMPACT);
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

            @Optional @OnClick(R.id.contact_button)
            void viewContacts(View v) {
                mAlertDialog = new AlertDialog.Builder(getContext()).create();
                ContactDialogLayout alertLayout = ContactDialogLayout.getInstance(mAlertDialog, mValuesArray[(int) v.getTag()]);
                mAlertDialog.setView(alertLayout);
                mAlertDialog.show();
            }

        }

        public ListAdapter() {
            mPercentages = new Float[mValuesArray.length];
            mWeightsBuilder = Rateraid.with(mPercentages, mMagnitude, clickedView -> {
                float sum = 0;
                for (float percentage : mPercentages) sum += percentage;
                Timber.d("List[%s] : Sum[%s]", Arrays.asList(mPercentages).toString(), sum);
                mDonationsAdjusted = true;
                renderActionBar();
                mProgressBar.setVisibility(View.VISIBLE);
                notifyDataSetChanged();
            });
        }

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override
        public @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_CHARITY) view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_donation, parent, false);
            else view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.button_collect, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) return VIEW_TYPE_BUTTON;
            else return VIEW_TYPE_CHARITY;
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            if (position == getItemCount() - 1) {
                holder.mAddButton.setOnClickListener(clickedView -> {
                    Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                    startActivity(searchIntent);
                });
                return;
            }

            if (mValuesArray == null || mValuesArray.length == 0 || mValuesArray[position] == null
                    || mPercentages == null || mPercentages.length == 0 || mPercentages[position] == null)
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

            ContentValues values = mValuesArray[position];
            final String name = values.getAsString(GivetrackContract.Entry.COLUMN_CHARITY_NAME);
            final int frequency =
                    values.getAsInteger(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY);
            final float impact = Float.parseFloat(values.getAsString(GivetrackContract.Entry.COLUMN_DONATION_IMPACT));

            String mutableName = name;
            if (mutableName.length() > 30) {
                mutableName = mutableName.substring(0, 30);
                mutableName = mutableName.substring(0, mutableName.lastIndexOf(" ")).concat("...");
            }
            holder.mNameView.setText(mutableName);

            holder.mFrequencyView.setText(getString(R.string.indicator_donation_frequency, String.valueOf(frequency)));
            holder.mImpactView.setText(String.format(Locale.US, getString(R.string.indicator_donation_impact), currencyInstance.format(impact)));
            if (impact > 10)
                if (Build.VERSION.SDK_INT > 23)
                    holder.mImpactView.setTextAppearance(R.style.AppTheme_TextEmphasis);
                else
                    holder.mImpactView.setTextAppearance(getContext(), R.style.AppTheme_TextEmphasis);

            for (View view : holder.itemView.getTouchables()) view.setTag(position);

            holder.mPercentageView.setText(percentInstance.format(mPercentages[position]));
            holder.mAmountView.setText(currencyInstance.format(mPercentages[position] * mAmountTotal));

            if (!mDualPane)
                holder.mInspectButton.setImageResource(R.drawable.ic_baseline_expand_more_24px);
            else if (mDualPane && mLastClicked != null)
                mLastClicked.setImageResource(R.drawable.ic_baseline_expand_less_24px);

            final int adapterPosition = holder.getAdapterPosition();

            mWeightsBuilder.addButtonSet(holder.mIncrementButton, holder.mDecrementButton, adapterPosition);
            mWeightsBuilder.addValueEditor(holder.mPercentageView, adapterPosition);
        }

        /**
         * Returns the number of items to display.
         */
        @Override
        public int getItemCount() {
            return mValuesArray != null ? mValuesArray.length + 1 : 1;
        }

        /**
         * Swaps the Cursor after completing a load or resetting Loader.
         */
        void swapValues() {
            if (mPercentages.length != mValuesArray.length)
                mPercentages = Arrays.copyOf(mPercentages, mValuesArray.length);
            for (int i = 0; i < mPercentages.length; i++) {
                mPercentages[i] = Float.parseFloat(mValuesArray[i].getAsString(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE));
            }
            Calibrater.resetRatings(mPercentages, false);
            notifyDataSetChanged();
        }

        /**
         * Syncs donation percentage and amount mValues to table.
         */
        private void syncDonations() {
            if (mPercentages == null || mPercentages.length == 0) return;
            ContentValues[] valuesArray = new ContentValues[mPercentages.length];
            for (int i = 0; i < mPercentages.length; i++) {
                ContentValues values = new ContentValues();
                values.put(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE, String.valueOf(mPercentages[i]));
                Timber.d(mPercentages[i] + " " + mAmountTotal + " " + i + " " + mPercentages.length);
                valuesArray[i] = values;
            }
            DataService.startActionUpdatePercentages(getContext(), valuesArray);
        }
    }
    
    public static class ContactDialogLayout extends LinearLayout {

        Context mContext;
        static AlertDialog mAlertDialog;
        static String mPhone;
        static String mEmail;
        static String mWebsite;
        static String mLocation;
        @BindView(R.id.email_button) @Nullable Button emailButton;
        @BindView(R.id.phone_button) @Nullable Button phoneButton;
        @BindView(R.id.location_button) @Nullable Button locationButton;
        @BindView(R.id.website_button) @Nullable Button websiteButton;

        ContactDialogLayout(Context context) {
            super(context);
            mContext = context;
            LayoutInflater.from(mContext).inflate(R.layout.dialog_contact, this, true);
            ButterKnife.bind(this);

            if (emailButton != null)
                if (mEmail.isEmpty()) emailButton.setVisibility(View.GONE);
                else emailButton.setText(mEmail.toLowerCase());

            if (phoneButton != null)
                if (mPhone.isEmpty()) phoneButton.setVisibility(View.GONE);
                else phoneButton.setText(String.format("+%s", mPhone));

            if (websiteButton != null)
                if (mWebsite.isEmpty()) websiteButton.setVisibility(View.GONE);
                else websiteButton.setText(mWebsite.toLowerCase());

            if (locationButton != null)
                if (mLocation.isEmpty()) locationButton.setVisibility(View.GONE);
                else locationButton.setText(mLocation);
        }

        public static ContactDialogLayout getInstance(AlertDialog alertDialog, ContentValues values) {
            mAlertDialog = alertDialog;
            mEmail = values.getAsString(GivetrackContract.Entry.COLUMN_EMAIL_ADDRESS);
            mPhone = values.getAsString(GivetrackContract.Entry.COLUMN_PHONE_NUMBER);
            mWebsite = values.getAsString(GivetrackContract.Entry.COLUMN_HOMEPAGE_URL);
            mLocation = valuesToAddress(values);
            return new ContactDialogLayout(mAlertDialog.getContext());
        }

        private static String valuesToAddress(ContentValues values) {
            String street = values.getAsString(GivetrackContract.Entry.COLUMN_LOCATION_STREET);
            String detail = values.getAsString(GivetrackContract.Entry.COLUMN_LOCATION_DETAIL);
            String city = values.getAsString(GivetrackContract.Entry.COLUMN_LOCATION_CITY);
            String state = values.getAsString(GivetrackContract.Entry.COLUMN_LOCATION_STATE);
            String zip = values.getAsString(GivetrackContract.Entry.COLUMN_LOCATION_ZIP);

            return street + (detail.isEmpty() ? "" : '\n' + detail) + '\n' + city + ", " + state.toUpperCase() + " " + zip;
        }

        @Optional @OnClick(R.id.email_button) void launchEmail() {
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(Uri.parse("mailto:"));
            mailIntent.putExtra(Intent.EXTRA_EMAIL, mEmail);
            if (mailIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(mailIntent);
            }
        }

        @Optional @OnClick(R.id.phone_button) void launchPhone() {

            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + mPhone));
            if (phoneIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(phoneIntent);
            }
        }

        @Optional @OnClick(R.id.website_button) void launchWebsite() {
            new CustomTabsIntent.Builder()
                    .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                    .build()
                    .launchUrl(mContext, Uri.parse(mWebsite));
        }

        @Optional @OnClick(R.id.location_button) void launchMap() {

            Uri intentUri = Uri.parse("geo:0,0?q=" + mLocation);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(mapIntent);
        }
    }
}