package info.ascetx.stockstalker.activity;

/**
 * Created by JAYESH on 18-03-2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

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


public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private final String URL_LOGIN = Config.URL_LOGIN;
    private final String URL_FORGOT_PASS = Config.URL_FORGOT_PASS;
    private Button btnLogin, btnLinkToRegister, btnSkip, btnForgotPass;
    private EditText inputEmail;
    private EditText inputPassword;
    private TextView appNameTV;
    private ProgressDialog pDialog;
    private SessionManager session;
    private LoginHandler db;
//    public static BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnSkip = (Button) findViewById(R.id.btnSkip);
        btnForgotPass = (Button) findViewById(R.id.btnForgotPass);
        appNameTV = (TextView) findViewById(R.id.app_name_tv);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/rajdhani-bold.ttf");
        appNameTV.setTypeface(custom_font);
//        Log.e(TAG, "Login activity on create ");

/*
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "Broadcast on receive : ");
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
//                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    Log.e(TAG, "Registration complete and subscribed to Topic global ");
//                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    Log.e(TAG, "Firebase message : " + message);
                    String replace = message.replaceAll("(\\[)|(\")|(\\])","");
                    String allStock = replace.replaceAll(",","\n");
                    Toast.makeText(getApplicationContext(), "Check Stocks Trend: \n" + allStock, Toast.LENGTH_LONG).show();

                }
            }
        };

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(LoginActivity.mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(LoginActivity.mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
//        NotificationUtils.clearNotifications(getApplicationContext());
*/

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        // SQLite database handler
        db = new LoginHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    showSnackBar(view,"Please enter your credentials");
                }
            }

        });

        btnForgotPass.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty()) {
                    // user forgot pass
                    updatePass(email);
                } else {
                    showSnackBar(view,"Please enter your email id");
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
//                Log.e(TAG,"Skip clicked");
//                showSnackBar(view , "Skip registration");
                session.setSkip(true);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void updatePass(final String email) {
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Forgot Password");
        alert.setMessage("Mail will be sent to you for update of password"); //Message here

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                doUpdatePass(email);
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
       /* Alert Dialog Code End*/
    }

    private void doUpdatePass(final String email){
        // Tag used to cancel the request
        String tag_string_req = "req";

        pDialog.setMessage("Sending mail ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                URL_FORGOT_PASS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        String msg = jObj.getString("msg");
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    } else {
                        // Error in login. Get the error message
                        String msg = jObj.getString("msg");
                        showSnackBar(getWindow().getDecorView().getRootView(),msg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                showSnackBar(getWindow().getDecorView().getRootView(),getResources().getString(R.string.msg_check_internet));
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("ForgotPassword", "yes");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);
                        session.setSkip(false);

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
//                        String number = user.getString("phone_number");
                        String created_at = user.getString("created_at");
//                        String log_in = user.getString("log_in");

                        Log.e(TAG, name);
                        Log.e(TAG, email);

                        // Inserting row in users table
                        db.addUser(name, email);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
//                        Toast.makeText(getApplicationContext(),
//                                errorMsg, Toast.LENGTH_LONG).show();
                        showSnackBar(getWindow().getDecorView().getRootView(),errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, "Login Error: " + error.getMessage());
//                Toast.makeText(getApplicationContext(),
//                        " Check Internet connection ", Toast.LENGTH_LONG).show();
                showSnackBar(getWindow().getDecorView().getRootView(),"Please check internet connection");
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

    @Override
    public void onBackPressed() {
        AppController.getInstance().cancelPendingRequests("req");
        super.onBackPressed();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (this.isDestroyed()) {
            return;
        }
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /*private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);
//        Toast.makeText(this, "Firebase reg id: " + regId, Toast.LENGTH_SHORT).show();

        if (TextUtils.isEmpty(regId))
            Log.e(TAG, "Firebase Reg Id is not received yet!");

    }*/
}
