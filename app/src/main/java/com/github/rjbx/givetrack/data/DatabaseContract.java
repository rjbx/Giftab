package com.github.rjbx.givetrack.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.User;

/**
 * Defines attributes for tables and entries of database initialized in {@link DatabaseOpener}.
 */
public final class DatabaseContract {

    static final String AUTHORITY = "com.github.rjbx.givetrack";
    static final String PATH_SPAWN_TABLE = "spawn.table";
    static final String PATH_TARGET_TABLE = "target.table";
    static final String PATH_RECORD_TABLE = "record.table";
    static final String PATH_USER_TABLE = "user.table";

    private static final String SCHEME = "content";
    private static final Uri BASE_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    public static final int LOADER_ID_SPAWN = 1;
    public static final int LOADER_ID_TARGET = 2;
    public static final int LOADER_ID_RECORD = 3;
    public static final int LOADER_ID_USER = 4;

    public static final class CompanyEntry implements BaseColumns {

        static final String TABLE_NAME_SPAWN = "spawn";
        static final String TABLE_NAME_TARGET = "target";
        static final String TABLE_NAME_RECORD = "record";
        
        public static final Uri CONTENT_URI_SPAWN =
                BASE_URI.buildUpon().appendPath(PATH_SPAWN_TABLE).build();
        public static final Uri CONTENT_URI_TARGET =
                BASE_URI.buildUpon().appendPath(PATH_TARGET_TABLE).build();
        public static final Uri CONTENT_URI_RECORD =
                BASE_URI.buildUpon().appendPath(PATH_RECORD_TABLE).build();

        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_EIN = "ein";
        public static final String COLUMN_STAMP = "stamp";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LOCATION_STREET = "locationStreet";
        public static final String COLUMN_LOCATION_DETAIL = "locationDetail";
        public static final String COLUMN_LOCATION_CITY = "locationCity";
        public static final String COLUMN_LOCATION_STATE = "locationState";
        public static final String COLUMN_LOCATION_ZIP = "locationZip";
        public static final String COLUMN_HOMEPAGE_URL = "homepageUrl";
        public static final String COLUMN_NAVIGATOR_URL = "navigatorUrl";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_SOCIAL = "social";
        public static final String COLUMN_IMPACT = "impact";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_PERCENT = "percent";
        public static final String COLUMN_FREQUENCY = "frequency";
        public static final String COLUMN_MEMO = "memo";
        public static final String COLUMN_TIME = "time";
    }

    public static final class UserEntry implements BaseColumns {
        static final String TABLE_NAME_USER = "user";

        public static final Uri CONTENT_URI_USER =
                BASE_URI.buildUpon().appendPath(PATH_USER_TABLE).build();

        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_USER_EMAIL = "userEmail";
        public static final String COLUMN_USER_ACTIVE = "userActive";
        public static final String COLUMN_USER_BIRTHDATE = "userBirthdate";
        public static final String COLUMN_USER_GENDER = "userGender";
        public static final String COLUMN_GIVE_IMPACT = "giveImpact";
        public static final String COLUMN_GIVE_MAGNITUDE = "giveMagnitude";
        public static final String COLUMN_GIVE_ANCHOR = "giveAnchor";
        public static final String COLUMN_GIVE_TIMING = "giveTiming";
        public static final String COLUMN_GLANCE_ANCHOR = "glanceAnchor";
        public static final String COLUMN_GLANCE_SINCE = "glanceSince";
        public static final String COLUMN_GLANCE_THEME = "glanceTheme";
        public static final String COLUMN_SPAWN_DIALOG = "spawnDialog";
        public static final String COLUMN_SPAWN_FOCUS = "spawnFocus";
        public static final String COLUMN_SPAWN_FILTER = "spawnFilter";
        public static final String COLUMN_SPAWN_COMPANY = "spawnCompany";
        public static final String COLUMN_SPAWN_TERM = "spawnTerm";
        public static final String COLUMN_SPAWN_CITY = "spawnCity";
        public static final String COLUMN_SPAWN_STATE = "spawnState";
        public static final String COLUMN_SPAWN_ZIP = "spawnZip";
        public static final String COLUMN_SPAWN_MINRATING = "spawnMinrating";
        public static final String COLUMN_SPAWN_PAGES = "spawnPages";
        public static final String COLUMN_SPAWN_ROWS = "spawnRows";
        public static final String COLUMN_GIVE_RESET = "giveReset";
        public static final String COLUMN_SPAWN_SORT = "spawnSort";
        public static final String COLUMN_SPAWN_ORDER = "spawnOrder";
        public static final String COLUMN_RECORD_SORT = "recordSort";
        public static final String COLUMN_RECORD_ORDER = "recordOrder";
        public static final String COLUMN_TARGET_STAMP = "giveStamp";
        public static final String COLUMN_RECORD_STAMP = "recordStamp";
        public static final String COLUMN_USER_STAMP = "userStamp";
    }

    public static <T extends Entry> Uri getContentUri(Class<T> entryType) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_TARGET: return CompanyEntry.CONTENT_URI_TARGET;
            case CompanyEntry.TABLE_NAME_RECORD: return CompanyEntry.CONTENT_URI_RECORD;
            case CompanyEntry.TABLE_NAME_SPAWN: return CompanyEntry.CONTENT_URI_SPAWN;
            case UserEntry.TABLE_NAME_USER: return UserEntry.CONTENT_URI_USER;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static <T extends Entry> String getTimeTableColumn(Class<T> entryType) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_TARGET: return UserEntry.COLUMN_TARGET_STAMP;
            case CompanyEntry.TABLE_NAME_RECORD: return UserEntry.COLUMN_RECORD_STAMP;
            case CompanyEntry.TABLE_NAME_SPAWN: return "";
            case UserEntry.TABLE_NAME_USER: return UserEntry.COLUMN_USER_STAMP;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static <T extends Entry> long getTableTime(Class<T> entryType, User user) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_SPAWN: return 0;
            case CompanyEntry.TABLE_NAME_TARGET: return user.getTargetStamp();
            case CompanyEntry.TABLE_NAME_RECORD: return user.getRecordStamp();
            case UserEntry.TABLE_NAME_USER: return user.getUserStamp();
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static <T extends Entry> void setTableTime(Class<T> entryType, User user, long time) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_SPAWN: break;
            case CompanyEntry.TABLE_NAME_TARGET: user.setTargetStamp(time); break;
            case CompanyEntry.TABLE_NAME_RECORD: user.setRecordStamp(time); break;
            case UserEntry.TABLE_NAME_USER: user.setUserStamp(time); break;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }
}
