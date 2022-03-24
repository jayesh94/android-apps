package info.ascetx.stockstalker.app;

/**
 * Created by JAYESH on 14-08-2018.
 */

import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;

import java.lang.ref.WeakReference;
import java.util.List;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.billing.BillingManager;

import static info.ascetx.stockstalker.billing.BillingConstants.SKU_GOLD_MONTHLY;
import static info.ascetx.stockstalker.billing.BillingConstants.SKU_GOLD_YEARLY;
import static info.ascetx.stockstalker.billing.BillingConstants.SKU_PREMIUM;

/**
 * Handles control logic of the BaseGamePlayActivity
 */
public class MainViewController {
    private static final String TAG = "MainViewController";


    private final UpdateListener mUpdateListener;
    private WeakReference<MainActivity> mainActivityWeakReference;

    // Tracks if we currently own a premium car
    private boolean mIsPremium;
    private boolean mGoldMonthly;
    private boolean mGoldYearly;

    public MainViewController(MainActivity mainActivity) {
        mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);
        mUpdateListener = new UpdateListener();

        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        loadData();
    }

//    public void useGas() {
//        mTank--;
//        saveData();
//        Log.d(TAG, "Tank is now: " + mTank);
//    }

    public UpdateListener getUpdateListener() {
        return mUpdateListener;
    }

//    public boolean isTankEmpty() {
//        return mTank <= 0;
//    }
//
//    public boolean isTankFull() {
//        return mTank >= TANK_MAX;
//    }

    public boolean isPremiumPurchased() {
        return mIsPremium;
    }


//    public @DrawableRes int getTankResId() {
//        int index = (mTank >= TANK_RES_IDS.length) ? (TANK_RES_IDS.length - 1) : mTank;
//        return TANK_RES_IDS[index];
//    }

    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            MainActivity mActivity = mainActivityWeakReference.get();
            if (mActivity == null || mActivity.isFinishing())
                return;
            mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingClient.BillingResponseCode int result) {
            MainActivity mActivity = mainActivityWeakReference.get();
            if (mActivity == null || mActivity.isFinishing())
                return;
            Log.d(TAG, "Consumption finished. Purchase token: " + token + ", result: " + result);

            // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
            // check if token corresponding to the expected sku was consumed.
            // If you have more than one sku, you probably need to validate that the token matches
            // the SKU you expect.
            // It could be done by maintaining a map (updating it every time you call consumeAsync)
            // of all tokens into SKUs which were scheduled to be consumed and then looking through
            // it here to check which SKU corresponds to a consumed token.
            if (result == BillingClient.BillingResponseCode.OK) {
                // Successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                new SessionManager(mActivity).setPremiumUser(false);
                new SessionManager(mActivity).setPurchaseAcknowledged(false);
//                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
//                saveData();
//                mActivity.alert(R.string.alert_fill_gas, mTank);
            } else {
//                mActivity.alert(R.string.alert_error_consuming, result);
            }

//            mActivity.showRefreshedUi();
            Log.d(TAG, "End consumption flow.");
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            MainActivity mActivity = mainActivityWeakReference.get();
            if (mActivity == null || mActivity.isFinishing())
                return;
            Log.d(TAG, "onPurchasesUpdated.");
            mGoldMonthly = false;
            mGoldYearly = false;
            Log.d(TAG, "Purchase SKU: "+purchaseList.size());
            SessionManager session = new SessionManager(mActivity);
            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case SKU_PREMIUM:
                        Log.d(TAG, "You are Premium! Congratulations!!!");
                        // TODO do server verification by sending purchase token to server
                        // MAY NOT BE REQUIRED
                        Log.e(TAG, "getPurchaseToken: "+purchase.getPurchaseToken());
                        Log.e(TAG, "getPackageName: "+purchase.getPackageName());
                        Log.e(TAG, "getSku: "+purchase.getSku());
                        mIsPremium = true;
                        if(!session.isPremiumUser()) {
                            session.setPremiumUser(true);
                        }
//                        mActivity.getBillingManager().consumeAsync(purchase.getPurchaseToken());
                        break;
                    case SKU_GOLD_MONTHLY:
                        mGoldMonthly = true;
                        Log.e(TAG, "getPurchaseToken: "+purchase.getPurchaseToken());
                        Log.e(TAG, "getPackageName: "+purchase.getPackageName());
                        Log.e(TAG, "getSku: "+purchase.getSku());
                        mIsPremium = true;
                        if(!session.isPremiumUser()) {
                            session.setPremiumUser(true);
                        }
                        break;
                    case SKU_GOLD_YEARLY:
                        mGoldYearly = true;
                        Log.e(TAG, "getPurchaseToken: "+purchase.getPurchaseToken());
                        Log.e(TAG, "getPackageName: "+purchase.getPackageName());
                        Log.e(TAG, "getSku: "+purchase.getSku());
                        mIsPremium = true;
                        if(!session.isPremiumUser()) {
                            session.setPremiumUser(true);
                        }
                        break;
                }
            }
            mActivity.showRefreshedUi();
        }
    }

    /**
     * Save current tank level to disc
     *
     * Note: In a real application, we recommend you save data in a secure way to
     * prevent tampering.
     * For simplicity in this sample, we simply store the data using a
     * SharedPreferences.
     */
    private void saveData() {
//        SharedPreferences.Editor spe = mActivity.getPreferences(MODE_PRIVATE).edit();
//        spe.putInt("tank", mTank);
//        spe.apply();
//        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    private void loadData() {
//        SharedPreferences sp = mActivity.getPreferences(MODE_PRIVATE);
//        mTank = sp.getInt("tank", 2);
//        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }
}
