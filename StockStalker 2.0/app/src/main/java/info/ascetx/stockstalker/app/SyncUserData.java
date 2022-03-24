package info.ascetx.stockstalker.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.dbhandler.LoginHandler;
import info.ascetx.stockstalker.helper.StockName;

import static info.ascetx.stockstalker.app.Config.URL_SYNC_NSQ_USER_DETAILS;

public class SyncUserData {

    private static final String TAG = "SyncUserData";
    private OnUserDataSyncedListener mListener;
    private Bundle savedInstanceState;
    private ProgressDialog pDialog;
    private DatabaseHandler db;
    private SnackBarDisplay snackBarDisplay;
    private MainActivity mainActivity;

    public SyncUserData(MainActivity mainActivity, Bundle savedInstanceState, UserInfo user, LoginHandler dbl, DatabaseHandler db) {
        this.db = db;
        this.mListener = (OnUserDataSyncedListener) mainActivity;
        this.savedInstanceState = savedInstanceState;
        this.mainActivity = mainActivity;

        pDialog = new ProgressDialog(mainActivity);
        pDialog.setMessage(((Context) mainActivity).getResources().getString(R.string.please_wait));
        pDialog.setCancelable(false);

        snackBarDisplay = new SnackBarDisplay(mainActivity);

        Map<String, String> map;
        ArrayList list = new ArrayList();
        for (StockName stockName : db.getAllStockNames(mainActivity)){
            map = new HashMap<>();
            map.put("stock",stockName.getStock());
            map.put("name",stockName.getName());
            list.add(map);
        }

        Log.e(TAG, listmap_to_json_string(list));

        Log.e(TAG, user.getProviderId());
        Log.e(TAG, user.getEmail());
        String userName;

        if (user.getDisplayName() != null){
            if (!user.getDisplayName().isEmpty()){
                Log.e(TAG, user.getDisplayName());
                userName = user.getDisplayName();
                Log.e(TAG, "userName getDisplayName: "+userName);
            } else {
                userName = ((Context) mainActivity).getResources().getString(R.string.app_name);
                Log.e(TAG, "userName: "+userName);
            }
        } else {
            userName = ((Context) mainActivity).getResources().getString(R.string.app_name);
            Log.e(TAG, "userName: "+userName);
        }

        dbl.deleteUsers();
        dbl.addUser(userName, user.getEmail());

        SharedPreferences pref = ((Context) mainActivity).getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        Log.e(TAG, "Reg ID " + regId);

        syncUserDetails(user.getDisplayName(), user.getEmail(), regId, listmap_to_json_string(list));

    }

    private String listmap_to_json_string(List<Map<String, Object>> list)
    {
        JSONArray json_arr=new JSONArray();
        for (Map<String, Object> map : list) {
            JSONObject json_obj=new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                try {
                    json_obj.put(key,value);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            json_arr.put(json_obj);
        }
        return json_arr.toString();
    }

    /**
     * function to sync user details in mysql db
     * */
    private void syncUserDetails(final String name, final String email, final String regId, String jsonString) {
        // Tag used to cancel the request
        String tag_string_req = "string_req";

        pDialog.setMessage("Syncing ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_SYNC_NSQ_USER_DETAILS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Sync user Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        boolean msg = jObj.getBoolean("msg");

                        if(msg){
                            Log.d(TAG, "msg: " + jObj.getString("response_msg"));
                        } else {
                            JSONArray stocks = jObj.getJSONArray("stocks");
                            // looping through json and adding to stock list

                            db.deleteStockWhole();

                            for (int i = 0; i < stocks.length(); i++) {
                                try {
                                    JSONObject stockObj = stocks.getJSONObject(i);

                                    String stock_id = stockObj.getString("stock_id");
                                    String stock_name = stockObj.getString("stock_name");

                                    db.addStockName(stock_id, stock_name);
                                    Log.e(TAG, stock_id);

                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Object Parsing error: " + e.getMessage());
                                }
                            }
                        }
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Log.e(TAG, "error_msg: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    snackBarDisplay.parseErrorOccurred(TAG, mainActivity.getWindow().getDecorView().findViewById(android.R.id.content), e.getMessage());
                }
                mListener.onUserDataSynced(savedInstanceState);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e(TAG, "sync Error: " + e.getMessage());
                hideDialog();
                mListener.onUserDataSynced(savedInstanceState);
                snackBarDisplay.volleyErrorOccurred(TAG, mainActivity.getWindow().getDecorView().findViewById(android.R.id.content), e);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("name", name);
                params.put("reg_id", regId);
                params.put("json_stocks", jsonString);

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
        if (mainActivity.isDestroyed()) {
            return;
        }
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public interface OnUserDataSyncedListener {
        // TODO: Update argument type and name
        void onUserDataSynced(Bundle savedInstanceState);
    }
}
