package info.ascetx.stockstalker.app;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.fragment.MainFragment;

import static info.ascetx.stockstalker.MainActivity.CURRENT_TAG;
import static info.ascetx.stockstalker.app.Config.URL_AR_NSQ_USER_STOCK;

public class AddRemoveUserStock {
    private static final String TAG = "AddRemoveUserStock";
    private MainActivity mainActivity;
    public AddRemoveUserStock(Context mListener) {
        this.mainActivity = (MainActivity) mListener;
    }

    public void arUserStock(String email, String stock, String name, String ar){
        // Tag used to cancel the request
        String tag_string_req = "string_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_AR_NSQ_USER_STOCK, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Sync user Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // Now store the user in SQLite
                        String msg = jObj.getString("error_msg");
                        Log.e(TAG, "msg: " + msg);

                        boolean ar = jObj.getBoolean("add");

                        FragmentManager fm = mainActivity.getSupportFragmentManager();
                        MainFragment fragment = (MainFragment)fm.findFragmentByTag(CURRENT_TAG);
                        assert fragment != null;

                        fragment.addRemoveStock(stock, name, ar);

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Log.e(TAG, "error_msg: " + errorMsg);
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError e) {
                Log.e(TAG, "Add Remove Error: " + e.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("stock_id", stock);
                params.put("stock_name", name);
                params.put("ar", ar);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}

