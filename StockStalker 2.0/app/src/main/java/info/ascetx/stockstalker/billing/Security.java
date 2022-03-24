package info.ascetx.stockstalker.billing;

/**
 * Created by JAYESH on 14-08-2018.
 * Whole purchase sign verification is done on server. In app verification code removed.
 */
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.billingclient.util.BillingHelper;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import info.ascetx.stockstalker.BuildConfig;
import info.ascetx.stockstalker.app.AppController;

import static info.ascetx.stockstalker.app.Config.URL_CURRENT_VERIFY_SIGNATURE;

/**
 * Security-related methods. For a secure implementation, all of this code should be implemented on
 * a server that communicates with the application on the device.
 */
public class Security {
    private static final String TAG = "IABUtil/Security";

    /**
     * Verifies that the data was signed with the given signature, and returns the verified
     * purchase.
//     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     * @throws IOException if encoding algorithm is not supported or key specification
     * is invalid
     */
    static void verifyPurchase(final VolleyCallback callback, String signedData,
                                         String signature) throws IOException {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(signature)) {
            BillingHelper.logWarn(TAG, "Purchase verification failed: missing data.");

            // Signature data will be missing if using Test SKUs, thus replace the return false; with the below code.
            if (BuildConfig.DEBUG) {
                callback.onSuccess(true);
            }
            callback.onSuccess(false);
        }

        String urlStr;

        urlStr = URL_CURRENT_VERIFY_SIGNATURE + URLEncoder.encode(signedData, "UTF-8") + "&signature=" + URLEncoder.encode(signature, "UTF-8");
        Log.d(TAG, "urlStr: "+urlStr);

        String  tag_string_req = "string_req";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                urlStr, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "response.toString: "+response);
                Log.e(TAG,"signedData: "+signedData);
                Log.e(TAG,"signature: "+signature);
                Log.e(TAG,"verifyValidSignature: "+response);

                if(response.equals("good")) {
                    try {
                        JSONObject jsonObject = new JSONObject(signedData);
                        jsonObject.getBoolean("autoRenewing");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                callback.onSuccess(response.equals("good"));
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String err = (error.getMessage()==null)?"Error in file code: ":error.getMessage();
                VolleyLog.d(TAG, "Volley Error: " + err);
                Log.e(TAG, err);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
