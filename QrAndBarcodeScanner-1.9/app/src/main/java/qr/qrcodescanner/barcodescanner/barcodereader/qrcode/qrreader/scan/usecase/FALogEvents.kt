package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

class FALogEvents(private val context: Context) {

    private val mFirebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    companion object {

        private var INSTANCE: FALogEvents? = null

        fun getInstance(context: Context): FALogEvents {
            return INSTANCE ?: FALogEvents(context.applicationContext).apply { INSTANCE = this }
        }
    }

    fun logCustomButtonClickEvent(button_id: String?) {
        val bundle = Bundle()
        bundle.putString("button_id", button_id)
        mFirebaseAnalytics.logEvent("button_click", bundle)
    }

    fun logCustomCameraPermissionEvent(camera_permission_status: String?) {
        val bundle = Bundle()
        bundle.putString("camera_permission_status", camera_permission_status)
        mFirebaseAnalytics.logEvent("camera_permission", bundle)
    }

    fun logCustomStoragePermissionEvent(storage_permission_status: String?) {
        val bundle = Bundle()
        bundle.putString("storage_permission_status", storage_permission_status)
        mFirebaseAnalytics.logEvent("storage_permission", bundle)
    }

    fun logCustomContactsPermissionEvent(contacts_permission_status: String?) {
        val bundle = Bundle()
        bundle.putString("contacts_permission_status", contacts_permission_status)
        mFirebaseAnalytics.logEvent("contacts_permission", bundle)
    }

    fun logImportedDataEvent(data_action_type: String?) {
        val bundle = Bundle()
        bundle.putString("data_action_type", data_action_type)
        mFirebaseAnalytics.logEvent("imported_data", bundle)
    }

    fun logAppActionEvent(action_type: String?) {
        val bundle = Bundle()
        bundle.putString("action_type", action_type)
        mFirebaseAnalytics.logEvent("app_action", bundle)
    }

    fun logInAppUpdateEvent(in_app_update_detail: String?) {
        val bundle = Bundle()
        bundle.putString("in_app_update_detail", in_app_update_detail)
        mFirebaseAnalytics.logEvent("in_app_update_event", bundle)
    }

    /*fun logButtonClickEvent(button_id: String?) {

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, button_id)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }*/

    fun logScreenViewEvent(screenName: String?, screenClass: String?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /*public void logInitiateCheckout(SkuDetails skuDetails, double priceAmount) {
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
    }*/
}