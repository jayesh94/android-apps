package info.ascetx.stockstalker.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Random;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;

/**
 * Created by JAYESH on 23-12-2018.
 */

public class AppRater {

    private final static int DAYS_UNTIL_PROMPT = 7;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 5;//Min number of launches

    public AppRater(MainActivity mainActivity) {
        WeakReference<MainActivity> mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        app_launched(mainActivityWeakReference);
    }

    private static void app_launched(WeakReference<MainActivity> mainActivity) {

        MainActivity activity = mainActivity.get();

        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

//        final int random = new Random().nextInt((max - min) + 1) + min;
        final int random = new Random().nextInt(5) + 10;

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity activity = mainActivity.get();
                        if(activity == null || activity.isFinishing())
                            return;

                        if (!activity.isFinishing()) {
                            showRateDialog(activity, editor);
                        }
                    }
                }, 1000 * random);
//                showDialog(mActivity, editor);
            }
        }

        editor.commit();
    }

    private static void showRateDialog(MainActivity mainActivity, final SharedPreferences.Editor editor){
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
//            alert.setTitle("Exit"); //Set Alert dialog title here
        alert.setMessage(mainActivity.getString(R.string.review_app)); //Message here
        alert.setPositiveButton("Rate StockStalker", new DialogInterface.OnClickListener() {
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
        alert.setNeutralButton("No, thanks", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.cancel();
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton

        alert.setNegativeButton("Remind later", new DialogInterface.OnClickListener() {
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
        }

        TextView textView = alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_milli));

        Button buttonPositvie = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button buttonNeutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
//        textView.setTextSize(context.getResources().getDimension(R.dimen.text_pretty_micro));
        buttonNeutral.setTextColor(ContextCompat.getColor(mainActivity, R.color.premium_alert_dialog_button_negative));
        buttonNeutral.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_micro));
        buttonNegative.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_micro));
        buttonPositvie.setTextSize(mainActivity.getResources().getDimension(R.dimen.text_pretty_micro));

       /* Alert Dialog Code End*/
    }
}
