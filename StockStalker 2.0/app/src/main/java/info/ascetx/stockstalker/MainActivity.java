package info.ascetx.stockstalker;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.billingclient.api.BillingClient;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.messaging.FirebaseMessaging;

import com.facebook.appevents.AppEventsLogger;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.NoSuchPaddingException;

import info.ascetx.stockstalker.activity.SettingsActivity;
import info.ascetx.stockstalker.adapter.BookAutoCompleteAdapter;
import info.ascetx.stockstalker.app.AcquireDialog;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.AppRater;
import info.ascetx.stockstalker.app.Config;
import info.ascetx.stockstalker.app.DelayAutoCompleteTextView;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.KSEncryptDecrypt;
import info.ascetx.stockstalker.app.LogOut;
import info.ascetx.stockstalker.app.MainViewController;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SignUp;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.app.SyncUserData;
import info.ascetx.stockstalker.app.VersionCheck;
import info.ascetx.stockstalker.billing.BillingManager;
import info.ascetx.stockstalker.billing.BillingProvider;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.dbhandler.LoginHandler;
import info.ascetx.stockstalker.fragment.GainFragment;
import info.ascetx.stockstalker.fragment.GraphFragment;
import info.ascetx.stockstalker.fragment.HistoricalFragment;
import info.ascetx.stockstalker.fragment.IntradayFragment;
import info.ascetx.stockstalker.fragment.LossFragment;
import info.ascetx.stockstalker.fragment.MainFragment;
import info.ascetx.stockstalker.fragment.MostActiveFragment;
import info.ascetx.stockstalker.fragment.NewsFragment;
import info.ascetx.stockstalker.fragment.StockFragment;
import info.ascetx.stockstalker.fragment.StockInfoFragment;
import info.ascetx.stockstalker.fragment.StockNewsFragment;
import info.ascetx.stockstalker.fragment.StockProfileFragment;
import info.ascetx.stockstalker.fragment.TopGLMFragment;
import info.ascetx.stockstalker.fragment.UpgradeFragment;
import info.ascetx.stockstalker.helper.Book;
import info.ascetx.stockstalker.helper.StockName;
import info.ascetx.stockstalker.util.NotificationUtils;

import static info.ascetx.stockstalker.app.Config.STOCK_FRAGMENT_INTER_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_NSQ_USER_SUBSCRIBED;
import static info.ascetx.stockstalker.app.Config.URL_VERIFY_SOURCE_GET_RESOURCE;
import static info.ascetx.stockstalker.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
, StockFragment.OnStockFragmentInteractionListener, MainFragment.OnFragmentInteractionListener, GraphFragment.OnFragmentInteractionListener,
        IntradayFragment.OnListFragmentInteractionListener, HistoricalFragment.OnListFragmentInteractionListener
,TopGLMFragment.OnFragmentInteractionListener, GainFragment.OnListFragmentInteractionListener, LossFragment.OnListFragmentInteractionListener,
        MostActiveFragment.OnListFragmentInteractionListener, NewsFragment.OnFragmentInteractionListener, SyncUserData.OnUserDataSyncedListener,
        StockInfoFragment.OnFragmentInteractionListener, UpgradeFragment.OnUpgradePremiumFragmentInteractionListener, StockNewsFragment.OnStockNewsFragmentInteractionListener,
        StockProfileFragment.OnStockProfileFragmentInteractionListener, BillingProvider{

    private static final int STOCK_STALKER_LIMIT = 100;
    private static final int TRAIL_USER_STOCK_STALKER_LIMIT = 10;
    private static String TAG = "MainActivity";
    private static final String URL_UPDATE_REG_ID = Config.URL_UPDATE_REG_ID;
    public static String BASE_URL = null;
    private final static int SECONDS_UNTIL_INTERSTITIAL_PROMPT = 90;//Seconds after which int Ad should be shown after int ad close or app launch

    private DatabaseHandler db;
    private ActionBarDrawerToggle toggle;
    private static LoginHandler dbl;
    private SessionManager session;
    private DelayAutoCompleteTextView bookTitle;
    private TextView isin, stock;
    private View autoComplete;
    private ProgressBar progressBar;
    private static BroadcastReceiver mRegistrationBroadcastReceiver;
    public StockName m;
    private static String snackBarMsg;
    private static int notify;
    private TextView nav_name, nav_email;
    private Button refreshBtn;
    private Handler mHandler;
    public static AnimationDrawable animationDrawable;
    private ImageView imageView;
    private Animation animation;
    private InterstitialAd mInterstitialAd;
    private FbLogAdEvents fbLogAdEvents;

    private MainViewController mViewController;
    private BillingManager mBillingManager;
    private AcquireDialog mAcquireDialog;
    private KSEncryptDecrypt ksEncryptDecrypt;
    private SnackBarDisplay snackBarDisplay;
    private FirebaseAuth auth;
    private AppEventsLogger logger;
    private ProgressDialog pDialog;

    // tags used to attach the fragments
    public static final String TAG_MAIN_FRAME = "main_frame";
    public static final String TAG_STOCK_INFO_FRAME = "stock_info_frame";
    public static final String TAG_STOCK_FRAME = "stock_frame";
    public static final String TAG_STOCK_CHART_FRAME = "stock_chart_frame";
    public static final String TAG_STOCK_HISTORICAL_FRAME = "stock_historical_frame";
    public static final String TAG_STOCK_INTRADAY_FRAME = "stock_intraday_frame";
    public static final String TAG_STOCK_NEWS_FRAME = "stock_news_frame";
    public static final String TAG_TOP_GLM_FRAME = "stock_top_glm_frame";
    public static final String TAG_MOST_ACTIVE_FRAME = "stock_most_active";
    public static final String TAG_UPGRADE_PREMIUM_FRAME = "upgrade_premium_frame";

    public static String CURRENT_TAG = TAG_MAIN_FRAME;

    public static Long appLaunchTimeAndInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"Main Activity: onCreate");
        // session manager
        session = new SessionManager(getApplicationContext());
        snackBarDisplay = new SnackBarDisplay(this);
        pDialog = new ProgressDialog(this);
        fbLogAdEvents = new FbLogAdEvents(this);
        
        if (session.getTheme() == 0) {
            Log.e(TAG, "Select Dark Theme: " + String.valueOf(session.getTheme()));
            setTheme(R.style.AppTheme);
        } else if (session.getTheme() == 1) {
            setTheme(R.style.LightAppTheme);
        }

        new AppRater(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this,
                getString(R.string.admob_app_id));

        logger = AppEventsLogger.newLogger(this);
        appLaunchTimeAndInterstitialAd = System.currentTimeMillis();

        imageView = findViewById(R.id.imageView);

        animation = new AlphaAnimation(1, 0); //to change visibility from visible to invisible
        animation.setDuration(300); //1 second duration for each animation cycle
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE); //repeating indefinitely
        animation.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.
        imageView.startAnimation(animation); //to start animation

        refreshBtn = (Button) findViewById(R.id.action_refresh);

        mHandler = new Handler();

//************************* Start Billing ****************************************************************************

        // Start the controller and load game data
        mViewController = new MainViewController(this);

        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this, mViewController.getUpdateListener());

        Log.e(TAG, "isPremiumPurchased: " + session.isPremiumUser());

//************************* End Billing ****************************************************************************

        if (getIntent().getStringExtra("fragment") != null) {
            CURRENT_TAG = getIntent().getStringExtra("fragment");
        } else {
            Log.e(TAG, "getIntent()getStringExtra(\"fragment\") is NULL");
        }

        dbl = new LoginHandler(getApplicationContext());
        // SqLite database
        db = new DatabaseHandler(getApplicationContext());

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_NEWS);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset != 0) {
                    if (!session.isSkip()) {
                        HashMap<String, String> user = dbl.getUserDetails();
                        nav_name = (TextView) drawerView.findViewById(R.id.nav_name);
                        nav_email = (TextView) drawerView.findViewById(R.id.nav_email);
                        try {
                            nav_name.setText(user.get("name"));
                            nav_email.setText(user.get("email"));
                        } catch (NullPointerException e) {
                            Log.e(TAG, String.valueOf(e));
                        }
                    }
                }

                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (!session.isSkip()) {
                    HashMap<String, String> user = dbl.getUserDetails();
                    nav_name = (TextView) drawerView.findViewById(R.id.nav_name);
                    nav_email = (TextView) drawerView.findViewById(R.id.nav_email);
                    try {
                        Log.e(TAG, user.get("name"));
                        nav_name.setText(user.get("name"));
                        Log.e(TAG, user.get("email"));
                        nav_email.setText(user.get("email"));
                    } catch (NullPointerException e) {
                        Log.e(TAG, String.valueOf(e));
                    }
                }
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
//                toggleFab();
                super.onDrawerClosed(drawerView);

            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();


//      Below code is to show back arrow at drawer when switching through fragments.
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
//                Log.e(TAG, "getSupportFragmentManager().getBackStackEntryCount(): " + getSupportFragmentManager().getBackStackEntryCount());
                if (getSupportFragmentManager().getBackStackEntryCount() >= 1 && !CURRENT_TAG.equals(TAG_MAIN_FRAME)) {
                    toggle.setDrawerIndicatorEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);// show back button
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                } else {
                    //show hamburger
                    toggle.setDrawerIndicatorEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                }
            }
        });

//************************* Load Interstitial Ad - START ****************************************************************************
        if(!session.isPremiumUser()) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(STOCK_FRAGMENT_INTER_AD_UNIT_ID);
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

            mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened: backpress_interstitial");
                    fbLogAdEvents.logAdImpressionEvent("backpress_interstitial");
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    Log.e(TAG,"onAdLeftApplication: backpress_interstitial");
                    fbLogAdEvents.logAdClickEvent("backpress_interstitial");
                }

                @Override
                public void onAdClosed() {
                    // Increment launch counter
                    long launch_count = session.getPurchLaunchCount() + 1;
                    session.setPurchLaunchCount(launch_count);

                    // Get date of first launch
                    Long date_firstLaunch = session.getPurchDateFirstLaunch();
                    if (date_firstLaunch == 0) {
                        date_firstLaunch = System.currentTimeMillis();
                        session.setPurchDateFirstLaunch(date_firstLaunch);
                    }

                    appLaunchTimeAndInterstitialAd = System.currentTimeMillis();

                    // Load the next interstitial.
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());

                }
            });
        }
//************************* Load Interstitial Ad - END ****************************************************************************

//************************* Check User - START ****************************************************************************
        Log.e(TAG, "Check User - START");
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            for (UserInfo user : Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getProviderData()) {
                new SyncUserData(this, savedInstanceState, user, dbl, db);
                session.setSkip(false);
                break;
            }
        } else {
            session.setSkip(true);
            checkKeyAndLoadFragment(savedInstanceState);
        }
//************************* Check User - END ****************************************************************************

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (session.isSkip()) {
            navigationView.getMenu().findItem(R.id.action_logout).setVisible(false);
        } else {
            navigationView.getMenu().findItem(R.id.action_sign_up).setVisible(false);
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "Broadcast on receive : ");
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `news` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_NEWS);
                    Log.e(TAG, "Registration complete and subscribed to Topic news ");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");
                    Log.e(TAG, "Firebase message : " + message);
                    String replace = message.replaceAll("(\\[)|(\")|(\\])", "");
                    String allStock = replace.replaceAll(",", "\n");
                    Toast.makeText(getApplicationContext(), "Check Stocks Trend: \n" + allStock, Toast.LENGTH_LONG).show();

                }
            }
        };

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        handleIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            String stockId = appLinkData.getLastPathSegment();
            Log.e(TAG, "handleIntent: "+stockId);
//            TODO: Add/Display stocks directly from website opened in app with stockId
//            https://ascetx.com/stockstalker/stockId
//            Uri appData = Uri.parse("content:/ascetx.com/stockstalker/").buildUpon()
//                    .appendPath(stockId).build();
        }
    }

    private void checkKeyAndLoadFragment(Bundle savedInstanceState) {
        try {
            ksEncryptDecrypt = new KSEncryptDecrypt(this);
            if(ksEncryptDecrypt.isResources()){
                BASE_URL = getBaseUrl(session.getBaseUrl());
                ksEncryptDecrypt.decryptor();
                if(savedInstanceState == null) {
                    loadFragment();
                }
                animation.setRepeatCount(0);
                imageView.setVisibility(View.GONE);
                new VersionCheck(MainActivity.this, savedInstanceState);
            } else {
                verifySourceGetResource(savedInstanceState, false);
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | NoSuchPaddingException | UnrecoverableEntryException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void verifySourceGetResource(Bundle savedInstanceState, boolean isApiChanged) {

        if (isApiChanged){
            pDialog.setMessage("Please Wait...");
            pDialog.setCancelable(false);
            showpDialog();
        }

        String aKey = "";

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplication().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                aKey = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
//                Log.e("KeyHash", aKey);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String  tag_string_req = "string_req";
        String finalAKey = aKey;
        Log.e(TAG, URL_VERIFY_SOURCE_GET_RESOURCE);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_VERIFY_SOURCE_GET_RESOURCE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response.toString: "+response);
                String k, u;
                int api_ver;
                if (response!=null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        k = jsonObject.getString("k");
                        u = jsonObject.getString("u");
                        api_ver = jsonObject.getInt("api_ver");
                        session.setBaseUrl(u);
                        session.setApiVer(api_ver);
                        ksEncryptDecrypt.encryptor(k);
                        BASE_URL = getBaseUrl(u);
                        new VersionCheck(MainActivity.this, savedInstanceState);

                        if (savedInstanceState == null) {
                            loadFragment();
                        }
                        animation.setRepeatCount(0);
                        imageView.setVisibility(View.GONE);
                    } catch (IOException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchProviderException | UnrecoverableEntryException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Volley Error: " + error.getMessage());
                logVolleyError(error);
                hidepDialog();
                snackBarDisplay.volleyErrorOccurred(TAG, getWindow().getDecorView().findViewById(android.R.id.content), error);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                Log.e(TAG,getPackageName());
                Log.e(TAG,finalAKey);

                params.put("pId", getPackageName());
                params.put("aKey", finalAKey);
                return params;
            }
        };

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        strReq.setRetryPolicy(retryPolicy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void logVolleyError(VolleyError error){
        if (error == null || error.networkResponse == null) {
            return;
        }
        if(error instanceof NoConnectionError){
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }
            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()){
                Log.e(TAG, "Server is not connected to internet.");
            } else {
                Log.e(TAG, "Your device is not connected to internet.");
            }
        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException
                || (error.getCause().getMessage() != null
                && error.getCause().getMessage().contains("connection"))){
            Log.e(TAG, "Your device is not connected to internet.");
        } else if (error.getCause() instanceof MalformedURLException){
            Log.e(TAG, "Bad Request.");
        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                || error.getCause() instanceof JSONException
                || error.getCause() instanceof XmlPullParserException){
            Log.e(TAG, "Parse Error (because of invalid json or xml).");
        } else if (error.getCause() instanceof OutOfMemoryError){
            Log.e(TAG, "Out Of Memory Error.");
        }else if (error instanceof AuthFailureError){
            Log.e(TAG, "Server couldn't find the authenticated request.");
        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
            Log.e(TAG, "Server is not responding.");
        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                || error.getCause() instanceof ConnectTimeoutException
                || error.getCause() instanceof SocketException
                || (error.getCause().getMessage() != null
                && error.getCause().getMessage().contains("Connection timed out"))) {
            Log.e(TAG, "Connection timeout error");
        } else {
            Log.e(TAG, "An unknown error occurred.");
        }
    }

    private String getBaseUrl(String u){
        byte[] data = Base64.decode(u, Base64.DEFAULT);
        String text = new String(data, StandardCharsets.UTF_8);
        Log.e(TAG,text);
        return text;
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG,"onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    private void loadFragment() {

            // set toolbar title
            setToolbarTitle();

            // if user select the current navigation menu again, don't do anything
            // just close the navigation drawer
//            if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
//                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//                drawer.closeDrawer(GravityCompat.START);

                // show or hide the fab button
//                toggleFab();
//                return;
//            }

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
//                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.addToBackStack(CURRENT_TAG);
                    fragmentTransaction.commitAllowingStateLoss();
                    FragmentManager fm = getSupportFragmentManager();
                    Log.e(TAG,"loadFragment BackStackEntryCount: "+ fm.getBackStackEntryCount());
//                    fragmentTransaction.commit();
//                    commitAllowingStateLoss is use because of IllegalStateException: Can not perform this action after onSaveInstanceState
//                    Such an exception will occur if you try to perform a fragment transition after your fragment activity's onSaveInstanceState() gets called.
                }
            };

            // If mHandler is not null, then add to the message queue
            if (mHandler != null) {
                mHandler.post(mPendingRunnable);
            }

        // show or hide the fab button
//        toggleFab();

        /*//Closing drawer on item click
        drawer.closeDrawers();*/

            // refresh toolbar menu
//            invalidateOptionsMenu();
    }

    private void loadFragment(Fragment currentFragment) {
        // set toolbar title
        setToolbarTitle();

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);

                if (currentFragment instanceof StockFragment || currentFragment instanceof StockNewsFragment){
                    StockInfoFragment stockInfoFragment = (StockInfoFragment) getSupportFragmentManager().findFragmentByTag(TAG_STOCK_INFO_FRAME);
                    assert stockInfoFragment != null;
                    fragmentTransaction.hide(stockInfoFragment);
                } else {
                    fragmentTransaction.hide(currentFragment);
                }

                fragmentTransaction.add(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.addToBackStack(CURRENT_TAG);
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
            case TAG_MAIN_FRAME:
                return new MainFragment();
            case TAG_STOCK_INFO_FRAME:
                return new StockInfoFragment();
            case TAG_STOCK_HISTORICAL_FRAME:
                return new HistoricalFragment();
            case TAG_STOCK_CHART_FRAME:
                return new GraphFragment();
            case TAG_STOCK_INTRADAY_FRAME:
                return new IntradayFragment();
            case TAG_TOP_GLM_FRAME:
                return new TopGLMFragment();
            case TAG_MOST_ACTIVE_FRAME:
                return new MostActiveFragment();
            case TAG_STOCK_NEWS_FRAME:
                return new NewsFragment();
            case TAG_UPGRADE_PREMIUM_FRAME:
                return new UpgradeFragment();
            default:
                return new MainFragment();
        }
    }

   /* private void updateUserAppDB() {
        Log.e(TAG,"updateUserAppDB");
        // Tag used to cancel the request
        String tag_string_req = "tag_updateUserAppDB";
        // Fetching user details from sqlite
        HashMap<String, String> user = dbl.getUserDetails();
        String email = user.get("email");
        String url = URL_STOCK + email;
        swipeRefreshLayout.setRefreshing(true);
        JsonArrayRequest strReq = new JsonArrayRequest(
                url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                Log.d(TAG, "User Stock Response: " + response.toString());

                if (response.length() > 0) {

                    // Delete whole db table NseStocks
                    db.deleteStockWhole();

                    // looping through json and adding to stock list
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject stockObj = response.getJSONObject(i  );

                            String name = stockObj.getString("name");
                            String stock = stockObj.getString("stock");
                            m = new StockName(name, stock, null, null, null, null, null);

                            int j = stockNameDate.size();
//                            Log.d(TAG, Integer.toString(j));

                            stockNameDate.add(0,m);
                            Log.d(TAG, stockNameDate.toString());

                            // Add values to db table NseStocks as being refreshed
                            db.addStockName(stock, name);

                        } catch (JSONException e) {
//                            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                        }
                    }
                    int j = stockNameDate.size();
                    notify = j;

//                    fetchStockCurrent();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Server Error: " + error.getMessage());
                showSnackBar(getWindow().getDecorView().getRootView(),getResources().getString(R.string.msg_check_internet));

                List<StockName> stockName = db.getAllStockNames(getApplicationContext());

                for (StockName m : stockName) {
                    stockNameDate.add(0, m);
                }
                if(stockNameDate != null)
                    adapter.notifyDataSetChanged();

                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }*/

    public static void updRegID(final int login, final Context context) {
        Log.e(TAG, "in update reg id");
        String  tag_string_req = "string_req";

        Log.e(TAG, URL_UPDATE_REG_ID);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_UPDATE_REG_ID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "response.toString: "+response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley Error: " + error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                HashMap<String, String> user = dbl.getUserDetails();
                String email = user.get("email");
                SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
                String regId = pref.getString("regId", null);
                Log.d(TAG, "Reg ID " + regId);
                Map<String, String> params = new HashMap<String, String>();

                assert email != null;
                params.put("email", email);
                assert regId != null;
                params.put("regId", regId);
                params.put("login", Integer.toString(login));
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void addStockAlertDialog(final View view) {
        autoComplete = View.inflate(this,R.layout.auto_complete,null);
        isin = (TextView) autoComplete.findViewById(R.id.isin);
        stock = (TextView) autoComplete.findViewById(R.id.stock);
        bookTitle = (DelayAutoCompleteTextView) autoComplete.findViewById(R.id.et_book_title);
        bookTitle.setThreshold(2);
        bookTitle.setAdapter(new BookAutoCompleteAdapter(this,autoComplete)); // 'this' is Activity instance
        bookTitle.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
        bookTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 1 && !charSequence.toString().trim().equals(""))
                    autoComplete.findViewById(R.id.pb_loading_indicator).setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 1 || charSequence.toString().trim().equals(""))
                    autoComplete.findViewById(R.id.pb_loading_indicator).setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Book book = (Book) adapterView.getItemAtPosition(position);
                bookTitle.setText(book.getName());
                stock.setText(book.getTitle());
                isin.setText(book.getAuthor());
            }
        });

        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add a stock to your stalk list!"); //Set Alert dialog title here
        alert.setMessage("Enter NASDAQ/NYSE/AMEX listed company name"); //Message here
        alert.setView(autoComplete);
        // Set an EditText view to get user input
//        final EditText input = new EditText(this);
//        input.setSingleLine(true);

        // End of onClick(DialogInterface dialog, int whichButton)
        alert.setPositiveButton("ADD", (dialog, whichButton) -> {
            //You will get as string input data in this variable.
            // here we convert the input to a string and show in a toast.
            String name = bookTitle.getText().toString();
            String id = stock.getText().toString();
            if (!id.isEmpty()) {
//                    if(!session.isSkip()) {
                    FragmentManager fm = getSupportFragmentManager();
                    MainFragment fragment = (MainFragment)fm.findFragmentByTag(CURRENT_TAG);
                    assert fragment != null;
                    fragment.addStock(id, name, false);
//                    }
//                    else {
//                        skipAlert(getResources().getString(R.string.skip_add));
//                    }
            }
            else{
                showSnackBar(view,"Please select stock from suggestions");
            }
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

    private void addStockTGLM(String name, String id){
        autoComplete = View.inflate(this,R.layout.auto_complete,null);
        isin = (TextView) autoComplete.findViewById(R.id.isin);
        stock = (TextView) autoComplete.findViewById(R.id.stock);
        bookTitle = (DelayAutoCompleteTextView) autoComplete.findViewById(R.id.et_book_title);
        isin.setVisibility(View.GONE);
        bookTitle.setText(name);
        stock.setText(id);
        bookTitle.setFocusable(false);

        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.wish_to_add_stock)); //Set Alert dialog title here
//        alert.setMessage("Enter NASDAQ/NYSE/AMEX listed company name"); //Message here
        alert.setView(autoComplete);

        // End of onClick(DialogInterface dialog, int whichButton)
        alert.setPositiveButton("ADD", (dialog, whichButton) -> {
            //You will get as string input data in this variable.
            // here we convert the input to a string and show in a toast.
            if (!id.isEmpty()) {
//                    if(!session.isSkip()) {
                FragmentManager fm = getSupportFragmentManager();
                MainFragment fragment = (MainFragment)fm.findFragmentByTag(TAG_MAIN_FRAME);
                assert fragment != null;
                fragment.addStock(id, name, true);
            } else{
                showSnackBar(getWindow().getDecorView().findViewById(android.R.id.content),"Please select stock from suggestions");
            }

//            Open Main Fragment
            CURRENT_TAG = TAG_MAIN_FRAME;
            if(getBackStackFragment().equals(CURRENT_TAG))
                simpleBackPress();
            else
                loadFragment();

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

    private void stockStalkLimitReachedAlertDialog(){
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.stock_stalk_limit_reached_title)); //Set Alert dialog title here
        alert.setMessage(getResources().getString(R.string.stock_stalk_limit_reached_message)); //Message here
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        /* Alert Dialog Code End*/
    }

    private void skipAlert(String skipMessage) {
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Please Sign Up"); //Set Alert dialog title here
        alert.setMessage(skipMessage); //Message here

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new SignUp(MainActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Context wrapper = new ContextThemeWrapper(this,R.style.MyPopupMenu);
//        PopupMenu popupMenu = new PopupMenu(wrapper,);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem suMenuItem = menu.findItem(R.id.action_sign_up);
        MenuItem loMenuItem = menu.findItem(R.id.action_logout);
        if(session.isSkip())
            loMenuItem.setVisible(false);
        else
            suMenuItem.setVisible(false);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET  |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_app_message_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_app_message_body));
        // Fetch and store ShareActionProvider
//        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        ShareActionProvider mShareActionProvider = new ShareActionProvider(this) {
            @Override
            public View onCreateActionView() {
                return null;
            }
        };
        mShareActionProvider.setShareIntent(sharingIntent);
        item.setIcon(R.drawable.abc_ic_menu_share_mtrl_alpha);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);
//        https://play.google.com/store/apps/details?id=info.ascetx.stockstalker

//        startActivity(Intent.createChooser(sharingIntent, "Share via"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //To handle navigation drawer hamburger click
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_sign_up) {
            signUpPopup();
        }
        if (id == R.id.action_refresh) {
            Log.e(TAG, "Refresh clicked");
            animationDrawable = (AnimationDrawable) item.getIcon();
            animationDrawable.start();
            try {
                switch (CURRENT_TAG) {
                    case TAG_MAIN_FRAME:
                        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        mainFragment.onRefresh();
                        break;
                    case TAG_STOCK_INFO_FRAME:
                        StockInfoFragment stockInfoFragment = (StockInfoFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        stockInfoFragment.onRefresh();
                        break;
                    case TAG_STOCK_HISTORICAL_FRAME:
                        HistoricalFragment historicalFragment = (HistoricalFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        historicalFragment.onRefresh();
                        break;
                    case TAG_STOCK_CHART_FRAME:
                        GraphFragment graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        graphFragment.onRefresh();
                        break;
                    case TAG_STOCK_INTRADAY_FRAME:
                        IntradayFragment intradayFragment = (IntradayFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        intradayFragment.onRefresh();
                        break;
                    case TAG_TOP_GLM_FRAME:
                        TopGLMFragment topGLMFragment = (TopGLMFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        topGLMFragment.onRefresh();
                        break;
                    case TAG_MOST_ACTIVE_FRAME:
                        MostActiveFragment mostActiveFragment = (MostActiveFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        mostActiveFragment.onRefresh();
                        break;
                    case TAG_STOCK_NEWS_FRAME:
                        NewsFragment newsFragment = (NewsFragment) getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                        newsFragment.onRefresh();
                        break;
                }
            } catch (NullPointerException npe) {
                Log.e(TAG, npe.getMessage());
            }
        }
        if (id == R.id.action_logout) {
            new LogOut(this);
        }

        return super.onOptionsItemSelected(item);
    }

    public void RateReviewApp(MenuItem item){
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET  |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    public void reportBug(MenuItem item) {
        String body = null;
        try {
            body = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
            body = body.replace("\n", "<br/>");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String subject = "Bug Report from Stock Stalker";
        String to = getString(R.string.email_ascetx);

        StringBuilder builder = new StringBuilder("mailto:" + Uri.encode(to));
        char operator = '?';
        builder.append(operator).append("subject=").append(Uri.encode(subject));
        operator = '&';
        if (body != null) {
            builder.append(operator).append("body=").append(Uri.encode(body));
        }
        String uri = builder.toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)));
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "On Resume");

        // Note: We query purchases in onResume() to handle purchases completed while the activity
        // is inactive. For example, this can happen if the activity is destroyed during the
        // purchase flow. This ensures that when the activity is resumed it reflects the user's
        // current purchases.
        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode() == BillingClient.BillingResponseCode.OK) {
            mBillingManager.queryPurchases();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
//            toggleFab();
            if(!session.isSkip()) {
                HashMap<String, String> user = dbl.getUserDetails();
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                nav_name = (TextView) headerView.findViewById(R.id.nav_name);
                nav_email = (TextView) headerView.findViewById(R.id.nav_email);
                if(user.get("name")!=null)
                    nav_name.setText(user.get("name"));
                if(user.get("email")!=null)
                    nav_email.setText(user.get("email"));
            }
        }

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivity.mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivity.mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "On pause");
        session.setFirstLoginReg(false);
//        Log.e(TAG,"Executor isShutdown: "+executorService.isShutdown());
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MainActivity.mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /*private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

//        Log.e(TAG, "Firebase reg id: " + regId);
//        Toast.makeText(this, "Firebase reg id: " + regId, Toast.LENGTH_SHORT).show();

        if (TextUtils.isEmpty(regId))
//            Log.e(TAG, "Firebase Reg Id is not received yet!");

    }*/

    @Override
    public void onBackPressed() {

//        session.setFirstLoginReg(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{
            switch(CURRENT_TAG){
                case TAG_MAIN_FRAME:
                    exitAppAlert();
                    break;
                case TAG_STOCK_INFO_FRAME:
                case TAG_TOP_GLM_FRAME:
                case TAG_MOST_ACTIVE_FRAME:
                case TAG_UPGRADE_PREMIUM_FRAME:
                    showInterstitialAd();
                    CURRENT_TAG = TAG_MAIN_FRAME;
                    if(getBackStackFragment().equals(CURRENT_TAG))
                        simpleBackPress();
                    else
                        loadFragment();
                    break;
                case TAG_STOCK_HISTORICAL_FRAME:
                case TAG_STOCK_CHART_FRAME:
                case TAG_STOCK_INTRADAY_FRAME:
                    showInterstitialAd();
                    CURRENT_TAG = TAG_STOCK_INFO_FRAME;
                    if(getBackStackFragment().equals(CURRENT_TAG))
                        simpleBackPress();
                    else
                        loadFragment();
                    break;
                case TAG_STOCK_NEWS_FRAME:
                    showInterstitialAd();
                    if (NewsFragment.webView.canGoBack()) {
                        NewsFragment.webView.goBack();
                    } else {
                        CURRENT_TAG = TAG_STOCK_INFO_FRAME;
                        if(getBackStackFragment().equals(CURRENT_TAG))
                            simpleBackPress();
                        else
                            loadFragment();
                    }
                    break;
            }
            FragmentManager fm = getSupportFragmentManager();
            Log.e(TAG,"onBackPressed BackStackEntryCount: "+ fm.getBackStackEntryCount());
        }
        AppController.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    private void showInterstitialAd() {
        if (!session.isPremiumUser()) {

            if(mInterstitialAd == null)
                return;

            if (mInterstitialAd.isLoaded()) {
                if (System.currentTimeMillis() >= appLaunchTimeAndInterstitialAd +
                        (SECONDS_UNTIL_INTERSTITIAL_PROMPT * 1000)) {
                    mInterstitialAd.show();
                } 
            }
        } 
    }


    private String getBackStackFragment(){

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            Log.e(TAG, "BackStackEntryCount() == 0");
            return "null";
        }
        else {
            Log.e(TAG, "onBackPressed BackStackEntryCount: " + getSupportFragmentManager().getBackStackEntryCount());
            String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Log.e(TAG, "fragmentTag: " + fragmentTag);
            String fragmentTag2 = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2).getName();
            Log.e(TAG, "fragmentTag: " + fragmentTag2);
            return fragmentTag2;
        }
    }

    private void simpleBackPress() {
        super.onBackPressed();
        setToolbarTitle();
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

    public void checkRestorePurchase(String originalJson){
        Log.e(TAG, "In checkRestorePurchase");
        Log.e(TAG, originalJson);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        try {
            JSONObject jsonObject = new JSONObject(originalJson);
            session.setProductId(jsonObject.getString("productId"));
            session.setPackageName(jsonObject.getString("packageName"));
            if (!jsonObject.getBoolean("autoRenewing"))
                navigationView.getMenu().findItem(R.id.restore_purchase).setVisible(true);
        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }
        sendUserSubscriptionData(originalJson);
    }

    private void sendUserSubscriptionData(String originalJson) {
        String  tag_string_req = "string_req";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_NSQ_USER_SUBSCRIBED, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(!jsonObject.getBoolean("error"))
                        Log.e(TAG, "Success response: " + jsonObject.getString("error_msg"));
                    else
                        Log.e(TAG, "Error response: " + jsonObject.getString("error_msg"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Volley Error: " + error.getMessage());
                snackBarDisplay.volleyErrorOccurred(TAG, getWindow().getDecorView().findViewById(android.R.id.content), error);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                String email = null;
                if (auth.getCurrentUser() != null) {
                    email = auth.getCurrentUser().getEmail();
                }
                if(email == null || email.isEmpty()) {
                    email = getResources().getString(R.string.email_ascetx);
                }
                Log.e(TAG,email);

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("json", originalJson);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == android.R.id.home){
            Log.e(TAG, "Nav Home");
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }else{
                drawer.openDrawer(GravityCompat.START);
            }
        }
        /*if(id == R.id.action_index){
        *//* Alert Dialog Code Start*//*
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Coming Soon"); //Set Alert dialog title here
            alert.setMessage("In progress for BSE, NASDAQ etc..."); //Message here

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                } // End of onClick(DialogInterface dialog, int whichButton)
            }); //End of alert.setPositiveButton
            AlertDialog alertDialog = alert.create();
            alertDialog.show();
       *//* Alert Dialog Code End*//*
        }*/

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_premium) {
            if (mAcquireDialog == null) {
                mAcquireDialog = new AcquireDialog(session);
            }

            if (mBillingManager != null
                    && mBillingManager.getBillingClientResponseCode()
                    > BILLING_MANAGER_NOT_INITIALIZED) {
                Log.e(TAG,"onManagerReady: "+mBillingManager.getBillingClientResponseCode());
                mAcquireDialog.onManagerReady(this);
            }

            mAcquireDialog.handleManagerAndUiReady(this);
        }

        if (id == R.id.restore_purchase) {
            if (isPremiumPurchased()) {
                String url;
                if(session.getProductId()!=null){
                    url = "https://play.google.com/store/account/subscriptions?sku="
                            + session.getProductId() + "&package=" + session.getPackageName();
                } else {
                    url = "https://play.google.com/store/account/subscriptions";
                }
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url)));
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Purchase Error");
                alertDialog.setMessage("Nothing to restore");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }

        if (id == R.id.action_top_gain_loss) {
            if (!CURRENT_TAG.equals(TAG_TOP_GLM_FRAME)) {
                CURRENT_TAG = TAG_TOP_GLM_FRAME;
                loadFragment();
            }
        }
        if (id == R.id.action_most_active) {
            if (!CURRENT_TAG.equals(TAG_MOST_ACTIVE_FRAME)) {
                CURRENT_TAG = TAG_MOST_ACTIVE_FRAME;
                loadFragment();
            }
        }
//        if (id == R.id.action_trend) {
//            Intent intent = new Intent(MainActivity.this, TrendActivity.class);
//            startActivity(intent);
//        }
        if (id == R.id.action_help) {
            helpPopup();
        }
        if (id == R.id.action_logout) {
            new LogOut(this);
        }
        if (id == R.id.action_sign_up) {
            signUpPopup();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void signUpPopup(){
        new SignUp(this);
    }

    public void helpPopup() {
        /* Alert Dialog Code Start*/
        LayoutInflater inflater = getLayoutInflater();
        View aboutLayout = inflater.inflate(R.layout.alert_help, null);
        TextView abbr = (TextView) aboutLayout.findViewById(R.id.abbr_tv);
//        TextView st = (TextView) aboutLayout.findViewById(R.id.st_tv);
        Button bt = (Button) aboutLayout.findViewById(R.id.help_bt);

        Resources res = getResources();
        String text = res.getString(R.string.alert_help_abbr);
        CharSequence styledText = Html.fromHtml(text);
        abbr.setText(styledText);

        AlertDialog.Builder ad_about = new AlertDialog.Builder(this);
        ad_about.setView(aboutLayout);
        final AlertDialog alertDialog = ad_about.create();
        alertDialog.show();

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void purchasePopUp (){
        if (mAcquireDialog == null) {
            mAcquireDialog = new AcquireDialog(session);
        }

        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode()
                > BILLING_MANAGER_NOT_INITIALIZED) {
            Log.e(TAG,"onManagerReady: "+mBillingManager.getBillingClientResponseCode());
            mAcquireDialog.onManagerReady(this);
        }

        mAcquireDialog.handleManagerAndUiReady(this);
    }

    private void createUpgradePremium(UpgradeFragment upgradeFragment){

        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode()
                > BILLING_MANAGER_NOT_INITIALIZED) {
            Log.e(TAG,"onManagerReady: "+mBillingManager.getBillingClientResponseCode());
            upgradeFragment.onManagerReady(this);
            upgradeFragment.handleManagerAndUiReady(this);
        }

    }

    private void showSnackBar(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.snackbar_err_color));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

/*    // show or hide the fab
    private void toggleFab() {
        *//*if (CURRENT_TAG.equals(TAG_MAIN_FRAME)){
            if (fab.isShown())
                fab.hide();
            else
                fab.show();
        } else {
            fab.hide();
        }*//*
    }*/

    @Override
    public void onStockFragmentInteraction(Fragment stockFragment) {
        loadFragment(stockFragment);
    }


    @Override
    public void onFragmentInteraction() {
        loadFragment();
    }

    @Override
    public void onFragmentFabClick(View view) {
        List<StockName> stockName = db.getAllStockNames(this);
        if(!session.isPremiumUser()){
            if ((stockName.size() >= TRAIL_USER_STOCK_STALKER_LIMIT)){
                showUpgradePremiumDialog();
            } else {
                addStockAlertDialog(view);
            }
        } else {
            if (!(stockName.size() >= STOCK_STALKER_LIMIT))
                addStockAlertDialog(view);
            else
                stockStalkLimitReachedAlertDialog();
        }
    }

    private void showUpgradePremiumDialog() {
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        alert.setTitle(this.getResources().getString(R.string.upgrade_to_premium));
        alert.setMessage(Html.fromHtml("<font color='#ffffff'>Upgrade to add more stocks and get premium access to all" +
                " cool features! <br><b>\uD83D\uDD25 30% OFF Discount available now!</b></font>")); //Message here
        alert.setPositiveButton("LEARN MORE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment =  fm.findFragmentByTag(CURRENT_TAG);
                CURRENT_TAG = TAG_UPGRADE_PREMIUM_FRAME;
                loadFragment(fragment);
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton

        AlertDialog alertDialog = alert.create();
        alertDialog.show();

        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(this, R.color.premium_alert_dialog_button_positive));
        /* Alert Dialog Code End*/
    }

    @Override
    public void onMainFragmentUPInteraction(MainFragment mainFragment) {
        loadFragment(mainFragment);
    }

    @Override
    public void onFragmentInteractionPurchPopUp() {
        purchasePopUp();
    }

    private void setToolbarTitle() {
        String stockName = session.getStockName();
        String stockPeriod = session.getStockPeriod();
        switch (CURRENT_TAG){
            case TAG_STOCK_INFO_FRAME:
                getSupportActionBar().setTitle(stockName);
                break;
            case TAG_STOCK_HISTORICAL_FRAME:
            case TAG_STOCK_CHART_FRAME:
            case TAG_STOCK_INTRADAY_FRAME:
                getSupportActionBar().setTitle(stockName+" "+stockPeriod);
                break;
            case TAG_TOP_GLM_FRAME:
                getSupportActionBar().setTitle(getResources().getString(R.string.action_top_gl));
                break;
            case TAG_MOST_ACTIVE_FRAME:
                getSupportActionBar().setTitle(getResources().getString(R.string.action_most_active));
                break;
            case TAG_STOCK_NEWS_FRAME:
                getSupportActionBar().setTitle(stockName + " News");
                break;
            default:
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
    }

    @Override
    protected void onDestroy() {
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        super.onDestroy();
    }

    public void onBillingManagerSetupFinished() {
        if (mAcquireDialog != null) {
            mAcquireDialog.onManagerReady(this);
        }
    }

    @Override
    public BillingManager getBillingManager() {
        return mBillingManager;
    }

    @Override
    public boolean isPremiumPurchased() {
        return mViewController.isPremiumPurchased();
    }

    @Override
    public boolean isGoldMonthlySubscribed() {
        return false;
    }

    @Override
    public boolean isGoldYearlySubscribed() {
        return false;
    }

    @Override
    public void onIntraHistFragmentInteraction() {
        setToolbarTitle();
    }

    @Override
    public void onListFragmentInteractionAddStock(String name, String id) {
        List<StockName> stockName = db.getAllStockNames(this);
        if(!session.isPremiumUser()){
            if ((stockName.size() >= TRAIL_USER_STOCK_STALKER_LIMIT)){
                showUpgradePremiumDialog();
            } else {
                addStockTGLM(name, id);
            }
        } else {
            if (!(stockName.size() >= STOCK_STALKER_LIMIT))
                addStockTGLM(name, id);
            else
                stockStalkLimitReachedAlertDialog();
        }
    }

    @Override
    public void onUserDataSynced(Bundle savedInstanceState) {
        checkKeyAndLoadFragment(savedInstanceState);
    }

    @Override
    public void onUpgradePremiumFragmentCreate(UpgradeFragment upgradeFragment) {
        createUpgradePremium(upgradeFragment);
    }

    public void showRefreshedUi() {
        if (TAG_UPGRADE_PREMIUM_FRAME.equals(CURRENT_TAG)) {
            CURRENT_TAG = TAG_MAIN_FRAME;
            simpleBackPress();
        }
        if (TAG_MAIN_FRAME.equals(CURRENT_TAG)) {
            FragmentManager fm = getSupportFragmentManager();
            MainFragment fragment = (MainFragment)fm.findFragmentByTag(CURRENT_TAG);
            if(fragment!=null) {
                try {
                    fragment.upgrade_premium.setVisibility(View.GONE);
                    fragment.mAdView.setVisibility(View.GONE);
                } catch (NullPointerException e){
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
}
