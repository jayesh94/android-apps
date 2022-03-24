package info.ascetx.flashlight.app;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.R;
import info.ascetx.flashlight.billing.BillingProvider;
import info.ascetx.flashlight.helper.SkuRowData;

import static info.ascetx.flashlight.billing.BillingConstants.SKU_PREMIUM;
import static info.ascetx.flashlight.billing.BillingConstants.getSkuList;

/**
 * Created by JAYESH on 14-08-2018.
 */

public class AcquireDialog {

    private static final String TAG = "AcquireDialog";

    private BillingProvider mBillingProvider;
    
    private String price;

    private AppEventsLogger logger;
    private WeakReference<MainActivity> mainActivityWeakReference;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FALogEvents faLogEvents;

    /**
     * Notifies the fragment/class that billing manager is ready and provides a BillingProviders
     * instance to access it
     */
    public void onManagerReady(BillingProvider billingProvider) {
        Log.e(TAG, "onManagerReady()");
        mBillingProvider = billingProvider;
//        if (mRecyclerView != null) {
//            handleManagerAndUiReady();
//        }
    }

    private void show(final SkuDetails skuDetails) {
        Log.e(TAG, "show AcquireDialog()");
        final MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;
        Log.e(TAG, "show AcquireDialog() returned?");

        Fragment f = activity.getFragmentManager().findFragmentByTag("SETTINGS_FRAGMENT");
        if (!(f != null && f.isVisible())) {
            Log.e(TAG, "Settings Fragment NOT displayed");
        } else {
            Log.e(TAG, "Settings Fragment IS displayed");
        }

        logger = AppEventsLogger.newLogger(activity);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        faLogEvents = new FALogEvents(mFirebaseAnalytics);

        faLogEvents.logScreenViewEvent("Acquire Dialog", "AcquireDialog");

        SessionManager session = new SessionManager(activity);
        final double priceAmount = skuDetails.getPriceAmountMicros() / 1000000.0;
        session.setPriceAmountMicros(skuDetails.getPriceAmountMicros());
        session.setPriceCurrencyCode(skuDetails.getPriceCurrencyCode());

        faLogEvents.logViewProductDetails(skuDetails, priceAmount);

        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder(activity, R.style.NoAdDialogTheme);
        alert.setTitle(activity.getResources().getString(R.string.premium_title));
        alert.setMessage(Html.fromHtml("<font color='#ffffff'>"+activity.getResources().getString(R.string.premium_message)
                +"<br><b>"+price+"</b>"
                +"</font>")); //Message here
        Log.e(TAG, "show AcquireDialog() setMessage");

        alert.setPositiveButton(activity.getResources().getString(R.string.premium_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                faLogEvents.logButtonClickEvent("buy_now");

                if (skuDetails != null && mBillingProvider.isPremiumPurchased()) {
                    showAlreadyPurchasedToast();
                }  else {
                    Log.e(TAG, "getDescription: " + skuDetails.getDescription());
                    Log.e(TAG, "getTitle: " + skuDetails.getTitle());

                    faLogEvents.logInitiateCheckout(skuDetails, priceAmount);

                    logInitiateCheckoutEvent(skuDetails.getOriginalJson(), skuDetails.getSku(), skuDetails.getType(), true,
                            skuDetails.getPriceCurrencyCode(), priceAmount);
                    assert skuDetails != null;
                    mBillingProvider.getBillingManager().initiatePurchaseFlow(skuDetails);

                }
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        Log.e(TAG, "show AcquireDialog() setPositiveButton");

        if (!(f != null && f.isVisible())) {
            alert.setNeutralButton(activity.getResources().getString(R.string.rewarded_vid_cta), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    faLogEvents.logButtonClickEvent("free_premium");
                    activity.showFreePremiumPopUp();
                }
            }); //End of alert.setNegativeButton
        }

        alert.setNegativeButton(activity.getResources().getString(R.string.premium_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                faLogEvents.logButtonClickEvent("later");
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                activity.btnRemAd.setImageResource(R.drawable.noad_btn_off);
                Log.e("AcquireDialog", "Dialog dismissed");
            }
        });
        Log.e(TAG, "show AcquireDialog() setOnDismissListener");
        AlertDialog alertDialog = alert.create();
        Log.e(TAG, "show AcquireDialog() NOW" + alertDialog);

        alertDialog.show();

        Button buttonPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPositive.setTextColor(ContextCompat.getColor(activity, R.color.premium_alert_dialog_button_positive));
        Button buttonNeutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        buttonNeutral.setTextColor(ContextCompat.getColor(activity, R.color.premium_alert_dialog_button_neutral));
        Button buttonNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonNegative.setTextColor(ContextCompat.getColor(activity, R.color.premium_alert_dialog_button_negative));
       /* Alert Dialog Code End*/
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    private void logInitiateCheckoutEvent(String contentData, String contentId, String contentType, boolean paymentInfoAvailable, String currency, double totalPrice) {

        Log.e(TAG, contentData);
        Log.e(TAG, contentId);
        Log.e(TAG, contentType);
        Log.e(TAG, currency);
        Log.e(TAG, String.valueOf(totalPrice));

        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putInt(AppEventsConstants.EVENT_PARAM_PAYMENT_INFO_AVAILABLE, paymentInfoAvailable ? 1 : 0);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
        logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, totalPrice, params);
    }

    private void showAlreadyPurchasedToast() {
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;
        Context context = mBillingProvider.getBillingManager().getContext();
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), R.string.alert_already_purchased, Snackbar.LENGTH_LONG).setAction("Action",null);
//        GradientDrawable gradientDrawable = new GradientDrawable(
//                GradientDrawable.Orientation.LEFT_RIGHT,
//                new int[]{ContextCompat.getColor(activity, R.color.color5),
//                        ContextCompat.getColor(activity, R.color.color1),
//                        ContextCompat.getColor(activity, R.color.color2),
//                        ContextCompat.getColor(activity, R.color.color3),
//                        ContextCompat.getColor(activity, R.color.color4),
//                        ContextCompat.getColor(activity, R.color.color5)});
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            snackbar.getView().setBackground(activity.getResources().getDrawable(R.drawable.premium_gold_dialog));
        } else {
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(context,R.color.premium_alert_dialog_start));
        }*/
        snackbar.getView().setBackgroundResource(R.drawable.premium_gold_dialog);
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        textView.setTextColor(ContextCompat.getColor(context,R.color.white));
        snackbar.show();
    }

        /**
         * Executes query for SKU details at the background thread
         */
    public void handleManagerAndUiReady(MainActivity mainActivity) {
        Log.e(TAG, "handleManagerAndUiReady() START");
        mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);

        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;

        // If Billing Manager was successfully initialized - start querying for SKUs
//        setWaitScreen(true);
        try {
            querySkuDetails();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "handleManagerAndUiReady() END");
    }

    private void displayAnErrorIfNeeded() {
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            Log.i(TAG, "No need to show an error - activity is finishing already");
            return;
        }

//        mLoadingView.setVisibility(View.GONE);
//        mErrorTextView.setVisibility(View.VISIBLE);
        int billingResponseCode = mBillingProvider.getBillingManager()
                .getBillingClientResponseCode();

        switch (billingResponseCode) {
            case BillingClient.BillingResponseCode.OK:
                // If manager was connected successfully, then show no SKUs error
                showSnackBar(activity.getWindow().getDecorView().getRootView(),activity.getResources().getString(R.string.error_no_skus));
                break;
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                showSnackBar(activity.getWindow().getDecorView().getRootView(),activity.getResources().getString(R.string.error_billing_unavailable));
                break;
            default:
                showSnackBar(activity.getWindow().getDecorView().getRootView(),activity.getResources().getString(R.string.error_billing_default));
        }
    }

    /**
     * Queries for in-app and subscriptions SKU details and updates an adapter with new data
     */
    private void querySkuDetails() {
        Log.e(TAG, "querySkuDetails()");
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;
        long startTime = System.currentTimeMillis();

        Log.e(TAG, "querySkuDetails() got subscriptions and inApp SKU details lists for: "
                + (System.currentTimeMillis() - startTime) + "ms");

        if (activity != null && !activity.isFinishing()) {
            final List<SkuRowData> dataList = new ArrayList<>();
//            mAdapter = new SkusAdapter();
//            final UiManager uiManager = createUiManager(mAdapter, mBillingProvider);
//            mAdapter.setUiManager(uiManager);
            // Filling the list with all the data to render subscription rows
//            List<String> subscriptionsSkus = getSkuList(BillingClient.SkuType.SUBS);
//            addSkuRows(dataList, subscriptionsSkus, BillingClient.SkuType.SUBS, new Runnable() {
//                @Override
//                public void run() {
                    // Once we added all the subscription items, fill the in-app items rows below
                    List<String> inAppSkus = getSkuList(BillingClient.SkuType.INAPP);
                    addSkuRows(dataList, inAppSkus, BillingClient.SkuType.INAPP, null);
//                }
//            });
        }
    }

    private void addSkuRows(final List<SkuRowData> inList, List<String> skusList,
                            final @BillingClient.SkuType String billingType, final Runnable executeWhenFinished) {
        Log.e(TAG, "addSkuRows()");
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;

        mBillingProvider.getBillingManager().querySkuDetailsAsync(billingType, skusList,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        MainActivity activity = mainActivityWeakReference.get();
                        if (activity == null || activity.isFinishing())
                            return;

                        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                            Log.w(TAG, "Unsuccessful query for type: " + billingType
                                    + ". Error code: " + billingResult.getResponseCode());
                        } else if (skuDetailsList != null
                                && skuDetailsList.size() > 0) {
                            // If we successfully got SKUs, add a header in front of the row
//                            int stringRes = BillingClient.SkuType.INAPP.equals(billingType)
//                                    ? R.string.header_inapp : R.string.header_subscriptions;
//                            inList.add(new SkuRowData(getString(stringRes)));

                            SkuDetails sendDetails = null;
                            // Then fill all the other rows
                            for (SkuDetails skuDetails : skuDetailsList) {
                                Log.e(TAG, "Adding sku: " + skuDetails);
                                inList.add(new SkuRowData(skuDetails, billingType));

                                Log.e(TAG, skuDetails.getSku());

                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                if (SKU_PREMIUM.equals(sku)) {
                                    sendDetails = skuDetails;
                                }
                            }

                            if (inList.size() == 0) {
                                displayAnErrorIfNeeded();
                            } else {

                                price = inList.get(0).getPrice();

                                final SkuDetails finalSendDetails = sendDetails;
                                (activity).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        show(finalSendDetails);
                                    }
                                });
//                                if (mRecyclerView.getAdapter() == null) {
//                                    mRecyclerView.setAdapter(mAdapter);
//                                    Resources res = getContext().getResources();
//                                    mRecyclerView.addItemDecoration(new CardsWithHeadersDecoration(
//                                            mAdapter, (int) res.getDimension(R.dimen.header_gap),
//                                            (int) res.getDimension(R.dimen.row_gap)));
//                                    mRecyclerView.setLayoutManager(
//                                            new LinearLayoutManager(getContext()));
//                                }
//
//                                mAdapter.updateData(inList);
//                                setWaitScreen(false);
                            }
                        } else {
                            // Handle empty state
                            displayAnErrorIfNeeded();
                        }

                        if (executeWhenFinished != null) {
                            executeWhenFinished.run();
                        }
                    }
                });
    }

    private void showSnackBar(View view, String message){
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(activity,R.color.snackbar_err_color));
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
