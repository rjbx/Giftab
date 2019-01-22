package com.github.rjbx.givetrack.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.bumptech.glide.Glide;
import com.github.rjbx.givetrack.R;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Presents a list of API request generated items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class RecordActivity extends AppCompatActivity implements
        DialogInterface.OnClickListener {

    private static final String STATE_PANE = "com.github.rjbx.givetrack.ui.state.RECORD_PANE";
    private static boolean sDialogShown;
    private static boolean sDualPane;
    private ListAdapter mAdapter;
    private AlertDialog mRecordDialog;
    private String mSnackbar;
    @BindView(R.id.record_fab) FloatingActionButton mFab;
    @BindView(R.id.record_toolbar) Toolbar mToolbar;
    @BindView(R.id.record_list) RecyclerView mRecyclerView;
    @BindView(R.id.record_list_container) View mListContainer;
    @BindView(R.id.record_item_container) View mItemContainer;

    /**
     * Instantiates a swipeable RecyclerView and FloatingActionButton.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            sDualPane = savedInstanceState.getBoolean(STATE_PANE);
        } else sDualPane = mItemContainer.getVisibility() == View.VISIBLE;

        Bundle bundle = getIntent().getExtras();

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        mRecordDialog = new AlertDialog.Builder(this).create();
        mRecordDialog.setMessage(getString(R.string.dialog_filter_setup));
        mRecordDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_start), this);
        mRecordDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_later), this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        assert mRecyclerView != null;
        mAdapter = new ListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT) {

            @Override public boolean onMove(@NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder,
                                            @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Bundle bundle = (Bundle) viewHolder.itemView.getTag();
                final String ein =  bundle.getString(DetailFragment.ARG_ITEM_EIN);
                if (direction == ItemTouchHelper.LEFT) DatabaseService.startActionRemoveGenerated(getBaseContext(), ein);
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    /**
     * Saves Layout configuration state.
     */
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PANE, sDualPane);
    }

    /**
     * Generates an options Menu.
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.record, menu);
        return true;
    }

    /**
     * Defines behavior onClick of each MenuItem.
     */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case (android.R.id.home):
                navigateUpTo(new Intent(this, MainActivity.class));
                return true;
            case (R.id.action_filter):
                launchFilterPreferences(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onClick(DialogInterface dialog, int which) {
        if (dialog == mRecordDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mRecordDialog.dismiss();
                    break;
                case AlertDialog.BUTTON_POSITIVE:
                    sDialogShown = true;
                    launchFilterPreferences(this);
                    break;
                default:
            }
        }
    }

    private static void launchFilterPreferences(Context context) {
        Intent filterIntent = new Intent(context, ConfigActivity.class);
        filterIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ConfigActivity.GivingPreferenceFragment.class.getName());
        filterIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        context.startActivity(filterIntent);
    }

    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private ContentValues[] mValuesArray;

        private View mLastClicked;

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override public @NonNull ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_record, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            if (mValuesArray == null || mValuesArray.length == 0) return;

            ContentValues values = mValuesArray[position];
            String ein = values.getAsString(DatabaseContract.Entry.COLUMN_EIN);
            String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
            String city = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_CITY);
            String state = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_STATE);
            String zip = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_ZIP);
            String homepage = values.getAsString(DatabaseContract.Entry.COLUMN_HOMEPAGE_URL);
            String url = values.getAsString(DatabaseContract.Entry.COLUMN_NAVIGATOR_URL);

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mAddressView.setText(String.format("%s, %s %s", city, state, zip));

            Glide.with(RecordActivity.this).load("https://logo.clearbit.com/" + homepage)
                    .into(holder.mLogoView);

            Bundle arguments = new Bundle();
            arguments.putString(DetailFragment.ARG_ITEM_NAME, name);
            arguments.putString(DetailFragment.ARG_ITEM_EIN, ein);
            arguments.putString(DetailFragment.ARG_ITEM_URL, url);

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
        private void swapValues(ContentValues[] valuesArray) {
            mValuesArray = valuesArray;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.record_item_primary) TextView mNameView;
            @BindView(R.id.record_item_secondary) TextView mIdView;
            @BindView(R.id.record_item_tertiary) TextView mAddressView;
            @BindView(R.id.record_item_logo) ImageView mLogoView;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            /**
             * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
             */
            @OnClick(R.id.record_item_view) void togglePane(View v) {
                if (mLastClicked != null && mLastClicked.equals(v)) sDualPane = !sDualPane;
                else sDualPane = true;

                mLastClicked = v;
            }
        }
    }
}
