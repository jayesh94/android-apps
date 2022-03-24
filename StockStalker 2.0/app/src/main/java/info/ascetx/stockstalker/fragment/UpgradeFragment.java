package info.ascetx.stockstalker.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.billing.BillingProvider;
import info.ascetx.stockstalker.helper.SkuRowData;

import static info.ascetx.stockstalker.billing.BillingConstants.SKU_GOLD_MONTHLY;
import static info.ascetx.stockstalker.billing.BillingConstants.SKU_GOLD_YEARLY;
import static info.ascetx.stockstalker.billing.BillingConstants.getSkuList;

public class UpgradeFragment extends Fragment implements View.OnClickListener{

    private final String TAG = "UpgradeFragment";

    private View mView;
    private TextView mTvRestorePurch, mTvSubsPolicy;
    private Button  mBtnMonthSub, mBtnYearSub;
    private BillingProvider mBillingProvider;
    private OnUpgradePremiumFragmentInteractionListener mListener;
    private WeakReference<MainActivity> mainActivityWeakReference;
    private SessionManager session;
    private SkuDetails skuDetailsMonthly = null, skuDetailsYearly = null;
    private MainActivity mainActivity;
    private AppEventsLogger logger;

    public UpgradeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager((Context) mListener);
        logger = AppEventsLogger.newLogger(mainActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upgrade, container, false);
        mView = view;

        mainActivity = (MainActivity) mListener;

        mBtnMonthSub = view.findViewById(R.id.btn_sub_monthly);
        mBtnYearSub = view.findViewById(R.id.btn_sub_yearly);
        mTvRestorePurch = view.findViewById(R.id.btn_restore_purchase);
        mTvSubsPolicy = view.findViewById(R.id.tv_subscription_policy);
        mTvRestorePurch.setPaintFlags(mTvRestorePurch.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mTvSubsPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        view.findViewById(R.id.btn_sub_monthly).setOnClickListener(this);
        view.findViewById(R.id.btn_sub_yearly).setOnClickListener(this);
        view.findViewById(R.id.btn_restore_purchase).setOnClickListener(this);

        mListener.onUpgradePremiumFragmentCreate(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sub_monthly:
                if (skuDetailsMonthly != null)
                    initiateSubPurchase(skuDetailsMonthly);
                break;
            case R.id.btn_sub_yearly:
                if (skuDetailsMonthly != null)
                    initiateSubPurchase(skuDetailsYearly);
                break;
            case R.id.btn_restore_purchase:
                initiateRestorePurchase();
        }
    }

    private void initiateRestorePurchase() {
        if (mBillingProvider.isPremiumPurchased()) {
            String url;
            if(session.getProductId()!=null){
                url = "https://play.google.com/store/account/subscriptions?sku="
                        + session.getProductId() + "&package=" + session.getPackageName();
            } else {
                url = "https://play.google.com/store/account/subscriptions";
            }
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url)));
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(mainActivity).create();
            alertDialog.setTitle("Purchase Error");
            alertDialog.setMessage("Nothing to restore");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

    }

    private void initiateSubPurchase(SkuDetails skuDetails){
        double priceAmount = skuDetails.getPriceAmountMicros() / 1000000.0;
        session.setPriceAmountMicros(skuDetails.getPriceAmountMicros());
        session.setPriceCurrencyCode(skuDetails.getPriceCurrencyCode());

        if (skuDetails != null && mBillingProvider.isPremiumPurchased()) {
            showAlreadyPurchasedToast();
        }  else {
            logInitiateCheckoutEvent(skuDetails.getOriginalJson(), skuDetails.getSku(), skuDetails.getType(), true,
                    skuDetails.getPriceCurrencyCode(), priceAmount);
            assert skuDetails != null;
            mBillingProvider.getBillingManager().initiatePurchaseFlow(skuDetails);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnUpgradePremiumFragmentInteractionListener) {
            mListener = (OnUpgradePremiumFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStockFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Notifies the fragment/class that billing manager is ready and provides a BillingProviders
     * instance to access it
     */
    public void onManagerReady(BillingProvider billingProvider) {
        mBillingProvider = billingProvider;
//        if (mRecyclerView != null) {
//            handleManagerAndUiReady();
//        }
    }

    private void showAlreadyPurchasedToast() {
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;

        Context context = mBillingProvider.getBillingManager().getContext();
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), R.string.alert_already_purchased, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackground(activity.getResources().getDrawable(R.drawable.premium_gold_dialog));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(ContextCompat.getColor(context,R.color.white));
        snackbar.show();
    }


    /**
     * Executes query for SKU details at the background thread
     */
    public void handleManagerAndUiReady(MainActivity mainActivity) {

        mainActivityWeakReference = new WeakReference<MainActivity>(mainActivity);

        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;

        // If Billing Manager was successfully initialized - start querying for SKUs
//        setWaitScreen(true);
        querySkuDetails();
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
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        Log.d(TAG, "querySkuDetails() got subscriptions and inApp SKU details lists for: "
                + (System.currentTimeMillis() - startTime) + "ms");

        final List<SkuRowData> dataList = new ArrayList<>();
//            List<String> inAppSkus = getSkuList(BillingClient.SkuType.INAPP);
//            addSkuRows(dataList, inAppSkus, BillingClient.SkuType.INAPP, null);
        List<String> inAppSkus = getSkuList(BillingClient.SkuType.SUBS);
        addSkuRows(dataList, inAppSkus, BillingClient.SkuType.SUBS, null);
    }

    private void addSkuRows(final List<SkuRowData> inList, List<String> skusList,
                            final @BillingClient.SkuType String billingType, final Runnable executeWhenFinished) {
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
                        if (!isAdded()) {
                            return;
                        }
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
                                if (SKU_GOLD_MONTHLY.equals(sku)) {
                                    skuDetailsMonthly = skuDetails;
                                    String monthly_sub = activity.getResources().getString(R.string.subscribe_monthly);
                                    mBtnMonthSub.setText(String.format(monthly_sub, skuDetailsMonthly.getPrice()));
                                }
                                if (SKU_GOLD_YEARLY.equals(sku)) {
                                    skuDetailsYearly = skuDetails;
                                    String yearly_sub = activity.getResources().getString(R.string.subscribe_yearly);
                                    mBtnYearSub.setText(String.format(yearly_sub, skuDetailsYearly.getPrice()));
                                }
                            }

                            if (inList.size() == 0) {
                                displayAnErrorIfNeeded();
                            } else {

                                //                                show(sendDetails);
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

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void logInitiateCheckoutEvent (String contentData, String contentId, String contentType, boolean paymentInfoAvailable, String currency, double totalPrice) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
        params.putInt(AppEventsConstants.EVENT_PARAM_PAYMENT_INFO_AVAILABLE, paymentInfoAvailable ? 1 : 0);
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
        logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, totalPrice, params);
    }

    private void showSnackBar(View view, String message){
        MainActivity activity = mainActivityWeakReference.get();
        if (activity == null || activity.isFinishing())
            return;
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(activity,R.color.snackbar_err_color));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public interface OnUpgradePremiumFragmentInteractionListener{
        void onUpgradePremiumFragmentCreate(UpgradeFragment upgradeFragment);
    }
}
