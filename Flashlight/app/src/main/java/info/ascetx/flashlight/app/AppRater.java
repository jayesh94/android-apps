package info.ascetx.flashlight.app;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Random;

import info.ascetx.flashlight.BuildConfig;
import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.R;

/**
 * Created by JAYESH on 23-12-2018.
 */

public class AppRater {

    private final static int DAYS_UNTIL_PROMPT = 5;//Min 5 number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 4;//Min 4 number of launches
    private final static int DAYS_UNTIL_NEXT_PROMPT = 3;//Min 3 number of days after review ask
    private final static String TAG = "AppRater";
    public final static String APP_RATER_SHARED_PREF = "apprater";

    public AppRater(MainActivity mainActivity) {
        WeakReference<MainActivity> mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        app_launched(mainActivityWeakReference);
    }

    private static void app_launched(final WeakReference<MainActivity> mainActivity) {

        MainActivity activity = mainActivity.get();
        if(activity == null || activity.isFinishing())
            return;

        final SharedPreferences prefs = activity.getSharedPreferences(APP_RATER_SHARED_PREF, 0);
        if (prefs.getBoolean("dontshowagain", false)) { return; }

        final SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);
        logError( "launch_count: " + prefs.getLong("launch_count", 0));

        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
            logError( String.valueOf("date_firstlaunch: " + date_firstLaunch));
        }

        // Get launch of app review request dialog.
        long date_last_review_request_launch = prefs.getLong("date_last_review_request_launch", 0);
        logError( "date_last_review_request_launch: " + date_last_review_request_launch);

//        final int random = new Random().nextInt((max - min) + 1) + min;
        final int random = new Random().nextInt(5) + 5; // Showing the rating dialog after few seconds delay

        logError( "random: " + random);
        logError( "launch_count: " + launch_count);
        logError( "LAUNCHES_UNTIL_PROMPT: " + LAUNCHES_UNTIL_PROMPT);
        logError( "date_firstLaunch: " + date_firstLaunch);
        logError( "currentTimeMillis: " + System.currentTimeMillis());
        logError( "date_last_review_request_launch: " + date_last_review_request_launch);

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                if (date_last_review_request_launch == 0 || System.currentTimeMillis() >= date_last_review_request_launch +
                        (DAYS_UNTIL_NEXT_PROMPT * 24 * 60 * 60 * 1000) ){
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity activity = mainActivity.get();
                            if(activity == null || activity.isFinishing())
                                return;

                            if (!activity.isFinishing()) {
                                getReviewSettings(activity, editor);
                            }
                        }
                    }, 1000 * random);
//                showDialog(mainActivity, editor);
                }
            }
        }

        editor.apply();
    }

    private static void getReviewSettings(final MainActivity activity, final SharedPreferences.Editor editor){
        logError( "getReviewSettings");
        if(activity == null || activity.isFinishing())
            return;

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        String url = "https://ascetx.com/AndroidApps/FlashLight/rate.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    logError( String.valueOf(response));
                    try {
                        if (response.getBoolean("use_in_app_rating_api")){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                launchInAppReviewAPI(activity, editor, response.getBoolean("dontshowagain"));
                            } else {
                                if (response.getBoolean("show_new_rating_dialog")) {
                                    showNewRateDialog(activity, editor);
                                } else {
                                    showOldRateDialog(activity, editor);
                                }
                            }
                        } else {
                            if (response.getBoolean("show_new_rating_dialog")) {
                                showNewRateDialog(activity, editor);
                            } else {
                                showOldRateDialog(activity, editor);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logError( error.toString());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private static void showNewRateDialog(final MainActivity mainActivity, SharedPreferences.Editor editor) {
        logError( "In showNewRateDialog");
        if(mainActivity == null || mainActivity.isFinishing())
            return;
        logError( "In showNewRateDialog 2");
        final RatingDialog ratingDialog = new RatingDialog.Builder(mainActivity)
                .threshold(5)
                .ratingBarColor(R.color.rate_star)
                .playstoreUrl("https://play.google.com/store/apps/details?id=info.ascetx.flashlight")
                .onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
                    @Override
                    public void onRatingSelected(float rating, boolean thresholdCleared) {

                    }
                })
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        String body = null;
                        try {
                            body = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0).versionName;
                            body = feedback + "\n\n-----------------------------\nFrom auto rater: \nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
                        } catch (PackageManager.NameNotFoundException e) {
                        }

                        Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
                        emailSelectorIntent.setData(Uri.parse("mailto:"));

                        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"flashlight@ascetx.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Flashlight");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        emailIntent.setSelector( emailSelectorIntent );

                        if( emailIntent.resolveActivity(mainActivity.getPackageManager()) != null )
                            mainActivity.startActivity(emailIntent);
                    }
                }).build();

        ratingDialog.show();
        recordLastLaunchDate(mainActivity);
    }

    private static void showOldRateDialog(final MainActivity mainActivity, final SharedPreferences.Editor editor){
        if(mainActivity == null || mainActivity.isFinishing())
            return;

        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
        alert.setTitle(mainActivity.getString(R.string.review_app_title)); //Set Alert dialog title here
        alert.setMessage(mainActivity.getString(R.string.review_app_msg)); //Message here
        alert.setPositiveButton(mainActivity.getString(R.string.review_app_btn_positive), new DialogInterface.OnClickListener() {
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
                            Uri.parse("http://play.google.com/store/apps/details?id=" + mainActivity.getPackageName())));
                }
                editor.putBoolean("dontshowagain", true);
                dialog.cancel();
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNeutralButton(mainActivity.getString(R.string.review_app_btn_neutral), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.cancel();
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton

        alert.setNegativeButton(mainActivity.getString(R.string.review_app_btn_negative), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        //      To prevent error: android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@1c12b18 is not valid; is your activity running?
        if(!mainActivity.isFinishing())
        {
            //show dialog
            alertDialog.show();
            recordLastLaunchDate(mainActivity);
        }

        TextView textView = alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_milli));

        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button buttonNeutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
//        textView.setTextSize(context.getResources().getDimension(R.dimen.text_pretty_micro));
        buttonNeutral.setTextColor(ContextCompat.getColor(mainActivity, R.color.premium_alert_dialog_button_negative));
        buttonNeutral.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_micro));
        buttonNegative.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_micro));
        buttonPositive.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_micro));

       /* Alert Dialog Code End*/
    }

    private static void launchInAppReviewAPI(final MainActivity activity, final SharedPreferences.Editor editor, final boolean dontshowagain){
        if(activity == null || activity.isFinishing())
            return;

        final ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    // We can get the ReviewInfo object
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                    flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.
                            if (dontshowagain){
                                if (editor != null) {
                                    editor.putBoolean("dontshowagain", true);
                                    editor.commit();
                                }
                            }
                            recordLastLaunchDate(activity);
                            logError("launchInAppReviewAPI Successful!");
                        }
                    });
                } else {
                    // There was some problem, continue regardless of the result.
                    logError( "launchInAppReviewAPI ERROR!");
                }
            }
        });
    }

    private static void recordLastLaunchDate(MainActivity activity){
        final SharedPreferences prefs = activity.getSharedPreferences(APP_RATER_SHARED_PREF, 0);
        final SharedPreferences.Editor editor = prefs.edit();
        // Get launch of app review request dialog.
        long date_last_review_request_launch = prefs.getLong("date_last_review_request_launch", 0);
        // Record the launch of app review request dialog.
        date_last_review_request_launch = System.currentTimeMillis();
        editor.putLong("date_last_review_request_launch", date_last_review_request_launch);
        prefs.getLong("date_last_review_request_launch", 0);
        logError("date_last_review_request_launch: "+date_last_review_request_launch);

        // reset Launch Count
        editor.putLong("launch_count", 0);
        editor.apply();
    }
    
    private static void logError(String msg){
        if (BuildConfig.BUILD_TYPE.contentEquals("debug"))
            Log.e(TAG, msg);
    }

}
