package app.mugup.mugup;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.ReadPosition;
import com.folioreader.util.AppUtil;
import com.google.android.gms.common.util.Strings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import app.mugup.mugup.activity.FirstActivity;
import app.mugup.mugup.database.DatabaseHelper;
import app.mugup.mugup.fragment.HomeFragment;
import app.mugup.mugup.fragment.LibraryFragment;
import app.mugup.mugup.fragment.NotificationFragment;
import app.mugup.mugup.fragment.OrderConfirmationFragment;
import app.mugup.mugup.fragment.ProfileFragment;
import app.mugup.mugup.fragment.SubjectDetailsFragment;
import app.mugup.mugup.fragment.SubjectSelectionFragment;
import app.mugup.mugup.fragment.profile.AboutContentsFragment;
import app.mugup.mugup.fragment.profile.AboutFragment;
import app.mugup.mugup.fragment.profile.AccountFragment;
import app.mugup.mugup.fragment.profile.OrderFragment;
import app.mugup.mugup.fragment.profile.ReferFragment;
import app.mugup.mugup.helper.NotificationItem;

import static app.mugup.mugup.activity.FirstActivity.googleSignInAccount;
import static app.mugup.mugup.app.Config.URL_UPDATE_USER_REG_ID;
import static app.mugup.mugup.fragment.profile.AboutFragment.ABOUT_CONTENT;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener, SubjectDetailsFragment.OnSubjectDetailsFragmentInteractionListener, LibraryFragment.OnLibraryListFragmentInteractionListener,
        SubjectSelectionFragment.OnFragmentInteractionListener ,NotificationFragment.OnListFragmentInteractionListener, OrderConfirmationFragment.OnFragmentInteractionListener, AccountFragment.OnFragmentInteractionListener,
        AboutFragment.OnAboutFragmentInteractionListener, ReferFragment.OnFragmentInteractionListener, OrderFragment.OnListFragmentInteractionListener {

    private static String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FolioReader folioReader;
    private ImageLoader mImageLoader;

    private DatabaseHelper db;

    private ReadPosition readPosition;

    // Progress Dialog
    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    private MainActivity mInstance;
    private BottomNavigationView navigation;

    // tags used to attach the fragments
    public static final String TAG_HOME_FRAME = "home_frame";
    public static final String TAG_PROFILE_FRAME = "profile_frame";
    public static final String TAG_NOTIFICATION_FRAME = "notification_frame";
    public static final String TAG_LIBRARY_FRAME = "library_frame";
    public static final String TAG_SUBJECT_DETAILS_FRAME = "subject_details_frame";
    public static final String TAG_SUBJECT_SELECTION_FRAME = "subject_selection_frame";
    public static final String TAG_ORDER_CONFIRMATION_FRAME = "order_confirmation_frame";

    public static final String TAG_ACCOUNT_FRAME = "account_frame";
    public static final String TAG_ABOUT_FRAME = "about_frame";
    public static final String TAG_REFER_FRAME = "refer_frame";
    public static final String TAG_ORDER_HISTORY_FRAME = "order_history_frame";
    public static final String TAG_ABOUT_CONTENT_FRAME = "privacy_policy_frame";

    public static String CURRENT_TAG = TAG_HOME_FRAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String[] token = new String[1];

        Config config = new Config()
                .setThemeColorRes(R.color.primary_light);
        folioReader = FolioReader.get().setConfig(config,true);

        // Disable screen shots of app
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        db = new DatabaseHelper(this);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mHandler = new Handler();
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // set toolbar title
        setToolbarTitle();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (getIntent().getStringExtra("fragment") != null) {
            CURRENT_TAG = getIntent().getStringExtra("fragment");
        } else {
            Log.e(TAG, "getIntent()getStringExtra(\"fragment\") is NULL");
        }
// ******************** [START Firebase Notification] ********************

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.e(TAG, "Key: " + key + " Value: " + value);
            }
        } else {
            Log.e(TAG, "getIntent().getExtras() is NULL");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            setNotificationChannel(getString(R.string.news_notification_channel_id),getString(R.string.news_notification_channel_name));
            setNotificationChannel(getString(R.string.exams_notification_channel_id),getString(R.string.exams_notification_channel_name));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]

        Log.d(TAG, "Subscribing to Exams topic");
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic("Exams")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
//                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("News")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
//                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        // [END subscribe_topics]

        // Get token
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        token[0] = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token[0]);
                        Log.d(TAG, msg);
//                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        // [END retrieve_current_token]

// ******************** [END Firebase Notification] ********************

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.e(TAG, String.valueOf(googleSignInAccount)+ " " + String.valueOf(user));
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, FirstActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();

                } else {
                    try {
                        updateUserRegId(user, token[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (!Strings.isEmptyOrWhitespace(getIntent().getStringExtra("notification_fragment"))) {
            CURRENT_TAG = getIntent().getStringExtra("notification_fragment");
        }
        loadFragment();
    }

    private void updateUserRegId(final FirebaseUser user, final String reg_id){
        Log.e(TAG, user.getUid());
        Log.e(TAG, reg_id);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST, URL_UPDATE_USER_REG_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uid",user.getUid());
                params.put("reg_id",reg_id);

                return params;
            }
        };
        queue.add(sr);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNotificationChannel(String channelId, String channelName){
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    null;
            notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel mChannel = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setSound(defaultSoundUri, attributes);

            if (notificationManager != null)
                notificationManager.createNotificationChannel(mChannel);
        }
    }

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @Override
    public void onBackPressed() {
        switch(CURRENT_TAG){
            case TAG_HOME_FRAME:
                exitAppAlert();
                break;
            case TAG_PROFILE_FRAME:
                CURRENT_TAG = TAG_HOME_FRAME;
                loadFragment();
                break;
            case TAG_LIBRARY_FRAME:
                CURRENT_TAG = TAG_HOME_FRAME;
                loadFragment();
                break;
            case TAG_NOTIFICATION_FRAME:
                CURRENT_TAG = TAG_HOME_FRAME;
                loadFragment();
                break;
            case TAG_ACCOUNT_FRAME:
                CURRENT_TAG = TAG_PROFILE_FRAME;
                loadFragment();
                break;
            case TAG_ABOUT_FRAME:
                CURRENT_TAG = TAG_PROFILE_FRAME;
                loadFragment();
                break;
            case TAG_REFER_FRAME:
                CURRENT_TAG = TAG_PROFILE_FRAME;
                loadFragment();
                break;
            case TAG_ORDER_HISTORY_FRAME:
                CURRENT_TAG = TAG_PROFILE_FRAME;
                loadFragment();
                break;
            case TAG_SUBJECT_DETAILS_FRAME:
                CURRENT_TAG = TAG_HOME_FRAME;
                loadFragment();
                break;
            case TAG_SUBJECT_SELECTION_FRAME:
                CURRENT_TAG = TAG_SUBJECT_DETAILS_FRAME;
                loadFragment();
                break;
            case TAG_ORDER_CONFIRMATION_FRAME:
                CURRENT_TAG = TAG_LIBRARY_FRAME;
                loadFragment();
                break;
            case TAG_ABOUT_CONTENT_FRAME:
                CURRENT_TAG = TAG_ABOUT_FRAME;
                loadFragment();
        }
    }

    private void exitAppAlert() {
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//            alert.setTitle("Exit"); //Set Alert dialog title here
        alert.setMessage("Do you want to exit?"); //Message here
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        /* Alert Dialog Code End*/
    }

    private void setToolbarTitle() {
        switch (CURRENT_TAG){
            case TAG_PROFILE_FRAME:
                getSupportActionBar().setTitle("Profile");
                break;
            case TAG_LIBRARY_FRAME:
                getSupportActionBar().setTitle("Library");
                break;
            case TAG_HOME_FRAME:
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                break;
            case TAG_NOTIFICATION_FRAME:
                getSupportActionBar().setTitle("Notifications");
                break;
            case TAG_ACCOUNT_FRAME:
                getSupportActionBar().setTitle("Account");
                break;
            case TAG_ABOUT_FRAME:
                getSupportActionBar().setTitle("About");
                break;
            case TAG_REFER_FRAME:
                getSupportActionBar().setTitle(getResources().getString(R.string.refer_earn));
                break;
            case TAG_ORDER_HISTORY_FRAME:
                getSupportActionBar().setTitle("Order History");
                break;
            case TAG_ABOUT_CONTENT_FRAME:
                getSupportActionBar().setTitle(ABOUT_CONTENT);
                break;
            default:
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    CURRENT_TAG = TAG_HOME_FRAME;
                    loadFragment();
                    return true;
                case R.id.navigation_library:
                    CURRENT_TAG = TAG_LIBRARY_FRAME;
                    loadFragment();
                    return true;
                case R.id.navigation_notifications:
                    CURRENT_TAG = TAG_NOTIFICATION_FRAME;
                    loadFragment();
                    //folioReader.openBook(R.raw.childrens_literature);
                    //new DownloadFileFromURL().execute(file_url);
                    return true;
                case R.id.navigation_profile:
                    CURRENT_TAG = TAG_PROFILE_FRAME;
                    loadFragment();
                    return true;
                default:
                    return false;
            }
        }
    };


    @Override
    public void onProfileFragmentInteraction(String tagAccountFrame) {
        CURRENT_TAG = tagAccountFrame;
        loadFragment();
    }

    @Override
    public void onLibraryListFragmentInteraction(String file_url) {

        Config config = AppUtil.getSavedConfig(getApplicationContext());
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);
        new DownloadFileFromURL().execute(file_url);
    }

    @Override
    public void onListFragmentInteraction(NotificationItem item) {
        if(!item.getUrl().equals("")) {
            String url = item.getUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onSubjectDetailsFragmentInteractionListener(String bookSampleUrl)
    {
        Config config = AppUtil.getSavedConfig(getApplicationContext());
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);
        new DownloadFileFromURL().execute(bookSampleUrl);
    }

    @Override
    public void onAboutFragmentInteraction(String tagAboutFrame) {
        CURRENT_TAG = tagAboutFrame;
        loadFragment();
    }

    /**
     * Background Async Task to download file
     * */
    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {

                URL url = new URL(f_url[0]);
//*********************** SSL Check workaround: START *****************************************
//
//              URLConnection connection = url.openConnection();

//                SSLContext ctx = SSLContext.getInstance("TLS");
//                ctx.init(null, new TrustManager[] {
//                        new X509TrustManager() {
//                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
//                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
//                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
//                        }
//                }, null);
//                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
//
//                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                    public boolean verify(String hostname, SSLSession session) {
//                        return true;
//                    }
//                });
//*********************** SSL Check workaround: END *****************************************

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                Log.e(TAG,"Storage Path: "+Environment.getExternalStorageDirectory().toString());
                Log.e(TAG,"Storage Path: "+MainActivity.this.getFilesDir());
                Log.e(TAG,"Storage Path: "+MainActivity.this.getCacheDir());

                // Output stream
                OutputStream output = new FileOutputStream(MainActivity.this.getCacheDir()+"/test.epub");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        private File getTempFile(Context context, String url) {
            File file = null;
            try {
                String fileName = Uri.parse(url).getLastPathSegment();
                file = File.createTempFile(fileName, null, context.getCacheDir());
            } catch (IOException e) {
                // Error while creating file
            }
            return file;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

            // Displaying downloaded image into image view
            // Reading image path from sdcard
            String filePath = MainActivity.this.getCacheDir() + "/test.epub";
            Config config = new Config()
                    .setThemeColorRes(R.color.primary_light);

            folioReader.setReadPosition(readPosition).openBook(filePath);
        }
    }

    private void loadFragment()
    {
        setToolbarTitle();

        if (CURRENT_TAG.equals(TAG_HOME_FRAME))
            navigation.getMenu().getItem(0).setChecked(true);

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commitAllowingStateLoss();
//                    fragmentTransaction.commit();
//                    commitAllowingStateLoss is use because of IllegalStateException: Can not perform this action after onSaveInstanceState
//                    Such an exception will occur if you try to perform a fragment transition after your fragment activity's onSaveInstanceState() gets called.
            }
        };

        // If mHandler is not null, then add to the message queue
        if (mHandler != null) {
            mHandler.post(mPendingRunnable);
        }
    }

    private Fragment getFragment() {
        switch (CURRENT_TAG) {
            case TAG_HOME_FRAME:
                return new HomeFragment();
            case TAG_PROFILE_FRAME:
                return new ProfileFragment();
            case TAG_LIBRARY_FRAME:
                return new LibraryFragment();
            case TAG_NOTIFICATION_FRAME:
                return new NotificationFragment();
            case TAG_ACCOUNT_FRAME:
                return new AccountFragment();
            case TAG_ABOUT_FRAME:
                return new AboutFragment();
            case TAG_REFER_FRAME:
                return new ReferFragment();
            case TAG_ORDER_HISTORY_FRAME:
                return new OrderFragment();
            case TAG_ABOUT_CONTENT_FRAME:
                return new AboutContentsFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        FolioReader.clear();
    }

    @Override
    public void onFragmentInteraction() {

    }

    @Override
    public void onListFragmentInteraction() {

    }

    public synchronized MainActivity getInstance() {
        return mInstance;
    }
}