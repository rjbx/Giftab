package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static com.github.rjbx.givetrack.data.DatabaseContract.UserEntry.*;

// TODO: Convert attributes to same types as preference values
/**
 * Interfaces with {@link com.google.firebase.auth.FirebaseUser} through object relational mapping.
 */
@IgnoreExtraProperties public class User implements Entry, Parcelable, Cloneable {

    private String uid;
    private String userEmail;
    private boolean userActive;
    private String userBirthdate;
    private String userGender;
    private String giveImpact;
    private String giveMagnitude;
    private long giveAnchor;
    private int giveTiming;
    private long glanceAnchor;
    private boolean glanceSince;
    private int glanceTheme;
    private boolean searchDialog;
    private boolean searchFocus;
    private boolean searchFilter;
    private String searchCompany;
    private String searchTerm;
    private String searchCity;
    private String searchState;
    private String searchZip;
    private String searchMinrating;
    private String searchPages;
    private String searchRows;
    private boolean giveReset;
    private String searchSort;
    private String searchOrder;
    private String recordSort;
    private String recordOrder;
    private long giveStamp;
    private long recordStamp;
    private long userStamp;

    @Exclude public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override public User createFromParcel(Parcel source) { return new User(source); }
        @Override public User[] newArray(int size) { return new User[size]; }
    };

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(userEmail);
        dest.writeInt(userActive ? 1 : 0);
        dest.writeString(userBirthdate);
        dest.writeString(userGender);
        dest.writeString(giveImpact);
        dest.writeString(giveMagnitude);
        dest.writeLong(giveAnchor);
        dest.writeInt(giveTiming);
        dest.writeLong(glanceAnchor);
        dest.writeInt(glanceSince ? 1 : 0);
        dest.writeInt(glanceTheme);
        dest.writeInt(searchDialog ? 1 : 0);
        dest.writeInt(searchFocus ? 1 : 0);
        dest.writeInt(searchFilter ? 1 : 0);
        dest.writeString(searchCompany);
        dest.writeString(searchTerm);
        dest.writeString(searchCity);
        dest.writeString(searchState);
        dest.writeString(searchZip);
        dest.writeString(searchMinrating);
        dest.writeString(searchPages);
        dest.writeString(searchRows);
        dest.writeInt(giveReset ? 1 : 0);
        dest.writeString(searchSort);
        dest.writeString(searchOrder);
        dest.writeString(recordSort);
        dest.writeString(recordOrder);
        dest.writeLong(giveStamp);
        dest.writeLong(recordStamp);
        dest.writeLong(userStamp);
    }

    @Override public int describeContents() { return 0; }

    public User (Parcel source) {
        uid = source.readString();
        userEmail = source.readString();
        userActive = source.readInt() == 1;
        userBirthdate = source.readString();
        userGender = source.readString();
        giveImpact = source.readString();
        giveMagnitude = source.readString();
        giveAnchor = source.readLong();
        giveTiming = source.readInt();
        glanceAnchor = source.readLong();
        glanceSince = source.readInt() == 1;
        glanceTheme = source.readInt();
        searchDialog = source.readInt() == 1;
        searchFocus = source.readInt() == 1;
        searchFilter = source.readInt() == 1;
        searchCompany = source.readString();
        searchTerm = source.readString();
        searchCity = source.readString();
        searchState = source.readString();
        searchZip = source.readString();
        searchMinrating = source.readString();
        searchPages = source.readString();
        searchRows = source.readString();
        giveReset = source.readInt() == 1;
        searchSort = source.readString();
        searchOrder = source.readString();
        recordSort = source.readString();
        recordOrder = source.readString();
        giveStamp = source.readLong();
        recordStamp = source.readLong();
        userStamp = source.readLong();
    }
    
    public User(User user) {
        this.uid = user.uid;
        this.userEmail = user.userEmail;
        this.userActive = user.userActive;
        this.userBirthdate = user.userBirthdate;
        this.userGender = user.userGender;
        this.giveImpact = user.giveImpact;
        this.giveMagnitude = user.giveMagnitude;
        this.giveAnchor = user.giveAnchor;
        this.giveTiming = user.giveTiming;
        this.glanceAnchor = user.glanceAnchor;
        this.glanceSince = user.glanceSince;
        this.glanceTheme = user.glanceTheme;
        this.searchDialog = user.searchDialog;
        this.searchFocus = user.searchFocus;
        this.searchFilter = user.searchFilter;
        this.searchCompany = user.searchCompany;
        this.searchTerm = user.searchTerm;
        this.searchCity = user.searchCity;
        this.searchState = user.searchState;
        this.searchZip = user.searchZip;
        this.searchMinrating = user.searchMinrating;
        this.searchPages = user.searchPages;
        this.searchRows = user.searchRows;
        this.giveReset = user.giveReset;
        this.searchSort = user.searchSort;
        this.searchOrder = user.searchOrder;
        this.recordSort = user.recordSort;
        this.recordOrder = user.recordOrder;
        this.giveStamp = user.giveStamp;
        this.recordStamp = user.recordStamp;
        this.userStamp = user.userStamp;
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
            String userEmail,
            boolean userActive,
            String userBirthdate,
            String userGender,
            long giveAnchor,
            int giveTiming,
            long glanceAnchor,
            boolean glanceSince,
            String giveImpact,
            String giveMagnitude,
            int glanceTheme,
            boolean searchDialog,
            boolean searchFocus,
            boolean searchFilter,
            String searchCompany,
            String searchTerm,
            String searchCity,
            String searchState,
            String searchZip,
            String searchMinrating,
            String searchPages,
            String searchRows,
            boolean giveReset,
            String searchSort,
            String searchOrder,
            String recordSort,
            String recordOrder,
            long giveStamp,
            long recordStamp,
            long userStamp) {
        this.uid = uid;
        this.userEmail = userEmail;
        this.userActive = userActive;
        this.userBirthdate = userBirthdate;
        this.userGender = userGender;
        this.giveImpact = giveImpact;
        this.giveMagnitude = giveMagnitude;
        this.giveAnchor = giveAnchor;
        this.giveTiming = giveTiming;
        this.glanceAnchor = glanceAnchor;
        this.glanceSince = glanceSince;
        this.glanceTheme = glanceTheme;
        this.searchDialog = searchDialog;
        this.searchFocus = searchFocus;
        this.searchFilter = searchFilter;
        this.searchCompany = searchCompany;
        this.searchTerm = searchTerm;
        this.searchCity = searchCity;
        this.searchState = searchState;
        this.searchZip = searchZip;
        this.searchMinrating = searchMinrating;
        this.searchPages = searchPages;
        this.searchRows = searchRows;
        this.giveReset = giveReset;
        this.searchSort = searchSort;
        this.searchOrder = searchOrder;
        this.recordSort = recordSort;
        this.recordOrder = recordOrder;
        this.giveStamp = giveStamp;
        this.recordStamp = recordStamp;
        this.userStamp = userStamp;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public boolean getUserActive() { return userActive; }
    public void setUserActive(boolean userActive) { this.userActive = userActive; }
    public String getUserBirthdate() { return userBirthdate; }
    public void setUserBirthdate(String birthday) { this.userBirthdate = birthday; }
    public String getUserGender() { return userGender; }
    public void setUserGender(String userGender) { this.userGender = userGender; }
    public String getGiveImpact() { return giveImpact; }
    public void setGiveImpact(String giveImpact) { this.giveImpact = giveImpact; }
    public String getGiveMagnitude() { return giveMagnitude; }
    public void setGiveMagnitude(String giveMagnitude) { this.giveMagnitude = giveMagnitude; }
    public long getGiveAnchor() { return giveAnchor; }
    public void setGiveAnchor(long giveAnchor) { this.giveAnchor = giveAnchor; }
    public int getGiveTiming() { return giveTiming; }
    public void setGiveTiming(int giveTiming) { this.giveTiming = giveTiming; }
    public long getGlanceAnchor() { return glanceAnchor; }
    public void setGlanceAnchor(long glanceAnchor) { this.glanceAnchor = glanceAnchor; }
    public boolean getGlanceSince() { return glanceSince; }
    public void setGlanceSince(boolean glanceSince) { this.glanceSince = glanceSince; }
    public int getGlanceTheme() { return glanceTheme; }
    public void setGlanceTheme(int glanceTheme) { this.glanceTheme = glanceTheme; }
    public boolean getSearchDialog() { return searchDialog; }
    public void setSearchDialog(boolean searchDialog) { this.searchDialog = searchDialog; }
    public String getSearchCompany() { return searchCompany; }
    public void setSearchCompany(String searchCompany) { this.searchCompany = searchCompany; }
    public boolean getSearchFocus() { return searchFocus; }
    public void setSearchFocus(boolean searchFocus) { this.searchFocus = searchFocus; }
    public boolean getSearchFilter() { return searchFilter; }
    public void setSearchFilter(boolean searchFilter) { this.searchFilter = searchFilter; }
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    public String getSearchCity() { return searchCity; }
    public void setSearchCity(String searchCity) { this.searchCity = searchCity; }
    public String getSearchState() { return searchState; }
    public void setSearchState(String searchState) { this.searchState = searchState; }
    public String getSearchZip() { return searchZip; }
    public void setSearchZip(String searchZip) { this.searchZip = searchZip; }
    public String getSearchMinrating() { return searchMinrating; }
    public void setSearchMinrating(String searchMinrating) { this.searchMinrating = searchMinrating; }
    public String getSearchPages() { return searchPages; }
    public void setSearchPages(String searchPages) { this.searchPages = searchPages; }
    public String getSearchRows() { return searchRows; }
    public void setSearchRows(String searchRows){ this.searchRows = searchRows; }
    public boolean getGiveReset() { return giveReset; }
    public void setGiveReset(boolean giveReset) { this.giveReset = giveReset; }
    public String getSearchSort() { return searchSort; }
    public void setSearchSort(String searchSort) { this.searchSort = searchSort; }
    public String getSearchOrder() { return searchOrder; }
    public void setSearchOrder(String searchOrder) { this.searchOrder = searchOrder; }
    public String getRecordSort() { return recordSort; }
    public void setRecordSort(String recordSort) { this.recordSort = recordSort; }
    public String getRecordOrder() { return recordOrder; }
    public void setRecordOrder(String recordOrder) { this.recordOrder = recordOrder; }
    public long getGiveStamp() { return giveStamp; }
    public void setGiveStamp(long giveStamp) { this.giveStamp = giveStamp; }
    public long getRecordStamp() { return recordStamp; }
    public void setRecordStamp(long recordStamp) { this.recordStamp = recordStamp; }
    public long getUserStamp() { return userStamp; }
    public void setUserStamp(long userStamp) { this.userStamp = userStamp; }
    @Override public String getId() { return uid; }
    public User getObject() { return this; }

    public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(COLUMN_UID, uid);
        map.put(COLUMN_USER_EMAIL, userEmail);
        map.put(COLUMN_USER_ACTIVE, userActive);
        map.put(COLUMN_USER_BIRTHDATE, userBirthdate);
        map.put(COLUMN_USER_GENDER, userGender);
        map.put(COLUMN_GIVE_ANCHOR, giveAnchor);
        map.put(COLUMN_GIVE_TIMING, giveTiming);
        map.put(COLUMN_GLANCE_ANCHOR, glanceAnchor);
        map.put(COLUMN_GLANCE_SINCE, glanceSince);
        map.put(COLUMN_GLANCE_THEME, glanceTheme);
        map.put(COLUMN_SEARCH_DIALOG, searchDialog);
        map.put(COLUMN_SEARCH_FOCUS, searchFocus);
        map.put(COLUMN_SEARCH_FILTER, searchFilter);
        map.put(COLUMN_SEARCH_COMPANY, searchCompany);
        map.put(COLUMN_GIVE_MAGNITUDE, giveMagnitude);
        map.put(COLUMN_GIVE_IMPACT, giveImpact);
        map.put(COLUMN_SEARCH_TERM, searchTerm);
        map.put(COLUMN_SEARCH_CITY, searchCity);
        map.put(COLUMN_SEARCH_STATE, searchState);
        map.put(COLUMN_SEARCH_ZIP, searchZip);
        map.put(COLUMN_SEARCH_MINRATING, searchMinrating);
        map.put(COLUMN_SEARCH_PAGES, searchPages);
        map.put(COLUMN_SEARCH_ROWS, searchRows);
        map.put(COLUMN_GIVE_RESET, giveReset);
        map.put(COLUMN_SEARCH_SORT, searchSort);
        map.put(COLUMN_SEARCH_ORDER, searchOrder);
        map.put(COLUMN_RECORD_SORT, recordSort);
        map.put(COLUMN_RECORD_ORDER, recordOrder);
        map.put(COLUMN_GIVE_STAMP, giveStamp);
        map.put(COLUMN_RECORD_STAMP, recordStamp);
        map.put(COLUMN_USER_STAMP, userStamp);
        return map;
    }

    public void fromParameterMap(Map<String, Object> map) {
        uid = (String) map.get(COLUMN_UID);
        userEmail = (String) map.get(COLUMN_USER_EMAIL);
        userActive = (int) map.get(COLUMN_USER_ACTIVE) == 1;
        userBirthdate = (String) map.get(COLUMN_USER_BIRTHDATE);
        userGender = (String) map.get(COLUMN_USER_GENDER);
        giveImpact = (String) map.get(COLUMN_GIVE_IMPACT);
        giveMagnitude = (String) map.get(COLUMN_GIVE_MAGNITUDE);
        giveAnchor = (long) map.get(COLUMN_GIVE_ANCHOR);
        giveTiming = (int) map.get(COLUMN_GIVE_TIMING);
        glanceAnchor = (long) map.get(COLUMN_GLANCE_ANCHOR);
        glanceSince = (int) map.get(COLUMN_GLANCE_SINCE) == 1;
        glanceTheme = (int) map.get(COLUMN_GLANCE_THEME);
        searchDialog = (int) map.get(COLUMN_SEARCH_DIALOG) == 1;
        searchFocus = (int) map.get(COLUMN_SEARCH_FOCUS) == 1;
        searchFilter = (int) map.get(COLUMN_SEARCH_FILTER) == 1;
        searchCompany = (String) map.get(COLUMN_SEARCH_COMPANY);
        searchTerm = (String) map.get(COLUMN_SEARCH_TERM);
        searchCity = (String) map.get(COLUMN_SEARCH_CITY);
        searchState = (String) map.get(COLUMN_SEARCH_STATE);
        searchZip = (String) map.get(COLUMN_SEARCH_ZIP);
        searchMinrating = (String) map.get(COLUMN_SEARCH_MINRATING);
        searchPages = (String) map.get(COLUMN_SEARCH_PAGES);
        searchRows = (String) map.get(COLUMN_SEARCH_ROWS);
        giveReset = (boolean) map.get(COLUMN_GIVE_RESET);
        searchSort = (String) map.get(COLUMN_SEARCH_SORT);
        searchOrder = (String) map.get(COLUMN_SEARCH_ORDER);
        recordSort = (String) map.get(COLUMN_RECORD_SORT);
        recordOrder = (String) map.get(COLUMN_RECORD_ORDER);
        giveStamp = (long) map.get(COLUMN_GIVE_STAMP);
        recordStamp = (long) map.get(COLUMN_RECORD_STAMP);
        userStamp = (long) map.get(COLUMN_USER_STAMP);
    }

    @Override public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_USER_EMAIL, userEmail);
        values.put(COLUMN_USER_ACTIVE, userActive);
        values.put(COLUMN_USER_BIRTHDATE, userBirthdate);
        values.put(COLUMN_USER_GENDER, userGender);
        values.put(COLUMN_GIVE_ANCHOR, giveAnchor);
        values.put(COLUMN_GIVE_TIMING, giveTiming);
        values.put(COLUMN_GLANCE_ANCHOR, glanceAnchor);
        values.put(COLUMN_GLANCE_SINCE, glanceSince);
        values.put(COLUMN_GLANCE_THEME, glanceTheme);
        values.put(COLUMN_SEARCH_DIALOG, searchDialog);
        values.put(COLUMN_SEARCH_FOCUS, searchFocus);
        values.put(COLUMN_SEARCH_FILTER, searchFilter);
        values.put(COLUMN_SEARCH_COMPANY, searchCompany);
        values.put(COLUMN_GIVE_MAGNITUDE, giveMagnitude);
        values.put(COLUMN_GIVE_IMPACT, giveImpact);
        values.put(COLUMN_SEARCH_TERM, searchTerm);
        values.put(COLUMN_SEARCH_CITY, searchCity);
        values.put(COLUMN_SEARCH_STATE, searchState);
        values.put(COLUMN_SEARCH_ZIP, searchZip);
        values.put(COLUMN_SEARCH_MINRATING, searchMinrating);
        values.put(COLUMN_SEARCH_PAGES, searchPages);
        values.put(COLUMN_SEARCH_ROWS, searchRows);
        values.put(COLUMN_GIVE_RESET, giveReset);
        values.put(COLUMN_SEARCH_SORT, searchSort);
        values.put(COLUMN_SEARCH_ORDER, searchOrder);
        values.put(COLUMN_RECORD_SORT, recordSort);
        values.put(COLUMN_RECORD_ORDER, recordOrder);
        values.put(COLUMN_GIVE_STAMP, giveStamp);
        values.put(COLUMN_RECORD_STAMP, recordStamp);
        values.put(COLUMN_USER_STAMP, userStamp);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        uid = values.getAsString(COLUMN_UID);
        userEmail = values.getAsString(COLUMN_USER_EMAIL);
        userActive = values.getAsInteger(COLUMN_USER_ACTIVE) == 1;
        userBirthdate = values.getAsString(COLUMN_USER_BIRTHDATE);
        userGender = values.getAsString(COLUMN_USER_GENDER);
        giveImpact = values.getAsString(COLUMN_GIVE_IMPACT);
        giveMagnitude = values.getAsString(COLUMN_GIVE_MAGNITUDE);
        giveAnchor = values.getAsLong(COLUMN_GIVE_ANCHOR);
        giveTiming = values.getAsInteger(COLUMN_GIVE_TIMING);
        glanceAnchor = values.getAsLong(COLUMN_GLANCE_ANCHOR);
        glanceSince = values.getAsInteger(COLUMN_GLANCE_SINCE) == 1;
        glanceTheme = values.getAsInteger(COLUMN_GLANCE_THEME);
        searchDialog = values.getAsInteger(COLUMN_SEARCH_DIALOG) == 1;
        searchFocus = values.getAsInteger(COLUMN_SEARCH_FOCUS) == 1;
        searchFilter = values.getAsInteger(COLUMN_SEARCH_FILTER) == 1;
        searchCompany = values.getAsString(COLUMN_SEARCH_COMPANY);
        searchTerm = values.getAsString(COLUMN_SEARCH_TERM);
        searchCity = values.getAsString(COLUMN_SEARCH_CITY);
        searchState = values.getAsString(COLUMN_SEARCH_STATE);
        searchZip = values.getAsString(COLUMN_SEARCH_ZIP);
        searchMinrating = values.getAsString(COLUMN_SEARCH_MINRATING);
        searchPages = values.getAsString(COLUMN_SEARCH_PAGES);
        searchRows = values.getAsString(COLUMN_SEARCH_ROWS);
        giveReset = values.getAsBoolean(COLUMN_GIVE_RESET);
        searchSort = values.getAsString(COLUMN_SEARCH_SORT);
        searchOrder = values.getAsString(COLUMN_SEARCH_ORDER);
        recordSort = values.getAsString(COLUMN_RECORD_SORT);
        recordOrder = values.getAsString(COLUMN_RECORD_ORDER);
        giveStamp = values.getAsLong(COLUMN_GIVE_STAMP);
        recordStamp = values.getAsLong(COLUMN_RECORD_STAMP);
        userStamp = values.getAsLong(COLUMN_USER_STAMP);
    }

    @Override public User clone() {
        User clone  = new User(this);
        try { super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Class must implement Cloneable interface");
        } return clone;
    }
    
    public static User getDefault() {
        User user = new User();
        user.uid = "";
        user.userEmail = "";
        user.userActive = true;
        user.userBirthdate = "0/0/2000";
        user.userGender = "";
        user.giveImpact = "0";
        user.giveMagnitude = "0.01";
        user.giveAnchor = 0;
        user.giveTiming = 0;
        user.glanceAnchor = 0;
        user.glanceSince = false;
        user.glanceTheme = 0;
        user.searchDialog = false;
        user.searchFocus = false;
        user.searchFilter = true;
        user.searchCompany = "";
        user.searchTerm = "";
        user.searchCity = "";
        user.searchState = "";
        user.searchZip = "";
        user.searchMinrating = "";
        user.searchPages = "";
        user.searchRows = "";
        user.giveReset = false;
        user.searchSort = "RATING";
        user.searchOrder = "DESC";
        user.recordSort = "donationTime";
        user.recordOrder = "DESC";
        user.giveStamp = 0;
        user.recordStamp = 0;
        user.userStamp = 0;
        return user;
    }
}
