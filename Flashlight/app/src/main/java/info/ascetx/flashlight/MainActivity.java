package info.ascetx.flashlight;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.android.billingclient.api.BillingClient;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.yodo1.mas.Yodo1Mas;
import com.yodo1.mas.banner.Yodo1MasBannerAdSize;
import com.yodo1.mas.banner.Yodo1MasBannerAdView;
import com.yodo1.mas.error.Yodo1MasError;
import com.yodo1.mas.event.Yodo1MasAdEvent;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import info.ascetx.flashlight.activity.ScreenLightActivity;
import info.ascetx.flashlight.activity.SettingsActivity;
import info.ascetx.flashlight.app.AcquireDialog;
import info.ascetx.flashlight.app.AppRater;
import info.ascetx.flashlight.app.FALogEvents;
import info.ascetx.flashlight.app.FbLogAdEvents;
import info.ascetx.flashlight.app.LaunchPlayStoreConfirmationDialog;
import info.ascetx.flashlight.app.MainViewController;
import info.ascetx.flashlight.app.SessionManager;
import info.ascetx.flashlight.app.configs.RemoteConfigs;
import info.ascetx.flashlight.app.nativeAds.NativeAdManager;
import info.ascetx.flashlight.app.updater.InAppUpdater;
import info.ascetx.flashlight.billing.BillingManager;
import info.ascetx.flashlight.billing.BillingProvider;

import static info.ascetx.flashlight.app.AppRater.APP_RATER_SHARED_PREF;
import static info.ascetx.flashlight.app.Config.adaptiveBannerAdUnit;
import static info.ascetx.flashlight.app.Config.interstitialAdUnit;
import static info.ascetx.flashlight.app.Config.rewardedVideoAd;
import static info.ascetx.flashlight.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED;

public class MainActivity extends AppCompatActivity implements SensorEventListener, BillingProvider {

    public static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    public static final int IN_APP_UPDATE_REQUEST_CODE = 404;

    private FirebaseRemoteConfig remoteConfig;

    //    private ImageButton btnSwitch;
    // define the display assembly compass picture
    private ImageView ivSliderBg, ivFlashlightStrobe, ivFlashlightGroove, ivCompassBg, ivCompassScreen, ivCompassDisc, ivCompassArrow, btnScreenLight, btnSwitch, btnSos, btnSettings;
    private ImageView newAppIcon;
    public ImageView btnRemAd;
    public Drawable newThumbBtnOn, newThumbBtnOff;
    private DigitalClock digitalClock;
    private FirebaseAnalytics mFirebaseAnalytics;
    public FALogEvents faLogEvents;
    private SeekBar strobeSeekBar;
    public static SessionManager session;
    // record the compass picture angle turned
    private float currentDegree = 0f;

    private float seekbarThumbHW = 100;

    // device sensor manager
    private SensorManager mSensorManager;
    //private Button flashFreq;
    //private Button [] freq;
    private TextView tvDirection, tvCompassError;
    public FrameLayout flSlider, adContainerView;

    private Camera camera;
    private final Object lock = new Object();

    private static boolean isFlashOn;
    private static boolean isFlashSOSOn;
    private static boolean hasFlash;
    private Parameters params;
    private MediaPlayer mp;

    private CameraManager mCameraManager;
    private String mCameraId;

    private static Timer t;
//    private Timer showInterstitialAd;
    private static boolean isOn = false;
    private static boolean isOnPause = false;
    private static boolean fromOnPause = false;
    private static boolean pauseIntAd = false;
    private static boolean pauseBilling = false;
    private static int selectedFlashRate = 0;
    private static int flashCount = 0;
    private ArrayList<Integer> morseArray;
    private ArrayList<String> skuList;

    private AdView adaptiveAdView;
    private InterstitialAd mInterstitialAd;
    public RewardedAd rewardedAd;
    private NativeAdManager nativeAdManager;
    boolean isLoading;

    public static final int REQUEST_CODE = 1001;

    private MainViewController mViewController;
    private BillingManager mBillingManager;
    private AcquireDialog mAcquireDialog;

    private NotificationManager notificationManager;
    private RemoteViews notificationView;
    private Notification notification;
    private NotificationCompat.Builder mBuilder;

    private switchButtonListener sbl;
    private Long appLaunchTimeAndInterstitialAd;

    private FbLogAdEvents fbLogAdEvents;
    private PowerManager pm;
    private static boolean screenOn;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private static boolean cameraStatus = false;

    private SharedPreferences sharedPref;
    private boolean switchPref;
    private StrictMode.ThreadPolicy oldPolicy;
    private static MainActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        view.setKeepScreenOn(true);
        setContentView(view);

        initMobileAds();

        instance = this;

        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()             // Checks for all violations
                    .penaltyLog()            // Output violations via logging
                    .build()
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                StrictMode.setVmPolicy(new StrictMode.VmPolicy
                        .Builder()
                        .detectNonSdkApiUsage()  // Detect private API usage
                        .penaltyLog()            // Output violations via logging
                        .build()
                );
            }
        }

        boolean isOpenedFromNotification = getIntent().getBooleanExtra("isOpenedFromNotification", false);
        fromOnPause = false;

        // session manager
         oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            // Do reads here
            session = new SessionManager(this);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        fbLogAdEvents = new FbLogAdEvents(this);

        logError("On create");
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        
        faLogEvents = new FALogEvents(mFirebaseAnalytics);

        final Activity mActivity = this;

        oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            new AppRater(this); // check for showing ads after this
            getAdsSettings(this);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        setFirebaseRemoteConfigs();

        loadRewardedAd();

//************************* Start Rating Dialogue ****************************************************************************
//        checkRatingDialog(this);
//************************* End Rating Dialogue ****************************************************************************

        appLaunchTimeAndInterstitialAd = System.currentTimeMillis();
//************************* Start Billing ****************************************************************************

        // Start the controller and load game data
        mViewController = new MainViewController(this);
        logError("Start Billing");

        // Create and initialize BillingManager which talks to BillingLibrary
        mBillingManager = new BillingManager(this, mViewController.getUpdateListener());
        logError("isPremiumPurchased: " + session.isPremiumUser());

//************************* End Billing ****************************************************************************
//************************* Start Setting ****************************************************************************

        oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

//************************* End Setting ****************************************************************************

//************************* To Keep Blinking Running even After Screen Close - START ********************************************

        // assume we start with screen on and save that state ;-)
        this.pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        screenOn = this.pm.isScreenOn();

        // program a timer which checks if the light needs to be re-activated
        this.mTimer = new Timer();
        this.mTimerTask = new TimerTask() {
            public void run() {
                // re-activate the LED if screen turned off
                if(!pm.isScreenOn() && pm.isScreenOn() != screenOn) {
                    Log.i("SleepLEDservice", "re-activated the LED");

                    // really it's NOT ENOUGH to just "turn it on", i double-checked this
                    setFlashlight(Parameters.FLASH_MODE_OFF);
                    setFlashlight(Parameters.FLASH_MODE_TORCH);
                }
                screenOn = pm.isScreenOn();
            }
        };
//************************* To Keep Blinking Running even After Screen Close - END ********************************************
        oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(this);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        switchPref = sharedPref
                .getBoolean("notification", true);

        if (switchPref)
            startNotification();
        

        adContainerView = findViewById(R.id.ad_view_container);
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        // Comment below if you want real ads
//        MobileAds.setRequestConfiguration(
//                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("AB324050C36DD246F13695AEB8C58061")).build());

        logError("startShowingAds: "+startShowingAds());
        if (!session.isPremiumUser() && startShowingAds()) {
            if (new Date().getTime() > session.getRewardAdDateTime().getTime()) {

                adContainerView.post(new Runnable() {
                    @Override
                    public void run() {
                        loadAdaptiveBanner();
                    }
                });
                
                loadInterstitialAd();

            }
        }


/*********************** Starting connection to iap *****************************
 mServiceConn = new ServiceConnection() {
@Override public void onServiceDisconnected(ComponentName name) {
mService = null;
}

@Override public void onServiceConnected(ComponentName name,
IBinder service) {
mService = IInAppBillingService.Stub.asInterface(service);
new GetItemList(getPackageName()).execute();
}
};
 Intent serviceIntent =
 new Intent("com.android.vending.billing.InAppBillingService.BIND");
 serviceIntent.setPackage("com.android.vending");
 bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
 /************************ END - Starting connection to iap *****************************/

        flSlider = findViewById(R.id.fl_slider);
        ivSliderBg = findViewById(R.id.slider_bg);

        ivFlashlightStrobe = findViewById(R.id.flashlight_strobe);

        ivFlashlightGroove = findViewById(R.id.flashlight_groove);
        ivCompassBg = findViewById(R.id.ivCompassBg);
        ivCompassDisc = findViewById(R.id.imageViewCompass);
        ivCompassScreen = findViewById(R.id.ivComapssScreen);
        ivCompassArrow = findViewById(R.id.compass_arrow);
        digitalClock = findViewById(R.id.digitalClock);
        btnSos = findViewById(R.id.fabBtnSos);
        btnRemAd = findViewById(R.id.fabBtnRemAd);
        strobeSeekBar = findViewById(R.id.strobe_seekbar);

        ivSliderBg.getImageMatrix();

        strobeSeekBar.setProgress(0);
        strobeSeekBar.incrementProgressBy(10);
        strobeSeekBar.setMax(100);

        strobeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            final float smoothnessFactor = 10;
            int progressStart, progressEnd, progressChangedTimes;
            final int DRAG_THRESHOLD = 5;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                logError("onProgressChanged: " + progress);
                ++progressChangedTimes;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                logError("onStartTrackingTouch: "+seekBar.getProgress());
                progressStart = seekBar.getProgress();
                progressChangedTimes = 0;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                strobeSeekBar.setProgress((int) (Math.round((seekBar.getProgress() + (smoothnessFactor / 2)) / smoothnessFactor) * smoothnessFactor));
                logError("onStopTrackingTouch: "+seekBar.getProgress());
                faLogEvents.logButtonClickEvent("strobe_seek_bar");

                int flashFreq = 1;

                int progressEndInt = seekBar.getProgress();

                progressEnd = progressEndInt;
                logError("Progress Value-Before: " + progressEnd);
                progressEnd = (int) (Math.round(progressEnd/smoothnessFactor) * smoothnessFactor);
                logError("Progress Value-After: " + progressEnd);

                if (progressEnd == 0){
                    flashFreq = -1;
                } else {
                    flashFreq = (progressEnd - 10)/10;
                }
                logError("Progress Value-flashFreq: " + flashFreq);

                if (progressChangedTimes > DRAG_THRESHOLD) {
                    animateProgression(progressEndInt, progressEnd, flashFreq);
                } else {
                    animateProgression(progressStart, progressEnd, flashFreq);
                }
            }
        });

//        // Create a new ImageView
//        ImageView image = new ImageView(this);
        // Set the background color to white
//        image.setBackgroundColor(Color.WHITE);
        // Parse the SVG file from the resource
//        SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.compass2);
//        // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
//        image.setImageDrawable(svg.createPictureDrawable());
//        // Set the ImageView as the content view for the Activity
//        setContentView(image);

        // TextView that will tell the user what degree is he heading
        tvDirection = findViewById(R.id.tvDirection);

        // TextView to show if compass works for the device or not
        tvCompassError = findViewById(R.id.tvCompassError);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // for the system's orientation sensor registered listeners
        if (mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME)) {
            tvCompassError.setVisibility(View.GONE);
            logError("tvCompassError.setVisibility(View.GONE)");
        } else {
            tvDirection.setVisibility(View.GONE);
//            tvDirection.setText("");
            logError("tvDirection.setText");
        }

        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)) {
            // This device does not have a compass, turn off the compass feature
            logError("disableCompassFeature");
            tvDirection.setVisibility(View.GONE);
        }

        logError("OnCreate:mSensorManager.registerListener: " + mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME));
        // flash switch button
        btnSwitch = findViewById(R.id.btnSwitch);
        btnScreenLight = findViewById(R.id.btnScreenLight);

        // settings
        btnSettings = findViewById(R.id.settings);

        // app icon
        newAppIcon = findViewById(R.id.roundedImageView);

        /*freq = new Button[10];

        freq[0] = (Button) findViewById(R.id.freq0);
        freq[1] = (Button) findViewById(R.id.freq1);
        freq[2] = (Button) findViewById(R.id.freq2);
        freq[3] = (Button) findViewById(R.id.freq3);
        freq[4] = (Button) findViewById(R.id.freq4);
        freq[5] = (Button) findViewById(R.id.freq5);
        freq[6] = (Button) findViewById(R.id.freq6);
        freq[7] = (Button) findViewById(R.id.freq7);
        freq[8] = (Button) findViewById(R.id.freq8);
        freq[9] = (Button) findViewById(R.id.freq9);*/

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*
         * First check if device is supporting flashlight or not
         */
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        btnScreenLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnScreenLight.setImageResource(R.drawable.screen_light_btn_on);

                faLogEvents.logButtonClickEvent("screen_light_btn");

                if (hasFlash)
                    turnOffFlashAsync();

                final Intent myIntent = new Intent(MainActivity.this, ScreenLightActivity.class);
                // delay the animation
                Handler mHandler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        if (myIntent != null) {
                            startActivity(myIntent);
                        }
                    }
                };
                mHandler.postDelayed(r, 10);
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSettings.setImageResource(R.drawable.settings_btn_on);

                faLogEvents.logButtonClickEvent("settings_btn");

                final Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);

                // delay the animation
                Handler mHandler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        if (myIntent != null) {
                            startActivity(myIntent);
                        }
                    }
                };
                mHandler.postDelayed(r, 10);
            }
        });

        newAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faLogEvents.logButtonClickEvent("new_app_icon_btn");

                showLaunchPlayStoreConfirmationDialog();

            }
        });

        if (!hasFlash) {
            showFLNotSupportedDialog();
            return;
        }

        //        Attempt to resolve: java.lang.RuntimeException: Camera is being used after Camera.release() was called
        try {
            getCamera();
        } catch (Exception e) {
            e.printStackTrace();
            showFLNotSupportedDialog();
            return;
        }

        logError("Is flash ON? " + isFlashOn);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){

            if (isNotCameraPermissionGranted(mActivity)) {

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }

        // displaying button image
        toggleButtonImage();

        /*
         * Switch click event to toggle flash on/off
         */
        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                faLogEvents.logButtonClickEvent("switch_btn");

                if (isFlashOn) {
                    if (t != null) {
                        logError("Turn flash off and async task too at init");
                        turnOffFlashAsync();
                    }
                } else {
                    logError("Request for Camera permission: " + ContextCompat.checkSelfPermission(mActivity,
                            Manifest.permission.CAMERA));

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                        // Here, thisActivity is the current activity
                        if (isNotCameraPermissionGranted(mActivity)) {

                            ActivityCompat.requestPermissions(mActivity,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);

                        } else {
                            if (t != null)
                                turnOffFlashAsync();
                            logError("Turn flash ON and async task too at init");
                            flashFrequency(selectedFlashRate);
                        }
                    } else {
                        if (t != null)
                            turnOffFlashAsync();
                        logError("Turn flash ON and async task too at init");
                        flashFrequency(selectedFlashRate);
                    }
                }
//                ********************* Load Interstitial Ad - START ****************************
                if (!session.isPremiumUser() && startShowingAds()) {
                    if (new Date().getTime() > session.getRewardAdDateTime().getTime()) {
                        if (System.currentTimeMillis() >= appLaunchTimeAndInterstitialAd +
                                (session.getInterstitialPromptTime() * 1000L)) {
                            logError( String.valueOf(session.getShowInterstitialAd()));
                            showInterstitialAd();
                        }
                    }
                }
//                ********************* Load Interstitial Ad - END ****************************
            }
        });

        if (hasFlash)
            flashFrequency(selectedFlashRate);

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        handleIntent(getIntent());
    }

    private void showFLNotSupportedDialog() {
        // device doesn't support flash
        // Show alert message and close the application
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.NormalDialogTheme);
        alert.setTitle(getResources().getString(R.string.error));
        alert.setMessage(getResources().getString(R.string.camera_not_supported));
        alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // closing the application
//                    finish();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        faLogEvents.logScreenViewEvent("FlashLightNotSupported", "FlashLightNotSupported");
    }

    private void showInterstitialAd() {
        Log.e("TAG", "showInterstitialAd");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            boolean isAdLoaded = Yodo1Mas.getInstance().isInterstitialAdLoaded();
            if(isAdLoaded  && session.getShowInterstitialAd()) {
                Yodo1Mas.getInstance().showInterstitialAd(MainActivity.this);
            } else {
                Log.e("TAG", "The interstitial wasn't loaded yet OR set to false");
            }
        } else {
            if (mInterstitialAd != null && session.getShowInterstitialAd()) {
                logError( String.valueOf(session.getInterstitialPromptTime()));
                mInterstitialAd.show(MainActivity.this);
            } else {
                Log.e("TAG", "The interstitial wasn't loaded yet OR set to false");
            }
        }
    }

    private boolean isNotCameraPermissionGranted(Activity mActivity){
        return ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void showLaunchPlayStoreConfirmationDialog() {
        LaunchPlayStoreConfirmationDialog launchPlayStoreConfirmationDialog =
                new LaunchPlayStoreConfirmationDialog(MainActivity.this);
        launchPlayStoreConfirmationDialog.show();

        Window window = launchPlayStoreConfirmationDialog.getWindow();

        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void setInterstitialAdCallback() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d("TAG", "The ad was dismissed.");
                appLaunchTimeAndInterstitialAd = System.currentTimeMillis();
                loadInterstitialAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when fullscreen content failed to show.
                Log.d("TAG", "The ad failed to show.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
                mInterstitialAd = null;
                Log.d("TAG", "The ad was shown.");
                fbLogAdEvents.logAdImpressionEvent("interstitial");
                faLogEvents.logAdDisplayEvent("admob_interstitial");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                fbLogAdEvents.logAdClickEvent("interstitial");
                faLogEvents.logAdClickEvent("admob_interstitial");
            }
        });
    }

    private void loadInterstitialAd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            loadInterstitialAdYodo1();
        } else {
            loadInterstitialAdAdMob();
        }
    }

    private void loadInterstitialAdAdMob() {
        Log.e("TAG", "loadInterstitialAdAdMob");
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,interstitialAdUnit, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        setInterstitialAdCallback();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void loadInterstitialAdYodo1() {
        Log.e("TAG Yodo", "loadInterstitialAdYodo1");
        Yodo1Mas.getInstance().setInterstitialListener(new Yodo1Mas.InterstitialListener() {

            @Override
            public void onAdOpened(@NonNull Yodo1MasAdEvent event) {
                Log.e("TAG Yodo", "onAdOpened");
                faLogEvents.logAdDisplayEvent("yodo1_interstitial");
            }

            @Override
            public void onAdError(@NonNull Yodo1MasAdEvent event, @NonNull Yodo1MasError error) {
                Log.e("TAG Yodo", "onAdError");
            }

            @Override
            public void onAdClosed(@NonNull Yodo1MasAdEvent event) {
                Log.e("TAG Yodo", "onAdClosed");
                appLaunchTimeAndInterstitialAd = System.currentTimeMillis();
                loadInterstitialAd();
            }
        });
    }

    private void loadRewardedAd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            loadRewardedAdYodo1();
        } else {
            loadRewardedAdAdMob();
        }
    }

    private void loadRewardedAdYodo1() {
        Yodo1Mas.getInstance().setRewardListener(new Yodo1Mas.RewardListener() {
            @Override
            public void onAdOpened(@NonNull Yodo1MasAdEvent event) {
                Log.e("TAG Yodo", "onAdOpened");
                faLogEvents.logAdDisplayEvent("yodo1_rewarded_ad");
            }

            @Override
            public void onAdvertRewardEarned(@NonNull Yodo1MasAdEvent event) {
                Log.e("TAG Yodo", "onAdvertRewardEarned");
                userEarnedReward();
                faLogEvents.logRewardedAdEvent("yodo1_reward_earned");
            }

            @Override
            public void onAdError(@NonNull Yodo1MasAdEvent event, @NonNull Yodo1MasError error) {
                Log.e("TAG Yodo", "onAdError");

            }

            @Override
            public void onAdClosed(@NonNull Yodo1MasAdEvent event) {
                Log.e("TAG Yodo", "onAdClosed");

            }
        });
    }

    private void loadRewardedAdAdMob() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    rewardedVideoAd,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            rewardedAd = null;
                            MainActivity.this.isLoading = false;
//                            Toast.makeText(MainActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAd = rewardedAd;
                            logError("rewardedAd: "+rewardedAd);
                            logError("loadRewardedAd: onAdLoaded");
                            MainActivity.this.isLoading = false;
//                            Toast.makeText(MainActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadRewardedAdWithProgressDialogYodo1() {

        final ProgressDialog dialog = new ProgressDialog(this);;
        dialog.setMessage(getResources().getString(R.string.progress_dialog_message));
        dialog.show();

        new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.e("TAG Yodo1", "seconds remaining: " + millisUntilFinished / 1000);
                if (Yodo1Mas.getInstance().isRewardedAdLoaded()) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    showRewardedAdYodo1();
                    cancel();
                }
            }

            public void onFinish() {
                if (!Yodo1Mas.getInstance().isRewardedAdLoaded()) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    showRewardedAdError();
                }
            }
        }.start();
    }

    private void showRewardedAdYodo1() {
        if (Yodo1Mas.getInstance().isRewardedAdLoaded())
            Yodo1Mas.getInstance().showRewardedAd(MainActivity.this);
    }

    private void loadRewardedAdWithProgressDialogAdMob() {
            isLoading = true;

            final ProgressDialog dialog = new ProgressDialog(this);;
            dialog.setMessage(getResources().getString(R.string.progress_dialog_message));
            dialog.show();

            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    rewardedVideoAd,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            logError("loadRewardedAd: onAdFailedToLoad");
                            rewardedAd = null;
                            MainActivity.this.isLoading = false;
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showRewardedAdError();
//                            Toast.makeText(MainActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAd = rewardedAd;
                            logError("loadRewardedAd: onAdLoaded");
                            MainActivity.this.isLoading = false;
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            setRewardedAdCallbackAdMob();
                            showRewardedAdAdMob();
//                            Toast.makeText(MainActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void initMobileAds() {
        Log.e("TAG MA", "initMobileAds");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Yodo1Mas.getInstance().init(this, "EmOmP8cZFQ", new Yodo1Mas.InitListener() {
                @Override
                public void onMasInitSuccessful() {
                    Log.e("MainActivity","onMasInitSuccessful");
                }

                @Override
                public void onMasInitFailed(@NonNull Yodo1MasError error) {
                    Log.e("MainActivity","onMasInitFailed: "+ error);
                }
            });
        } else {
            MobileAds.initialize(
                    this,
                    new OnInitializationCompleteListener() {
                        @Override
                        public void onInitializationComplete(InitializationStatus initializationStatus) {

                        }
                    });
        }

    }

    private void setFirebaseRemoteConfigs() {
        RemoteConfigs.INSTANCE.setDefaultRemoteConfigs(this);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    public void onBackPressed() {
        faLogEvents.logButtonClickEvent("app_back_exit");

        nativeAdManager.showNativeAdExitDialog();
    }

    private void animateProgression(int progressStart, int progressEnd, final int flashFreq) {
        int progressDiff = Math.abs(progressEnd - progressStart);
        logError("Progress Diff: " + progressDiff);
        final ObjectAnimator animation = ObjectAnimator.ofInt(strobeSeekBar, "progress", progressStart, progressEnd);

        if (progressDiff <= 20) {
            animation.setDuration(150);
        } else {
            animation.setDuration(200);
        }

        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (flashFreq == -1){
                    isFlashSOSOn = false;
                    switchFlashSOS();
                } else {
                    flashFrequency(flashFreq);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animation.start();
        strobeSeekBar.clearAnimation();
    }

    private void switchFlashSOS() {
        if (!isFlashSOSOn){
            if (isFlashOn)
                t.cancel();
            morseArray = new ArrayList<Integer>(Arrays.asList(0,0,0,1,1,1,0,0,0));
            flashCount = 0;
            turnOffFlash();
            turnOnFlashSOS();
            isFlashOn = true;
            isFlashSOSOn = true;
            toggleButtonImage();
        } else{
            logError("To: turnOffFlashSOS");
            turnOffFlashSOS();
        }
    }

    private void loadAdaptiveBanner() {
        logError("loadAdaptiveBanner");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            loadAdaptiveBannerYodo1();
        } else {
            loadAdaptiveBannerAdMob();
        }
    }

    private void loadAdaptiveBannerAdMob() {
        logError("loadAdaptiveBannerAdMob");
        // Create an ad request.
        adaptiveAdView = new AdView(this);
        adaptiveAdView.setAdUnitId(adaptiveBannerAdUnit);

        adaptiveAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adaptiveAdView.setVisibility(View.VISIBLE);
                fbLogAdEvents.logAdImpressionEvent("banner");
                faLogEvents.logAdDisplayEvent("admob_banner");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                fbLogAdEvents.logAdClickEvent("banner");
                faLogEvents.logAdClickEvent("admob_banner");
            }
        });

        adContainerView.removeAllViews();
        adContainerView.addView(adaptiveAdView);

        AdSize adSize = getAdSize();
        adaptiveAdView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adaptiveAdView.loadAd(adRequest);
    }

    private void loadAdaptiveBannerYodo1() {
        /*Yodo1MasBannerAdView bannerAdView = new Yodo1MasBannerAdView(this);
        bannerAdView.setAdSize(Yodo1MasBannerAdSize.AdaptiveBanner);
        adContainerView.removeAllViews();

        // Hoping that below will resolve crash:- java.lang.ClassCastException (Didn't resolve)
        //android.widget.FrameLayout$LayoutParams cannot be cast to android.widget.RelativeLayout$LayoutParams
        ViewGroup.LayoutParams yodoBannerAdLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        adContainerView.addView(bannerAdView, yodoBannerAdLayoutParams);*/

        Yodo1MasBannerAdView bannerAdView = findViewById(R.id.yodo1_mas_banner);
        bannerAdView.setVisibility(View.VISIBLE);
        bannerAdView.loadAd();
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        logError("adWidth: " + adWidth);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void getSizes() {
        final int[] grooveH = new int[1];
        final int strobeBgH = 0;
        final int[] sliderH = new int[1];
        final int[] sliderBgH = new int[1];

        ViewTreeObserver vto = ivFlashlightGroove.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {

                ivFlashlightGroove.getViewTreeObserver().removeOnPreDrawListener(this);
                grooveH[0] = ivFlashlightGroove.getMeasuredHeight();
                int finalWidth = ivFlashlightGroove.getMeasuredWidth();
                logError("Height fg: " + grooveH[0] + " Width: " + finalWidth);

                int strobeBgH = ivFlashlightStrobe.getMeasuredHeight();
                int width = ivFlashlightStrobe.getMeasuredWidth();
                logError("Height strobe: " + strobeBgH + " Width: " + width);

                sliderBgH[0] = ivSliderBg.getMeasuredHeight();
                width = ivSliderBg.getMeasuredWidth();
                logError("Height slider: " + sliderBgH[0] + " Width: " + width);

                sliderH[0] = strobeSeekBar.getMeasuredHeight();
                finalWidth = strobeSeekBar.getMeasuredWidth();
                logError("Height sb: " + sliderH[0] + " Width: " + finalWidth);

                int sliderMT = (grooveH[0] + strobeBgH) - (sliderH[0]/2) + (sliderBgH[0]/2);
                logError( "sliderMT: "+sliderMT);

                setSizes();
                return true;
            }
        });

        /*ivFlashlightGroove.post(new Runnable() {
            @Override
            public void run() {
                // Below gives original dimensions of the Image and NOT the ImageView.
                int strobeBgH = ivFlashlightGroove.getDrawable().getIntrinsicHeight();
                int width = ivFlashlightGroove.getDrawable().getIntrinsicWidth();
                logError("Height groove2: " + strobeBgH + " Width: " + width);

                // Below gives actual height of the ImageView
                int strobeBgH2 = ivFlashlightGroove.getHeight();
                int width2 = ivFlashlightGroove.getWidth();
                logError("Height groove3: " + strobeBgH2 + " Width: " + width2);
            }
        });
        ivFlashlightStrobe.post(new Runnable() {
            @Override
            public void run() {
                // Below gives original dimensions of the Image and NOT the ImageView.
                int strobeBgH = ivFlashlightStrobe.getDrawable().getIntrinsicHeight();
                int width = ivFlashlightStrobe.getDrawable().getIntrinsicWidth();
                logError("Height strobe2: " + strobeBgH + " Width: " + width);

                // Below gives actual height of the ImageView
                int strobeBgH2 = ivFlashlightStrobe.getHeight();
                int width2 = ivFlashlightStrobe.getWidth();
                logError("Height strobe3: " + strobeBgH2 + " Width: " + width2);
            }
        });*/
    }

    private void setSizes() {
        RelativeLayout time_rl, rlFlBg;
        float tvDirectionMT, tvDirectionH, ivCompassArrowH, ivCompassArrowMT, ivCompassArrowMB, ivCompassBgH,
                ivScreenLightMT, ivScreenLightMB, ivScreenLightH, btnPowerH, btnPowerMB, sosBtnH;

        float totalBtnsH, hDiff;

        float ivCompassDsc, ivCompassArrowW;

        float seekbarPLR;

        rlFlBg = findViewById(R.id.flashlight_background);
        time_rl = findViewById(R.id.time_rl);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int pixel_width =  metrics.widthPixels;
        int pixel_height = metrics.heightPixels;
        float screen_density =getResources().getDisplayMetrics().density; // density factor: 0.75, 1, 1.5, 2.75
        logError("width px:"+pixel_width+" height px:"+pixel_height+" screen density:"+screen_density);
        // Convert Pixels to DP (dp = pixels/screen_density)
        int dp_width = (int) (pixel_width/screen_density);
        int dp_height = (int) (pixel_height/screen_density);
        logError("width dp:"+dp_width+" height dp:"+dp_height);

        float widthDpi = metrics.xdpi;
        float heightDpi = metrics.ydpi;
        float widthInches = pixel_width / widthDpi;
        float heightInches = pixel_height / heightDpi;
        //a² + b² = c²
        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        double diagonalInches = Math.sqrt(
                (widthInches * widthInches)
                        + (heightInches * heightInches));
        logError("widthInches: "+widthInches + " heightInches: "+heightInches);
        if (diagonalInches >= 10) {
            logError("Device is a 10in tablet: "+diagonalInches);
        }
        else if (diagonalInches >= 7) {
            logError("Device is a 7in tablet: "+diagonalInches);
        } else {
            logError("Device is a normal phone: "+diagonalInches);
        }

        int flBgH = rlFlBg.getMeasuredHeight();
        int flBgW = rlFlBg.getMeasuredWidth();
        logError("Height flBg: " + flBgH + " Width: " + flBgW);

        sosBtnH = (float) (pixel_width/6.25);
        ivCompassDsc = (float) (pixel_width/5.7);
        seekbarThumbHW = (float) (sosBtnH/1.45);
        ivCompassArrowW = (float) (sosBtnH/5.66);
        seekbarPLR = (float) ((float) (pixel_width/11) * 0.5);

        // All height deciding metrics
        tvDirectionMT = (float) (pixel_width/16.5);
        tvDirectionH = (float) (pixel_width/14.0);
        ivCompassArrowH = (float) (sosBtnH/6.63);
        ivCompassArrowMT = (float) (pixel_width/100.0);
        ivCompassArrowMB = (float) (pixel_width/100.0);
        ivCompassBgH = (float) (pixel_width/4.7);
        ivScreenLightMT = (float) (pixel_width/20.6);
        ivScreenLightMB = (float) (pixel_width/20.6);
        ivScreenLightH = (float) (pixel_width/8.5);
        btnPowerH = (float) (pixel_width/2.0);
        btnPowerMB = (float) (pixel_width/20.6);
        sosBtnH = (float) (pixel_width/6.25);

        totalBtnsH = tvDirectionMT + tvDirectionH + ivCompassArrowH + ivCompassArrowMT + ivCompassArrowMB +
                ivCompassBgH + ivScreenLightMT + ivScreenLightMB + ivScreenLightH + btnPowerH + btnPowerMB + sosBtnH;

        logError("Available height: " + flBgH + " Estimated Height use: " + totalBtnsH);

        logError("btnPowerH: "+btnPowerH);

        if(totalBtnsH > flBgH){
            hDiff = totalBtnsH - flBgH;
            logError("Height difference: " + hDiff);
            logError("hDiff/totalBtnsH: " + hDiff/totalBtnsH);

            tvDirectionMT = tvDirectionMT - ((tvDirectionMT * hDiff)/totalBtnsH);
            tvDirectionH = tvDirectionH - ((tvDirectionH * hDiff)/totalBtnsH);
            ivCompassArrowH = ivCompassArrowH - ((ivCompassArrowH * hDiff)/totalBtnsH);
            ivCompassArrowMT = ivCompassArrowMT - ((ivCompassArrowMT * hDiff)/totalBtnsH);
            ivCompassArrowMB = ivCompassArrowMB - ((ivCompassArrowMB * hDiff)/totalBtnsH);
            ivCompassBgH = ivCompassBgH - ((ivCompassBgH * hDiff)/totalBtnsH);
            ivScreenLightMT = ivScreenLightMT - ((ivScreenLightMT * hDiff)/totalBtnsH);
            ivScreenLightMB = ivScreenLightMB - ((ivScreenLightMB * hDiff)/totalBtnsH);
            ivScreenLightH = ivScreenLightH - ((ivScreenLightH * hDiff)/totalBtnsH);
            btnPowerH = btnPowerH - ((btnPowerH * hDiff)/totalBtnsH);
            btnPowerMB = btnPowerMB - ((btnPowerMB * hDiff)/totalBtnsH);
            sosBtnH = sosBtnH - ((sosBtnH * hDiff)/totalBtnsH);

            ivCompassArrowW = (float) (sosBtnH/5.66);

            ivCompassDsc = (int) (ivCompassBgH * (4.7/5.7));
        }

        logError("btnPowerH: "+btnPowerH);

        setIVLPWH(btnSwitch, btnPowerH);
        setIVLPWH(btnSos, sosBtnH);
        setIVLPWH(btnRemAd, sosBtnH);
        setIVLPWH(btnScreenLight, ivScreenLightH);
        setIVLPWH(btnSettings, (float) ((float) ivScreenLightH/1.7));
        setIVLPML(btnSettings, (float) ((float) ivScreenLightH/2.5));
        setIVLPWH(ivCompassBg, ivCompassBgH);
        setIVLPWH(ivCompassDisc, ivCompassDsc);
        setIVLPWH(ivCompassScreen, ivCompassDsc);
        setIVLPWH(ivCompassArrow, ivCompassArrowW, ivCompassArrowH);
        setTVLPWH(tvDirection, 3 * tvDirectionH, tvDirectionH); // TODO change the width accordingly
        setDCLPWH(digitalClock, pixel_width*3/8, pixel_width/8); // Don't change
        setRLLPW(time_rl, (int) (pixel_width/1.3)); // Don't change

        setTVLPMT(tvDirection, tvDirectionMT);
        setIVLPMTMB(ivCompassArrow, ivCompassArrowMT, ivCompassArrowMB);
        setIVLPMTMB(btnScreenLight, ivScreenLightMT, ivScreenLightMB);
        setIVLPMTMB(btnSwitch, 0, btnPowerMB);
        setSBPLR(strobeSeekBar, seekbarPLR);

        newThumbBtnOn = resizeThumbImage(getResources().getDrawable(R.drawable.slider_btn_on), seekbarThumbHW);
        newThumbBtnOff = resizeThumbImage(getResources().getDrawable(R.drawable.slider_btn_off), seekbarThumbHW);

        logError("fromScreenLightActivity: "+ fromOnPause);
        if (!fromOnPause)
            strobeSeekBar.setThumb(newThumbBtnOn); // Required to set the margins properly at first launch

        int grooveH = ivFlashlightGroove.getMeasuredHeight();
        int finalWidth = ivFlashlightGroove.getMeasuredWidth();
        logError("Height fg: " + grooveH + " Width: " + finalWidth);

        int strobeBgH = ivFlashlightStrobe.getMeasuredHeight();
        int width = ivFlashlightStrobe.getMeasuredWidth();
        logError("Height strobe: " + strobeBgH + " Width: " + width);

        int sliderBgH = ivSliderBg.getMeasuredHeight();
        width = ivSliderBg.getMeasuredWidth();
        logError("Height slider: " + sliderBgH + " Width: " + width);

        int sliderH = newThumbBtnOn.getIntrinsicHeight();
        finalWidth = strobeSeekBar.getMeasuredWidth();
        logError("Height sb: " + sliderH + " Width: " + finalWidth);

        int sliderMT = (grooveH + strobeBgH) - (sliderH/2) + (sliderBgH/2);
        logError( "sliderMT: "+sliderMT);

        setFLLPMT(flSlider, sliderMT);
    }

    private Drawable resizeThumbImage(Drawable slider_btn, float seekbarThumbHW) {
        Bitmap bmpOrg = ((BitmapDrawable) slider_btn).getBitmap();
        Bitmap bmpScaled = Bitmap.createScaledBitmap(bmpOrg, Math.round(seekbarThumbHW), Math.round(seekbarThumbHW), true);
        Drawable newThumb = new BitmapDrawable(getResources(), bmpScaled);
        newThumb.setBounds(0, 0, newThumb.getIntrinsicWidth(), newThumb.getIntrinsicHeight());
        return newThumb;
    }

    private void setSBPLR(SeekBar view, float seekbarPLR) {
        int lr = Math.round(seekbarPLR);
        view.setPadding(lr, 0, lr, 0); // set padding as margin clips the thumb.
        logError("SB Height:"+view.getHeight()+" SB Width:"+view.getWidth());
    }

    private void setIVLPWH(ImageView view, float wh) {
        wh = Math.round(wh);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.width = (int) wh; // Specify in pixels
        view_lp.height = (int) wh; // Specify in pixels
        view.setLayoutParams(view_lp);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setIVLPWH(ImageView view, float w, float h) {
        w = Math.round(w);
        h = Math.round(h);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.width = (int) w; // Specify in pixels
        view_lp.height = (int) h; // Specify in pixels
        view.setLayoutParams(view_lp);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }

    private void setIVLPH(ImageView view, float h) {
        h = Math.round(h);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.height = (int) h; // Specify in pixels
        view.setLayoutParams(view_lp);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }

    private void setRLLPW(RelativeLayout view, float width) {
        width = Math.round(width);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.width = (int) width; // Specify in pixels
        view.setLayoutParams(view_lp);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setTVLPWH(TextView view, float width, float height) {
        width = Math.round(width);
        height = Math.round(height);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.width = (int) width; // Specify in pixels
        view_lp.height = (int) height; // Specify in pixels
        view.setLayoutParams(view_lp);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setDCLPWH(DigitalClock view, float width, float height) {
        width = Math.round(width);
        height = Math.round(height);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.width = (int) width; // Specify in pixels
        view_lp.height = (int) height; // Specify in pixels
        view.setLayoutParams(view_lp);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setTVLPMT(TextView view, float top) {
        top = Math.round(top);
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginParams.setMargins(0, (int) top, 0, 0);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setFLLPMT(FrameLayout view, float top) {
        top = Math.round(top);
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginParams.setMargins(0, (int) top, 0, 0);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setIVLPMTMB(ImageView view, float top, float bottom) {
        top = Math.round(top);
        bottom = Math.round(bottom);
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginParams.setMargins(0, (int) top, 0, (int) bottom);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }
    private void setIVLPML(ImageView view, float left) {
        left = Math.round(left);
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginParams.setMargins((int) left, 0, 0, 0);
        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
    }

    private void getAdsSettings(final MainActivity activity) {
        if(activity == null || activity.isFinishing())
            return;

        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        String url = "https://ascetx.com/AndroidApps/FlashLight/ads.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    logError( String.valueOf(response));
                    try {
                        if (!response.getBoolean("interstitial_ad_show")){
                            if(activity == null || activity.isFinishing())
                                return;
                            if (session == null)
                                return;
                            session.setShowInterstitialAd(false);
                        }
                        if(activity == null || activity.isFinishing())
                            return;
                        if (session == null)
                            return;
                        session.setInterstitialPromptTime(response.getInt("interstitial_ad_timing"));

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        ******************* Start: Get Ad remove dimensions **************************

//        ******************* End: Get Ad remove dimensions **************************
    }

    public Boolean startShowingAds(){

        int gapDayLaunch = RemoteConfigs.INSTANCE.dayLaunchGapToStartShowingAds();
        gapDayLaunch = 0;
        logError("gapDayLaunch: "+gapDayLaunch);
        final SharedPreferences prefs = getSharedPreferences(APP_RATER_SHARED_PREF, 0);
        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        long launch_count = prefs.getLong("launch_count", 0);
        long currentDateTime = System.currentTimeMillis();
        return (currentDateTime >= date_firstLaunch +
                    ((long) gapDayLaunch * 24 * 60 * 60 * 1000))
                &&
             launch_count > gapDayLaunch;
    }

    private void setFlashlight(String newMode) {
        try {
            this.params = this.camera.getParameters();
            if(this.params.getFlashMode() != newMode) {
                this.params.setFlashMode(newMode);
                this.camera.setParameters(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            logError( "handleIntent: "+stockId);
//            https://ascetx.com/flashlight
//            Uri appData = Uri.parse("content:/ascetx.com/stockstalker/").buildUpon()
//                    .appendPath(stockId).build();
        }
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        logError( "onNewIntent is called!");
        logError( intent.getStringExtra("KEY"));
        logError(String.valueOf(isFlashOn));
        logError(String.valueOf(isOn));
        if (intent.getStringExtra("KEY").equals("Toggle")){
            if (!isOn) {
                logError( "turnOnFlash");
                turnOnFlash();
            }
            else {
                logError( "turnOffFlash");
                turnOffFlash();
            }
        }
    }*/

    public void showFreePremiumPopUp() {
        final MainActivity activity = this;
        if (activity == null || activity.isFinishing())
            return;

        logError("rewardedAd: "+rewardedAd);
        if (rewardedAd == null && !isLoading) {
            loadRewardedAd();
        }

        faLogEvents.logScreenViewEvent("Watch Rewarded Ad Dialog", "free_premium_dialog");

        AlertDialog dialog;
        AlertDialog.Builder builder;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
//                } else {
//                    builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
//                }
//        builder = new android.app.AlertDialog.Builder(MainActivity.this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder = new AlertDialog.Builder(MainActivity.this, R.style.FreePremiumDialogTheme);
        // Add the buttons
        builder.setPositiveButton(R.string.rewarded_vid_btn_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                faLogEvents.logButtonClickEvent("watch");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    loadRewardedAdWithProgressDialogYodo1();
                } else {
                    if (rewardedAd == null) {
                        loadRewardedAdWithProgressDialogAdMob();
                    } else {
                        setRewardedAdCallbackAdMob();
                        showRewardedAdAdMob();
                    }
                }
            }
        });
        builder.setTitle(getResources().getString(R.string.rewarded_vid_title));
        builder.setMessage(getResources().getString(R.string.rewarded_vid_msg));
                /*builder.setNegativeButton(R.string.rewarded_vid_btn_negative, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });*/
        builder.setNeutralButton(R.string.rewarded_vid_btn_neutral, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                faLogEvents.logButtonClickEvent("premium");
                removeAd(getWindow().findViewById(android.R.id.content));
            }
        });
        // Create the AlertDialog
        dialog = builder.create();
        dialog.show();

        Button buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(activity, R.color.premium_alert_dialog_button_positive));
//        buttonPositive.setBackgroundColor(ContextCompat.getColor(activity, R.color.premium_alert_dialog_button_negative_background));
    }

    private void showRewardedAdAdMob() {
        if (rewardedAd != null) {
            Activity activityContext = MainActivity.this;
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    userEarnedReward();
                    faLogEvents.logRewardedAdEvent("admob_reward_earned");
                    adaptiveAdView.setVisibility(View.GONE);
                }
            });
        } else {
            Log.d(TAG, getResources().getString(R.string.rewarded_vid_err_msg));
            Toast.makeText(MainActivity.this, getResources().getString(R.string.rewarded_vid_err_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void userEarnedReward() {
        // Handle the reward.
        Log.e(TAG, "The user earned the reward.");
        fbLogAdEvents.logAdClickEvent("rewarded_video_completed");
        // The method is invoked when the user should be rewarded for interacting with the ad.
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date today = new Date();
        String strDate = dateFormat.format(today);
        logError( strDate);
        long ltime = today.getTime() + 1 * 24 * 60 * 60 * 1000;
        Date today3 = new Date(ltime);
        session.setRewardAdDateTime(today3);
        strDate = dateFormat.format(session.getRewardAdDateTime());
        logError( strDate);
        Toast.makeText(MainActivity.this, String.format(getResources().getString(R.string.rewarded_vid_ads_rmd_till), strDate), Toast.LENGTH_LONG).show();
    }

    private void setRewardedAdCallbackAdMob() {
//        Toast.makeText(activity, activity.getString(R.string.error), Toast.LENGTH_LONG).show();

        if (rewardedAd == null) // just for safety
            return;

         rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad was shown.");
                logError("Ad was shown.");
                fbLogAdEvents.logAdImpressionEvent("rewarded_video");
                faLogEvents.logAdDisplayEvent("admob_rewarded_video");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.d(TAG, "Ad failed to show.");
                logError("Ad failed to show.");
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                rewardedAd = null;
                showRewardedAdError();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad was dismissed.");
                logError("Ad was dismissed.");
                // Ad closed.
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String strDate = dateFormat.format(session.getRewardAdDateTime());
                Toast.makeText(MainActivity.this, String.format(getResources().getString(R.string.rewarded_vid_ads_rmd_till), strDate), Toast.LENGTH_LONG).show();
                rewardedAd = null;
                loadRewardedAd();
            }
        });
    }

    private void showRewardedAdError() {
        Toast.makeText(MainActivity.this, getResources().getString(R.string.rewarded_video_error_message), Toast.LENGTH_LONG).show();
    }

    // ************* init app from setting prefs ***************
    private void loadSettingPrefs() {
        sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        switchPref = sharedPref
                .getBoolean("notification", true);

        if (switchPref)
            startNotification();
        else
            closeNotification();
    }

    //    **************** Start Notification ********************
    private void startNotification(){
        logError("start notification");

//        Intent serviceIntent = new Intent(this, KillNotificationsService.class);
//        startService(serviceIntent);

        String ns = MainActivity.NOTIFICATION_SERVICE;
        notificationManager =
                (NotificationManager) getSystemService(ns);

        String channelId = "my_channel_id";
        CharSequence channelName = "Flash Light";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setSound(null,null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationView = new RemoteViews(getPackageName(),
                R.layout.main_notification);
        notificationView.setImageViewResource(R.id.closeOnFlash, R.drawable.flashlight_on);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.flashlight_on)
                    .setLargeIcon(bitmap)
                    .setContent(notificationView);
        } else {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(notificationView);
        }

        mBuilder.setContentTitle("Flash Light") // required`
                .setContentText("Toggle Flash Light") // required
                .setChannelId(channelId);

        // Create pending intent, mention the Activity which needs to be
        //triggered when user clicks on notification(MainActivity.class in this case)

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isOpenedFromNotification",true);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        } else {
            contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        mBuilder.setContentIntent(contentIntent);

        notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        sbl = new switchButtonListener(this);

        //this is the intent that is supposed to be called when the
        //button is clicked
        Intent switchIntent = new Intent();
        switchIntent.setAction("COM_FLASHLIGHT");

        IntentFilter intentFilter = new IntentFilter("COM_FLASHLIGHT");

        registerReceiver(sbl, intentFilter);

//        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
//                switchIntent, 0);

        PendingIntent pendingSwitchIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
                    switchIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
                    switchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        } else {
            pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
                    switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        notificationView.setOnClickPendingIntent(R.id.closeOnFlash,
                pendingSwitchIntent);

        notificationManager.notify(1, notification);
    }

    public void showRefreshedUi() {
        logError("showRefreshedUi");
        logError("adaptiveAdView: "+adaptiveAdView);
        if (adaptiveAdView != null)
            adaptiveAdView.setVisibility(View.GONE);
        else
            adaptiveAdView = null;
        mInterstitialAd = null;
    }

    public class switchButtonListener extends BroadcastReceiver {

        MainActivity main;

        public switchButtonListener(MainActivity mainActivity) {
            this.main = mainActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(isFlashOn) {
                main.notificationView.setImageViewResource(R.id.closeOnFlash, R.drawable.flashlight_off);
            } else {
                main.notificationView.setImageViewResource(R.id.closeOnFlash, R.drawable.flashlight_on);
            }
            main.mBuilder.setContent(notificationView);
            main.notification.contentView = notificationView;
            main.notificationManager.notify(1, notification);

            if (isFlashOn) {

                if (t != null) {
                    logError("Turn flash off and async task too at init");
                    turnOffFlashAsync();
                }
            } else {
                logError("Request for Camera permission: "+ContextCompat.checkSelfPermission(main,
                        Manifest.permission.CAMERA));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    // Here, thisActivity is the current activity
                    if (isNotCameraPermissionGranted(main)) {

                        ActivityCompat.requestPermissions(main,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);

                    } else {
                        if(t != null)
                            turnOffFlashAsync();
                        logError("Turn flash ON and async task too at init");
                        flashFrequency(selectedFlashRate);
                    }
                } else {
                    if(t != null)
                        turnOffFlashAsync();
                    logError("Turn flash ON and async task too at init");
                    flashFrequency(selectedFlashRate);
                }
            }
        }

    }

    private void closeNotification(){
        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(sbl);

        } catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error: unregisterReceiver(sbl)");
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
            nManager.cancelAll();
        }
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }
//    **************** End Notification ********************

    public void removeAd(View view) {
        btnRemAd.setImageResource(R.drawable.noad_btn_on);
        faLogEvents.logButtonClickEvent("noad_btn");
        initPurchase();
    }

    public void initPurchase(){
        if (mAcquireDialog == null) {
            mAcquireDialog = new AcquireDialog();
        }

        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode()
                > BILLING_MANAGER_NOT_INITIALIZED) {
            logError("onManagerReady: " + mBillingManager.getBillingClientResponseCode());
            mAcquireDialog.onManagerReady(this);
            mAcquireDialog.handleManagerAndUiReady(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        logError("requestCode after alert for CAMERA permission: " + requestCode);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getCamera();
                    if (t != null)
                        turnOffFlashAsync();
                    logError("Turn flash ON and async task too at init");
                    flashFrequency(selectedFlashRate);

                } else {
                    if (t != null)
                        turnOffFlashAsync();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    private void hideAd(){
//        mAdView.setVisibility(View.GONE);
//        mInterstitialAd = null;
//        showInterstitialAd.cancel();
//    }

/*********************** The naked async class for iap service ******************************************
    /**private class GetItemList extends AsyncTask<Integer, Integer, Long> {

        private String pName;

        GetItemList(String packagename){
            pName = packagename;
        }

        @Override
        protected Long doInBackground(Integer... params) {
//            Querying for items available for purchase
            ArrayList<String> skuList = new ArrayList<String> ();
            skuList.add("no_ads");
            skuList.add("android.test.canceled");
            skuList.add("android.test.refunded");
            skuList.add("android.test.item_unavailable");
            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
            Bundle skuDetails = null;
            try {
/*//*********************** Querying for items available for purchase ******************************************
                skuDetails = mService.getSkuDetails(3, pName, "inapp", querySkus);
                int response = skuDetails.getInt("RESPONSE_CODE");
                logError("Querying for items available for purchase, Response Code: " + response);
                if (response == 0) {
                    ArrayList<String> responseList
                            = skuDetails.getStringArrayList("DETAILS_LIST");
                    logError(String.valueOf(responseList));
                    for (String thisResponse : responseList) {
                        JSONObject object;
                        object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        String price = object.getString("price");
                        String mFirstIntermediate;
                        String mSecondIntermediate;
                        if (sku.equals("no_ads")) mFirstIntermediate = price;
//                        else if (sku.equals("i002")) mSecondIntermediate = price;
//                        pView.setText(sku + ": " + price);
                    }
                }
/*//*********************** Querying for purchased items ******************************************
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                response = ownedItems.getInt("RESPONSE_CODE");
                logError("ownedItems response: "+ response);
                if (response == BILLING_RESPONSE_RESULT_OK) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String>  purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String>  signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken =
                            ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                    for (int i = 0; i < purchaseDataList.size(); ++i) {
                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);

                        logError("ownedItems: " + sku +" "+purchaseData+" sign: "+signature);
                        // do something with this purchase information
                        // e.g. display the updated list of products owned by user
                    }

                    // if continuationToken != null, call getPurchases again
                    // and pass in the token to retrieve more items
                }
/*//********** Consuming a purchase, using purchaseToken which is got in product data. Used for consuming coins in games ******************************************

                response = mService.consumePurchase(3, getPackageName(), "inapp:info.ascetx.flashlight:no_ads");
                logError("consume purchase response: "+ response);

            } catch (NullPointerException ne)  {
                logDebug("Error Null Pointer: " + ne.getMessage());
                ne.printStackTrace();
            }
            catch (RemoteException e) {
                // TODO Auto-generated catch block
                logDebug("Error Remote: " + e.getMessage());
                e.printStackTrace();
            }
            catch (JSONException je) {
                // TODO Auto-generated catch block
                logDebug("Error JSON: " + je.getMessage());
                je.printStackTrace();
            }
            return null;
        }
    }*/


    private void turnOffFlashSOS(){
        logError("turnOffFlashSOS");
        logError("Cancel timer and flash light hopefully SOS");
        turnOffFlash();
        t.cancel();
        isFlashOn = false;
        isFlashSOSOn = false;
        toggleButtonImage();
    }

    private void turnOnFlashSOS(){
        logError("turnOnFlashSOS");
        final int temp = morseArray.get((int) Math.floor(flashCount/2));
        int period;
        t = new Timer();
        if (morseArray.get((int) Math.floor(flashCount/2)) == 0) {
            period = 300;
        }
        else {
            period = 600;
        }
        logError("FlashPeriod: "+ period);

        t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    logError("FlashCount: "+ flashCount);
                    try{
                        if (morseArray.get((int) Math.floor(flashCount/2)) == temp){
                            if (!isOn) {
                                turnOnFlash();
                                logError("Flash ON "+ flashCount);
                            }
                            else {
                                turnOffFlash();
                                logError("Flash OFF "+ flashCount);
                            }
                        }else {
                            t.cancel();
                            turnOnFlashSOS();
                            flashCount --;
                        }
                    }catch (Exception e){
                        logError("Run time exception: " + e.getMessage());
                        try{
//                            if (e.getMessage().contains("Invalid index")) {
//                                turnOffFlashSOS();
//                            If condition is commented as was getting different error message for api version 23 and 25
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                flashCount = -1;
                                t.cancel();
                                turnOnFlashSOS();
//                            }
                        }catch (Exception ex){
                            logError("Run time exception: " + ex.getMessage());
                        }
                    }
                    flashCount ++;
                }
            },0, period );
    }

    public void flashSOS (View view){

        faLogEvents.logButtonClickEvent("sos_btn");

        if (!isFlashSOSOn){
            if (isFlashOn)
                t.cancel();
            morseArray = new ArrayList<Integer>(Arrays.asList(0,0,0,1,1,1,0,0,0));
            flashCount = 0;
            turnOffFlash();
            turnOnFlashSOS();
            isFlashOn = true;
            isFlashSOSOn = true;
            toggleButtonImage();
            animateProgression(strobeSeekBar.getProgress(), 0, -1);
        } else{
            logError("To: turnOffFlashSOS");
            turnOffFlashSOS();
        }
    }

        private void turnOffFlashAsync(){
        logError("turnOffFlashAsync");
        logError("Cancel timer and flash light hopefully");
            try {
                turnOffFlash();
            } catch (Exception e) {
                e.printStackTrace();
            }
            t.cancel();
        isFlashOn = false;
        isFlashSOSOn = false;
        toggleButtonImage();
    }

    private void turnOnFlashAsync(int f){
        logError("turnOnFlashAsync");
        logError("FlashRate: "+ f);
        t = new Timer();
        selectedFlashRate = f;
        if (f > 0){
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 2 seconds
                    if (!isOn)
                        turnOnFlash();
                    else
                        turnOffFlash();
                }
            },0, 1500/f );
        }
        else{
            turnOnFlash();
        }
        isFlashOn = true;
        isFlashSOSOn = false;
        toggleButtonImage();
    }

    private void turnOnFlash() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                strobeSeekBar.setThumb(resizeThumbImage(getResources().getDrawable(R.drawable.slider_btn_on), seekbarThumbHW));
            }
        });
        if (!isOn) {
            isOn = true;
            // play sound
//            playSound();
            logError("turning on flash");
            // TODO getting run time exception as camera.getParameters() is being called when the camera is still in preview/not closed
            synchronized (lock) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mCameraId == null || mCameraManager == null) {
                        return;
                    }
                    try {
                        if(!cameraStatus) {
                            mCameraManager.setTorchMode(mCameraId, true);
                            cameraStatus = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (camera == null || params == null) {
                        return;
                    }
                    params = camera.getParameters();
                    params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
//                try {
//                    camera.setPreviewTexture(new SurfaceTexture(0));
//                } catch (IOException e) {
//                    logError("Flash On: setPreviewTexture error");
//                    e.printStackTrace();
//                }
                    camera.startPreview();
                }
            }
//
//                // changing button/switch image
//                toggleButtonImage();
        }

    }

    private void turnOffFlash() {
        logError("isOn: "+isOn+" camera: "+camera+" params: "+params);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                strobeSeekBar.setThumb(resizeThumbImage(getResources().getDrawable(R.drawable.slider_btn_off), seekbarThumbHW));
            }
        });
        if (isOn) {
            isOn = false;
            // play sound
//            playSound();
            logError("turning off flash");
            synchronized (lock) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mCameraId == null || mCameraManager == null) {
                        return;
                    }
                    try {
                        if(cameraStatus) {
                            mCameraManager.setTorchMode(mCameraId, false);
                            cameraStatus = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (camera == null || params == null) {
                        return;
                    }
                    params = camera.getParameters();
                    params.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
//                try {
//                    camera.setPreviewTexture(new SurfaceTexture(0));
//                } catch (IOException e) {
//                    logError("Flash On: setPreviewTexture error");
//                    e.printStackTrace();
//                }
                    camera.stopPreview();
                }
            }
//
//                // changing button/switch image
//                toggleButtonImage();
        }
    }

    public void flashFrequency(int freq){

//        flashFreq = (Button) view;
//        flashFreq.setTextColor(getResources().getColor(R.color.colorFlashFreqButOn));
//        flashFreq.setBackgroundDrawable(getResources().getDrawable(R.drawable.flon));
//        logError("Toggle flash button's background");
//        for (Button frq : freq)
//            if (frq != flashFreq){
//                frq.setTextColor(getResources().getColor(R.color.colorWhite));
//                frq.setBackgroundDrawable(getResources().getDrawable(R.drawable.floff));
//            }
//
//

        if(t != null)
            turnOffFlashAsync();
//
//        logError("Array index " + String.valueOf(Arrays.asList(freq).indexOf(flashFreq)));
//
        if (strobeSeekBar.getProgress() == 0){
            switchFlashSOS();
        } else {
            turnOnFlashAsync(freq);
        }
    }

    // getting camera parameters
    private void getCamera() {
        if (hasFlash) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mCameraManager == null) {
                    mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        mCameraId = mCameraManager.getCameraIdList()[0];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (camera == null) {
                    try {
                        synchronized (lock) {
                            camera = Camera.open();
                            params = camera.getParameters();
                        }
                    } catch (Exception e) {
                        if (e.getMessage() != null)
                            logError(e.getMessage());
                    }
                }
            }
        }
    }

    private void toggleButtonImage(){
        logError( "toggleButtonImage: "+isFlashOn);
        if(isFlashOn) {
            btnSwitch.setImageResource(R.drawable.power_btn_on);
            logError("set Seekbar thumb ON");
            strobeSeekBar.setThumb(getResources().getDrawable(R.drawable.slider_btn_on));
        } else {
            logError("set Seekbar thumb OFF");
            strobeSeekBar.setThumb(getResources().getDrawable(R.drawable.slider_btn_off));
            btnSwitch.setImageResource(R.drawable.power_btn_off);
        }

        if(isFlashSOSOn) {
            strobeSeekBar.setThumb(getResources().getDrawable(R.drawable.slider_btn_on));
            btnSos.setImageResource(R.drawable.sos_btn_on);
        } else {
            strobeSeekBar.setThumb(getResources().getDrawable(R.drawable.slider_btn_off));
            btnSos.setImageResource(R.drawable.sos_btn_off);
        }


        if (switchPref)
            toggleNotificationButton(isFlashOn || isFlashSOSOn);

        if (isFlashOn) {
            turnOnFlash();
            logError("Flash ON "+ flashCount);
        }
        else {
            turnOffFlash();
            logError("Flash OFF "+ flashCount);
        }
    }

    private void toggleNotificationButton(boolean isAnyFlashOn){
        try {
            if (isAnyFlashOn)
                notificationView.setImageViewResource(R.id.closeOnFlash, R.drawable.flashlight_on);
            else
                notificationView.setImageViewResource(R.id.closeOnFlash, R.drawable.flashlight_off);

            mBuilder.setContent(notificationView);
            notification.contentView = notificationView;
            notificationManager.notify(1, notification);
        } catch (NullPointerException e){
            logError( e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        if (mBillingManager != null) {
            mBillingManager.destroy();
        }
        if (adaptiveAdView != null) {
            adaptiveAdView.destroy();
        }

        nativeAdManager.destroyNativeAd();
        instance = null;

        try {
            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(sbl);

        } catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error: unregisterReceiver(sbl)");
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
            nManager.cancelAll();
        }
        mSensorManager.unregisterListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if(cameraStatus) {
                    mCameraManager.setTorchMode(mCameraId, false);
                    cameraStatus = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // on stop release the camera
            if (camera != null) {
                synchronized (lock) {
                    camera.release();
                    camera = null;
                }
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adaptiveAdView != null) {
            adaptiveAdView.pause();
        }
        super.onPause();
// todo have to check what is causing activity to come in OnPause():
//        Interstitial ad
//        Google Play purchase
//        Back button
//        Home Button
        logError("On Pause");

        fromOnPause = true; // Using this to set the seekbar thumb accordingly

        isOnPause = true;
        logError("pauseBilling: "+pauseBilling+" pauseIntAd: "+pauseIntAd);
        if (!pauseBilling && !pauseIntAd){
            // on pause turn off the flash
            try {
//                turnOffFlashAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // to stop the listener and save battery
//        TODO Moving below to onDestroy as in pause its causing the compass to stop. Might have to enable to save battery
//        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        logError("On Resume");

        loadSettingPrefs();
        nativeAdManager = new NativeAdManager(this);
        nativeAdManager.loadNativeAd();

        InAppUpdater inAppUpdater = InAppUpdater.getInstance(this);
        inAppUpdater.onResumeCalled();

        faLogEvents.logScreenViewEvent("Main Activity", "MainActivity");

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        getSizes();

        if (adaptiveAdView != null) {
            adaptiveAdView.resume();
        }

        btnScreenLight.setImageResource(R.drawable.screen_light_btn_off);
        btnSettings.setImageResource(R.drawable.settings_btn_off);

        isOnPause = false;
        pauseBilling = false;
        pauseIntAd = false;

        // Note: We query purchases in onResume() to handle purchases completed while the activity
        // is inactive. For example, this can happen if the activity is destroyed during the
        // purchase flow. This ensures that when the activity is resumed it reflects the user's
        // current purchases.
        if (mBillingManager != null
                && mBillingManager.getBillingClientResponseCode() == BillingClient.BillingResponseCode.OK) {
            mBillingManager.queryPurchases();
        }

        // Commented below code as there is no orientation change so no need account for orientation change.
        // for the system's orientation sensor registered listeners
        /*if (mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME)) {
            tvCompassError.setVisibility(View.GONE);
        } else {
//            tvDirection.setVisibility(View.GONE);
            tvDirection.setText("");
            logError("on resume tvDirection.setText");
        }
        logError("OnResume:mSensorManager.registerListener: "+mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME));*/

        // on resume turn on the flash
//        Commented below two lines as when the interstitial Ad is opened the activity goes to onPause()
//        When Interstitial Ad is Closed the activity goes to onResume and switches on the Flash again. Not a goot UI experience!
//        So, moving this code to onCreate()
//        if(hasFlash)
//            flashFrequency(freq[selectedFlashRate]);
    }


    @Override
    protected void onStart() {
        super.onStart();
        logError("On Start");

        try { // To be safe in case bad data is sent by mistake or proguard issue in different devices
            InAppUpdater inAppUpdater = InAppUpdater.getInstance(this);
            inAppUpdater.checkInAppUpdates(this);
        } catch (Exception e) {
            Log.e("TAG BTA", "APP CRASHED");
        }

        // on starting the app get the camera params
        getCamera();
    }

    @Override
    protected void onStop() {
        InAppUpdater inAppUpdater = InAppUpdater.getInstance(this);
        inAppUpdater.onStopCalled();
        inAppUpdater.clearActivityInstance();

        super.onStop();
        logError("On Stop");
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if(cameraStatus) {
                    mCameraManager.setTorchMode(mCameraId, false);
                    cameraStatus = false;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            // on stop release the camera
            if (camera != null) {
                synchronized (lock) {
                    camera.release();
                    camera = null;
                }
            }
        }*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        int degree = Math.round(event.values[0]);
        if (degree >= 355 || degree <= 5)
            tvDirection.setText(degree + "\u00B0 N");
        else if (degree > 5 && degree < 85)
            tvDirection.setText(degree + "\u00B0 NE");
        else if (degree >= 85 && degree <= 95)
            tvDirection.setText(degree + "\u00B0 E");
        else if (degree > 95 && degree < 175)
            tvDirection.setText(degree + "\u00B0 SE");
        else if (degree >= 175 && degree <= 185)
            tvDirection.setText(degree + "\u00B0 S");
        else if (degree > 185 && degree < 265)
            tvDirection.setText(degree + "\u00B0 SW");
        else if (degree >= 265 && degree <= 275)
            tvDirection.setText(degree + "\u00B0 W");
        else if (degree > 275 && degree < 355)
            tvDirection.setText(degree + "\u00B0 NW");

//            tvDirection.setText(Float.toString(degree) + "\u00B0 N");
//            tvDirection.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        ivCompassDisc.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    void logError(String msg) {
        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            Log.e(TAG, msg);
        }
    }
    void logDebug(String msg) {
        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            Log.d(TAG, msg);
        }
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

}
