package info.ascetx.stockstalker.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.dbhandler.LoginHandler;

import static info.ascetx.stockstalker.app.Config.URL_NSQ_USER_LOG_OUT;

/**
 * Created by JAYESH on 03-05-2017.
 */

public class LogOut {

    private static String TAG = "LogOut";
    private LoginHandler dbl;
    private DatabaseHandler db;
    private Activity activity;
    private SessionManager session;
    private ProgressDialog pDialog;
    private SnackBarDisplay snackBarDisplay;
    private View mView;

    public LogOut(Activity activity) {
        this.activity = activity;
        // Progress dialog
        pDialog = new ProgressDialog(this.activity);
        pDialog.setCancelable(false);
        dbl = new LoginHandler(activity);
        db = new DatabaseHandler(activity);
        session = new SessionManager(activity);
        snackBarDisplay = new SnackBarDisplay(activity);
        mView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        logOut();
    }

    private void logOut() {
        HashMap<String, String> user = dbl.getUserDetails();
        String name = user.get("name");
        final String email = user.get("email");
    /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Hi " + name);
        alert.setMessage("Are you sure you want to logout?"); //Message here

        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dbLogout(email);

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

    private void dbLogout(final String email) {
        String tag_string_req = "req";
        pDialog.setMessage(activity.getResources().getString(R.string.logging_out));
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_NSQ_USER_LOG_OUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        session.setLogin(false);
                        session.setSkip(true);
                        // Now store the user in SQLite
                        String msg = jObj.getString("error_msg");
                        Log.e(TAG, "msg: " + msg);

                        FirebaseAuth.getInstance().signOut();

                        dbl.deleteUsers();
                        db.deleteStockWhole();
                        db.dbRefresh();

                        // Launching the login activity
                        Intent intent = new Intent(activity, MainActivity.class);
                        // To start a new task by clearing all activity stack and making login activity the root activity
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Log.e(TAG, "error_msg: " + errorMsg);
                        snackBarDisplay.parseErrorOccurred(TAG, mView, errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    snackBarDisplay.parseErrorOccurred(TAG, mView, e.getMessage());
                }

                hideDialog();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Course", "logout volley error");
                hideDialog();
                snackBarDisplay.volleyErrorOccurred(TAG, mView, error);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (this.activity.isDestroyed()) {
            return;
        }
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
