package app.mugup.mugup.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import app.mugup.mugup.MainActivity;
import app.mugup.mugup.R;
import app.mugup.mugup.activity.FirstActivity;
import app.mugup.mugup.activity.PhoneActivity;
import app.mugup.mugup.adapter.NotificationRecyclerViewAdapter;
import app.mugup.mugup.helper.NotificationItem;

import static app.mugup.mugup.MainActivity.TAG_HOME_FRAME;
import static app.mugup.mugup.activity.FirstActivity.mGoogleSignInClient;
import static app.mugup.mugup.app.Config.URL_ADD_USER_DEVICE;
import static app.mugup.mugup.app.Config.URL_CHECK_USER_DEVICES;
import static app.mugup.mugup.app.Config.URL_GET_USER_NOTIFICATIONS;

public class CheckUserDevices {
    private Context context;
    private String email, deviceId, action, TAG;
    private ProgressDialog pDialog;

    public CheckUserDevices(Context context, String email, String deviceId, String action) {
        this.context = context;
        this.email = email;
        this.deviceId = deviceId;
        this.action = action;
        this.TAG = context.getClass().getSimpleName()+"."+CheckUserDevices.class.getSimpleName();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
    }

    public void verifyUserDeviceNos(){
        showpDialog();
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, URL_CHECK_USER_DEVICES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, response);
                hidepDialog();
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    Boolean error = jsonObject.getBoolean("error");
                    String error_msg = jsonObject.getString("error_msg");

                    if(error){
                        showSnackBar(error_msg);
                    } else {
                        if(!error_msg.equals("warning")){
                            if (action.equals("sign_in")){
                                Intent intent = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("fragment",TAG_HOME_FRAME);
                                context.startActivity(intent);
                                Activity activity = (Activity) context;
                                activity.finish();
                            } else if (action.equals("sign_up")){
                                context.startActivity(new Intent(context, PhoneActivity.class));
                            }
                        } else {
                            new AlertDialog.Builder(context)
                                    .setTitle("Warning!")
                                    .setMessage("One account can only be used in maximum two devices. Are you sure to use this device as your second device?")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addUserDevice();
                                        }
                                    })
                                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            signOut();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
                hidepDialog();
                showSnackBar(context.getResources().getString(R.string.volley_error));
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("device_id", deviceId);
                return params;
            }
        };
        queue.add(sr);
    }

    private void addUserDevice(){
        showpDialog();
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest sr = new StringRequest(Request.Method.POST, URL_ADD_USER_DEVICE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, response);
                hidepDialog();
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    Boolean error = jsonObject.getBoolean("error");
                    String error_msg = jsonObject.getString("error_msg");

                    if(error){
                        showSnackBar(error_msg);
                    } else {
                        Log.e(TAG,error_msg);
                        Toast.makeText(context,
                                "Device added successfully!",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("fragment",TAG_HOME_FRAME);
                        context.startActivity(intent);
                        Activity activity = (Activity) context;
                        activity.finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
                hidepDialog();
                showSnackBar(context.getResources().getString(R.string.volley_error));
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("device_id", CheckUserDevices.this.deviceId);
                return params;
            }
        };
        queue.add(sr);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    void showSnackBar(String msg){
        signOut();
        Activity activity = (Activity) context;
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.red));
        snackbar.show();

    }

    private void signOut(){
        Activity activity = (Activity) context;
        FirebaseAuth.getInstance().signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(activity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "G Sign Out");
                    }
                });
    }
}
