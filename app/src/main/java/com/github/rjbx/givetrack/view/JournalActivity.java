package com.github.rjbx.givetrack.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import timber.log.Timber;

import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseManager;
import com.github.rjbx.givetrack.data.entry.Company;
import com.github.rjbx.givetrack.data.entry.Record;
import com.github.rjbx.givetrack.data.entry.Spawn;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static com.github.rjbx.givetrack.AppUtilities.CURRENCY_FORMATTER;
import static com.github.rjbx.givetrack.AppUtilities.DATE_FORMATTER;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_RECORD;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_TARGET;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;

/**
 * Presents a list of editable giving records with toggleable detail pane.
 */
public class JournalActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DetailFragment.MasterDetailFlow,
        DialogInterface.OnClickListener {

    public static final String ACTION_JOURNAL_INTENT = "com.github.rjbx.givetrack.ui.action.JOURNAL_INTENT";
    private static final String STATE_POSITION = "com.github.rjbx.givetrack.ui.state.GIVE_POSITION";
    private static final String STATE_PANE = "com.github.rjbx.givetrack.ui.state.RECORD_PANE";
    private static final String STATE_ADDED = "com.github.rjbx.givetrack.ui.state.ADDED_TARGET";
    private static final String STATE_REMOVED = "com.github.rjbx.givetrack.ui.state.REMOVED_TARGET";
    private static final String STATE_ARRAY = "com.github.rjbx.givetrack.ui.state.RECORD_ARRAY";
    private static final String STATE_LOCK = "com.github.rjbx.givetrack.ui.state.LOADER_LOCK";
    private static final String STATE_USER = "com.github.rjbx.givetrack.ui.state.ACTIVE_USER";
    private long mDeletedTime;
    private static boolean sDualPane;
    private DetailFragment mDetailFragment;
    private Record[] mValuesArray;
    private ListAdapter mAdapter;
    private AlertDialog mRemoveDialog;
    private String mSnackbarMessage;
    private User mUser;
    private String mAddedName;
    private String mRemovedName;
    private int mPanePosition;
    private boolean mLock = true;
    @BindView(R.id.record_toolbar) Toolbar mToolbar;
    @BindView(R.id.record_list) RecyclerView mRecyclerView;
    @BindView(R.id.record_list_container) View mListContainer;
    @BindView(R.id.record_detail_container) View mDetailContainer;

    /**
     * Instantiates a swipeable RecyclerView and FloatingActionButton.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(LOADER_ID_USER, null, this);

        if (savedInstanceState != null) {
            mLock = savedInstanceState.getBoolean(STATE_LOCK);
            sDualPane = savedInstanceState.getBoolean(STATE_PANE);
            mPanePosition = savedInstanceState.getInt(STATE_POSITION);
            mAddedName = savedInstanceState.getString(STATE_ADDED);
            mRemovedName = savedInstanceState.getString(STATE_REMOVED);
            mUser = savedInstanceState.getParcelable(STATE_USER);
            Parcelable[] pRecords = savedInstanceState.getParcelableArray(STATE_ARRAY);
            if (pRecords != null) mValuesArray = AppUtilities.getTypedArrayFromParcelables(pRecords, Record.class);
        } else sDualPane = mDetailContainer.getVisibility() == View.VISIBLE;

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        assert mRecyclerView != null;
        mAdapter = new ListAdapter(mValuesArray);
        mRecyclerView.setAdapter(mAdapter);
        new ItemTouchHelper(getSimpleCallback(
                ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        )).attachToRecyclerView(mRecyclerView);

        if (isDualPane()) ViewUtilities.launchDetailPane(this, mListContainer, mDetailContainer);
    }

    /**
     * Persists values through destructive lifecycle changes.
     */
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_USER, mUser);
        outState.putBoolean(STATE_LOCK, mLock);
        outState.putBoolean(STATE_PANE, sDualPane);
        outState.putInt(STATE_POSITION, mPanePosition);
        outState.putString(STATE_ADDED, mAddedName);
        outState.putString(STATE_REMOVED, mRemovedName);
        outState.putParcelableArray(STATE_ARRAY, mValuesArray);
    }

    /**
     * Generates an options Menu.
     */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.journal, menu);
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
            case (R.id.action_record):
                AppUtilities.launchPreferenceFragment(this, ACTION_JOURNAL_INTENT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }

    /**
     * Defines the data to be returned from {@link LoaderManager.LoaderCallbacks}.
     */
    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_TARGET: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_TARGET, null, DatabaseContract.CompanyEntry.COLUMN_UID + " = ? ", new String[] { mUser.getUid() }, null);
            case LOADER_ID_RECORD: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_RECORD, null, DatabaseContract.CompanyEntry.COLUMN_UID + " = ? ", new String[] { mUser.getUid() }, mUser.getJournalSort() + " " + mUser.getJournalOrder());
            case LOADER_ID_USER: return new CursorLoader(this, DatabaseContract.UserEntry.CONTENT_URI_USER, null, DatabaseContract.UserEntry.COLUMN_USER_ACTIVE + " = ? ", new String[] { "1" }, null);
            default: throw new RuntimeException(this.getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null || (!data.moveToFirst())) return;
        int id = loader.getId();
      Snackbar sb = Snackbar.make(mToolbar, mSnackbarMessage, Snackbar.LENGTH_LONG);
        sb.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        switch (id) {
            case DatabaseContract.LOADER_ID_TARGET:
                if (mAddedName != null) {
                    mSnackbarMessage = getString(R.string.message_collected_add, mAddedName);
                    sb.setText(mSnackbarMessage).show();
                    mAddedName = null;
                } else if (mRemovedName != null) {
                    mSnackbarMessage = getString(R.string.message_collected_remove, mRemovedName);
                    sb.setText(mSnackbarMessage).show();
                    mRemovedName = null;
                }
                break;
            case DatabaseContract.LOADER_ID_RECORD:
                if (mLock) break;
                mValuesArray = new Record[data.getCount()];
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        Record record = new Record();
                        AppUtilities.cursorRowToEntry(data, record);
                        mValuesArray[i++] = record;
                    } while (data.moveToNext());
                    mAdapter.swapValues(mValuesArray);
                }
                if (sDualPane) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DetailFragment.ARG_ITEM_COMPANY, mValuesArray[mPanePosition]);
                    showDualPane(bundle);
                }
                if (mSnackbarMessage == null || mSnackbarMessage.isEmpty()) mSnackbarMessage = getString(R.string.message_record_refresh);
                sb.setText(mSnackbarMessage).show();
                break;
            case DatabaseContract.LOADER_ID_USER:
                if (data.moveToFirst()) {
                    do {
                        User user = User.getDefault();
                        AppUtilities.cursorRowToEntry(data, user);
                        if (mUser != null && user.getTargetStamp() != mUser.getTargetStamp() && isDualPane()) {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(DetailFragment.ARG_ITEM_COMPANY, mValuesArray[mPanePosition]);
                            showDualPane(bundle);
                        }
                        if (user.getUserActive()) {
                            mLock = false;
                            mUser = user;
                            if (mValuesArray == null) getSupportLoaderManager().initLoader(LOADER_ID_RECORD, null, this);
                            if (mAddedName == null && mRemovedName == null) getSupportLoaderManager().initLoader(LOADER_ID_TARGET, null, this);
                            break;
                        }
                    } while (data.moveToNext());
                }
                break;
            default:
                throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Tells the application to remove any stored references to the {@link Loader} data.
     */
    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) { mAdapter.swapValues(null); }

    /**
     * Indicates whether the MasterDetailFlow is in dual pane mode.
     */
    @Override public boolean isDualPane() { return sDualPane; }

    /**
     * Presents the list of items and item details side-by-side using two vertical panes.
     */
    @Override public void showDualPane(@NonNull Bundle args) {

        if (mDetailFragment == null) {
            mDetailFragment = DetailFragment.newInstance(args);
            JournalActivity.this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.record_detail_container, mDetailFragment)
                    .commit();
        }

        ViewUtilities.launchDetailPane(this, mListContainer, mDetailContainer);

        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    /**
     * Presents the list of items in a single vertical pane, hiding the item details.
     */
    @Override public void showSinglePane() {
        if (mDetailFragment != null) getSupportFragmentManager().beginTransaction().remove(mDetailFragment);
        mDetailFragment = null;
        sDualPane = false;

        if (mListContainer != null) mListContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void addEntry(Spawn spawn) {
        DatabaseManager.startActionTargetSpawn(this, spawn);
        mAddedName = spawn.getName();
        if (isDualPane()) showSinglePane();
    }

    @Override
    public void removeEntry(Company company) {
        DatabaseManager.startActionUntargetCompany(this, company);
        mRemovedName = company.getName();
        if (isDualPane()) showSinglePane();
    }

    /**
     * Defines behaviors on click of DialogInterface buttons.
     */
    @Override public void onClick(DialogInterface dialog, int which) {
        if (dialog == mRemoveDialog) {
            switch (which) {
                case AlertDialog.BUTTON_NEUTRAL:
                    mRemoveDialog.dismiss();
                    mAdapter.notifyDataSetChanged();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    Record record = (Record) mRemoveDialog.getButton(AlertDialog.BUTTON_NEGATIVE).getTag();
                    DatabaseManager.startActionRemoveRecord(getBaseContext(), record);
                    break;
                default:
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            getLoaderManager().destroyLoader(DatabaseContract.LOADER_ID_USER);
            getLoaderManager().destroyLoader(DatabaseContract.LOADER_ID_RECORD);
        }
    }

    /**
     * Defines behavior on interactions with list items.
     */
    private ItemTouchHelper.SimpleCallback getSimpleCallback(int dragDirs, int swipeDirs) {
        return new ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = (int) viewHolder.itemView.getTag();
                Record values = mAdapter.mValuesArray[position];
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        String amount = values.getImpact();
                        String name = values.getName();
                        String formattedDate = DATE_FORMATTER.format(mDeletedTime);
                        mDeletedTime = values.getTime();
                        mRemoveDialog = new AlertDialog.Builder(JournalActivity.this).create();
                        String messageArgs = String.format("this contribution for %s in the amount of %s on %s", name, amount, formattedDate);
                        String screenName = JournalActivity.class.getSimpleName().replace("Activity", "").toLowerCase();
                        mRemoveDialog.setMessage(getString(R.string.message_remove_entry, messageArgs, screenName));
                        mRemoveDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), JournalActivity.this);
                        mRemoveDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), JournalActivity.this);
                        mRemoveDialog.show();
                        mRemoveDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                        Button button = mRemoveDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
                        button.setTextColor(getResources().getColor(R.color.colorAttention, null));
                        button.setTag(values);
                        break;
                    case ItemTouchHelper.RIGHT:
                        final String url = values.getNavigatorUrl();
                        ViewUtilities.launchBrowserIntent(JournalActivity.this, Uri.parse(url));
                        break;
                    default:
                }
            }
        };
    }

    /**
     * Populates {@link JournalActivity} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private Record[] mValuesArray;

        ListAdapter(Record[] valuesArray) {
            super();
            mValuesArray = valuesArray;
            mLock = true;
        }

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override public @NonNull ViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_journal, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            if (mValuesArray == null || mValuesArray.length == 0) return;

            if (isDualPane()) {
                if (position != mPanePosition) {
                    holder.mStatsView.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
                } else {
                    holder.mStatsView.setBackgroundColor(getResources().getColor(R.color.colorHeat, null));
                }
                holder.mEntityView.setVisibility(View.GONE);
            }
            else {
                holder.mEntityView.setVisibility(View.VISIBLE);
                holder.mStatsView.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            }

            Record values = mValuesArray[position];
            int type = values.getType();
            switch (type) {
                case 0: holder.mTypeView.setText("M"); break;
                case 1: holder.mTypeView.setText("S"); break;
                case 2: holder.mTypeView.setText("G"); break;
                default:
            }

            String ein = values.getEin();
            String name = values.getName();
            final float impact = Float.parseFloat(values.getImpact());
            final long time = values.getTime();

            if (name.length() > 35) { name = name.substring(0, 35);
            name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mTimeView.setText(DATE_FORMATTER.format(new Date(time)));
            holder.mAmountView.setText(CURRENCY_FORMATTER.format(impact));
            holder.mAmountView.setFocusableInTouchMode(!isDualPane());
            holder.mAmountView.setFocusable(!isDualPane());
            holder.mAmountView.setClickable(true);
            if (impact > 99999f &&
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                float scaleFactor = .9f;
                float impactDividend = impact;
                while (impactDividend > 999999) {
                    impactDividend /= 10;
                    scaleFactor *= .90f;
                }
                holder.mAmountView.setTextScaleX(scaleFactor);
            }

            holder.itemView.setTag(position);
            for (View view : holder.itemView.getTouchables()) view.setTag(position);
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
        private void swapValues(Record[] valuesArray) {
            mValuesArray = valuesArray;
            notifyDataSetChanged();
        }

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder implements
                TextView.OnEditorActionListener,
                DatePickerDialog.OnDateSetListener,
                DialogInterface.OnClickListener {

            @BindView(R.id.record_text_view) View mEntityView;
            @BindView(R.id.record_stats_view) View mStatsView;
            @BindView(R.id.record_primary) TextView mNameView;
            @BindView(R.id.record_secondary) TextView mIdView;
            @BindView(R.id.record_amount_text) EditText mAmountView;
            @BindView(R.id.record_time_text) TextView mTimeView;
            @BindView(R.id.record_memo_text) TextView mMemoView;
            @BindView(R.id.record_type_text) TextView mTypeView;
            @BindView(R.id.record_share_button) @Nullable ImageButton mShareButton;
            @BindView(R.id.record_contact_button) @Nullable ImageButton mContactButton;
            private AlertDialog mContactDialog;
            private AlertDialog mDateDialog;
            private AlertDialog mMemoDialog;
            private EditText mDetailView;
            private long mTime;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                mAmountView.setOnEditorActionListener(this);
            }

            @Optional @OnClick(R.id.record_memo_text) void editMemo(View v) {
                int position = (int) v.getTag();
                Record record = mValuesArray[position];

                String memo = record.getMemo();
                mDetailView = new EditText(JournalActivity.this);
                mMemoDialog = new AlertDialog.Builder(JournalActivity.this).create();
                mDetailView.setText(memo);
                mMemoDialog.setView(mDetailView);
                mMemoDialog.setMessage("Edit Memo");
                mMemoDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_cancel), this);
                mMemoDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.dialog_option_confirm), this);
                mMemoDialog.show();
                mMemoDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                Button button = mMemoDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setTextColor(getResources().getColor(R.color.colorConversion, null));
                button.setTag(position);
            }

            /**
             * Defines behavior on click of type button.
             */
            @Optional @OnClick(R.id.record_type_text) void toggleType(View v) {

                int position = (int) v.getTag();
                Record record = mValuesArray[position];

                int type = record.getType() + 1;
                if (type > 2) type = 0;
                record.setType(type);
                DatabaseManager.startActionUpdateRecord(JournalActivity.this, mValuesArray);
            }

            /**
             * Defines behavior on click of item view.
             */
            @OnClick(R.id.record_text_view) void clickEntity(View v) {
                togglePane(v);
            }

            /**
             * Defines behavior on click of item view.
             */
            @OnClick(R.id.record_stats_view) void clickStats(View v) {
                if (isDualPane()) togglePane(v);
            }

            /**
             * Defines behavior on click of stats view.
             */
            @OnClick(R.id.record_time_text) void editTime(View v) {
                int position = (int) v.getTag();
                long time = mValuesArray[position].getTime();
                if (isDualPane()) togglePane(v);
                else {
                    Context context = v.getContext();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);
                    DatePickerDialog datePicker = new DatePickerDialog(
                            context,
                            this,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    datePicker.getDatePicker().setTag(v.getTag());
                    datePicker.show();
                }
            }

            /**
             * Defines behavior on click of amount view.
             */
            @OnClick(R.id.record_amount_text) void editAmount(EditText v) {
                if (isDualPane()) togglePane(v);
            }

            /**
             * Defines behavior on click of share button.
             */
            @Optional @OnClick(R.id.record_share_button) void shareRecord(View v) {

                Record values = mValuesArray[(int) v.getTag()];
                String name = values.getName().replace(" ", "");
                long time = values.getTime();
                float impact = Float.parseFloat(values.getImpact());

                String textMessage = String.format("My donation on %s totaling %s to #%s have been added to my personal record with #%s App!",
                        DATE_FORMATTER.format(new Date(time)), CURRENCY_FORMATTER.format(impact), name, getString(R.string.app_name));
                ViewUtilities.launchShareIntent(JournalActivity.this, textMessage);
            }

            /**
             * Defines behavior on click of contact button.
             */
            @Optional @OnClick(R.id.record_contact_button) void viewContacts(View v) {
                mContactDialog = new AlertDialog.Builder(JournalActivity.this).create();
                ViewUtilities.ContactDialogLayout alertLayout = ViewUtilities.ContactDialogLayout.getInstance(
                        mContactDialog, mValuesArray[(int) v.getTag()]);
                mContactDialog.setView(alertLayout);
                mContactDialog.show();
            }

            /**
             * Listens for and persists changes to text editor value.
             */
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Record record = mValuesArray[(int) v.getTag()];
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        if (v == mAmountView) {
                            float amountTotal;
                            try {
                                String viewText = mAmountView.getText().toString();
                                if (viewText.contains("$"))
                                    amountTotal = CURRENCY_FORMATTER.parse(viewText).floatValue();
                                else amountTotal = Float.parseFloat(viewText);
                            } catch (ParseException e) {
                                Timber.e(e);
                                return false;
                            }
                            record.setImpact(String.valueOf(amountTotal));
                            DatabaseManager.startActionUpdateRecord(JournalActivity.this, record);
                            DatabaseManager.startActionTargetRecord(JournalActivity.this, record);
                            String formattedAmount = CURRENCY_FORMATTER.format(amountTotal);
                            mAmountView.setText(formattedAmount);
                            mAmountView.setContentDescription(getString(R.string.description_donation_text, formattedAmount));
                        }
                        InputMethodManager inputMethodManager =
                                (InputMethodManager) JournalActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager == null) return false;
                        inputMethodManager.toggleSoftInput(0, 0);
                        return true;
                    default:
                        return false;
                }
            }

            /**
             * Updates the DatePicker with the date selected from the Dialog.
             */
            @Override public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                int position = (int) view.getTag();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                mTime = calendar.getTimeInMillis();

                Context context = view.getContext();
                mDateDialog = new AlertDialog.Builder(context).create();
                mDateDialog.setMessage("Are you sure you want to change the date of this record?");
                mDateDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.dialog_option_cancel), this);
                mDateDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_option_confirm), this);
                mDateDialog.show();
                mDateDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark, null));
                Button button = mDateDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setTextColor(getResources().getColor(R.color.colorConversion, null));
                button.setTag(position);
            }

            /**
             * Defines behavior onClick of each DialogInterface option.
             */
            @Override public void onClick(DialogInterface dialog, int which) {
                if (dialog == mDateDialog) {
                    switch (which) {
                        case AlertDialog.BUTTON_NEUTRAL:
                            dialog.dismiss();
                            break;
                        case AlertDialog.BUTTON_POSITIVE:
                            int position = (int) mDateDialog.getButton(DialogInterface.BUTTON_POSITIVE).getTag();
                            Record record = mValuesArray[position];
                            record.setTime(mTime);
                            DatabaseManager.startActionUpdateRecord(JournalActivity.this, record);
                        default:
                    }
                } else if (dialog == mMemoDialog) {
                    switch(which) {
                        case AlertDialog.BUTTON_NEUTRAL:
                            dialog.dismiss();
                            break;
                        case AlertDialog.BUTTON_POSITIVE:
                            String text = mDetailView.getText().toString();
                            int position = (int) mMemoDialog.getButton(AlertDialog.BUTTON_POSITIVE).getTag();
                            Record record = mValuesArray[position];
                            record.setMemo(text);
                            DatabaseManager.startActionUpdateRecord(JournalActivity.this, record);
                            break;
                    }

                }
            }

            /**
             * Defines behavior on click of record item view.
             */
            private void togglePane(View v) {
                if (mDetailFragment != null) getSupportFragmentManager().beginTransaction().remove(mDetailFragment);
                mDetailFragment = null;

                int position = (int) v.getTag();
                Record values = mValuesArray[position];
                if (mPanePosition == position) sDualPane = !sDualPane;
                else sDualPane = true;

                mPanePosition = position;
                Bundle arguments = new Bundle();
                arguments.putParcelable(DetailFragment.ARG_ITEM_COMPANY, values.getSuper());
                if (sDualPane) showDualPane(arguments);
                else if (isDualPane()) showSinglePane();
            }
        }
    }
}
