package info.ascetx.flashlight.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by JAYESH on 18-03-2017.
 */

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "LoginSharedPref";

    private static final String KEY_PREMIUM_USER = "premiumUser";

    private static final String KEY_DATE_TIME = "adDateTime";

    private static final String KEY_PURCHASE_ACKNOWLEDGED = "userPurchaseAcknowledged";

    private static final String KEY_PRICE_AMOUNT_MICROS = "priceAmountMicros";
    private static final String KEY_PRICE_CURRENCY_CODE = "priceCurrencyCode";

    private static final String KEY_SHOW_INTERSTITIAL_AD = "showInterstitialAd";
    private static final String KEY_INTERSTITIAL_PROMPT_TIME = "timeInterstitialAdPrompt";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setPremiumUser(boolean premiumUser){
        editor.putBoolean(KEY_PREMIUM_USER , premiumUser);
        editor.commit();
    }

   public void setPurchaseAcknowledged(boolean userPurchaseAcknowledged){
        editor.putBoolean(KEY_PURCHASE_ACKNOWLEDGED , userPurchaseAcknowledged);
        editor.commit();
    }

    public void setRewardAdDateTime(Date date){
        long milliseconds = date.getTime();
        editor.putLong(KEY_DATE_TIME , milliseconds);
        editor.commit();
    }

    public void setPriceAmountMicros(long priceAmount){
        editor.putLong(KEY_PRICE_AMOUNT_MICROS , priceAmount);
        editor.commit();
    }

    public void setPriceCurrencyCode(String currencyCode){
        editor.putString(KEY_PRICE_CURRENCY_CODE , currencyCode);
        editor.commit();
    }

    public void setShowInterstitialAd(boolean show) {
        editor.putBoolean(KEY_SHOW_INTERSTITIAL_AD , show);
        editor.commit();
    }

    public void setInterstitialPromptTime(int promptTime) {
        editor.putInt(KEY_INTERSTITIAL_PROMPT_TIME , promptTime);
        editor.commit();
    }

    public boolean getShowInterstitialAd() {
        return pref.getBoolean(KEY_SHOW_INTERSTITIAL_AD, true);
    }

    public int getInterstitialPromptTime() {
        return pref.getInt(KEY_INTERSTITIAL_PROMPT_TIME, 35); //Seconds after which int Ad should be shown after int ad close or app launch
    }

    public Date getRewardAdDateTime(){
        return new Date(pref.getLong(KEY_DATE_TIME, 0));
    }

    public boolean isPremiumUser(){
        return pref.getBoolean(KEY_PREMIUM_USER, false);
    }

    public boolean isPurchaseAcknowledged(){
        return pref.getBoolean(KEY_PURCHASE_ACKNOWLEDGED, false);
    }

    public String getPriceCurrencyCode(){
        return pref.getString(KEY_PRICE_CURRENCY_CODE, null);
    }

    public long getPriceAmountMicros(){
        return pref.getLong(KEY_PRICE_AMOUNT_MICROS, 0);
    }

}
