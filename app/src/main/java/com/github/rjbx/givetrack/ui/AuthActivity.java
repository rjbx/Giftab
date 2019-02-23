package com.github.rjbx.givetrack.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import com.github.rjbx.givetrack.BuildConfig;
import com.github.rjbx.givetrack.R;
import com.github.rjbx.givetrack.data.DatabaseAccessor;
import com.github.rjbx.givetrack.data.DatabaseCallbacks;
import com.github.rjbx.givetrack.data.DatabaseContract;
import com.github.rjbx.givetrack.data.DatabaseController;
import com.github.rjbx.givetrack.data.UserPreferences;
import com.github.rjbx.givetrack.data.entry.User;
import com.github.rjbx.givetrack.data.DatabaseService;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// TODO: Disable remote persistence for guests
// TODO: Replace remote database interactions with DatabaseService calls
/**
 * Provides a login screen.
 */
public class AuthActivity extends AppCompatActivity implements
        DatabaseController {

    private static final int REQUEST_SIGN_IN = 0;

    public static final String ACTION_SIGN_IN = "com.github.rjbx.givetrack.ui.action.SIGN_IN";
    public static final String ACTION_SIGN_OUT = "com.github.rjbx.givetrack.ui.action.SIGN_OUT";
    public static final String ACTION_DELETE_ACCOUNT = "com.github.rjbx.givetrack.ui.action.DELETE_ACCOUNT";

    private boolean mPendingResult;
    private boolean mFirebaseUpdated;
    private List<User> mUsers;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    @BindView(R.id.auth_progress) ProgressBar mProgressbar;

    /**
     * Handles sign in, sign out, and account deletion launch Intent actions.
     * @param savedInstanceState
     */
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        getSupportLoaderManager().initLoader(DatabaseContract.LOADER_ID_USER, null, new DatabaseCallbacks(this));
    }

    /**
     * Registers the Activity to listen for changes to the last Preference in UserPreferences
     * in order to launch MainActivity.
     */
    @Override protected void onResume() {
        super.onResume();
    }

    /**
     * Hides {@link ProgressBar} when launching AuthUI
     * and unregisters this Activity from listening to Preference changes
     * in order to prevent relaunching MainActivity.
     */
    @Override protected void onStop() {
        mProgressbar.setVisibility(View.GONE);
        super.onStop();
    }

    @Override public void onBackPressed() { finish(); }

    /**
     * Defines behavior on user submission of login credentials.
     */
    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                //TODO: Migrate to DatabaseAccessors as part of User fetch logic
                mFirebaseDatabase.getReference("users").addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mFirebaseUpdated) return;
                        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                        if (firebaseUser == null) return;
                        Timber.v(firebaseUser.getUid());
                        User activeUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                        if (activeUser == null) activeUser = UserPreferences.generateUserProfile();
                        if (!mUsers.contains(activeUser)) mUsers.add(activeUser);
                        for (int i = 0; i < mUsers.size(); i++)
                            mUsers.get(i).setActive(mUsers.get(i).getUid().equals(activeUser.getUid()));
                        User[] userArray = mUsers.toArray(new User[mUsers.size()]);
                        DatabaseService.startActionUpdateUser(AuthActivity.this, userArray);
                        DatabaseAccessor.updateFirebaseUser(userArray);
                        mFirebaseUpdated = true;
                        startActivity(new Intent(AuthActivity.this, MainActivity.class).setAction(ACTION_SIGN_IN));
                        finish();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError databaseError) { Timber.e(databaseError.getMessage()); }
                });
            } else {
                IdpResponse response = IdpResponse.fromResultIntent(data);
                mProgressbar.setVisibility(View.VISIBLE);
                String message;
                if (response == null) message = getString(R.string.network_error_message);
                else message = getString(R.string.provider_error_message, response.getProviderType());
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLoadFinished(int id, Cursor cursor) {
        if (mPendingResult) return;
        mUsers = DatabaseAccessor.getEntryListFromCursor(cursor, User.class);
        cursor.close();
        if (mUsers.isEmpty()) {
            // TODO DatabaseAccessor.fetchUser()
            return;
        }
        handleAction();
    }

    @Override
    public void onLoaderReset() {
        mUsers = null;
    }

    private void handleAction() {

        String launchingAction = getIntent().getAction();

        // TODO: Enable user selection and persist preference to active attribute across users
        User user = null;
        for (User u : mUsers) if (u.getActive()) user = u;

        if (launchingAction != null) {
            switch (launchingAction) {
                case ACTION_SIGN_OUT:
                    mFirebaseDatabase.getReference("users").child(user.getUid())
                            .updateChildren(user.toParameterMap())
                            .addOnCompleteListener(updatedChildrenTask ->
                                    AuthUI.getInstance().signOut(AuthActivity.this)
                                            .addOnCompleteListener(signedOutTask -> {
                                                finish();
                                                startActivity(new Intent(AuthActivity.this, AuthActivity.class).setAction(Intent.ACTION_MAIN));
                                                Toast.makeText(AuthActivity.this, getString(R.string.message_logout), Toast.LENGTH_SHORT).show();
                                            }));
                    break;
                // TODO: Ensure all data is properly removed
                case ACTION_DELETE_ACCOUNT:
                    DatabaseService.startActionResetData(AuthActivity.this);
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                    if (firebaseUser == null || firebaseUser.getEmail() == null) return;
                    String userId = firebaseUser.getUid();
                    firebaseUser.reauthenticate(EmailAuthProvider.getCredential(firebaseUser.getEmail(), getString(R.string.message_password_request)))
                            .addOnCompleteListener(signedOutTask ->
                                    firebaseUser.delete().addOnCompleteListener(completedTask -> {
                                        if (completedTask.isSuccessful()) {
                                            mFirebaseDatabase.getReference("users").child(userId).removeValue()
                                                    .addOnCompleteListener(removedValueTask -> {
                                                        AuthUI.getInstance().signOut(this);
                                                        finish();
                                                        startActivity(new Intent(AuthActivity.this, AuthActivity.class).setAction(Intent.ACTION_MAIN));
                                                        Toast.makeText(AuthActivity.this, getString(R.string.message_data_erase), Toast.LENGTH_LONG).show();
                                                    });
                                        }
                                    })
                            );
                    break;
                // TODO: Ensure new users are properly logged in
                case Intent.ACTION_MAIN:
                    if (mFirebaseAuth.getCurrentUser() == null) {

                        List<AuthUI.IdpConfig> providers = new ArrayList<>();
                        providers.add(new AuthUI.IdpConfig.GoogleBuilder().build());
                        providers.add(new AuthUI.IdpConfig.EmailBuilder().build());
                        providers.add(new AuthUI.IdpConfig.AnonymousBuilder().build());

                        Intent signIn = AuthUI.getInstance().createSignInIntentBuilder()
                                .setLogo(R.mipmap.ic_launcher_round)
                                .setTosAndPrivacyPolicyUrls("https://github.com/rjbx/Givetrack", "https://github.com/rjbx/Givetrack")
                                .setTheme(R.style.AppTheme_AuthOverlay)
                                .setIsSmartLockEnabled(false, true)
                                .setAvailableProviders(providers)
                                .build();
                        startActivityForResult(signIn, REQUEST_SIGN_IN);
                        mPendingResult = true;
                    } else {
                        finish();
                        Toast.makeText(this, getString(R.string.message_login), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class).setAction(AuthActivity.ACTION_SIGN_IN));
                    }
            }
        }
    }
}