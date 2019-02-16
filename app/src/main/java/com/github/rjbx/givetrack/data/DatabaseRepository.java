package com.github.rjbx.givetrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.github.rjbx.givetrack.data.entry.Search;

import java.util.ArrayList;
import java.util.List;


import com.github.rjbx.givetrack.data.DatabaseContract.*;

public final class DatabaseRepository {

    static List<Search> getSearches(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Entry.CONTENT_URI_SEARCH, null, null, null, null
        );
        if (cursor == null) return null;
        return cursorToSearches(cursor);
    }

    static void setSearches(Context context, List<Search> searches) {
        ContentValues[] values = new ContentValues[searches.size()];
        for (int i = 0; i < searches.size(); i++) {
            values[i] = searches.get(i).toContentValues();
        }
        context.getContentResolver().bulkInsert(Entry.CONTENT_URI_SEARCH, values);
    }

    public static Search cursorRowToSearch(Cursor cursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        return new Search(values);
    }

    public static List<Search> cursorToSearches(Cursor cursor) {
        cursor.moveToFirst();
        List<Search> searches = new ArrayList<>();
        do searches.add(cursorRowToSearch(cursor));
        while (cursor.moveToNext());
        return searches;
    }
}