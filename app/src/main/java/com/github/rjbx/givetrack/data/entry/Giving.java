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

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(frequency);
        dest.writeString(percent);
    }

    @Override public int describeContents() {
        return 0;
    }

    public Giving(Parcel source) {
        super(source);
        frequency = source.readInt();
        percent = source.readString();
    }

    public Giving(Search search, int frequency, String percent) {
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
            String uid,
            String ein,
            long stamp,
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
            String social,
            String impact,
            int type,
            int frequency,
            String percent) {
        super(
                uid,
                ein,
                stamp,
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
                social,
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
    @Override public String getId() { return super.getId(); }
    @Override public Giving getObject() { return this; }

    @Override public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("frequency", frequency);
        map.put("percent", percent);
        return map;
    }

    @Override public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_FREQUENCY, frequency);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_PERCENTAGE, percent);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        this.frequency = values.getAsInteger(DatabaseContract.CompanyEntry.COLUMN_DONATION_FREQUENCY);
        this.percent = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_DONATION_PERCENTAGE);
    }

    public Search getSuper() { return super.clone(); }

    public static Giving fromSuper(Search search) {
        Giving giving = new Giving(search, 0, "0");
        return giving;
    }

    @Override public Giving clone() {
        super.clone();
        return new Giving(getSuper(), this.frequency, this.percent);
    }

    public static Giving getDefault() {
        Giving giving = new Giving();
        giving.setUid("");
        giving.setEin("");
        giving.setStamp(0);
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
