package info.ascetx.stockstalker.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;

/**
 * Created by JAYESH on 01-07-2018.
 */

public class VersionCheck {
    private static String TAG = "VersionCheck";
    private static WeakReference<MainActivity> mainActivityWeakReference;
    private static Bundle bundle;

    public VersionCheck(MainActivity activity, Bundle savedInstanceState) {
        mainActivityWeakReference = new WeakReference<MainActivity>((MainActivity) activity);
        bundle = savedInstanceState;
        getCurrentVersion(activity);
    }

    private static void getCurrentVersion(MainActivity mainActivity) {
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        String  tag_string_req = "string_req";
        String url = Config.URL_CURRENT_VERSION;

        PackageInfo pInfo = null;
        try {
            pInfo = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert pInfo != null;
        final String version = pInfo.versionName;

        Resources res = mainActivity.getResources();
        String text = res.getString(R.string.log_ver_name, version);
        CharSequence styledText = Html.fromHtml(text);
        Log.d(TAG, "In App Version: "+styledText);

//        Log.e(TAG, url);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "response.toString: "+ response);
                MainActivity activity = mainActivityWeakReference.get();
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                SessionManager session = new SessionManager(activity);
                String ver, title, msg, url;
                int api_ver;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    ver = jsonObject.getString("ver");
                    title = jsonObject.getString("title");
                    msg = jsonObject.getString("msg");
                    url = jsonObject.getString("url");
                    api_ver = jsonObject.getInt("api_ver");

                    if (api_ver == session.getApiVer()) {
                        if (!version.equals(ver))
                            updatePopUp(mainActivity, title, msg, url);
                    } else {
                        mainActivity.verifySourceGetResource(bundle, true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Volley Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private static void updatePopUp(Activity mainActivity, String title, String msg, String url) {

        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
        alert.setTitle(title);
        alert.setMessage(msg); //Message here

        alert.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Uri uri = Uri.parse("market://details?id=" + mainActivity.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET  |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    mainActivity.startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    mainActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url)));
                }
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton

        alert.setNegativeButton("LATER", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
       /* Alert Dialog Code End*/
    }
}
