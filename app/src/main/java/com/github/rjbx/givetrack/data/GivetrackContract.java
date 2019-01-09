package com.github.rjbx.givetrack.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines attributes for tables and entries of database initialized in {@link GivetrackOpener}.
 */
public final class GivetrackContract {

    static final String AUTHORITY = "com.github.rjbx.givetrack";
    static final String PATH_COLLECTION_TABLE = "collection.table";
    static final String PATH_GENERATION_TABLE = "generation.table";

    private static final String SCHEME = "content";
    private static final Uri BASE_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    public static final class Entry implements BaseColumns {

        static final String TABLE_NAME_COLLECTION = "collection";
        static final String TABLE_NAME_GENERATION = "generation";
        
        public static final Uri CONTENT_URI_COLLECTION =
                BASE_URI.buildUpon().appendPath(PATH_COLLECTION_TABLE).build();
        public static final Uri CONTENT_URI_GENERATION =
                BASE_URI.buildUpon().appendPath(PATH_GENERATION_TABLE).build();

        public static final String COLUMN_EIN = "ein";
        public static final String COLUMN_CHARITY_NAME = "charityName";
        public static final String COLUMN_LOCATION_STREET = "locationStreet";
        public static final String COLUMN_LOCATION_DETAIL = "locationDetail";
        public static final String COLUMN_LOCATION_CITY = "locationCity";
        public static final String COLUMN_LOCATION_STATE = "locationState";
        public static final String COLUMN_LOCATOIN_ZIP = "locationZip";
        public static final String COLUMN_PHONE_NUMBER = "phoneNumber";
        public static final String COLUMN_EMAIL_ADDRESS = "emailAddress";
        public static final String COLUMN_HOMEPAGE_URL = "homepageUrl";
        public static final String COLUMN_NAVIGATOR_URL = "navigatorUrl";
        public static final String COLUMN_DONATION_PERCENTAGE = "donationPercentage";
        public static final String COLUMN_DONATION_IMPACT = "donationTotal";
        public static final String COLUMN_DONATION_FREQUENCY = "donationFrequency";

        public static final int INDEX_EIN = 0;
        public static final int INDEX_CHARITY_NAME = 1;
        public static final int INDEX_LOCATION_STREET = 2;
        public static final int INDEX_LOCATION_DETAIL = 3;
        public static final int INDEX_LOCATION_CITY = 4;
        public static final int INDEX_LOCATION_STATE = 5;
        public static final int INDEX_LOCATION_ZIP = 6;
        public static final int INDEX_PHONE_NUMBER = 7;
        public static final int INDEX_EMAIL_ADDRESS = 8;
        public static final int INDEX_HOMEPAGE_URL = 9;
        public static final int INDEX_NAVIGATOR_URL = 10;
        public static final int INDEX_DONATION_PERCENTAGE = 11;
        public static final int INDEX_DONATION_IMPACT = 12;
        public static final int INDEX_DONATION_FREQUENCY = 13;
    }
}