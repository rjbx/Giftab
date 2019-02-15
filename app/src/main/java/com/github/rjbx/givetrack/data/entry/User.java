package com.github.rjbx.givetrack.data.entry;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interfaces with {@link com.google.firebase.auth.FirebaseUser} through object relational mapping.
 */
@IgnoreExtraProperties public class User {

    private String uid;
    private String email;
    private String birthdate;
    private String gender;
    private int theme;
    private String donation;
    private String magnitude;
    private List<String> charities;
    private String term;
    private String city;
    private String state;
    private String zip;
    private String minrating;
    private boolean filter;
    private String searchSort;
    private String searchOrder;
    private String recordSort;
    private String recordOrder;
    private String pages;
    private String rows;
    private boolean focus;
    private String ein;
    private boolean viewtrack;
    private List<String> records;
    private boolean historical;
    private long anchor;
    private long timetrack;

    /**
     * Provides default constructor required for object relational mapping.
     */
    public User() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    public User(
            String uid,
            String email,
            String birthdate,
            String gender,
            int theme,
            String donation,
            String magnitude,
            List<String> charities,
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
            String ein,
            boolean viewtrack,
            List<String> records,
            boolean historical,
            long anchor,
            long timetrack) {
        this.uid = uid;
        this.email = email;
        this.birthdate = birthdate;
        this.gender = gender;
        this.theme = theme;
        this.magnitude = magnitude;
        this.donation = donation;
        this.charities = charities;
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
        this.ein = ein;
        this.viewtrack = viewtrack;
        this.records = records;
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
    public List<String> getCharities() { return charities; }
    public void setCharities(List<String> charities) {
        this.charities = charities;
    }
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
    public String getEin() {
        return ein;
    }
    public void setEin(String ein) {
        this.ein = ein;
    }
    public boolean getViewtrack() {
        return viewtrack;
    }
    public void setViewtrack(boolean viewtrack) {
        this.viewtrack = viewtrack;
    }
    public List<String> getRecords() { return records; }
    public void setRecords(List<String> records) { this.records = records; }
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
        map.put("email", email);
        map.put("birthdate", birthdate);
        map.put("gender", gender);
        map.put("theme", theme);
        map.put("magnitude", magnitude);
        map.put("donation", donation);
        map.put("charities", charities);
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
        map.put("ein", ein);
        map.put("viewtrack", viewtrack);
        map.put("records", records);
        map.put("historical", historical);
        map.put("anchor", anchor);
        map.put("timetrack", timetrack);
        return map;
    }
}
