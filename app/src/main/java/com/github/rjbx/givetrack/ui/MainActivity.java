package com.github.rjbx.givetrack.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.data.DatabaseAccessor;
import com.github.rjbx.givetrack.data.entry.Giving;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseService;

import static com.github.rjbx.givetrack.AppUtilities.DATE_FORMATTER;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_GIVING;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_RECORD;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_SEARCH;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;

/**
 * Provides the main screen for this application.
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener,
        DialogInterface.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    public static final String ACTION_CUSTOM_TABS = "com.github.rjbx.givetrack.ui.action.CUSTOM_TABS";
    public static final String ACTION_MAIN_INTENT = "com.github.rjbx.givetrack.ui.action.MAIN_INTENT";

    public static final String ARGS_GIVING_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.GIVING_ATTRIBUTES";
    public static final String ARGS_RECORD_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.RECORD_ATTRIBUTES";
    public static final String ARGS_USER_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.USER_ATTRIBUTES";

    private static final String STATE_RECORD_ARRAY = "com.github.rjbx.givetrack.ui.state.RECORD_ARRAY";
    private static final String STATE_GIVING_ARRAY = "com.github.rjbx.givetrack.ui.state.GIVING_ARRAY";
    private SectionsPagerAdapter mPagerAdapter;
    private Giving[] mGivingArray;
    private Record[] mRecordArray;
    private User mUser;
    private long mAnchorTime;
    private int mDateDifference;
    private AlertDialog mAnchorDialog;
    private AlertDialog mCurrentDialog;
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
            mGivingArray = AppUtilities.getTypedArrayFromParcelables(savedInstanceState.getParcelableArray(STATE_GIVING_ARRAY), Giving.class);
            mRecordArray = AppUtilities.getTypedArrayFromParcelables(savedInstanceState.getParcelableArray(STATE_RECORD_ARRAY), Record.class);
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
        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_USER, null, this);
    }

    public Context getContext() {
        return this;
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
                AppUtilities.launchPreferenceFragment(this, mUser, ACTION_MAIN_INTENT);
                return true;
            case R.id.action_date:
                Calendar calendar = Calendar.getInstance();
                if (mUser.getHistorical()) calendar.setTimeInMillis(mUser.getAnchor());
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

    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_SEARCH: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_SEARCH, null, null, null, null);
            case LOADER_ID_GIVING: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_GIVING, null, null, null, null);
            case LOADER_ID_RECORD: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_RECORD, null, null, null, null);
            case LOADER_ID_USER: return new CursorLoader(this, DatabaseContract.UserEntry.CONTENT_URI_USER, null, null, null, null);
            default: throw new RuntimeException(this.getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case DatabaseContract.LOADER_ID_GIVING:
                mGivingArray = new Giving[data.getCount()];
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        Giving giving = new Giving();
                        DatabaseAccessor.cursorRowToEntry(data, giving);
                        mGivingArray[i++] = giving;
                    } while (data.moveToNext());
                }
//                DatabaseService.startActionFetchGiving(this);
                break;
            case DatabaseContract.LOADER_ID_RECORD:
                mRecordArray = new Record[data.getCount()];
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        Record record = new Record();
                        DatabaseAccessor.cursorRowToEntry(data, record);
                        mRecordArray[i++] = record;
                    } while (data.moveToNext());
                }
                break;
            case DatabaseContract.LOADER_ID_USER:
                if (data.moveToFirst()) {
                    do {
                        User user = User.getDefault();
                        DatabaseAccessor.cursorRowToEntry(data, user);
                        if (user.getActive())
                            mUser = user;
                        long difference = System.currentTimeMillis() - mUser.getAnchor();
                        int days = (int) TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
                        if (!mUser.getHistorical() && days != 0) {
                            mUser.setAnchor(System.currentTimeMillis());
                            DatabaseService.startActionUpdateUser(this, mUser);
                        }
                    } while (data.moveToNext());
                }
                break;
        }
        if (mGivingArray != null && mRecordArray != null && mUser != null) {
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
            case (R.id.nav_record): startActivity(new Intent(this, RecordActivity.class)); break;
            case (R.id.nav_settings): startActivity(new Intent(this, ConfigActivity.class).setAction(ACTION_MAIN_INTENT).putExtra(ConfigActivity.ARG_ITEM_USER, mUser)); break;
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
        DATE_FORMATTER.setTimeZone(TimeZone.getDefault());
        String formattedDate = DATE_FORMATTER.format(mAnchorTime);
        
        mDateDifference = calendar.compareTo(Calendar.getInstance());
        String qualifier = mDateDifference < 0 ? "" : "past ";

        mAnchorDialog = new AlertDialog.Builder(this).create();
        mAnchorDialog.setMessage(getString(R.string.anchor_dialog_message, formattedDate, qualifier));
        mAnchorDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mAnchorDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mAnchorDialog.show();
        mAnchorDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
        mAnchorDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
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
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override public void onClick(DialogInterface dialog, int which) {
        if (dialog == mAnchorDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    mUser.setAnchor(mAnchorTime);
                    if (mDateDifference < 2) {
                        mCurrentDialog = new AlertDialog.Builder(this).create();
                        mCurrentDialog.setMessage(getString(R.string.historical_dialog_message));
                        mCurrentDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                        mCurrentDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_change), this);
                        mCurrentDialog.show();
                        mCurrentDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                        mCurrentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
                    }
                    DatabaseService.startActionUpdateUser(this, mUser);
                    break;
                default:
            }
        } else if (dialog == mCurrentDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mUser.setHistorical(true);
                    DatabaseService.startActionUpdateUser(this, mUser);
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    mUser.setHistorical(false);
                    DatabaseService.startActionUpdateUser(this, mUser);
                    break;
                default:
            }
        }
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
                argsGiving.putParcelable(ARGS_USER_ATTRIBUTES, mUser);
                argsRecord.putParcelable(ARGS_USER_ATTRIBUTES, mUser);
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

    // TODO: Handled by accessor validation; factor out
//    /**
//     * Confirms whether item exists in collection table and updates status accordingly.
//     */
//    private static class StatusAsyncTask extends AsyncTask<Giving[], Void, Boolean> {
//
//        private WeakReference<MainActivity> mActivity;
//
//        /**
//         * Constructs an instance with a Fragment that is converted to a {@link WeakReference} in order
//         * to prevent memory leak.
//         */
//        StatusAsyncTask(MainActivity mainActivity) {
//            mActivity = new WeakReference<>(mainActivity);
//        }
//
//        /**
//         * Retrieves the item collection status.
//         */
//        @Override protected Boolean doInBackground(Giving[]... givingArray) {
//
//            Context context = mActivity.get().getBaseContext();
//            List<String> charities = UserPreferences.getCharities(context);
//            List<String> eins = new ArrayList<>();
//            for (String charity : charities) eins.add(charity.split(":")[0]);
//            if (context == null) return false;
//            Giving[] valuesArray = givingArray[0];
//            if (valuesArray == null || valuesArray.length == 0) return false;
//            Boolean isCurrent = true;
//            for (Giving giving: valuesArray) {
//               isCurrent = eins.contains(giving.getEin());
//            }
//            if (!isCurrent) DatabaseService.startActionFetchGiving(mActivity.get());
//            return isCurrent;
//        }
//
//        /**
//         * Updates the Fragment field corresponding to the item collection status.
//         */
//        @Override protected void onPostExecute(Boolean isCurrent) {
//            if (!isCurrent) {
//                mActivity.get().mPagerAdapter.notifyDataSetChanged();
//            }
//        }
//    }
}