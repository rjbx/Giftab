package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.github.rjbx.givetrack.AppUtilities;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static com.github.rjbx.givetrack.data.DatabaseContract.UserEntry.*;

/**
 * Stores data about an end user; initialized from {@link com.google.firebase.auth.FirebaseUser},
 * persisted locally with {@link android.content.ContentProvider}
 * and remotely with {@link com.google.firebase.database.FirebaseDatabase}.
 */
@IgnoreExtraProperties
public class User implements Entry, Parcelable, Cloneable {

    private long giveAnchor;
    private String giveImpact;
    private String giveMagnitude;
    private boolean giveReset;
    private int giveRounding;
    private long targetStamp; // Time of most recent change to target table
    private int giveTiming;
    private long glanceAnchor;
    private boolean glanceSince;
    private int glanceHometype;
    private int glanceGraphtype;
    private int glanceInterval;
    private int glanceTheme;
    private String journalOrder;
    private String journalSort;
    private long recordStamp; // Time of most recent change to record table
    private long indexAnchor; // Time of most recent fetch count reset
    private int indexCount; // Store remaining fetch count
    private String indexCity;
    private String indexCompany;
    private boolean indexDialog;
    private boolean indexFilter;
    private boolean indexFocus;
    private String indexMinrating;
    private String indexOrder;
    private String indexPages;
    private String indexRows;
    private String indexSort;
    private String indexState;
    private String indexTerm;
    private String indexZip;
    private String uid;
    private boolean userActive;
    private String userBirthdate;
    private String userEmail;
    private String userGender;
    private long userStamp; // Time of most recent change (save those to target or record stamps) to user table

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
        dest.writeInt(giveRounding);
        dest.writeLong(glanceAnchor);
        dest.writeInt(glanceSince ? 1 : 0);
        dest.writeInt(glanceHometype);
        dest.writeInt(glanceGraphtype);
        dest.writeInt(glanceInterval);
        dest.writeInt(glanceTheme);
        dest.writeLong(indexAnchor);
        dest.writeInt(indexCount);
        dest.writeInt(indexDialog ? 1 : 0);
        dest.writeInt(indexFocus ? 1 : 0);
        dest.writeInt(indexFilter ? 1 : 0);
        dest.writeString(indexCompany);
        dest.writeString(indexTerm);
        dest.writeString(indexCity);
        dest.writeString(indexState);
        dest.writeString(indexZip);
        dest.writeString(indexMinrating);
        dest.writeString(indexPages);
        dest.writeString(indexRows);
        dest.writeInt(giveReset ? 1 : 0);
        dest.writeString(indexSort);
        dest.writeString(indexOrder);
        dest.writeString(journalSort);
        dest.writeString(journalOrder);
        dest.writeLong(targetStamp);
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
        giveRounding = source.readInt();
        glanceAnchor = source.readLong();
        glanceSince = source.readInt() == 1;
        glanceHometype = source.readInt();
        glanceGraphtype = source.readInt();
        glanceInterval = source.readInt();
        glanceTheme = source.readInt();
        indexAnchor = source.readLong();
        indexCount = source.readInt();
        indexDialog = source.readInt() == 1;
        indexFocus = source.readInt() == 1;
        indexFilter = source.readInt() == 1;
        indexCompany = source.readString();
        indexTerm = source.readString();
        indexCity = source.readString();
        indexState = source.readString();
        indexZip = source.readString();
        indexMinrating = source.readString();
        indexPages = source.readString();
        indexRows = source.readString();
        giveReset = source.readInt() == 1;
        indexSort = source.readString();
        indexOrder = source.readString();
        journalSort = source.readString();
        journalOrder = source.readString();
        targetStamp = source.readLong();
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
        this.giveRounding = user.giveRounding;
        this.glanceAnchor = user.glanceAnchor;
        this.glanceSince = user.glanceSince;
        this.glanceHometype = user.glanceHometype;
        this.glanceGraphtype = user.glanceGraphtype;
        this.glanceInterval = user.glanceInterval;
        this.glanceTheme = user.glanceTheme;
        this.indexAnchor = user.indexAnchor;
        this.indexCount = user.indexCount;
        this.indexDialog = user.indexDialog;
        this.indexFocus = user.indexFocus;
        this.indexFilter = user.indexFilter;
        this.indexCompany = user.indexCompany;
        this.indexTerm = user.indexTerm;
        this.indexCity = user.indexCity;
        this.indexState = user.indexState;
        this.indexZip = user.indexZip;
        this.indexMinrating = user.indexMinrating;
        this.indexPages = user.indexPages;
        this.indexRows = user.indexRows;
        this.giveReset = user.giveReset;
        this.indexSort = user.indexSort;
        this.indexOrder = user.indexOrder;
        this.journalSort = user.journalSort;
        this.journalOrder = user.journalOrder;
        this.targetStamp = user.targetStamp;
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
            int giveRounding,
            long glanceAnchor,
            boolean glanceSince,
            String giveImpact,
            String giveMagnitude,
            int glanceHometype,
            int glanceGraphtype,
            int glanceInterval,
            int glanceTheme,
            long indexAnchor,
            int indexCount,
            boolean indexDialog,
            boolean indexFocus,
            boolean indexFilter,
            String indexCompany,
            String indexTerm,
            String indexCity,
            String indexState,
            String indexZip,
            String indexMinrating,
            String indexPages,
            String indexRows,
            boolean giveReset,
            String indexSort,
            String indexOrder,
            String journalSort,
            String journalOrder,
            long targetStamp,
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
        this.giveRounding = giveRounding;
        this.glanceAnchor = glanceAnchor;
        this.glanceSince = glanceSince;
        this.glanceHometype = glanceHometype;
        this.glanceGraphtype = glanceGraphtype;
        this.glanceInterval = glanceInterval;
        this.glanceTheme = glanceTheme;
        this.indexAnchor = indexAnchor;
        this.indexCount = indexCount;
        this.indexDialog = indexDialog;
        this.indexFocus = indexFocus;
        this.indexFilter = indexFilter;
        this.indexCompany = indexCompany;
        this.indexTerm = indexTerm;
        this.indexCity = indexCity;
        this.indexState = indexState;
        this.indexZip = indexZip;
        this.indexMinrating = indexMinrating;
        this.indexPages = indexPages;
        this.indexRows = indexRows;
        this.giveReset = giveReset;
        this.indexSort = indexSort;
        this.indexOrder = indexOrder;
        this.journalSort = journalSort;
        this.journalOrder = journalOrder;
        this.targetStamp = targetStamp;
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
    public int getGiveRounding() { return giveRounding; }
    public void setGiveRounding(int giveRounding) { this.giveRounding = giveRounding; }
    public long getGlanceAnchor() { return glanceAnchor; }
    public void setGlanceAnchor(long glanceAnchor) { this.glanceAnchor = glanceAnchor; }
    public boolean getGlanceSince() { return glanceSince; }
    public void setGlanceSince(boolean glanceSince) { this.glanceSince = glanceSince; }
    public int getGlanceHometype() { return glanceHometype; }
    public void setGlanceHometype(int glanceHometype) { this.glanceHometype = glanceHometype; }
    public int getGlanceGraphtype() { return glanceGraphtype; }
    public void setGlanceGraphtype(int glanceGraphtype) { this.glanceGraphtype = glanceGraphtype; }
    public int getGlanceInterval() { return glanceInterval; }
    public void setGlanceInterval(int glanceInterval) { this.glanceInterval = glanceInterval; }
    public int getGlanceTheme() { return glanceTheme; }
    public void setGlanceTheme(int glanceTheme) { this.glanceTheme = glanceTheme; }
    public long getIndexAnchor() { return indexAnchor; }
    public void setIndexAnchor(long indexAnchor) { this.indexAnchor = indexAnchor; }
    public int getIndexCount() { return indexCount; }
    public void setIndexCount(int indexCount) { this.indexCount = indexCount; }
    public boolean getIndexDialog() { return indexDialog; }
    public void setIndexDialog(boolean indexDialog) { this.indexDialog = indexDialog; }
    public String getIndexCompany() { return indexCompany; }
    public void setIndexCompany(String indexCompany) { this.indexCompany = indexCompany; }
    public boolean getIndexFocus() { return indexFocus; }
    public void setIndexFocus(boolean indexFocus) { this.indexFocus = indexFocus; }
    public boolean getIndexFilter() { return indexFilter; }
    public void setIndexFilter(boolean indexFilter) { this.indexFilter = indexFilter; }
    public String getIndexTerm() { return indexTerm; }
    public void setIndexTerm(String indexTerm) { this.indexTerm = indexTerm; }
    public String getIndexCity() { return indexCity; }
    public void setIndexCity(String indexCity) { this.indexCity = indexCity; }
    public String getIndexState() { return indexState; }
    public void setIndexState(String indexState) { this.indexState = indexState; }
    public String getIndexZip() { return indexZip; }
    public void setIndexZip(String indexZip) { this.indexZip = indexZip; }
    public String getIndexMinrating() { return indexMinrating; }
    public void setIndexMinrating(String indexMinrating) { this.indexMinrating = indexMinrating; }
    public String getIndexPages() { return indexPages; }
    public void setIndexPages(String indexPages) { this.indexPages = indexPages; }
    public String getIndexRows() { return indexRows; }
    public void setIndexRows(String indexRows){ this.indexRows = indexRows; }
    public boolean getGiveReset() { return giveReset; }
    public void setGiveReset(boolean giveReset) { this.giveReset = giveReset; }
    public String getIndexSort() { return indexSort; }
    public void setIndexSort(String indexSort) { this.indexSort = indexSort; }
    public String getIndexOrder() { return indexOrder; }
    public void setIndexOrder(String indexOrder) { this.indexOrder = indexOrder; }
    public String getJournalSort() { return journalSort; }
    public void setJournalSort(String journalSort) { this.journalSort = journalSort; }
    public String getJournalOrder() { return journalOrder; }
    public void setJournalOrder(String journalOrder) { this.journalOrder = journalOrder; }
    public long getTargetStamp() { return targetStamp; }
    public void setTargetStamp(long targetStamp) { this.targetStamp = targetStamp; }
    public long getRecordStamp() { return recordStamp; }
    public void setRecordStamp(long recordStamp) { this.recordStamp = recordStamp; }
    public long getUserStamp() { return userStamp; }
    public void setUserStamp(long userStamp) { this.userStamp = userStamp; }
    @Override public String getId() { return uid; }
    @Exclude @Override public User getObject() { return this; }

    @Override public Map<String, Object> toParameterMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(COLUMN_UID, uid);
        map.put(COLUMN_USER_EMAIL, userEmail);
        map.put(COLUMN_USER_ACTIVE, userActive);
        map.put(COLUMN_USER_BIRTHDATE, userBirthdate);
        map.put(COLUMN_USER_GENDER, userGender);
        map.put(COLUMN_GIVE_ANCHOR, giveAnchor);
        map.put(COLUMN_GIVE_TIMING, giveTiming);
        map.put(COLUMN_GIVE_ROUNDING, giveRounding);
        map.put(COLUMN_GLANCE_ANCHOR, glanceAnchor);
        map.put(COLUMN_GLANCE_SINCE, glanceSince);
        map.put(COLUMN_GLANCE_HOMETYPE, glanceHometype);
        map.put(COLUMN_GLANCE_GRAPHTYPE, glanceGraphtype);
        map.put(COLUMN_GLANCE_INTERVAL, glanceInterval);
        map.put(COLUMN_GLANCE_THEME, glanceTheme);
        map.put(COLUMN_INDEX_ANCHOR, indexAnchor);
        map.put(COLUMN_INDEX_COUNT, indexCount);
        map.put(COLUMN_INDEX_DIALOG, indexDialog);
        map.put(COLUMN_INDEX_FOCUS, indexFocus);
        map.put(COLUMN_INDEX_FILTER, indexFilter);
        map.put(COLUMN_INDEX_COMPANY, indexCompany);
        map.put(COLUMN_GIVE_MAGNITUDE, giveMagnitude);
        map.put(COLUMN_GIVE_IMPACT, giveImpact);
        map.put(COLUMN_INDEX_TERM, indexTerm);
        map.put(COLUMN_INDEX_CITY, indexCity);
        map.put(COLUMN_INDEX_STATE, indexState);
        map.put(COLUMN_INDEX_ZIP, indexZip);
        map.put(COLUMN_INDEX_MINRATING, indexMinrating);
        map.put(COLUMN_INDEX_PAGES, indexPages);
        map.put(COLUMN_INDEX_ROWS, indexRows);
        map.put(COLUMN_GIVE_RESET, giveReset);
        map.put(COLUMN_INDEX_SORT, indexSort);
        map.put(COLUMN_INDEX_ORDER, indexOrder);
        map.put(COLUMN_JOURNAL_SORT, journalSort);
        map.put(COLUMN_JOURNAL_ORDER, journalOrder);
        map.put(COLUMN_TARGET_STAMP, targetStamp);
        map.put(COLUMN_RECORD_STAMP, recordStamp);
        map.put(COLUMN_USER_STAMP, userStamp);
        return map;
    }

    @Override public void fromParameterMap(Map<String, Object> map) {
        if (map.containsKey(COLUMN_GIVE_IMPACT)) giveImpact = (String) map.get(COLUMN_GIVE_IMPACT);
        if (map.containsKey(COLUMN_GIVE_MAGNITUDE)) giveMagnitude = (String) map.get(COLUMN_GIVE_MAGNITUDE);
        if (map.containsKey(COLUMN_GIVE_ANCHOR)) giveAnchor = (long) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GIVE_ANCHOR), Long.class);
        if (map.containsKey(COLUMN_GIVE_TIMING)) giveTiming = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GIVE_TIMING), Integer.class);
        if (map.containsKey(COLUMN_GIVE_ROUNDING)) giveRounding = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GIVE_ROUNDING), Integer.class);
        if (map.containsKey(COLUMN_GIVE_RESET)) giveReset = (boolean) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GIVE_RESET), Boolean.class);
        if (map.containsKey(COLUMN_GLANCE_ANCHOR)) glanceAnchor = (long) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GLANCE_ANCHOR), Long.class);
        if (map.containsKey(COLUMN_GLANCE_SINCE)) glanceSince = (boolean) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GLANCE_SINCE), Boolean.class);
        if (map.containsKey(COLUMN_GLANCE_HOMETYPE)) glanceHometype = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GLANCE_HOMETYPE), Integer.class);
        if (map.containsKey(COLUMN_GLANCE_GRAPHTYPE)) glanceGraphtype = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GLANCE_GRAPHTYPE), Integer.class);
        if (map.containsKey(COLUMN_GLANCE_INTERVAL)) glanceInterval = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GLANCE_INTERVAL), Integer.class);
        if (map.containsKey(COLUMN_GLANCE_THEME)) glanceTheme = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_GLANCE_THEME), Integer.class);
        if (map.containsKey(COLUMN_INDEX_ANCHOR)) indexAnchor = (long) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_INDEX_ANCHOR), Long.class);
        if (map.containsKey(COLUMN_GIVE_TIMING)) indexCount = (int) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_INDEX_COUNT), Integer.class);
        if (map.containsKey(COLUMN_INDEX_DIALOG)) indexDialog = (boolean) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_INDEX_DIALOG), Boolean.class);
        if (map.containsKey(COLUMN_INDEX_FOCUS)) indexFocus = (boolean) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_INDEX_FOCUS), Boolean.class);
        if (map.containsKey(COLUMN_INDEX_FILTER)) indexFilter = (boolean) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_INDEX_FILTER), Boolean.class);
        if (map.containsKey(COLUMN_INDEX_COMPANY)) indexCompany = (String) map.get(COLUMN_INDEX_COMPANY);
        if (map.containsKey(COLUMN_INDEX_TERM)) indexTerm = (String) map.get(COLUMN_INDEX_TERM);
        if (map.containsKey(COLUMN_INDEX_CITY)) indexCity = (String) map.get(COLUMN_INDEX_CITY);
        if (map.containsKey(COLUMN_INDEX_STATE)) indexState = (String) map.get(COLUMN_INDEX_STATE);
        if (map.containsKey(COLUMN_INDEX_ZIP)) indexZip = (String) map.get(COLUMN_INDEX_ZIP);
        if (map.containsKey(COLUMN_INDEX_MINRATING)) indexMinrating = (String) map.get(COLUMN_INDEX_MINRATING);
        if (map.containsKey(COLUMN_INDEX_PAGES)) indexPages = (String) map.get(COLUMN_INDEX_PAGES);
        if (map.containsKey(COLUMN_INDEX_ROWS)) indexRows = (String) map.get(COLUMN_INDEX_ROWS);
        if (map.containsKey(COLUMN_INDEX_SORT)) indexSort = (String) map.get(COLUMN_INDEX_SORT);
        if (map.containsKey(COLUMN_INDEX_ORDER)) indexOrder = (String) map.get(COLUMN_INDEX_ORDER);
        if (map.containsKey(COLUMN_JOURNAL_SORT)) journalSort = (String) map.get(COLUMN_JOURNAL_SORT);
        if (map.containsKey(COLUMN_JOURNAL_ORDER)) journalOrder = (String) map.get(COLUMN_JOURNAL_ORDER);
        if (map.containsKey(COLUMN_RECORD_STAMP)) recordStamp = (long) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_RECORD_STAMP), Long.class);
        if (map.containsKey(COLUMN_TARGET_STAMP)) targetStamp = (long) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_TARGET_STAMP), Long.class);
        if (map.containsKey(COLUMN_USER_STAMP)) userStamp = (long) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_USER_STAMP), Long.class);
        if (map.containsKey(COLUMN_UID)) uid = (String) map.get(COLUMN_UID);
        if (map.containsKey(COLUMN_USER_EMAIL)) userEmail = (String) map.get(COLUMN_USER_EMAIL);
        if (map.containsKey(COLUMN_USER_ACTIVE)) userActive = (boolean) AppUtilities.preferenceValueToNumerical(map.get(COLUMN_USER_ACTIVE), Boolean.class);
        if (map.containsKey(COLUMN_USER_BIRTHDATE)) userBirthdate = (String) map.get(COLUMN_USER_BIRTHDATE);
        if (map.containsKey(COLUMN_USER_GENDER)) userGender = (String) map.get(COLUMN_USER_GENDER);
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
        values.put(COLUMN_GIVE_ROUNDING, giveRounding);
        values.put(COLUMN_GLANCE_ANCHOR, glanceAnchor);
        values.put(COLUMN_GLANCE_SINCE, glanceSince);
        values.put(COLUMN_GLANCE_HOMETYPE, glanceHometype);
        values.put(COLUMN_GLANCE_GRAPHTYPE, glanceGraphtype);
        values.put(COLUMN_GLANCE_INTERVAL, glanceInterval);
        values.put(COLUMN_GLANCE_THEME, glanceTheme);
        values.put(COLUMN_INDEX_COUNT, indexCount);
        values.put(COLUMN_INDEX_DIALOG, indexDialog);
        values.put(COLUMN_INDEX_FOCUS, indexFocus);
        values.put(COLUMN_INDEX_FILTER, indexFilter);
        values.put(COLUMN_INDEX_COMPANY, indexCompany);
        values.put(COLUMN_GIVE_MAGNITUDE, giveMagnitude);
        values.put(COLUMN_GIVE_IMPACT, giveImpact);
        values.put(COLUMN_INDEX_ANCHOR, indexAnchor);
        values.put(COLUMN_INDEX_TERM, indexTerm);
        values.put(COLUMN_INDEX_CITY, indexCity);
        values.put(COLUMN_INDEX_STATE, indexState);
        values.put(COLUMN_INDEX_ZIP, indexZip);
        values.put(COLUMN_INDEX_MINRATING, indexMinrating);
        values.put(COLUMN_INDEX_PAGES, indexPages);
        values.put(COLUMN_INDEX_ROWS, indexRows);
        values.put(COLUMN_GIVE_RESET, giveReset);
        values.put(COLUMN_INDEX_SORT, indexSort);
        values.put(COLUMN_INDEX_ORDER, indexOrder);
        values.put(COLUMN_JOURNAL_SORT, journalSort);
        values.put(COLUMN_JOURNAL_ORDER, journalOrder);
        values.put(COLUMN_TARGET_STAMP, targetStamp);
        values.put(COLUMN_RECORD_STAMP, recordStamp);
        values.put(COLUMN_USER_STAMP, userStamp);
        return values;
    }

    @Override public void fromContentValues(ContentValues values) {
        uid = values.getAsString(COLUMN_UID);
        userEmail = values.getAsString(COLUMN_USER_EMAIL);
        userActive = values.getAsBoolean(COLUMN_USER_ACTIVE);
        userBirthdate = values.getAsString(COLUMN_USER_BIRTHDATE);
        userGender = values.getAsString(COLUMN_USER_GENDER);
        giveImpact = values.getAsString(COLUMN_GIVE_IMPACT);
        giveMagnitude = values.getAsString(COLUMN_GIVE_MAGNITUDE);
        giveAnchor = values.getAsLong(COLUMN_GIVE_ANCHOR);
        giveTiming = values.getAsInteger(COLUMN_GIVE_TIMING);
        giveRounding = values.getAsInteger(COLUMN_GIVE_ROUNDING);
        glanceAnchor = values.getAsLong(COLUMN_GLANCE_ANCHOR);
        glanceSince = values.getAsBoolean(COLUMN_GLANCE_SINCE);
        glanceHometype = values.getAsInteger(COLUMN_GLANCE_HOMETYPE);
        glanceGraphtype = values.getAsInteger(COLUMN_GLANCE_GRAPHTYPE);
        glanceInterval = values.getAsInteger(COLUMN_GLANCE_INTERVAL);
        glanceTheme = values.getAsInteger(COLUMN_GLANCE_THEME);
        indexAnchor = values.getAsLong(COLUMN_INDEX_ANCHOR);
        indexCount = values.getAsInteger(COLUMN_INDEX_COUNT);
        indexDialog = values.getAsBoolean(COLUMN_INDEX_DIALOG);
        indexFocus = values.getAsBoolean(COLUMN_INDEX_FOCUS);
        indexFilter = values.getAsBoolean(COLUMN_INDEX_FILTER);
        indexCompany = values.getAsString(COLUMN_INDEX_COMPANY);
        indexTerm = values.getAsString(COLUMN_INDEX_TERM);
        indexCity = values.getAsString(COLUMN_INDEX_CITY);
        indexState = values.getAsString(COLUMN_INDEX_STATE);
        indexZip = values.getAsString(COLUMN_INDEX_ZIP);
        indexMinrating = values.getAsString(COLUMN_INDEX_MINRATING);
        indexPages = values.getAsString(COLUMN_INDEX_PAGES);
        indexRows = values.getAsString(COLUMN_INDEX_ROWS);
        giveReset = values.getAsBoolean(COLUMN_GIVE_RESET);
        indexSort = values.getAsString(COLUMN_INDEX_SORT);
        indexOrder = values.getAsString(COLUMN_INDEX_ORDER);
        journalSort = values.getAsString(COLUMN_JOURNAL_SORT);
        journalOrder = values.getAsString(COLUMN_JOURNAL_ORDER);
        targetStamp = values.getAsLong(COLUMN_TARGET_STAMP);
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
        user.giveRounding = 0;
        user.glanceAnchor = 0;
        user.glanceSince = false;
        user.glanceHometype = 0;
        user.glanceGraphtype = 0;
        user.glanceInterval = 0;
        user.glanceTheme = 0;
        user.indexAnchor = 0;
        user.indexCount = 0;
        user.indexDialog = false;
        user.indexFocus = false;
        user.indexFilter = true;
        user.indexCompany = "";
        user.indexTerm = "";
        user.indexCity = "";
        user.indexState = "";
        user.indexZip = "";
        user.indexMinrating = "";
        user.indexPages = "";
        user.indexRows = "";
        user.giveReset = false;
        user.indexSort = "RATING";
        user.indexOrder = "DESC";
        user.journalSort = "time";
        user.journalOrder = "DESC";
        user.targetStamp = 0; // Resets User stamps
        user.recordStamp = 0; // Resets User stamps
        user.userStamp = 0; // Resets User stamps
        return user;
    }
}
