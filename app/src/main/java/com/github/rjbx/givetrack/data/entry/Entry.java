package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;

import com.google.firebase.database.Exclude;

public interface Entry {

    ContentValues toContentValues();
    void fromContentValues(ContentValues values);
    @Exclude static <T extends Entry> T getInstance() {
        return T.getInstance();
    }
}
