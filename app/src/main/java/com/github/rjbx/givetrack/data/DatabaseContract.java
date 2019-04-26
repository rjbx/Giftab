package com.github.rjbx.givetrack.data;

import android.net.Uri;
import android.provider.BaseColumns;

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
        public static final String COLUMN_SHARE = "share";
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
        public static final String COLUMN_GIVE_ROUNDING = "giveRounding";
        public static final String COLUMN_GIVE_TIMING = "giveTiming";
        public static final String COLUMN_GLANCE_ANCHOR = "glanceAnchor";
        public static final String COLUMN_GLANCE_SINCE = "glanceSince";
        public static final String COLUMN_GLANCE_THEME = "glanceTheme";
        public static final String COLUMN_INDEX_ANCHOR = "indexAnchor";
        public static final String COLUMN_INDEX_COUNT = "indexCount";
        public static final String COLUMN_INDEX_DIALOG = "indexDialog";
        public static final String COLUMN_INDEX_FOCUS = "indexFocus";
        public static final String COLUMN_INDEX_FILTER = "indexFilter";
        public static final String COLUMN_INDEX_COMPANY = "indexCompany";
        public static final String COLUMN_INDEX_TERM = "indexTerm";
        public static final String COLUMN_INDEX_CITY = "indexCity";
        public static final String COLUMN_INDEX_STATE = "indexState";
        public static final String COLUMN_INDEX_ZIP = "indexZip";
        public static final String COLUMN_INDEX_MINRATING = "indexMinrating";
        public static final String COLUMN_INDEX_PAGES = "indexPages";
        public static final String COLUMN_INDEX_ROWS = "indexRows";
        public static final String COLUMN_GIVE_RESET = "giveReset";
        public static final String COLUMN_INDEX_SORT = "indexSort";
        public static final String COLUMN_INDEX_ORDER = "indexOrder";
        public static final String COLUMN_JOURNAL_SORT = "journalSort";
        public static final String COLUMN_JOURNAL_ORDER = "journalOrder";
        public static final String COLUMN_TARGET_STAMP = "targetStamp";
        public static final String COLUMN_RECORD_STAMP = "recordStamp";
        public static final String COLUMN_USER_STAMP = "userStamp";
    }
}
