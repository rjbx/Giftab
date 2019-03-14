package com.github.rjbx.givetrack.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.entry.Company;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ShareCompat.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

final class ViewUtilities {

    static void launchBrowserIntent(Context context, Uri webUrl) {
        new CustomTabsIntent.Builder()
                .setToolbarColor(context.getColor(R.color.colorPrimaryDark))
                .build()
                .launchUrl(context, webUrl);
    }

    static void launchShareIntent(Activity launchingActivity, String textMessage) {
        IntentBuilder intentBuilder = IntentBuilder.from(launchingActivity)
                .setType("text/plain")
                .setText(textMessage);
        launchingActivity.startService(intentBuilder.getIntent());
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
        private static String mSocial;
        private static String mWebsite;
        private static String mLocation;
        @BindView(R.id.phone_button) @Nullable Button mPhoneButton;
        @BindView(R.id.email_button) @Nullable Button mEmailButton;
        @BindView(R.id.social_button) @Nullable Button mSocialButton;
        @BindView(R.id.website_button) @Nullable Button mWebsiteButton;
        @BindView(R.id.location_button) @Nullable Button mLocationButton;

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

            if (mSocialButton != null)
                if (mSocial.isEmpty()) mSocialButton.setVisibility(View.GONE);
                else mSocialButton.setText(mSocial.toLowerCase());

            if (mLocationButton != null)
                if (mLocation.isEmpty()) mLocationButton.setVisibility(View.GONE);
                else mLocationButton.setText(mLocation);
        }

        /**
         * Initializes value instance fields and generates an instance of this layout.
         */
        public static ContactDialogLayout getInstance(AlertDialog alertDialog, Company values) {
            mAlertDialog = alertDialog;
            mPhone = values.getPhone();
            mEmail = values.getEmail();
            mSocial = values.getSocial();
            mWebsite = values.getHomepageUrl();
            mLocation = valuesToAddress(values);
            return new ContactDialogLayout(mAlertDialog.getContext());
        }

        /**
         * Converts a set of ContentValues to a single formatted String.
         */
        private static String valuesToAddress(Company values) {
            String street = values.getLocationStreet();
            String detail = values.getLocationDetail();
            String city = values.getLocationCity();
            String state = values.getLocationState();
            String zip = values.getLocationZip();
            return street + (detail.isEmpty() ? "" : '\n' + detail) + '\n' + city + ", " + state.toUpperCase() + " " + zip;
        }

        /**
         * Defines behavior on click of email launch button.
         */
        @Optional
        @OnClick(R.id.email_button) void launchEmail() {
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
         * Defines behavior on click of social launch button.
         */
        @Optional @OnClick(R.id.social_button) void launchSocial() {
            launchBrowserIntent(mContext, Uri.parse(mWebsite));
        }

        /**
         * Defines behavior on click of website launch button.
         */
        @Optional @OnClick(R.id.website_button) void launchWebsite() {
            launchBrowserIntent(mContext, Uri.parse("https://twitter.com/" + mSocial));
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
