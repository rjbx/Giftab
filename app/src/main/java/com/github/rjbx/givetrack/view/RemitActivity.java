package com.github.rjbx.givetrack.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rjbx.givetrack.AppUtilities;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.entry.Target;
import com.github.rjbx.givetrack.data.entry.User;
import com.google.android.gms.wallet.*;
import com.google.android.gms.common.api.*;
import com.stripe.android.model.Token;

import java.util.Arrays;
import java.util.zip.DataFormatException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.github.rjbx.givetrack.AppUtilities.CURRENCY_FORMATTER;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_TARGET;
import static com.github.rjbx.givetrack.data.DatabaseContract.LOADER_ID_USER;


/**
 * Provides a UI for and manages payment confirmation and processing.
 */
public class RemitActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 1;
    private double mAmount;
    private User mUser;
    private PaymentsClient mPaymentsClient;
    private ListAdapter mAdapter;
    @BindView(R.id.remit_toolbar) Toolbar mToolbar;
    @BindView(R.id.remit_list) RecyclerView mRecyclerView;
    @BindView(R.id.remit_action_bar) ImageButton mConfirmButton;
    @BindView(R.id.remit_action_wrapper) View mButtonWrapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remit);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);


        getSupportLoaderManager().initLoader(LOADER_ID_USER, null, this);

        if (mUser != null) {
            mPaymentsClient
                    = Wallet.getPaymentsClient(this, new Wallet
                    .WalletOptions.Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build()
            );

            isReadyToPay();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
                switch (resultCode) {

                    case RESULT_OK:
                        if (data == null) throw new DataFormatException();
                        PaymentData paymentData = PaymentData.getFromIntent(data);

                        if (paymentData == null) throw new DataFormatException();
                        PaymentMethodToken paymentMethodToken = paymentData.getPaymentMethodToken();

                        if (paymentMethodToken == null) throw new DataFormatException();
                        String rawToken = paymentMethodToken.getToken();

                        Token stripeToken = Token.fromString(rawToken);
                        if (stripeToken != null) {
                            chargeToken(stripeToken.getId());
                        }
                        break;

                    case RESULT_CANCELED:
                        Timber.d("Payment was cancelled");
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        String statusMessage
                                = status != null ? status.getStatusMessage() : "Status unavailable";
                        Timber.e(statusMessage);
                        break;

                    default: Timber.d("Payment could not be processed");
                }
            }
        } catch (DataFormatException e) {
            Timber.e("Payment data is unavailable");
        }
    }

    /**
     * Defines the data to be returned from {@link LoaderManager.LoaderCallbacks}.
     */
    @NonNull
    @Override public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_ID_TARGET: return new CursorLoader(this, DatabaseContract.CompanyEntry.CONTENT_URI_TARGET, null, DatabaseContract.CompanyEntry.COLUMN_UID + " = ? ", new String[] { mUser.getUid() }, null);
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
        switch (id) {
            case DatabaseContract.LOADER_ID_TARGET:
                Target[] targets = new Target[data.getCount()];
                if (data.moveToFirst()) {
                    int i = 0;
                    do {
                        Target target = new Target();
                        AppUtilities.cursorRowToEntry(data, target);
                        targets[i++] = target;
                    } while (data.moveToNext());
                    assert mRecyclerView != null;
                    if (mAdapter == null) {
                        mAdapter = new ListAdapter(targets);
                        mRecyclerView.setAdapter(mAdapter);
                    } else mAdapter.swapValues(targets);
                    mPaymentsClient
                            = Wallet.getPaymentsClient(this, new Wallet
                            .WalletOptions.Builder()
                            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                            .build()
                    );

                    isReadyToPay();
                }
                break;
            case DatabaseContract.LOADER_ID_USER:
                if (data.moveToFirst()) {
                    do {
                        User user = User.getDefault();
                        AppUtilities.cursorRowToEntry(data, user);
                        if (user.getUserActive()) {
                            mUser = user;
                            mAmount = mUser.getGiveImpact();
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
    @Override public void onLoaderReset(@NonNull Loader<Cursor> loader) { }


    @OnClick(R.id.remit_action_bar) void confirmPayment() {
        PaymentDataRequest request = createPaymentDataRequest();
        if (request != null) {
            AutoResolveHelper.resolveTask(
                    mPaymentsClient.loadPaymentData(request),
                    this,
                    LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    private void chargeToken(String tokenId) {

    }

    private void isReadyToPay() {
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();
        mPaymentsClient.isReadyToPay(request).addOnCompleteListener((task) -> {
                        try {
                            final Boolean result = task.getResult(ApiException.class);
                            if (result != null && result) createPaymentDataRequest();
                            mConfirmButton.setBackgroundColor(getColor(R.color.colorConversion));
                            mButtonWrapper.setBackgroundColor(getColor(R.color.colorConversionDark));
                        } catch (NullPointerException|ApiException e) {
                            Toast.makeText(
                                    this,
                                    R.string.payment_error_message,
                                    Toast.LENGTH_LONG)
                                .show();
                        }
                    });
    }

    private PaymentMethodTokenizationParameters createTokenizationParameters() {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(
                        WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "stripe")
                .addParameter("stripe:key", getString(R.string.sp_api_key))
                .addParameter("stripe:version", "2019-05-16") // Changelog: https://stripe.com/docs/upgrades
                .build();
    }

    private PaymentDataRequest createPaymentDataRequest() {
        return PaymentDataRequest.newBuilder()
                .setTransactionInfo(
                        TransactionInfo.newBuilder()
                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                .setTotalPrice(String.valueOf(mAmount))
                                .setCurrencyCode("USD")
                                .build())
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setCardRequirements(
                        CardRequirements.newBuilder()
                                .addAllowedCardNetworks(Arrays.asList(
                                        WalletConstants.CARD_NETWORK_AMEX,
                                        WalletConstants.CARD_NETWORK_DISCOVER,
                                        WalletConstants.CARD_NETWORK_VISA,
                                        WalletConstants.CARD_NETWORK_MASTERCARD))
                                .build())
                .setPaymentMethodTokenizationParameters(createTokenizationParameters())
                .build();
    }

    /**
     * Populates {@link JournalActivity} {@link RecyclerView}.
     */
    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        Target[] mValuesArray;

        ListAdapter(Target[] targets) {
            super();
            mValuesArray = targets;
        }

        /**
         * Generates a Layout for the ViewHolder based on its Adapter position and orientation
         */
        @Override public @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_remit, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Updates contents of the {@code ViewHolder} to displays movie data at the specified position.
         */
        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            Target values = mValuesArray[position];
            if (values == null) return;

            String ein = values.getEin();
            String name = values.getName();
            final double percent = values.getPercent();
            final double amount = percent * mAmount;

            if (name.length() > 35) { name = name.substring(0, 35);
                name = name.substring(0, name.lastIndexOf(" ")).concat("..."); }

            holder.mNameView.setText(name);
            holder.mIdView.setText(String.format("EIN: %s", ein));
            holder.mAmountView.setText(CURRENCY_FORMATTER.format(amount));

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

        private void swapValues(Target[] targets) {
            mValuesArray = targets;
            notifyDataSetChanged();
        }

        /**
         * Provides ViewHolders for binding Adapter list items to the presentable area in {@link RecyclerView}.
         */
        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.remit_primary) TextView mNameView;
            @BindView(R.id.remit_secondary) TextView mIdView;
            @BindView(R.id.remit_amount) TextView mAmountView;

            /**
             * Constructs this instance with the list item Layout generated from Adapter onCreateViewHolder.
             */
            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}


