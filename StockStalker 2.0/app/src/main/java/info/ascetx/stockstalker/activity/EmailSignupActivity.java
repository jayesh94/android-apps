package info.ascetx.stockstalker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;

import static info.ascetx.stockstalker.MainActivity.TAG_MAIN_FRAME;

public class EmailSignupActivity extends AppCompatActivity {

    private EditText inputFullName, inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private SignInButton btnGoogleSignIn;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private AppEventsLogger logger;

    private String TAG = "EmailSignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        logger = AppEventsLogger.newLogger(this);

        btnGoogleSignIn = findViewById(R.id.btn_gp_sign_in);
        btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputFullName = (EditText) findViewById(R.id.full_name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        /*btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmailSignupActivity.this, ResetPasswordActivity.class));
            }
        });*/

        // Customizing G+ button
        btnGoogleSignIn.setSize(SignInButton.SIZE_STANDARD);
        TextView textView = (TextView) btnGoogleSignIn.getChildAt(0);
        textView.setText(R.string.btn_register_google);

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmailSignupActivity.this, SignupActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("google_signup",true);
                startActivity(intent);
                finish();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fullName = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(fullName)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.et_name_error), Toast.LENGTH_SHORT).show();
                    inputFullName.setError(getString(R.string.et_name_error));
                    return;
                }

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

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), getString(R.string.et_valid_password_error), Toast.LENGTH_SHORT).show();
                    inputPassword.setError(getString(R.string.et_valid_password_error));
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast.makeText(getApplicationContext(), getString(R.string.et_valid_email_error), Toast.LENGTH_SHORT).show();
                    inputEmail.setError(getString(R.string.et_valid_email_error));
                    return;
                }

                createUser(fullName, email, password);

            }
        });
    }

    public void createUser(final String fullName, final String email, final String password){
        progressBar.setVisibility(View.VISIBLE);
        //create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(EmailSignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Toast.makeText(EmailSignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(EmailSignupActivity.this, "Authentication failed." + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        } else {

                            logCompleteRegistrationEvent("Email");

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            if (auth.getCurrentUser()!=null) {
                                auth.getCurrentUser().updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated. Full Name: " + auth.getCurrentUser().getDisplayName());
                                                }
                                                startMainActivity();
                                            }
                                        });
                            } else {
                                startMainActivity();
                            }
                        }
                    }
                });
    }

    private void startMainActivity(){
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(EmailSignupActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("fragment",TAG_MAIN_FRAME);
        startActivity(intent);
        finish();
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void logCompleteRegistrationEvent (String registrationMethod) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, registrationMethod);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
