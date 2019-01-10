package com.github.rjbx.givetrack.ui;

import android.content.ContentValues;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.UserPreferences;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Provides the logic and views for an activity overview screen.
 */
public class HomeFragment extends Fragment {


    private MainActivity mParentActivity;
    View mRootView;
    ContentValues[] mValuesArray;
    private static final int [] COLORS = new int[] {
        R.color.colorAttention,
        R.color.colorAccent,
        R.color.colorConversion,
        R.color.colorPrimary,
        R.color.colorComfort,
        R.color.colorNeutral
    };

    private static int sThemeIndex;
    private static boolean mShowTracked;
    private TextView mAmountView;
    private TextView mAmountLabel;
    private String mTotal;
    private String mTracked;
    private String mTrackedTime;
    private String mTotalTime;


    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public HomeFragment() {}

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    public static HomeFragment newInstance(@Nullable Bundle args) {
        HomeFragment fragment = new HomeFragment();
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


        Bundle args = getArguments();
        if (args != null) mValuesArray = (ContentValues[]) args.getParcelableArray(MainActivity.ARGS_VALUES_ARRAY);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();

        float tracked = Float.parseFloat(UserPreferences.getTracked(getContext()));
        mTracked = currencyFormatter.format(tracked);

        List<String> charities = UserPreferences.getCharities(getContext());
        float totalImpact = 0f;
        for (String charity : charities) totalImpact += Float.parseFloat(charity.split(":")[2]);
        mTotal = currencyFormatter.format(totalImpact);

        Date date = new Date(UserPreferences.getTimetrack(getContext()));
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
        dateFormatter.setTimeZone(TimeZone.getDefault());
        String formattedDate = dateFormatter.format(date);
        mTrackedTime = String.format("since %s", formattedDate);
        mTotalTime = "all-time";

        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        mAmountView = mRootView.findViewById(R.id.home_amount_text);
        mAmountView.setOnClickListener(clickedView -> {
            sThemeIndex++;
            if (sThemeIndex == 6) sThemeIndex = 0;
            mParentActivity.findViewById(R.id.home_amount_wrapper).setBackgroundColor(getResources().getColor(COLORS[sThemeIndex]));
            UserPreferences.setTheme(getContext(), sThemeIndex);
            UserPreferences.updateFirebaseUser(getContext());
        });

        mAmountLabel = mRootView.findViewById(R.id.home_amount_label);
        mAmountLabel.setOnClickListener(clickedView -> toggleAmountView());

        mRootView.findViewById(R.id.home_time_button).setOnClickListener(clickedView -> {
            AlertDialog timeDialog = new AlertDialog.Builder(getContext()).create();
            timeDialog.setMessage(String.format("Your tracked data %s will be lost. Do you want to start tracking from today instead?", mTrackedTime));
            timeDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel),
                    (onClickDialog, onClickPosition) -> timeDialog.dismiss());
            timeDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm),
                    (onClickDialog, onClickPosition) -> {
                        UserPreferences.setTracked(getContext(), "0");
                        UserPreferences.setTimetrack(getContext(), System.currentTimeMillis());
                        mAmountView.setText("0");
                    });
            timeDialog.show();
            timeDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
            timeDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
        });

        mRootView.findViewById(R.id.home_config_button).setOnClickListener(clickedView -> {

        });

        mRootView.findViewById(R.id.home_share_button).setOnClickListener(clickedView -> {
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
        });

        toggleAmountView();

        return mRootView;
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
        if (mRootView != null) createChart();
    }

    public void toggleAmountView() {
        mShowTracked = !mShowTracked;
        if (mShowTracked) {
            mAmountLabel.setText(mTrackedTime.toUpperCase());
            mAmountView.setText(String.valueOf(mTracked));
        } else {
            mAmountLabel.setText(mTotalTime.toUpperCase());
            mAmountView.setText(String.valueOf(mTotal));
        }
    }

    /**
     * Builds the charts supplied by the Fragment layout.
     */
    private void createChart() {

        Context context = getContext();
        if (context == null) return;

        int fontSize = (int) getResources().getDimension(R.dimen.text_size_subtitle);
        int backgroundColor = getResources().getColor(R.color.colorChalk);

        List<PieEntry> percentageEntries = new ArrayList<>();
        float donationAmount = 0f;
        int donationFrequency = 0;

        if (mValuesArray == null || mValuesArray.length == 0) return;
        for (ContentValues values : mValuesArray) {
            float percentage = Float.parseFloat(values.getAsString(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE));
            if (percentage < .01f) continue;
            String name = values.getAsString(GivetrackContract.Entry.COLUMN_CHARITY_NAME);
            if (name.length() > 20) { name = name.substring(0, 20);
            name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }
            percentageEntries.add(new PieEntry(percentage, name));

            donationAmount += Float.parseFloat(values.getAsString(GivetrackContract.Entry.COLUMN_DONATION_IMPACT));
            donationFrequency += values.getAsInteger(GivetrackContract.Entry.COLUMN_DONATION_FREQUENCY);
        }

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
        PieChart percentageChart = mRootView.findViewById(R.id.percentage_chart);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.piechart_diameter));
            params.setMargins(margin * 2, margin, margin * 2, margin);
            percentageChart.setLayoutParams(params);
        }
//        percentageChart.animateY(1000, Easing.EasingOption.EaseInOutCirc);
        percentageChart.setData(percentageData);
        percentageChart.setDescription(percentageDesc);
        percentageChart.setEntryLabelTextSize(fontSize * 1.25f);
        percentageChart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        percentageChart.setHoleRadius(20f);
        percentageChart.setTransparentCircleRadius(50f);
        percentageChart.setBackgroundColor(backgroundColor);
        percentageChart.setTransparentCircleColor(backgroundColor);
        percentageChart.setHoleColor(backgroundColor);
        percentageChart.setRotationEnabled(false);
        percentageChart.getLegend().setEnabled(false);
        percentageChart.invalidate();

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
        PieChart usageChart = mRootView.findViewById(R.id.usage_chart);
        usageChart.setData(usageData);
        usageChart.setDescription(usageDesc);
        usageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        usageChart.setHoleRadius(15f);
        usageChart.setBackgroundColor(backgroundColor);
        usageChart.setTransparentCircleColor(backgroundColor);
        usageChart.setHoleColor(backgroundColor);
        usageChart.getLegend().setEnabled(false);
        usageChart.invalidate();

        float conversionsTotal = donationFrequency;
        List<PieEntry> typeEntries = new ArrayList<>();
        typeEntries.add(new PieEntry(conversionsTotal != 0f ? donationFrequency / conversionsTotal : .5f, getString(R.string.indicator_donation_frequency)));

        PieDataSet typeSet = new PieDataSet(typeEntries, "");
        typeSet.setColors(gaugeColors);
        Description typeDesc = new Description();
        typeDesc.setText(getString(R.string.chart_title_type));
        typeDesc.setTextSize(fontSize / 1.1f);

        PieData typeData = new PieData(typeSet);
        PieChart typeChart = mRootView.findViewById(R.id.type_chart);
        typeChart.setData(typeData);
        typeChart.setDescription(typeDesc);
        typeChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        typeChart.setHoleRadius(15f);
        typeChart.setBackgroundColor(backgroundColor);
        typeChart.setTransparentCircleColor(backgroundColor);
        typeChart.setHoleColor(backgroundColor);
        typeChart.getLegend().setEnabled(false);
        typeChart.invalidate();

        Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        String[] tallyArray =
                UserPreferences.getTally(context).split(":");

        long currentTime = System.currentTimeMillis();
        long lastConversionTime = UserPreferences.getTimestamp(context);
        long timeBetweenConversions = currentTime - lastConversionTime;

        long daysBetweenConversions =
                TimeUnit.DAYS.convert(
                        timeBetweenConversions,
                        TimeUnit.MILLISECONDS
                );

        if (daysBetweenConversions > 0) {
            for (int j = 0; j < daysBetweenConversions; j++) tallyArray[j] = "0";
            UserPreferences.setTally(mParentActivity, Arrays.asList(tallyArray).toString().replace("[", "").replace("]", "").replace(", ", ":"));
        }

        float daysSum = 0;
        float[] days = new float[tallyArray.length];
        for (int j = 0; j < tallyArray.length; j++) {
            days[j] = Float.parseFloat(tallyArray[j]);
            daysSum += days[j];
        }

        float today = Float.parseFloat(tallyArray[0]);
        float high = Math.max(Float.parseFloat(UserPreferences.getHigh(context)), today);
        UserPreferences.setHigh(context, String.format(Locale.getDefault(), "%.2f", high));
        UserPreferences.updateFirebaseUser(mParentActivity);

        List<PieEntry> averageEntries = new ArrayList<>();
        averageEntries.add(new PieEntry(high, getString(R.string.axis_value_alltime)));
        averageEntries.add(new PieEntry(daysSum / 7, getString(R.string.axis_value_daily)));

        PieDataSet averageSet = new PieDataSet(averageEntries, "");
        averageSet.setColors(gaugeColors);
        Description averageDesc = new Description();
        averageDesc.setText(getString(R.string.chart_title_average));
        averageDesc.setTextSize(fontSize / 1.1f);
        PieData averageData = new PieData(averageSet);
        PieChart averageChart = mRootView.findViewById(R.id.average_chart);
        averageChart.setData(averageData);
        averageChart.setDescription(averageDesc);
        averageChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        averageChart.setHoleRadius(15f);
        averageChart.setBackgroundColor(backgroundColor);
        averageChart.setTransparentCircleColor(backgroundColor);
        averageChart.setHoleColor(backgroundColor);
        averageChart.getLegend().setEnabled(false);
        averageChart.invalidate();

        List<BarEntry> activityEntries = new ArrayList<>();
        activityEntries.add(new BarEntry(0f, high));
        activityEntries.add(new BarEntry(1f, days[0]));
        activityEntries.add(new BarEntry(2f, days[1]));

        activityEntries.add(new BarEntry(3f, days[2]));
        activityEntries.add(new BarEntry(4f, days[3]));
        activityEntries.add(new BarEntry(5f, days[4]));
        activityEntries.add(new BarEntry(6f, days[5]));
        activityEntries.add(new BarEntry(7f, days[6]));

        BarDataSet activityDataSet = new BarDataSet(activityEntries, "");
        activityDataSet.setColors(chartColors);

        BarData activityData = new BarData(activityDataSet);
        activityData.setBarWidth(.75f);
        Description activityDesc = new Description();
        activityDesc.setText(getString(R.string.chart_title_activity));
        activityDesc.setTextSize(fontSize);

        BarChart activityChart = mRootView.findViewById(R.id.activity_chart);
        activityChart.setData(activityData);
        activityChart.setDescription(activityDesc);
        activityChart.getXAxis().setValueFormatter((axisValue, axis) -> {
            switch ((int) axisValue) {
                case 0: return getString(R.string.axis_value_high);
                case 1: return getString(R.string.axis_value_today);
                case 2: return getString(R.string.axis_value_yesterday);
                default: return getString(R.string.axis_value_days, (int) axisValue);
            }
        });
        activityChart.getXAxis().setTextSize(fontSize / 1.1f);
        activityChart.setFitBars(true);
        activityChart.getLegend().setEnabled(false);
        activityChart.setPinchZoom(true);
        activityChart.notifyDataSetChanged();
        activityChart.invalidate();
    }
}