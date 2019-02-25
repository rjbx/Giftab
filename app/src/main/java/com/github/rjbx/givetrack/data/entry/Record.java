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
    private int rid;

    @Exclude  public static final Parcelable.Creator<Record> CREATOR = new Parcelable.Creator<Record>() {
        @Override public Record createFromParcel(Parcel source) {
            return new Record(source);
        }
        @Override public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    @Exclude @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(memo);
        dest.writeLong(time);
        dest.writeLong(rid);
    }

    @Exclude @Override public int describeContents() {
        return 0;
    }

    Record(Parcel source) {
        super(source);
        memo = source.readString();
        time = source.readLong();
        rid = source.readInt();
    }

    private Record(Search search, String memo, long time, int rid) {
        super(search);
        this.memo = memo;
        this.time = time;
        this.rid = rid;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Record() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Record(
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
             String memo,
             long time,
             int rid) {
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
        this.memo = memo;
        this.time = time;
        this.rid = rid;
    }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }
    public int getRid() { return rid; }
    public void setRid(int rid) { this.rid = rid; }

    @Exclude public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("memo", memo);
        map.put("time", time);
        map.put("rid", rid);
        return map;
    }


    // TODO: If operation is addition, RID value should be null; if update,
    @Exclude public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_MEMO, memo);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_TIME, time);
        values.put(DatabaseContract.CompanyEntry.COLUMN_RID, rid == -1 ? null : rid );
        return values;
    }

    @Exclude public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        this.memo = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_DONATION_MEMO);
        this.time = values.getAsLong(DatabaseContract.CompanyEntry.COLUMN_DONATION_TIME);
        this.rid = values.getAsInteger(DatabaseContract.CompanyEntry.COLUMN_RID);
    }

    @Exclude public Search getSuper() { return super.clone(); }

    @Exclude @Override public Record clone() {
        super.clone();
        return new Record(getSuper(), this.memo, this.time, this.rid);
    }
    
    @Exclude public static Record getDefault() {
        Record record = new Record();
        record.setEin("");
        record.setUid("");
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
        record.rid = -1;
        return record;
    }
}
