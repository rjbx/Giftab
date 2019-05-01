package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

// TODO: Implement memo
// TODO: Dynamically resize impact text according to String length
/**
 * Stores data about a giving record; associated with a {@link com.google.firebase.auth.FirebaseUser},
 * persisted locally with {@link android.content.ContentProvider}
 * and remotely with {@link com.google.firebase.database.FirebaseDatabase}.
 */
@IgnoreExtraProperties
public class Record extends Spawn implements Company, Parcelable, Cloneable {

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

    public Record(Parcel source) {
        super(source);
        memo = source.readString();
        time = source.readLong();
    }

    public Record(Spawn spawn, String memo, long time) {
        super(spawn);
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
             String social,
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
                social,
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
    @Override public String getId() { return super.getId(); }
    @Exclude @Override public Record getObject() { return this; }

    @Override public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put(DatabaseContract.CompanyEntry.COLUMN_MEMO, memo);
        map.put(DatabaseContract.CompanyEntry.COLUMN_TIME, time);
        return map;
    }

    @Override public void fromParameterMap(Map<String, Object> map) {
        super.fromParameterMap(map);
        memo = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_MEMO);
        time = (long) map.get(DatabaseContract.CompanyEntry.COLUMN_TIME);
    }

    @Override public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_MEMO, memo);
        values.put(DatabaseContract.CompanyEntry.COLUMN_TIME, time);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        memo = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_MEMO);
        time = values.getAsLong(DatabaseContract.CompanyEntry.COLUMN_TIME);
     }

    public Spawn getSuper() { return super.clone(); }

    public static Record fromSuper(Spawn spawn) {
        return new Record(spawn, "", 0);
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
