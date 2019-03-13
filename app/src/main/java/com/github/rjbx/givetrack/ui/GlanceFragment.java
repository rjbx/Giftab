package com.github.rjbx.givetrack.ui;

import android.app.DatePickerDialog;
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
import com.github.rjbx.givetrack.data.DatabaseService;
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

// TODO: Add toggle for type as with interval attribute
/**
 * Provides the logic and views for an activity overview screen.
 */
public class GlanceFragment extends Fragment implements
        DialogInterface.OnClickListener,
        DatePickerDialog.OnDateSetListener,
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
    private static Record[] sValuesArray;
    private static User sUser;
    private static boolean mViewTracked;
    private static boolean mShowYears;
    private static int sThemeIndex;
    private HomeActivity mParentActivity;
    private Unbinder mUnbinder;
    private AlertDialog mTimeDialog;
    private AlertDialog mChartDialog;
    private String mIntervalLabel;
    private String mTotal = "$0.00";
    private String mTracked = "$0.00";
    private String mTimeTracked;
    private String mTotalTime = "all-time";
    private long mAnchorDate;
    private int mInterval;
    @BindView(R.id.home_title) TextView mTitleText;
    @BindView(R.id.home_amount_text) TextView mAmountView;
    @BindView(R.id.home_amount_wrapper) View mAmountWrapper;
    @BindView(R.id.percentage_chart) PieChart mPercentageChart;
    @BindView(R.id.average_chart) PieChart mAverageChart;
    @BindView(R.id.usage_chart) PieChart mUsageChart;
    @BindView(R.id.timing_chart) PieChart mTimingChart;
    @BindView(R.id.activity_chart) BarChart mActivityChart;
    @BindView(R.id.home_amount_label) TextView mAmountLabel;

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
                    sUser.setGlanceAnchor(mAnchorDate);
                    DatabaseService.startActionUpdateUser(getContext(), sUser);
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
        mAmountWrapper.setBackgroundColor(getResources().getColor(COLORS[sThemeIndex], null));
        sUser.setGlanceTheme(sThemeIndex);
        DatabaseService.startActionUpdateUser(getContext(), sUser);
    }

    /**
     * Defines behavior on click of toggle amount button.
     */
    @OnClick(R.id.home_amount_label)
    void toggleTracked() {
        mViewTracked = !mViewTracked;
        sUser.setGlanceSince(mViewTracked);
        DatabaseService.startActionUpdateUser(getContext(), sUser);
        toggleAmount(mAmountLabel, mViewTracked);
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
        Calendar calendar = Calendar.getInstance();
        if (sUser.getGlanceAnchor() != 0) calendar.setTimeInMillis(sUser.getGlanceAnchor());
        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        mAnchorDate = calendar.getTimeInMillis();
        Context context = getContext();
        if (context == null) return;
        mTimeDialog = new AlertDialog.Builder(context).create();
        mTimeDialog.setMessage(String.format("Your tracked data %s will be lost. Do you want to start tracking from today instead?", mTimeTracked));
        mTimeDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mTimeDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mTimeDialog.show();
        mTimeDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
        mTimeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark, null));

    }

    /**
     * Defines behavior on click of share text button.
     */
    @OnClick(R.id.home_share_button)
    void shareText() {
        String amount = mViewTracked ? mTracked : mTotal;
        String timeframe = mViewTracked ? mTimeTracked : mTotalTime;
        Intent shareIntent = ShareCompat.IntentBuilder.from(mParentActivity)
                .setType("text/plain")
                .setText(String.format("My %s in donations %s have been added to my personal record with #%s App",
                        amount,
                        timeframe,
                        getString(R.string.app_name)))
                .getIntent();
        startActivity(shareIntent);
    }

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
        mAmountView.setText(String.valueOf(amountStr));
    }

    /**
     * Builds the charts supplied by the Fragment layout.
     */
    private void renderCharts() {

        Context context = getContext();
        if (context == null) return;

        mTitleText.setText(getString(R.string.charts_title, mIntervalLabel));

        int fontSize = (int) getResources().getDimension(R.dimen.text_size_subtitle);
        int backgroundColor = getResources().getColor(R.color.colorSlate, null);

        Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        float recordsTotal = 0;
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

            recordsTotal += amount;
            if (time <= tracktime) tracked += amount;

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

            if (intervalDifference < 7) recordAggregates.put(name, recordAmount);
            if (highDifference < intervalDifference) highDifference = intervalDifference;
        }

        mTracked = CURRENCY_FORMATTER.format(tracked);

        float high = 0f;
        for (int i = 0; i < intervalAggregates.length; i++) {
            float a = intervalAggregates[i];
            if (a > high) high = a;
            if (i < 8) donationAmount += a;
        }

        StringBuilder percentageMessageBuilder = new StringBuilder(String.format("Past 7 %ss\n", mIntervalLabel));

        mTotal = CURRENCY_FORMATTER.format(recordsTotal);
        mAmountView.setText(mTotal);
        toggleAmount(mAmountLabel, mViewTracked);

        List<Map.Entry<String, Float>> entries = new ArrayList<>(recordAggregates.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for (Map.Entry<String, Float> entry : entries) {
            String name = entry.getKey();
            float value = entry.getValue();
            float percent = value / recordsTotal;
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
                getResources().getColor(R.color.colorAccent, null),
                getResources().getColor(R.color.colorAccentDark, null),
                getResources().getColor(R.color.colorHeat, null),
                getResources().getColor(R.color.colorHeatDark, null),
                getResources().getColor(R.color.colorComfort, null),
                getResources().getColor(R.color.colorComfortDark, null)
        };

        int overviewColors[] = {
                getResources().getColor(R.color.colorConversion, null),
                getResources().getColor(R.color.colorConversionDark, null)
        };

        PieDataSet percentageSet = new PieDataSet(percentageEntries, "");
        percentageSet.setColors(detailColors);

        PieData percentageData = new PieData(percentageSet);
        Description percentageDesc = new Description();
        percentageDesc.setText(getString(R.string.chart_title_percentage));
        percentageDesc.setTextSize(fontSize);
        percentageDesc.setTextColor(Color.WHITE);

        int margin = (int) context.getResources().getDimension(R.dimen.item_initial_top_margin);
        float labelSize = fontSize * 1.35f;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.piechart_diameter));
            params.setMargins(margin * 2, margin, margin * 2, margin);
            mPercentageChart.setLayoutParams(params);
            labelSize = fontSize * 1.15f;
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

        String intervalLabel = "Average " + mIntervalLabel;
        String donationLabel = "Average Donation";
        float perInterval = recordsTotal / (highDifference + 1);
        float perDonation = recordsTotal / sValuesArray.length;
        String averageMessage = String.format(" %sly\n\n%s %s\n%s %s\n", mIntervalLabel, intervalLabel, CURRENCY_FORMATTER.format(perInterval), donationLabel, CURRENCY_FORMATTER.format(perDonation));

        List<PieEntry> averageEntries = new ArrayList<>();
        averageEntries.add(new PieEntry(perInterval, intervalLabel));
        averageEntries.add(new PieEntry(perDonation, donationLabel));

        PieDataSet averageSet = new PieDataSet(averageEntries, "");
        averageSet.setColors(overviewColors);

        Description averageDesc = new Description();
        averageDesc.setText(getString(R.string.chart_title_average));
        averageDesc.setTextSize(fontSize);
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

        String highLabel = getString(R.string.axis_value_alltime, mIntervalLabel);
        String currentLabel = getFormattedValue(1, null);
        String usageMessage = String.format(" %sly\n\n%s %s\n%s %s\n", mIntervalLabel, highLabel, CURRENCY_FORMATTER.format(high), currentLabel, CURRENCY_FORMATTER.format(intervalAggregates[0]));

        List<PieEntry> usageEntries = new ArrayList<>();
        usageEntries.add(new PieEntry(high, getString(R.string.axis_value_alltime, mIntervalLabel)));
        if (intervalAggregates[0] > 0f) usageEntries.add(new PieEntry(intervalAggregates[0], getFormattedValue(1, null)));

        PieDataSet usageSet = new PieDataSet(usageEntries, "");
        usageSet.setColors(overviewColors);

        Description usageDesc = new Description();
        usageDesc.setText(getString(R.string.chart_title_usage));
        usageDesc.setTextSize(fontSize);
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

        String recentLabel = String.format("Within 7 %ss", mIntervalLabel);
        String oldLabel = String.format("Over 7 %ss", mIntervalLabel);
        float percentRecent = donationAmount / recordsTotal;
        float percentOld = 1f - percentRecent;
        String timingMessage = String.format("Past 7 %ss\n\n%s %s\n%s %s\n", mIntervalLabel, recentLabel, PERCENT_FORMATTER.format(percentRecent), oldLabel, PERCENT_FORMATTER.format(percentOld));

        List<PieEntry> timingEntries = new ArrayList<>();
        if (percentRecent > 0f) timingEntries.add(new PieEntry(percentRecent, String.format("Within 7 %ss", mIntervalLabel)));
        if (percentOld > 0f) timingEntries.add(new PieEntry(percentOld, String.format("Over 7 %ss", mIntervalLabel)));

        PieDataSet timingSet = new PieDataSet(timingEntries, "");
        timingSet.setColors(overviewColors);

        Description timingDesc = new Description();
        timingDesc.setText(getString(R.string.chart_title_timing));
        timingDesc.setTextSize(fontSize);
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

        StringBuilder activityMessageBuilder = new StringBuilder(String.format("Past 7 %ss\n\n", mIntervalLabel));
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
        activityDesc.setTextSize(fontSize);
        activityDesc.setTextColor(Color.WHITE);

        mActivityChart.setTag(activityMessage);
        mActivityChart.setData(activityData);
        mActivityChart.setDescription(activityDesc);
        mActivityChart.getXAxis().setValueFormatter(this);
        mActivityChart.getXAxis().setTextSize(fontSize / 1.1f);
        mActivityChart.getXAxis().setTextColor(Color.WHITE);
        mActivityChart.getAxisRight().setTextColor(Color.WHITE);
        mActivityChart.getAxisRight().setTextSize(fontSize / 1.1f);
        mActivityChart.getAxisLeft().setTextColor(Color.WHITE);
        mActivityChart.getAxisLeft().setTextSize(fontSize / 1.1f);
        mActivityChart.getBarData().setValueTextColor(Color.WHITE);
        mActivityChart.getBarData().setValueTextSize(fontSize / 1.4f);
        mActivityChart.getBarData().setDrawValues(false);
        mActivityChart.setFitBars(true);
        mActivityChart.getLegend().setEnabled(false);
        mActivityChart.setPinchZoom(true);
        mActivityChart.notifyDataSetChanged();
        mActivityChart.setOnChartGestureListener(new OnSelectedChartOnGestureListener(mActivityChart));
        mActivityChart.invalidate();
    }

    private void expandChart(Chart chart, String title, String stats) {

        float fontSize = getResources().getDimension(R.dimen.text_size_subtitle);
        mChartDialog = new AlertDialog.Builder(mParentActivity).create();
        mChartDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_return), this);
        mChartDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.action_share), this);
        Chart chartClone;

        if (chart instanceof PieChart) {
            chartClone = new PieChart(mParentActivity);
            chartClone.setLayoutParams(chart.getLayoutParams());
            ((PieChart) chartClone).setData(((PieChart) chart).getData());
            ((PieChart) chartClone).setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
            ((PieChart) chartClone).setHoleRadius(15f);
            ((PieChart) chartClone).setEntryLabelTextSize(fontSize * 1.25f);
        } else if (chart instanceof HorizontalBarChart) {
            chartClone = new HorizontalBarChart(mParentActivity);
            chartClone.getXAxis().setValueFormatter(chart.getXAxis().getValueFormatter());
            chartClone.getXAxis().setTextSize(fontSize / 1.1f);
            ((HorizontalBarChart) chartClone).setData(((HorizontalBarChart) chart).getData());
            ((HorizontalBarChart) chartClone).setDoubleTapToZoomEnabled(false);
            ((HorizontalBarChart) chartClone).getBarData().setDrawValues(false);
            ((HorizontalBarChart) chartClone).setFitBars(true);
            ((HorizontalBarChart) chartClone).setPinchZoom(true);
        } else return;

        chartClone.getLegend().setEnabled(false);
        chartClone.setDescription(chart.getDescription());
        chartClone.setMinimumWidth(500);
        chartClone.setMinimumHeight(500);

        int padding = (int) getResources().getDimension(R.dimen.button_padding);
        ScrollView scrollView = new ScrollView(mParentActivity);
        LinearLayout linearLayout = new LinearLayout(mParentActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(padding, padding, padding, padding);

        TextView titleView = new TextView(mParentActivity);
        titleView.setText(title);
        titleView.setTextSize(fontSize * 1.25f);
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView statsView = new TextView(mParentActivity);
        statsView.setText(stats);
        statsView.setTextSize(fontSize * 1.2f);
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

    private void shareDialogText(String message) {
        Intent shareIntent = ShareCompat.IntentBuilder.from(mParentActivity)
                .setType("text/plain")
                .setText(String.format("My giving trends: %s\n#%s App",
                        message,
                        getString(R.string.app_name)))
                .getIntent();
        startActivity(shareIntent);
    }

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