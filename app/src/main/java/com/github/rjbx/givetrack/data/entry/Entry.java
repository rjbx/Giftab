package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;

import java.util.Map;

public interface Entry {

    ContentValues toContentValues();
    void fromContentValues(ContentValues values);
    Map<String, Object> toParameterMap();
    void fromParameterMap(Map<String, Object> map);
    String getUid();
    String getId();
    Entry getObject();
}
