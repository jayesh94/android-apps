package info.ascetx.flashlight.app.nativeAds;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import info.ascetx.flashlight.R;
import info.ascetx.flashlight.app.FALogEvents;

public class SetNativeAd {

    private final Activity activity;
    private final NativeAd nativeAd;
    private View view = null;
    private PreferenceViewHolder holder = null;

    public SetNativeAd(Activity activity, NativeAd nativeAd, View view) {
        this.activity = activity;
        this.nativeAd = nativeAd;
        this.view = view;
    }

    public SetNativeAd(Activity activity, NativeAd nativeAd, PreferenceViewHolder holder) {
        this.activity = activity;
        this.nativeAd = nativeAd;
        this.holder = holder;
    }

    public void setMediaViewSize() {
        if (nativeAd == null)
            return;

        MediaView mediaView = null;

        if (view != null){
            mediaView = (MediaView) view.findViewById(R.id.ad_media);
        }

        if (holder != null){
            mediaView = (MediaView) holder.findViewById(R.id.ad_media);
        }

        float scale = activity.getResources().getDisplayMetrics().density;

        int maxHeightPixels = 175;
        int maxHeightDp = (int) (maxHeightPixels * scale + 0.5f);

        if (nativeAd.getMediaContent().hasVideoContent()) {
            Log.e("SetNativeAd", "Videos");
//            ViewGroup.LayoutParams params = mediaView.getLayoutParams();
//            params.height = maxHeightDp;
//            mediaView.setLayoutParams(params);
        } else {
            Log.e("SetNativeAd", "ImageView");
            ViewGroup.LayoutParams params = mediaView.getLayoutParams();
            params.height = maxHeightDp;
            mediaView.setLayoutParams(params);
        }
    }

    /**
     * Populates a {@link NativeAdView} object with data from a given
     * {@link NativeAd}.
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView  the view to be populated
     */
    public void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        Log.e("TAG NativeAd","getHeadline: "+nativeAd.getHeadline());
        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        Log.e("TAG NativeAd","Body: "+nativeAd.getBody());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        Log.e("TAG NativeAd","getCallToAction: "+nativeAd.getCallToAction());

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        Log.e("TAG NativeAd","getPrice: "+nativeAd.getPrice());
        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        Log.e("TAG NativeAd","getStore: "+nativeAd.getStore());
        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        Log.e("TAG NativeAd","getStarRating: "+nativeAd.getStarRating());
        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        Log.e("TAG NativeAd","getAdvertiser: "+nativeAd.getAdvertiser());
        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }

    public void isNativeAdVideoAd() {
        if (nativeAd == null)
            return;

        if (nativeAd.getMediaContent().hasVideoContent()) {

            float mediaAspectRatio = nativeAd.getMediaContent().getAspectRatio();
            float duration = nativeAd.getMediaContent().getDuration();
            Log.e("SetNativeAd", "mediaAspectRatio: "+mediaAspectRatio);
            Log.e("SetNativeAd", "duration: "+duration);
            logNativeAdEvents();
        }
    }

    private void logNativeAdEvents() {

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);

        final FALogEvents faLogEvents = new FALogEvents(mFirebaseAnalytics);

        nativeAd.getMediaContent().getVideoController()
                .setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {

                    final String logPrefix = "native_video_ad_";

                    /** Called when video playback first begins. */
                    @Override
                    public void onVideoStart() {
                        // Do something when the video starts the first time.
                        Log.e("SetNativeAd", "Video Started");
                        faLogEvents.logButtonClickEvent(logPrefix + "started");
                    }

                    /** Called when video playback is playing. */
                    @Override
                    public void onVideoPlay() {
                        // Do something when the video plays.
                        Log.e("SetNativeAd", "Video Played");
                        faLogEvents.logButtonClickEvent(logPrefix + "played");
                    }

                    /** Called when video playback is paused. */
                    @Override
                    public void onVideoPause() {
                        // Do something when the video pauses.
                        Log.e("SetNativeAd", "Video Paused");
                        faLogEvents.logButtonClickEvent(logPrefix + "paused");
                    }

                    /** Called when video playback finishes playing. */
                    @Override
                    public void onVideoEnd() {
                        // Do something when the video ends.
                        Log.e("SetNativeAd", "Video Ended");
                        faLogEvents.logButtonClickEvent(logPrefix + "ended");
                    }

                    /** Called when the video changes mute state. */
                    @Override
                    public void onVideoMute(boolean isMuted) {
                        // Do something when the video is muted.
                        Log.e("SetNativeAd", "Video Muted");
                        faLogEvents.logButtonClickEvent(logPrefix + "muted");
                    }
                });
    }
}
