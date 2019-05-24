package com.github.rjbx.givetrack.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.BindView;
import butterknife.OnClick;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseManager;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.github.rjbx.givetrack.AppUtilities.CURRENCY_FORMATTER;
import static com.github.rjbx.givetrack.AppUtilities.PERCENT_FORMATTER;
import static com.github.rjbx.givetrack.AppUtilities.DATE_FORMATTER;

/**
 * Provides the logic and views for a user activity overview screen.
 */
public class GlanceFragment extends Fragment implements
        DialogInterface.OnClickListener,
        DatePickerDialog.OnDateSetListener,
        IAxisValueFormatter {

    private static final String SCROLL_STATE = "com.github.rjbx.givetrack.ui.state.GLANCE_SCROLL";
    private static final String USER_STATE = "com.github.rjbx.givetrack.ui.state.GLANCE_USER";
    private static final String RECORDS_STATE = "com.github.rjbx.givetrack.ui.state.GLANCE_RECORDS";
    private static final String SINCE_STATE = "com.github.rjbx.givetrack.ui.state.GLANCE_SINCE";
    private static final String THEME_STATE = "com.github.rjbx.givetrack.ui.state.GLANCE_THEME";
    private static String[] GIFT_TYPES = { "monetary", "goods", "service" };
    private static final int[] COLORS = new int[]{
            R.color.colorAttention,
            R.color.colorCheer,
            R.color.colorPrimary,
            R.color.colorHeat,
            R.color.colorConversion,
            R.color.colorComfort,
            R.color.colorNeutral
    };
    private static Record[] sValuesArray;
    private static User sUser;
    private static boolean mViewTracked;
    private static int mInterval = 1;
    private static int mGraphType;
    private static int mHomeType;
    private static int sThemeIndex;
    private int mDescFontSize;
    private long mAnchorDate;
    private float mAxisFontSize;
    private float mValueFontSize;
    private Context mContext;
    private HomeActivity mParentActivity;
    private Unbinder mUnbinder;
    private AlertDialog mTimeDialog;
    private AlertDialog mChartDialog;
    private String mIntervalContent;
    private String mGraphTypeContent;
    private String mHomeTypeContent;
    private String mTotal = "$0.00";
    private String mTracked = "$0.00";
    private String mTimeTracked;
    private String mTotalTime = "all-time";
    @BindView(R.id.title_text) TextView mTitleText;
    @BindView(R.id.home_amount_text) TextView mAmountView;
    @BindView(R.id.home_amount_wrapper) View mAmountWrapper;
    @BindView(R.id.glance_scroll_view) NestedScrollView mScrollView;
    @BindView(R.id.percentage_chart) PieChart mPercentageChart;
    @BindView(R.id.average_chart) PieChart mAverageChart;
    @BindView(R.id.usage_chart) PieChart mUsageChart;
    @BindView(R.id.timing_chart) PieChart mTimingChart;
    @BindView(R.id.activity_chart) BarChart mActivityChart;
    @BindView(R.id.home_amount_label) TextView mAmountLabel;
    @BindView(R.id.home_type_label) TextView mTypeLabel;
    @BindView(R.id.interval_text) TextView mIntervalText;
    @BindView(R.id.type_text) TextView mTypeText;

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public GlanceFragment() {}

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    static GlanceFragment newInstance(@Nullable Bundle args) {
        GlanceFragment fragment = new GlanceFragment();
        if (args != null) fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (savedInstanceState != null) {
//            mUser = savedInstanceState.getParcelable(USER_STATE);
//            mViewTracked = savedInstanceState.getBoolean(SINCE_STATE);
//            mThemeIndex = savedInstanceState.getInt(THEME_STATE);
//            Parcelable[] parcelables = savedInstanceState.getParcelableArray(RECORDS_STATE);
//            if (parcelables != null) mValuesArray = AppUtilities.getTypedArrayFromParcelables(parcelables, Record.class);
//        } else if (args != null) {
//            mUser = args.getParcelable(HomeActivity.ARGS_USER_ATTRIBUTES);
//            Parcelable[] parcelables = args.getParcelableArray(HomeActivity.ARGS_RECORD_ATTRIBUTES);
//            if (parcelables != null) mValuesArray = AppUtilities.getTypedArrayFromParcelables(parcelables, Record.class);
//        }
//        super.onCreate(savedInstanceState);
//    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) sUser = savedInstanceState.getParcelable(USER_STATE);
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

        View rootView = inflater.inflate(R.layout.fragment_glance, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            int scrollState = savedInstanceState.getInt(SCROLL_STATE, 0);
            mScrollView.setScrollY(scrollState);
        }

        mIntervalText.setPaintFlags(mIntervalText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mTypeText.setPaintFlags(mTypeText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        Bundle args = getArguments();
        if (args != null) {
            sUser = args.getParcelable(HomeActivity.ARGS_USER_ATTRIBUTES);
            Parcelable[] parcelables = args.getParcelableArray(HomeActivity.ARGS_RECORD_ATTRIBUTES);
            if (parcelables != null) sValuesArray = AppUtilities.getTypedArrayFromParcelables(parcelables, Record.class);
        }
        if (sUser != null) {
            Date date = new Date(sUser.getGlanceAnchor());
            DATE_FORMATTER.setTimeZone(TimeZone.getDefault());
            String formattedDate = DATE_FORMATTER.format(date);
            mTimeTracked = String.format("since %s", formattedDate);

            mViewTracked = sUser.getGlanceSince();
            mHomeType = sUser.getGlanceHometype();
            mGraphType = sUser.getGlanceGraphtype();
            mInterval = sUser.getGlanceInterval();
            toggleAmount(mAmountLabel, mViewTracked);

            sThemeIndex = sUser.getGlanceTheme();
            mAmountWrapper.setBackgroundColor(getResources().getColor(COLORS[sThemeIndex], null));
        }
        return rootView;
    }

    /**
     * Saves reference to parent Activity, initializes Loader and updates Layout configuration.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null || !(getActivity() instanceof HomeActivity)) return;
        mParentActivity = (HomeActivity) getActivity();
    }

    /**
     * Ensures the parent Activity has been created and data has been retrieved before
     * invoking the method that references them in order to populate the UI.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (sUser == null) return;
        updateTime();
        updateGraphTyoe();
        updateHomeType();
        renderCharts();
    }

    /**
     * Unbinds Butterknife from this Fragment.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(SCROLL_STATE, mScrollView.getScrollY());
        outState.putParcelable(USER_STATE, sUser);
        super.onSaveInstanceState(outState);
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (sUser == null) return;
        if (dialog == mTimeDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mTimeDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    sUser.setGlanceAnchor(mAnchorDate);
                    if (sUser.getGlanceAnchor() == 0) sUser.setGlanceAnchor(System.currentTimeMillis());
                    DatabaseManager.startActionUpdateUser(getContext(), sUser);
                    break;
                default:
            }
        } else if (dialog == mChartDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mChartDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    Window window = mChartDialog.getWindow();
                    if (window != null) {
                        String message = (String) window.getDecorView().getTag();
                        shareDialogText(message);
                    }
                default:
            }
        }
    }

    /**
     * Formats axis value of BarChart depending on their axis and the set time interval.
     */
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        switch ((int) value) {
            case 0:
                return getString(R.string.axis_value_high);
            case 1:
                return getString(R.string.axis_value_this, mIntervalContent);
            case 2:
                return getString(R.string.axis_value_yester, mIntervalContent.toLowerCase());
            default:
                return getString(R.string.axis_value_interval, (int) value - 1, mIntervalContent);
        }
    }

    @OnClick(R.id.header_bar)
    void toggleColor() {
        if (sUser == null) return;
        sThemeIndex++;
        if (sThemeIndex == 7) sThemeIndex = 0;
        mAmountWrapper.setBackgroundColor(getResources().getColor(COLORS[sThemeIndex], null));
        sUser.setGlanceTheme(sThemeIndex);
        DatabaseManager.startActionUpdateUser(getContext(), sUser);
    }

    /**
     * Defines behavior on click of toggle amount button.
     */
    @OnClick(R.id.home_amount_label)
    void toggleTracked() {
        if (sUser == null) return;
        mViewTracked = !mViewTracked;
        sUser.setGlanceSince(mViewTracked);
        DatabaseManager.startActionUpdateUser(getContext(), sUser);
        toggleAmount(mAmountLabel, mViewTracked);
    }

    /**
     * Defines behavior on click of toggle time button.
     */
    @OnClick(R.id.interval_text)
    void toggleTime() {
        if (sUser == null) return;
        if (mInterval < 3) mInterval++;
        else mInterval = 1;
        updateTime();
        sUser.setGlanceInterval(mInterval);
        DatabaseManager.startActionUpdateUser(getContext(), sUser);
    }

    private void updateTime() {
        switch (mInterval) {
            case Calendar.YEAR: mIntervalContent = "Year"; break;
            case Calendar.MONTH: mIntervalContent = "Month"; break;
            case Calendar.WEEK_OF_YEAR: mIntervalContent = "Week"; break;
        }
    }

    /**
     * Defines behavior on click of toggle time button.
     */
    @OnClick(R.id.type_text)
    void toggleGraphType() {
        if (sUser == null) return;
        if (mGraphType < 3) mGraphType++;
        else mGraphType = 0;
        updateGraphTyoe();
        sUser.setGlanceGraphtype(mGraphType);
        DatabaseManager.startActionUpdateUser(getContext(), sUser);
    }

    private void updateGraphTyoe() {
        switch (mGraphType) {
            case 0: mGraphTypeContent = "Total"; break;
            case 1: mGraphTypeContent = "Monetary"; break;
            case 2: mGraphTypeContent = "Goods"; break;
            case 3: mGraphTypeContent = "Service"; break;
        }
    }

    /**
     * Defines behavior on click of toggle time button.
     */
    @OnClick(R.id.home_type_label)
    void toggleHomeType() {
        if (sUser == null) return;
        if (mHomeType < 3) mHomeType++;
        else mHomeType = 0;
        updateHomeType();
        sUser.setGlanceHometype(mHomeType);
        DatabaseManager.startActionUpdateUser(getContext(), sUser);
    }

    private void updateHomeType() {
        switch (mHomeType) {
            case 0: mHomeTypeContent = "TOTAL"; break;
            case 1: mHomeTypeContent = "MONETARY"; break;
            case 2: mHomeTypeContent = "GOODS"; break;
            case 3: mHomeTypeContent = "SERVICE"; break;
        }
    }

    /**
     * Defines behavior on click of track amount button.
     */
    @OnClick(R.id.home_config_button)
    void trackAmount() {
        if (sUser == null) return;
        Calendar calendar = Calendar.getInstance();
        if (sUser.getGlanceAnchor() != 0) calendar.setTimeInMillis(sUser.getGlanceAnchor());
        DatePickerDialog datePicker = new DatePickerDialog(
                mContext,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    /**
     * Updates the DatePicker with the date selected from the Dialog.
     */
    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        mAnchorDate = calendar.getTimeInMillis();
        if (mContext == null) return;
        mTimeDialog = new AlertDialog.Builder(mContext).create();
        mTimeDialog.setMessage(String.format("Do you want to display your total contributions since %s? Toggle by tapping the adjacent label.", DATE_FORMATTER.format(mAnchorDate)));
        mTimeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mTimeDialog.show();
        mTimeDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
        mTimeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAttention, null));

    }

    /**
     * Defines behavior on click of share text button.
     */
    @OnClick(R.id.home_share_button)
    void shareText() {
        String amount = mViewTracked ? mTracked : mTotal;
        String timeframe = mViewTracked ? mTimeTracked : mTotalTime;
        String textMessage =
                String.format("My %s in donations %s have been added to my personal record with #%s App",
                        amount, timeframe, getString(R.string.app_name));
        ViewUtilities.launchShareIntent(mParentActivity, textMessage);
    }

    /**
     * Handles changes between and populates tracked and total impact views.
     */
    private void toggleAmount(TextView amountLabel, boolean viewTracked) {
        String amountStr;
        String labelStr;
        if (viewTracked) {
            labelStr = mTimeTracked.toUpperCase();
            amountStr = String.valueOf(mTracked);
            int amountLength = amountStr.length();
            if (amountLength > 12) amountStr = String.format("%s%sM", amountStr.substring(0, amountLength - 11),
                    amountLength > 14 ? "" : "." + amountStr.substring(amountLength - 9, amountLength - 7));
            else if (amountLength > 6) amountStr = amountStr.substring(0, amountLength - 3);
        } else {
            labelStr = mTotalTime.toUpperCase();
            amountStr = String.valueOf(mTotal);
            int amountLength = amountStr.length();
            if (amountLength > 12) amountStr = String.format("%s%sM", amountStr.substring(0, amountLength - 11),
                    amountLength > 14 ? "" : "." + amountStr.substring(amountLength - 9, amountLength - 7));
            else if (amountLength > 6) amountStr = amountStr.substring(0, amountLength - 3);
        }
        amountLabel.setText(labelStr);
        mAmountView.setText(amountStr);
    }

    /**
     * Builds the charts supplied by the Fragment layout.
     */
    private void renderCharts() {

        if (mContext == null || sUser == null) return;

        mIntervalText.setText(mIntervalContent + "ly");
        mTypeText.setText(mGraphTypeContent);
        mTypeLabel.setText(mHomeTypeContent);

        mDescFontSize = (int) getResources().getDimension(R.dimen.description_text);
        mValueFontSize = mContext.getResources().getDimension(R.dimen.value_text);
        mAxisFontSize = mContext.getResources().getDimension(R.dimen.axis_text);
        int backgroundColor = getResources().getColor(R.color.colorSlate, null);

        float recordsTotal = 0;
        float typeTotal = 0;
        float intervalTotal = 0;

        float[] intervalAggregates = new float[10000];

        int highDifference = 0;

        float tracked = 0f;
        long tracktime = sUser.getGlanceAnchor();

        List<PieEntry> percentageEntries = new ArrayList<>();
        float donationAmount = 0f;
        int donationFrequency = sValuesArray.length;
        if (donationFrequency == 0) return;
        Map<String, Float> recordAggregates = new HashMap<>(donationFrequency);
        for (Record record : sValuesArray) {
            long time = record.getTime();
            Float amount = Float.parseFloat(record.getImpact());
            String name = record.getName();
            int type = record.getType();


            if (mHomeType == 0 || type + 1 == mHomeType) {
                typeTotal += amount;
                if (time >= tracktime) tracked += amount;
            }
            if (mGraphType != 0 && type + 1 != mGraphType) continue;

            recordsTotal += amount;
            Float recordAggregate = recordAggregates.get(name);
            float recordAmount = amount + (recordAggregate != null ? recordAggregate : 0f);

            Calendar recordCalendar = Calendar.getInstance();
            recordCalendar.setTimeInMillis(time);
            int recordInterval = recordCalendar.get(mInterval);
            int currentInterval = Calendar.getInstance().get(mInterval);
            int intervalDifference = currentInterval - recordInterval;

            if (mInterval != Calendar.YEAR) {
                int recordYear = recordCalendar.get(Calendar.YEAR);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int yearsDifference = currentYear - recordYear;
                if (mInterval == Calendar.MONTH) intervalDifference = intervalDifference + (yearsDifference * 12);
                else intervalDifference = intervalDifference + (yearsDifference * 52);
                if (intervalDifference > 10000) intervalAggregates = Arrays.copyOf(intervalAggregates, intervalDifference);
            } intervalAggregates[intervalDifference] += amount;

            if (intervalDifference < 7) {
                recordAggregates.put(name, recordAmount);
                intervalTotal += amount;
            }
            if (highDifference < intervalDifference) highDifference = intervalDifference;
        }

        mTracked = CURRENCY_FORMATTER.format(tracked);

        float high = 0f;
        for (int i = 0; i < intervalAggregates.length; i++) {
            float a = intervalAggregates[i];
            if (a > high) high = a;
            if (i < 8) donationAmount += a;
        }

        StringBuilder percentageMessageBuilder = new StringBuilder(String.format("Past 7 %ss\n", mIntervalContent));

        mTotal = CURRENCY_FORMATTER.format(typeTotal);
        mAmountView.setText(mTotal);
        toggleAmount(mAmountLabel, mViewTracked);

        List<Map.Entry<String, Float>> entries = new ArrayList<>(recordAggregates.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for (Map.Entry<String, Float> entry : entries) {
            String name = entry.getKey();
            float value = entry.getValue();
            float percent = value / intervalTotal;
            percentageMessageBuilder.append(String.format(Locale.getDefault(), "\n%s %s %s\n", PERCENT_FORMATTER.format(percent), name, CURRENCY_FORMATTER.format(value)));
            if (percent < .20f) name = "";
            if (name.length() > 20) {
                name = name.substring(0, 20);
                name = name.substring(0, name.lastIndexOf(" ")).concat("...");
            }
            percentageEntries.add(new PieEntry(percent, name));
        }
        String percentMessage = percentageMessageBuilder.toString();

        int detailColors[] = {
                getResources().getColor(R.color.colorPrimary, null),
                getResources().getColor(R.color.colorPrimaryDark, null),
                getResources().getColor(R.color.colorAttention, null),
                getResources().getColor(R.color.colorAttentionDark, null),
                getResources().getColor(R.color.colorNeutral, null),
                getResources().getColor(R.color.colorNeutralDark, null),
                getResources().getColor(R.color.colorCheer, null),
                getResources().getColor(R.color.colorCheerDark, null),
                getResources().getColor(R.color.colorHeat, null),
                getResources().getColor(R.color.colorHeatDark, null),
                getResources().getColor(R.color.colorComfort, null),
                getResources().getColor(R.color.colorComfortDark, null)
        };

        int overviewColors[] = {
                getResources().getColor(R.color.colorConversionDark, null),
                getResources().getColor(R.color.colorConversionLight, null)
        };

        PieDataSet percentageSet = new PieDataSet(percentageEntries, "");
        percentageSet.setColors(detailColors);

        PieData percentageData = new PieData(percentageSet);
        Description percentageDesc = new Description();
        percentageDesc.setText(getString(R.string.chart_title_percentage));
        percentageDesc.setTextSize(mDescFontSize);
        percentageDesc.setTextColor(Color.WHITE);

        int margin = (int) mContext.getResources().getDimension(R.dimen.item_initial_top_margin);
        float labelSize = mContext.getResources().getDimension(R.dimen.label_text);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.piechart_diameter));
            params.setMargins(margin * 2, margin, margin * 2, margin);
            mPercentageChart.setLayoutParams(params);
        }

        mPercentageChart.setTag(percentMessage);
        mPercentageChart.setData(percentageData);
        mPercentageChart.setDescription(percentageDesc);
        mPercentageChart.setEntryLabelTextSize(labelSize);
        mPercentageChart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        mPercentageChart.setHoleRadius(20f);
        mPercentageChart.setTransparentCircleRadius(50f);
        mPercentageChart.setBackgroundColor(backgroundColor);
        mPercentageChart.setTransparentCircleColor(backgroundColor);
        mPercentageChart.setHoleColor(backgroundColor);
        mPercentageChart.getData().setDrawValues(false);
        mPercentageChart.getLegend().setEnabled(false);
        mPercentageChart.setHighlightPerTapEnabled(true);
        mPercentageChart.setClickable(true);
        mPercentageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mPercentageChart));
        mPercentageChart.invalidate();

        String intervalLabel = "Average " + mIntervalContent;
        String donationLabel = "Average Gift";
        float perInterval = recordsTotal / (highDifference + 1);
        float perDonation = recordsTotal / sValuesArray.length;
        String averageMessage = String.format(" %sly\n\n%s %s\n%s %s\n", mIntervalContent, intervalLabel, CURRENCY_FORMATTER.format(perInterval), donationLabel, CURRENCY_FORMATTER.format(perDonation));

        List<PieEntry> averageEntries = new ArrayList<>();
        averageEntries.add(new PieEntry(perDonation, donationLabel));
        averageEntries.add(new PieEntry(perInterval, intervalLabel));

        PieDataSet averageSet = new PieDataSet(averageEntries, "");
        averageSet.setColors(overviewColors);

        Description averageDesc = new Description();
        averageDesc.setText(getString(R.string.chart_title_average));
        averageDesc.setTextSize(mDescFontSize);
        averageDesc.setTextColor(Color.WHITE);

        PieData averageData = new PieData(averageSet);
        mAverageChart.setTag(averageMessage);
        mAverageChart.setData(averageData);
        mAverageChart.setDescription(averageDesc);
        mAverageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mAverageChart.setHoleRadius(15f);
        mAverageChart.setBackgroundColor(backgroundColor);
        mAverageChart.setTransparentCircleColor(backgroundColor);
        mAverageChart.setHoleColor(backgroundColor);
        mAverageChart.getData().setDrawValues(false);
        mAverageChart.getLegend().setEnabled(false);
        mAverageChart.setEntryLabelTextSize(labelSize);
        mAverageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mAverageChart));
        mAverageChart.invalidate();

        String highLabel = getString(R.string.axis_value_alltime, mIntervalContent);
        String currentLabel = getFormattedValue(1, null);
        String usageMessage = String.format(" %sly\n\n%s %s\n%s %s\n", mIntervalContent, highLabel, CURRENCY_FORMATTER.format(high), currentLabel, CURRENCY_FORMATTER.format(intervalAggregates[0]));

        List<PieEntry> usageEntries = new ArrayList<>();
        usageEntries.add(new PieEntry(high, getString(R.string.axis_value_alltime, mIntervalContent)));
        if (intervalAggregates[0] > 0f) usageEntries.add(new PieEntry(intervalAggregates[0], getFormattedValue(1, null)));

        PieDataSet usageSet = new PieDataSet(usageEntries, "");
        usageSet.setColors(overviewColors);

        Description usageDesc = new Description();
        usageDesc.setText(getString(R.string.chart_title_usage));
        usageDesc.setTextSize(mDescFontSize);
        usageDesc.setTextColor(Color.WHITE);

        PieData usageData = new PieData(usageSet);
        mUsageChart.setTag(usageMessage);
        mUsageChart.setData(usageData);
        mUsageChart.setDescription(usageDesc);
        mUsageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mUsageChart.setHoleRadius(15f);
        mUsageChart.setBackgroundColor(backgroundColor);
        mUsageChart.setTransparentCircleColor(backgroundColor);
        mUsageChart.setHoleColor(backgroundColor);
        mUsageChart.getData().setDrawValues(false);
        mUsageChart.getLegend().setEnabled(false);
        mUsageChart.setEntryLabelTextSize(labelSize);
        mUsageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mUsageChart));
        mUsageChart.invalidate();

        String recentLabel = String.format("Within 7 %ss", mIntervalContent);
        String oldLabel = String.format("Over 7 %ss", mIntervalContent);
        float percentRecent = donationAmount / recordsTotal;
        float percentOld = 1f - percentRecent;
        String timingMessage = String.format("%sly\n\n%s %s\n%s %s\n", mIntervalContent, recentLabel, PERCENT_FORMATTER.format(percentRecent), oldLabel, PERCENT_FORMATTER.format(percentOld));

        List<PieEntry> timingEntries = new ArrayList<>();
        if (percentOld > 0f) timingEntries.add(new PieEntry(percentOld, String.format("Over 7 %ss", mIntervalContent)));
        if (percentRecent > 0f) timingEntries.add(new PieEntry(percentRecent, String.format("Within 7 %ss", mIntervalContent)));
        PieDataSet timingSet = new PieDataSet(timingEntries, "");
        timingSet.setColors(overviewColors);

        Description timingDesc = new Description();
        timingDesc.setText(getString(R.string.chart_title_timing));
        timingDesc.setTextSize(mDescFontSize);
        timingDesc.setTextColor(Color.WHITE);

        PieData timingData = new PieData(timingSet);
        mTimingChart.setTag(timingMessage);
        mTimingChart.setData(timingData);
        mTimingChart.setDescription(timingDesc);
        mTimingChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mTimingChart.setHoleRadius(15f);
        mTimingChart.setBackgroundColor(backgroundColor);
        mTimingChart.setTransparentCircleColor(backgroundColor);
        mTimingChart.setHoleColor(backgroundColor);
        mTimingChart.getData().setDrawValues(false);
        mTimingChart.getLegend().setEnabled(false);
        mTimingChart.setEntryLabelTextSize(labelSize);
        mTimingChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mTimingChart));
        mTimingChart.invalidate();

        List<BarEntry> activityEntries = new ArrayList<>();
        activityEntries.add(new BarEntry(0f, high));
        activityEntries.add(new BarEntry(1f, intervalAggregates[0]));
        activityEntries.add(new BarEntry(2f, intervalAggregates[1]));
        activityEntries.add(new BarEntry(3f, intervalAggregates[2]));
        activityEntries.add(new BarEntry(4f, intervalAggregates[3]));
        activityEntries.add(new BarEntry(5f, intervalAggregates[4]));
        activityEntries.add(new BarEntry(6f, intervalAggregates[5]));
        activityEntries.add(new BarEntry(7f, intervalAggregates[6]));

        StringBuilder activityMessageBuilder = new StringBuilder(String.format("Past 7 %ss\n\n", mIntervalContent));
        for (int i = 0; i < activityEntries.size(); i++) {
            String label = getFormattedValue(i, null);
            String amount = CURRENCY_FORMATTER.format(i < 1 ? high : intervalAggregates[i - 1]);
            String intervalStr = String.format(Locale.getDefault(), "%s %s\n", label, amount);
            activityMessageBuilder.append(intervalStr);
        }
        String activityMessage = activityMessageBuilder.toString();

        BarDataSet activityDataSet = new BarDataSet(activityEntries, "");
        activityDataSet.setColors(detailColors);

        BarData activityData = new BarData(activityDataSet);
        activityData.setBarWidth(.75f);
        averageData.setValueTextColor(Color.WHITE);

        Description activityDesc = new Description();
        activityDesc.setText(getString(R.string.chart_title_activity));
        activityDesc.setTextSize(mDescFontSize);
        activityDesc.setTextColor(Color.WHITE);

        mActivityChart.setTag(activityMessage);
        mActivityChart.setData(activityData);
        mActivityChart.setDescription(activityDesc);
        mActivityChart.getXAxis().setValueFormatter(this);
        mActivityChart.getXAxis().setTextSize(mAxisFontSize);
        mActivityChart.getXAxis().setTextColor(Color.WHITE);
        mActivityChart.getAxisRight().setDrawLabels(false);
        mActivityChart.getAxisLeft().setDrawLabels(false);
        mActivityChart.getBarData().setValueTextColor(Color.WHITE);
        mActivityChart.getBarData().setValueTextSize(mValueFontSize);
        mActivityChart.getBarData().setDrawValues(false);
        mActivityChart.setFitBars(true);
        mActivityChart.getLegend().setEnabled(false);
        mActivityChart.setPinchZoom(true);
        mActivityChart.notifyDataSetChanged();
        mActivityChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mActivityChart));
        mActivityChart.invalidate();
    }

    /**
     * Builds and launches dialog populated with chart view and description.
     */
    private void expandChart(Chart chart, String title, String stats) {

        float fontSize = getResources().getDimension(R.dimen.chart_dialog_text);
        mChartDialog = new AlertDialog.Builder(mContext).create();
        mChartDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_return), this);
        mChartDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.action_share), this);
        Chart chartClone;

        if (chart instanceof PieChart) {
            chartClone = new PieChart(mContext);
            chartClone.setLayoutParams(chart.getLayoutParams());
            ((PieChart) chartClone).setData(((PieChart) chart).getData());
            ((PieChart) chartClone).setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
            ((PieChart) chartClone).setHoleRadius(15f);
            ((PieChart) chartClone).setEntryLabelTextSize(fontSize);
        } else if (chart instanceof HorizontalBarChart) {
            chartClone = new HorizontalBarChart(mContext);
            chartClone.getXAxis().setValueFormatter(chart.getXAxis().getValueFormatter());
            chartClone.getXAxis().setTextSize(mAxisFontSize);
            ((HorizontalBarChart) chartClone).getAxisLeft().setDrawLabels(false);
            ((HorizontalBarChart) chartClone).getAxisRight().setDrawLabels(false);
            ((HorizontalBarChart) chartClone).setData(((HorizontalBarChart) chart).getData());
            ((HorizontalBarChart) chartClone).setDoubleTapToZoomEnabled(false);
            ((HorizontalBarChart) chartClone).getBarData().setDrawValues(false);
            ((HorizontalBarChart) chartClone).setFitBars(true);
            ((HorizontalBarChart) chartClone).setPinchZoom(true);

        } else return;

        chartClone.setMinimumWidth(500);
        chartClone.setMinimumHeight(1000);

        chartClone.getLegend().setEnabled(false);
        chartClone.getDescription().setEnabled(false);

        int padding = (int) getResources().getDimension(R.dimen.button_padding);
        ScrollView scrollView = new ScrollView(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(padding, padding, padding, padding);

        TextView titleView = new TextView(mContext);
        titleView.setText(title);
        titleView.setTextSize(fontSize);
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView statsView = new TextView(mContext);
        statsView.setText(stats);
        statsView.setTextSize(mDescFontSize);
        statsView.setTextColor(Color.BLACK);
        statsView.setGravity(Gravity.CENTER_HORIZONTAL);

        linearLayout.addView(titleView);
        linearLayout.addView(statsView);
        linearLayout.addView(chartClone);

        scrollView.addView(linearLayout);
        mChartDialog.setView(scrollView);
        mChartDialog.show();
        mChartDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
        mChartDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
        Window window = mChartDialog.getWindow();
        if (window == null) return;
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.getDecorView().setTag(String.format("%s", stats));
    }

    /**
     * Builds and launches implicit text messaging Intent.
     */
    private void shareDialogText(String message) {
        String textMessage = String.format("My giving trends: %s\n#%s App", message, getString(R.string.app_name));
        ViewUtilities.launchShareIntent(mParentActivity, textMessage);
    }

    /**
     * Defines listener for handling chart interactions.
     */
    class OnSelectedChartOnGestureListener implements OnChartGestureListener {

        Chart mView;
        String mTitle;
        String mStats;

        OnSelectedChartOnGestureListener(Chart chart) {
            mView = chart;
            mTitle = chart.getDescription().getText();
            mStats = (String) chart.getTag();
        }

        @Override public void onChartSingleTapped(MotionEvent me) {  expandChart(mView, mTitle, mStats); }
        @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { }
        @Override public void onChartDoubleTapped(MotionEvent me) { }
        @Override public void onChartLongPressed(MotionEvent me) { }
        @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }
        @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }
        @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) { }
        @Override public void onChartTranslate(MotionEvent me, float dX, float dY) { }
    }
}