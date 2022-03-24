package info.ascetx.stockstalker.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.android.material.snackbar.Snackbar;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import info.ascetx.stockstalker.R;

public class SnackBarDisplay {

    private static String TAG = "SnackBarDisplay";
    
    private Context context;
    private String res;

    public SnackBarDisplay(Context context) {
        this.context = context;
    }

    public void volleyErrorOccurred(String TAG, View mView, VolleyError volleyError) {
        if (volleyError == null || volleyError.networkResponse == null) {
            showSnackBar(mView, context.getResources().getString(R.string.msg_unknown_error));
            return;
        } else {
            showSnackBar(mView, logVolleyError(volleyError));
        }

        NetworkResponse response = volleyError.networkResponse;

        try {
            String statusCode = String.valueOf(volleyError.networkResponse.statusCode);
            String body = new String(volleyError.networkResponse.data,"UTF-8");
//            Log.e(TAG,"ErrorCode: "+statusCode);
//            Log.e(TAG,"body: "+body);
            res = TAG + ": " + statusCode + ": " + body;
            showSnackBarSF(mView, logVolleyError(volleyError));
        } catch (Exception e) {
            // exception
        }
        if (volleyError instanceof ServerError && response != null) {
            try {
                res = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                res = TAG + ": " + res;
                Log.e(TAG,"Volley Error: " + res);
                showSnackBarSF(mView, logVolleyError(volleyError));
            } catch (UnsupportedEncodingException e1) {
                // Couldn't properly decode data to string
                e1.printStackTrace();
            }
        }

        if(volleyError.getMessage() != null){
            Log.e(TAG,"Volley Error: " +volleyError.getMessage());
            showSnackBar(mView, logVolleyError(volleyError));
        }
    }

    private String logVolleyError(VolleyError error){
        String msg = "";
        if(error instanceof NoConnectionError){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }
            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()){
                msg = "Server is not connected to internet.";
            } else {
                msg = "Your device is not connected to internet.";
            }
        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException
                || (error.getCause().getMessage() != null
                && error.getCause().getMessage().contains("connection"))){
            msg = "Your device is not connected to internet.";
        } else if (error.getCause() instanceof MalformedURLException){
            msg = "Bad Request.";
        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                || error.getCause() instanceof JSONException
                || error.getCause() instanceof XmlPullParserException){
            msg = "Parse Error (because of invalid json or xml).";
        } else if (error.getCause() instanceof OutOfMemoryError){
            msg = "Out Of Memory Error.";
        }else if (error instanceof AuthFailureError){
            msg = "Server couldn't find the authenticated request.";
        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
            msg = "Server is not responding.";
        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                || error.getCause() instanceof ConnectTimeoutException
                || error.getCause() instanceof SocketException
                || (error.getCause().getMessage() != null
                && error.getCause().getMessage().contains("Connection timed out"))) {
            msg = "Connection timeout error";
        } else {
            msg = "An unknown error occurred.";
        }
        Log.e(TAG, msg);
        return msg;
    }

    public void parseErrorOccurred(String tag, View mView, String message) {
        res = tag + ": " + message;
        Log.e(tag,"Response Parse Error: " + res);
        showSnackBarSF(mView, context.getResources().getString(R.string.msg_unknown_error));
    }

    private void showSnackBar(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_err_color));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void showSnackBarSF(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, 8000).setAction("Send Feedback",new MyUndoListener());
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_err_color));
        snackbar.setActionTextColor(Color.YELLOW);
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public class MyUndoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            sendFeedback();
        }
    }

    private void sendFeedback() {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER +
                    "\n Error: " + ((res != null) ? res : "");
            body = body.replace("\n", "<br/>");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String subject = "Query from Stock Stalker";
        String to = context.getString(R.string.email_ascetx);

        StringBuilder builder = new StringBuilder("mailto:" + Uri.encode(to));
        char operator = '?';
        builder.append(operator).append("subject=").append(Uri.encode(subject));
        operator = '&';
        if (body != null) {
            builder.append(operator).append("body=").append(Uri.encode(body));
        }
        String uri = builder.toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }
}
