package com.github.rjbx.givetrack;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.rjbx.givetrack.data.DatabaseAccessor;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.ui.JournalActivity;
import com.github.rjbx.givetrack.ui.HomeActivity;
import com.github.rjbx.givetrack.ui.IndexActivity;

import static com.github.rjbx.givetrack.AppUtilities.CURRENCY_FORMATTER;
import static com.github.rjbx.givetrack.AppUtilities.PERCENT_FORMATTER;

/**
 * Provides a home screen interface that displays user data and offers navigation shortcuts.
 */
public class AppWidget extends AppWidgetProvider {

    /**
     * Retrieves and sets {@link PendingIntent} on {@link RemoteViews} of a single {@link AppWidget}.
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_app);

        Intent populateIntent = new Intent(context, AppWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_list, populateIntent);

        Intent listIntent = new Intent(context, HomeActivity.class);
        PendingIntent listPendingIntent = PendingIntent.getActivity(context, 0, listIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list, listPendingIntent);

        Intent spawnIntent = new Intent(context, IndexActivity.class);
        PendingIntent spawnPendingIntent = PendingIntent.getActivity(context, 0, spawnIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_spawn, spawnPendingIntent);

        Intent recordIntent = new Intent(context, JournalActivity.class);
        PendingIntent recordPendingIntent = PendingIntent.getActivity(context, 0, recordIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_record, recordPendingIntent);
        
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
            mCursor = mContext.getContentResolver().query(DatabaseContract.CompanyEntry.CONTENT_URI_TARGET,
                    null, null, null, null);
            Binder.restoreCallingIdentity(token);
        }

        /**
         * Populates {@link RemoteViews} at each position of the remote collection.
         */
        @Override public RemoteViews getViewAt(int position) {

            if (mCursor == null || mCursor.getCount() == 0) return null;
            mCursor.moveToPosition(position);
            Target target = Target.getDefault();
            DatabaseAccessor.cursorRowToEntry(mCursor, target);

            String name = target.getName();
            if (name.length() > 15) { name = name.substring(0, 15);
                name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }

            Float amount = Float.parseFloat(target.getImpact());
            String amountStr = CURRENCY_FORMATTER.format(amount);
            int amountLength = amountStr.length();
            if (amountLength > 12) amountStr = String.format("%s%sM", amountStr.substring(0, amountLength - 11),
                amountLength > 14 ? "" : "." + amountStr.substring(amountLength - 9, amountLength - 7));
            else if (amountLength > 6) amountStr = amountStr.substring(0, amountLength - 3);

            String percentStr = PERCENT_FORMATTER.format(target.getPercent());

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget);
            remoteViews.setTextViewText(R.id.widget_item_name, name);
            remoteViews.setTextViewText(R.id.widget_item_percent, percentStr);
            remoteViews.setTextViewText(R.id.widget_item_amount, amountStr);
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

    public static void refresh(Context context) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] ids = awm.getAppWidgetIds(new ComponentName(context, AppWidget.class));
        awm.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }
}