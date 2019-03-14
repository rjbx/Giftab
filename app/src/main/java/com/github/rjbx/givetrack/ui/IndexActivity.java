package com.github.rjbx.givetrack.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.bumptech.glide.Glide;
import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;

import com.github.rjbx.givetrack.data.DatabaseAccessor;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.github.rjbx.givetrack.data.entry.Spawn;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_SPAWN;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;

/**
 * Presents a list of entities spawned from a remote data API with toggleable detail pane.
 */
public class IndexActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DetailFragment.MasterDetailFlow,
        DialogInterface.OnClickListener {

    public static final String ACTION_SPAWN_INTENT = "com.github.rjbx.givetrack.ui.action.SPAWN_INTENT";
    private static final String STATE_PANE = "com.github.rjbx.givetrack.ui.state.SPAWN_PANE";
    private static final String STATE_SHOWN = "com.github.rjbx.givetrack.ui.state.SPAWN_PANE";
    private static boolean sDialogShown;
    private static boolean sDualPane;
    private Spawn[] mValuesArray;
    private ListAdapter mAdapter;
    private AlertDialog mSpawnDialog;
    private String mSnackbar;
    private User mUser;
    private boolean mLock = true;
    @BindView(R.id.spawn_fab) FloatingActionButton mFab;
    @BindView(R.id.spawn_toolbar) Toolbar mToolbar;
    @BindView(R.id.spawn_list) RecyclerView mRecyclerView;
    @BindView(R.id.spawn_list_container) View mListContainer;
    @BindView(R.id.spawn_item_container) View mItemContainer;
    @BindView(R.id.spawn_progress) View mSpawnProgress;

    /**
     * Instantiates a swipeable RecyclerView and FloatingActionButton.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_USER, null, this);
        if (mUser != null) getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_SPAWN, null, this);
        if (savedInstanceState != null) {
            sDualPane = savedInstanceState.getBoolean(STATE_PANE);
            sDialogShown = savedInstanceState.getBoolean(STATE_SHOWN);
        } else sDualPane = mItemContainer.getVisibility() == View.VISIBLE;

        Bundle bundle = getIntent().getExtras();
        if (sDualPane) showDualPane(bundle);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        if (!sDialogShown) showDialog();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        assert mRecyclerView != null;
        mAdapter = new ListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        new ItemTouchHelper(getSimpleCallback(
                ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                )).attachToRecyclerView(mRecyclerView);
    }

    /**
     * Persists values through destructive lifecycle changes.
     */
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PANE, sDualPane);
        outState.putBoolean(STATE_SHOWN, sDialogShown);
    }

    /**
     * Generates an options Menu.
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.index, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case (android.R.id.home):
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            case (R.id.action_filter):
                AppUtilities.launchPreferenceFragment(this, ACTION_SPAWN_INTENT);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Defines the data to be returned from {@link LoaderManager.LoaderCallbacks}.
     */
    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_SPAWN: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_SPAWN, null, DatabaseContract.CompanyEntry.COLUMN_UID + " = ? ", new String[] { mUser.getUid() }, null);
            case LOADER_ID_USER: return new CursorLoader(this, DatabaseContract.UserEntry.CONTENT_URI_USER, null, null, null, null);
            default: throw new RuntimeException(this.getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) return;
        int id = loader.getId();
        switch (id) {
            case DatabaseContract.LOADER_ID_SPAWN:
                mSpawnProgress.setVisibility(View.GONE);
                mValuesArray = new Spawn[data.getCount()];
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        Spawn spawn = Spawn.getDefault();
                        DatabaseAccessor.cursorRowToEntry(data, spawn);
                        mValuesArray[i++] = spawn;
                    } while (data.moveToNext());
                    if (!mLock) mAdapter.swapValues(mValuesArray);
                }
                if (mSnackbar == null || mSnackbar.isEmpty()) mSnackbar = getString(R.string.message_spawn_refresh);
                Snackbar sb = Snackbar.make(mFab, mSnackbar, Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
                sb.show();
                break;
            case DatabaseContract.LOADER_ID_USER:
                if (data.moveToFirst()) {
                    do {

                        User user = User.getDefault();
                        DatabaseAccessor.cursorRowToEntry(data, user);
                        if (mUser != null && user.getTargetStamp() != mUser.getTargetStamp() && isDualPane()) {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(DetailFragment.ARG_ITEM_COMPANY, mValuesArray[mAdapter.mLastPosition]);
                            showDualPane(bundle);
                        }
                        if (user.getUserActive()) {
                            mLock = false;
                            mUser = user;
                            if (mValuesArray == null) getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_SPAWN, null, this);
                            break;
                        }
                    } while (data.moveToNext());
                }
                sDialogShown = mUser.getIndexDialog();
                break;
            default:
                throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapValues(null);
    }

    /**
     * Indicates whether the MasterDetailFlow is in dual pane mode.
     */
    @Override public boolean isDualPane() { return sDualPane; }

    /**
     * Presents the list of items and item details side-by-side using two vertical panes.
     */
    @Override public void showDualPane(Bundle args) {
        if (args != null) IndexActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.spawn_item_container, DetailFragment.newInstance(args))
                .commit();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (width * .5f), ViewGroup.LayoutParams.MATCH_PARENT);
        mListContainer.setLayoutParams(params);
        mItemContainer.setVisibility(View.VISIBLE);
        mItemContainer.setLayoutParams(params);
    }

    /**
     * Presents the list of items in a single vertical pane, hiding the item details.
     */
    @Override public void showSinglePane() {
        mListContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sDualPane = false;
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override public void onClick(DialogInterface dialog, int which) {
        if (dialog == mSpawnDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mSpawnDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    sDialogShown = true;
                    mUser.setIndexDialog(sDialogShown);
                    DatabaseService.startActionUpdateUser(this, mUser);
                    AppUtilities.launchPreferenceFragment(this, ACTION_SPAWN_INTENT);
                    break;
                default:
            }
        }
    }

    /**
     * Populates {@link IndexActivity} {@link RecyclerView}.
     */
    @OnClick(R.id.spawn_fab) public void refreshResults() {
        fetchResults();
    }

    /**
     * Displays a dialog with instructions.
     */
    private void showDialog() {
        mSpawnDialog = new AlertDialog.Builder(this).create();
        mSpawnDialog.setMessage(getString(R.string.dialog_filter_setup));
        mSpawnDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_start), this);
        mSpawnDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_later), this);
        mSpawnDialog.show();
        mSpawnDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark, null));
        mSpawnDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
    }

    /**
     * Fetches entities spawned from the remote data API based on the settings defined in
     * {@link ConfigActivity.IndexPreferenceFragment}.
     */
    private void fetchResults() {
        mSpawnProgress.setVisibility(View.VISIBLE);

        DatabaseService.startActionFetchSpawn(getBaseContext());
        mSnackbar = getString(R.string.message_spawn_refresh);
    }

    /**
     * Defines behavior on interactions with list items.
     */
    private ItemTouchHelper.SimpleCallback getSimpleCallback(int dragDirs, int swipeDirs) {
        return new ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = (int) viewHolder.itemView.getTag();
                Spawn values = mValuesArray[position];
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        final String company =  values.getId();
                        DatabaseService.startActionRemoveSpawn(getBaseContext(), values);
                        break;
                    case ItemTouchHelper.RIGHT:
                        final String url = values.getNavigatorUrl();
                        new CustomTabsIntent.Builder()
                                .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark, null))
                                .build()
                                .launchUrl(IndexActivity.this, Uri.parse(url));
                        getIntent().setAction(HomeActivity.ACTION_CUSTOM_TABS);
                        break;
                    default:
                }
            }
        };
    }

    /**
     * Populates {@link IndexActivity} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Spawn[] mValuesArray;
        int mLastPosition;

        /**
         * Instantiates the {@link RecyclerView.Adapter} and locks requests to populate
         * until active {@link User} is returned from {@link Loader} callback.
         */
        ListAdapter() {
            super();
            mLock = true;
        }

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override public @NonNull ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_index, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            if (mValuesArray == null || mValuesArray.length == 0) return;

            Spawn values = mValuesArray[position];
            String ein = values.getEin();
            String name = values.getName();
            String city = values.getLocationCity();
            String state = values.getLocationState();
            String zip = values.getLocationZip();
            String homepage = values.getHomepageUrl();

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mAddressView.setText(String.format("%s, %s %s", city, state, zip));

            Glide.with(IndexActivity.this).load("https://logo.clearbit.com/" + homepage)
                    .into(holder.mLogoView);

            holder.itemView.setTag(position);
        }

        /**
         * Returns the number of items to display.
         */
        @Override
        public int getItemCount() {
            return mValuesArray != null ? mValuesArray.length : 0;
        }

        /**
         * Swaps the Cursor after completing a load or resetting Loader.
         */
        private void swapValues(Spawn[] valuesArray) {
            mValuesArray = valuesArray;
            notifyDataSetChanged();
        }

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.spawn_item_primary) TextView mNameView;
            @BindView(R.id.spawn_item_secondary) TextView mIdView;
            @BindView(R.id.spawn_item_tertiary) TextView mAddressView;
            @BindView(R.id.spawn_item_logo) ImageView mLogoView;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            /**
             * Defines behavior on click of spawn item view.
             */
            @OnClick(R.id.spawn_item_view) void togglePane(View v) {
                int position = (int) v.getTag();
                Spawn values = mValuesArray[position];
                if (mLastPosition == position) sDualPane = !sDualPane;
                else sDualPane = true;

                mLastPosition = position;
                Bundle arguments = new Bundle();
                arguments.putParcelable(DetailFragment.ARG_ITEM_COMPANY, values);
                if (sDualPane) showDualPane(arguments);
                else showSinglePane();
            }
        }
    }
}
