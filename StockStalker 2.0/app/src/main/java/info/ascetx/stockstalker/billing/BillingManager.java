package info.ascetx.stockstalker.billing;

/**
 * Created by JAYESH on 14-08-2018.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.SessionManager;

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed
 */
public class BillingManager implements PurchasesUpdatedListener {
    // Default value of mBillingClientResponseCode until BillingManager was not yet initialized
    public static final int BILLING_MANAGER_NOT_INITIALIZED  = -1;

    private static final String TAG = "BillingManager";

    /** A reference to BillingClient **/
    private BillingClient mBillingClient;

    private AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener;

    /** A reference to BillingResult **/
    private BillingResult mBillingResult;

    /**
     * True if billing service is connected now.
     */
    private boolean mIsServiceConnected;

    private final BillingUpdatesListener mBillingUpdatesListener;

    private final Activity mActivity;

    private final List<Purchase> mPurchases = new ArrayList<>();

    private Set<String> mTokensToBeConsumed;
    private AppEventsLogger logger;
    private SessionManager session;

    private int mBillingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "onPurchasesUpdated() - BillingClient.BillingResponseCode.OK");

            assert purchases != null;
            if (purchases.size() == 0) {
                session.setPremiumUser(false);
                session.setPurchaseAcknowledged(false);
            }

            assert purchases != null;
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
//            Log.d(TAG,"Before: mBillingUpdatesListener.onPurchasesUpdated(mPurchases);");
            // Adding this line of code in Callback at handlePurchase as the
            // purchase signature verification is done at server side using Async volley request causing delay
//            mBillingUpdatesListener.onPurchasesUpdated(mPurchases);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            Log.w(TAG, "onPurchasesUpdated() got unknown resultCode: " + billingResult.getResponseCode());
        }
    }

    /**
     * Listener to the updates that happen when purchases list was updated or consumption of the
     * item was finished
     */
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();
        void onConsumeFinished(String token, @BillingClient.BillingResponseCode int result);
        void onPurchasesUpdated(List<Purchase> purchases);
    }

    /**
     * Listener for the Billing client state to become connected
     */
    public interface ServiceConnectedListener {
        void onServiceConnected(@BillingClient.BillingResponseCode int resultCode);
    }

    public BillingManager(Activity activity, final BillingUpdatesListener updatesListener) {
        Log.d(TAG, "Creating Billing client.");
        mActivity = activity;
        mBillingUpdatesListener = updatesListener;
        mBillingClient = BillingClient.newBuilder(mActivity)
                .enablePendingPurchases()
                .setListener(this).build();

        logger = AppEventsLogger.newLogger(activity);
        session = new SessionManager(activity);

        Log.d(TAG, "Starting setup.");

        // Start setup. This is asynchronous and the specified listener will be called
        // once setup completes.
        // It also starts to report all the new purchases through onPurchasesUpdated() callback.
        startServiceConnection(new Runnable() {
            @Override
            public void run() {
                // Notifying the listener that billing client is ready
                mBillingUpdatesListener.onBillingClientSetupFinished();
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                queryPurchases();
            }
        });
    }

    /**
     * Start a purchase or subscription replace flow
     */
    public void initiatePurchaseFlow(SkuDetails skuDetails) {
        Runnable purchaseFlowRequest = new Runnable() {
            @Override
            public void run() {

                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails)
                        .build();
                BillingResult responseCode = mBillingClient.launchBillingFlow(mActivity, flowParams);
            }
        };

        executeServiceRequest(purchaseFlowRequest);
    }

    public Context getContext() {
        return mActivity;
    }

    /**
     * Clear the resources
     */
    public void destroy() {
        Log.d(TAG, "Destroying the manager.");

        if (mBillingClient != null && mBillingClient.isReady()) {
            mBillingClient.endConnection();
            mBillingClient = null;
        }
    }

    public void querySkuDetailsAsync(@SkuType final String itemType, final List<String> skuList,
                                     final SkuDetailsResponseListener listener) {
        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                // Query the purchase async
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(itemType);
                mBillingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                                listener.onSkuDetailsResponse(billingResult, skuDetailsList);
                            }
                        });
            }
        };

        executeServiceRequest(queryRequest);
    }

    public void consumeAsync(final String purchaseToken) {
        // If we've already scheduled to consume this token - no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        if (mTokensToBeConsumed == null) {
            mTokensToBeConsumed = new HashSet<>();
        } else if (mTokensToBeConsumed.contains(purchaseToken)) {
            Log.i(TAG, "Token was already scheduled to be consumed - skipping...");
            return;
        }
        mTokensToBeConsumed.add(purchaseToken);

        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build();

        // Generating Consume Response listener
        final ConsumeResponseListener onConsumeListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String s) {
                // If billing service was disconnected, we try to reconnect 1 time
                // (feel free to introduce your retry policy here).
                mBillingUpdatesListener.onConsumeFinished(purchaseToken, billingResult.getResponseCode());
            }
        };

        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable consumeRequest = new Runnable() {
            @Override
            public void run() {
                // Consume the purchase async
                mBillingClient.consumeAsync(consumeParams, onConsumeListener);
            }
        };

        executeServiceRequest(consumeRequest);
    }

    /**
     * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
     * client connection response was not received yet.
     */
    public int getBillingClientResponseCode() {
        return mBillingClientResponseCode;
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * </p>
     * @param purchase Purchase to be handled
     */
    private void handlePurchase(Purchase purchase) {
        Log.e(TAG, "handlePurchase()");
        String signedData = purchase.getOriginalJson();
        String signature = purchase.getSignature();
        try {
            Security.verifyPurchase(new info.ascetx.stockstalker.billing.VolleyCallback() {
                @Override
                public void onSuccess(boolean result) {
                    if (result){
                        Log.e(TAG, "Got a verified purchase: " + purchase);

                        ((MainActivity) mActivity).checkRestorePurchase(purchase.getOriginalJson());

                        // Acknowledge the purchase if it hasn't already been acknowledged.
                        if (!purchase.isAcknowledged()) {
                            AcknowledgePurchaseParams acknowledgePurchaseParams =
                                    AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(purchase.getPurchaseToken())
                                            .build();
                            Log.e(TAG,"isPremiumUser: "+ session.isPremiumUser());
                            acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {

                                @Override
                                public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                                    Log.e(TAG,"isPremiumUser: "+ session.isPremiumUser());
                                    if(!session.isPurchaseAcknowledged()){
                                        showCongratsMessage();
                                        logSubscribeEvent(purchase.getOrderId());
                                        session.setPurchaseAcknowledged(true);
                                    }
                                }
                            };

                            mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                        }
                        mPurchases.add(purchase);
                        mBillingUpdatesListener.onPurchasesUpdated(mPurchases);
                    } else {
                        Log.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
                    }
                }
            }, signedData, signature);
        } catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            Log.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
        }
    }

    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesFinished(PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (mBillingClient == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "Billing client was null or result code (" + result.getResponseCode()
                    + ") was bad - quitting");
            return;
        }

        Log.d(TAG, "Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        mPurchases.clear();
        onPurchasesUpdated(mBillingResult, result.getPurchasesList());
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    private void logSubscribeEvent(String orderId) {

        double priceAmount = session.getPriceAmountMicros() / 1000000.0;
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_ORDER_ID, orderId);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, session.getPriceCurrencyCode());
        logger.logEvent(AppEventsConstants.EVENT_NAME_SUBSCRIBE, priceAmount, params);
    }

    /**
     * Checks if subscriptions are supported for current client
     * <p>Note: This method does not automatically retry for RESULT_SERVICE_DISCONNECTED.
     * It is only used in unit tests and after queryPurchases execution, which already has
     * a retry-mechanism implemented.
     * </p>
     */
    public boolean areSubscriptionsSupported() {
        BillingResult billingResult = mBillingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS);
        if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "areSubscriptionsSupported() got an error response: " + billingResult.getResponseCode());
        }
        return billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK;
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through
     * a listener
     */
    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();

                PurchasesResult purchasesResult = mBillingClient.queryPurchases(SkuType.INAPP);
                Log.i(TAG, "Querying purchases elapsed time: " + (System.currentTimeMillis() - time)
                        + "ms");
                // If there are subscriptions supported, we add subscription rows as well
                if (areSubscriptionsSupported()) {
                    PurchasesResult subscriptionResult
                            = mBillingClient.queryPurchases(SkuType.SUBS);
                    Log.i(TAG, "Querying purchases and subscriptions elapsed time: "
                            + (System.currentTimeMillis() - time) + "ms");
                    /*Handling below error:
                    java.lang.NullPointerException: Attempt to invoke interface method 'int java.util.List.size()' on a null object reference
                    at info.ascetx.stockstalker.billing.BillingManager$7.run(BillingManager.java:380)
                    at info.ascetx.stockstalker.billing.BillingManager.executeServiceRequest(BillingManager.java:428)
                    at info.ascetx.stockstalker.billing.BillingManager.queryPurchases(BillingManager.java:398)
                    at info.ascetx.stockstalker.MainActivity.onResume(MainActivity.java:1035)*/
                    if (subscriptionResult.getPurchasesList() == null){
                        return;
                    }

                    Log.i(TAG, "Querying subscriptions result code: "
                            + subscriptionResult.getResponseCode()
                            + " res: " + subscriptionResult.getPurchasesList().size());

                    if (subscriptionResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        purchasesResult.getPurchasesList().addAll(
                                subscriptionResult.getPurchasesList());
                    } else {
                        Log.e(TAG, "Got an error response trying to query subscription purchases");
                    }
                } else if (purchasesResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Skipped subscription purchases query since they are not supported");
                } else {
                    Log.w(TAG, "queryPurchases() got an error response code: "
                            + purchasesResult.getResponseCode());
                }
                onQueryPurchasesFinished(purchasesResult);
            }
        };

        executeServiceRequest(queryToExecute);
    }

    private void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.d(TAG, "Setup finished. Response code: " + billingResult.getResponseCode());

                mBillingResult = billingResult;

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    mIsServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
                mBillingClientResponseCode = billingResult.getResponseCode();
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
            }
        });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (mIsServiceConnected) {
            runnable.run();
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable);
        }
    }

    private void showCongratsMessage() {
        Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content), R.string.alert_congrats_purchased, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackground(mActivity.getResources().getDrawable(R.drawable.premium_gold_dialog));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
        snackbar.show();
    }

}
