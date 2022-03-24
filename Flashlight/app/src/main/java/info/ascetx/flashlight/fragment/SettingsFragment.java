package info.ascetx.flashlight.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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

import info.ascetx.flashlight.BuildConfig;
import info.ascetx.flashlight.R;
import info.ascetx.flashlight.app.RatingDialog;

public class SettingsFragment extends PreferenceFragmentCompat {

    private GetPremiumInterface getPremiumInterfaceListener;
    private LogSettingsFragmentItemClick logSettingsFragmentItemClickListener;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference preferenceNotification = this.findPreference("notification");

        assert preferenceNotification != null;
        preferenceNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                logSettingsFragmentItemClickListener.logSettingsFragmentItemClick("toggle_notification");
                if ((Boolean) newValue) {
//                    Toast.makeText(getActivity(),"true", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getActivity(),"false", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        Preference preferenceFeedback = this.findPreference("feedback");

        assert preferenceFeedback != null;
        preferenceFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Toast.makeText(getActivity(),"Send Feedback", Toast.LENGTH_SHORT).show();
                if(getActivity() == null || getActivity().isFinishing())
                    return true;

                logSettingsFragmentItemClickListener.logSettingsFragmentItemClick("feedback");
                sendFeedback(getActivity());
                return true;
            }
        });

        Preference preferenceRate = this.findPreference("rate_us");

        assert preferenceRate != null;
        preferenceRate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Toast.makeText(getActivity(),"Send Feedback", Toast.LENGTH_SHORT).show();
                if(getActivity() == null || getActivity().isFinishing())
                    return true;

                logSettingsFragmentItemClickListener.logSettingsFragmentItemClick("rate_us");
                getReviewSettings(getActivity());
                return true;
            }
        });

        Preference preferenceShare = this.findPreference("share");

        assert preferenceShare != null;
        preferenceShare.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Toast.makeText(getActivity(),"Send Feedback", Toast.LENGTH_SHORT).show();
                if(getActivity() == null || getActivity().isFinishing())
                    return true;

                logSettingsFragmentItemClickListener.logSettingsFragmentItemClick("share");
                shareApp(getActivity());
                return true;
            }
        });

        Preference preferencePremium = this.findPreference("premium");

        assert preferencePremium != null;
        preferencePremium.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
//                Toast.makeText(getActivity(),"Send Feedback", Toast.LENGTH_SHORT).show();
                if(getActivity() == null || getActivity().isFinishing())
                    return true;

                logSettingsFragmentItemClickListener.logSettingsFragmentItemClick("premium");
                getPremiumInterfaceListener.buyNow();
                return true;
            }
        });

        // Version Name preference check and change the version name
        try {
            findPreference("version").setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            getPremiumInterfaceListener = (GetPremiumInterface) context;
            logSettingsFragmentItemClickListener = (LogSettingsFragmentItemClick) context;
            logError("listener onAttqch");
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
            logError("The activity does not implement the listener.");
        }
    }


    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nFrom feedback: \nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
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

        if( emailIntent.resolveActivity(context.getPackageManager()) != null )
            context.startActivity(emailIntent);
    }

    /**
     * launch App rating
     */
    private static void getReviewSettings(final Activity activity){
        logError( "getReviewSettings");
        if(activity == null || activity.isFinishing())
            return;

        final ProgressDialog dialog = new ProgressDialog(activity);;
        dialog.setMessage(activity.getResources().getString(R.string.progress_dialog_message));
        dialog.show();

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        String url = "https://ascetx.com/AndroidApps/FlashLight/rate.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response != null) {
                    logError( String.valueOf(response));
                    try {
                        if (response.getBoolean("use_in_app_rating_api")){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                launchInAppReviewAPI(activity);
                            } else {
                                if (response.getBoolean("show_new_rating_dialog")) {
                                    showNewRateDialog(activity);
                                } else {
                                    showOldRateDialog(activity);
                                }
                            }
                        } else {
                            if (response.getBoolean("show_new_rating_dialog")) {
                                showNewRateDialog(activity);
                            } else {
                                showOldRateDialog(activity);
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    Toast.makeText(activity, activity.getString(R.string.error), Toast.LENGTH_LONG).show();
                }
                logError( error.toString());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private static void showNewRateDialog(final Activity mainActivity) {
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
                            body = feedback + "\n\n-----------------------------\nFrom settings rater: \nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
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
    }

    private static void showOldRateDialog(final Activity mainActivity){
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
                dialog.cancel();
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNeutralButton(mainActivity.getString(R.string.review_app_btn_neutral), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
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

    private static void launchInAppReviewAPI(final Activity activity){
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

    /**
     * Share App
     *
     * @param activity*/
    private void shareApp(FragmentActivity activity){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_app_message_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_app_message_body));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public interface GetPremiumInterface {
        void buyNow();
    }

    public interface LogSettingsFragmentItemClick {
        void logSettingsFragmentItemClick(String button_id);
    }

    private static void logError(String msg){
        if (BuildConfig.BUILD_TYPE.contentEquals("debug"))
            Log.e("SettingsFragment", msg);
    }
}