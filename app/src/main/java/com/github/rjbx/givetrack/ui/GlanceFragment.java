package com.github.rjbx.givetrack.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Provides the logic and views for an activity overview screen.
 */
public class GlanceFragment extends Fragment implements
        DialogInterface.OnClickListener,
        IAxisValueFormatter {

    private static final int [] COLORS = new int[] {
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
    private String[] mRecordsArray;
    private int mInterval;
    @BindView(R.id.home_title) TextView mTitleText;
    @BindView(R.id.home_amount_text) TextView mAmountView;
    @BindView(R.id.home_amount_wrapper) View mAmountWrapper;
    @BindView(R.id.percentage_chart) PieChart mPercentageChart;
    @BindView(R.id.usage_chart) PieChart mUsageChart;
    @BindView(R.id.type_chart) PieChart mTypeChart;
    @BindView(R.id.average_chart) PieChart mAverageChart;
    @BindView(R.id.activity_chart) BarChart mActivityChart;

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public GlanceFragment() {}

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
    @Override public @Nullable View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_glance, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Bundle args = getArguments();
        if (args != null) sValuesArray = (ContentValues[]) args.getParcelableArray(MainActivity.ARGS_ITEM_ATTRIBUTES);

        float tracked = Float.parseFloat(UserPreferences.getTracked(getContext()));
        mTracked = CURRENCY_FORMATTER.format(tracked);

        List<String> charities = UserPreferences.getCharities(getContext());
        float totalImpact = 0f;
        for (String charity : charities) totalImpact += Float.parseFloat(charity.split(":")[4]);
        mTotal = CURRENCY_FORMATTER.format(totalImpact);
        mAmountView.setText(mTotal);

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
    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        mParentActivity = (MainActivity) getActivity();
    }

    /**
     * Ensures the parent Activity has been created and data has been retrieved before
     * invoking the method that references them in order to populate the UI.
     */
    @Override public void onResume() {
        super.onResume();
        List<String> recordsList = UserPreferences.getRecords(getContext());
        mRecordsArray = recordsList.toArray(new String[recordsList.size()]);
        toggleTime();
    }

    /**
     * Unbinds Butterknife from this Fragment.
     */
    @Override public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override public void onClick(DialogInterface dialog, int which) {
        if (dialog == mTimeDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mTimeDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    UserPreferences.setTracked(getContext(), "0");
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
    @Override public String getFormattedValue(float value, AxisBase axis) {
        switch ((int) value) {
            case 0: return getString(R.string.axis_value_high);
            case 1: return getString(R.string.axis_value_today);
            case 2: return getString(R.string.axis_value_yester, mIntervalLabel.toLowerCase());
            default: return getString(R.string.axis_value_interval, (int) value, mIntervalLabel);
        }
    }

    /**
     * Defines behavior on click of toggle color button.
     */
    @OnClick(R.id.home_amount_text) void toggleColor() {
        sThemeIndex++;
        if (sThemeIndex == 7) sThemeIndex = 0;
        mAmountWrapper.setBackgroundColor(getResources().getColor(COLORS[sThemeIndex]));
        UserPreferences.setTheme(getContext(), sThemeIndex);
        UserPreferences.updateFirebaseUser(getContext());
    }

    /**
     * Defines behavior on click of toggle amount button.
     */
    @OnClick(R.id.home_amount_label) void toggleAmount(TextView label) {
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
    @OnClick(R.id.home_time_button) void toggleTime() {
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
    @OnClick(R.id.home_config_button) void trackAmount() {
        mTimeDialog = new AlertDialog.Builder(getContext()).create();
        mTimeDialog.setMessage(String.format("Your tracked data %s will be lost. Do you want to start tracking from today instead?", mTrackedTime));
        mTimeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mTimeDialog.show();
        mTimeDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
        mTimeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    /**
     * Defines behavior on click of share text button.
     */
    @OnClick(R.id.home_share_button) void shareText() {
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

        List<PieEntry> percentageEntries = new ArrayList<>();
        float donationAmount = 0f;
        int donationFrequency = 0;

        StringBuilder percentageMessageBuilder = new StringBuilder();
        if (sValuesArray == null || sValuesArray.length == 0) return;
        for (ContentValues values : sValuesArray) {
            String percentageStr = values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE);
            float percentage = Float.parseFloat(percentageStr);
            if (percentage < .01f) continue;
            String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
            percentageMessageBuilder.append(String.format(Locale.getDefault(), "%s %s\n", PERCENT_FORMATTER.format(percentage), name));
            if (name.length() > 20) { name = name.substring(0, 20);
                name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }
            percentageEntries.add(new PieEntry(percentage, name));
            donationAmount += Float.parseFloat(values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_IMPACT));
            donationFrequency += values.getAsInteger(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY);
        }
        String percentMessage = percentageMessageBuilder.toString();

        int chartColors[] = {
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

        PieDataSet percentageSet = new PieDataSet(percentageEntries, "");
        percentageSet.setColors(chartColors);

        PieData percentageData = new PieData(percentageSet);
        Description percentageDesc = new Description();
        percentageDesc.setText(getString(R.string.chart_title_percentage));
        percentageDesc.setTextSize(fontSize);

        int margin = (int) context.getResources().getDimension(R.dimen.item_initial_top_margin);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.piechart_diameter));
            params.setMargins(margin * 2, margin, margin * 2, margin);
            mPercentageChart.setLayoutParams(params);
        }

//        mPercentageChart.animateY(1000, Easing.EasingOption.EaseInOutCirc);
        mPercentageChart.setTag(percentMessage);
        mPercentageChart.setData(percentageData);
        mPercentageChart.setDescription(percentageDesc);
        mPercentageChart.setEntryLabelTextSize(fontSize * 1.25f);
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

        float amountTotal = donationAmount;
        List<PieEntry> usageEntries = new ArrayList<>();
        usageEntries.add(new PieEntry(donationAmount / amountTotal, getString(R.string.axis_value_donation)));

        int gaugeColors[] = {
                getResources().getColor(R.color.colorConversion),
                getResources().getColor(R.color.colorConversionDark),
        };
        PieDataSet usageSet = new PieDataSet(usageEntries, "");
        usageSet.setColors(gaugeColors);
        Description usageDesc = new Description();
        usageDesc.setText(getString(R.string.chart_title_usage));
        usageDesc.setTextSize(fontSize / 1.1f);
        usageEntries.add(new PieEntry(donationAmount / amountTotal, getString(R.string.axis_value_donation)));

        PieData usageData = new PieData(usageSet);
        mUsageChart.setData(usageData);
        mUsageChart.setDescription(usageDesc);
        mUsageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mUsageChart.setHoleRadius(15f);
        mUsageChart.setBackgroundColor(backgroundColor);
        mUsageChart.setTransparentCircleColor(backgroundColor);
        mUsageChart.setHoleColor(backgroundColor);
        mUsageChart.getLegend().setEnabled(false);
        mUsageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mUsageChart));
        mUsageChart.invalidate();

        float conversionsTotal = donationFrequency;
        List<PieEntry> typeEntries = new ArrayList<>();
        typeEntries.add(new PieEntry(conversionsTotal != 0f ? donationFrequency / conversionsTotal : .5f, getString(R.string.indicator_donation_frequency)));

        PieDataSet typeSet = new PieDataSet(typeEntries, "");
        typeSet.setColors(gaugeColors);
        Description typeDesc = new Description();
        typeDesc.setText(getString(R.string.chart_title_type));
        typeDesc.setTextSize(fontSize / 1.1f);

        PieData typeData = new PieData(typeSet);
        mTypeChart.setData(typeData);
        mTypeChart.setDescription(typeDesc);
        mTypeChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mTypeChart.setHoleRadius(15f);
        mTypeChart.setBackgroundColor(backgroundColor);
        mTypeChart.setTransparentCircleColor(backgroundColor);
        mTypeChart.setHoleColor(backgroundColor);
        mTypeChart.getLegend().setEnabled(false);
        mTypeChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mTypeChart));
        mTypeChart.invalidate();

        Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        long currentTime = System.currentTimeMillis();
        long lastConversionTime = UserPreferences.getTimestamp(context);
        long timeBetweenConversions = currentTime - lastConversionTime;

        float recordsTotal = 0;
        float[] recordAmounts = new float[mRecordsArray.length];
        float[] intervalAggregates = new float[7];
        if (mRecordsArray.length == 0 || mRecordsArray[0].isEmpty()) return;
        for (int j = 0; j < mRecordsArray.length; j++) {
            recordAmounts[j] = Float.parseFloat(mRecordsArray[j].split(":")[1]);
            recordsTotal += recordAmounts[j];

            int currentInterval = Calendar.getInstance().get(mInterval);

            long recordTimestamp = Long.parseLong(mRecordsArray[j].split(":")[0]);
            Calendar recordCalendar  = Calendar.getInstance();
            recordCalendar.setTimeInMillis(recordTimestamp);
            int recordInterval = recordCalendar.get(mInterval);

            int intervalDifference = currentInterval - recordInterval;
            boolean validInterval = true;

            if (mInterval != Calendar.YEAR  && recordInterval > currentInterval) {
                int priorYearIntervals = 7 - currentInterval;
                int yearsDifference = Calendar.getInstance().get(Calendar.YEAR) - recordCalendar.get(Calendar.YEAR);
                if (priorYearIntervals > 0 && yearsDifference < 2) {
                    int priorYearIndex = recordCalendar.get(mInterval) - 13;
                    intervalDifference = currentInterval - priorYearIndex;
                } else validInterval = false;
            }
            if (validInterval && intervalDifference < 7) intervalAggregates[intervalDifference] += recordAmounts[j];
        }

        float today = Float.parseFloat(mRecordsArray[0].split(":")[1]);
        float high = Math.max(Float.parseFloat(UserPreferences.getHigh(context)), today);
        UserPreferences.setHigh(context, String.format(Locale.getDefault(), "%.2f", high));
        UserPreferences.updateFirebaseUser(mParentActivity);

        List<PieEntry> averageEntries = new ArrayList<>();
        averageEntries.add(new PieEntry(high, getString(R.string.axis_value_alltime)));
        averageEntries.add(new PieEntry(recordsTotal / 7, getString(R.string.axis_value_daily)));

        PieDataSet averageSet = new PieDataSet(averageEntries, "");
        averageSet.setColors(gaugeColors);
        Description averageDesc = new Description();
        averageDesc.setText(getString(R.string.chart_title_average));
        averageDesc.setTextSize(fontSize / 1.1f);
        PieData averageData = new PieData(averageSet);
        mAverageChart.setData(averageData);
        mAverageChart.setDescription(averageDesc);
        mAverageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        mAverageChart.setHoleRadius(15f);
        mAverageChart.setBackgroundColor(backgroundColor);
        mAverageChart.setTransparentCircleColor(backgroundColor);
        mAverageChart.setHoleColor(backgroundColor);
        mAverageChart.getLegend().setEnabled(false);
        mAverageChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mAverageChart));
        mAverageChart.invalidate();

        List<BarEntry> activityEntries = new ArrayList<>();
        activityEntries.add(new BarEntry(0f, high));
        activityEntries.add(new BarEntry(1f, intervalAggregates[0]));
        activityEntries.add(new BarEntry(2f, intervalAggregates[1]));
        activityEntries.add(new BarEntry(3f, intervalAggregates[2]));
        activityEntries.add(new BarEntry(4f, intervalAggregates[3]));
        activityEntries.add(new BarEntry(5f, intervalAggregates[4]));
        activityEntries.add(new BarEntry(6f, intervalAggregates[5]));
        activityEntries.add(new BarEntry(7f, intervalAggregates[6]));

        StringBuilder activityMessageBuilder = new StringBuilder();
        activityMessageBuilder.append(String.format(Locale.getDefault(),"Today %s\n", intervalAggregates[0]));
        activityMessageBuilder.append(String.format(Locale.getDefault(),"Yesterday %s\n", intervalAggregates[1]));
        for (int i = 2; i < activityEntries.size(); i++) {
            activityMessageBuilder.append(String.format(Locale.getDefault(), "%s %d %s\n", mIntervalLabel, i, intervalAggregates[1]));
        }

        String activityMessage = activityMessageBuilder.toString();

        BarDataSet activityDataSet = new BarDataSet(activityEntries, "");
        activityDataSet.setColors(chartColors);

        BarData activityData = new BarData(activityDataSet);
        activityData.setBarWidth(.75f);
        Description activityDesc = new Description();
        activityDesc.setText(getString(R.string.chart_title_activity));
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
        chartDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        chartDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
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
        chartDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
        chartDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
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