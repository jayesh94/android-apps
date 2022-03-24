package info.ascetx.flashlight.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.R;
import info.ascetx.flashlight.app.FALogEvents;
import info.ascetx.flashlight.app.nativeAds.NativeAdManager;
import info.ascetx.flashlight.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.GetPremiumInterface, SettingsFragment.LogSettingsFragmentItemClick {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FALogEvents faLogEvents;
    private NativeAdManager nativeAdManager;
    public ListenFromActivity activityListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        faLogEvents = new FALogEvents(mFirebaseAnalytics);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

// Show menu icon
        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(), "SETTINGS_FRAGMENT")
                    .commit();
        }

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        faLogEvents.logScreenViewEvent("Settings", "SettingsActivity");

        nativeAdManager = new NativeAdManager(this);
        nativeAdManager.loadNativeAd();
    }

    @Override
    protected void onDestroy() {
        nativeAdManager.destroyNativeAd();
        super.onDestroy();
    }

    @Override
    public void buyNow() {
        MainActivity activity = MainActivity.getInstance();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.initPurchase();
    }


    @Override
    public void logSettingsFragmentItemClick(String button_id) {
        faLogEvents.logButtonClickEvent(button_id);
    }


    public interface ListenFromActivity {
        void doSomethingInFragment(NativeAd nativeAd);
    }

    public void setActivityListener(ListenFromActivity activityListener) {
        this.activityListener = activityListener;
    }
}