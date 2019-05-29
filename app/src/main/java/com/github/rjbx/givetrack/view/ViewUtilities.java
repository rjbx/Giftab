package com.github.rjbx.givetrack.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import timber.log.Timber;

final class ViewUtilities {


    /**
     * Defines and launches {@link CustomTabsIntent} for displaying an integrated browser at the given URL.
     */
    static void launchBrowserIntent(Context context, Uri webUrl) {
        CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                .setToolbarColor(context.getColor(R.color.colorPrimaryDark))
                .addDefaultShareMenuItem()
                .enableUrlBarHiding()
                .build();

        tabsIntent.intent.putExtra(
                        Intent.EXTRA_REFERRER,
                        Uri.parse(String.format("android-app://%s", context.getString(R.string.app_name))));

        tabsIntent.launchUrl(context, webUrl);
    }

    static void launchSocialIntent(Context context, String handle) {
        try {
            context.getPackageManager().getPackageInfo("com.android.twitter", 0);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("twitter://user?user_id=%s", handle)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e);
            launchBrowserIntent(context, Uri.parse(String.format("https://twitter.com/%s", handle)));
        }
    }

    static void launchPhoneIntent(Context context, String number) {
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + number));
        if (phoneIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(phoneIntent);
        }
    }

    static void launchMapIntent(Context context, String location) {
        Uri intentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }

    static void launchDetailPane(Activity launchingActivity, View master, View detail) {

        DisplayMetrics metrics = new DisplayMetrics();
        launchingActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (width * .5f), ViewGroup.LayoutParams.MATCH_PARENT);
        master.setLayoutParams(params);
        detail.setVisibility(View.VISIBLE);
        detail.setLayoutParams(params);
    }

    static void launchShareIntent(Activity launchingActivity, String textMessage) {
        IntentBuilder intentBuilder = IntentBuilder.from(launchingActivity)
                .setType("text/plain")
                .setText(textMessage);
        launchingActivity.startActivity(intentBuilder.getIntent());
    }

    static Toast centerToastMessage(Toast toast) {
        View view = toast.getView().findViewById(android.R.id.message);
        if (view != null) view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return toast;
    }

    /**
     * Defines and launches Intent for displaying a {@link android.preference.PreferenceFragment}.
     */
    static void launchPreferenceFragment(Context context, String action) {
        Intent filterIntent = new Intent(context, ConfigActivity.class);
        filterIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, getPreferenceFragmentName(action));
        filterIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        filterIntent.setAction(action);
        context.startActivity(filterIntent);
    }

    static String getPreferenceFragmentName(String action) {
        switch (action) {
            case HomeActivity.ACTION_HOME_INTENT:
                return ConfigActivity.HomePreferenceFragment.class.getName();
            case IndexActivity.ACTION_INDEX_INTENT:
                return ConfigActivity.IndexPreferenceFragment.class.getName();
            case JournalActivity.ACTION_JOURNAL_INTENT:
                return ConfigActivity.JournalPreferenceFragment.class.getName();
            default:
                throw new IllegalArgumentException(
                        String.format("Action must derive from %s, %s or %s",
                                HomeActivity.ACTION_HOME_INTENT,
                                IndexActivity.ACTION_INDEX_INTENT,
                                JournalActivity.ACTION_JOURNAL_INTENT
                        ));
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

                // TODO: Launch with social app intent
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
            launchPhoneIntent(mContext, mPhone);
        }

        /**
         * Defines behavior on click of social launch button.
         */
        @Optional @OnClick(R.id.social_button) void launchSocial() {
            launchSocialIntent(mContext, mSocial);
        }

        /**
         * Defines behavior on click of website launch button.
         */
        @Optional @OnClick(R.id.website_button) void launchWebsite() {
            launchBrowserIntent(mContext, Uri.parse(mWebsite));
        }

        /**
         * Defines behavior on click of map launch button.
         */
        @Optional @OnClick(R.id.location_button) void launchMap() {
            launchMapIntent(mContext, mLocation);
        }
    }
}
