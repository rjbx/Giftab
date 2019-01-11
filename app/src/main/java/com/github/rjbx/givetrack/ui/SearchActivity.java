package com.github.rjbx.givetrack.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.rjbx.givetrack.R;

import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.DataService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Presents a list of API request generated items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SearchActivity extends AppCompatActivity
        implements CharityFragment.MasterDetailFlow, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_GENERATION_LOADER = 123;
    private static final String STATE_PANE = "state_pane";
    private ListAdapter mAdapter;
    private String mSnackbarMessage;
    private AlertDialog mDialog;
    private static boolean sDialogShown;
    private boolean mDualPane;

    /**
     * Instantiates a swipeable RecyclerView and FloatingActionButton.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportLoaderManager().initLoader(ID_GENERATION_LOADER, null, this);
        if (savedInstanceState != null) {
            mDualPane = savedInstanceState.getBoolean(STATE_PANE);
        } else mDualPane = findViewById(R.id.search_item_container).getVisibility() == View.VISIBLE;

        Bundle bundle = getIntent().getExtras();
        if (mDualPane) showDualPane(bundle);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mDialog = new AlertDialog.Builder(this).create();
        mDialog.setMessage(getString(R.string.dialog_filter_setup));
        mDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_start),
                (onClickDialog, onClickPosition) -> {
                    sDialogShown = true;
                    launchFilterPreferences(this);
        });
        mDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_later),
                (onClickDialog, onClickPosition) -> mDialog.dismiss());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findViewById(R.id.search_fab);
        fab.setOnClickListener(clickedView -> {
            Context context = SearchActivity.this;
            HashMap<String, String> hashMap = new HashMap<>();
            if (UserPreferences.getFocus(context)) hashMap.put(DataService.FetchContract.PARAM_EIN, UserPreferences.getEin(context));
            else {
                hashMap.put(DataService.FetchContract.PARAM_SEARCH, UserPreferences.getTerm(context));
                hashMap.put(DataService.FetchContract.PARAM_CITY, UserPreferences.getCity(context));
                hashMap.put(DataService.FetchContract.PARAM_STATE, UserPreferences.getState(context));
                hashMap.put(DataService.FetchContract.PARAM_ZIP, UserPreferences.getZip(context));
                hashMap.put(DataService.FetchContract.PARAM_MIN_RATING, UserPreferences.getMinrating(context));
                hashMap.put(DataService.FetchContract.PARAM_FILTER, UserPreferences.getFilter(context) ? "1" : "0");
                hashMap.put(DataService.FetchContract.PARAM_SORT, UserPreferences.getSort(context) + ":" + UserPreferences.getOrder(context));
                hashMap.put(DataService.FetchContract.PARAM_PAGE_NUM, UserPreferences.getPages(context));
                hashMap.put(DataService.FetchContract.PARAM_PAGE_SIZE, UserPreferences.getRows(context));
            }
            DataService.startActionFetchGenerated(getBaseContext(), hashMap);
            mSnackbarMessage = getString(R.string.message_search_refresh);
        });

        RecyclerView recyclerView = findViewById(R.id.search_list);
        assert recyclerView != null;
        mAdapter = new ListAdapter();
        recyclerView.setAdapter(mAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Bundle bundle = (Bundle) viewHolder.itemView.getTag();
                final String ein =  bundle.getString(CharityFragment.ARG_ITEM_EIN);
                if (direction == ItemTouchHelper.LEFT) DataService.startActionRemoveGenerated(getBaseContext(), ein);
            }
        }).attachToRecyclerView(recyclerView);
    }

    /**
     * Saves Layout configuration state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PANE, mDualPane);
    }

    /**
     * Generates an options Menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case (android.R.id.home):
                navigateUpTo(new Intent(this, MainActivity.class));
                return true;
            case (R.id.action_settings):
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case (R.id.action_clear):
                DataService.startActionResetGenerated(this);
                return true;
            case (R.id.action_filter):
                launchFilterPreferences(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiates and returns a new {@link Loader} for the given ID.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        switch (id) {
            case ID_GENERATION_LOADER:
                Uri ratingUri = GivetrackContract.Entry.CONTENT_URI_GENERATION;
                return new CursorLoader(
                        this, ratingUri,
                        null, null, null, null);
            default:
                throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || (!cursor.moveToFirst() && !sDialogShown)) {
            mDialog.show();
            mDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
            mDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
        }
        int id = loader.getId();
        switch (id) {
            case ID_GENERATION_LOADER:
                mAdapter.swapCursor(cursor);
                if (mSnackbarMessage == null || mSnackbarMessage.isEmpty()) mSnackbarMessage = getString(R.string.message_search_refresh);
                Snackbar sb = Snackbar.make(findViewById(R.id.search_fab), mSnackbarMessage, Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                sb.show();
                break;
            default:
                throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private static void launchFilterPreferences(Context context) {
        Intent filterIntent = new Intent(context, SettingsActivity.class);
        filterIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.SearchPreferenceFragment.class.getName());
        filterIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        context.startActivity(filterIntent);
    }

    /**
     * Populates {@link SearchActivity} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Cursor mCursor;
        private View mLastClicked;

        /**
         * Augments {@code ViewHolder} {@code onClick} behavior.
         */
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastClicked != null && mLastClicked.equals(view)) mDualPane = !mDualPane;
                else mDualPane = true;

                mLastClicked = view;
                if (mDualPane) showDualPane((Bundle) view.getTag());
                else showSinglePane();
            }
        };

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override public @NonNull ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            if (mCursor == null || mCursor.getCount() == 0) return;

            mCursor.moveToPosition(position);
            String ein = mCursor.getString(GivetrackContract.Entry.INDEX_EIN);
            String name = mCursor.getString(GivetrackContract.Entry.INDEX_CHARITY_NAME);
            String city = mCursor.getString(GivetrackContract.Entry.INDEX_LOCATION_CITY);
            String state = mCursor.getString(GivetrackContract.Entry.INDEX_LOCATION_STATE);
            String zip = mCursor.getString(GivetrackContract.Entry.INDEX_LOCATION_ZIP);
            String homepage = mCursor.getString(GivetrackContract.Entry.INDEX_HOMEPAGE_URL);
            String url = mCursor.getString(GivetrackContract.Entry.INDEX_NAVIGATOR_URL);

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mAddressView.setText(String.format("%s, %s %s", city, state, zip));

            Glide.with(SearchActivity.this).load("https://logo.clearbit.com/" + homepage)
                    .into(holder.mLogoView);

            Bundle arguments = new Bundle();
            arguments.putString(CharityFragment.ARG_ITEM_NAME, name);
            arguments.putString(CharityFragment.ARG_ITEM_EIN, ein);
            arguments.putString(CharityFragment.ARG_ITEM_URL, url);

            holder.itemView.setTag(arguments);
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        /**
         * Returns the number of items to display.
         */
        @Override
        public int getItemCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        /**
         * Swaps the Cursor after completing a load or resetting Loader.
         */
        void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.search_item_primary) TextView mNameView;
            @BindView(R.id.search_item_secondary) TextView mIdView;
            @BindView(R.id.search_item_tertiary) TextView mAddressView;
            @BindView(R.id.search_item_logo) ImageView mLogoView;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    /**
     * Indicates whether the MasterDetailFlow is in dual pane mode.
     */
    public boolean isDualPane() { return mDualPane; }

    /**
     * Presents the list of items and item details side-by-side using two vertical panes.
     */
    public void showDualPane(Bundle args) {
        if (args != null) SearchActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.search_item_container, CharityFragment.newInstance(args))
                .commit();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (width * .5f), ViewGroup.LayoutParams.MATCH_PARENT);
        findViewById(R.id.search_list_container).setLayoutParams(params);
        View container = findViewById(R.id.search_item_container);
        container.setVisibility(View.VISIBLE);
        container.setLayoutParams(params);
    }

    /**
     * Presents the list of items in a single vertical pane, hiding the item details.
     */
    public void showSinglePane() {
        findViewById(R.id.search_list_container).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mDualPane = false;
    }
}
