package info.ascetx.flashlight.app.nativeAds;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Date;

import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.activity.ScreenLightActivity;
import info.ascetx.flashlight.activity.SettingsActivity;
import info.ascetx.flashlight.app.Config;
import info.ascetx.flashlight.app.configs.RemoteConfigs;
import info.ascetx.flashlight.app.nativeAds.dialog.NativeAdExitDialog;
import info.ascetx.flashlight.app.nativeAds.dialog.NativeAdScreenTransitionDialog;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT;
import static info.ascetx.flashlight.app.AppRater.APP_RATER_SHARED_PREF;
import static info.ascetx.flashlight.app.Config.screenTransitionNativeAd;
import static info.ascetx.flashlight.app.Config.settingsNativeAd;

public class NativeAdManager {
    private final Activity activity;
    private NativeAd nativeAd;

    public NativeAdManager(Activity activity) {
        this.activity = activity;
    }

    public void loadNativeAd (){
        try {
            if (!MainActivity.session.isPremiumUser() && startShowingAds()) {
                if (new Date().getTime() > MainActivity.session.getRewardAdDateTime().getTime()) {
                    if(nativeAd != null) {// Included to keep the ad showing when the user return to the app after clicking the ad
                        if(nativeAd.getHeadline() == null){
                            nativeAd.destroy();
                            nativeAd = null;
                            loadNativeExitAd();
                        }
                    } else {
                        loadNativeExitAd();
                    }
                }
            }
        } catch (Exception e) {
            // I suspect that startShowingAds() is failing with npe
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public Boolean startShowingAds(){

        int gapDayLaunch = RemoteConfigs.INSTANCE.dayLaunchGapToStartShowingAds();
        gapDayLaunch = 0;
        final SharedPreferences prefs = activity.getSharedPreferences(APP_RATER_SHARED_PREF, 0);
        // Get date of first launch
        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        long launch_count = prefs.getLong("launch_count", 0);
        long currentDateTime = System.currentTimeMillis();
        return (currentDateTime >= date_firstLaunch +
                ((long) gapDayLaunch * 24 * 60 * 60 * 1000))
                &&
                launch_count > gapDayLaunch;
    }

    private void loadNativeExitAd() {
        Log.e("TAG MA NativeAd","loadNativeExitAd");

        String nativeAdID = Config.nativeAd;

        if (activity instanceof SettingsActivity) {
            Log.e("TAG MA NativeAd","SettingsActivity");
            nativeAdID = settingsNativeAd;
        } else if (activity instanceof ScreenLightActivity) {
            Log.e("TAG MA NativeAd","ScreenLightActivity");
            nativeAdID = screenTransitionNativeAd;
        }

        AdLoader.Builder builder = new AdLoader.Builder(activity, nativeAdID); // change AdId based on activity

        setNativeAdListener(builder);

        setAdOptions(builder);

        loadNativeAd(builder);

    }

    private void loadNativeAd(AdLoader.Builder builder) {
        AdLoader adLoader =
                builder
                        .withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                                        String error =
                                                String.format(
                                                        "domain: %s, code: %d, message: %s",
                                                        loadAdError.getDomain(),
                                                        loadAdError.getCode(),
                                                        loadAdError.getMessage());
                                    }
                                })
                        .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void setAdOptions(AdLoader.Builder builder) {
        VideoOptions videoOptions =
                new VideoOptions.Builder()
                        .setStartMuted(true).build(); //  set True to Keep video muted

        NativeAdOptions adOptions =
                new NativeAdOptions.Builder()
                        .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
                        .setVideoOptions(videoOptions).build();

        builder.withNativeAdOptions(adOptions);
    }

    private void setNativeAdListener(AdLoader.Builder builder) {
        builder.forNativeAd(
                new NativeAd.OnNativeAdLoadedListener() {
                    // OnNativeAdLoadedListener implementation.
                    @Override
                    public void onNativeAdLoaded(NativeAd NativeAd) {
                        Log.e("TAG MA NativeAd","loadNativeExitAd: onNativeAdLoaded");
                        // If this callback occurs after the activity is destroyed, you must call
                        // destroy and return or you may get a memory leak.
                        boolean isDestroyed = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            isDestroyed = activity.isDestroyed();
                        }
                        if (isDestroyed || activity.isFinishing() || activity.isChangingConfigurations()) {
                            NativeAd.destroy();
                            return;
                        }
                        // You must call destroy on old ads when you are done with them,
                        // otherwise you will have a memory leak.
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        nativeAd = NativeAd;

                        if (activity instanceof SettingsActivity) {
                            SettingsActivity settingsActivity = (SettingsActivity) activity;
                            if (null != settingsActivity.activityListener) {
                                settingsActivity.activityListener.doSomethingInFragment(nativeAd);
                            }
                        }

                        Log.e("TAG MA NativeAd","getHeadline: "+nativeAd.getHeadline());
                        Log.e("TAG MA NativeAd","Body: "+nativeAd.getBody());
                        Log.e("TAG MA NativeAd","getCallToAction: "+nativeAd.getCallToAction());
                        Log.e("TAG MA NativeAd","getPrice: "+nativeAd.getPrice());
                        Log.e("TAG MA NativeAd","getStore: "+nativeAd.getStore());
                        Log.e("TAG MA NativeAd","getStarRating: "+nativeAd.getStarRating());
                        Log.e("TAG MA NativeAd","getAdvertiser: "+nativeAd.getAdvertiser());

                    }
                });
    }

    public void destroyNativeAd(){
        if (nativeAd != null){
            nativeAd.destroy();
        }
    }

    public void showNativeAdExitDialog() {

        if (nativeAd == null) {
            activity.finishAffinity();
            return;
        }

        NativeAdExitDialog nativeAdExitDialog = new NativeAdExitDialog((MainActivity) activity, nativeAd);
        nativeAdExitDialog.show();

        Window window = nativeAdExitDialog.getWindow();

        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void showScreenTransitionAd() {

        if (nativeAd == null) {
            activity.finish();
            return;
        }

        NativeAdScreenTransitionDialog nativeAdScreenTransitionDialog = new NativeAdScreenTransitionDialog((ScreenLightActivity) activity, nativeAd);
        nativeAdScreenTransitionDialog.show();

        Window window = nativeAdScreenTransitionDialog.getWindow();

        if (window != null)
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
