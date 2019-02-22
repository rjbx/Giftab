package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;

import com.google.firebase.database.Exclude;

import timber.log.Timber;

public interface Entry {

    ContentValues toContentValues();
    void fromContentValues(ContentValues values);
    @Exclude static <T extends Entry> T getInstance(Class<T> type) {
        T entry = null;
        try { entry = type.newInstance(); }
        catch (InstantiationException|IllegalAccessException e) { Timber.e(e); }
        return entry;
    }
}
