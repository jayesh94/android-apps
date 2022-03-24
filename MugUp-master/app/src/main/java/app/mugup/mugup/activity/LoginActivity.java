package app.mugup.mugup.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import app.mugup.mugup.MainActivity;
import app.mugup.mugup.R;
import app.mugup.mugup.app.CheckUserDevices;

import static app.mugup.mugup.MainActivity.TAG_HOME_FRAME;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                Log.e(TAG, user.getProviderId());
                if (user.getProviderId().equals("password")) {
                    startActivity(new Intent(LoginActivity.this, PhoneActivity.class));
                }
            }
        }

        // set the view now
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);

        Intent myIntent = getIntent(); // gets the previously created intent
        String etEmail = myIntent.getStringExtra("etEmail");
        inputEmail.setText(etEmail);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.et_email_error), Toast.LENGTH_SHORT).show();
                    inputEmail.setError(getString(R.string.et_email_error));
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.et_password_error), Toast.LENGTH_SHORT).show();
                    inputPassword.setError(getString(R.string.et_password_error));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                                            Settings.Secure.ANDROID_ID);
                                    try {
                                        Log.d(TAG, auth.getCurrentUser().getPhoneNumber());

                                        CheckUserDevices checkUserDevices = new CheckUserDevices(LoginActivity.this, auth.getCurrentUser().getEmail(), android_id, "sign_in");
                                        checkUserDevices.verifyUserDeviceNos();

                                        //TODO check for device (Account will exist) (if no error and error_msg is not 'Warning' If account linked then continue)
                                        //TODO (If no error and error_msg is Warning then show dialogue) (If ignore add deviceId to device 2 and sign in)
//                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        intent.putExtra("fragment",TAG_HOME_FRAME);
//                                        startActivity(intent);
//                                        finish();
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                        CheckUserDevices checkUserDevices = new CheckUserDevices(LoginActivity.this, auth.getCurrentUser().getEmail(), android_id, "sign_up");
                                        checkUserDevices.verifyUserDeviceNos();

                                        //TODO check for device (Account will not exist) (If no error then continue)
//                                        startActivity(new Intent(LoginActivity.this, PhoneActivity.class));
                                    }
                                }
                            }
                        });
            }
        });
    }
}