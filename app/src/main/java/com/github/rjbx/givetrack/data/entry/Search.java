package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Search implements Company, Parcelable, Cloneable {

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
    private String impact;
    private int type;

    @Exclude public static final Parcelable.Creator<Search> CREATOR = new Parcelable.Creator<Search>() {
        @Override public Search createFromParcel(Parcel source) { return new Search(source); }
        @Override public Search[] newArray(int size) { return new Search[size]; }
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
        dest.writeString(impact);
        dest.writeInt(type);
    }

    @Override public int describeContents() { return 0; }

    public Search(Parcel source) {
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
        impact = source.readString();
        type = source.readInt();
    }

    public Search(Search search) {
        this.uid = search.uid;
        this.ein = search.ein;
        this.stamp = search.stamp;
        this.name = search.name;
        this.locationStreet = search.locationStreet;
        this.locationDetail = search.locationDetail;
        this.locationCity = search.locationCity;
        this.locationState = search.locationState;
        this.locationZip = search.locationZip;
        this.homepageUrl = search.homepageUrl;
        this.navigatorUrl = search.navigatorUrl;
        this.phone = search.phone;
        this.email = search.email;
        this.impact = search.impact;
        this.type = search.type;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Search() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public Search(
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
    @Override public String getImpact() { return impact; }
    @Override public void setImpact(String impact) { this.impact = impact; }
    @Override public int getType() { return type; }
    @Override public void setType(int type) { this.type = type; }
    @Override public String getId() { return ein; }
    @Override public Search getObject() { return this; }
    
    @Override public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("ein", ein);
        map.put("stamp", stamp);
        map.put("name", name );
        map.put("locationStreet", locationStreet);
        map.put("locationDetail", locationDetail);
        map.put("locationCity", locationCity);
        map.put("locationState", locationState);
        map.put("locationZip", locationZip);
        map.put("homepageUrl", homepageUrl);
        map.put("navigatorUrl", navigatorUrl);
        map.put("phone", phone);
        map.put("email", email);
        map.put("impact", impact);
        map.put("type", type);
        return map;
    }
    
    @Override public void fromParameterMap(Map<String, Object> map) {
        uid = (String) map.get("uid");
        ein = (String) map.get("ein");
        stamp = (long) map.get("stamp");
        name = (String) map.get("name");
        locationStreet = (String) map.get("locationStreet");
        locationDetail = (String) map.get("locationDetail");
        locationCity = (String) map.get("locationCity");
        locationState = (String) map.get("locationState");
        locationZip = (String) map.get("locationZip");
        homepageUrl = (String) map.get("homepageUrl");
        navigatorUrl = (String) map.get("navigatorUrl");
        phone = (String) map.get("phone");
        email = (String) map.get("email");
        impact = (String) map.get("impact");
        type = (int) map.get("type");
    }

    @Override public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_UID, uid);
        values.put(DatabaseContract.CompanyEntry.COLUMN_EIN, ein);
        values.put(DatabaseContract.CompanyEntry.COLUMN_STAMP, stamp);
        values.put(DatabaseContract.CompanyEntry.COLUMN_CHARITY_NAME, name);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STREET, locationStreet);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_DETAIL, locationDetail);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_CITY, locationCity);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STATE, locationState);
        values.put(DatabaseContract.CompanyEntry.COLUMN_LOCATION_ZIP, locationZip);
        values.put(DatabaseContract.CompanyEntry.COLUMN_HOMEPAGE_URL, homepageUrl);
        values.put(DatabaseContract.CompanyEntry.COLUMN_NAVIGATOR_URL, navigatorUrl);
        values.put(DatabaseContract.CompanyEntry.COLUMN_PHONE_NUMBER, phone);
        values.put(DatabaseContract.CompanyEntry.COLUMN_EMAIL_ADDRESS, email);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_IMPACT, impact);
        values.put(DatabaseContract.CompanyEntry.COLUMN_DONATION_TYPE, type);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        this.uid = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_UID);
        this.ein = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_EIN);
        this.stamp = values.getAsLong(DatabaseContract.CompanyEntry.COLUMN_STAMP);
        this.name = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_CHARITY_NAME);
        this.locationStreet = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STREET);
        this.locationDetail = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_DETAIL);
        this.locationCity = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_CITY);
        this.locationState = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_STATE);
        this.locationZip = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_LOCATION_ZIP);
        this.homepageUrl = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_HOMEPAGE_URL);
        this.navigatorUrl = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_NAVIGATOR_URL);
        this.phone = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_PHONE_NUMBER);
        this.email = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_EMAIL_ADDRESS);
        this.impact = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_DONATION_IMPACT);
        this.type = values.getAsInteger(DatabaseContract.CompanyEntry.COLUMN_DONATION_TYPE);
    }

    @Override public Search clone() {
        Search clone  = new Search(this);
        try { super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Class must implement Cloneable interface");
        } return clone;
    }

    public static Search getDefault() {
        Search search = new Search();
        search.uid = "";
        search.ein = "";
        search.stamp = 0;
        search.name = "";
        search.locationStreet = "";
        search.locationDetail = "";
        search.locationCity = "";
        search.locationState = "";
        search.locationZip = "";
        search.homepageUrl = "";
        search.navigatorUrl = "";
        search.phone = "";
        search.email = "";
        search.impact = "";
        search.type = 0;
        return search;
    }
}
