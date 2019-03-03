package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;


@IgnoreExtraProperties
public class Record extends Search implements Company, Parcelable, Cloneable {

    private String memo;
    private long time;

    @Exclude  public static final Parcelable.Creator<Record> CREATOR = new Parcelable.Creator<Record>() {
        @Override public Record createFromParcel(Parcel source) {
            return new Record(source);
        }
        @Override public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(memo);
        dest.writeLong(time);
    }

    @Override public int describeContents() {
        return 0;
    }

    Record(Parcel source) {
        super(source);
        memo = source.readString();
        time = source.readLong();
    }

    private Record(Search search, String memo, long time) {
        super(search);
        this.memo = memo;
        this.time = time;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Record() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Record(
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
             String impact,
             int type,
             String memo,
             long time) {
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
                impact,
                type
        );
        this.memo = memo;
        this.time = time;
    }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }
    @Override public String getId() { return String.valueOf(super.getStamp()); }
    public Record getObject() { return this; }

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("memo", memo);
        map.put("time", time);
        return map;
    }

    public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_MEMO, memo);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_TIME, time);
        return values;
    }

    public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        this.memo = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_DONATION_MEMO);
        this.time = values.getAsLong(DatabaseContract.CompanyEntry.COLUMN_DONATION_TIME);
     }

    public Search getSuper() { return super.clone(); }

    public static Record fromSuper(Search search) {
        Record record = new Record(search, "", 0);
        return record;
    }

    @Override public Record clone() {
        super.clone();
        return new Record(getSuper(), this.memo, this.time);
    }
    
    public static Record getDefault() {
        Record record = new Record();
        record.setUid("");
        record.setEin("");
        record.setStamp(0);
        record.setName("");
        record.setLocationStreet("");
        record.setLocationDetail("");
        record.setLocationCity("");
        record.setLocationState("");
        record.setLocationZip("");
        record.setHomepageUrl("");
        record.setNavigatorUrl("");
        record.setPhone("");
        record.setEmail("");
        record.setImpact("");
        record.setType(0);
        record.memo = "";
        record.time = 0;
        return record;
    }
}
