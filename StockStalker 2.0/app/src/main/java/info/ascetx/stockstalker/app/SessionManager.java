package info.ascetx.stockstalker.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by JAYESH on 18-03-2017.
 */

public class SessionManager {
    // LogCat tag
    private static String TAG = "SessionManager";

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "LoginSharedPref";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_FIRST_LOGIN_REG = "firstLoginReg";

    private static final String KEY_SORT_TOGGLE = "sortToggle";

    private static final String KEY_THEME = "theme";

    private static final String KEY_SORT_BY = "sortBy";

    private static final String KEY_SKIP_USER = "skipUser";

    private static final String KEY_STOCK_NAME = "stockName";

    private static final String KEY_STOCK_PERIOD = "stockPeriod";

    private static final String KEY_CHART_PERIOD = "chartPeriod";

    private static final String KEY_CHART_TYPE = "chartType";

    private static final String KEY_PREMIUM_USER = "premiumUser";

    private static final String KEY_PRODUCT_ID = "productId";
    private static final String KEY_PRICE_AMOUNT_MICROS = "priceAmountMicros";
    private static final String KEY_PRICE_CURRENCY_CODE = "priceCurrencyCode";

    private static final String KEY_PACKAGE_NAME = "packageName";

    private static final String KEY_PURCH_DONT_SHOW_AGAIN = "purchDontShowAgain";

    private static final String KEY_PURCH_LAUNCH_COUNT = "purchLaunchCount";

    private static final String KEY_PURCH_DATE_FIRST_LAUNCH = "purchDateFirstLaunch";

    private static final String KEY_PURCHASE_ACKNOWLEDGED = "userPurchaseAcknowledged";

    private static final String KEY_API_VER = "apiVer";
    private static final String KEY_BASE_URL = "baseUrl";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

//        Log.d(TAG, "User login session modified!");
    }

    public void setFirstLoginReg(boolean firstLoginReg){
        editor.putBoolean(KEY_FIRST_LOGIN_REG , firstLoginReg);
        editor.commit();
    }

    public void setToggle(boolean toggle){
        editor.putBoolean(KEY_SORT_TOGGLE , toggle);
        editor.commit();
    }

    public void setSortBy(String sortBy){
        editor.putString(KEY_SORT_BY , sortBy);
        editor.commit();
    }

    public void setSkip(boolean skip){
        editor.putBoolean(KEY_SKIP_USER , skip);
        editor.commit();
    }

    public void setTheme(int theme){
        editor.putInt(KEY_THEME , theme);
        editor.commit();
    }

    public void setStockName(String stockName){
        editor.putString(KEY_STOCK_NAME , stockName);
        editor.commit();
    }

    public void setStockPeriod(String stockPeriod){
        editor.putString(KEY_STOCK_PERIOD , stockPeriod);
        editor.commit();
    }

    public void setChartType(int chartType){
        editor.putInt(KEY_CHART_TYPE , chartType);
        editor.commit();
    }

    public void setChartPeriod(int chartPeriod){
        editor.putInt(KEY_CHART_PERIOD , chartPeriod);
        editor.commit();
    }

    public void setPremiumUser(boolean premiumUser){
        editor.putBoolean(KEY_PREMIUM_USER , premiumUser);
        editor.commit();
    }

    public void setPurchDontShowAgain(boolean premiumUser){
        editor.putBoolean(KEY_PURCH_DONT_SHOW_AGAIN , premiumUser);
        editor.commit();
    }

    public void setPurchLaunchCount(long launch_count){
        editor.putLong(KEY_PURCH_LAUNCH_COUNT , launch_count);
        editor.commit();
    }

    public void setPurchDateFirstLaunch(long date_firstLaunch){
        editor.putLong(KEY_PURCH_DATE_FIRST_LAUNCH , date_firstLaunch);
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

    public void setPackageName(String packageName){
        editor.putString(KEY_PACKAGE_NAME , packageName);
        editor.commit();
    }

    public void setProductId(String productId){
        editor.putString(KEY_PRODUCT_ID , productId);
        editor.commit();
    }

    public void setPurchaseAcknowledged(boolean userPurchaseAcknowledged){
        editor.putBoolean(KEY_PURCHASE_ACKNOWLEDGED , userPurchaseAcknowledged);
        editor.commit();
    }

    public void setBaseUrl(String baseUrl){
        editor.putString(KEY_BASE_URL , baseUrl);
        editor.commit();
    }

    public void setApiVer(int apiVer){
        editor.putInt(KEY_API_VER , apiVer);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public boolean isFirstLoginReg(){
        return pref.getBoolean(KEY_FIRST_LOGIN_REG, false);
    }

    public boolean isToggle(){
        return pref.getBoolean(KEY_SORT_TOGGLE, true);
    }

    public String isSortBy(){
        return pref.getString(KEY_SORT_BY, "nsq_stock_id");
    }

    public boolean isSkip(){
        return pref.getBoolean(KEY_SKIP_USER, true);
    }

    public boolean isPremiumUser(){
        return pref.getBoolean(KEY_PREMIUM_USER, false);
    }

    public boolean isPurchDontShowAgain(){
        return pref.getBoolean(KEY_PURCH_DONT_SHOW_AGAIN, false);
    }

    public boolean isPurchaseAcknowledged(){
        return pref.getBoolean(KEY_PURCHASE_ACKNOWLEDGED, false);
    }

    public int getTheme(){
        return pref.getInt(KEY_THEME, 0);
    }

    public String getStockName(){
        return pref.getString(KEY_STOCK_NAME, null);
    }

    public String getStockPeriod(){
        return pref.getString(KEY_STOCK_PERIOD, null);
    }
    public int getChartType(){
        return pref.getInt(KEY_CHART_TYPE, 0);
    }

    public int getChartPeriod(){
        return pref.getInt(KEY_CHART_PERIOD, 0);
    }
    public long getPurchLaunchCount(){
        return pref.getLong(KEY_PURCH_LAUNCH_COUNT, 0);
    }

    public long getPurchDateFirstLaunch(){
        return pref.getLong(KEY_PURCH_DATE_FIRST_LAUNCH, 0);
    }

    public String getProductId(){
        return pref.getString(KEY_PRODUCT_ID, null);
    }
    public String getPackageName(){
        return pref.getString(KEY_PACKAGE_NAME, null);
    }

    public String getPriceCurrencyCode(){
        return pref.getString(KEY_PRICE_CURRENCY_CODE, null);
    }

    public long getPriceAmountMicros(){
        return pref.getLong(KEY_PRICE_AMOUNT_MICROS, 0);
    }

    public String getBaseUrl(){
        return pref.getString(KEY_BASE_URL, null);
    }

    public int getApiVer(){
        return pref.getInt(KEY_API_VER, 0);
    }

}
