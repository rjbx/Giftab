package com.github.rjbx.givetrack.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import com.github.rjbx.givetrack.data.DatabaseContract.Entry;

/**
 * Defines behavior on creation and upgrade of {@link SQLiteDatabase} database.
 */
public class DatabaseOpener extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "givetrack.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Instantiates {@link SQLiteOpenHelper} extended by this class.
     * @param context provides access to environment of caller;
     *                cannot store {@code null} value or be reassigned.
     */
    DatabaseOpener(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Builds and executes statements that create data-caching tables for given database.
     * @param db database with which to populate tables.
     */
    @Override public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_SEARCH_TABLE =

                "CREATE TABLE IF NOT EXISTS " +
                        Entry.TABLE_NAME_SEARCH             + " ("                              +

                        Entry.COLUMN_EIN                    + " TEXT PRIMARY KEY NOT NULL, "    +
                        Entry.COLUMN_CHARITY_NAME           + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_STREET        + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_DETAIL        + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_CITY          + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_STATE         + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_ZIP           + " TEXT NOT NULL, "                +
                        Entry.COLUMN_HOMEPAGE_URL           + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_NAVIGATOR_URL          + " TEXT NOT NULL,"                 +

                        "UNIQUE (" + Entry.COLUMN_EIN +
                        ") ON CONFLICT REPLACE" + ");";

        final String SQL_CREATE_GIVING_TABLE =

                "CREATE TABLE IF NOT EXISTS " +
                        Entry.TABLE_NAME_GIVING             + " ("                              +

                        Entry.COLUMN_EIN                    + " TEXT PRIMARY KEY NOT NULL, "    +
                        Entry.COLUMN_CHARITY_NAME           + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_STREET        + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_DETAIL        + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_CITY          + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_STATE         + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_ZIP           + " TEXT NOT NULL, "                +
                        Entry.COLUMN_HOMEPAGE_URL           + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_NAVIGATOR_URL          + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_PHONE_NUMBER           + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_EMAIL_ADDRESS          + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_DONATION_IMPACT        + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_DONATION_PERCENTAGE    + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_DONATION_FREQUENCY     + " INTEGER NOT NULL,"              +

                        "UNIQUE (" + Entry.COLUMN_EIN +
                        ") ON CONFLICT REPLACE" + ");";

        final String SQL_CREATE_RECORD_TABLE =

                "CREATE TABLE IF NOT EXISTS " +
                        Entry.TABLE_NAME_RECORD             + " ("                              +

                        Entry.COLUMN_EIN                    + " TEXT PRIMARY KEY NOT NULL, "    +
                        Entry.COLUMN_CHARITY_NAME           + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_STREET        + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_DETAIL        + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_CITY          + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_STATE         + " TEXT NOT NULL, "                +
                        Entry.COLUMN_LOCATION_ZIP           + " TEXT NOT NULL, "                +
                        Entry.COLUMN_HOMEPAGE_URL           + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_NAVIGATOR_URL          + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_PHONE_NUMBER           + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_EMAIL_ADDRESS          + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_DONATION_IMPACT        + " TEXT NOT NULL,"                 +
                        Entry.COLUMN_DONATION_TIME          + " INTEGER NOT NULL,"              +

                        "UNIQUE (" + Entry.COLUMN_DONATION_TIME +
                        ") ON CONFLICT REPLACE" + ");";

        db.execSQL(SQL_CREATE_SEARCH_TABLE);
        db.execSQL(SQL_CREATE_GIVING_TABLE);
        db.execSQL(SQL_CREATE_RECORD_TABLE);
    }

    /**
     * Removes and creates a new table on version upgrades.
     */
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Entry.TABLE_NAME_SEARCH);
        db.execSQL("DROP TABLE IF EXISTS " + Entry.TABLE_NAME_GIVING);
        db.execSQL("DROP TABLE IF EXISTS " + Entry.TABLE_NAME_RECORD);
        onCreate(db);
    }
}
