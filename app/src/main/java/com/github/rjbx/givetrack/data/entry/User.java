package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static com.github.rjbx.givetrack.data.DatabaseContract.UserEntry.*;

/**
 * Interfaces with {@link com.google.firebase.auth.FirebaseUser} through object relational mapping.
 */
@IgnoreExtraProperties public class User implements Entry, Parcelable {

    private String uid = "";
    private boolean active = false;
    private String email = "";
    private String birthdate = "";
    private String gender = "";
    private int theme = 0;
    private String donation = "";
    private String magnitude = "";
    private String term = "";
    private String city = "";
    private String state = "";
    private String zip = "";
    private String minrating = "";
    private boolean filter = false;
    private String searchSort = "";
    private String searchOrder = "";
    private String recordSort = "";
    private String recordOrder = "";
    private String pages = "";
    private String rows = "";
    private boolean focus = false;
    private String company = "";
    private boolean viewtrack = false;
    private boolean searchguide = false;
    private boolean historical = false;
    private long anchor = 0;
    private long timetrack = 0;

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override public User createFromParcel(Parcel source) { return new User(source); }
        @Override public User[] newArray(int size) { return new User[size]; }
    };

    User (Parcel source) {
         uid = source.readString();
         active = source.readInt() == 1;
         email = source.readString();
         birthdate = source.readString();
         gender = source.readString();
         theme = source.readInt();
         donation = source.readString();
         magnitude = source.readString();
         term = source.readString();
         city = source.readString();
         state = source.readString();
         zip = source.readString();
         minrating = source.readString();
         filter = source.readInt() == 1;
         searchSort = source.readString();
         searchOrder = source.readString();
         recordSort = source.readString();
         recordOrder = source.readString();
         pages = source.readString();
         rows = source.readString();
         focus = source.readInt() == 1;
         company = source.readString();
         viewtrack = source.readInt() == 1;
         searchguide = source.readInt() == 1;
         historical = source.readInt() == 1;
         anchor = source.readLong();
         timetrack = source.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(uid);
         dest.writeInt(active ? 0 : 1);
         dest.writeString(email);
         dest.writeString(birthdate);
         dest.writeString(gender);
         dest.writeInt(theme);
         dest.writeString(donation);
         dest.writeString(magnitude);
         dest.writeString(term);
         dest.writeString(city);
         dest.writeString(state);
         dest.writeString(zip);
         dest.writeString(minrating);
         dest.writeInt(filter ? 0 : 1);
         dest.writeString(searchSort);
         dest.writeString(searchOrder);
         dest.writeString(recordSort);
         dest.writeString(recordOrder);
         dest.writeString(pages);
         dest.writeString(rows);
         dest.writeInt(focus ? 0 : 1);
         dest.writeString(company);
         dest.writeInt(viewtrack ? 0 : 1);
         dest.writeInt(searchguide ? 0 : 1);
         dest.writeInt(historical ? 0 : 1);
         dest.writeLong(anchor);
         dest.writeLong(timetrack);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public User() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public User(
            String uid,
            boolean active,
            String email,
            String birthdate,
            String gender,
            int theme,
            String donation,
            String magnitude,
            String term,
            String city,
            String state,
            String zip,
            String minrating,
            boolean filter,
            String searchSort,
            String searchOrder,
            String recordSort,
            String recordOrder,
            String pages,
            String rows,
            boolean focus,
            String company,
            boolean viewtrack,
            boolean searchguide,
            boolean historical,
            long anchor,
            long timetrack) {
        this.uid = uid;
        this.active = active;
        this.email = email;
        this.birthdate = birthdate;
        this.gender = gender;
        this.theme = theme;
        this.magnitude = magnitude;
        this.donation = donation;
        this.term = term;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.minrating = minrating;
        this.filter = filter;
        this.searchSort = searchSort;
        this.searchOrder = searchOrder;
        this.recordSort = recordSort;
        this.recordOrder = recordOrder;
        this.pages = pages;
        this.rows = rows;
        this.focus = focus;
        this.company = company;
        this.viewtrack = viewtrack;
        this.searchguide = searchguide;
        this.historical = historical;
        this.anchor = anchor;
        this.timetrack = timetrack;
    }

    @Exclude public String getUid() {
        return uid;
    }
    @Exclude public void setUid(String uid) {
        this.uid = uid;
    }
    public boolean getActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(String birthday) {
        this.birthdate = birthday;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) { this.gender = gender; }
    public int getTheme() { return theme; }
    public void setTheme(int theme) { this.theme = theme; }
    public String getMagnitude() { return magnitude; }
    public void setMagnitude(String magnitude) { this.magnitude = magnitude; }
    public String getDonation() { return donation; }
    public void setDonation(String donation) { this.donation = donation; }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }
    public String getMinrating() {
        return minrating;
    }
    public void setMinrating(String minrating) {
        this.minrating = minrating;
    }
    public boolean getFilter() {
        return filter;
    }
    public void setFilter(boolean filter) {
        this.filter = filter;
    }
    public String getSearchSort() {
        return searchSort;
    }
    public void setSearchSort(String searchSort) {
        this.searchSort = searchSort;
    }
    public String getSearchOrder() {
        return searchOrder;
    }
    public void setSearchOrder(String searchOrder) {
        this.searchOrder = searchOrder;
    }
    public String getRecordSort() {
        return recordSort;
    }
    public void setRecordSort(String recordSort) {
        this.recordSort = recordSort;
    }
    public String getRecordOrder() {
        return recordOrder;
    }
    public void setRecordOrder(String recordOrder) {
        this.recordOrder = recordOrder;
    }
    public String getPages() {
        return pages;
    }
    public void setPages(String pages) {
        this.pages = pages;
    }
    public String getRows() {
        return rows;
    }
    public void setRows(String rows) {
        this.rows = rows;
    }
    public boolean getFocus() {
        return focus;
    }
    public void setFocus(boolean focus) {
        this.focus = focus;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public boolean getViewtrack() {
        return viewtrack;
    }
    public void setViewtrack(boolean viewtrack) {
        this.viewtrack = viewtrack;
    }
    public boolean getSearchguide() { return searchguide; }
    public void setSearchguide(boolean searchguide) { this.searchguide = searchguide; }
    public boolean getHistorical() {
        return historical;
    }
    public void setHistorical(boolean historical) {
        this.historical = historical;
    }
    public long getAnchor() {
        return anchor;
    }
    public void setAnchor(long anchor) {
        this.anchor = anchor;
    }
    public long getTimetrack() { return timetrack; }
    public void setTimetrack(long timetrack) {
        this.timetrack = timetrack;
    }

    @Exclude public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("active", active);
        map.put("email", email);
        map.put("birthdate", birthdate);
        map.put("gender", gender);
        map.put("theme", theme);
        map.put("magnitude", magnitude);
        map.put("donation", donation);
        map.put("term", term);
        map.put("city", city);
        map.put("state", state);
        map.put("zip", zip);
        map.put("minrating", minrating);
        map.put("filter", filter);
        map.put("searchSort", searchSort);
        map.put("searchOrder", searchOrder);
        map.put("recordSort", recordSort);
        map.put("recordOrder", recordOrder);
        map.put("pages", pages);
        map.put("rows", rows);
        map.put("focus", focus);
        map.put("company", company);
        map.put("viewtrack", viewtrack);
        map.put("searchguide", searchguide);
        map.put("historical", historical);
        map.put("anchor", anchor);
        map.put("timetrack", timetrack);
        return map;
    }
    
    @Exclude public void fromParameterMap(Map<String, Object> map) {
        uid = (String) map.get("uid");
        active = (boolean) map.get("active");
        email = (String) map.get("email");
        birthdate = (String) map.get("birthdate");
        gender = (String) map.get("gender");
        theme = (int) map.get("theme");
        magnitude = (String) map.get("magnitude");
        donation = (String) map.get("donation");
        term = (String) map.get("term");
        city = (String) map.get("city");
        state = (String) map.get("state");
        zip = (String) map.get("zip");
        minrating = (String) map.get("minrating");
        filter = (boolean) map.get("filter");
        searchSort = (String) map.get("searchSort");
        searchOrder = (String) map.get("searchOrder");
        recordSort = (String) map.get("recordSort");
        recordOrder = (String) map.get("recordOrder");
        pages = (String) map.get("pages");
        rows = (String) map.get("rows");
        focus = (boolean) map.get("focus");
        company = (String) map.get("company");
        viewtrack = (boolean) map.get("viewtrack");
        searchguide = (boolean) map.get("searchguide");
        historical = (boolean) map.get("historical");
        anchor = (long) map.get("anchor");
        timetrack = (long) map.get("timetrack");
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_ACTIVE, active);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_BIRTHDATE, birthdate);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_THEME, theme);
        values.put(COLUMN_MAGNITUDE, magnitude);
        values.put(COLUMN_DONATION, donation);
        values.put(COLUMN_TERM, term);
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_STATE,state);
        values.put(COLUMN_ZIP, zip);
        values.put(COLUMN_MINRATING, minrating);
        values.put(COLUMN_FILTER, filter);
        values.put(COLUMN_SEARCHSORT, searchSort);
        values.put(COLUMN_SEARCHORDER, searchOrder);
        values.put(COLUMN_RECORDSORT, recordSort);
        values.put(COLUMN_RECORDORDER, recordOrder);
        values.put(COLUMN_PAGES, pages);
        values.put(COLUMN_ROWS, rows);
        values.put(COLUMN_FOCUS, focus);
        values.put(COLUMN_COMPANY, company);
        values.put(COLUMN_VIEWTRACK, viewtrack);
        values.put(COLUMN_SEARCHGUIDE, searchguide);
        values.put(COLUMN_HISTORICAL, historical);
        values.put(COLUMN_ANCHOR, anchor);
        values.put(COLUMN_TIMETRACK, timetrack);
        return values;
    }

    @Override
    public void fromContentValues(ContentValues values) {
        this.uid = values.getAsString(COLUMN_UID);
        this.active = values.getAsInteger(COLUMN_ACTIVE) == 1;
        this.email = values.getAsString(COLUMN_EMAIL);
        this.birthdate = values.getAsString(COLUMN_BIRTHDATE);
        this.gender = values.getAsString(COLUMN_GENDER);
        this.theme = values.getAsInteger(COLUMN_THEME);
        this.magnitude = values.getAsString(COLUMN_MAGNITUDE);
        this.donation = values.getAsString(COLUMN_DONATION);
        this.term = values.getAsString(COLUMN_TERM);
        this.city = values.getAsString(COLUMN_CITY);
        this.state = values.getAsString(COLUMN_STATE);
        this.zip = values.getAsString(COLUMN_ZIP);
        this.minrating = values.getAsString(COLUMN_MINRATING);
        this.filter = values.getAsInteger(COLUMN_FILTER) == 1;
        this.searchSort = values.getAsString(COLUMN_SEARCHSORT);
        this.searchOrder = values.getAsString(COLUMN_SEARCHORDER);
        this.recordSort = values.getAsString(COLUMN_RECORDSORT);
        this.recordOrder = values.getAsString(COLUMN_RECORDORDER);
        this.pages = values.getAsString(COLUMN_PAGES);
        this.rows = values.getAsString(COLUMN_ROWS);
        this.focus = values.getAsInteger(COLUMN_FOCUS) == 1;
        this.company = values.getAsString(COLUMN_COMPANY);
        this.viewtrack = values.getAsInteger(COLUMN_VIEWTRACK) == 1;
        this.searchguide = values.getAsInteger(COLUMN_SEARCHGUIDE) == 1;
        this.historical = values.getAsInteger(COLUMN_HISTORICAL) == 1;
        this.anchor = values.getAsInteger(COLUMN_ANCHOR);
        this.timetrack = values.getAsInteger(COLUMN_TIMETRACK);
    }
}
