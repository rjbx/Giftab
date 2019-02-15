package com.github.rjbx.givetrack.data.entry;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Record extends Company {

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
        memo = source.readString();
        time = source.readInt();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
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

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Record(
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
             String memo,
             long time) {
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
        this.memo = memo;
        this.time = time;
    }

    public String getmemo() {
        return memo;
    }
    public void setmemo(String memo) {
        this.memo = memo;
    }
    public long gettime() {
        return time;
    }
    public void settime(long time) {
        this.time = time;
    }

    @Exclude public Map<String, Object> toParameterMap() {
        Map<String, Object> map = super.toParameterMap();
        map.put("memo", memo);
        map.put("time", time);
        return map;
    }
}
