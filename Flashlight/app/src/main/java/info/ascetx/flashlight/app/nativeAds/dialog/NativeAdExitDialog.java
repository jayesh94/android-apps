package info.ascetx.flashlight.app.nativeAds.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.R;
import info.ascetx.flashlight.app.nativeAds.SetNativeAd;

public class NativeAdExitDialog extends Dialog {

    private final MainActivity activity;
    private final NativeAd nativeAd;
    private SetNativeAd setNativeAd;

    public NativeAdExitDialog(final MainActivity activity, NativeAd nativeAd) {
        super(activity);
        this.activity = activity;
        this.nativeAd = nativeAd;

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Log.e("TAG NativeAd","setOnShowListener: NativeAdExitDialog");
                activity.faLogEvents.logScreenViewEvent("NativeAdExitDialog", "NativeAdExitDialog");
            }
        });

        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.native_ad_exit_dialog);

        setNativeAd = new SetNativeAd(activity, nativeAd, this.findViewById(android.R.id.content));

        setNativeAd.isNativeAdVideoAd();
        showNativeAd();
        setYesButton();
        setNoButton();

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

    private void setNoButton() {
        Button no = findViewById(R.id.button_later);

        if (no == null)
            return;

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.faLogEvents.logButtonClickEvent("native_exit_dialog_no");
                dismiss();
            }
        });
    }

    private void setYesButton() {
        Button yes = findViewById(R.id.button_yes);

        if (yes == null)
            return;

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.faLogEvents.logButtonClickEvent("native_exit_dialog_yes");
                dismiss();
                activity.finishAffinity();
            }
        });
    }
}
