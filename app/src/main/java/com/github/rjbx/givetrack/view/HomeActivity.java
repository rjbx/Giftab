package com.github.rjbx.givetrack.view;

import android.app.Activity;
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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.transition.Fade;
import androidx.viewpager.widget.ViewPager;
import androidx.core.view.GravityCompat;

import android.os.Parcelable;
import android.os.PersistableBundle;
import android.widget.DatePicker;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import butterknife.ButterKnife;
import butterknife.Optional;
import butterknife.Unbinder;
import butterknife.BindView;
import butterknife.OnClick;

import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.data.DatabaseManager;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.TimeZone;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract;

import static com.github.rjbx.givetrack.AppUtilities.DATE_FORMATTER;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_TARGET;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_RECORD;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;

/**
 * Provides the application home screen; manages {@link CursorLoader} and {@link ViewPager} for
 * {@link GlanceFragment} and {@link GiveFragment}.
 */
public class HomeActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener,
        DialogInterface.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    public static final String ACTION_HOME_INTENT = "com.github.rjbx.givetrack.ui.action.HOME_INTENT";

    public static final String ARGS_PLACEHOLDER_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.PLACEHOLDER_ATTRIBUTES";
    public static final String ARGS_TARGET_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.GIVE_ATTRIBUTES";
    public static final String ARGS_RECORD_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.RECORD_ATTRIBUTES";
    public static final String ARGS_USER_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.USER_ATTRIBUTES";
    public static final String ARGS_ACTION_ATTRIBUTES = "com.github.rjbx.givetrack.ui.arg.ACTION_ATTRIBUTES";

    private static final String STATE_RECORD_ARRAY = "com.github.rjbx.givetrack.ui.state.RECORD_ARRAY";
    private static final String STATE_TARGET_ARRAY = "com.github.rjbx.givetrack.ui.state.TARGET_ARRAY";
    private static final String STATE_ACTIVE_USER = "com.github.rjbx.givetrack.ui.state.ACTIVE_USER";
    private static final String STATE_USER_LOCK = "com.github.rjbx.givetrack.ui.state.USER_LOCK";
    private static final String STATE_TARGET_LOCK = "com.github.rjbx.givetrack.ui.state.TARGET_LOCK";
    private static final String STATE_RECORD_LOCK = "com.github.rjbx.givetrack.ui.state.RECORD_LOCK";

    private boolean mUserLock = true;
    private boolean mTargetLock = true;
    private boolean mRecordLock = true;

    private boolean mInstanceStateRestored;
    private long mAnchorTime;
    private SectionsPagerAdapter mPagerAdapter;
    private Target[] mTargetArray;
    private Record[] mRecordArray;
    private AlertDialog mAnchorDialog;
    private AlertDialog mCurrentDialog;
    private User mUser;
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
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        if (savedInstanceState != null) {
            mUserLock = savedInstanceState.getBoolean(STATE_USER_LOCK);
            mTargetLock = savedInstanceState.getBoolean(STATE_TARGET_LOCK);
            mRecordLock = savedInstanceState.getBoolean(STATE_RECORD_LOCK);
            mUser = savedInstanceState.getParcelable(STATE_ACTIVE_USER);
            Parcelable[] pTargets = savedInstanceState.getParcelableArray(STATE_TARGET_ARRAY);
            Parcelable[] pRecords = savedInstanceState.getParcelableArray(STATE_RECORD_ARRAY);
            if (pTargets != null) mTargetArray = AppUtilities.getTypedArrayFromParcelables(pTargets, Target.class);
            if (pRecords != null) mRecordArray = AppUtilities.getTypedArrayFromParcelables(pRecords, Record.class);
            mInstanceStateRestored = true;
//            savedInstanceState.clear();
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

        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_USER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean(STATE_USER_LOCK, mUserLock);
        outState.putBoolean(STATE_TARGET_LOCK, mTargetLock);
        outState.putBoolean(STATE_RECORD_LOCK, mRecordLock);
        super.onSaveInstanceState(outState, outPersistentState);
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
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                AppUtilities.launchPreferenceFragment(this, ACTION_HOME_INTENT);
                return true;
            case R.id.action_date:
                if (mUser == null) return false;
                if (mUser.getGiveTiming() == 0 && !AppUtilities.dateIsCurrent(mUser.getGiveAnchor())) {
                    mUser.setGiveAnchor(System.currentTimeMillis());
                    DatabaseManager.startActionUpdateUser(this, mUser);
                }
                Calendar calendar = Calendar.getInstance();
                if (mUser.getGiveTiming() != 0) calendar.setTimeInMillis(mUser.getGiveAnchor());
                DatePickerDialog datePicker = new DatePickerDialog(
                        this,
                        this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
                break;
            case R.id.action_add: finish(); startActivity(new Intent(this, IndexActivity.class)); break;
            case R.id.action_history: finish(); startActivity(new Intent(this, JournalActivity.class)); break;
            default: return super.onOptionsItemSelected(item);
        } return false;
    }

    /**
     * Persists values through destructive lifecycle changes.
     */
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArray(STATE_TARGET_ARRAY, mTargetArray);
        outState.putParcelableArray(STATE_RECORD_ARRAY, mRecordArray);
        outState.putParcelable(STATE_ACTIVE_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    /**
     * Defines the data to be returned from {@link LoaderManager.LoaderCallbacks}.
     */
    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_TARGET: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_TARGET, null, DatabaseContract.CompanyEntry.COLUMN_UID + " = ?", new String[] { mUser.getUid() }, DatabaseContract.CompanyEntry.COLUMN_SHARE + " " + "DESC");
            case LOADER_ID_RECORD: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_RECORD, null, DatabaseContract.CompanyEntry.COLUMN_UID + " = ? ", new String[] { mUser.getUid() }, null);
            case LOADER_ID_USER: return new CursorLoader(this, DatabaseContract.UserEntry.CONTENT_URI_USER, null, DatabaseContract.UserEntry.COLUMN_USER_ACTIVE + " = ? ", new String[] { "1" }, null);
            default: throw new RuntimeException(this.getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case DatabaseContract.LOADER_ID_TARGET:
                if (!mUserLock && mTargetArray == null) {
                    mTargetArray = new Target[data.getCount()];
                    DatabaseManager.startActionFetchTarget(this);
                } else {
                    mTargetLock = false;
                    mTargetArray = new Target[data.getCount()];
                    if (data.moveToFirst()) {
                        int i = 0;
                        do {
                            Target target = new Target();
                            AppUtilities.cursorRowToEntry(data, target);
                            mTargetArray[i++] = target;
                        } while (data.moveToNext());
                    }
                }
                break;
            case DatabaseContract.LOADER_ID_RECORD:
                if (!mUserLock && mRecordArray == null) {
                    mRecordArray = new Record[data.getCount()];
                    DatabaseManager.startActionFetchRecord(this);
                } else {
                    mRecordLock = false;
                    mRecordArray = new Record[data.getCount()];
                    if (data.moveToFirst()) {
                        int i = 0;
                        do {
                            Record record = new Record();
                            AppUtilities.cursorRowToEntry(data, record);
                            mRecordArray[i++] = record;
                        } while (data.moveToNext());
                    }
                }
                break;
            case DatabaseContract.LOADER_ID_USER:
                if (data.moveToFirst()) {
                    do {
                        User user = User.getDefault();
                        AppUtilities.cursorRowToEntry(data, user);
                        if (user.getUserActive()) {
                            mUserLock = false;
                            mUser = user;
                            if (mTargetArray == null || mInstanceStateRestored) getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_TARGET, null, this);
                            if (mRecordArray == null || mInstanceStateRestored) getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_RECORD, null, this);
                            mInstanceStateRestored = false;
                            break;
                        }
                    } while (data.moveToNext());
                }
                break;
        }
        if (!mUserLock && !mTargetLock && !mRecordLock) {
            Intent intent = getIntent();
            if ((intent.getAction() == null || !intent.getAction().equals(DetailFragment.ACTION_CUSTOM_TABS))/* && !mInstanceStateRestored*/) {
                mPagerAdapter.notifyDataSetChanged();
            } else {
                mUserLock = true;
                mTargetLock = true;
                mRecordLock = true;
                intent.setAction(null);
            }
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    /**
     * Defines behavior onClick of each Navigation MenuItem.
     */
    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case (R.id.nav_spawn): finish(); startActivity(new Intent(this, IndexActivity.class)); break;
            case (R.id.nav_record): finish(); startActivity(new Intent(this, JournalActivity.class)); break;
            case (R.id.nav_settings): finish(); startActivity(new Intent(this, ConfigActivity.class).setAction(ACTION_HOME_INTENT).putExtra(ConfigActivity.ARG_ITEM_USER, mUser)); break;
            case (R.id.nav_logout): finish(); startActivity(new Intent(this, AuthActivity.class).setAction(AuthActivity.ACTION_SIGN_OUT)); break;
            case (R.id.nav_cn): ViewUtilities.launchBrowserIntent(this, Uri.parse(getString(R.string.url_cn))); break;
            case (R.id.nav_clearbit): ViewUtilities.launchBrowserIntent(this, Uri.parse(getString(R.string.url_clearbit))); break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            getLoaderManager().destroyLoader(DatabaseContract.LOADER_ID_USER); // Prevents callback resulting from persisting user active status to false within AuthActivity
            getLoaderManager().destroyLoader(DatabaseContract.LOADER_ID_TARGET);
            getLoaderManager().destroyLoader(DatabaseContract.LOADER_ID_RECORD);
        }
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

        mAnchorDialog = new AlertDialog.Builder(this).create();
        mAnchorDialog.setMessage(getString(R.string.anchor_dialog_message, formattedDate));
        mAnchorDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
        mAnchorDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
        mAnchorDialog.show();
        mAnchorDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
        mAnchorDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversion, null));
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override public void onClick(DialogInterface dialog, int which) {
        if (mUser == null) return;
        if (dialog == mAnchorDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    mUser.setGiveAnchor(mAnchorTime);
                    boolean anchorToday = AppUtilities.dateIsCurrent(mUser.getGiveAnchor());
                    if (!anchorToday) {
                        mCurrentDialog = new AlertDialog.Builder(this).create();
                        mCurrentDialog.setMessage(getString(R.string.historical_dialog_message));
                        mCurrentDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), this);
                        mCurrentDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_change), this);
                        mCurrentDialog.show();
                        mCurrentDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorAttention, null));
                        mCurrentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversion, null));
                    } else mUser.setGiveTiming(0);
                    DatabaseManager.startActionUpdateUser(this, mUser);
                    break;
                default:
            }
        } else if (dialog == mCurrentDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mUser.setGiveTiming(2);
                    DatabaseManager.startActionUpdateUser(this, mUser);
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    mUser.setGiveTiming(1);
                    DatabaseManager.startActionUpdateUser(this, mUser);
                    break;
                default:
            }
        }
    }

    /**
     * Provides logic and views for a default screen when others are unavailable.
     */
    public static class PlaceholderFragment extends Fragment {

        private Activity mParentActivity;
        private Unbinder mUnbinder;
        @Nullable @BindView(R.id.launch_progress) ProgressBar mLaunchProgress;
        @Nullable @BindView(R.id.launch_icon) ImageView mLaunchIcon;

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
            fragment.setExitTransition(new Fade(Fade.OUT));
            return fragment;
        }

        /**
         * Generates a Layout for the Fragment.
         */
        @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            boolean launchScreen = true;
            if (arguments != null) launchScreen = getArguments().getBoolean(ARGS_PLACEHOLDER_ATTRIBUTES);
            int rootResource = launchScreen ? R.layout.placeholder_launch : R.layout.placeholder_add;
            View rootView = inflater.inflate(rootResource, container, false);
            mUnbinder = ButterKnife.bind(this, rootView);
            return rootView;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            super.onActivityCreated(savedInstanceState);
            if (getActivity() == null || !(getActivity() instanceof HomeActivity)) return;
            mParentActivity = getActivity();
        }

        /**
         * Manages the display of the loading icon.
         */
        @Override public void onResume() {
            super.onResume();
            Bundle arguments = getArguments();
            if (arguments == null) return;
            String launchAction = arguments.getString(ARGS_ACTION_ATTRIBUTES);
            if (launchAction != null &&
                    (launchAction.equals(AuthActivity.ACTION_SIGN_IN))) {
                if (mLaunchProgress != null) mLaunchProgress.setVisibility(View.VISIBLE);
                if (mLaunchIcon != null) mLaunchIcon.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Manages the display of the loading icon.
         */
        @Override public void onPause() {
            if (mLaunchProgress != null) mLaunchProgress.setVisibility(View.GONE);
            if (mLaunchIcon != null) mLaunchIcon.setVisibility(View.GONE);
            super.onPause();
        }

        /**
         * Unbinds Butterknife from this Fragment.
         */
        @Override public void onDestroy() {
            super.onDestroy();
            mUnbinder.unbind();
        }

        /**
         * Defines behavior on click of launch spawn button.
         */
        @Optional
        @OnClick(R.id.placeholder_button) void launchSpawn()  { mParentActivity.finish(); startActivity(new Intent(getActivity(), IndexActivity.class)); }
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
        @Override public @NonNull Fragment getItem(int position) {

            Bundle argsPlaceholder = new Bundle();
            if (mTargetArray == null || mRecordArray == null) {
                argsPlaceholder.putBoolean(ARGS_PLACEHOLDER_ATTRIBUTES, true);
                argsPlaceholder.putString(ARGS_ACTION_ATTRIBUTES, getIntent().getAction());
                return PlaceholderFragment.newInstance(argsPlaceholder);
            } else if (mTargetArray.length == 0) {
                argsPlaceholder.putBoolean(ARGS_PLACEHOLDER_ATTRIBUTES, false);
                return PlaceholderFragment.newInstance(argsPlaceholder);
            } else {
                Bundle argsTarget = new Bundle();
                Bundle argsRecord = new Bundle();
                argsTarget.putParcelableArray(ARGS_TARGET_ATTRIBUTES, mTargetArray);
                argsRecord.putParcelableArray(ARGS_RECORD_ATTRIBUTES, mRecordArray);
                argsTarget.putParcelable(ARGS_USER_ATTRIBUTES, mUser);
                argsRecord.putParcelable(ARGS_USER_ATTRIBUTES, mUser);

                switch (position) {
                    case 0: return GiveFragment.newInstance(argsTarget);
                    case 1: return GlanceFragment.newInstance(argsRecord);
                    default: return PlaceholderFragment.newInstance(argsPlaceholder);
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
            String action = getIntent().getAction();
            if (object instanceof DetailFragment.MasterDetailFlow
                    && ((DetailFragment.MasterDetailFlow) object).isDualPane() && (action != null && !action.equals(DetailFragment.ACTION_CUSTOM_TABS)))
                ((DetailFragment.MasterDetailFlow) object).showSinglePane();
            super.destroyItem(container, position, object);
        }
    }
}