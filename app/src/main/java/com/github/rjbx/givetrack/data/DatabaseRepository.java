package com.github.rjbx.givetrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.github.rjbx.givetrack.data.entry.Search;

import java.util.List;

import com.github.rjbx.givetrack.data.DatabaseContract.*;

public final class DatabaseRepository {

    static <T extends Search> void getEntries(Context context, List<T> entries) {
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_SEARCH, null, null, null, null
        );
        if (cursor != null) cursorToEntries(cursor, entries);
    }

    static <T extends Search> void setEntries(Context context, List<T> searches) {
        ContentValues[] values = new ContentValues[searches.size()];
        for (int i = 0; i < searches.size(); i++) {
            values[i] = searches.get(i).toContentValues();
        }
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_SEARCH, values);
    }

    public static <T extends Search> void cursorRowToEntry(Cursor cursor, T entry) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        entry.fromContentValues(values);
    }

    public static <T extends Search> void cursorToEntries(Cursor cursor, List<T> entries) {
        int i = 0;
        cursor.moveToFirst();
        do cursorRowToEntry(cursor, entries.get(i++));
        while (cursor.moveToNext());
    }
}