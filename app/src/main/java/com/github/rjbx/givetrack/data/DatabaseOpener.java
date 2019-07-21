package com.github.rjbx.givetrack.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import com.github.rjbx.givetrack.data.DatabaseContract.*;

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

        final String SQL_CREATE_SPAWN_TABLE =

                "CREATE TABLE IF NOT EXISTS "                       +
                        CompanyEntry.TABLE_NAME_SPAWN               + " ("                              +

                        CompanyEntry.COLUMN_STAMP                   + " INTEGER PRIMARY KEY NOT NULL,"  +
                        CompanyEntry.COLUMN_UID                     + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_EIN                     + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_NAME                    + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_STREET         + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_DETAIL         + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_CITY           + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_STATE          + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_ZIP            + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_HOMEPAGE_URL            + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_NAVIGATOR_URL           + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_PHONE                   + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_EMAIL                   + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_SOCIAL                  + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_IMPACT                  + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_TYPE                    + " INTEGER NOT NULL,"              +

                        "UNIQUE (" + CompanyEntry.COLUMN_STAMP +
                        ") ON CONFLICT REPLACE" + ");";

        final String SQL_CREATE_GIVE_TABLE =

                "CREATE TABLE IF NOT EXISTS "                       +
                        CompanyEntry.TABLE_NAME_TARGET              + " ("                              +

                        CompanyEntry.COLUMN_STAMP                   + " INTEGER PRIMARY KEY NOT NULL,"  +
                        CompanyEntry.COLUMN_UID                     + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_EIN                     + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_NAME                    + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_STREET         + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_DETAIL         + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_CITY           + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_STATE          + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_ZIP            + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_HOMEPAGE_URL            + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_NAVIGATOR_URL           + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_PHONE                   + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_EMAIL                   + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_SOCIAL                  + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_IMPACT                  + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_TYPE                    + " INTEGER NOT NULL,"              +
                        CompanyEntry.COLUMN_PERCENT                 + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_FREQUENCY               + " INTEGER NOT NULL,"              +

                        "UNIQUE (" + CompanyEntry.COLUMN_STAMP +
                        ") ON CONFLICT REPLACE" + ");";

        final String SQL_CREATE_RECORD_TABLE =

                "CREATE TABLE IF NOT EXISTS "                       +
                        CompanyEntry.TABLE_NAME_RECORD              + " ("                              +

                        CompanyEntry.COLUMN_STAMP                   + " INTEGER PRIMARY KEY NOT NULL,"  +
                        CompanyEntry.COLUMN_UID                     + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_EIN                     + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_NAME                    + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_STREET         + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_DETAIL         + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_CITY           + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_STATE          + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_LOCATION_ZIP            + " TEXT NOT NULL, "                +
                        CompanyEntry.COLUMN_HOMEPAGE_URL            + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_NAVIGATOR_URL           + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_PHONE                   + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_EMAIL                   + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_SOCIAL                  + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_IMPACT                  + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_TYPE                    + " INTEGER NOT NULL,"              +
                        CompanyEntry.COLUMN_MEMO                    + " TEXT NOT NULL,"                 +
                        CompanyEntry.COLUMN_TIME                    + " INTEGER NOT NULL,"              +

                        "UNIQUE (" + CompanyEntry.COLUMN_STAMP +
                        ") ON CONFLICT REPLACE" + ");";

        final String SQL_CREATE_USER_TABLE =

               "CREATE TABLE IF NOT EXISTS "                        +
                        UserEntry.TABLE_NAME_USER                   + " ("                              +

                        UserEntry.COLUMN_UID                        + " TEXT PRIMARY KEY NOT NULL, "    +
                        UserEntry.COLUMN_USER_EMAIL                 + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_USER_ACTIVE                + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_USER_BIRTHDATE             + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_USER_GENDER                + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_GIVE_IMPACT                + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_GIVE_MAGNITUDE             + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_GIVE_ANCHOR                + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GIVE_TIMING                + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GIVE_ROUNDING              + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GIVE_PAYMENT               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GLANCE_ANCHOR              + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GLANCE_SINCE               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GLANCE_HOMETYPE            + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GLANCE_GRAPHTYPE           + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GLANCE_INTERVAL            + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_GLANCE_THEME               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_ANCHOR               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_COUNT                + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_DIALOG               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_FOCUS                + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_FILTER               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_COMPANY              + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_TERM                 + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_CITY                 + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_STATE                + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_ZIP                  + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_MINRATING            + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_PAGES                + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_INDEX_ROWS                 + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_GIVE_RESET                 + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_INDEX_RANKED               + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_JOURNAL_SORT               + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_JOURNAL_ORDER              + " TEXT NOT NULL, "                +
                        UserEntry.COLUMN_TARGET_STAMP               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_RECORD_STAMP               + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_USER_STAMP                 + " INTEGER NOT NULL, "             +
                        UserEntry.COLUMN_USER_CREDIT                + " INTEGER NOT NULL, "             +
                        "UNIQUE (" + UserEntry.COLUMN_USER_EMAIL +
                        ") ON CONFLICT REPLACE" + ");";

        db.execSQL(SQL_CREATE_SPAWN_TABLE);
        db.execSQL(SQL_CREATE_GIVE_TABLE);
        db.execSQL(SQL_CREATE_RECORD_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    /**
     * Removes and creates a new table on version upgrades.
     */
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + CompanyEntry.TABLE_NAME_SPAWN);
        db.execSQL("DROP TABLE IF EXISTS " + CompanyEntry.TABLE_NAME_TARGET);
        db.execSQL("DROP TABLE IF EXISTS " + CompanyEntry.TABLE_NAME_RECORD);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME_USER);
        onCreate(db);
    }
}
