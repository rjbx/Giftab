package com.github.rjbx.givetrack.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;

import timber.log.Timber;

/**
 * Provides data in response to requests generated from {@link android.content.ContentResolver}.
 */
public class GivetrackProvider extends ContentProvider {

    private static final int CODE_COLLECTION = 100;
    private static final int CODE_COLLECTION_WITH_ID = 200;
    private static final int CODE_GENERATION = 101;
    private static final int CODE_GENERATION_WITH_ID = 201;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    
    private GivetrackOpener mOpenHelper;

    /**
     * Builds a {@link UriMatcher} for identifying distinct {@link Uri} and defining corresponding behaviors.
     * @return {@link UriMatcher}
     */
    private static UriMatcher buildUriMatcher() {
        
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GivetrackContract.AUTHORITY;

        matcher.addURI(authority, GivetrackContract.PATH_COLLECTION_TABLE, CODE_COLLECTION);
        matcher.addURI(authority, GivetrackContract.PATH_GENERATION_TABLE, CODE_GENERATION);
        matcher.addURI(authority, GivetrackContract.PATH_COLLECTION_TABLE + "/#", CODE_COLLECTION_WITH_ID);
        matcher.addURI(authority, GivetrackContract.PATH_GENERATION_TABLE + "/#", CODE_GENERATION_WITH_ID);

        return matcher;
    }

    /**
     * Initializes all registered {@link ContentProvider}s on the application glance thread at launch time.
     * @return {@code true} if the provider was successfully loaded; {@code false} otherwise.
     */
    @Override public boolean onCreate() {
        Context context = getContext();
        if (context == null) return false;
        mOpenHelper = new GivetrackOpener(context);
        return true;
    }

    /**
     * Inserts rows of data at a given {@link Uri}.
     * @param uri       {@link Uri} at which to insert data.
     * @param values    An array of sets of column_name/value pairs to add to the database;
     *                  cannot be {@code null}.
     * @return          Number of rows inserted.
     */
    @Override public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        
        switch (sUriMatcher.match(uri)) {
            case CODE_COLLECTION: tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION; break;
            case CODE_GENERATION: tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION; break;
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsInserted = 0;
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) rowsInserted++;
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }

        notifyResolverOfChange(uri, rowsInserted);
        return rowsInserted;
    }

    /**
     * Inserts a single row of data at a given {@link Uri}.
     * @param uri    {@link Uri} at which to insert data.
     * @param values Set of column_name/value pairs to add to the database; cannot be {@code null}.
     * @return       {@link Uri} of the inserted row.
     */
    @Override public Uri insert(@NonNull Uri uri, ContentValues values) {
        
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case CODE_COLLECTION: tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION; break;
            case CODE_GENERATION: tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION; break;
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsInserted = 0;
        db.beginTransaction();
        try {
            long _id = db.insert(tableName, null, values);
            if (_id != -1) rowsInserted++;
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }

        notifyResolverOfChange(uri, rowsInserted);
        return uri;
    }

    /**
     * Updates data at a given {@link Uri} with optional arguments.
     * @param uri           {@link Uri} at which to query data.
     * @param values        Set of column_name/value pairs with which to update the database.
     *                      Cannot be {@code null}.
     * @param selection     Optional statement defining criteria parameters
     * @param selectionArgs Optional criteria values
     * @return              Number of rows updated
     */
    @Override public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;

        String Id = uri.getLastPathSegment();

        switch (sUriMatcher.match(uri)) {
            case CODE_COLLECTION: tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION; break;
            case CODE_GENERATION: tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION; break;

            case CODE_COLLECTION_WITH_ID:
                selection = GivetrackContract.Entry.COLUMN_EIN + " = ? ";
                selectionArgs = new String[]{ Id };
                tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION;
                break;

            case CODE_GENERATION_WITH_ID:
                selection = GivetrackContract.Entry.COLUMN_EIN + " = ? ";
                selectionArgs = new String[]{ Id };
                tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION;
                break;
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsUpdated;
        db.beginTransaction();
        try {
            rowsUpdated = db.update(tableName, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }

        notifyResolverOfChange(uri, rowsUpdated);
        return rowsUpdated;
    }

    /**
     * Handles from clients their query requests with and without ID.
     * @param uri           {@link Uri} at which to query data
     * @param projection    list of columns to put into the cursor, or all columns if null is given
     * @param selection     Optional statement defining criteria parameters
     * @param selectionArgs Optional criteria values
     * @param sortOrder     How the rows in the cursor should be filtered
     * @return              {@link Cursor} containing the results of the query
     */
    @Override public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String tableName;

        String Id = uri.getLastPathSegment();

        switch (sUriMatcher.match(uri)) {

            case CODE_COLLECTION: tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION; break;
            case CODE_GENERATION: tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION; break;

            case CODE_COLLECTION_WITH_ID:
                selection = GivetrackContract.Entry.COLUMN_EIN + " = ? ";
                selectionArgs = new String[]{ Id };
                tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION;
                break;

            case CODE_GENERATION_WITH_ID:
                selection = GivetrackContract.Entry.COLUMN_EIN + " = ? ";
                selectionArgs = new String[]{ Id };
                tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION;
                break;

            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Cursor cursor;
        cursor = db.query(
                tableName, projection, selection, selectionArgs, null, null, sortOrder);

        Context context = getContext();
        if (context != null) cursor.setNotificationUri(context.getContentResolver(), uri);

        return cursor;
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine-tuned deletions.
     * @param uri           {@link Uri} at which to delete data.
     * @param selection     Optional statement defining criteria parameters
     * @param selectionArgs Optional criteria values
     * @return              Number of rows deleted
     */
    @Override public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;

        if (null == selection) selection = "1";
        String Id = uri.getLastPathSegment();

        switch (sUriMatcher.match(uri)) {
            case CODE_COLLECTION: tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION; break;
            case CODE_GENERATION: tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION; break;

            case CODE_COLLECTION_WITH_ID:
                selection = GivetrackContract.Entry.COLUMN_EIN + " = ? ";
                selectionArgs = new String[]{ Id };
                tableName = GivetrackContract.Entry.TABLE_NAME_COLLECTION; break;

            case CODE_GENERATION_WITH_ID:
                selection = GivetrackContract.Entry.COLUMN_EIN + " = ? ";
                selectionArgs = new String[]{ Id };
                tableName = GivetrackContract.Entry.TABLE_NAME_GENERATION; break;

            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsDeleted;
        db.beginTransaction();
        try {
            rowsDeleted = db.delete(tableName, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }

        notifyResolverOfChange(uri, rowsDeleted);
        return rowsDeleted;
    }

    /**
     * Retrieves a MIME type of the data at the given {@link Uri}.
     * @param uri address of single or multiple items
     * @return MIME type
     */
    @Override public String getType(@NonNull Uri uri) {
        String tableName = uri.getPathSegments().get(0);
        String lastPath = uri.getLastPathSegment();
        if (lastPath == null) {
            Timber.e("%s is not a valid Uri type", uri);
            throw new IllegalArgumentException();
        }
        String rowsSpecifier = uri.getLastPathSegment().matches("\\d+") ? "item" : "dir";
        return "vnd.android.cursor." + rowsSpecifier + "/vnd.movieglance." + tableName;
    }

    /**
     * Assists with {@link ContentProvider} cleanup during unit tests when automatic shutdown is disabled.
     * Prevents multiple {@link ContentProvider} instances resolving to the same underlying data.
     */
    @Override @TargetApi(11) public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    /**
     * Notifies {@link android.content.ContentResolver} of changes at {@link Uri};
     * initiates data reload with {@link androidx.loader.app.LoaderManager.LoaderCallbacks}.
     */
    private void notifyResolverOfChange(Uri uri, int rowsChanged) {
        Context context = getContext();
        if (context != null && rowsChanged > 0) context.getContentResolver().notifyChange(uri, null);
    }
}