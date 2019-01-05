package com.github.rjbx.givetrack.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import android.util.DisplayMetrics;
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
import com.github.rjbx.rateraid.Rateraid;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.DataService;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

// TODO: Implement OnTouchListeners for repeating actions on button long presses

/**
 * Provides the logic and views for a donation management screen.
 * Presents a list of collected items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class DonationFragment extends Fragment implements CharityFragment.MasterDetailFlow {

    private static final String STATE_PANE = "pane_state_donation";
    private static ContentValues[] mValuesArray;
    private static boolean mDonationsAdjusted;
    private MainActivity mParentActivity;
    private CharityFragment mCharityFragment;
    private ListAdapter mListAdapter;
    private View mBarWrapper;
    private ImageButton mActionBar;
    private ProgressBar mProgressBar;
    private float mAmountTotal;
    private boolean mDualPane;

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public DonationFragment() {}

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
    public @Nullable View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_donation, container, false);


        mAmountTotal = UserPreferences.getDonation(getContext());
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

        final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();


        final EditText donationTotalText = rootView.findViewById(R.id.donation_amount_text);
        final View donationTotalLabel = rootView.findViewById(R.id.donation_amount_text);

        donationTotalText.setText(currencyFormatter.format(mAmountTotal));
        donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, currencyFormatter.format(mAmountTotal)));

        donationTotalText.setOnEditorActionListener(
                (onEditorActionView, onEditorActionId, onEditorActionEvent) -> {
                    switch (onEditorActionId) {
                        case EditorInfo.IME_ACTION_DONE:
                            try {
                                mAmountTotal = currencyFormatter.parse(onEditorActionView.getText().toString()).floatValue();
                                UserPreferences.setDonation(getContext(), mAmountTotal);
                                UserPreferences.updateFirebaseUser(getContext());
                            } catch (ParseException e) {
                                Timber.e(e);
                                return false;
                            }
                            donationTotalText.setText(currencyFormatter.format(mAmountTotal));
                            donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, currencyFormatter.format(mAmountTotal)));
                            updateAmounts();
                            InputMethodManager inputMethodManager = mParentActivity != null ?
                                    (InputMethodManager) mParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE) : null;
                            if (inputMethodManager == null) return false;
                            inputMethodManager.toggleSoftInput(0, 0);
                            return true;
                        default: return false;
                    }
                });

        Button incrementTotalButton = rootView.findViewById(R.id.donation_increment_button);
        incrementTotalButton.setOnClickListener(clickedView -> {
            mAmountTotal += 0.01f;
            UserPreferences.setDonation(getContext(), mAmountTotal);
            UserPreferences.updateFirebaseUser(getContext());
            donationTotalText.setText(currencyFormatter.format(mAmountTotal));
            donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, currencyFormatter.format(mAmountTotal)));
            updateAmounts();
        });

        Button decrementTotalButton = rootView.findViewById(R.id.donation_decrement_button);
        decrementTotalButton.setOnClickListener(clickedView -> {
            if (mAmountTotal > 0f) {
                mAmountTotal -= 0.01f;
                UserPreferences.setDonation(getContext(), mAmountTotal);
                UserPreferences.updateFirebaseUser(getContext());
            }
            donationTotalText.setText(currencyFormatter.format(mAmountTotal));
            donationTotalLabel.setContentDescription(getString(R.string.description_donation_text, currencyFormatter.format(mAmountTotal)));
            updateAmounts();
        });

        if (savedInstanceState != null) mDualPane = savedInstanceState.getBoolean(STATE_PANE);
        else mDualPane = rootView.findViewById(R.id.donation_detail_container).getVisibility() == View.VISIBLE;

        if (mParentActivity != null && mDualPane) showDualPane(getArguments());

        if (mListAdapter == null) mListAdapter = new ListAdapter();
        else if (getFragmentManager() != null) getFragmentManager().popBackStack();

        RecyclerView recyclerView = rootView.findViewById(R.id.donation_list);
        recyclerView.setAdapter(mListAdapter);

        mBarWrapper = rootView.findViewById(R.id.action_bar_wrapper);
        mActionBar = rootView.findViewById(R.id.action_bar);
        mProgressBar = rootView.findViewById(R.id.save_progress_bar);

        // Prevents multithreading issues on simultaneous sync operations due to constant stream of database updates.
        mActionBar.setOnClickListener(clickedView -> {
            if (mDonationsAdjusted) {
                mListAdapter.syncDonations();
                mDonationsAdjusted = false;
            } else if (mAmountTotal > 0) {
                ContentValues values = new ContentValues();
                values.put(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY, 1);
                DataService.startActionUpdateFrequency(getContext(), values);
                UserPreferences.updateFirebaseUser(mParentActivity);
            }
            renderActionBar();
        });
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
    }

    /**
     * Saves Layout configuration state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PANE, mDualPane);
    }

    /**
     * Updates the amounts allocated to each charity on increment or decrement
     * of total donation amount.
     */
    private void updateAmounts() {
        renderActionBar();
        if (mValuesArray != null) mListAdapter.notifyDataSetChanged();
    }

    public static void resetDonationsAdjusted() { mDonationsAdjusted = false; }

    /**
     * Populates {@link DonationFragment} {@link RecyclerView}.
     */
    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Float[] mProportions;
        private View mLastClicked;
        private Rateraid.Builder mWeightsBuilder;

        /**
         * Augments {@code ViewHolder} {@code onClick} behavior.
         */
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastClicked != null && mLastClicked.equals(view)) mDualPane = !mDualPane;
                else mDualPane = true;

                mLastClicked = view;
                if (mDualPane) showDualPane((Bundle) view.getTag());
                else showSinglePane();
            }
        };

        public ListAdapter() {
            mProportions = new Float[mValuesArray.length];
            mWeightsBuilder = Rateraid.with(mProportions, 0.01f);
        }

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override
        public @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_donation, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            if (mValuesArray == null || mValuesArray.length == 0 || mValuesArray[position] == null
             || mProportions == null || mProportions.length == 0 || mProportions[position] == null) return;
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
            final String ein = values.getAsString(GivetrackContract.Entry.COLUMN_EIN);
            String name = values.getAsString(GivetrackContract.Entry.COLUMN_CHARITY_NAME);
            String url = values.getAsString(GivetrackContract.Entry.COLUMN_NAVIGATOR_URL);
            final int frequency =
                    values.getAsInteger(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY);
            final float impact = values.getAsFloat(GivetrackContract.Entry.COLUMN_DONATION_IMPACT);

            holder.mNameView.setText(name);

            holder.mFrequencyView.setText(getString(R.string.indicator_donation_frequency, String.valueOf(frequency)));
            holder.mImpactView.setText(String.format(Locale.US, getString(R.string.indicator_donation_impact), currencyInstance.format(impact)));
            if (impact > 10)
                if (Build.VERSION.SDK_INT > 23) holder.mImpactView.setTextAppearance(R.style.AppTheme_TextEmphasis);
                else holder.mImpactView.setTextAppearance(getContext(), R.style.AppTheme_TextEmphasis);

            Bundle arguments = new Bundle();
            arguments.putString(CharityFragment.ARG_ITEM_NAME, name);
            arguments.putString(CharityFragment.ARG_ITEM_EIN, ein);
            arguments.putString(CharityFragment.ARG_ITEM_URL, url);

            holder.itemView.setTag(arguments);
            holder.itemView.setOnClickListener(mOnClickListener);

            holder.mProportionView.setText(percentInstance.format(mProportions[position]));
            holder.mAmountView.setText(currencyInstance.format(mProportions[position] * mAmountTotal));
            holder.mRemoveButton.setOnClickListener(removeButtonclickedView -> {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
                dialog.setMessage(mParentActivity.getString(R.string.dialog_removal_alert, name));
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep),
                        (neutralButtonOnClickDialog, neutralButtonOnClickPosition) -> dialog.dismiss());
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_remove),
                        (negativeButtonOnClickDialog, negativeButtonOnClickPosition) -> {
                            if (mDualPane) showSinglePane();
//                                if (mValuesArray.length == 1) onDestroy();
                            DataService.startActionRemoveCollected(getContext(), ein);
                        });
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            });

            final int adapterPosition = holder.getAdapterPosition();

            mWeightsBuilder.addButtonSet(holder.mIncrementButton, holder.mDecrementButton, adapterPosition);
            mWeightsBuilder.addValueEditor(holder.mProportionView, adapterPosition);
            if (adapterPosition == getItemCount() - 1) {
                mWeightsBuilder.build().setOnClickListener(clickedView -> {
                    float sum = 0;
                    for (float proportion : mProportions) sum += proportion;
                    Timber.d("List[%s] : Sum[%s]", Arrays.asList(mProportions).toString(), sum);
                    mDonationsAdjusted = true;
                    renderActionBar();
                    mProgressBar.setVisibility(View.VISIBLE);
                    notifyDataSetChanged();
                });
            }
        }

        /**
         * Returns the number of items to display.
         */
        @Override
        public int getItemCount() { return mValuesArray != null ? mValuesArray.length : 0; }

        /**
         * Swaps the Cursor after completing a load or resetting Loader.
         */
        void swapValues() {
            if (mProportions.length != mValuesArray.length) mProportions = Arrays.copyOf(mProportions, mValuesArray.length);
            for (int i = 0; i < mProportions.length; i++) {
                mProportions[i] = mValuesArray[i].getAsFloat(GivetrackContract.Entry.COLUMN_DONATION_PROPORTION);
            }
            Calibrater.resetRatings(mProportions, false);
            notifyDataSetChanged();
        }

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.charity_primary) TextView mNameView;
            @BindView(R.id.charity_secondary) TextView mFrequencyView;
            @BindView(R.id.charity_tertiary) TextView mImpactView;
            @BindView(R.id.donation_proportion_text) EditText mProportionView;
            @BindView(R.id.donation_amount_text) TextView mAmountView;
            @BindView(R.id.donation_increment_button) TextView mIncrementButton;
            @BindView(R.id.donation_decrement_button) TextView mDecrementButton;
            @BindView(R.id.collection_remove_button) Button mRemoveButton;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        /**
         * Syncs donation proportion and amount values to table.
         */
        private void syncDonations() {
            if (mProportions == null || mProportions.length == 0) return;
            ContentValues[] valuesArray = new ContentValues[mProportions.length];
            for (int i = 0; i < mProportions.length; i++) {
                ContentValues values = new ContentValues();
                values.put(GivetrackContract.Entry.COLUMN_DONATION_PROPORTION, mProportions[i]);
                Timber.d(mProportions[i] + " " + mAmountTotal + " " + i + " " + mProportions.length);
                valuesArray[i] = values;
            }
            DataService.startActionUpdateProportions(getContext(), valuesArray);
        }
    }

    /**
     * Indicates whether the MasterDetailFlow is in dual pane mode.
     */
    public boolean isDualPane() { return mDualPane; }

    /**
     * Presents the list of items and item details side-by-side using two vertical panes.
     */
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
    public void showSinglePane() {
        getChildFragmentManager().beginTransaction().remove(mCharityFragment).commit();
        mParentActivity.findViewById(R.id.donation_list).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDualPane = false;
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
            actionBarIcon = R.drawable.action_manage;
            progressBarVisibility = View.GONE;
        } else {
            actionBarColor = R.color.colorConversion;
            barWrapperColor = R.color.colorConversionDark;
            actionBarIcon = R.drawable.action_download;
            progressBarVisibility = View.GONE;
        }

        mBarWrapper.setBackgroundColor(getResources().getColor(barWrapperColor));
        mActionBar.setBackgroundColor(getResources().getColor(actionBarColor));
        mActionBar.setImageResource(actionBarIcon);
        mProgressBar.setVisibility(progressBarVisibility);
    }
}