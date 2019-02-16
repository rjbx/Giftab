package com.github.rjbx.givetrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import com.github.rjbx.givetrack.data.entry.Company;
import java.util.List;

import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;

public final class DatabaseRepository {

    static <T extends Company> void getEntries(Context context, List<T> entries) {
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_SEARCH, null, null, null, null
        );
        if (cursor != null) cursorToEntries(cursor, entries);
    }

    static <T extends Company> void setEntries(Context context, List<T> companies) {
        ContentValues[] values = new ContentValues[companies.size()];
        for (int i = 0; i < companies.size(); i++) {
            values[i] = companies.get(i).toContentValues();
        }
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_SEARCH, values);
    }

    public static <T extends Company> void cursorRowToEntry(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    public static <T extends Company> void cursorToEntries(Cursor cursor, List<T> entries) {
        int i = 0;
        cursor.moveToFirst();
        do cursorRowToEntry(cursor, entries.get(i++));
        while (cursor.moveToNext());
    }

    public static <T extends Company> Uri getUriFromEntryType(Class<T> entryType) {
        if (entryType.isInstance(Search.class)) return Entry.CONTENT_URI_SEARCH;
        if (entryType.isInstance(Giving.class)) return Entry.CONTENT_URI_GIVING;
        if (entryType.isInstance(Record.class)) return Entry.CONTENT_URI_RECORD;
        return null;
    }
}