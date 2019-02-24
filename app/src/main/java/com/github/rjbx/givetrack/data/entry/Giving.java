package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.rateraid.Rateraid;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class Giving extends Search implements Company, Rateraid.RatedObject<Giving>, Parcelable, Cloneable {

    private int frequency;
    private String percent;


    @Exclude public static final Parcelable.Creator<Giving> CREATOR = new Parcelable.Creator<Giving>() {
        @Override public Giving createFromParcel(Parcel source) {
            return new Giving(source);
        }
        @Override public Giving[] newArray(int size) {
            return new Giving[size];
        }
    };

    @Exclude @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(frequency);
        dest.writeString(percent);
    }

    @Exclude @Override public int describeContents() {
        return 0;
    }

    Giving(Parcel source) {
        super(source);
        frequency = source.readInt();
        percent = source.readString();
    }

    private Giving(Search search, int frequency, String percent) {
        super(search);
        this.frequency = frequency;
        this.percent = percent;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Giving() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Giving(
            String ein,
            String uid,
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
                uid,
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

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    @Override public double getPercent() { return Double.parseDouble(percent); }
    @Override public void setPercent(double percent) { this.percent = String.valueOf(percent); }
    @Exclude @Override public Giving getObject() { return this; }

    @Exclude public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("frequency", frequency);
        map.put("percent", percent);
        return map;
    }

    @Exclude public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_FREQUENCY, frequency);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_PERCENTAGE, percent);
        return values;
    }

    @Exclude public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        this.frequency = values.getAsInteger(DatabaseContract.CompanyEntry.COLUMN_DONATION_FREQUENCY);
        this.percent = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_DONATION_PERCENTAGE);
    }

    @Exclude public Search getSuper() { return super.clone(); }

    @Exclude public static Giving fromSearch(Search search, int frequency, double percent) {
        return new Giving(search, frequency, String.valueOf(percent));
    }

    @Exclude @Override public Giving clone() {
        super.clone();
        return new Giving(getSuper(), this.frequency, this.percent);
    }

    @Exclude public static Giving getDefault() {
        Giving giving = new Giving();
        giving.setEin("");
        giving.setUid("");
        giving.setName("");
        giving.setLocationStreet("");
        giving.setLocationDetail("");
        giving.setLocationCity("");
        giving.setLocationState("");
        giving.setLocationZip("");
        giving.setHomepageUrl("");
        giving.setNavigatorUrl("");
        giving.setPhone("");
        giving.setEmail("");
        giving.setImpact("");
        giving.setType(0);
        giving.setFrequency(0);
        giving.setPercent(0d);
        return giving;
    }
}
