package com.github.rjbx.givetrack.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract.*;
import com.github.rjbx.givetrack.data.DatasourceContract.*;
import com.github.rjbx.givetrack.data.entry.Company;
import com.github.rjbx.givetrack.data.entry.Entry;
import com.github.rjbx.givetrack.data.entry.Spawn;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import timber.log.Timber;

/**
 * Accesses and simultaneously operates on local and remote databases to manage user requests.
 */
public final class DatabaseAccessor {

    static void fetchSpawn(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        User user =  getActiveUserFromLocal(local);
        
        Map<String, String> request = new HashMap<>();
        if (user.getIndexFocus()) request.put(DatasourceContract.PARAM_EIN, user.getIndexCompany());
        else {
            request.put(DatasourceContract.PARAM_SPAWN, user.getIndexTerm());
            request.put(DatasourceContract.PARAM_CITY, user.getIndexCity());
            request.put(DatasourceContract.PARAM_STATE, user.getIndexState());
            request.put(DatasourceContract.PARAM_ZIP, user.getIndexZip());
            request.put(DatasourceContract.PARAM_MIN_RATING, user.getIndexMinrating());
            request.put(DatasourceContract.PARAM_FILTER, user.getIndexFilter() ? "1" : "0");
            request.put(DatasourceContract.PARAM_SORT, user.getIndexSort() + ":" + user.getIndexOrder());
            request.put(DatasourceContract.PARAM_PAGE_NUM, user.getIndexPages());
            request.put(DatasourceContract.PARAM_PAGE_SIZE, user.getIndexRows());
        }

        Uri.Builder builder = Uri.parse(DatasourceContract.BASE_URL).buildUpon();
        builder.appendPath(DatasourceContract.API_PATH_ORGANIZATIONS);

        // Append required parameters
        builder.appendQueryParameter(DatasourceContract.PARAM_APP_ID, context.getString(R.string.cn_app_id));
        builder.appendQueryParameter(DatasourceContract.PARAM_APP_KEY, context.getString(R.string.cn_app_key));

        boolean single = request.containsKey(DatasourceContract.PARAM_EIN);
        if (single) builder.appendPath((String) request.get(DatasourceContract.PARAM_EIN));
        else {
            // Append optional parameters
            for (String param : DatasourceContract.OPTIONAL_PARAMS) {
                if (request.containsKey(param)) {
                    String value = (String) request.get(param);
                    if (value != null && !value.equals(""))
                        builder.appendQueryParameter(param, value);
                }
            }
        }
        URL url = DataUtilities.getUrl(builder.build());

        // Retrieve data
        String response = DataUtilities.requestResponseFromUrl(url, null);
        if (response == null) return;
        Spawn[] parsedResponse = DataUtilities.parseSpawns(response, user.getUid(), single);

        // Store data
        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, Spawn.class, stamp);
        addEntriesToLocal(local, Spawn.class, stamp, false, parsedResponse);
        addEntriesToRemote(remote, Spawn.class, stamp, false, parsedResponse);
    }

    @SafeVarargs static List<Spawn> getSpawn(Context context, Pair<String, String>... where) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = CompanyEntry.CONTENT_URI_SPAWN;

        String selection = CompanyEntry.COLUMN_UID + " = ? ";
        List<String> selectionArgList = new ArrayList<>();
        selectionArgList.add(activeUser.getUid());

        if (where != null) {
            for (Pair<String, String> p : where) {
                selection = selection.concat(", " + p.first);
                selectionArgList.add(p.second);
            }
        }
        String[] selectionArgs = selectionArgList.toArray(new String[0]);

        Cursor cursor = local.query(
                contentUri, null, selection, selectionArgs, null
        );
        List<Spawn> entries = AppUtilities.getEntryListFromCursor(cursor, Spawn.class);
        if (cursor != null) cursor.close();
        return entries;
    }

    static void addSpawn(Context context, Spawn... entries) {
        ContentResolver local = context.getContentResolver();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, Spawn.class, stamp, false, entries);
    }

    static void removeSpawn(Context context, Spawn... spawns) {
        ContentResolver local = context.getContentResolver();

        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, Spawn.class, stamp, spawns);
    }

    static void fetchTarget(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        validateEntries(local, remote, Target.class);
    }

    @SafeVarargs static List<Target> getTarget(Context context, Pair<String, String>... where) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = CompanyEntry.CONTENT_URI_TARGET;

        String selection = CompanyEntry.COLUMN_UID + " = ? ";
        List<String> selectionArgList = new ArrayList<>();
        selectionArgList.add(activeUser.getUid());

        if (where != null) {
            for (Pair<String, String> p : where) {
                selection = selection.concat("AND " + p.first);
                selectionArgList.add(p.second);
            }
        }
        String[] selectionArgs = selectionArgList.toArray(new String[0]);

        Cursor cursor = local.query(
                contentUri, null, selection, selectionArgs, null
        );
        List<Target> entries = AppUtilities.getEntryListFromCursor(cursor, Target.class);
        if (cursor != null) cursor.close();
        return entries;
    }

    static void addTarget(Context context, Target... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, Target.class, stamp, false, entries);
        addEntriesToRemote(remote, Target.class, stamp, false, entries);
    }

    static void removeTarget(Context context, Target... target) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();

        // TODO Update recalibration with Rateraid
        if (target != null && target.length > 0) {
            List<Target> targetList = getTarget(context);
            int[] removalIndeces = new int[targetList.size()];
            for (Target t1 : target)
                for (Target t2 : targetList)
                    if (t2.getId().equals(t1.getId()))
                        removalIndeces[targetList.indexOf(t2)] = 1;
            for (int i = 0; i < removalIndeces.length; i++)
                if (removalIndeces[i] == 1) targetList.remove(i);

            if (!targetList.isEmpty()) {
                Iterator<Target> iterator = targetList.iterator();
                do iterator.next().setPercent(1d / targetList.size());
                while (iterator.hasNext());
            }
            target = targetList.toArray(new Target[0]);
        }

        addEntriesToLocal(local, Target.class, stamp, true, target);
        addEntriesToRemote(remote, Target.class, stamp, true, target);
    }

    static void fetchRecord(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        validateEntries(local, remote, Record.class);
    }

    @SafeVarargs static List<Record> getRecord(Context context, Pair<String, String>... where) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = CompanyEntry.CONTENT_URI_RECORD;

        String selection = CompanyEntry.COLUMN_UID + " = ? ";
        List<String> selectionArgList = new ArrayList<>();
        selectionArgList.add(activeUser.getUid());

        if (where != null) {
            for (Pair<String, String> p : where) {
                selection = selection.concat(", " + p.first);
                selectionArgList.add(p.second);
            }
        }
        String[] selectionArgs = selectionArgList.toArray(new String[0]);

        Cursor cursor = local.query(
                contentUri, null, selection, selectionArgs, null
        );
        List<Record> entries = AppUtilities.getEntryListFromCursor(cursor, Record.class);

        if (cursor != null) cursor.close();
        return entries;
    }

    static void addRecord(Context context, Record... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, Record.class, stamp, false, entries);
        addEntriesToRemote(remote, Record.class, stamp, false, entries);
    }

    static void removeRecord(Context context, Record... record) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, Record.class, stamp, record);
        removeEntriesFromRemote(remote, Record.class, stamp,  record);
    }

    static void fetchUser(Context context) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        validateEntries(local, remote, User.class);
    }

    static List<User> getUser(Context context) {
        ContentResolver local = context.getContentResolver();

        User activeUser = getActiveUserFromLocal(local);
        Uri contentUri = UserEntry.CONTENT_URI_USER;
        Cursor cursor = local.query(
                contentUri, null, CompanyEntry.COLUMN_UID + " = ? ", new String[] { activeUser.getUid() }, null
        );
        List<User> entries = AppUtilities.getEntryListFromCursor(cursor, User.class);
        if (cursor != null) cursor.close();
        return entries;
    }

    static void addUser(Context context, User... entries) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        addEntriesToLocal(local, User.class, stamp, false, entries);
        addEntriesToRemote(remote, User.class, stamp, false, entries);
    }

    static void removeUser(Context context, User... user) {
        ContentResolver local = context.getContentResolver();
        FirebaseDatabase remote = FirebaseDatabase.getInstance();

        long stamp = System.currentTimeMillis();
        removeEntriesFromLocal(local, User.class, stamp, user);
        removeEntriesFromRemote(remote, User.class, stamp, user);
    }

    @SafeVarargs private static <T extends Entry> void addEntriesToLocal(ContentResolver local, Class<T> entryType, long stamp, boolean reset, T... entries) {

        Uri contentUri = DataUtilities.getContentUri(entryType);

        String uid =  entries == null || entries.length == 0 ?
                getActiveUserFromLocal(local).getUid() : entries[0].getUid();

        if (reset) local.delete(contentUri, UserEntry.COLUMN_UID + " = ? ", new String[] { uid });

        if (entries != null && entries.length > 0) {
            ContentValues[] values = new ContentValues[entries.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = entries[i].toContentValues();
            }
            local.bulkInsert(contentUri, values);
        }

        updateLocalTableTime(local, entryType, stamp, uid);
    }

    @SafeVarargs private static <T extends Entry> Task addEntriesToRemote(FirebaseDatabase remote, Class<T> entryType, long stamp,  boolean reset, T... entries) {

        Task<Void> task = null;
        if (entryType.equals(Spawn.class)) return null;

        String entryPath = entryType.getSimpleName().toLowerCase();
        DatabaseReference entryReference = remote.getReference(entryPath);

        String uid = entries == null || entries.length == 0 ?
                getActiveUserFromRemote(remote).getUid() : entries[0].getUid();

        DatabaseReference childReference = entryReference.child(uid);

        if (reset) task = childReference.removeValue();

        if (entries != null && entries.length > 0) {
            for (T entry : entries) {
                if (entry instanceof Company) childReference = childReference.child(entry.getId());
                task = childReference.updateChildren(entry.toParameterMap());
            }
        }

        updateRemoteTableTime(remote, entryType, stamp, uid);

        return task;
    }

    @SafeVarargs private static <T extends Entry> void removeEntriesFromLocal(ContentResolver local, Class<T> entryType, long stamp, T... entries) {

        Uri contentUri = DataUtilities.getContentUri(entryType);
        String uid;
        if (entries == null || entries.length == 0) {
            uid = getActiveUserFromLocal(local).getUid();
            local.delete(contentUri, UserEntry.COLUMN_UID + " = ?", new String[] { uid });
        } else {
            uid = entries[0].getUid();
            for (Entry entry : entries) {
                Uri rowUri = contentUri.buildUpon().appendPath(String.valueOf(entry.getId())).build();
                local.delete(rowUri, null, null);
            }
        } updateLocalTableTime(local, entryType, stamp, uid);
    }

    @SafeVarargs private static <T extends Entry> Task removeEntriesFromRemote(FirebaseDatabase remote, Class<T> entryType, long stamp, T... entries) {

        Task<Void> task = null;
        if (entryType.equals(Spawn.class)) return null;

        String entryPath = entryType.getSimpleName().toLowerCase();
        DatabaseReference entryReference = remote.getReference(entryPath);
        String uid;
        if (entries == null || entries.length == 0) {
            User user = getActiveUserFromRemote(remote);
            uid = user.getUid();
            DatabaseReference childReference = entryReference.child(uid);
            task = childReference.removeValue();
        } else {
            uid = entries[0].getUid();
            for (T entry : entries) {
                DatabaseReference childReference = entryReference.child(uid);
                if (entry instanceof Company) childReference = childReference.child(entry.getId());
                task = childReference.removeValue();
            }
        } updateRemoteTableTime(remote, entryType, stamp, uid);
        return task;
    }

    private static User getActiveUserFromLocal(ContentResolver local) {
        User u = User.getDefault();
        u.setUserStamp(-1);
        u.setTargetStamp(-1);
        u.setRecordStamp(-1);
        Cursor data = local.query(UserEntry.CONTENT_URI_USER, null, null, null, null);
        if (data == null) return u;
        if (data.moveToFirst()) {
            do {
                User user = User.getDefault();
                AppUtilities.cursorRowToEntry(data, user);
                if (user.getUserActive()) return user;
            } while (data.moveToNext());
        } return u;
    }

    private static User getActiveUserFromRemote(FirebaseDatabase remote) {

        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();

        DatabaseReference entryReference = remote.getReference(User.class.getSimpleName().toLowerCase());
        entryReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                for (DataSnapshot snapshot : iterable) {
                    User u = snapshot.getValue(User.class);
                    if (u != null && u.getUserActive()) taskSource.trySetResult(u);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        Task<User> task = taskSource.getTask();
        try { Tasks.await(task); }
        catch (ExecutionException|InterruptedException e) { task = Tasks.forException(e); }

        User u = User.getDefault();
        u.setUserStamp(-1);
        u.setTargetStamp(-1);
        u.setRecordStamp(-1);
        if (task.isSuccessful()) u = task.getResult();
        return u;
    }

    private static <T extends Entry> void updateLocalTableTime(ContentResolver local, Class<T> entryType, long stamp, String uid) {

        if (entryType.equals(Spawn.class)) return;

        ContentValues values = new ContentValues();
        values.put(DataUtilities.getTimeTableColumn(entryType), stamp);

        Uri uri = UserEntry.CONTENT_URI_USER.buildUpon().appendPath(uid).build();
        local.update(uri, values, null,null);
    }

    private static <T extends Entry> void updateRemoteTableTime(FirebaseDatabase remote, Class<T> entryType, long stamp, String uid) {

        Map<String, Object> map = new HashMap<>();
        map.put(DataUtilities.getTimeTableColumn(entryType), stamp);

        DatabaseReference userReference = remote.getReference(User.class.getSimpleName().toLowerCase());
        userReference.child(uid).updateChildren(map);
    }

    private static <T extends Entry> void pullLocalToRemoteEntries(ContentResolver local, FirebaseDatabase remote, Class<T> entryType, long stamp) {
        Uri contentUri = DataUtilities.getContentUri(entryType);
        Cursor cursor = local.query(contentUri, null, null, null, null);
        if (cursor == null) return;
        List<T> entryList = AppUtilities.getEntryListFromCursor(cursor, entryType);
        cursor.close();
        removeEntriesFromRemote(remote, entryType, stamp);
        if (entryList.isEmpty()) return;
        addEntriesToRemote(remote, entryType, stamp, false, entryList.toArray((T[]) Array.newInstance(entryType, entryList.size())));
    }

    private static <T extends Entry> void pullRemoteToLocalEntries(ContentResolver local, FirebaseDatabase remote, Class<T> entryType, long stamp, String uid) {

        String path = entryType.getSimpleName().toLowerCase();
        DatabaseReference pathReference = remote.getReference(path);
        if (entryType == Record.class || entryType == Target.class) pathReference = pathReference.child(uid);
        pathReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                List<T> entryList = new ArrayList<>();
                for (DataSnapshot snapshot : iterable) {
                    T entry = snapshot.getValue(entryType);
                    if (entry instanceof User) {
                        ((User) entry).setRecordStamp(0);
                        ((User) entry).setTargetStamp(0);
                    }
                    entryList.add(entry);
                }
                removeEntriesFromLocal(local, entryType, stamp);
                if (entryList.isEmpty()) return;
                addEntriesToLocal(local, entryType, stamp, false, entryList.toArray((T[]) Array.newInstance(entryType, entryList.size())));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private static <T extends Entry> void validateEntries(@NonNull ContentResolver local, @NonNull FirebaseDatabase remote, Class<T> entryType) {

        User localUser = getActiveUserFromLocal(local);
        User remoteUser = getActiveUserFromRemote(remote);

        long localTableStamp = DataUtilities.getTableTime(entryType, localUser);
        long remoteTableStamp = DataUtilities.getTableTime(entryType, remoteUser);
        int compareLocalToRemote = Long.compare(localTableStamp, remoteTableStamp);

        if (compareLocalToRemote > 0) pullLocalToRemoteEntries(local, remote, entryType, localTableStamp);
        else if (compareLocalToRemote < 0) pullRemoteToLocalEntries(local, remote, entryType, remoteTableStamp, remoteUser.getUid());
        else local.notifyChange(DataUtilities.getContentUri(entryType), null);
    }
}