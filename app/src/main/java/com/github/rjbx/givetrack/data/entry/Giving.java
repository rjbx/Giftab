package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class Giving extends Search implements Parcelable {

    private int frequency;
    private String percent;


    public static final Parcelable.Creator<Giving> CREATOR = new Parcelable.Creator<Giving>() {
        @Override public Giving createFromParcel(Parcel source) {
            return new Giving(source);
        }
        @Override public Giving[] newArray(int size) {
            return new Giving[size];
        }
    };

    Giving(Parcel source) {
        frequency = source.readInt();
        percent = source.readString();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(frequency);
        dest.writeString(percent);
    }

    @Override public int describeContents() {
        return 0;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Giving() {}

    public Giving(Search search, int frequency, String percent) {
        super(search);
        this.frequency = frequency;
        this.percent = percent;
    }

    public Giving(ContentValues values) {
        this.frequency = values.getAsInteger(DatabaseContract.Entry.COLUMN_DONATION_FREQUENCY);
        this.percent = values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_PERCENTAGE);
    }

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Giving(
            String ein,
            String name,
            String locationStreet,
            String locationDetail,
            String locationCity,
            String locationState,
            String locationZip,
            String homepageUrl,
            String navigatorUrl,
            String phone,
            String email,
            String impact,
            int type,
            int frequency,
            String percent) {
        super(
                ein,
                name,
                locationStreet,
                locationDetail,
                locationCity,
                locationState,
                locationZip,
                homepageUrl,
                navigatorUrl,
                phone,
                email,
                impact,
                type
        );
        this.frequency = frequency;
        this.percent = percent;
    }

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    public String getPercent() {
        return percent;
    }
    public void setPercent(String percent) {
        this.percent = percent;
    }

    @Exclude
    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("frequency", frequency);
        map.put("percent", percent);
        return map;
    }
}
