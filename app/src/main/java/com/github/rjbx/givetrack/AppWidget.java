package com.github.rjbx.givetrack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.ui.MainActivity;
import com.github.rjbx.givetrack.ui.SearchActivity;

import java.text.NumberFormat;
import java.util.Locale;

//TODO: Populate RemoteViews
/**
 * Implements App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    /**
     * Retrieves and sets {@link PendingIntent} on {@link RemoteViews} of a single {@link AppWidget}.
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_app);

        Intent populateIntent = new Intent(context, AppWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_list, populateIntent);

        Intent listIntent = new Intent(context, MainActivity.class);
        PendingIntent listPendingIntent = PendingIntent.getActivity(context, 0, listIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list, listPendingIntent);

        Intent searchIntent = new Intent(context, SearchActivity.class);
        PendingIntent searchPendingIntent = PendingIntent.getActivity(context, 0, searchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.collection_add_button, searchPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Updates all {@link AppWidget}s detected by {@link AppWidgetManager}.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        for (int appWidgetId : appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override public void onEnabled(Context context) {}
    @Override public void onDisabled(Context context) {}

    /**
     * Generates a {@link android.widget.RemoteViewsService.RemoteViewsFactory} for populating remote collections.
     */
    public static class AppWidgetRemoteViewsService extends RemoteViewsService {
        @Override public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new AppWidgetRemoteViewsFactory(getApplicationContext());
        }
    }

    /**
     * Builds {@link RemoteViews} for populating remote collections.
     */
    public static class AppWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        Context mContext;
        Cursor mCursor;

        /**
         * Constructs an instance with the Application {@link Context} used to query the {@link android.content.ContentProvider}.
         */
        AppWidgetRemoteViewsFactory(Context context) { mContext = context; }

        /**
         * Triggered when remote collection adapter invokes notifyDataSetChanged; synchronous processing
         * does not disrupt application main thread.
         */
        @Override public void onDataSetChanged() {
            long token = Binder.clearCallingIdentity();
            if (mCursor != null) mCursor.close();
            mCursor = mContext.getContentResolver().query(DatabaseContract.Entry.CONTENT_URI_GIVING,
                    null, null, null, null);
            Binder.restoreCallingIdentity(token);
        }

        /**
         * Populates {@link RemoteViews} at each position of the remote collection.
         */
        @Override public RemoteViews getViewAt(int position) {

            if (mCursor == null || mCursor.getCount() == 0) return null;
            mCursor.moveToPosition(position);

            String name = mCursor.getString(DatabaseContract.Entry.INDEX_CHARITY_NAME);
            Float percentage = Float.parseFloat(mCursor.getString(DatabaseContract.Entry.INDEX_DONATION_PERCENTAGE));

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget);
            remoteViews.setTextViewText(R.id.widget_item_name, name);
            remoteViews.setTextViewText(R.id.widget_item_percentage, NumberFormat.getPercentInstance().format(percentage));
            remoteViews.setOnClickFillInIntent(R.id.widget_item, new Intent());

            return remoteViews;
        }

        @Override public void onCreate() {}
        @Override public void onDestroy() {}
        @Override public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }
        @Override public RemoteViews getLoadingView() {
            return null;
        }
        @Override public int getViewTypeCount() {
            return 1;
        }
        @Override public long getItemId(int position) {
            return position;
        }
        @Override public boolean hasStableIds() {
            return true;
        }
    }
}