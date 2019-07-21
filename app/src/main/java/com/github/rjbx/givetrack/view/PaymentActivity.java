package com.github.rjbx.givetrack.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.rjbx.givetrack.R;

import com.google.android.gms.wallet.*;

/**
 * Provides a UI for and manages payment confirmation and processing.
 */
public class PaymentActivity extends AppCompatActivity {
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
    }
}


