package info.ascetx.flashlight.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import info.ascetx.flashlight.R;
import info.ascetx.flashlight.activity.SettingsActivity;
import info.ascetx.flashlight.app.nativeAds.SetNativeAd;

public class SettingsNativeAdPreference extends Preference implements SettingsActivity.ListenFromActivity {

    private PreferenceViewHolder holder;
    private SettingsActivity settingsActivity;
    private NativeAd nativeAd;

    public SettingsNativeAdPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SettingsNativeAdPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingsNativeAdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsNativeAdPreference(Context context) {
        super(context);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.holder = holder;

        settingsActivity = (SettingsActivity) getContext();
        settingsActivity.setActivityListener(this);

    }

    private void showNativeAd() {

        SetNativeAd setNativeAd = new SetNativeAd(settingsActivity, nativeAd, holder);
        NativeAdView adView = (NativeAdView) holder.findViewById(R.id.adViewPref);

        if (nativeAd == null){
            adView.setVisibility(View.GONE);
        } else {
            setNativeAd.populateNativeAdView(nativeAd, adView);
            setNativeAd.setMediaViewSize();
            adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void doSomethingInFragment(NativeAd nativeAd) {
        this.nativeAd = nativeAd;
        showNativeAd();
    }
}
