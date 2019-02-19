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

    private String ein;
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

    public static final Parcelable.Creator<Search> CREATOR = new Parcelable.Creator<Search>() {
        @Override public Search createFromParcel(Parcel source) {
            return new Search(source);
        }
        @Override public Search[] newArray(int size) {
            return new Search[size];
        }
    };

    Search(Parcel source) {
        ein = source.readString();
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

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ein);
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

    @Override public int describeContents() {
        return 0;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public Search() {}

    public Search(Search search) {
        this.ein = search.ein;
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
     * Provides POJO constructor required for object relational mapping.
     */
    public Search(
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
            int type) {
        this.ein = ein;
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
    
    public String getEin() {
        return ein;
    }
    public void setEin(String ein) {
        this.ein = ein;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLocationStreet() {
        return locationStreet;
    }
    public void setLocationStreet(String locationStreet) {
        this.locationStreet = locationStreet;
    }
    public String getLocationDetail() {
        return locationDetail;
    }
    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }
    public String getLocationCity() {
        return locationCity;
    }
    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }
    public String getLocationState() {
        return locationState;
    }
    public void setLocationState(String locationState) {
        this.locationState = locationState;
    }
    public String getLocationZip() {
        return locationZip;
    }
    public void setLocationZip(String locationZip) {
        this.locationZip = locationZip;
    }
    public String getHomepageUrl() {
        return homepageUrl;
    }
    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }
    public String getNavigatorUrl() {
        return navigatorUrl;
    }
    public void setNavigatorUrl(String navigatorUrl) {
        this.navigatorUrl = navigatorUrl;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getImpact() {
        return impact;
    }
    public void setImpact(String impact) {
        this.impact = impact;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    @Exclude
    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ein", ein);
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

    @Exclude
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CompanyEntry.COLUMN_EIN, ein);
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

    @Exclude
    public void fromContentValues(ContentValues values) {
        this.ein = values.getAsString(DatabaseContract.CompanyEntry.COLUMN_EIN);
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
}
