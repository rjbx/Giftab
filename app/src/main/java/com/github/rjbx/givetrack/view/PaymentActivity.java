package com.github.rjbx.givetrack.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.rjbx.givetrack.R;
import com.google.android.gms.wallet.*;
import com.google.android.gms.common.api.*;

import java.util.Arrays;


/**
 * Provides a UI for and manages payment confirmation and processing.
 */
public class PaymentActivity extends AppCompatActivity {

    private float mPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        PaymentsClient paymentsClient
                = Wallet.getPaymentsClient(this, new Wallet
                        .WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .build()
        );

        isReadyToPay(paymentsClient);
    }

    private void isReadyToPay(PaymentsClient client) {
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();
        client.isReadyToPay(request).addOnCompleteListener((task) -> {
                        try {
                            final Boolean result = task.getResult(ApiException.class);
                            if (result != null && result) createPaymentDataRequest();
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
                                .setTotalPrice(String.valueOf(mPrice))
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
}


