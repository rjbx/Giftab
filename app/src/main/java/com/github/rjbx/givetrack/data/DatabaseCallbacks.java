package com.github.rjbx.givetrack.data;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.github.rjbx.givetrack.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import static com.github.rjbx.givetrack.data.DatabaseContract.*;

// TODO: Handle cases based on Loader ID passed from UI classes

public class DatabaseCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private DatabaseController mController;

    public DatabaseCallbacks(DatabaseController controller) {
        mController = controller;
    }

    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Context context = mController.getBaseContext();
        switch (id) {
            case LOADER_ID_SEARCH: return new CursorLoader(context, CompanyEntry.CONTENT_URI_SEARCH, null, null, null, null);
            case LOADER_ID_GIVING: return new CursorLoader(context, CompanyEntry.CONTENT_URI_GIVING, null, null, null, null);
            case LOADER_ID_RECORD: return new CursorLoader(context, CompanyEntry.CONTENT_URI_RECORD, null, null, null, null);
            case LOADER_ID_USER: return new CursorLoader(context, UserEntry.CONTENT_URI_USER, null, null, null, null);
            default: throw new RuntimeException(context.getString(R.string.loader_error_message, id));
        }
    }

    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mController.onLoadFinished(loader.getId(), data);
    }

    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mController.onLoaderReset();
    }
}
