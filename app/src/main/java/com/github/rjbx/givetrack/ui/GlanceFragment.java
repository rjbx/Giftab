package com.github.rjbx.givetrack.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.icu.util.TimeUnit;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.UserPreferences;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Provides the logic and views for an activity overview screen.
 */
public class GlanceFragment extends Fragment implements
        DialogInterface.OnClickListener,
        IAxisValueFormatter {

    private static final int[] COLORS = new int[]{
            R.color.colorAttention,
            R.color.colorAccent,
            R.color.colorPrimary,
            R.color.colorHeat,
            R.color.colorConversion,
            R.color.colorComfort,
            R.color.colorNeutral
    };
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();
    private static ContentValues[] sValuesArray;
    private static boolean mShowTracked;
    private static boolean mShowYears;
    private static int sThemeIndex;
    private MainActivity mParentActivity;
    private Unbinder mUnbinder;
    private AlertDialog mTimeDialog;
    private String mIntervalLabel;
    private String mTotal;
    private String mTracked;
    private String mTrackedTime;
    private String mTotalTime;
    private String[] mDialogMessages;
    private int mInterval;
    @BindView(R.id.home_title) TextView mTitleText;
    @BindView(R.id.home_amount_text) TextView mAmountView;
    @BindView(R.id.home_amount_wrapper) View mAmountWrapper;
    @BindView(R.id.percentage_chart) PieChart mPercentageChart;
    @BindView(R.id.average_chart) PieChart mAverageChart;
    @BindView(R.id.usage_chart) PieChart mUsageChart;
    @BindView(R.id.timing_chart) PieChart mTimingChart;
    @BindView(R.id.activity_chart) BarChart mActivityChart;

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public GlanceFragment() {
    }

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    public static GlanceFragment newInstance(@Nullable Bundle args) {
        GlanceFragment fragment = new GlanceFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_glance, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Bundle args = getArguments();
        if (args != null)
            sValuesArray = (ContentValues[]) args.getParcelableArray(MainActivity.ARGS_RECORD_ATTRIBUTES);

        Date date = new Date(UserPreferences.getTimetrack(getContext()));
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
        dateFormatter.setTimeZone(TimeZone.getDefault());
        String formattedDate = dateFormatter.format(date);
        mTrackedTime = String.format("since %s", formattedDate);
        mTotalTime = "all-time";

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
    }

    /**
     * Ensures the parent Activity has been created and data has been retrieved before
     * invoking the method that references them in order to populate the UI.
     */
    @Override
    public void onResume() {
        super.onResume();
        toggleTime();
    }

    /**
     * Unbinds Butterknife from this Fragment.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mTimeDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mTimeDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    UserPreferences.setTimetrack(getContext(), System.currentTimeMillis());
                    mAmountView.setText("0");
                    break;
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
                return getString(R.string.axis_value_this, mIntervalLabel);
            case 2:
                return getString(R.string.axis_value_yester, mIntervalLabel.toLowerCase());
            default:
                return getString(R.string.axis_value_interval, (int) value - 1, mIntervalLabel);
        }
    }

    /**
     * Defines behavior on click of toggle color button.
     */
    @OnClick(R.id.home_amount_text)
    void toggleColor() {
        sThemeIndex++;
        if (sThemeIndex == 7) sThemeIndex = 0;
        mAmountWrapper.setBackgroundColor(getResources().getColor(COLORS[sThemeIndex]));
        UserPreferences.setTheme(getContext(), sThemeIndex);
        UserPreferences.updateFirebaseUser(getContext());
    }

    /**
     * Defines behavior on click of toggle amount button.
     */
    @OnClick(R.id.home_amount_label)
    void toggleAmount(TextView label) {
        mShowTracked = !mShowTracked;
        if (mShowTracked) {
            label.setText(mTrackedTime.toUpperCase());
            mAmountView.setText(String.valueOf(mTracked));
        } else {
            label.setText(mTotalTime.toUpperCase());
            mAmountView.setText(String.valueOf(mTotal));
        }
    }

    /**
     * Defines behavior on click of toggle time button.
     */
    @OnClick(R.id.home_time_button)
    void toggleTime() {
        if (mInterval < 3) mInterval++;
        else mInterval = 1;
        mShowYears = !mShowYears;
        switch (mInterval) {
            case Calendar.YEAR:
                mIntervalLabel = "Year";
                renderCharts();
                break;
            case Calendar.MONTH:
                mIntervalLabel = "Month";
                renderCharts();
                break;
            case Calendar.WEEK_OF_YEAR:
                mIntervalLabel = "Week";
                renderCharts();
                break;
        }
    }

    /**
     * Defines behavior on click of track amount button.
     */
    @OnClick(R.id.home_config_button)
    void trackAmount() {
        mTimeDialog = new AlertDialog.Builder(getContext()).create();
        mTimeDialog.setMessage(String.format("Your tracked data %s will be lost. Do you want to start tracking from today instead?", mTrackedTime));
        mTimeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mTimeDialog.show();
        mTimeDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
        mTimeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
    }

    /**
     * Defines behavior on click of share text button.
     */
    @OnClick(R.id.home_share_button)
    void shareText() {
        String amount = mShowTracked ? mTracked : mTotal;
        String timeframe = mShowTracked ? mTrackedTime : mTotalTime;
        Intent shareIntent = ShareCompat.IntentBuilder.from(mParentActivity)
                .setType("text/plain")
                .setText(String.format("My %s in donations %s have been added to my personal record with #%s App",
                        amount,
                        timeframe,
                        getString(R.string.app_name)))
                .getIntent();
        startActivity(shareIntent);
    }

    /**
     * Builds the charts supplied by the Fragment layout.
     */
    private void renderCharts() {

        Context context = getContext();
        if (context == null) return;

        mTitleText.setText(getString(R.string.charts_title, mIntervalLabel));

        int fontSize = (int) getResources().getDimension(R.dimen.text_size_subtitle);
        int backgroundColor = getResources().getColor(R.color.colorChalk);

        Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        float recordsTotal = 0;
        List<Float> intervalAggregates = new ArrayList<>();

        int highDifference = 0;

        float tracked = 0f;
        long tracktime = UserPreferences.getTimetrack(mParentActivity);

        List<PieEntry> percentageEntries = new ArrayList<>();
        float donationAmount = 0f;
        int donationFrequency = sValuesArray.length;
        Map<String, Float> recordAggregates = new HashMap<>(donationFrequency);
        if (sValuesArray != null && donationFrequency != 0) {
            for (int j = 0; j < sValuesArray.length; j++) {
                ContentValues record = sValuesArray[j];
                Long time = record.getAsLong(DatabaseContract.Entry.COLUMN_DONATION_TIME);
                Float amount = Float.parseFloat(record.getAsString(DatabaseContract.Entry.COLUMN_DONATION_IMPACT));
                String name = record.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);

                recordsTotal += amount;
                if (time >= tracktime) tracked += amount;

                float recordAmount = amount + recordAggregates.get(name);
                recordAggregates.put(name, recordAmount);

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
                } 
                float intervalAmount = amount + intervalAggregates.get(intervalDifference);
                if (intervalAggregates.size() <= intervalDifference) intervalAggregates.add(intervalDifference, intervalAmount);
                else intervalAggregates.set(intervalDifference, intervalAmount);
            }
        }

        mTracked = CURRENCY_FORMATTER.format(tracked);

        float high = 0f;
        for (float a : intervalAggregates) if (a > high) high = a;

        StringBuilder percentageMessageBuilder = new StringBuilder();

        mTotal = CURRENCY_FORMATTER.format(recordsTotal);
        mAmountView.setText(mTotal);

        Set<Map.Entry<String, Float>> entries = recordAggregates.entrySet();
        for (Map.Entry<String, Float> entry : entries) {
            String name = entry.getKey();
            float percentage = entry.getValue();
            if (percentage < .01f) continue;
            percentageMessageBuilder.append(String.format(Locale.getDefault(), "%s %s\n", PERCENT_FORMATTER.format(percentage), name));
            if (name.length() > 20) { name = name.substring(0, 20);
                name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }
            percentageEntries.add(new PieEntry(percentage, name));
        }
        String percentMessage = percentageMessageBuilder.toString();

        int detailColors[] = {
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAttention),
                getResources().getColor(R.color.colorNeutralDark),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.colorComfort),
                getResources().getColor(R.color.colorPrimaryDark),
                getResources().getColor(R.color.colorAttentionDark),
                getResources().getColor(R.color.colorNeutral),
                getResources().getColor(R.color.colorAccentDark),
                getResources().getColor(R.color.colorComfortDark),
                getResources().getColor(R.color.colorSlate),
        };

        int overviewColors[] = {
                getResources().getColor(R.color.colorConversion),
                getResources().getColor(R.color.colorConversionDark),
        };

        PieDataSet percentageSet = new PieDataSet(percentageEntries, "");
        percentageSet.setColors(detailColors);

        PieData percentageData = new PieData(percentageSet);
        Description percentageDesc = new Description();
        percentageDesc.setText(getString(R.string.chart_title_percentage));
        percentageDesc.setTextSize(fontSize);

        int margin = (int) context.getResources().getDimension(R.dimen.item_initial_top_margin);
        float labelSize = fontSize * 1.35f;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.piechart_diameter));
            params.setMargins(margin * 2, margin, margin * 2, margin);
            mPercentageChart.setLayoutParams(params);

            labelSize = fontSize * 1.15f;
        }

//        mPercentageChart.animateY(1000, Easing.EasingOption.EaseInOutCirc);
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
        mPercentageChart.setRotationEnabled(false);
        mPercentageChart.getLegend().setEnabled(false);
        mPercentageChart.setHighlightPerTapEnabled(true);
        mPercentageChart.setClickable(true);
        mPercentageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mPercentageChart));
        mPercentageChart.invalidate();

        String intervalLabel = "Average " + mIntervalLabel;
        String donationLabel = "Average Donation";
        float perInterval = recordsTotal / highDifference;
        float perDonation = recordsTotal / sValuesArray.length;
        String averageMessage = String.format("%s %s\n%s %s", intervalLabel, CURRENCY_FORMATTER.format(perInterval), donationLabel, CURRENCY_FORMATTER.format(perDonation));

        List<PieEntry> averageEntries = new ArrayList<>();
        averageEntries.add(new PieEntry(perInterval, intervalLabel));
        averageEntries.add(new PieEntry(perDonation, donationLabel));

        PieDataSet averageSet = new PieDataSet(averageEntries, "");
        averageSet.setColors(overviewColors);
        Description averageDesc = new Description();
        averageDesc.setText(getString(R.string.chart_title_average));
        averageDesc.setTextSize(fontSize);

        PieData averageData = new PieData(averageSet);
        mAverageChart.setTag(averageMessage);
        mAverageChart.setData(averageData);
        mAverageChart.setDescription(averageDesc);
        mAverageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mAverageChart.setHoleRadius(15f);
        mAverageChart.setBackgroundColor(backgroundColor);
        mAverageChart.setTransparentCircleColor(backgroundColor);
        mAverageChart.setHoleColor(backgroundColor);
        mAverageChart.getLegend().setEnabled(false);
        mAverageChart.setEntryLabelTextSize(labelSize);
        mAverageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mAverageChart));
        mAverageChart.invalidate();

        String highLabel = getString(R.string.axis_value_alltime, mIntervalLabel);
        String currentLabel = getFormattedValue(1, null);
        String usageMessage = String.format("%s %s\n%s %s", highLabel, CURRENCY_FORMATTER.format(high), currentLabel, CURRENCY_FORMATTER.format(intervalAggregates.get(0)));

        List<PieEntry> usageEntries = new ArrayList<>();
        usageEntries.add(new PieEntry(high, getString(R.string.axis_value_alltime, mIntervalLabel)));
        usageEntries.add(new PieEntry(intervalAggregates.get(0), getFormattedValue(1, null)));

        PieDataSet usageSet = new PieDataSet(usageEntries, "");
        usageSet.setColors(overviewColors);
        Description usageDesc = new Description();
        usageDesc.setText(getString(R.string.chart_title_usage));
        usageDesc.setTextSize(fontSize);

        PieData usageData = new PieData(usageSet);
        mUsageChart.setTag(usageMessage);
        mUsageChart.setData(usageData);
        mUsageChart.setDescription(usageDesc);
        mUsageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mUsageChart.setHoleRadius(15f);
        mUsageChart.setBackgroundColor(backgroundColor);
        mUsageChart.setTransparentCircleColor(backgroundColor);
        mUsageChart.setHoleColor(backgroundColor);
        mUsageChart.getLegend().setEnabled(false);
        mUsageChart.setEntryLabelTextSize(labelSize);
        mUsageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mUsageChart));
        mUsageChart.invalidate();

        String recentLabel = String.format("Within 7 %ss", mIntervalLabel);
        String oldLabel = String.format("Over 7 %ss", mIntervalLabel);
        float percentRecent = donationAmount / recordsTotal;
        float percentOld = 1f - percentRecent;
        String timingMessage = String.format("%s %s\n%s %s", recentLabel, PERCENT_FORMATTER.format(percentRecent), oldLabel, PERCENT_FORMATTER.format(percentOld));

        List<PieEntry> timingEntries = new ArrayList<>();
        timingEntries.add(new PieEntry(percentRecent, String.format("Within 7 %ss", mIntervalLabel)));
        if (percentRecent < 1f) timingEntries.add(new PieEntry(percentOld, String.format("Over 7 %ss", mIntervalLabel)));

        PieDataSet timingSet = new PieDataSet(timingEntries, "");
        timingSet.setColors(overviewColors);
        Description timingDesc = new Description();
        timingDesc.setText(getString(R.string.chart_title_timing));
        timingDesc.setTextSize(fontSize);

        PieData timingData = new PieData(timingSet);
        mTimingChart.setTag(timingMessage);
        mTimingChart.setData(timingData);
        mTimingChart.setDescription(timingDesc);
        mTimingChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mTimingChart.setHoleRadius(15f);
        mTimingChart.setBackgroundColor(backgroundColor);
        mTimingChart.setTransparentCircleColor(backgroundColor);
        mTimingChart.setHoleColor(backgroundColor);
        mTimingChart.getLegend().setEnabled(false);
        mTimingChart.setEntryLabelTextSize(labelSize);
        mTimingChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mTimingChart));
        mTimingChart.invalidate();

        List<BarEntry> activityEntries = new ArrayList<>();
        activityEntries.add(new BarEntry(0f, high));
        activityEntries.add(new BarEntry(1f, intervalAggregates.get(0)));
        activityEntries.add(new BarEntry(2f, intervalAggregates.get(1)));
        activityEntries.add(new BarEntry(3f, intervalAggregates.get(2)));
        activityEntries.add(new BarEntry(4f, intervalAggregates.get(3)));
        activityEntries.add(new BarEntry(5f, intervalAggregates.get(4)));
        activityEntries.add(new BarEntry(6f, intervalAggregates.get(5)));
        activityEntries.add(new BarEntry(7f, intervalAggregates.get(6)));

        StringBuilder activityMessageBuilder = new StringBuilder();
        for (int i = 0; i < activityEntries.size(); i++) {
            String label = getFormattedValue(i, null);
            String amount = CURRENCY_FORMATTER.format(i < 1 ? high : intervalAggregates.get(i - 1));
            String intervalStr = String.format(Locale.getDefault(), "%s %s\n", label, amount);
            activityMessageBuilder.append(intervalStr);
        }
        String activityMessage = activityMessageBuilder.toString();

        BarDataSet activityDataSet = new BarDataSet(activityEntries, "");
        activityDataSet.setColors(detailColors);

        BarData activityData = new BarData(activityDataSet);
        activityData.setBarWidth(.75f);
        Description activityDesc = new Description();
        activityDesc.setText(getString(R.string.chart_title_timing));
        activityDesc.setTextSize(fontSize);

        mActivityChart.setTag(activityMessage);
        mActivityChart.setData(activityData);
        mActivityChart.setDescription(activityDesc);
        mActivityChart.getXAxis().setValueFormatter(this);
        mActivityChart.getXAxis().setTextSize(fontSize / 1.1f);
        mActivityChart.setFitBars(true);
        mActivityChart.getLegend().setEnabled(false);
        mActivityChart.setPinchZoom(true);
        mActivityChart.notifyDataSetChanged();
        mActivityChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mActivityChart));
        mActivityChart.invalidate();
    }

    private void expandChart(Chart chart, String s) {

        float fontSize = getResources().getDimension(R.dimen.text_size_subtitle);
        AlertDialog chartDialog = new AlertDialog.Builder(mParentActivity).create();
        chartDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_return), this);
        Chart chartClone;

        if (chart instanceof PieChart) {
            chartClone = new PieChart(mParentActivity);
            chartClone.setLayoutParams(chart.getLayoutParams());
            ((PieChart) chartClone).setData(((PieChart) chart).getData());
            ((PieChart) chartClone).setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
            ((PieChart) chartClone).setHoleRadius(15f);
            ((PieChart) chartClone).setEntryLabelTextSize(fontSize * 1.25f);
            chartClone.getLegend().setEnabled(false);
        } else if (chart instanceof HorizontalBarChart) {
            chartClone = new HorizontalBarChart(mParentActivity);
            chartClone.setData(((HorizontalBarChart) chart).getData());
            chartClone.getXAxis().setValueFormatter(chart.getXAxis().getValueFormatter());
            chartClone.getXAxis().setTextSize(fontSize / 1.1f);
            ((HorizontalBarChart) chartClone).setFitBars(true);
            chartClone.getLegend().setEnabled(false);
            ((HorizontalBarChart) chartClone).setPinchZoom(true);
        } else return;

        chartClone.setDescription(chart.getDescription());
        chartClone.setMinimumWidth(1000);
        chartClone.setMinimumHeight(1000);

        chartDialog.setMessage((String) chart.getTag());
        chartDialog.setView(chartClone);
        chartDialog.show();
        chartDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        chartDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
    }

    class OnSelectedChartOnGestureListener implements OnChartGestureListener {

        Chart mView;
        String mStats;

        public OnSelectedChartOnGestureListener(Chart chart) {
            mView = chart;
            mStats = chart.getDescription().toString() + chart.getData().toString();
        }

        @Override public void onChartLongPressed(MotionEvent me) {
            expandChart(mView, mStats);
        }

        @Override public void onChartDoubleTapped(MotionEvent me) {
            expandChart(mView, mStats);
        }

        @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }
        @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) { }
        @Override public void onChartSingleTapped(MotionEvent me) { }
        @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) { }
        @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) { }
        @Override public void onChartTranslate(MotionEvent me, float dX, float dY) { }
    }
}