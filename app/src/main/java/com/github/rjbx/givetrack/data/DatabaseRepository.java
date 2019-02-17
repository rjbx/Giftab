package com.github.rjbx.givetrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.Log;

import com.github.rjbx.givetrack.data.entry.Company;

import java.util.ArrayList;
import java.util.List;

import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;

import androidx.annotation.Nullable;

public final class DatabaseRepository {

    static List<Search> getSearch(Context context, @Nullable String id) {
        Uri contentUri = Entry.CONTENT_URI_SEARCH;
        if (id != null) contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_SEARCH, null, null, null, null
        );
        List<Search> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Search());
            }
            cursorToCompanies(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addSearch(Context context, Search... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_SEARCH, values);
    }

    static void removeSearch(Context context, @Nullable String id) {
        Uri contentUri = Entry.CONTENT_URI_SEARCH;
        if (id != null) contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(Entry.CONTENT_URI_SEARCH, null, null);
    }

    static List<Giving> getGiving(Context context, @Nullable String id) {
        Uri contentUri = Entry.CONTENT_URI_GIVING;
        if (id != null) contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_GIVING, null, null, null, null
        );
        List<Giving> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Giving());
            }
            cursorToCompanies(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addGiving(Context context, Giving... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_GIVING, values);
    }

    static void removeGiving(Context context, @Nullable String id) {
        Uri contentUri = Entry.CONTENT_URI_GIVING;
        if (id != null) contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(Entry.CONTENT_URI_GIVING, null, null);
    }

    static List<Record> getRecord(Context context, @Nullable String id) {
        Uri contentUri = Entry.CONTENT_URI_RECORD;
        if (id != null) contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_RECORD, null, null, null, null
        );
        List<Record> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Record());
            }
            cursorToCompanies(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addRecord(Context context, Record... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_RECORD, values);
    }

    static void removeRecord(Context context, @Nullable String id) {
        Uri contentUri = Entry.CONTENT_URI_RECORD;
        if (id != null) contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(Entry.CONTENT_URI_RECORD, null, null);
    }

    public static <T extends Company> void cursorRowToCompany(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    public static <T extends Company> void cursorToCompanies(Cursor cursor, List<T> entries) {
        if (cursor == null || !cursor.moveToFirst()) return;
        int i = 0;
        cursor.moveToFirst();
        do cursorRowToCompany(cursor, entries.get(i++));
        while (cursor.moveToNext());
    }
}