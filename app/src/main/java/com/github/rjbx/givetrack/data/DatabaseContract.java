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
        public static final String COLUMN_CHARITY_NAME = "charityName";
        public static final String COLUMN_LOCATION_STREET = "locationStreet";
        public static final String COLUMN_LOCATION_DETAIL = "locationDetail";
        public static final String COLUMN_LOCATION_CITY = "locationCity";
        public static final String COLUMN_LOCATION_STATE = "locationState";
        public static final String COLUMN_LOCATION_ZIP = "locationZip";
        public static final String COLUMN_HOMEPAGE_URL = "homepageUrl";
        public static final String COLUMN_NAVIGATOR_URL = "navigatorUrl";
        public static final String COLUMN_PHONE_NUMBER = "phoneNumber";
        public static final String COLUMN_EMAIL_ADDRESS = "emailAddress";
        public static final String COLUMN_SOCIAL_HANDLE = "socialHandle";
        public static final String COLUMN_DONATION_IMPACT = "donationTotal";
        public static final String COLUMN_DONATION_TYPE = "donationType";
        public static final String COLUMN_DONATION_PERCENTAGE = "donationPercentage";
        public static final String COLUMN_DONATION_FREQUENCY = "donationFrequency";
        public static final String COLUMN_DONATION_MEMO = "donationMemo";
        public static final String COLUMN_DONATION_TIME = "donationTime";

        public static final int INDEX_STAMP = 0;
        public static final int INDEX_UID = 1;
        public static final int INDEX_EIN = 2;
        public static final int INDEX_CHARITY_NAME = 3;
        public static final int INDEX_LOCATION_STREET = 4;
        public static final int INDEX_LOCATION_DETAIL = 5;
        public static final int INDEX_LOCATION_CITY = 6;
        public static final int INDEX_LOCATION_STATE = 7;
        public static final int INDEX_LOCATION_ZIP = 8;
        public static final int INDEX_HOMEPAGE_URL = 9;
        public static final int INDEX_NAVIGATOR_URL = 10;
        public static final int INDEX_PHONE_NUMBER = 11;
        public static final int INDEX_EMAIL_ADDRESS = 12;
        public static final int INDEX_SOCIAL_HANDLE = 13;
        public static final int INDEX_DONATION_IMPACT = 14;
        public static final int INDEX_DONATION_TYPE = 15;
        public static final int INDEX_DONATION_PERCENTAGE = 16;
        public static final int INDEX_DONATION_MEMO = 16;
        public static final int INDEX_DONATION_FREQUENCY = 17;
        public static final int INDEX_DONATION_TIME = 17;
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
            case CompanyEntry.TABLE_NAME_GIVING: return UserEntry.COLUMN_TIMEGIVING;
            case CompanyEntry.TABLE_NAME_RECORD: return UserEntry.COLUMN_TIMERECORD;
            case CompanyEntry.TABLE_NAME_SEARCH: return "";
            case UserEntry.TABLE_NAME_USER: return UserEntry.COLUMN_TIMEUSER;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static <T extends Entry> long getTableTime(Class<T> entryType, User user) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_SEARCH: return 0;
            case CompanyEntry.TABLE_NAME_GIVING: return user.getTimeGiving();
            case CompanyEntry.TABLE_NAME_RECORD: return user.getTimeRecord();
            case UserEntry.TABLE_NAME_USER: return user.getTimeUser();
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static <T extends Entry> void setTableTime(Class<T> entryType, User user, long time) {
        String name = entryType.getSimpleName().toLowerCase();
        switch (name) {
            case CompanyEntry.TABLE_NAME_SEARCH: break;
            case CompanyEntry.TABLE_NAME_GIVING: user.setTimeGiving(time); break;
            case CompanyEntry.TABLE_NAME_RECORD: user.setTimeRecord(time); break;
            case UserEntry.TABLE_NAME_USER: user.setTimeUser(time); break;
            default: throw new IllegalArgumentException("Argument must implement Entry interface");
        }
    }

    public static final class UserEntry implements BaseColumns {
        static final String TABLE_NAME_USER = "user";

        public static final Uri CONTENT_URI_USER =
                BASE_URI.buildUpon().appendPath(PATH_USER_TABLE).build();

        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_EMAIL = "userEmail";
        public static final String COLUMN_ACTIVE = "userActive";
        public static final String COLUMN_BIRTHDATE = "userBirthdate";
        public static final String COLUMN_GENDER = "userGender";
        public static final String COLUMN_DONATION = "giveImpact";
        public static final String COLUMN_MAGNITUDE = "giveMagnitude";
        public static final String COLUMN_ANCHOR = "giveAnchor";
        public static final String COLUMN_HISTORICAL = "giveTiming";
        public static final String COLUMN_TIMETRACK = "glanceAnchor";
        public static final String COLUMN_VIEWTRACK = "glanceSince";
        public static final String COLUMN_THEME = "glanceTheme";
        public static final String COLUMN_SEARCHGUIDE = "searchDialog";
        public static final String COLUMN_FOCUS = "searchFocus";
        public static final String COLUMN_FILTER = "searchFilter";
        public static final String COLUMN_COMPANY = "searchCompany";
        public static final String COLUMN_TERM = "searchTerm";
        public static final String COLUMN_CITY = "searchCity";
        public static final String COLUMN_STATE = "searchState";
        public static final String COLUMN_ZIP = "searchZip";
        public static final String COLUMN_MINRATING = "searchMinrating";
        public static final String COLUMN_PAGES = "searchPages";
        public static final String COLUMN_ROWS = "20";
        public static final String COLUMN_RATINGRESET = "giveReset";
        public static final String COLUMN_SEARCHSORT = "searchSort";
        public static final String COLUMN_SEARCHORDER = "searchOrder";
        public static final String COLUMN_RECORDSORT = "recordSort";
        public static final String COLUMN_RECORDORDER = "recordOrder";
        public static final String COLUMN_TIMEGIVING = "giveStamp";
        public static final String COLUMN_TIMERECORD = "recordStamp";
        public static final String COLUMN_TIMEUSER = "userStamp";
    }
}
