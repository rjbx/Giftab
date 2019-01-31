package com.github.rjbx.rateraid;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.github.rjbx.calibrater.Calibrater;
import com.github.rjbx.calibrater.TypeConverters;

import java.text.NumberFormat;
import java.text.ParseException;
import androidx.annotation.Nullable;

public class Rateraid {

    private double[] mPercentages;
    private void setPercentages(double[] percentages) { this.mPercentages = percentages; }
    private void setPercentages(float[] percentages) { this.mPercentages = TypeConverters.arrayFloatToDouble(percentages); }
    private void setPercentages(Double[] percentages) {this.mPercentages = TypeConverters.arrayBoxedToPrimitiveDouble(percentages); }
    private void setPercentages(Float[] percentages) {this.mPercentages = TypeConverters.arrayFloatBoxedToDouble(percentages); }
    public double[] getPercentages() { return mPercentages; }
    public float[] getPercentagesBoxedFloat() { return TypeConverters.arrayDoubleToFloat(mPercentages); }
    public Double[] getPercentagesBoxedDouble() { return TypeConverters.arrayPrimitiveToBoxedDouble(mPercentages); }
    public Float[] getPercentagesFloat() { return TypeConverters.arrayDoubleToFloatBoxed(mPercentages); }

    public static Rateraid.Builder with(double[] percentages, float magnitude, int precision, @Nullable View.OnClickListener clickListener) {
        return new Rateraid.Builder(percentages, magnitude, precision,  clickListener);
    }

    public static class Builder {

        private Rateraid mRateraid;
        private double[] mPercentages;
        private double mMagnitude;
        private int mPrecision;
        private View.OnClickListener mClickListener;

        Builder(double[] percentages, float magnitude, int precision, @Nullable View.OnClickListener clickListener) {
            mPercentages = percentages;
            mMagnitude = magnitude;
            mPrecision = precision;
            mClickListener = clickListener;
        }

        public Rateraid.Builder addButtonSet(View incrementButton, View decrementButton, int index) {
            incrementButton.setOnClickListener(clickedView -> {
                Calibrater.shiftRatings(mPercentages, index, mMagnitude, mPrecision);
                if (mClickListener != null) mClickListener.onClick(incrementButton);
            });
            decrementButton.setOnClickListener(clickedView -> {
                Calibrater.shiftRatings(mPercentages, index, -mMagnitude, mPrecision);
                if (mClickListener != null) mClickListener.onClick(decrementButton);
            });
            return this;
        }

        public Rateraid.Builder addValueEditor(TextView valueEditor, int index) {
            valueEditor.setOnEditorActionListener((onEditorActionView, onEditorActionId, onEditorActionEvent) -> {
                switch (onEditorActionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        final NumberFormat percentFormatter = NumberFormat.getPercentInstance();
                        try {
                            float percentage;
                            String viewText = onEditorActionView.getText().toString();
                            if (viewText.contains("%")) percentage = percentFormatter.parse(viewText).floatValue();
                            else percentage = Float.parseFloat(viewText);
                            double magnitude = percentage - mPercentages[index];
                            Calibrater.shiftRatings(mPercentages, index, magnitude, mPrecision);
                            if (mClickListener != null) mClickListener.onClick(valueEditor);
                        } catch (ParseException e) {
                            throw new NumberFormatException();
                        }
                        return true;
                    default:
                        return false;
                }
            });
            return this;
        }

        public Rateraid build() {
            mRateraid = new Rateraid();
            mRateraid.setPercentages(mPercentages);
            return mRateraid;
        }
    }
}