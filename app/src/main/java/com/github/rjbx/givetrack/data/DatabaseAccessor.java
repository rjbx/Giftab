package com.github.rjbx.givetrack.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.entry.Company;
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
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Search.class);
        
        Uri contentUri = CompanyEntry.CONTENT_URI_SEARCH;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<Search> entries = getEntryListFromCursor(cursor, Search.class);
        cursor.close();
        return entries;
    }

    static void addSearch(Context context, Search... entries) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Search.class);
        
        addEntriesToLocal(local, Search.class, entries);
    }

    static void removeSearch(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Search.class);

        removeEntriesFromLocal(local, Search.class, id);
     }

    static void fetchGiving(Context context) {
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        context.getContentResolver().delete(contentUri, null, null);
        pullRemoteToLocalEntries(context.getContentResolver(), Giving.class);
    }

    static List<Giving> getGiving(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Giving.class);
        
        Uri contentUri = CompanyEntry.CONTENT_URI_GIVING;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<Giving> entries = getEntryListFromCursor(cursor, Giving.class);
        cursor.close();
        return entries;
    }

    static void addGiving(Context context, Giving... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, remote, Giving.class);
        
        addEntriesToLocal(local, Giving.class, entries);
        addEntriesToRemote(remote, Giving.class, entries);
    }

    static void removeGiving(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Giving.class);

        removeEntriesFromLocal(local, Giving.class, id);
    }

    static void fetchRecord(Context context) {
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        context.getContentResolver().delete(contentUri, null, null);
        pullRemoteToLocalEntries(context.getContentResolver(), Record.class);
    }

    static List<Record> getRecord(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Record.class);

        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<Record> entries = getEntryListFromCursor(cursor, Record.class);
        cursor.close();
        return entries;
    }

    static void addRecord(Context context, Record... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, remote, Record.class);

        addEntriesToLocal(local, Record.class, entries);
        addEntriesToRemote(remote, Record.class, entries);
    }

    static void removeRecord(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), Record.class);

        removeEntriesFromLocal(local, Record.class, id);
    }

    static void fetchUser(Context context) {
        Uri contentUri = UserEntry.CONTENT_URI_USER;
        context.getContentResolver().delete(contentUri, null, null);
        pullRemoteToLocalEntries(context.getContentResolver(), User.class);
    }

    static List<User> getUser(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), User.class);

        Uri contentUri = UserEntry.CONTENT_URI_USER;
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        Cursor cursor = local.query(
                contentUri, null, null, null, null
        );
        List<User> entries = getEntryListFromCursor(cursor, User.class);
        cursor.close();
        return entries;
    }

    static void addUser(Context context, User... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();
        validateEntries(local, remote, User.class);

        addEntriesToLocal(local, User.class, entries);
        addEntriesToRemote(remote, User.class, entries);
    }

    static void removeUser(Context context, @Nullable String id) {
        ContentResolver local = context.getContentResolver();
        validateEntries(local, FirebaseDatabase.getInstance(), User.class);

        removeEntriesFromLocal(local, User.class, id);
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

    public static <T extends Entry> void addEntriesToLocal(ContentResolver local, Class<T> entryType, T... entries) {
        ContentValues[] values = new ContentValues[entries.length];
        for (int i = 0; i < values.length; i++) {values[i] = entries[i].toContentValues(); }
        local.bulkInsert(DatabaseContract.getContentUri(entryType), values);
    }

    /**
     * Updates {@link FirebaseUser} attributes from {@link SharedPreferences}.
     */
    static <T extends Entry> /*Task<Void>*/void addEntriesToRemote(FirebaseDatabase remote, Class<T> entryType, T... entries) {

        String rootPath = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(rootPath);

        if (entries.length == 1) {
            T entry = entries[0];
            pathReference = pathReference.child(entry.getUid());
            if (entry instanceof Company) pathReference = pathReference.child((entry.getId().toString()));
            pathReference.updateChildren(entry.toParameterMap());
        } else {
            // TODO: Handle multiple entries with single update
            Map<String, Object> entryMap = new HashMap<>();
            for (T entry: entries) {
                pathReference = pathReference.child(entry.getUid());
                if (entry instanceof Company) pathReference = pathReference.child(entry.getId().toString());
                entryMap.put(entry.getId().toString(), entry);
                pathReference.updateChildren(entry.toParameterMap());
            }
//            pathReference.updateChildren(entryMap);
        }
    }

    static <T extends Entry> void removeEntriesFromLocal(ContentResolver local, Class<T> entryType, @Nullable String id) {

        Uri contentUri = DatabaseContract.getContentUri(entryType);
        if (id != null) contentUri = contentUri.buildUpon().appendPath(id).build();
        local.delete(contentUri, null, null);
    }

    static <T extends Entry> void pullRemoteToLocalEntries(ContentResolver local, Class<T> entryType) {

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

    static <T extends Entry> void validateEntries(ContentResolver local, FirebaseDatabase remote, Class<T> entryType) {

        DatabaseReference reference = remote.getReference(User.class.getSimpleName().toLowerCase());
        reference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long localUpdateTime = 0;
                long remoteUpdateTime = 0;

                Cursor cursor = local.query(UserEntry.CONTENT_URI_USER, null, null, null, null);
                if (cursor != null) {
                    List<User> localUsers = getEntryListFromCursor(cursor, User.class);
                    cursor.close();
                    for (User user : localUsers)
                        if (user.getActive())
                            localUpdateTime = DatabaseContract.getTableTime(entryType, user);
                }

                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    User user = iterator.next().getValue(User.class);
                    if (user != null && user.getActive()) remoteUpdateTime = DatabaseContract.getTableTime(entryType, user);
                    if (localUpdateTime < remoteUpdateTime) {
                        pullRemoteToLocalEntries(local, entryType);
                    } else if (localUpdateTime > remoteUpdateTime) {
                        remote.getReference(entryType.getSimpleName().toLowerCase()).removeValue();
                        cursor = local.query(DatabaseContract.getContentUri(entryType), null, null, null, null);
                        if (cursor != null) {
                            List<T> entryList = getEntryListFromCursor(cursor, entryType);
                            cursor.close();
                            T[] entries = (T[]) Array.newInstance(entryType, entryList.size());
                            for (int i = 0; i < entries.length; i++) entries[i] = entryList.get(i);
                            addEntriesToRemote(FirebaseDatabase.getInstance(), entryType, entries);
                        } else remote.getReference(entryType.getSimpleName().toLowerCase()).removeValue();
                    } else return;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
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