package info.ascetx.flashlight.app;

import android.os.Bundle;
import android.os.Parcelable;

import com.android.billingclient.api.SkuDetails;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FALogEvents {

    private final FirebaseAnalytics mFirebaseAnalytics;

    public FALogEvents(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    public void logButtonClickEvent(String button_id) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, button_id);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void logScreenViewEvent(String screenName, String screenClass) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void logInitiateCheckout(SkuDetails skuDetails, double priceAmount) {
        Bundle item = new Bundle();
        item.putString(FirebaseAnalytics.Param.ITEM_ID, skuDetails.getSku());
        item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, skuDetails.getType());
        item.putString(FirebaseAnalytics.Param.ITEM_NAME, skuDetails.getTitle());
        item.putString(FirebaseAnalytics.Param.CURRENCY, skuDetails.getPriceCurrencyCode());
        item.putDouble(FirebaseAnalytics.Param.PRICE, priceAmount);
        item.putDouble(FirebaseAnalytics.Param.QUANTITY, 1);

        Bundle beginCheckoutParams = new Bundle();
        beginCheckoutParams.putString(FirebaseAnalytics.Param.CURRENCY, skuDetails.getPriceCurrencyCode());
        beginCheckoutParams.putDouble(FirebaseAnalytics.Param.VALUE, priceAmount);
        beginCheckoutParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS,
                new Parcelable[]{ item });

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, beginCheckoutParams);
    }

    public void logViewProductDetails(SkuDetails skuDetails, double priceAmount) {

        Bundle item = new Bundle();
        item.putString(FirebaseAnalytics.Param.ITEM_ID, skuDetails.getSku());
        item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, skuDetails.getType());
        item.putString(FirebaseAnalytics.Param.ITEM_NAME, skuDetails.getTitle());
        item.putString(FirebaseAnalytics.Param.CURRENCY, skuDetails.getPriceCurrencyCode());
        item.putDouble(FirebaseAnalytics.Param.PRICE, priceAmount);
        item.putDouble(FirebaseAnalytics.Param.QUANTITY, 1);

        Bundle viewItemParams = new Bundle();
        viewItemParams.putString(FirebaseAnalytics.Param.CURRENCY, skuDetails.getPriceCurrencyCode());
        viewItemParams.putDouble(FirebaseAnalytics.Param.VALUE, priceAmount);
        viewItemParams.putParcelableArray(FirebaseAnalytics.Param.ITEMS,
                new Parcelable[] { item });

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, viewItemParams);
    }

    public void logInAppUpdateEvent(String in_app_update_detail) {
        Bundle bundle = new Bundle();
        bundle.putString("in_app_update_detail", in_app_update_detail);
        mFirebaseAnalytics.logEvent("in_app_update_event", bundle);
    }

    public void logAdDisplayEvent(String ad_type) {
        Bundle bundle = new Bundle();
        bundle.putString("ad_type", ad_type);
        mFirebaseAnalytics.logEvent("ad_shown", bundle);
    }

    public void logAdClickEvent(String ad_format) {
        Bundle bundle = new Bundle();
        bundle.putString("ad_format", ad_format);
        mFirebaseAnalytics.logEvent("ad_clicked", bundle);
    }

    public void logRewardedAdEvent(String reward_type) {
        Bundle bundle = new Bundle();
        bundle.putString("reward_type", reward_type);
        mFirebaseAnalytics.logEvent("rewarded_ad", bundle);
    }
}
