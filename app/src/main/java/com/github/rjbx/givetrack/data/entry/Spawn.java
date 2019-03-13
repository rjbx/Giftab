package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// TODO Convert ID to EIN
@IgnoreExtraProperties
public class Spawn implements Company, Parcelable, Cloneable {

    private String uid;
    private String ein;
    private long stamp;
    private String name;
    private String locationStreet;
    private String locationDetail;
    private String locationCity;
    private String locationState;
    private String locationZip;
    private String homepageUrl;
    private String navigatorUrl;
    private String phone;
    private String email;
    private String social;
    private String impact;
    private int type;

    @Exclude public static final Parcelable.Creator<Spawn> CREATOR = new Parcelable.Creator<Spawn>() {
        @Override public Spawn createFromParcel(Parcel source) { return new Spawn(source); }
        @Override public Spawn[] newArray(int size) { return new Spawn[size]; }
    };

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(ein);
        dest.writeLong(stamp);
        dest.writeString(name);
        dest.writeString(locationStreet);
        dest.writeString(locationDetail);
        dest.writeString(locationCity);
        dest.writeString(locationState);
        dest.writeString(locationZip);
        dest.writeString(homepageUrl);
        dest.writeString(navigatorUrl);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(social);
        dest.writeString(impact);
        dest.writeInt(type);
    }

    @Override public int describeContents() { return 0; }

    public Spawn(Parcel source) {
        uid = source.readString();
        ein = source.readString();
        stamp = source.readLong();
        name = source.readString();
        locationStreet = source.readString();
        locationDetail = source.readString();
        locationCity = source.readString();
        locationState = source.readString();
        locationZip = source.readString();
        homepageUrl = source.readString();
        navigatorUrl = source.readString();
        phone = source.readString();
        email = source.readString();
        social = source.readString();
        impact = source.readString();
        type = source.readInt();
    }

    public Spawn(Spawn spawn) {
        this.uid = spawn.uid;
        this.ein = spawn.ein;
        this.stamp = spawn.stamp;
        this.name = spawn.name;
        this.locationStreet = spawn.locationStreet;
        this.locationDetail = spawn.locationDetail;
        this.locationCity = spawn.locationCity;
        this.locationState = spawn.locationState;
        this.locationZip = spawn.locationZip;
        this.homepageUrl = spawn.homepageUrl;
        this.navigatorUrl = spawn.navigatorUrl;
        this.phone = spawn.phone;
        this.email = spawn.email;
        this.social = spawn.social;
        this.impact = spawn.impact;
        this.type = spawn.type;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Spawn() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Spawn(
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
            int type) {
        this.uid = uid;
        this.ein = ein;
        this.stamp = stamp;
        this.name = name;
        this.locationStreet = locationStreet;
        this.locationDetail = locationDetail;
        this.locationCity = locationCity;
        this.locationState = locationState;
        this.locationZip = locationZip;
        this.homepageUrl = homepageUrl;
        this.navigatorUrl = navigatorUrl;
        this.phone = phone;
        this.email = email;
        this.social = social;
        this.impact = impact;
        this.type = type;
    }

    @Override public String getUid() { return uid; }
    @Override public void setUid(String uid) { this.uid = uid; }
    @Override public String getEin() { return ein; }
    @Override public void setEin(String ein) { this.ein = ein; }
    @Override public long getStamp() { return stamp; }
    @Override public void setStamp(long stamp) { this.stamp = stamp; }
    @Override public String getName() { return name; }
    @Override public void setName(String name) { this.name = name; }
    @Override public String getLocationStreet() { return locationStreet; }
    @Override public void setLocationStreet(String locationStreet) { this.locationStreet = locationStreet; }
    @Override public String getLocationDetail() { return locationDetail; }
    @Override public void setLocationDetail(String locationDetail) { this.locationDetail = locationDetail; }
    @Override public String getLocationCity() { return locationCity; }
    @Override public void setLocationCity(String locationCity) { this.locationCity = locationCity; }
    @Override public String getLocationState() { return locationState; }
    @Override public void setLocationState(String locationState) { this.locationState = locationState; }
    @Override public String getLocationZip() { return locationZip; }
    @Override public void setLocationZip(String locationZip) { this.locationZip = locationZip; }
    @Override public String getHomepageUrl() { return homepageUrl; }
    @Override public void setHomepageUrl(String homepageUrl) { this.homepageUrl = homepageUrl; }
    @Override public String getNavigatorUrl() { return navigatorUrl; }
    @Override public void setNavigatorUrl(String navigatorUrl) { this.navigatorUrl = navigatorUrl; }
    @Override public String getPhone() { return phone; }
    @Override public void setPhone(String phone) { this.phone = phone; }
    @Override public String getEmail() { return email; }
    @Override public void setEmail(String email) { this.email = email; }
    @Override public String getSocial() { return social; }
    @Override public void setSocial(String social) { this.social = social; }
    @Override public String getImpact() { return impact; }
    @Override public void setImpact(String impact) { this.impact = impact; }
    @Override public int getType() { return type; }
    @Override public void setType(int type) { this.type = type; }
    @Override public String getId() { return String.valueOf(stamp); }
    @Exclude @Override public Spawn getObject() { return this; }
    
    @Override public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(DatabaseContract.CompanyEntry.COLUMN_UID, uid);
        map.put(DatabaseContract.CompanyEntry.COLUMN_EIN, ein);
        map.put(DatabaseContract.CompanyEntry.COLUMN_STAMP, stamp);
        map.put(DatabaseContract.CompanyEntry.COLUMN_NAME, name);
        map.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STREET, locationStreet);
        map.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_DETAIL, locationDetail);
        map.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_CITY, locationCity);
        map.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STATE, locationState);
        map.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_ZIP, locationZip);
        map.put(DatabaseContract.CompanyEntry.COLUMN_HOMEPAGE_URL, homepageUrl);
        map.put(DatabaseContract.CompanyEntry.COLUMN_NAVIGATOR_URL, navigatorUrl);
        map.put(DatabaseContract.CompanyEntry.COLUMN_PHONE, phone);
        map.put(DatabaseContract.CompanyEntry.COLUMN_EMAIL, email);
        map.put(DatabaseContract.CompanyEntry.COLUMN_SOCIAL, social);
        map.put(DatabaseContract.CompanyEntry.COLUMN_IMPACT, impact);
        map.put(DatabaseContract.CompanyEntry.COLUMN_TYPE, type);
        return map;
    }
    
    @Override public void fromParameterMap(Map<String, Object> map) {
        ein = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_EIN);
        stamp = (long) map.get(DatabaseContract.CompanyEntry.COLUMN_STAMP);
        name = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_NAME);
        locationStreet = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STREET);
        locationDetail = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_LOCATION_DETAIL);
        locationCity = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_LOCATION_CITY);
        locationState = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STATE);
        locationZip = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_LOCATION_ZIP);
        homepageUrl = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_HOMEPAGE_URL);
        navigatorUrl = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_NAVIGATOR_URL);
        phone = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_PHONE);
        email = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_EMAIL);
        social = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_SOCIAL);
        impact = (String) map.get(DatabaseContract.CompanyEntry.COLUMN_IMPACT);
        type = (int) map.get(DatabaseContract.CompanyEntry.COLUMN_TYPE);

    }

    @Override public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_UID, uid);
        values.put(DatabaseContract.CompanyEntry.COLUMN_EIN, ein);
        values.put(DatabaseContract.CompanyEntry.COLUMN_STAMP, stamp);
        values.put(DatabaseContract.CompanyEntry.COLUMN_NAME, name);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STREET, locationStreet);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_DETAIL, locationDetail);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_CITY, locationCity);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STATE, locationState);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_ZIP, locationZip);
        values.put(DatabaseContract.CompanyEntry.COLUMN_HOMEPAGE_URL, homepageUrl);
        values.put(DatabaseContract.CompanyEntry.COLUMN_NAVIGATOR_URL, navigatorUrl);
        values.put(DatabaseContract.CompanyEntry.COLUMN_PHONE, phone);
        values.put(DatabaseContract.CompanyEntry.COLUMN_EMAIL, email);
        values.put(DatabaseContract.CompanyEntry.COLUMN_SOCIAL, social);
        values.put(DatabaseContract.CompanyEntry.COLUMN_IMPACT, impact);
        values.put(DatabaseContract.CompanyEntry.COLUMN_TYPE, type);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        uid = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_UID);
        ein = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_EIN);
        stamp = values.getAsLong(DatabaseContract.CompanyEntry.COLUMN_STAMP);
        name = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_NAME);
        locationStreet = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STREET);
        locationDetail = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_DETAIL);
        locationCity = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_CITY);
        locationState = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STATE);
        locationZip = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_ZIP);
        homepageUrl = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_HOMEPAGE_URL);
        navigatorUrl = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_NAVIGATOR_URL);
        phone = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_PHONE);
        email = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_EMAIL);
        social = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_SOCIAL);
        impact = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_IMPACT);
        type = values.getAsInteger(DatabaseContract.CompanyEntry.COLUMN_TYPE);
    }

    @Override public Spawn clone() {
        Spawn clone  = new Spawn(this);
        try { super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Class must implement Cloneable interface");
        } return clone;
    }

    public static Spawn getDefault() {
        Spawn spawn = new Spawn();
        spawn.uid = "";
        spawn.ein = "";
        spawn.stamp = 0;
        spawn.name = "";
        spawn.locationStreet = "";
        spawn.locationDetail = "";
        spawn.locationCity = "";
        spawn.locationState = "";
        spawn.locationZip = "";
        spawn.homepageUrl = "";
        spawn.navigatorUrl = "";
        spawn.phone = "";
        spawn.email = "";
        spawn.social = "";
        spawn.impact = "";
        spawn.type = 0;
        return spawn;
    }
}
