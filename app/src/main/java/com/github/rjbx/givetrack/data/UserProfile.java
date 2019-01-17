package com.github.rjbx.givetrack.data;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interfaces with {@link com.google.firebase.auth.FirebaseUser} through object relational mapping.
 */
@IgnoreExtraProperties
public class UserProfile {

    private String uid;
    private String email;
    private String birthdate;
    private String gender;
    private int theme;
    private String tracked;
    private String donation;
    private String magnitude;
    private List<String> charities;
    private String term;
    private String city;
    private String state;
    private String zip;
    private String minrating;
    private boolean filter;
    private String sort;
    private String order;
    private String pages;
    private String rows;
    private boolean focus;
    private String ein;
    private String weeks;
    private String months;
    private String years;
    private String high;
    private String today;
    private long timestamp;
    private long timetrack;

    /**
     * Provides default constructor required for object relational mapping.
     */
    public UserProfile() {}

    /**
     * Provides POJO constructor required for object relational mapping.
     */
    UserProfile(
            String uid,
            String email,
            String birthdate,
            String gender,
            int theme,
            String tracked,
            String donation,
            String magnitude,
            List<String> charities,
            String term,
            String city,
            String state,
            String zip,
            String minrating,
            boolean filter,
            String sort,
            String order,
            String pages,
            String rows,
            boolean focus,
            String ein,
            String weeks,
            String months,
            String years,
            String high,
            String today,
            long timestamp,
            long timetrack) {
        this.uid = uid;
        this.email = email;
        this.birthdate = birthdate;
        this.gender = gender;
        this.theme = theme;
        this.tracked = tracked;
        this.magnitude = magnitude;
        this.donation = donation;
        this.charities = charities;
        this.term = term;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.minrating = minrating;
        this.filter = filter;
        this.sort = sort;
        this.order = order;
        this.pages = pages;
        this.rows = rows;
        this.focus = focus;
        this.ein = ein;
        this.weeks = weeks;
        this.months = months;
        this.years = years;
        this.high = high;
        this.today = today;
        this.timestamp = timestamp;
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
    void setTheme(int theme) { this.theme = theme; }
    public String getTracked() { return tracked; }
    public void setTracked(String tracked) { this.tracked = tracked; }
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
    public String getSort() {
        return sort;
    }
    public void setSort(String sort) {
        this.sort = sort;
    }
    public String getOrder() {
        return order;
    }
    public void setOrder(String order) {
        this.order = order;
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
    public String getWeeks() {
        return weeks;
    }
    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }
    public String getMonths() {
        return months;
    }
    public void setMonths(String months) {
        this.months = months;
    }
    public String getYears() {
        return years;
    }
    public void setYears(String years) {
        this.years = years;
    }
    public String getHigh() { return high; }
    public void setHigh(String high) { this.high = high; }
    public String getToday() { return today; }
    public void setToday(String today) { this.today = today; }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public long getTimetrack() { return timetrack; }
    public void setTimetrack(long timetrack) {
        this.timetrack = timetrack;
    }

    @Exclude
    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("birthdate", birthdate);
        map.put("gender", gender);
        map.put("theme", theme);
        map.put("tracked", tracked);
        map.put("magnitude", magnitude);
        map.put("donation", donation);
        map.put("charities", charities);
        map.put("term", term);
        map.put("city", city);
        map.put("state", state);
        map.put("zip", zip);
        map.put("minrating", minrating);
        map.put("filter", filter);
        map.put("sort", sort);
        map.put("order", order);
        map.put("pages", pages);
        map.put("rows", rows);
        map.put("focus", focus);
        map.put("ein", ein);
        map.put("weeks", weeks);
        map.put("months", months);
        map.put("years", weeks);
        map.put("high", high);
        map.put("today", today);
        map.put("timestamp", timestamp);
        map.put("timetrack", timetrack);
        return map;
    }
}
