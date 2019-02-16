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

public final class DatabaseRepository {

    static List<Search> getSearches(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_SEARCH, null, null, null, null
        );
        List<Search> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Search());
            }
            cursorToCompanies(cursor, entries);
        }
        return entries;
    }

    static void setSearch(Context context, List<Search> entries) {
        ContentValues[] values = new ContentValues[entries.size()];
        for (int i = 0; i < entries.size(); i++) values[i] = entries.get(i).toContentValues();
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_SEARCH, values);
    }

    static List<Record> getRecords(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_RECORD, null, null, null, null
        );
        List<Record> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Record());
            }
            cursorToCompanies(cursor, entries);
        }
        return entries;
    }

    static void setRecord(Context context, List<Record> entries) {
        ContentValues[] values = new ContentValues[entries.size()];
        for (int i = 0; i < entries.size(); i++) values[i] = entries.get(i).toContentValues();
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_RECORD, values);
    }

    static List<Giving> getGivings(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_GIVING, null, null, null, null
        );
        List<Giving> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Giving());
            }
            cursorToCompanies(cursor, entries);
        }
        return entries;
    }

    static void setGiving(Context context, List<Giving> entries) {
        ContentValues[] values = new ContentValues[entries.size()];
        for (int i = 0; i < entries.size(); i++) values[i] = entries.get(i).toContentValues();
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_GIVING, values);
    }

    static <T extends Company> void cursorRowToCompany(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    static <T extends Company> void cursorToCompanies(Cursor cursor, List<T> entries) {
        if (cursor == null || !cursor.moveToFirst()) return;
        int i = 0;
        cursor.moveToFirst();
        do cursorRowToCompany(cursor, entries.get(i++));
        while (cursor.moveToNext());
    }
}