package info.ascetx.stockstalker.activity;

/**
 * Created by JAYESH on 18-03-2017.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.Config;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.dbhandler.LoginHandler;

public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";
    private String[] signupDetails = new String[3];
    private final String URL_REGISTER = Config.URL_REGISTER;
    private boolean registered = false;
//    private int regorNot = 1;
    private Button btnRegister;
    private Button btnLinkToLogin;
    private Button btnSkip;
    private EditText inputFullName, inputEmail, inputPassword;
    private TextView appNameTV;
//    public boolean phoneVerified = false;
    private CheckBox checkBox;
//    private ProgressDialog pDialog;
    private SessionManager session;
    private LoginHandler ldb;
    private View mProgressView;
    private View mRegisterFormView;
//    private String phNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        appNameTV = (TextView) findViewById(R.id.app_name_tv);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/rajdhani-bold.ttf");
        appNameTV.setTypeface(custom_font);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        btnSkip = (Button) findViewById(R.id.btnSkip);
        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.login_progress);

        checkBox = (CheckBox) findViewById(R.id.cb_show_password);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // to encode password in dots
                    inputPassword.setTransformationMethod(null);
                } else {
                    // to display the password in normal text
                    inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });


        /*inputPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    regorNot = 1;
                    String name = inputFullName.getText().toString().trim();
                    String email = inputEmail.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();
                    signupDetails[0] = name;
                    signupDetails[1] = email;
                    signupDetails[2] = password;
                    Log.e(TAG,"IME click"+ name+email+password);
                    Log.e(TAG, name+email+password);

                    if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                        new SignUpValidation(signupDetails);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter your details", Toast.LENGTH_LONG)
                                .show();
                    }
                    return true;
                }
                return false;
            }
        });*/

        // Progress dialog
//        pDialog = new ProgressDialog(this);
//        pDialog.setCancelable(true);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        ldb = new LoginHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }
//        else if (session.isSkipped()){
//            // TODO: 03-05-2017  manipulate all the buttons so as to access only selective frames. Make skipped false when logging out.
////            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Skip registration", Snackbar.LENGTH_LONG).setAction("Action",null);
////            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_err_color));
//
//        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                Log.e(TAG, "Register Clicked");
//                regorNot = 1;
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                signupDetails[0] = name;
                signupDetails[1] = email;
                signupDetails[2] = password;
                Log.e(TAG,"register click "+ name+email+password);

//                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    new SignUpValidation(signupDetails);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "Please enter your details", Toast.LENGTH_LONG)
//                            .show();
//                }

            }

        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
//                finish();
            }
        });

    }

    private void showSnackBar(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.snackbar_err_color));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private class SignUpValidation {

        private SignUpValidation(String[] details) {

            inputFullName.setError(null);
            inputEmail.setError(null);
            inputPassword.setError(null);
            Log.e(TAG, "In sign up val");

            boolean cancel = false;
            View focusView = null;

            // Check for a valid full name, if the user entered one.
            if (details[0].isEmpty()){
                Log.e(TAG, "name empty "+details[0]);
                inputFullName.setError(getString(R.string.error_field_required));
                focusView = inputFullName;
                cancel = true;
            }
            else if (!valFullName(details[0])) {
                inputFullName.setError(getString(R.string.full_name_err));
                focusView = inputFullName;
                cancel = true;
            }
            // Check for a valid email id, if the user entered one.
            if (details[1].isEmpty()){
                Log.e(TAG, "email empty "+details[1]);
                inputEmail.setError(getString(R.string.error_field_required));
                focusView = inputEmail;
                cancel = true;
            }
            else if (!valEmailId(details[1])) {
                inputEmail.setError(getString(R.string.email_id_err));
                focusView = inputEmail;
                cancel = true;
            }

            if (details[2].isEmpty() || !valPassword(details[2])) {
                inputPassword.setError(getString(R.string.password_err));
                focusView = inputPassword;
                cancel = true;
            }

//            if (!phoneVerified){
//                digitsButton.setError("Please verify your phone number");
//                focusView = digitsButton;
//                cancel  = true;
//            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
//                showProgress(true);
//                mAuthTask = new UserLoginTask(email, password);
//                mAuthTask.execute((Void) null);
                registerUser(details[0], details[1], details[2]);
            }

//            checkRegisterDetails();
//            Log.e(TAG, "validateFields "+validateFields.toString());
//            validateFields.clear();
        }

        private boolean valFullName(String fn){
            return fn.matches("([a-zA-Z]+\\s*)+");
        }

        private boolean valEmailId(String ei) {
            return ei.matches("^[a-zA-Z0-9._%+-]{1,64}@(?:[a-zA-Z0-9-]{1,63}\\.){1,125}[a-zA-Z]{2,63}$");
        }
        private boolean valPassword(String pwd) {
            return pwd.matches(".{3,15}");
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

//        pDialog.setMessage("Registering ...");
//        showDialog();
        showProgress(true);
        StringRequest strReq = new StringRequest(Method.POST,
                URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
//                hideDialog();
                showProgress(false);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Log.e(TAG,"registerUser no error");
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
//                        String number = user.getString("phone_number");
                        String created_at = user.getString("created_at");

                        // Inserting row in users table
                        ldb.addUser(name, email);

                        session.setLogin(true);
                        session.setSkip(false);

                        // Launch main activity
                        if(session.isLoggedIn()) {
                            Log.e(TAG,"isLoggedIn");
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_successful_registration), Toast.LENGTH_LONG).show();

                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(getApplicationContext(),
//                                errorMsg, Toast.LENGTH_LONG).show();
                        showSnackBar(getWindow().getDecorView().getRootView(),errorMsg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
//                Toast.makeText(getApplicationContext(),
//                        " Check Internet connection ", Toast.LENGTH_LONG).show();
                showSnackBar(getWindow().getDecorView().getRootView(),getResources().getString(R.string.msg_check_internet));
//                hideDialog();
                showProgress(false);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
//                params.put("number", phNumber);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    /*private void showDialog() {
//        if (!pDialog.isShowing())
//            pDialog.show();
    }

    private void hideDialog() {
//        if (pDialog.isShowing())
//            pDialog.dismiss();
    }*/
}
