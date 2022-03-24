package info.ascetx.stockstalker.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.TaskStackBuilder;

import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.AppCompatPreferenceActivity;
import info.ascetx.stockstalker.app.SessionManager;

import static info.ascetx.stockstalker.MainActivity.CURRENT_TAG;
import static info.ascetx.stockstalker.MainActivity.TAG_MAIN_FRAME;
import static info.ascetx.stockstalker.app.Config.URL_PRIVACY_POLICY;
import static info.ascetx.stockstalker.app.Config.URL_TERMS_CONDITIONS;

/**
 *  For setting themes from Preference Android Studio
 * http://www.javarticles.com/2015/04/android-set-theme-dynamically.html
 *
 */

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = "SettingsActivity";
    public static final int RESULT_CODE_THEME_UPDATED = 1;
    private static SessionManager session;
    static Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        session = new SessionManager(getApplicationContext());
        if(session.getTheme() == 0){
            setTheme(R.style.AppThemeWithActionBar);
        } else if (session.getTheme() == 1){
            setTheme(R.style.LightAppThemeWithActionBar);
        }

        super.onCreate(savedInstanceState);

        act = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            // notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_notifications_new_message_ringtone)));

            // List preference change listener for selecting theme
            final ListPreference prefListThemes = (ListPreference) findPreference(getString(R.string.key_theme_change));
            prefListThemes.setSummary(prefListThemes.getEntries()[session.getTheme()]);

            prefListThemes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.e(TAG, "instanceof ListPreference");
                    String stringValue = newValue.toString();

                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the theme and summary based on the new value.
                    listPreference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                    session.setTheme(index);

                    CURRENT_TAG = TAG_MAIN_FRAME;
                    // Restart all tasks and corresponding Activities to apply Theme
                    TaskStackBuilder.create(getActivity())
                        .addNextIntent(new Intent(getActivity(), MainActivity.class))
                        .addNextIntent(getActivity().getIntent())
                        .startActivities();

                    return true;
                }
            });

            // Vibrate preference change listener
            findPreference(getString(R.string.key_vibrate))
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    boolean switched = ((SwitchPreference) preference)
                            .isChecked();
                    Log.e(TAG, "instanceof key_vibrate");
                    // https://stackoverflow.com/questions/24008764/disable-vibration-for-a-notificatio
                    // .setVibrate(null) works for me - and a better solution than creating a needless long[].

                    // Result: device doesn't vibrate and no grumbling in LogCat either. :)
//                    notificationBuilder.setDefaults(Notification.DEFAULT_LIGHT | Notification.DEFAULT_SOUND)
//                            .setVibrate(new long[]{0L}); // Passing null here silently fails

                    return true;
                }

            });

            // Notification preference change listener
            findPreference(getString(R.string.notifications_new_message))
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    boolean switched = ((SwitchPreference) preference)
                            .isChecked();
                    Log.e(TAG, "instanceof notifications_new_message");
                    // https://stackoverflow.com/questions/24008764/disable-vibration-for-a-notificatio
                    // .setVibrate(null) works for me - and a better solution than creating a needless long[].

                    // Result: device doesn't vibrate and no grumbling in LogCat either. :)
//                    notificationBuilder.setDefaults(Notification.DEFAULT_LIGHT | Notification.DEFAULT_SOUND)
//                            .setVibrate(new long[]{0L}); // Passing null here silently fails

                    return true;
                }

            });

            // feedback preference click listener
            Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Log.e(TAG, "instanceof key_send_feedback");
                    sendFeedback(getActivity());
                    return true;
                }
            });

            // Help preference click listener
            findPreference(getString(R.string.key_help))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Log.e(TAG, "instanceof key_help");
                    helpPopup(getActivity());
                    return true;
                }
            });

            // Disclaimer preference click listener
            findPreference(getString(R.string.key_disclaimer))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Log.e(TAG, "instanceof key_disclaimer");
                    disclaimerPopup(getActivity());
                    return true;
                }
            });

            // Privacy Policy preference click listener
            findPreference(getString(R.string.key_privacy_policy))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Log.e(TAG, "instanceof key_privacy_policy");
                    viewPPTC(getActivity(), preference, URL_PRIVACY_POLICY);
                    return true;
                }
            });

            // Terms & Conditions preference click listener
            findPreference(getString(R.string.key_terms_of_service))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Log.e(TAG, "instanceof key_terms_of_service");
                    viewPPTC(getActivity(), preference, URL_TERMS_CONDITIONS);
                    return true;
                }
            });

            // Version Name preference check and change the version name
            try {
                findPreference(getString(R.string.key_version_name)).setSummary(act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof RingtonePreference) {
                Log.e(TAG, "instanceof RingtonePreference");
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(R.string.summary_choose_ringtone);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof SwitchPreference){
                Log.e(TAG, "instanceof SwitchPreference");
                if (preference.getKey().equals("key_vibrate")) {
                    // https://stackoverflow.com/questions/24008764/disable-vibration-for-a-notificatio
                    // .setVibrate(null) works for me - and a better solution than creating a needless long[].

                    // Result: device doesn't vibrate and no grumbling in LogCat either. :)
//                    notificationBuilder.setDefaults(Notification.DEFAULT_LIGHT | Notification.DEFAULT_SOUND)
//                            .setVibrate(new long[]{0L}); // Passing null here silently fails
                }
            }
            return true;
        }
    };

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
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

    public static void helpPopup(Context context) {
        /* Alert Dialog Code Start*/
        LayoutInflater inflater = act.getLayoutInflater();
        View aboutLayout = inflater.inflate(R.layout.alert_help, null);
        TextView abbr = (TextView) aboutLayout.findViewById(R.id.abbr_tv);
//        TextView st = (TextView) aboutLayout.findViewById(R.id.st_tv);
        Button bt = (Button) aboutLayout.findViewById(R.id.help_bt);

        Resources res = act.getResources();
        String text = res.getString(R.string.alert_help_abbr);
        CharSequence styledText = Html.fromHtml(text);
        abbr.setText(styledText);

//        Log.e(TAG, "Screen width: "+Integer.toString(getScreenWidth()));
//        Log.e(TAG, "Screen height: "+Integer.toString(getScreenHeight()));

//        String text1 = res.getString(R.string.alert_help_st);
//        CharSequence styledText1 = Html.fromHtml(text1);
//        st.setText(styledText1);

        AlertDialog.Builder ad_about = new AlertDialog.Builder(context);
        ad_about.setView(aboutLayout);
//        ad_about.setCancelable(false);
        final AlertDialog alertDialog = ad_about.create();
//        alertDialog.getWindow().setContainer(ac);
        alertDialog.show();
//        alertDialog.getWindow().setLayout(MATCH_PARENT,WRAP_CONTENT);
//        if (getScreenHeight()>getScreenWidth())
//            alertDialog.getWindow().setLayout((int) (getScreenWidth()*0.95), (int) (getScreenHeight()*0.85));
//        else
//            alertDialog.getWindow().setLayout((int) (getScreenWidth()*0.85), (int) (getScreenHeight()*0.95));

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    public static void disclaimerPopup(Context context) {
        LayoutInflater inflater = act.getLayoutInflater();
        View aboutLayout = inflater.inflate(R.layout.alert_disclaimer, null);
        TextView about = (TextView) aboutLayout.findViewById(R.id.about_tv);
        Button bt_about = (Button) aboutLayout.findViewById(R.id.about_bt);
        about.setMovementMethod(new ScrollingMovementMethod());
        Resources res = act.getResources();
        String text = res.getString(R.string.alert_disclaimer);
//        CharSequence styledText = Html.fromHtml(text);
        about.setMovementMethod(LinkMovementMethod.getInstance());
        about.setText(R.string.alert_disclaimer);

        AlertDialog.Builder ad_about = new AlertDialog.Builder(context);
        ad_about.setView(aboutLayout);
//        ad_about.setCancelable(false);
        final AlertDialog alertDialog = ad_about.create();
//        alertDialog.getWindow().setContainer(ac);
        alertDialog.show();

        bt_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    public static void viewPPTC(Context context, Preference preference, String url){
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
//            alert.setTitle("Exit"); //Set Alert dialog title here
        String title = (String) preference.getTitle();
        alert.setMessage("View our " + title + " in web browser"); //Message here
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(browserIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No application can handle this request."
                            + " Please install a web browser",  Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
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
}