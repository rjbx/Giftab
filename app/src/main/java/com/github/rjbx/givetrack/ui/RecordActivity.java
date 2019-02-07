package com.github.rjbx.givetrack.ui;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ShareCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
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

import com.github.rjbx.givetrack.R;

import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseService;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Presents a list of API request generated items, which when touched, arrange the list of items and
 * item details side-by-side using two vertical panes.
 */
public class RecordActivity extends AppCompatActivity implements
        DetailFragment.MasterDetailFlow, LoaderManager.LoaderCallbacks<Cursor>,
        DialogInterface.OnClickListener {


    public static final String ACTION_RECORD_INTENT = "com.github.rjbx.givetrack.ui.action.RECORD_INTENT";
    private static final String STATE_PANE = "com.github.rjbx.givetrack.ui.state.RECORD_PANE";
    private long mDeletedTime;
    private static boolean sDualPane;
    private ListAdapter mAdapter;
    private AlertDialog mRemoveDialog;
    private String mSnackbar;
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

        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_RECORD, null, this);
        if (savedInstanceState != null) {
            sDualPane = savedInstanceState.getBoolean(STATE_PANE);
        } else sDualPane = mItemContainer.getVisibility() == View.VISIBLE;

        Bundle bundle = getIntent().getExtras();
        if (sDualPane) showDualPane(bundle);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

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
            case (R.id.action_record):
                launchFilterPreferences(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiates and returns a new {@link Loader} for the given ID.
     */
    @NonNull @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {

        String sort = UserPreferences.getRecordSort(this);
        String order = UserPreferences.getRecordOrder(this);
        String sortOrder = String.format("%s %s", sort, order);

        switch (id) {
            case DatabaseContract.LOADER_ID_RECORD:
                Uri ratingUri = DatabaseContract.Entry.CONTENT_URI_RECORD;
                return new CursorLoader(
                        this, ratingUri,
                        null, null, null, sortOrder);
            default:
                throw new RuntimeException(getString(R.string.loader_error_message, id));
        }
    }

    /**
     * Replaces old data that is to be subsequently released from the {@link Loader}.
     */
    @Override public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || (!cursor.moveToFirst())) return;
        int id = loader.getId();
        switch (id) {
            case DatabaseContract.LOADER_ID_RECORD:
                ContentValues[] valuesArray = new ContentValues[cursor.getCount()];
                int i = 0;
                do {
                    ContentValues values = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    valuesArray[i++] = values;
                } while (cursor.moveToNext());
                mAdapter.swapValues(valuesArray);
                if (mSnackbar == null || mSnackbar.isEmpty()) mSnackbar = getString(R.string.message_search_refresh);
                Snackbar sb = Snackbar.make(mToolbar, mSnackbar, Snackbar.LENGTH_LONG);
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
        if (args != null) RecordActivity.this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.record_item_container, DetailFragment.newInstance(args))
                .commit();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (width * .5f), ViewGroup.LayoutParams.MATCH_PARENT);
        mListContainer.setLayoutParams(params);
        mItemContainer.setVisibility(View.VISIBLE);
        mItemContainer.setLayoutParams(params);
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    /**
     * Presents the list of items in a single vertical pane, hiding the item details.
     */
    @Override public void showSinglePane() {
        mListContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sDualPane = false;
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
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
                    DatabaseService.startActionRemoveRecord(getBaseContext(), mDeletedTime);
                    break;
                default:
            }
        }
    }

    private ItemTouchHelper.SimpleCallback getSimpleCallback(int dragDirs, int swipeDirs) {
        return new ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = (int) viewHolder.itemView.getTag();
                ContentValues values = mAdapter.mValuesArray[position];
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        String amount = values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_IMPACT);
                        String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
                        String formattedDate = DateFormat.getDateInstance().format(mDeletedTime);
                        mDeletedTime = values.getAsLong(DatabaseContract.Entry.COLUMN_DONATION_TIME);
                        mRemoveDialog = new AlertDialog.Builder(RecordActivity.this).create();
                        String messageArgs = String.format("this donation for %s in the amount of %s on %s", name, amount, formattedDate);
                        mRemoveDialog.setMessage(getString(R.string.dialog_removal_record, messageArgs));
                        mRemoveDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.dialog_option_keep), RecordActivity.this);
                        mRemoveDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_option_remove), RecordActivity.this);
                        mRemoveDialog.show();
                        mRemoveDialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                        mRemoveDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAttentionDark));
                        break;
                    case ItemTouchHelper.RIGHT:
                        final String url = values.getAsString(DatabaseContract.Entry.COLUMN_NAVIGATOR_URL);
                        new CustomTabsIntent.Builder()
                                .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                                .build()
                                .launchUrl(RecordActivity.this, Uri.parse(url));
                        getIntent().setAction(MainActivity.ACTION_CUSTOM_TABS);
                        break;
                    default:
                }
            }
        };
    }

    /**
     * Defines and launches Intent for displaying {@link ConfigActivity.RecordPreferenceFragment}.
     */

    private static void launchFilterPreferences(Context context) {
        Intent filterIntent = new Intent(context, ConfigActivity.class);
        filterIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ConfigActivity.RecordPreferenceFragment.class.getName());
        filterIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        filterIntent.setAction(ACTION_RECORD_INTENT);
        context.startActivity(filterIntent);
    }

    /**
     * Populates {@link RecordActivity} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        private ContentValues[] mValuesArray;
        private int mLastPosition;

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

            if (isDualPane()) {
                if (position != mLastPosition) {
                    holder.mStatsView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.mStatsView.setBackgroundColor(getResources().getColor(R.color.colorAttention));
                }
                holder.mEntityView.setVisibility(View.GONE);
            }
            else {
                holder.mEntityView.setVisibility(View.VISIBLE);
                holder.mStatsView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }

            ContentValues values = mValuesArray[position];
            String ein = values.getAsString(DatabaseContract.Entry.COLUMN_EIN);
            String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
            String url = values.getAsString(DatabaseContract.Entry.COLUMN_NAVIGATOR_URL);
            final float impact = Float.parseFloat(values.getAsString(DatabaseContract.Entry.COLUMN_DONATION_IMPACT));
            final long time = values.getAsLong(DatabaseContract.Entry.COLUMN_DONATION_TIME);

            if (name.length() > 35) { name = name.substring(0, 35);
            name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mTimeView.setText(DateFormat.getDateInstance().format(new Date(time)));
            holder.mAmountView.setText(NumberFormat.getCurrencyInstance().format(impact));
            holder.mAmountView.setFocusableInTouchMode(!isDualPane());
            holder.mAmountView.setFocusable(!isDualPane());
            holder.mAmountView.setClickable(isDualPane());

            Bundle arguments = new Bundle();
            arguments.putString(DetailFragment.ARG_ITEM_NAME, name);
            arguments.putString(DetailFragment.ARG_ITEM_EIN, ein);
            arguments.putString(DetailFragment.ARG_ITEM_URL, url);

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
        private void swapValues(ContentValues[] valuesArray) {
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
            @BindView(R.id.record_amount_text) TextView mAmountView;
            @BindView(R.id.record_time_text) TextView mTimeView;
            @BindView(R.id.record_share_button) @Nullable ImageButton mShareButton;
            @BindView(R.id.record_contact_button) @Nullable ImageButton mContactButton;
            private AlertDialog mContactDialog;
            private AlertDialog mDateDialog;
            private long mTime;
            private long mOldTime;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
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
                if (isDualPane()) togglePane(v);
                else {
                    Context context = v.getContext();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(UserPreferences.getAnchor(context));
                    DatePickerDialog datePicker = new DatePickerDialog(
                            context,
                            this,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    datePicker.show();
                }
            }

            @OnClick(R.id.record_amount_text) void editAmount(EditText v) {
                if (isDualPane()) togglePane(v);
            }

            /**
             * Defines behavior on click of share button.
             */
            @Optional @OnClick(R.id.record_share_button) void shareRecord(View v) {

                ContentValues values = mValuesArray[(int) v.getTag()];
                String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
                long time = values.getAsLong(DatabaseContract.Entry.COLUMN_DONATION_TIME);
                float impact = values.getAsFloat(DatabaseContract.Entry.COLUMN_DONATION_IMPACT);

                Intent shareIntent = ShareCompat.IntentBuilder.from(RecordActivity.this)
                        .setType("text/plain")
                        .setText(String.format("My donation on %s totaling %s to %s have been added to my personal record with #%s App!",
                                DateFormat.getDateInstance().format(new Date(time)),
                                NumberFormat.getCurrencyInstance().format(impact),
                                name,
                                getString(R.string.app_name)))
                        .getIntent();
                startActivity(shareIntent);
            }

            /**
             * Defines behavior on click of contact button.
             */
            @Optional @OnClick(R.id.record_contact_button) void viewContacts(View v) {
                mContactDialog = new AlertDialog.Builder(RecordActivity.this).create();
                ContactDialogLayout alertLayout = ContactDialogLayout.getInstance(mContactDialog, mValuesArray[(int) v.getTag()]);
                mContactDialog.setView(alertLayout);
                mContactDialog.show();
            }

            /**
             * Listens for and persists changes to text editor value.
             */
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        float amountTotal;
                        NumberFormat formatter = NumberFormat.getCurrencyInstance();
                        try {
                            String viewText = mAmountView.getText().toString();
                            if (viewText.contains("$")) amountTotal = formatter.parse(viewText).floatValue();
                            else amountTotal = Float.parseFloat(viewText);
                            UserPreferences.setDonation(v.getContext(), String.valueOf(amountTotal));
                            UserPreferences.updateFirebaseUser(v.getContext());
                        } catch (ParseException e) {
                            Timber.e(e);
                            return false;
                        }
                        mAmountView.setText(formatter.format(amountTotal));
                        mAmountView.setContentDescription(getString(R.string.description_donation_text, formatter.format(amountTotal)));
                        InputMethodManager inputMethodManager =
                                (InputMethodManager) RecordActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager == null) return false;
                        inputMethodManager.toggleSoftInput(0, 0);
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                ContentValues values = ListAdapter.this.mValuesArray[(int) view.getTag()];
                mOldTime = values.getAsLong(DatabaseContract.Entry.COLUMN_DONATION_TIME);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
                dateFormatter.setTimeZone(TimeZone.getDefault());
                mTime = calendar.getTimeInMillis();
                String formattedDate = dateFormatter.format(mTime);

                Context context = view.getContext();
                mDateDialog = new AlertDialog.Builder(context).create();
                mDateDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.dialog_option_cancel), this);
                mDateDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_option_confirm), this);
                mDateDialog.show();
                mDateDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorNeutralDark));
                mDateDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorConversionDark));
            }

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog == mDateDialog) {
                    switch (which) {
                        case AlertDialog.BUTTON_NEUTRAL:
                            dialog.dismiss();
                            break;
                        case AlertDialog.BUTTON_POSITIVE:
                            DatabaseService.startActionUpdateTime(RecordActivity.this, mOldTime, mTime);
                        default:
                    }
                }
            }

            private void togglePane(View v) {
                int position = (int) v.getTag();
                ContentValues values = mValuesArray[position];
                String name = values.getAsString(DatabaseContract.Entry.COLUMN_CHARITY_NAME);
                String ein = values.getAsString(DatabaseContract.Entry.COLUMN_EIN);
                String navUrl = values.getAsString(DatabaseContract.Entry.COLUMN_NAVIGATOR_URL);
                if (mLastPosition == position) sDualPane = !sDualPane;
                else sDualPane = true;

                mLastPosition = position;
                Bundle arguments = new Bundle();
                arguments.putString(DetailFragment.ARG_ITEM_NAME, name);
                arguments.putString(DetailFragment.ARG_ITEM_EIN, ein);
                arguments.putString(DetailFragment.ARG_ITEM_URL, navUrl);
                if (sDualPane) showDualPane(arguments);
                else showSinglePane();
            }
        }
    }

    /**
     * Provides an inflated layout populated with contact method buttons and associated
     * listeners predefined.
     */
    static class ContactDialogLayout extends LinearLayout {

        private Context mContext;
        private static AlertDialog mAlertDialog;
        private static String mPhone;
        private static String mEmail;
        private static String mWebsite;
        private static String mLocation;
        @BindView(R.id.email_button) @Nullable Button mEmailButton;
        @BindView(R.id.phone_button) @Nullable Button mPhoneButton;
        @BindView(R.id.location_button) @Nullable Button mLocationButton;
        @BindView(R.id.website_button) @Nullable Button mWebsiteButton;

        /**
         * Defines visibility and appearance of button according to associated content value.
         */
        private ContactDialogLayout(Context context) {
            super(context);
            mContext = context;
            LayoutInflater.from(mContext).inflate(R.layout.dialog_contact, this, true);
            ButterKnife.bind(this);

            if (mEmailButton != null)
                if (mEmail.isEmpty()) mEmailButton.setVisibility(View.GONE);
                else mEmailButton.setText(mEmail.toLowerCase());

            if (mPhoneButton != null)
                if (mPhone.isEmpty()) mPhoneButton.setVisibility(View.GONE);
                else mPhoneButton.setText(String.format("+%s", mPhone));

            if (mWebsiteButton != null)
                if (mWebsite.isEmpty()) mWebsiteButton.setVisibility(View.GONE);
                else mWebsiteButton.setText(mWebsite.toLowerCase());

            if (mLocationButton != null)
                if (mLocation.isEmpty()) mLocationButton.setVisibility(View.GONE);
                else mLocationButton.setText(mLocation);
        }

        /**
         * Initializes value instance fields and generates an instance of this layout.
         */
        public static ContactDialogLayout getInstance(AlertDialog alertDialog, ContentValues values) {
            mAlertDialog = alertDialog;
            mEmail = values.getAsString(DatabaseContract.Entry.COLUMN_EMAIL_ADDRESS);
            mPhone = values.getAsString(DatabaseContract.Entry.COLUMN_PHONE_NUMBER);
            mWebsite = values.getAsString(DatabaseContract.Entry.COLUMN_HOMEPAGE_URL);
            mLocation = valuesToAddress(values);
            return new ContactDialogLayout(mAlertDialog.getContext());
        }

        /**
         * Converts a set of ContentValues to a single formatted String.
         */
        private static String valuesToAddress(ContentValues values) {
            String street = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_STREET);
            String detail = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_DETAIL);
            String city = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_CITY);
            String state = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_STATE);
            String zip = values.getAsString(DatabaseContract.Entry.COLUMN_LOCATION_ZIP);
            return street + (detail.isEmpty() ? "" : '\n' + detail) + '\n' + city + ", " + state.toUpperCase() + " " + zip;
        }

        /**
         * Defines behavior on click of email launch button.
         */
        @Optional @OnClick(R.id.email_button) void launchEmail() {
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(Uri.parse("mailto:"));
            mailIntent.putExtra(Intent.EXTRA_EMAIL, mEmail);
            if (mailIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(mailIntent);
            }
        }

        /**
         * Defines behavior on click of phone launch button.
         */
        @Optional @OnClick(R.id.phone_button) void launchPhone() {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + mPhone));
            if (phoneIntent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(phoneIntent);
            }
        }

        /**
         * Defines behavior on click of website launch button.
         */
        @Optional @OnClick(R.id.website_button) void launchWebsite() {
            new CustomTabsIntent.Builder()
                    .setToolbarColor(getResources().getColor(R.color.colorPrimaryDark))
                    .build()
                    .launchUrl(mContext, Uri.parse(mWebsite));
        }

        /**
         * Defines behavior on click of map launch button.
         */
        @Optional @OnClick(R.id.location_button) void launchMap() {
            Uri intentUri = Uri.parse("geo:0,0?q=" + mLocation);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(mapIntent);
        }
    }
}
