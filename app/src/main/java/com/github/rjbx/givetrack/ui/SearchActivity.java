package com.github.rjbx.givetrack.ui;

import android.content.Context;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.bumptech.glide.Glide;
import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;

import com.github.rjbx.givetrack.data.DatabaseAccessor;
import com.github.rjbx.givetrack.data.DatabaseCallbacks;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseController;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.github.rjbx.givetrack.data.entry.Search;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

/**
 * Presents a list of API request generated items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SearchActivity extends AppCompatActivity implements
        DatabaseController,
        DetailFragment.MasterDetailFlow,
        DialogInterface.OnClickListener {


    public static final String ACTION_SEARCH_INTENT = "com.github.rjbx.givetrack.ui.action.SEARCH_INTENT";
    private static final String STATE_PANE = "com.github.rjbx.givetrack.ui.state.SEARCH_PANE";
    private static final String STATE_SHOWN = "com.github.rjbx.givetrack.ui.state.SEARCH_PANE";
    private static boolean sDialogShown;
    private static boolean sDualPane;
    private ListAdapter mAdapter;
    private AlertDialog mSearchDialog;
    private String mSnackbar;
    private User mUser;
    @BindView(R.id.search_fab) FloatingActionButton mFab;
    @BindView(R.id.search_toolbar) Toolbar mToolbar;
    @BindView(R.id.search_list) RecyclerView mRecyclerView;
    @BindView(R.id.search_list_container) View mListContainer;
    @BindView(R.id.search_item_container) View mItemContainer;
    @BindView(R.id.search_progress) View mSearchProgress;

    /**
     * Instantiates a swipeable RecyclerView and FloatingActionButton.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

//        sDialogShown = UserPreferences.getSearchguide(this);
        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_SEARCH, null, new DatabaseCallbacks(this));
        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_USER, null, new DatabaseCallbacks(this));
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
     * Saves Layout configuration state.
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

        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case (android.R.id.home):
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case (R.id.action_filter):
                AppUtilities.launchPreferenceFragment(this, mUser, ACTION_SEARCH_INTENT);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(int id, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) return;
        switch (id) {
            case DatabaseContract.LOADER_ID_SEARCH:
                mSearchProgress.setVisibility(View.GONE);
                Search[] searches = new Search[cursor.getCount()];
                int i = 0;
                do {
                    Search search = new Search();
                    DatabaseAccessor.cursorRowToEntry(cursor, search);
                    searches[i++] = search;
                } while (cursor.moveToNext());
                mAdapter.swapValues(searches);
                if (mSnackbar == null || mSnackbar.isEmpty()) mSnackbar = getString(R.string.message_search_refresh);
                Snackbar sb = Snackbar.make(mFab, mSnackbar, Snackbar.LENGTH_LONG);
                sb.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                sb.show();
                break;
            case DatabaseContract.LOADER_ID_USER:
                if (cursor.moveToFirst()) {
                    do {
                        User user = User.getDefault();
                        DatabaseAccessor.cursorRowToEntry(cursor, user);
                        if (user.getActive()) mUser = user;
                    } while (cursor.moveToNext());
                }
                sDialogShown = mUser.getSearchguide();
                break;
            default:
                throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override public void onLoaderReset() {
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
        if (args != null) SearchActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.search_item_container, DetailFragment.newInstance(args))
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
        if (dialog == mSearchDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mSearchDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    sDialogShown = true;
//                    UserPreferences.setSearchguide(this, sDialogShown);
                    mUser.setSearchguide(sDialogShown);
                    DatabaseService.startActionUpdateUser(this, mUser);
                    AppUtilities.launchPreferenceFragment(this, mUser, ACTION_SEARCH_INTENT);
                    break;
                default:
            }
        }
    }

    /**
     * Populates {@link SearchActivity} {@link RecyclerView}.
     */
    @OnClick(R.id.search_fab) public void refreshResults() {
        fetchResults();
    }

    private void showDialog() {
        mSearchDialog = new AlertDialog.Builder(this).create();
        mSearchDialog.setMessage(getString(R.string.dialog_filter_setup));
        mSearchDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_start), this);
        mSearchDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_later), this);
        mSearchDialog.show();
        mSearchDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
        mSearchDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
    }

    private void fetchResults() {
        mSearchProgress.setVisibility(View.VISIBLE);
        Context context = SearchActivity.this;
        HashMap<String, String> hashMap = new HashMap<>();
        if (mUser.getFocus()/*UserPreferences.getFocus(context)*/) hashMap.put(DatabaseService.FetchContract.PARAM_EIN, mUser.getCompany()/*UserPreferences.getEin(context)*/);
        else {
            hashMap.put(DatabaseService.FetchContract.PARAM_SEARCH, mUser.getTerm()/* UserPreferences.getTerm(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_CITY, mUser.getCity() /* UserPreferences.getCity(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_STATE, mUser.getState() /* UserPreferences.getState(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_ZIP, mUser.getZip() /* UserPreferences.getZip(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_MIN_RATING, mUser.getMinrating() /* UserPreferences.getMinrating(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_FILTER, mUser.getFilter() /* UserPreferences.getFilter(context) */ ? "1" : "0");
            hashMap.put(DatabaseService.FetchContract.PARAM_SORT, mUser.getSearchSort() /* UserPreferences.getSearchSort(context) */ + ":" + mUser.getSearchOrder() /* UserPreferences.getSearchOrder(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_PAGE_NUM, mUser.getPages() /* UserPreferences.getPages(context) */);
            hashMap.put(DatabaseService.FetchContract.PARAM_PAGE_SIZE, mUser.getRows() /* UserPreferences.getRows(context) */);
        }
        DatabaseService.startActionFetchSearch(getBaseContext(), hashMap);
        mSnackbar = getString(R.string.message_search_refresh);
    }

    private ItemTouchHelper.SimpleCallback getSimpleCallback(int dragDirs, int swipeDirs) {
        return new ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Bundle bundle = (Bundle) viewHolder.itemView.getTag();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        final Search values =  bundle.getParcelable(DetailFragment.ARG_ITEM_COMPANY);
                        DatabaseService.startActionRemoveSearch(getBaseContext(), values);
                        break;
                    case ItemTouchHelper.RIGHT:
                        final String url = bundle.getString(DetailFragment.ARG_ITEM_URL);
                        new CustomTabsIntent.Builder()
                                .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                                .build()
                                .launchUrl(SearchActivity.this, Uri.parse(url));
                        getIntent().setAction(MainActivity.ACTION_CUSTOM_TABS);
                        break;
                    default:
                }
            }
        };
    }

    /**
     * Populates {@link SearchActivity} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Search[] mValuesArray;

        private View mLastClicked;

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
            if (mValuesArray == null || mValuesArray.length == 0) return;

            Search values = mValuesArray[position];
            String ein = values.getEin();
            String name = values.getName();
            String city = values.getLocationCity();
            String state = values.getLocationState();
            String zip = values.getLocationZip();
            String homepage = values.getHomepageUrl();
            String url = values.getNavigatorUrl();

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mAddressView.setText(String.format("%s, %s %s", city, state, zip));

            Glide.with(SearchActivity.this).load("https://logo.clearbit.com/" + homepage)
                    .into(holder.mLogoView);

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.ARG_ITEM_COMPANY, values);

            holder.itemView.setTag(arguments);
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
        private void swapValues(Search[] valuesArray) {
            mValuesArray = valuesArray;
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

            /**
             * Defines behavior on click of search item view.
             */
            @OnClick(R.id.search_item_view) void togglePane(View v) {
                if (mLastClicked != null && mLastClicked.equals(v)) sDualPane = !sDualPane;
                else sDualPane = true;

                mLastClicked = v;
                if (sDualPane) showDualPane((Bundle) v.getTag());
                else showSinglePane();
            }
        }
    }
}
