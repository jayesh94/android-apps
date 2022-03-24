package info.ascetx.flashlight.app.nativeAds.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.R;
import info.ascetx.flashlight.activity.ScreenLightActivity;
import info.ascetx.flashlight.app.FALogEvents;
import info.ascetx.flashlight.app.nativeAds.SetNativeAd;

public class NativeAdScreenTransitionDialog extends Dialog {

    private final ScreenLightActivity activity;
    private final NativeAd nativeAd;
    private SetNativeAd setNativeAd;

    public NativeAdScreenTransitionDialog(final ScreenLightActivity activity, NativeAd nativeAd) {
        super(activity);
        this.activity = activity;
        this.nativeAd = nativeAd;

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Log.e("TAG NativeAd","setOnShowListener: NativeAdScreenTransitionDialog");

                // Obtain the FirebaseAnalytics instance.
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

                final FALogEvents faLogEvents = new FALogEvents(mFirebaseAnalytics);
                faLogEvents.logScreenViewEvent("NativeAdScreenTransitionDialog", "NativeAdScreenTransitionDialog");
            }
        });

        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.native_ad_screen_transition_dialog);

        setNativeAd = new SetNativeAd(activity, nativeAd, this.findViewById(android.R.id.content));

        setNativeAd.isNativeAdVideoAd();
        showNativeAd();
        setExitButton();

    }

    private void showNativeAd() {

        FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
        NativeAdView adView =
                (NativeAdView) getLayoutInflater()
                        .inflate(R.layout.ad_unified, null);

        if (nativeAd == null){
            frameLayout.setVisibility(View.GONE);
        } else {
            frameLayout.setVisibility(View.VISIBLE);
            setNativeAd.populateNativeAdView(nativeAd, adView);
            frameLayout.removeAllViews();
            frameLayout.addView(adView);
            setNativeAd.setMediaViewSize();
        }
    }

    private void setExitButton() {
        ImageView exit = findViewById(R.id.imageView);

        if (exit == null)
            return;

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.finish();
            }
        });
    }
}
