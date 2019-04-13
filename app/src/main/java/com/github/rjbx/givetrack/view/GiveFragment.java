package com.github.rjbx.givetrack.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Gravity;
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

import androidx.transition.Slide;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

import timber.log.Timber;

import com.github.rjbx.calibrater.Calibrater;
import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseManager;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.User;
import com.github.rjbx.rateraid.Rateraid;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.github.rjbx.givetrack.AppUtilities.CURRENCY_FORMATTER;
import static com.github.rjbx.givetrack.AppUtilities.PERCENT_FORMATTER;

/**
 * Provides the logic and views for a user activity management screen.
 */
public class GiveFragment extends Fragment implements
        DetailFragment.MasterDetailFlow,
        TextView.OnEditorActionListener {

    private static final String USER_STATE = "com.github.rjbx.givetrack.ui.arg.GIVE_USER";
    private static final String TARGETS_STATE = "com.github.rjbx.givetrack.ui.arg.GIVE_TARGETS";
    private static final String PERCENTS_STATE = "com.github.rjbx.givetrack.ui.arg.GIVE_PERCENTS";
    private static final String PANE_STATE = "com.github.rjbx.givetrack.ui.state.GIVE_PANE";
    private static final String ADJUST_STATE = "com.github.rjbx.givetrack.ui.state.GIVE_ADJUST";
    private static final String POSITION_STATE = "com.github.rjbx.givetrack.ui.state.GIVE_POSITION";
    private static User sUser;
    private static boolean sDualPane;
    private static boolean sPercentagesAdjusted;
    private Context mContext;
    private InputMethodManager mMethodManager;
    private HomeActivity mParentActivity;
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
    public GiveFragment() {}

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    static GiveFragment newInstance(@Nullable Bundle args) {
        GiveFragment fragment = new GiveFragment();
        if (args != null) fragment.setArguments(args);
        fragment.setEnterTransition(new Slide(Gravity.TOP));
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sDualPane = savedInstanceState.getBoolean(PANE_STATE);
            sPercentagesAdjusted = savedInstanceState.getBoolean(ADJUST_STATE);
            mPanePosition = savedInstanceState.getInt(POSITION_STATE);
            Parcelable[] parcelableArray = savedInstanceState.getParcelableArray(TARGETS_STATE);
            if (mListAdapter == null && parcelableArray != null) {
                Target[] valuesArray = AppUtilities.getTypedArrayFromParcelables(parcelableArray, Target.class);
                List<Target> targetList = Arrays.asList(valuesArray);
                if (mListAdapter == null) mListAdapter = new ListAdapter(targetList);
                else if (getFragmentManager() != null) getFragmentManager().popBackStack();
                mRecyclerView.setAdapter(mListAdapter);
            }
        }
        super.onCreate(savedInstanceState);
    }

    /**
     * Generates a Layout for the Fragment.
     */
    @Override public @Nullable
    View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_give, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        sPercentagesAdjusted = false;

        Bundle args = getArguments();
        if (args != null) {
            Parcelable[] parcelableArray = args.getParcelableArray(HomeActivity.ARGS_TARGET_ATTRIBUTES);
            if (mListAdapter == null && parcelableArray != null) {
                Target[] valuesArray = AppUtilities.getTypedArrayFromParcelables(parcelableArray, Target.class);
                List<Target> targetList = Arrays.asList(valuesArray);
                sUser = args.getParcelable(HomeActivity.ARGS_USER_ATTRIBUTES);
                if (sUser == null) mParentActivity.recreate();
                else {
                    mAmountTotal = Float.parseFloat(sUser.getGiveImpact());
                    mMagnitude = Float.parseFloat(sUser.getGiveMagnitude());
                    if (mListAdapter == null) {
                        mListAdapter = new ListAdapter(targetList);
                        mRecyclerView.setAdapter(mListAdapter);
                    } else if (getFragmentManager() != null) getFragmentManager().popBackStack();
                }
            }
        }


        mTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
        mTotalText.setOnEditorActionListener(this);
        mTotalLabel.setContentDescription(getString(R.string.description_donation_text, CURRENCY_FORMATTER.format(mAmountTotal)));

        if (savedInstanceState != null) {
            sDualPane = savedInstanceState.getBoolean(PANE_STATE);
            sPercentagesAdjusted = savedInstanceState.getBoolean(ADJUST_STATE);
            mPanePosition = savedInstanceState.getInt(POSITION_STATE);
        } else sDualPane = mDetailContainer.getVisibility() == View.VISIBLE;

        if (mParentActivity != null && sDualPane) showDualPane(getArguments());

        renderActionBar();

        return rootView;
    }

    /**
     * Saves reference to parent Activity, initializes Loader and updates Layout configuration.
     */
    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null || !(getActivity() instanceof HomeActivity)) return;
        mParentActivity = (HomeActivity) getActivity();
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
            mMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
//        if (mListAdapter != null) {
//            List<Target> targetList = Arrays.asList(sValuesArray)
//            mListAdapter.swapValues(targetList);
//        }
    }

    /**
     * Registers this Activity to listen for magnitude preference changes generated from selecting related
     * {@link #mParentActivity} options {@link android.view.MenuItem}, as well as save item percentage changes.
     */
    @Override public void onPause() {
        super.onPause();
//        if (sPercentagesAdjusted) syncPercentages();
    }

    /**
     * Unbinds Butterknife from this Fragment.
     */
    @Override public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Persists values through destructive lifecycle changes.
     */
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PANE_STATE, sDualPane);
        outState.putBoolean(ADJUST_STATE, sPercentagesAdjusted);
        outState.putInt(POSITION_STATE, mPanePosition);
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
                    sUser.setGiveImpact(String.valueOf(mAmountTotal));
                    DatabaseManager.startActionUpdateUser(mContext, sUser);
                } catch (ParseException e) {
                    Timber.e(e);
                    return false;
                }
                mTotalText.setText(CURRENCY_FORMATTER.format(mAmountTotal));
                mTotalLabel.setContentDescription(getString(R.string.description_donation_text, CURRENCY_FORMATTER.format(mAmountTotal)));
                renderActionBar();
                if (mListAdapter != null) mListAdapter.notifyDataSetChanged();
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
    @OnClick(R.id.donation_decrement_button) void clickDecrementImpact() {
        if (mAmountTotal > 0f) {
            mAmountTotal -= mMagnitude;
            sUser.setGiveImpact(String.valueOf(mAmountTotal));
            DatabaseManager.startActionUpdateUser(mContext, sUser);
        }
        String formattedTotal = CURRENCY_FORMATTER.format(mAmountTotal);
        mTotalText.setText(formattedTotal);
        mTotalLabel.setContentDescription(getString(R.string.description_donation_text, formattedTotal));
        renderActionBar();
        if (mListAdapter != null) mListAdapter.notifyDataSetChanged();
    }

    /**
     * Defines behavior on click of increment amount button.
     */
    @OnClick(R.id.donation_increment_button) void clickIncrementImpact() {
        if (mTotalText == null) return;
        mAmountTotal += mMagnitude;
        sUser.setGiveImpact(String.valueOf(mAmountTotal));
        DatabaseManager.startActionUpdateUser(mContext, sUser);
        String formattedTotal = CURRENCY_FORMATTER.format(mAmountTotal);
        mTotalText.setText(formattedTotal);
        mTotalLabel.setContentDescription(getString(R.string.description_donation_text, formattedTotal));
        renderActionBar();
        if (mListAdapter != null) mListAdapter.notifyDataSetChanged();
    }

    /**
     * Defines behavior on click of sync adjustments button.
     */
    @OnClick(R.id.action_bar) void syncAdjustments() {
        // Prevents multithreading issues on simultaneous sync operations due to constant stream of database updates.
        if (sPercentagesAdjusted) {
            if (mListAdapter != null) mListAdapter.syncPercentages();
            mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentDark, null)));
            mActionBar.setImageResource(R.drawable.action_sync);
        } else if (mAmountTotal > 0) {
            if (mListAdapter != null) mListAdapter.syncDonations();
            mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorConversionDark, null)));
            mActionBar.setImageResource(R.drawable.action_sync);
        }
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
                    if (sPercentagesAdjusted) mListAdapter.syncPercentages();
                    sPercentagesAdjusted = false;
                });
            }
        }, 2000); // Wait until this time interval has elapsed to sync percentages
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

        mActionWrapper.setBackgroundColor(getResources().getColor(barWrapperColor, null));
        mActionBar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(actionBarColor, null)));
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
     * Populates {@link GiveFragment} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private static final int VIEW_TYPE_CHARITY = 0;
        private static final int VIEW_TYPE_BUTTON = 1;
        private ImageButton mLastClicked;
        private Rateraid.Objects mObjects;
        private List<Target> mTargetList;

        /**
         * Initializes percentage array and percentage button click mRepeatHandler and view updater.
         */
        ListAdapter(List<Target> targetList) {
            mTargetList = targetList;
            mObjects = Rateraid.with(mTargetList, mMagnitude, Calibrater.STANDARD_PRECISION, clickedView -> {
//                float sum = 0;
//                for (double percentage : sPercentages) sum += percentage;
//                Timber.d("List[%s] : Sum[%s]", Arrays.asList(sPercentages).toString(), sum);
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
            if (viewType == VIEW_TYPE_CHARITY) view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_give, parent, false);
            else view = LayoutInflater.from(mContext)
                    .inflate(R.layout.button_collect, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Reserves the last element in the list to an add button for launching {@link IndexActivity}.
         */
        @Override public int getItemViewType(int position) {
            if (position == getItemCount() - 1) return VIEW_TYPE_BUTTON;
            else return VIEW_TYPE_CHARITY;
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

            TextView nameView = holder.mNameView;
            TextView frequencyView = holder.mFrequencyView;
            TextView impactView = holder.mImpactView;
            EditText percentageView = holder.mPercentageView;
            TextView amountView = holder.mAmountView;
            TextView incrementButton = holder.mIncrementButton;
            TextView decrementButton = holder.mDecrementButton;
            Button addButton = holder.mAddButton;
            ImageButton inspectButton = holder.mInspectButton;

            if (position == getItemCount() - 1 && addButton != null) {
                addButton.setOnClickListener(clickedView -> {
                    Intent spawnIntent = new Intent(mContext, IndexActivity.class);
                    getActivity().finish();
                    startActivity(spawnIntent);
                });
                return;
            }

            if (mTargetList == null || mTargetList.size()== 0 || mTargetList.get(position) == null)
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

            Target target = mTargetList.get(position);

            String name = target.getName();
            if (name.length() > 30) {
                name = name.substring(0, 30);
                name = name.substring(0, name.lastIndexOf(" ")).concat("...");
            }

            final int frequency = target.getFrequency();

            double amount = target.getPercent() * mAmountTotal;
            String amountStr = CURRENCY_FORMATTER.format(amount);
            int amountLength = amountStr.length();
            if (amountLength > 12) amountStr = String.format("%s%sM", amountStr.substring(0, amountLength - 11),
                    amountLength > 14 ? "" : "." + amountStr.substring(amountLength - 9, amountLength - 7));
            else if (amountLength > 9) amountStr = String.format("%s%sK", amountStr.substring(0, amountLength - 7),
                    amountLength > 10 ? "" : "." + amountStr.substring(amountLength - 6, amountLength - 4));
            else if (amountLength > 6) amountStr = amountStr.substring(0, amountLength - 3);

            final float impact = Float.parseFloat(target.getImpact());
            String impactStr = CURRENCY_FORMATTER.format(impact);
            int impactLength = impactStr.length();

            if (impactLength > 12) impactStr = String.format("%s%sM", impactStr.substring(0, impactLength - 11),
                    impactLength > 14 ? "" : "." + impactStr.substring(impactLength - 9, impactLength - 7));
            else if (impactLength > 9) impactStr = String.format("%s%sK", impactStr.substring(0, impactLength - 7),
                    impactLength > 10 ? "" : "." + impactStr.substring(impactLength - 6, impactLength - 4));
            else if (impactLength > 6) impactStr = impactStr.substring(0, impactLength - 3);

            if (nameView != null) nameView.setText(name);
            if (frequencyView != null) frequencyView.setText(getString(R.string.indicator_donation_frequency, String.valueOf(frequency)));
            if (amountView != null) amountView.setText(amountStr);
            if (impactView != null) {
                impactView.setText(String.format(Locale.US, getString(R.string.indicator_donation_impact), impactStr));
                if (impact > 9999) impactView.setTextColor(getResources().getColor(R.color.colorConversionDark, null));
                if (impact > 999) impactView.setTextAppearance(R.style.AppTheme_TextEmphasis);
            }

            if (percentageView != null) percentageView.setText(PERCENT_FORMATTER.format(mTargetList.get(position).getPercent()));

            for (View view : holder.itemView.getTouchables()) view.setTag(position);

            if (inspectButton != null) {
                if (!sDualPane) inspectButton.setImageResource(R.drawable.ic_baseline_expand_more_24px);
                else if (mPanePosition == position) {
                    inspectButton.setImageResource(R.drawable.ic_baseline_expand_less_24px);
                    mLastClicked = inspectButton;
                }
            }

            final int adapterPosition = holder.getAdapterPosition();
            if (incrementButton != null && decrementButton != null && percentageView != null) {
                mObjects.addShifters(incrementButton, decrementButton, adapterPosition)
                        .addEditor(percentageView, adapterPosition, mMethodManager, null);
            }
        }

        /**
         * Returns the number of items to display.
         */
        @Override public int getItemCount() {
            return mTargetList != null ? mTargetList.size() + 1 : 1;
        }

        /**
         * Swaps the Cursor after completing a load or resetting Loader.
         */
        private void swapValues(List<Target> targetList) {
//            if (sPercentages.length != sValuesArray.length)
//                sPercentages = Arrays.copyOf(sPercentages, sValuesArray.length);
//            for (int i = 0; i < sPercentages.length; i++) {
//                sPercentages[i] = sValuesArray[i].getPercent();
//            }
            mTargetList = targetList;
            if (sUser.getGiveReset()) {
                Rateraid.resetRatings(mTargetList, true, Calibrater.STANDARD_PRECISION);
                syncPercentages();
                sUser.setGiveReset(false);
                DatabaseManager.startActionUpdateUser(mContext, sUser);
            }
            notifyDataSetChanged();
        }

        /**
         * Syncs donation percentage and amount mValues to database from which table is repopulated.
         */
        private void syncPercentages() {
            if (mTargetList == null || mTargetList.size() == 0) return;
//        for (int i = 0; i < sValuesArray.length; i++) {
//            sValuesArray[i].setPercent(sPercentages[i]);
//            Timber.d(sPercentages[i] + " " + mAmountTotal + " " + i + " " + sPercentages.length);
//        }
            DatabaseManager.startActionUpdateTarget(mContext, mTargetList.toArray(new Target[0])); // Locks UI on signout and remote launch
            sPercentagesAdjusted = false;
        }

        /**
         * Syncs donations to database.
         */
        private void syncDonations() {
            DatabaseManager.startActionRecordTarget(mContext, mTargetList.toArray(new Target[0])); // Locks UI on signout and remote launch
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
                            int position = (int) mRemoveDialog.getButton(AlertDialog.BUTTON_NEGATIVE).getTag();
                            if (sDualPane) showSinglePane();
//                                if (sValuesArray.length == 1) onDestroy();
                            DatabaseManager.startActionRemoveTarget(mContext, mTargetList.get(position)); // Locks UI on signout and remote launch
                            break;
                        default:
                    }
                }
            }

            /**
             * Defines behavior on click of remove button.
             */
            @Optional @OnClick(R.id.collection_remove_button) void removeGive(View v) {

                int position = (int) v.getTag();
                Target target = mTargetList.get(position);
                String name = target.getName();

                if (mContext == null) return;
                mRemoveDialog = new AlertDialog.Builder(mContext).create();
                mRemoveDialog.setMessage(mContext.getString(R.string.message_remove_entry, name, "collection"));
                mRemoveDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                mRemoveDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), this);
                mRemoveDialog.show();
                mRemoveDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                Button button = mRemoveDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setTextColor(getResources().getColor(R.color.colorAttentionDark, null));
                button.setTag(position);
            }

            /**
             * Defines behavior on click of inspect button.
             */
            @Optional @OnClick(R.id.inspect_button) void inspectGive(View v) {

                int position = (int) v.getTag();
                Target values = mTargetList.get(position);
                if (mLastClicked != null && mLastClicked.equals(v)) sDualPane = !sDualPane;
                else sDualPane = true;

                if (mLastClicked != null) mLastClicked.setImageResource(R.drawable.ic_baseline_expand_more_24px);
                mLastClicked = (ImageButton) v;
                mPanePosition = position;

                int resId = sDualPane ? R.drawable.ic_baseline_expand_less_24px : R.drawable.ic_baseline_expand_more_24px;
                mLastClicked.setImageResource(resId);
                mLastClicked.invalidate();

                Bundle arguments = new Bundle();
                arguments.putParcelable(DetailFragment.ARG_ITEM_COMPANY, values);
                if (sDualPane) showDualPane(arguments);
                else showSinglePane();
            }

            /**
             * Defines behavior on click of share button.
             */
            @Optional @OnClick(R.id.share_button) void shareGive(View v) {

                Target values = mTargetList.get((int) v.getTag());
                String name = values.getName().replace(" ", "");
                int frequency = values.getFrequency();
                float impact = Float.parseFloat(values.getImpact());

                String textMessage =
                        String.format("My %s donations totaling %s to #%s have been added to my personal record with #%s App!",
                                frequency, CURRENCY_FORMATTER.format(impact), name, getString(R.string.app_name));
                ViewUtilities.launchShareIntent(mParentActivity, textMessage);
            }

            /**
             * Defines behavior on click of contact button.
             */
            @Optional @OnClick(R.id.contact_button) void viewContacts(View v) {
                if (mContext == null) return;
                mContactDialog = new AlertDialog.Builder(mContext).create();
                ViewUtilities.ContactDialogLayout alertLayout = ViewUtilities.ContactDialogLayout.getInstance(mContactDialog, mTargetList.get((int) v.getTag()));
                mContactDialog.setView(alertLayout);
                mContactDialog.show();
            }
        }
    }
}