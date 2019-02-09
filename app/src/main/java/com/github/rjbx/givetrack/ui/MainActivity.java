package com.github.rjbx.givetrack.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.core.view.GravityCompat;

import android.preference.PreferenceActivity;
import android.widget.DatePicker;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.BindView;
import butterknife.OnClick;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.DatabaseService;

/**
 * Provides the main screen for this application.
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        DialogInterface.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    public static final String ACTION_CUSTOM_TABS = "com.github.rjbx.givetrack.ui.action.CUSTOM_TABS";
    public static final String ACTION_MAIN_INTENT = "com.github.rjbx.givetrack.ui.action.MAIN_INTENT";

    public static final String ARGS_GIVING_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.GIVING_ATTRIBUTES";
    public static final String ARGS_RECORD_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.RECORD_ATTRIBUTES";

    private static final String STATE_RECORD_ARRAY = "com.github.rjbx.givetrack.ui.state.RECORD_ARRAY";
    private static final String STATE_GIVING_ARRAY = "com.github.rjbx.givetrack.ui.state.GIVING_ARRAY";
    private SectionsPagerAdapter mPagerAdapter;
    private ContentValues[] mGivingArray;
    private ContentValues[] mRecordArray;
    private long mAnchorTime;
    private int mDateDifference;
    private AlertDialog mAnchorDialog;
    @BindView(R.id.main_navigation) NavigationView mNavigation;
    @BindView(R.id.main_drawer) DrawerLayout mDrawer;
    @BindView(R.id.main_toolbar) Toolbar mToolbar;
    @BindView(R.id.main_pager) ViewPager mPager;
    @BindView(R.id.main_tabs) TabLayout mTabs;

    /**
     * Populates the TabLayout with a SectionsPagerAdapter and the NavigationView with a DrawerLayout.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        if (savedInstanceState != null) {
            mGivingArray = (ContentValues[]) savedInstanceState.getParcelableArray(STATE_GIVING_ARRAY);
            mRecordArray = (ContentValues[]) savedInstanceState.getParcelableArray(STATE_RECORD_ARRAY);
        }

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
        mTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));
        if (mPagerAdapter == null) mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigation.setNavigationItemSelectedListener(this);
        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_GIVING, null, this);
        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_RECORD, null, this);
    }

    /**
     * Closes an open NavigationDrawer when back navigating.
     */
    @Override public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else super.onBackPressed();
    }

    /**
     * Generates an options Menu.
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                launchTunerPreferences(this);
                return true;
            case R.id.action_date:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(UserPreferences.getAnchor(this));
                DatePickerDialog datePicker = new DatePickerDialog(
                        this,
                        this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
                break;
            case R.id.action_add: startActivity(new Intent(this, SearchActivity.class)); break;
            case R.id.action_history: startActivity(new Intent(this, RecordActivity.class));
            default: return super.onOptionsItemSelected(item);
        } return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArray(STATE_GIVING_ARRAY, mGivingArray);
        outState.putParcelableArray(STATE_RECORD_ARRAY, mRecordArray);
        super.onSaveInstanceState(outState);
    }

    /**
     * Instantiates and returns a new {@link Loader} for the given ID.
     */
    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        switch (id) {
            case DatabaseContract.LOADER_ID_GIVING:
                return new CursorLoader(this, DatabaseContract.Entry.CONTENT_URI_GIVING,
                        null, null, null, null);
            case DatabaseContract.LOADER_ID_RECORD:
                String sort = getResources().getStringArray(R.array.list_preference_sortRecord_values)[1];
                String order = getResources().getStringArray(R.array.list_preference_orderRecord_values)[0];
                String sortOrder = String.format("%s %s", sort, order);
                Uri ratingUri = DatabaseContract.Entry.CONTENT_URI_RECORD;
                return new CursorLoader(
                        this, ratingUri,
                        null, null, null, sortOrder);
            default: throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case DatabaseContract.LOADER_ID_GIVING:
                mGivingArray = new ContentValues[cursor.getCount()];
                if (cursor.moveToFirst()) {
                    int i = 0;
                    do {
                        ContentValues values = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor, values);
                        mGivingArray[i++] = values;
                    } while (cursor.moveToNext());
                }
                new StatusAsyncTask(this).execute(mGivingArray);
                break;
            case DatabaseContract.LOADER_ID_RECORD:
                mRecordArray = new ContentValues[cursor.getCount()];
                if (cursor.moveToFirst()) {
                    int i = 0;
                    do {
                        ContentValues values = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor, values);
                        mRecordArray[i++] = values;
                    } while (cursor.moveToNext());
                }
                break;
        }
        if (mGivingArray != null && mRecordArray != null) {
            Intent intent = getIntent();
            if (intent.getAction() == null || !intent.getAction().equals(ACTION_CUSTOM_TABS)) {
                mPagerAdapter.notifyDataSetChanged();
            }
            else intent.setAction(null);
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mGivingArray = null;
        mRecordArray = null;
    }

    /**
     * Defines behavior onClick of each Navigation MenuItem.
     */
    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case (R.id.nav_search): startActivity(new Intent(this, SearchActivity.class)); break;
            case (R.id.nav_settings): startActivity(new Intent(this, ConfigActivity.class).setAction(ACTION_MAIN_INTENT)); break;
            case (R.id.nav_logout): startActivity(new Intent(this, AuthActivity.class).setAction(AuthActivity.ACTION_SIGN_OUT)); break;
            case (R.id.nav_cn): launchCustomTabs(getString(R.string.url_cn)); break;
            case (R.id.nav_clearbit): launchCustomTabs(getString(R.string.url_clearbit)); break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Updates the DatePicker with the date selected from the Dialog.
     */
    @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        mAnchorTime = calendar.getTimeInMillis();
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
        dateFormatter.setTimeZone(TimeZone.getDefault());
        String formattedDate = dateFormatter.format(mAnchorTime);
        
        mDateDifference = calendar.compareTo(Calendar.getInstance());
        String qualifier = mDateDifference < 2 ? "" : "past ";

        mAnchorDialog = new AlertDialog.Builder(this).create();
        mAnchorDialog.setMessage(getString(R.string.anchor_dialog_message, formattedDate, qualifier));
        mAnchorDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mAnchorDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mAnchorDialog.show();
        mAnchorDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
        mAnchorDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override public void onClick(DialogInterface dialog, int which) {
        AlertDialog currentDialog = new AlertDialog.Builder(this).create();
        if (dialog == mAnchorDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    UserPreferences.setAnchor(this, mAnchorTime);
                    if (mDateDifference < 2) {
                        currentDialog.setMessage(getString(R.string.historical_dialog_message));
                        currentDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                        currentDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_change), this);
                        currentDialog.show();
                        currentDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                        currentDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
                    } else UserPreferences.setHistorical(this, true);
                    UserPreferences.updateFirebaseUser(this);
                    break;
                default:
            }
        } else if (dialog == currentDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    UserPreferences.setHistorical(this, true);
                    UserPreferences.updateFirebaseUser(this);
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    UserPreferences.setHistorical(this, false);
                    UserPreferences.updateFirebaseUser(this);
                    break;
                default:
            }
        }
    }
    
    /**
     * Defines and launches Intent for displaying the {@link ConfigActivity.GivingPreferenceFragment} screen.
     */
    private static void launchTunerPreferences(Context context) {
        Intent filterIntent = new Intent(context, ConfigActivity.class);
        filterIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ConfigActivity.GivingPreferenceFragment.class.getName());
        filterIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        filterIntent.setAction(ACTION_MAIN_INTENT);
        context.startActivity(filterIntent);
    }

    /**
     * Defines and launches {@link CustomTabsIntent} for displaying an integrated browser at the given URL.
     */
    private void launchCustomTabs(String url) {
        new CustomTabsIntent.Builder()
                .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                .build()
                .launchUrl(this, Uri.parse(url));
        getIntent().setAction(ACTION_CUSTOM_TABS);
    }

    /**
     * Provides logic and views for a default screen when others are unavailable.
     */
    public static class PlaceholderFragment extends Fragment {

        private Unbinder mUnbinder;

        /**
         * Provides default constructor required for the {@link FragmentManager}
         * to instantiate this Fragment.
         */
        public PlaceholderFragment() {}

        /**
         * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
         */
        static PlaceholderFragment newInstance(@Nullable Bundle args) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            if (args != null) fragment.setArguments(args);
            return fragment;
        }

        /**
         * Generates a Layout for the Fragment.
         */
        @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            mUnbinder = ButterKnife.bind(this, rootView);
            return rootView;
        }

        /**
         * Unbinds Butterknife from this Fragment.
         */
        @Override public void onDestroy() {
            super.onDestroy();
            mUnbinder.unbind();
        }

        /**
         * Defines behavior on click of launch search button.
         */
        @OnClick(R.id.placeholder_button) void launchSearch()  { startActivity(new Intent(getActivity(), SearchActivity.class)); }
    }

    /**
     * Returns a Fragment corresponding to a section.
     */
    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Constructs this instance from a {@link FragmentManager}.
         */
        private SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        /**
         * Instantiates the Fragment for a given section.
         */
        @Override public Fragment getItem(int position) {

            if (mGivingArray == null || mGivingArray.length == 0) return PlaceholderFragment.newInstance(null);
            else {
                Bundle argsGiving = new Bundle();
                Bundle argsRecord = new Bundle();
                argsGiving.putParcelableArray(ARGS_GIVING_ATTRIBUTES, mGivingArray);
                argsRecord.putParcelableArray(ARGS_RECORD_ATTRIBUTES, mRecordArray);
                switch (position) {
                    case 0: return GivingFragment.newInstance(argsGiving);
                    case 1: return GlanceFragment.newInstance(argsRecord);
                    default: return PlaceholderFragment.newInstance(null);
                }
            }
        }

        /**
         * Defines the number of sections.
         */
        @Override public int getCount() { return 2; }

        /**
         * Forces pager adapter to recreate Fragments on calls to {@link #notifyDataSetChanged()}
         */
        @Override public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        /**
         * Prevents persisting child past parent when navigating away from Fragment
         */
        @Override public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (object instanceof DetailFragment.MasterDetailFlow
                    && ((DetailFragment.MasterDetailFlow) object).isDualPane())
                ((DetailFragment.MasterDetailFlow) object).showSinglePane();
            super.destroyItem(container, position, object);
        }
    }

    /**
     * Confirms whether item exists in collection table and updates status accordingly.
     */
    private static class StatusAsyncTask extends AsyncTask<ContentValues[], Void, Boolean> {

        private WeakReference<MainActivity> mActivity;

        /**
         * Constructs an instance with a Fragment that is converted to a {@link WeakReference} in order
         * to prevent memory leak.
         */
        StatusAsyncTask(MainActivity mainActivity) {
            mActivity = new WeakReference<>(mainActivity);
        }

        /**
         * Retrieves the item collection status.
         */
        @Override protected Boolean doInBackground(ContentValues[]... valueArrays) {

            Context context = mActivity.get().getBaseContext();
            List<String> charities = UserPreferences.getCharities(context);
            List<String> eins = new ArrayList<>();
            for (String charity : charities) eins.add(charity.split(":")[0]);
            if (context == null) return false;
            ContentValues[] valuesArray = valueArrays[0];
            if (valuesArray == null || valuesArray.length == 0) return false;
            Boolean isCurrent = true;
            for (ContentValues values : valuesArray) {
               isCurrent = eins.contains(values.getAsString(DatabaseContract.Entry.COLUMN_EIN));
            }
            if (!isCurrent) DatabaseService.startActionFetchGiving(mActivity.get());
            return isCurrent;
        }

        /**
         * Updates the Fragment field corresponding to the item collection status.
         */
        @Override protected void onPostExecute(Boolean isCurrent) {
            if (!isCurrent) {
                mActivity.get().mPagerAdapter.notifyDataSetChanged();
            }
        }
    }
}