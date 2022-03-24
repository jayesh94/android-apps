package app.mugup.mugup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import app.mugup.mugup.MainActivity;
import app.mugup.mugup.R;
import app.mugup.mugup.app.CheckUserDevices;

import static app.mugup.mugup.MainActivity.TAG_HOME_FRAME;

public class FirstActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FirstActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;

    protected static final String PREF_FIRST_RUN_SENT = "first_run_sent";
    public static final String PREF_INSTALL_REFERRER_CODE = "install_referrer_code";

    public static GoogleSignInAccount googleSignInAccount;

    public static GoogleApiClient mGoogleApiClient;
    public static GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private SignInButton btnSignIn;
    private Button btnEmailSignIn;
    private EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        Log.e(TAG, "onCreate");

        btnSignIn =  findViewById(R.id.btn_gp_sign_in);
        btnEmailSignIn = findViewById(R.id.btn_email_sign_in);
        etEmail = findViewById(R.id.et_email);

        btnSignIn.setOnClickListener(this);
        btnEmailSignIn.setOnClickListener(this);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final Boolean[] authFlag = {false};

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // The install event may have been sent by the InstallReferrerReceiver,
        // so first-run and install are not always sent at the same time.
        // Since 3.x, the marketplace behavior has been to fire the INSTALL_REFERRER intent
        // after first launch.  So we are leaving FIRST_RUN here and letting the InstallReferrerReceiver
        // fire the INSTALL event.  Otherwise, we will either get two install events or one without the
        // referrer value, which is useful for determining proper attribution.
        if (!prefs.getBoolean(PREF_FIRST_RUN_SENT, false)) {
            prefs.edit().putBoolean(PREF_FIRST_RUN_SENT, true).apply();
        }

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                hideProgressDialog();
                if (authFlag[0] == false) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    Log.e(TAG, "onAuthStateChanged");
                    if (user != null) {
                        if (user.getPhoneNumber() != null) {
                            if (!user.getPhoneNumber().equals("")) {
                                Log.e(TAG, "User not null");
                                // user auth state is changed - user is null
                                // launch login activity
                                Intent intent = new Intent(FirstActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("fragment",TAG_HOME_FRAME);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }
                authFlag[0] = true;
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customizing G+ button
        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        btnSignIn.setScopes(gso.getScopeArray());
        TextView textView = (TextView) btnSignIn.getChildAt(0);
        textView.setText("Sign in with Google");
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_gp_sign_in:
                signIn();
                break;

            case R.id.btn_email_sign_in:
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("etEmail",etEmail.getText().toString().trim());
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            googleSignInAccount = acct;
                            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID);
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            try {
                                Log.d(TAG, user.getPhoneNumber());

                                CheckUserDevices checkUserDevices = new CheckUserDevices(FirstActivity.this, user.getEmail(), android_id, "sign_in");
                                checkUserDevices.verifyUserDeviceNos();

                                //TODO check for device (Account will exist) (if no error and error_msg is not 'Warning' If account linked then continue)
                                //TODO (If no error and error_msg is Warning then show dialogue) (If ignore add deviceId to device 2 and sign in)
//                                Intent intent = new Intent(FirstActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                intent.putExtra("fragment",TAG_HOME_FRAME);
//                                startActivity(intent);
//                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();

                                CheckUserDevices checkUserDevices = new CheckUserDevices(FirstActivity.this, user.getEmail(), android_id, "sign_up");
                                checkUserDevices.verifyUserDeviceNos();

                                //TODO check for device (Account will not exist) (If no error then continue)
//                                startActivity(new Intent(FirstActivity.this, PhoneActivity.class));
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
        showProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if (user.getPhoneNumber() == null) {
                auth.signOut();
                // Google sign out
                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.e(TAG, "G Sign Out");
                            }
                        });
            } else {
                if (user.getPhoneNumber().equals("")) {
                    auth.signOut();
                    // Google sign out
                    mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.e(TAG, "G Sign Out");
                                }
                            });
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}