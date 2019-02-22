package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;

public interface Entry {

    ContentValues toContentValues();
    void fromContentValues(ContentValues values);
}
