package com.github.rjbx.givetrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;
import com.github.rjbx.givetrack.data.entry.User;

import androidx.annotation.Nullable;

// TODO: For each getter and setter method, add remote persistence logic
public final class DatabaseAccessor {

    static List<Search> getSearch(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_SEARCH;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                contentUri, null, null, null, null
        );
        List<Search> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Search());
            }
            cursorToEntries(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addSearch(Context context, Search... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(CompanyEntry.CONTENT_URI_SEARCH, values);
    }

    static void removeSearch(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_SEARCH;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(contentUri, null, null);
    }

    static List<Giving> getGiving(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                contentUri, null, null, null, null
        );
        List<Giving> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Giving());
            }
            cursorToEntries(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addGiving(Context context, Giving... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(CompanyEntry.CONTENT_URI_GIVING, values);
    }

    static void removeGiving(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(contentUri, null, null);
    }

    static List<Record> getRecord(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                contentUri, null, null, null, null
        );
        List<Record> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new Record());
            }
            cursorToEntries(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addRecord(Context context, Record... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(CompanyEntry.CONTENT_URI_RECORD, values);
    }

    static void removeRecord(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(contentUri, null, null);
    }

    static List<User> getUser(Context context, @Nullable String id) {
        Uri contentUri = UserEntry.CONTENT_URI_USER;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                contentUri, null, null, null, null
        );
        List<User> entries = new ArrayList<>();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                entries.add(new User());
            }
            cursorToEntries(cursor, entries);
            cursor.close();
        }
        return entries;
    }

    static void addUser(Context context, User... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(UserEntry.CONTENT_URI_USER, values);
    }

    static void removeUser(Context context, @Nullable String id) {
        Uri contentUri = UserEntry.CONTENT_URI_USER;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(contentUri, null, null);
    }

    public static <T extends Entry> void cursorRowToEntry(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    public static <T extends Entry> void cursorToEntries(Cursor cursor, List<T> entries) {
        if (cursor == null || !cursor.moveToFirst()) return;
        int i = 0;
        cursor.moveToFirst();
        do cursorRowToEntry(cursor, entries.get(i++));
        while (cursor.moveToNext());
    }
}