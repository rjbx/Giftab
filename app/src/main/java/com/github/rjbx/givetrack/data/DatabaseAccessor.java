package com.github.rjbx.givetrack.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

// TODO: For each getter and setter method, add remote persistence logic
public final class DatabaseAccessor {

    static List<Search> getSearch(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_SEARCH;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = context.getContentResolver().query(
                contentUri, null, null, null, null
        );
        List<Search> entries = getEntryListFromCursor(cursor, Search.class);
        cursor.close();
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
        List<Giving> entries = getEntryListFromCursor(cursor, Giving.class);
        cursor.close();
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
        List<Record> entries = getEntryListFromCursor(cursor, Record.class);
        cursor.close();
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
        List<User> entries = getEntryListFromCursor(cursor, User.class);
        cursor.close();
        return entries;
    }

    static void addUser(Context context, User... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < entries.length; i++) values[i] = entries[i].toContentValues();
        context.getContentResolver().bulkInsert(UserEntry.CONTENT_URI_USER, values);
        updateFirebaseUser(entries);
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

    public static <T extends Entry> List<T> getEntryListFromCursor(@NonNull Cursor cursor, Class<T> type) {
        List<T> entries = new ArrayList<>();
        if (!cursor.moveToFirst()) return entries;
        entries.clear();
        int i = 0;
        do {
            try { entries.add(type.newInstance());
            } catch (InstantiationException|IllegalAccessException e) { Timber.e(e); }
            cursorRowToEntry(cursor, entries.get(i++));
        } while (cursor.moveToNext());
        return entries;
    }

    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    public static void updateFirebaseUser(User... users) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (users.length == 1) {
            User user = users[0];
            user.setUid(firebaseUser == null ? "" : firebaseUser.getUid());
            user.setEmail(firebaseUser == null ? "" : firebaseUser.getEmail());
            firebaseDatabase.getReference("users").child(user.getUid())
                    .updateChildren(user.toParameterMap());
        } else {
            Map<String, Object> userMap = new HashMap<>();
            for (User user: users) userMap.put(user.getUid(), user);
            firebaseDatabase.getReference("users").updateChildren(userMap);
        }
    }
}