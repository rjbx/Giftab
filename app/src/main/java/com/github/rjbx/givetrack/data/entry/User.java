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
@IgnoreExtraProperties public class User implements Entry, Parcelable, Cloneable {

    private String uid;
    private String email;
    private boolean active;
    private String birthdate;
    private String gender;
    private String donation;
    private String magnitude;
    private long anchor;
    private boolean historical;
    private long timetrack;
    private boolean viewtrack;
    private int theme;
    private boolean searchguide;
    private boolean focus;
    private boolean filter;
    private String company;
    private String term;
    private String city;
    private String state;
    private String zip;
    private String minrating;
    private String pages;
    private String rows;
    private boolean ratingReset;
    private String searchSort;
    private String searchOrder;
    private String recordSort;
    private String recordOrder;
    private long timeGiving;
    private long timeRecord;
    private long timeUser;

    @Exclude public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override public User createFromParcel(Parcel source) { return new User(source); }
        @Override public User[] newArray(int size) { return new User[size]; }
    };

    @Exclude @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeInt(active ? 1 : 0);
        dest.writeString(birthdate);
        dest.writeString(gender);
        dest.writeString(donation);
        dest.writeString(magnitude);
        dest.writeLong(anchor);
        dest.writeInt(historical ? 1 : 0);
        dest.writeLong(timetrack);
        dest.writeInt(viewtrack ? 1 : 0);
        dest.writeInt(theme);
        dest.writeInt(searchguide ? 1 : 0);
        dest.writeInt(focus ? 1 : 0);
        dest.writeInt(filter ? 1 : 0);
        dest.writeString(company);
        dest.writeString(term);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(zip);
        dest.writeString(minrating);
        dest.writeString(pages);
        dest.writeString(rows);
        dest.writeInt(ratingReset ? 1 : 0);
        dest.writeString(searchSort);
        dest.writeString(searchOrder);
        dest.writeString(recordSort);
        dest.writeString(recordOrder);
        dest.writeLong(timeGiving);
        dest.writeLong(timeRecord);
        dest.writeLong(timeUser);
    }

    @Exclude @Override public int describeContents() {
        return 0;
    }

    private User (Parcel source) {
        uid = source.readString();
        email = source.readString();
        active = source.readInt() == 1;
        birthdate = source.readString();
        gender = source.readString();
        donation = source.readString();
        magnitude = source.readString();
        anchor = source.readLong();
        historical = source.readInt() == 1;
        timetrack = source.readLong();
        viewtrack = source.readInt() == 1;
        theme = source.readInt();
        searchguide = source.readInt() == 1;
        focus = source.readInt() == 1;
        filter = source.readInt() == 1;
        company = source.readString();
        term = source.readString();
        city = source.readString();
        state = source.readString();
        zip = source.readString();
        minrating = source.readString();
        pages = source.readString();
        rows = source.readString();
        ratingReset = source.readInt() == 1;
        searchSort = source.readString();
        searchOrder = source.readString();
        recordSort = source.readString();
        recordOrder = source.readString();
        timeGiving = source.readLong();
        timeRecord = source.readLong();
        timeUser = source.readLong();
    }
    
    private User(User user) {
        this.uid = user.uid;
        this.email = user.email;
        this.active = user.active;
        this.birthdate = user.birthdate;
        this.gender = user.gender;
        this.donation = user.donation;
        this.magnitude = user.magnitude;
        this.anchor = user.anchor;
        this.historical = user.historical;
        this.timetrack = user.timetrack;
        this.viewtrack = user.viewtrack;
        this.theme = user.theme;
        this.searchguide = user.searchguide;
        this.focus = user.focus;
        this.filter = user.filter;
        this.company = user.company;
        this.term = user.term;
        this.city = user.city;
        this.state = user.state;
        this.zip = user.zip;
        this.minrating = user.minrating;
        this.pages = user.pages;
        this.rows = user.rows;
        this.ratingReset = user.ratingReset;
        this.searchSort = user.searchSort;
        this.searchOrder = user.searchOrder;
        this.recordSort = user.recordSort;
        this.recordOrder = user.recordOrder;
        this.timeGiving = user.timeGiving;
        this.timeRecord = user.timeRecord;
        this.timeUser = user.timeUser;
    }

    /**
     * Provides default constructor required for object relational mapping.
     */
    public User() { }

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public User(
            String uid,
            String email,
            boolean active,
            String birthdate,
            String gender,
            long anchor,
            boolean historical,
            long timetrack,
            boolean viewtrack,
            String donation,
            String magnitude,
            int theme,
            boolean searchguide,
            boolean focus,
            boolean filter,
            String company,
            String term,
            String city,
            String state,
            String zip,
            String minrating,
            String pages,
            String rows,
            boolean ratingReset,
            String searchSort,
            String searchOrder,
            String recordSort,
            String recordOrder,
            long timeGiving,
            long timeRecord,
            long timeUser) {
        this.uid = uid;
        this.email = email;
        this.active = active;
        this.birthdate = birthdate;
        this.gender = gender;
        this.donation = donation;
        this.magnitude = magnitude;
        this.anchor = anchor;
        this.historical = historical;
        this.timetrack = timetrack;
        this.viewtrack = viewtrack;
        this.theme = theme;
        this.searchguide = searchguide;
        this.focus = focus;
        this.filter = filter;
        this.company = company;
        this.term = term;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.minrating = minrating;
        this.pages = pages;
        this.rows = rows;
        this.ratingReset = ratingReset;
        this.searchSort = searchSort;
        this.searchOrder = searchOrder;
        this.recordSort = recordSort;
        this.recordOrder = recordOrder;
        this.timeGiving = timeGiving;
        this.timeRecord = timeRecord;
        this.timeUser = timeUser;
    }

    @Exclude public String getUid() { return uid; }
    @Exclude public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean getActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthday) { this.birthdate = birthday; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDonation() { return donation; }
    public void setDonation(String donation) { this.donation = donation; }
    public String getMagnitude() { return magnitude; }
    public void setMagnitude(String magnitude) { this.magnitude = magnitude; }
    public long getAnchor() { return anchor; }
    public void setAnchor(long anchor) { this.anchor = anchor; }
    public boolean getHistorical() { return historical; }
    public void setHistorical(boolean historical) { this.historical = historical; }
    public long getTimetrack() { return timetrack; }
    public void setTimetrack(long timetrack) { this.timetrack = timetrack; }
    public boolean getViewtrack() { return viewtrack; }
    public void setViewtrack(boolean viewtrack) { this.viewtrack = viewtrack; }
    public int getTheme() { return theme; }
    public void setTheme(int theme) { this.theme = theme; }
    public boolean getSearchguide() { return searchguide; }
    public void setSearchguide(boolean searchguide) { this.searchguide = searchguide; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public boolean getFocus() { return focus; }
    public void setFocus(boolean focus) { this.focus = focus; }
    public boolean getFilter() { return filter; }
    public void setFilter(boolean filter) { this.filter = filter; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public String getMinrating() { return minrating; }
    public void setMinrating(String minrating) { this.minrating = minrating; }
    public String getPages() { return pages; }
    public void setPages(String pages) { this.pages = pages; }
    public String getRows() { return rows; }
    public void setRows(String rows){ this.rows = rows; }
    public boolean getRatingReset() { return ratingReset; }
    public void setRatingReset(boolean ratingReset) { this.ratingReset = ratingReset; }
    public String getSearchSort() { return searchSort; }
    public void setSearchSort(String searchSort) { this.searchSort = searchSort; }
    public String getSearchOrder() { return searchOrder; }
    public void setSearchOrder(String searchOrder) { this.searchOrder = searchOrder; }
    public String getRecordSort() { return recordSort; }
    public void setRecordSort(String recordSort) { this.recordSort = recordSort; }
    public String getRecordOrder() { return recordOrder; }
    public void setRecordOrder(String recordOrder) { this.recordOrder = recordOrder; }
    public long getTimeGiving() { return timeGiving; }
    public void setTimeGiving(long timeGiving) { this.timeGiving = timeGiving; }
    public long getTimeRecord() { return timeRecord; }
    public void setTimeRecord(long timeRecord) { this.timeRecord = timeRecord; }
    public long getTimeUser() { return timeUser; }
    public void setTimeUser(long timeUser) { this.timeUser = timeUser; }
    @Exclude @Override public String getId() { return uid; }
    @Exclude public User getObject() { return this; }

    @Exclude public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("active", active);
        map.put("email", email);
        map.put("birthdate", birthdate);
        map.put("gender", gender);
        map.put("donation", donation);
        map.put("magnitude", magnitude);
        map.put("anchor", anchor);
        map.put("historical", historical);
        map.put("timetrack", timetrack);
        map.put("viewtrack", viewtrack);
        map.put("theme", theme);
        map.put("searchguide", searchguide);
        map.put("focus", focus);
        map.put("filter", filter);
        map.put("company", company);
        map.put("term", term);
        map.put("city", city);
        map.put("state", state);
        map.put("zip", zip);
        map.put("minrating", minrating);
        map.put("pages", pages);
        map.put("rows", rows);
        map.put("ratingReset", ratingReset);
        map.put("searchSort", searchSort);
        map.put("searchOrder", searchOrder);
        map.put("recordSort", recordSort);
        map.put("recordOrder", recordOrder);
        map.put("timeGiving", timeGiving);
        map.put("timeRecord", timeRecord);
        map.put("timeUser", timeUser);
        return map;
    }

    @Exclude public void fromParameterMap(Map<String, Object> map) {
        uid = (String) map.get("uid");
        email = (String) map.get("email");
        active = (boolean) map.get("active");
        birthdate = (String) map.get("birthdate");
        gender = (String) map.get("gender");
        anchor = (long) map.get("anchor");
        historical = (boolean) map.get("historical");
        timetrack = (long) map.get("timetrack");
        viewtrack = (boolean) map.get("viewtrack");
        theme = (int) map.get("theme");
        searchguide = (boolean) map.get("searchguide");
        focus = (boolean) map.get("focus");
        filter = (boolean) map.get("filter");
        company = (String) map.get("company");
        magnitude = (String) map.get("magnitude");
        donation = (String) map.get("donation");
        term = (String) map.get("term");
        city = (String) map.get("city");
        state = (String) map.get("state");
        zip = (String) map.get("zip");
        minrating = (String) map.get("minrating");
        pages = (String) map.get("pages");
        rows = (String) map.get("rows");
        ratingReset = (boolean) map.get("ratingReset");
        searchSort = (String) map.get("searchSort");
        searchOrder = (String) map.get("searchOrder");
        recordSort = (String) map.get("recordSort");
        recordOrder = (String) map.get("recordOrder");
        timeGiving = (long) map.get("timeGiving");
        timeRecord = (long) map.get("timeRecord");
        timeUser = (long) map.get("timeUser");
    }

    @Exclude @Override public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_ACTIVE, active);
        values.put(COLUMN_BIRTHDATE, birthdate);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_ANCHOR, anchor);
        values.put(COLUMN_HISTORICAL, historical);
        values.put(COLUMN_TIMETRACK, timetrack);
        values.put(COLUMN_VIEWTRACK, viewtrack);
        values.put(COLUMN_THEME, theme);
        values.put(COLUMN_SEARCHGUIDE, searchguide);
        values.put(COLUMN_FOCUS, focus);
        values.put(COLUMN_FILTER, filter);
        values.put(COLUMN_COMPANY, company);
        values.put(COLUMN_MAGNITUDE, magnitude);
        values.put(COLUMN_DONATION, donation);
        values.put(COLUMN_TERM, term);
        values.put(COLUMN_CITY, city);
        values.put(COLUMN_STATE,state);
        values.put(COLUMN_ZIP, zip);
        values.put(COLUMN_MINRATING, minrating);
        values.put(COLUMN_PAGES, pages);
        values.put(COLUMN_ROWS, rows);
        values.put(COLUMN_RATINGRESET, ratingReset);
        values.put(COLUMN_SEARCHSORT, searchSort);
        values.put(COLUMN_SEARCHORDER, searchOrder);
        values.put(COLUMN_RECORDSORT, recordSort);
        values.put(COLUMN_RECORDORDER, recordOrder);
        values.put(COLUMN_TIMEGIVING, timeGiving);
        values.put(COLUMN_TIMERECORD, timeRecord);
        values.put(COLUMN_TIMEUSER, timeUser);
        return values;
    }

    @Exclude @Override public void fromContentValues(ContentValues values) {
        this.uid = values.getAsString(COLUMN_UID);
        this.email = values.getAsString(COLUMN_EMAIL);
        this.active = values.getAsInteger(COLUMN_ACTIVE) == 1;
        this.birthdate = values.getAsString(COLUMN_BIRTHDATE);
        this.gender = values.getAsString(COLUMN_GENDER);
        this.donation = values.getAsString(COLUMN_DONATION);
        this.magnitude = values.getAsString(COLUMN_MAGNITUDE);
        this.anchor = values.getAsInteger(COLUMN_ANCHOR);
        this.historical = values.getAsInteger(COLUMN_HISTORICAL) == 1;
        this.timetrack = values.getAsInteger(COLUMN_TIMETRACK);
        this.viewtrack = values.getAsInteger(COLUMN_VIEWTRACK) == 1;
        this.theme = values.getAsInteger(COLUMN_THEME);
        this.searchguide = values.getAsInteger(COLUMN_SEARCHGUIDE) == 1;
        this.focus = values.getAsInteger(COLUMN_FOCUS) == 1;
        this.filter = values.getAsInteger(COLUMN_FILTER) == 1;
        this.company = values.getAsString(COLUMN_COMPANY);
        this.term = values.getAsString(COLUMN_TERM);
        this.city = values.getAsString(COLUMN_CITY);
        this.state = values.getAsString(COLUMN_STATE);
        this.zip = values.getAsString(COLUMN_ZIP);
        this.minrating = values.getAsString(COLUMN_MINRATING);
        this.pages = values.getAsString(COLUMN_PAGES);
        this.rows = values.getAsString(COLUMN_ROWS);
        this.ratingReset = values.getAsBoolean(COLUMN_RATINGRESET);
        this.searchSort = values.getAsString(COLUMN_SEARCHSORT);
        this.searchOrder = values.getAsString(COLUMN_SEARCHORDER);
        this.recordSort = values.getAsString(COLUMN_RECORDSORT);
        this.recordOrder = values.getAsString(COLUMN_RECORDORDER);
        this.timeGiving = values.getAsLong(COLUMN_TIMEGIVING);
        this.timeRecord = values.getAsLong(COLUMN_TIMERECORD);
        this.timeUser = values.getAsLong(COLUMN_TIMEUSER);
    }

    @Exclude @Override public User clone() {
        User clone  = new User(this);
        try { super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Class must implement Cloneable interface");
        } return clone;
    }
    
    @Exclude public static User getDefault() {
        User user = new User();
        user.uid = "";
        user.email = "";
        user.active = true;
        user.birthdate = "";
        user.gender = "";
        user.donation = "0";
        user.magnitude = "0.01";
        user.anchor = 0;
        user.historical = false;
        user.timetrack = 0;
        user.viewtrack = false;
        user.theme = 0;
        user.searchguide = false;
        user.focus = false;
        user.filter = true;
        user.company = "";
        user.term = "";
        user.city = "";
        user.state = "";
        user.zip = "";
        user.minrating = "";
        user.pages = "";
        user.rows = "";
        user.ratingReset = false;
        user.searchSort = "RATING";
        user.searchOrder = "DESC";
        user.recordSort = "donationTime";
        user.recordOrder = "DESC";
        user.timeGiving = 0;
        user.timeRecord = 0;
        user.timeUser = 0;
        return user;
    }
}
