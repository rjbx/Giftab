package com.github.rjbx.givetrack.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.DataService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides the main screen for this application.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ACTION_CUSTOM_TABS = "com.github.rjbx.givetrack.ui.action.CUSTOM_TABS";
    public static final String ARGS_VALUES_ARRAY = "values_array";
    private static final int ID_MAIN_LOADER = 444;
    private SectionsPagerAdapter mPagerAdapter;
    private Cursor mCursor;

    /**
     * Populates the TabLayout with a SectionsPagerAdapter and the NavigationView with a DrawerLayout.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.main_pager);

        TabLayout tabLayout = findViewById(R.id.main_tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        if (mPagerAdapter == null) mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

        DrawerLayout drawer = findViewById(R.id.main_activity);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.main_navigation);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportLoaderManager().initLoader(ID_MAIN_LOADER, null, this);
    }

    /**
     * Closes an open NavigationDrawer when back navigating.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.main_activity);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else super.onBackPressed();
    }

    /**
     * Generates an options Menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_recalibrate:
                DonationFragment.resetDonationsAdjusted();
                ContentValues values = new ContentValues();
                values.putNull(GivetrackContract.Entry.COLUMN_DONATION_PERCENTAGE);
                DataService.startActionUpdatePercentages(this, values);
                return true;
            case R.id.action_clear:
                AlertDialog clearDialog = new AlertDialog.Builder(this).create();
                clearDialog.setMessage(getString(R.string.dialog_removal_alert, getString(R.string.snippet_all_charities)));
                clearDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep),
                        (onClickDialog, onClickPosition) -> clearDialog.dismiss());
                clearDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_erase),
                        (onClickDialog, onClickPosition) -> DataService.startActionResetCollected(this));
                clearDialog.show();
                clearDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
                clearDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                return false;
            case R.id.action_magnitude:
                View view = getLayoutInflater().inflate(R.layout.seekbar_main, new LinearLayout(this));
                AlertDialog magnitudeDialog = new AlertDialog.Builder(this).create();
                SeekBar seekBar = view.findViewById(R.id.main_seek_bar);
                TextView textView = view.findViewById(R.id.seek_progress_display);
                textView.setText(String.format(Locale.getDefault(), "%.2f", seekBar.getProgress() / 1000f));

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        textView.setText(String.format(Locale.getDefault(), "%.2f", progress / 1000f));
                    }
                    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                seekBar.setProgress(Math.round(Float.parseFloat(UserPreferences.getMagnitude(this)) * 1000f));
                magnitudeDialog.setView(view);
                magnitudeDialog.setMessage(this.getString(R.string.dialog_description_magnitude_adjustment));
                magnitudeDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel),
                        (onClickDialog, onClickPosition) -> magnitudeDialog.dismiss());
                magnitudeDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm),
                        (onClickDialog, onClickPosition) -> {
                            float magnitude = seekBar.getProgress() / 1000f;
                            UserPreferences.setMagnitude(this, String.format(Locale.getDefault(), "%.2f", magnitude));
                            UserPreferences.updateFirebaseUser(this);
                        });
                magnitudeDialog.show();
                magnitudeDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.GRAY);
                magnitudeDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Instantiates and returns a new {@link Loader} for the given ID.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        switch (id) {
            case ID_MAIN_LOADER:
                return new CursorLoader(this, GivetrackContract.Entry.CONTENT_URI_COLLECTION,
                        null, null, null, null);
            default: throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case ID_MAIN_LOADER:
                mCursor = cursor;
                new StatusAsyncTask(this).execute(mCursor);
                Intent intent = getIntent();
                if (intent.getAction() == null || !intent.getAction().equals(ACTION_CUSTOM_TABS))
                    mPagerAdapter.notifyDataSetChanged();
                else intent.setAction(null);
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) { mCursor = null;}

    /**
     * Defines behavior onClick of each Navigation MenuItem.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case (R.id.nav_search): startActivity(new Intent(this, SearchActivity.class)); break;
            case (R.id.nav_settings): startActivity(new Intent(this, SettingsActivity.class)); break;
            case (R.id.nav_logout): startActivity(new Intent(this, AuthActivity.class).setAction(AuthActivity.ACTION_SIGN_OUT)); break;
            case (R.id.nav_cn): launchCustomTabs(getString(R.string.url_cn)); break;
            case (R.id.nav_clearbit): launchCustomTabs(getString(R.string.url_clearbit)); break;
        }

        DrawerLayout drawer = findViewById(R.id.main_activity);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void launchCustomTabs(String url) {
        new CustomTabsIntent.Builder()
                .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                .build()
                .launchUrl(this, Uri.parse(url));
        getIntent().setAction(ACTION_CUSTOM_TABS);
    }

    public static class PlaceholderFragment extends Fragment {

        /**
         * Provides default constructor required for the {@link FragmentManager}
         * to instantiate this Fragment.
         */
        public PlaceholderFragment() {}

        /**
         * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
         */
        public static PlaceholderFragment newInstance(@Nullable Bundle args) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            if (args != null) fragment.setArguments(args);
            return fragment;
        }

        /**
         * Generates a Layout for the Fragment.
         */
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            ((TextView) rootView.findViewById(R.id.placeholder_message)).setText(R.string.message_empty_collection);
            rootView.findViewById(R.id.placeholder_button).setOnClickListener(clickedView -> startActivity(new Intent(getActivity(), SearchActivity.class)));
            return rootView;
        }
    }

    /**
     * Returns a Fragment corresponding to a section.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Constructs this instance from a {@link FragmentManager}.
         */
        SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        /**
         * Instantiates the Fragment for a given section.
         */
        @Override
        public Fragment getItem(int position) {

            if (mCursor == null || !mCursor.moveToFirst()) return PlaceholderFragment.newInstance(null);
            else {
                int i = 0;
                Bundle args = new Bundle();
                ContentValues[] valuesArray = new ContentValues[mCursor.getCount()];
                do {
                    ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(mCursor, values);
                    valuesArray[i++] = values;
                } while (mCursor.moveToNext());
                args.putParcelableArray(ARGS_VALUES_ARRAY, valuesArray);
                switch (position) {
                    case 0: return DonationFragment.newInstance(args);
                    case 1: return HomeFragment.newInstance(args);
                    default: return PlaceholderFragment.newInstance(null);
                }
            }
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
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
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (object instanceof CharityFragment.MasterDetailFlow
                    && ((CharityFragment.MasterDetailFlow) object).isDualPane())
                ((CharityFragment.MasterDetailFlow) object).showSinglePane();
            super.destroyItem(container, position, object);
        }
    }

    /**
     * Confirms whether item exists in collection table and updates status accordingly.
     */
    public static class StatusAsyncTask extends AsyncTask<Cursor, Void, Boolean> {

        WeakReference<MainActivity> mActivity;

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
        @Override protected Boolean doInBackground(Cursor... cursors) {

            Context context = mActivity.get().getBaseContext();
            List<String> charities = UserPreferences.getCharities(context);
            List<String> eins = new ArrayList<>();
            for (String charity : charities) eins.add(charity.split(":")[0]);
            if (context == null) return false;
            Cursor collectionCursor = cursors[0];
            if (collectionCursor == null || !collectionCursor.moveToFirst()) return false;
            Boolean isCurrent = true;
            do {
               isCurrent = -1 != eins.indexOf(collectionCursor.getString(GivetrackContract.Entry.INDEX_EIN));
            } while (collectionCursor.moveToNext());
            collectionCursor.moveToFirst();
            return isCurrent;
        }

        /**
         * Updates the Fragment field corresponding to the item collection status.
         */
        @Override protected void onPostExecute(Boolean isCurrent) {
            if (!isCurrent) {
                DataService.startActionFetchCollected(mActivity.get());
                mActivity.get().mPagerAdapter.notifyDataSetChanged();
            }
        }
    }
}