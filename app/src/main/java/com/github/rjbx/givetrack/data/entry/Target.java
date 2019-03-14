package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.rateraid.Rateraid;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Stores data about a giving target; associated with a {@link com.google.firebase.auth.FirebaseUser},
 * persisted locally with {@link android.content.ContentProvider}
 * and remotely with {@link com.google.firebase.database.FirebaseDatabase}.
 */
@IgnoreExtraProperties
public class Target extends Spawn implements Company, Rateraid.RatedObject<Target>, Parcelable, Cloneable {

    private int frequency;
    private String share;
    
    @Exclude public static final Parcelable.Creator<Target> CREATOR = new Parcelable.Creator<Target>() {
        @Override public Target createFromParcel(Parcel source) {
            return new Target(source);
        }
        @Override public Target[] newArray(int size) {
            return new Target[size];
        }
    };

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(frequency);
        dest.writeString(share);
    }

    @Override public int describeContents() {
        return 0;
    }

    public Target(Parcel source) {
        super(source);
        frequency = source.readInt();
        share = source.readString();
    }

    public Target(Spawn spawn, int frequency, String share) {
        super(spawn);
        this.frequency = frequency;
        this.share = share;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Target() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Target(
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
            String share) {
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
        this.share = share;
    }

    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    public void setShare(String share) { this.share = share; }
    public String getShare() { return share; }
    @Exclude @Override public double getPercent() { return Double.parseDouble(share); }
    @Exclude @Override public void setPercent(double percent) { this.share = String.valueOf(percent); }
    @Override public String getId() { return super.getId(); }
    @Exclude @Override public Target getObject() { return this; }

    @Override public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put(DatabaseContract.CompanyEntry.COLUMN_FREQUENCY, frequency);
        map.put(DatabaseContract.CompanyEntry.COLUMN_SHARE, share);
        return map;
    }

    @Override public void fromParameterMap(Map<String, Object> map) {
        super.fromParameterMap(map);
        frequency = (int) map.get(DatabaseContract.CompanyEntry.COLUMN_FREQUENCY);
        share = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_SHARE);
    }

    @Override public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_FREQUENCY, frequency);
        values.put(DatabaseContract.CompanyEntry.COLUMN_SHARE, share);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        frequency = values.getAsInteger(DatabaseContract.CompanyEntry.COLUMN_FREQUENCY);
        share = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_SHARE);
    }

    public Spawn getSuper() { return super.clone(); }

    public static Target fromSuper(Spawn spawn) {
        return new Target(spawn, 0, "0");
    }

    @Override public Target clone() {
        super.clone();
        return new Target(getSuper(), this.frequency, this.share);
    }

    public static Target getDefault() {
        Target target = new Target();
        target.setUid("");
        target.setEin("");
        target.setStamp(0);
        target.setName("");
        target.setLocationStreet("");
        target.setLocationDetail("");
        target.setLocationCity("");
        target.setLocationState("");
        target.setLocationZip("");
        target.setHomepageUrl("");
        target.setNavigatorUrl("");
        target.setPhone("");
        target.setEmail("");
        target.setImpact("");
        target.setType(0);
        target.setFrequency(0);
        target.setShare("");
        return target;
    }
}
