package com.github.rjbx.givetrack.data;

import android.content.Context;
import android.database.Cursor;

public interface DatabaseController {
    Context getBaseContext();
    void onLoadFinished(int id, Cursor cursor);
    void onLoaderReset();
}
