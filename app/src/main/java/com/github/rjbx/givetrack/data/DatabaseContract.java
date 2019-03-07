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
    static final String PATH_SEARCH_TABLE = "search.table";
    static final String PATH_GIVING_TABLE = "giving.table";
    static final String PATH_RECORD_TABLE = "record.table";
    static final String PATH_USER_TABLE = "user.table";

    private static final String SCHEME = "content";
    private static final Uri BASE_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    public static final int LOADER_ID_SEARCH = 1;
    public static final int LOADER_ID_GIVING = 2;
    public static final int LOADER_ID_RECORD = 3;
    public static final int LOADER_ID_USER = 4;

    public static final class CompanyEntry implements BaseColumns {

        static final String TABLE_NAME_SEARCH = "search";
        static final String TABLE_NAME_GIVING = "giving";
        static final String TABLE_NAME_RECORD = "record";
        
        public static final Uri CONTENT_URI_SEARCH =
                BASE_URI.buildUpon().appendPath(PATH_SEARCH_TABLE).build();
        public static final Uri CONTENT_URI_GIVING =
                BASE_URI.buildUpon().appendPath(PATH_GIVING_TABLE).build();
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
        public static final String COLUMN_SEARCH_DIALOG = "searchDialog";
        public static final String COLUMN_SEARCH_FOCUS = "searchFocus";
        public static final String COLUMN_SEARCH_FILTER = "searchFilter";
        public static final String COLUMN_SEARCH_COMPANY = "searchCompany";
        public static final String COLUMN_SEARCH_TERM = "searchTerm";
        public static final String COLUMN_SEARCH_CITY = "searchCity";
        public static final String COLUMN_SEARCH_STATE = "searchState";
        public static final String COLUMN_SEARCH_ZIP = "searchZip";
        public static final String COLUMN_SEARCH_MINRATING = "searchMinrating";
        public static final String COLUMN_SEARCH_PAGES = "searchPages";
        public static final String COLUMN_SEARCH_ROWS = "searchRows";
        public static final String COLUMN_GIVE_RESET = "giveReset";
        public static final String COLUMN_SEARCH_SORT = "searchSort";
        public static final String COLUMN_SEARCH_ORDER = "searchOrder";
        public static final String COLUMN_RECORD_SORT = "recordSort";
        public static final String COLUMN_RECORD_ORDER = "recordOrder";
        public static final String COLUMN_GIVE_STAMP = "giveStamp";
        public static final String COLUMN_RECORD_STAMP = "recordStamp";
        public static final String COLUMN_USER_STAMP = "userStamp";
    }

    public static <T extends Entry> Uri getContentUri(Class<T> entryType) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_GIVING: return CompanyEntry.CONTENT_URI_GIVING;
            case CompanyEntry.TABLE_NAME_RECORD: return CompanyEntry.CONTENT_URI_RECORD;
            case CompanyEntry.TABLE_NAME_SEARCH: return CompanyEntry.CONTENT_URI_SEARCH;
            case UserEntry.TABLE_NAME_USER: return UserEntry.CONTENT_URI_USER;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static <T extends Entry> String getTimeTableColumn(Class<T> entryType) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_GIVING: return UserEntry.COLUMN_GIVE_STAMP;
            case CompanyEntry.TABLE_NAME_RECORD: return UserEntry.COLUMN_RECORD_STAMP;
            case CompanyEntry.TABLE_NAME_SEARCH: return "";
            case UserEntry.TABLE_NAME_USER: return UserEntry.COLUMN_USER_STAMP;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }
}
