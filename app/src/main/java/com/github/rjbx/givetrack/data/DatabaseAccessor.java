package com.github.rjbx.givetrack.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Search;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;


// TODO: Remotely persist removals
public final class DatabaseAccessor {

    static void fetchSearch(Context context, Map<String, Object> request) {

    }

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

    static void fetchGiving(Context context) {
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        context.getContentResolver().delete(contentUri, null, null);
        // TODO: Convert to helper method
        FirebaseDatabase
                .getInstance()
                .getReference("giving")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            Giving giving = iterator.next().getValue(Giving.class);
                            context.getContentResolver().insert(contentUri, giving.toContentValues());
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

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
        addLocalToRemoteEntries(FirebaseDatabase.getInstance(), Giving.class, entries);
    }

    static void removeGiving(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(contentUri, null, null);
    }

    static void fetchRecord(Context context) {}

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
        addLocalToRemoteEntries(FirebaseDatabase.getInstance(), Record.class, entries);
    }

    static void removeRecord(Context context, @Nullable String id) {
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        context.getContentResolver().delete(contentUri, null, null);
    }

    static void fetchUser(Context context) {
        // TODO if (entries.isEmpty()) User user = convertFirebaseUser(); addUser(context, user.getUid());
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
        addLocalToRemoteEntries(FirebaseDatabase.getInstance(), User.class, entries);
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

    // TODO: Handle multiple entries with single update
    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    public static <T extends Entry> /*Task<Void>*/void addLocalToRemoteEntries(FirebaseDatabase remote, Class<T> entryType, T... entries) {

        String path = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(path);

        if (entries.length == 1) {
            T entry = entries[0];
            pathReference.child(entry.getUid()).updateChildren(entry.toParameterMap());
        } else {
            Map<String, Object> entryMap = new HashMap<>();
            for (T entry: entries) {
//                userMap.put(entry.getUid(), entry);
                pathReference.child(entry.getUid()).updateChildren(entry.toParameterMap());
            }
//            reference.updateChildren(entryMap);
        }
    }

    public static <T extends Entry> void pullRemoteToLocalEntries(ContentResolver local, Class<T> entryType) {

        Uri uri = DatabaseContract.getContentUri(entryType);
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        String path = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(path);

        pathReference.addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            Giving giving = iterator.next().getValue(Giving.class);
                            local.insert(uri, giving.toContentValues());
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    /**
     * Generates a {@link User} from {@link SharedPreferences} and {@link FirebaseUser} attributes.
     */
    public static User convertRemoteToLocalUser(FirebaseUser firebaseUser) {

        User user = User.getDefault();
        user.setUid(firebaseUser == null ? "" : firebaseUser.getUid());
        user.setEmail(firebaseUser == null ? "" : firebaseUser.getEmail());
        user.setActive(true);
        return user;
    }
}