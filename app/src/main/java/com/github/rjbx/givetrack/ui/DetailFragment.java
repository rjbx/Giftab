package com.github.rjbx.givetrack.ui;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.BindView;
import butterknife.OnClick;

import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.GivetrackContract;
import com.github.rjbx.givetrack.data.DataService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;

/**
 * Provides the logic and views for a single Charity detail screen.
 */
public class DetailFragment extends Fragment {

    public static final String ARG_ITEM_NAME = "com.github.rjbx.givetrack.ui.arg.ITEM_NAME";
    public static final String ARG_ITEM_EIN = "com.github.rjbx.givetrack.ui.arg.ITEM_EIN";
    public static final String ARG_ITEM_URL= "com.github.rjbx.givetrack.ui.arg.ITEM_URL";
    private static final String SCROLL_STATE = "com.github.rjbx.givetrack.ui.state.DETAIL_SCROLL";
    private static final String INITIAL_STATE = "com.github.rjbx.givetrack.ui.state.DETAIL_INITIAL";
    private static final String CURRENT_STATE = "com.github.rjbx.givetrack.ui.state.DETAIL_CURRENT";
    private static String sName;
    private static String sEin;
    private static String sUrl;
    private static boolean sInitialState;
    private static boolean sCurrentState;
    private static int sScrollState = 0;
    private AppCompatActivity mParentActivity;
    private MasterDetailFlow mMasterDetailFlow;
    private WebView mWebview;
    private Unbinder mUnbinder;
    @BindView(R.id.detail_fab) FloatingActionButton mFab;
    @BindView(R.id.detail_progress) ProgressBar mProgress;
    @BindView(R.id.detail_frame) FrameLayout mFrame;

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
    public DetailFragment() {}

    /**
     * Provides the arguments for this Fragment from a static context in order to survive lifecycle changes.
     */
    static DetailFragment newInstance(@Nullable Bundle args) {
        DetailFragment fragment = new DetailFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        if (getActivity() != null) {
            mParentActivity = (AppCompatActivity) getActivity();

            Fragment parentFragment = getParentFragment();
            mMasterDetailFlow = parentFragment == null ? (MasterDetailFlow) mParentActivity : (MasterDetailFlow) parentFragment;
        }

        if (savedInstanceState != null) {
            sScrollState = savedInstanceState.getInt(SCROLL_STATE);
            sInitialState = savedInstanceState.getBoolean(INITIAL_STATE);
            sCurrentState = savedInstanceState.getBoolean(CURRENT_STATE);
            sName = savedInstanceState.getString(ARG_ITEM_NAME);
            sEin = savedInstanceState.getString(ARG_ITEM_EIN);
            sUrl = savedInstanceState.getString(ARG_ITEM_URL);
            drawActionButton();
        } else if (getArguments() != null && getArguments().getString(ARG_ITEM_EIN) != null) {

            sName = getArguments().getString(ARG_ITEM_NAME);
            sEin = getArguments().getString(ARG_ITEM_EIN);
            sUrl = getArguments().getString(ARG_ITEM_URL);
            sScrollState = 0;
            Uri collectionUri = GivetrackContract.Entry.CONTENT_URI_COLLECTION.buildUpon()
                    .appendPath(sEin).build();
            new StatusAsyncTask(this).execute(collectionUri);
        }

        mWebview = new WebView(inflater.getContext().getApplicationContext());
        mWebview.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int padding = (int) getResources().getDimension(R.dimen.text_margin);
        mWebview.setPadding(padding, padding, padding, padding);
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgress.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mWebview == null) return;
                mProgress.setVisibility(View.GONE);
                mWebview.setScrollY(sScrollState);
                super.onPageFinished(view, url);
            }
        });
        mWebview.setWebChromeClient(new WebChromeClient());

        mFrame.addView(mWebview);
        mWebview.loadUrl(sUrl);

        if (mMasterDetailFlow != mParentActivity) mFab.setVisibility(View.GONE);

        return rootView;
    }

    /**
     * Forces garbage collection on {@link WebView} in addition to default behavior.
     */
    @Override
    public void onDestroyView() {
        mWebview.destroy();
        super.onDestroyView();
    }

    /**
     * Syncs item collection status only onDestroy in order to prevent multithreading issues on
     * simultaneous sync operations due to repetitive toggling of item collection status.
     */
    @Override public void onDestroy() {
        if (sInitialState != sCurrentState) {
            if (sCurrentState) DataService.startActionCollectGenerated(mParentActivity, sEin);
            else DataService.startActionRemoveCollected(mParentActivity, sEin);
        }
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Saves {@link WebView} scroll state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWebview != null) outState.putInt(SCROLL_STATE, mWebview.getScrollY());
        outState.putString(ARG_ITEM_EIN, sEin);
        outState.putString(ARG_ITEM_URL, sUrl);
        outState.putBoolean(INITIAL_STATE, sInitialState);
        outState.putBoolean(CURRENT_STATE, sCurrentState);
    }

    /**
     * Generates {@link Snackbar} based on item collection status.
     */
    private void drawSnackbar() {
        String message = String.format(getString(sCurrentState ? R.string.message_collected_add : R.string.message_collected_remove), sName);
        Snackbar sb = Snackbar.make(mFab, message, Snackbar.LENGTH_LONG);
        sb.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        sb.show();
    }

    // TODO: Launch Dialog and sync onClick rather than remove item on parent Fragment Lifecycle method invocation
    /**
     * Generates toggle Button based on item collection status.
     */
    private void drawActionButton() {
        if (getContext() == null || mFab == null) return;
        mFab.setImageResource(sCurrentState ?
                R.drawable.action_remove: R.drawable.action_download);
        mFab.setBackgroundTintList(sCurrentState ?
                ColorStateList.valueOf(Color.WHITE) :
                ColorStateList.valueOf(getContext().getResources().getColor(R.color.colorAccent)));
        mFab.setContentDescription(sCurrentState ? getContext().getString(R.string.description_collected_remove_button) :
                mParentActivity.getString(R.string.description_collected_add_button));
        mFab.refreshDrawableState();
    }

    /**
     * Defines behavior onClick of item collection status toggle Button.
     */
    private void onClickActionButton() {
        sCurrentState = !sCurrentState;
        drawActionButton();
        drawSnackbar();
    }

    @OnClick(R.id.browser_close_button) void closeBrowser() {
        mMasterDetailFlow.showSinglePane();
    }

    @OnClick(R.id.browser_open_button) void openBrowser() {
        new CustomTabsIntent.Builder()
                .setToolbarColor(getResources()
                        .getColor(R.color.colorPrimaryDark))
                .build()
                .launchUrl(mParentActivity, Uri.parse(sUrl));
        mParentActivity.getIntent().setAction(MainActivity.ACTION_CUSTOM_TABS);
    }

    @OnClick(R.id.detail_fab) void toggleSaved() {
        onClickActionButton();
    }

    /**
     * Confirms whether item exists in collection table and updates status accordingly.
     */
    private static class StatusAsyncTask extends AsyncTask<Uri, Void, Boolean> {

        private WeakReference<DetailFragment> mFragment;

        /**
         * Constructs an instance with a Fragment that is converted to a {@link WeakReference} in order
         * to prevent memory leak.
         */
        StatusAsyncTask(DetailFragment detailFragment) {
            mFragment = new WeakReference<>(detailFragment);
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
            sInitialState = isSaved;
            sCurrentState = sInitialState;
            mFragment.get().drawActionButton();
        }
    }
}