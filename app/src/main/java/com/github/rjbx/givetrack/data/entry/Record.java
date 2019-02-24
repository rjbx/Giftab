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

    public static final Parcelable.Creator<Record> CREATOR = new Parcelable.Creator<Record>() {
        @Override public Record createFromParcel(Parcel source) {
            return new Record(source);
        }
        @Override public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    Record(Parcel source) {
        super(source);
        memo = source.readString();
        time = source.readLong();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(memo);
        dest.writeLong(time);
    }

    @Override public int describeContents() {
        return 0;
    }
    
    /**
     * Provides default constructor required for object relational mapping.
     */
    public Record() {}

    public Record(Search search, String memo, long time) {
        super(search);
        this.memo = memo;
        this.time = time;
    }

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
             long time) {
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
    }

    public String getMemo() {
        return memo;
    }
    public void setMemo(String memo) { this.memo = memo; }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    @Exclude public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("memo", memo);
        map.put("time", time);
        return map;
    }

    @Exclude public ContentValues toContentValues() {
        ContentValues values = super.toContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_MEMO, memo);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_TIME, time);
        return values;
    }

    @Exclude public void fromContentValues(ContentValues values) {
        super.fromContentValues(values);
        this.memo = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_DONATION_MEMO);
        this.time = values.getAsLong(DatabaseContract.CompanyEntry.COLUMN_DONATION_TIME);
    }

    @Exclude public Search getSearch() {
        return super.clone();
    }

    @Override public Record clone() {
        super.clone();
        return new Record(this, this.memo, this.time);
    }
}
