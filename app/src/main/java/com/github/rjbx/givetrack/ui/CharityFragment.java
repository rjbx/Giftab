package com.github.rjbx.givetrack.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.DataService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;

/**
 * Provides the logic and views for a single Charity detail screen.
 */
public class CharityFragment extends Fragment {

    public static final String ARG_ITEM_NAME = "item_name";
    public static final String ARG_ITEM_EIN = "item_ein";
    public static final String ARG_ITEM_URL= "item_url";
    private static final String SCROLL_STATE = "state_scroll";
    private static final String INITIAL_STATE = "state_initial";
    private static final String CURRENT_STATE = "state_current";
    private static String mName;
    private static String mEin;
    private static String mUrl;
    private static int mScrollState = 0;
    private static boolean mInitialSaveState;
    private static boolean mCurrentSaveState;
    private AppCompatActivity mParentActivity;
    private MasterDetailFlow mMasterDetailFlow;
    private WebView mWebView;
    private FloatingActionButton mFab;

    /**
     * Provides callback interface for updating parent Layout on interaction with this Fragment.
     */
    public interface MasterDetailFlow {
        boolean isDualPane();
        void showDualPane(Bundle args);
        void showSinglePane();
    }

    /**
     * Provides default constructor required for the {@link androidx.fragment.app.FragmentManager}
     * to instantiate this Fragment.
     */
    public CharityFragment() {}

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    public static CharityFragment newInstance(@Nullable Bundle args) {
        CharityFragment fragment = new CharityFragment();
        if (args != null) fragment.setArguments(args);
        return fragment;
    }

    /**
     * Saves references to parent Activity or Fragment.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null) return;
        mParentActivity = (AppCompatActivity) getActivity();

        Fragment parentFragment = getParentFragment();
        mMasterDetailFlow = parentFragment == null ? (MasterDetailFlow) mParentActivity : (MasterDetailFlow) parentFragment;
    }

    /**
     * Initializes collection status, populates {@link WebView} and defines Button onClick behavior
     * of this Fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_charity, container, false);

        if (getActivity() != null) {
            mParentActivity = (AppCompatActivity) getActivity();

            Fragment parentFragment = getParentFragment();
            mMasterDetailFlow = parentFragment == null ? (MasterDetailFlow) mParentActivity : (MasterDetailFlow) parentFragment;
        }

        mFab = rootView.findViewById(R.id.charity_fab);
        if (mFab != null) {
            if (mMasterDetailFlow == mParentActivity) mFab.setOnClickListener(clickedView -> onClickActionButton());
            else ((View) mFab).setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            mScrollState = savedInstanceState.getInt(SCROLL_STATE);
            mInitialSaveState = savedInstanceState.getBoolean(INITIAL_STATE);
            mCurrentSaveState = savedInstanceState.getBoolean(CURRENT_STATE);
            mName = savedInstanceState.getString(ARG_ITEM_NAME);
            mEin = savedInstanceState.getString(ARG_ITEM_EIN);
            mUrl = savedInstanceState.getString(ARG_ITEM_URL);
            drawActionButton();
        } else if (getArguments() != null && getArguments().getString(ARG_ITEM_EIN) != null) {

            mName = getArguments().getString(ARG_ITEM_NAME);
            mEin = getArguments().getString(ARG_ITEM_EIN);
            mUrl = getArguments().getString(ARG_ITEM_URL);
            mScrollState = 0;
            Uri collectionUri = GivetrackContract.Entry.CONTENT_URI_COLLECTION.buildUpon()
                    .appendPath(mEin).build();
            new StatusAsyncTask(this).execute(collectionUri);
        }


        mWebView = new WebView(inflater.getContext().getApplicationContext());
        mWebView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = (int) getResources().getDimension(R.dimen.text_margin);
        mWebView.setPadding(padding, padding, padding, padding);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                rootView.findViewById(R.id.webview_progress).setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mWebView == null) return;
                rootView.findViewById(R.id.webview_progress).setVisibility(View.GONE);
                mWebView.setScrollY(mScrollState);
                super.onPageFinished(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());

        ((FrameLayout) rootView.findViewById(R.id.webview_frame)).addView(mWebView);
        mWebView.loadUrl(mUrl);

        rootView.findViewById(R.id.browser_open_button).setOnClickListener(clickedView -> {
            new CustomTabsIntent.Builder()
                    .setToolbarColor(getResources()
                    .getColor(R.color.colorPrimaryDark))
                    .build()
                    .launchUrl(mParentActivity, Uri.parse(mUrl));
            mParentActivity.getIntent().setAction(MainActivity.ACTION_CUSTOM_TABS);
        });

        rootView.findViewById(R.id.browser_close_button).setOnClickListener(clickedView ->
            mMasterDetailFlow.showSinglePane());

        return rootView;
    }

    /**
     * Forces garbage collection on {@link WebView} in addition to default behavior.
     */
    @Override
    public void onDestroyView() {
        mWebView.destroy();
        super.onDestroyView();
    }

    /**
     * Saves {@link WebView} scroll state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWebView != null) outState.putInt(SCROLL_STATE, mWebView.getScrollY());
        outState.putString(ARG_ITEM_EIN, mEin);
        outState.putString(ARG_ITEM_URL, mUrl);
        outState.putBoolean(INITIAL_STATE, mInitialSaveState);
        outState.putBoolean(CURRENT_STATE, mCurrentSaveState);
    }

    /**
     * Generates {@link Snackbar} based on item collection status.
     */
    public void drawSnackbar() {
        String message = String.format(getString(mCurrentSaveState ? R.string.message_collected_add : R.string.message_collected_remove), mName);
        Snackbar sb = Snackbar.make(mFab, message, Snackbar.LENGTH_LONG);
        sb.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        sb.show();
    }

    // TODO: Launch Dialog and sync onClick rather than remove item on parent Fragment Lifecycle method invocation
    /**
     * Generates toggle Button based on item collection status.
     */
    public void drawActionButton() {
        if (getContext() == null || mFab == null) return;
        mFab.setImageResource(mCurrentSaveState ?
                R.drawable.action_remove: R.drawable.action_download);
        mFab.setBackgroundTintList(mCurrentSaveState ?
                ColorStateList.valueOf(Color.WHITE) :
                ColorStateList.valueOf(getContext().getResources().getColor(R.color.colorAccent)));
        mFab.setContentDescription(mCurrentSaveState ? getContext().getString(R.string.description_collected_remove_button) :
                mParentActivity.getString(R.string.description_collected_add_button));
        mFab.refreshDrawableState();
    }

    /**
     * Defines behavior onClick of item collection status toggle Button.
     */
    public void onClickActionButton() {
        mCurrentSaveState = !mCurrentSaveState;
        drawActionButton();
        drawSnackbar();
    }

    /**
     * Syncs item collection status only onDestroy in order to prevent multithreading issues on
     * simultaneous sync operations due to repetitive toggling of item collection status.
     */
    @Override public void onDestroy() {
        if (mInitialSaveState != mCurrentSaveState) {
            if (mCurrentSaveState) DataService.startActionCollectGenerated(mParentActivity, mEin);
            else DataService.startActionRemoveCollected(mParentActivity, mEin);
        }
        super.onDestroy();
    }

    /**
     * Confirms whether item exists in collection table and updates status accordingly.
     */
    public static class StatusAsyncTask extends AsyncTask<Uri, Void, Boolean> {

        WeakReference<CharityFragment> mFragment;

        /**
         * Constructs an instance with a Fragment that is converted to a {@link WeakReference} in order
         * to prevent memory leak.
         */
        StatusAsyncTask(CharityFragment charityFragment) {
            mFragment = new WeakReference<>(charityFragment);
        }

        /**
         * Retrieves the item collection status.
         */
        @Override protected Boolean doInBackground(Uri[] uri) {
            Context context = mFragment.get().getContext();
            if (context == null) return null;
            Cursor collectionCursor = context.getContentResolver()
                    .query(uri[0], null, null, null, null);
            if (collectionCursor == null) return null;
            boolean isSaved = collectionCursor.getCount() == 1;
            collectionCursor.close();
            return isSaved;
        }

        /**
         * Updates the Fragment field corresponding to the item collection status.
         */
        @Override protected void onPostExecute(Boolean isSaved) {
            mInitialSaveState = isSaved;
            mCurrentSaveState = mInitialSaveState;
            mFragment.get().drawActionButton();
        }
    }
}