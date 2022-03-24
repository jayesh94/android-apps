package info.ascetx.stockstalker.app;

/**
 * Created by JAYESH on 07-03-2017.
 */
public class Config {
    //Config: Constant variables used across the app

    // global topic to receive app wide push notifications
    public static final String TOPIC_NEWS = "news";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "aha_firebase";
    public static final String SHARED_PREF_MSG = "firebase_msgs";

    public static String LOGGED_OR_SKIP ;

    public static String URL_VERIFY_SOURCE_GET_RESOURCE = "https://ascetx.com/stockStalker/ssu/app/login.php";

//    LoginActivity Urls
    public static final String URL_LOGIN = "http://jaeznet.pe.hu/ssu/user/login.php";
    static final String URL_LOGOUT = "http://jaeznet.pe.hu/ssu/user/logout.php";

//    Url check Current Version
    static final String URL_CURRENT_VERSION = "https://ascetx.com/stockStalker/ssu/app/ver.php";

//    Verify signature of purchase
    public static final String URL_CURRENT_VERIFY_SIGNATURE = "https://ascetx.com/stockStalker/ssu/app/verSign.php?data=";

//    User related urls
    public static final String URL_FORGOT_PASS = "http://jaeznet.pe.hu/ssu/user/fpusercheck.php";
    public static final String URL_UPDATE_REG_ID = "http://jaeznet.pe.hu/ssu/user/updateRegID.php";

//    Nasdaq Stocks related Urls
    public static final String URL_GET_US_NSQ_STOCKS = "https://ascetx.com/stockStalker/ssu/app/getUSNsqStocks.php";

//    Nasdaq User details related Urls
    static final String URL_SYNC_NSQ_USER_DETAILS = "https://ascetx.com/stockStalker/ssu/user/syncUserDetailsStocks.php";

//    Nasdaq User Stocks related Urls
    static final String URL_AR_NSQ_USER_STOCK = "https://ascetx.com/stockStalker/ssu/user/userStockAddRemove.php";

//    Nasdaq User Log Out url
    static final String URL_NSQ_USER_LOG_OUT = "https://ascetx.com/stockStalker/ssu/user/userLogOut.php";

//    Nasdaq User Purchased Subscription
    public static final String URL_NSQ_USER_SUBSCRIBED = "https://ascetx.com/stockStalker/ssu/user/userSubscribed.php";

    public static final String URL_PRIVACY_POLICY = "https://ascetx.com/privacy-policy/";
    public static final String URL_TERMS_CONDITIONS = "https://ascetx.com/terms-conditions/";

    public static final String URL_GET_NASDAQ_STOCKS_DETAILS = "http://finance.google.com/finance/getprices?x=NASDAQ&i=60&p=2d&";
    public static final String URL_GET_NASDAQ_STOCKS_INTRADAY_DETAILS = "https://finance.google.com/finance/getprices?x=NASDAQ&p=1d&";
    public static final String URL_GET_NASDAQ_STOCKS_CHART_DETAILS = "https://finance.google.com/finance/getprices?x=NASDAQ&";

    public static final String URL_GET_NSQ_STOCK_INTRA_CHART_DETAILS = "%sstock/%s/intraday-prices?%s";
//    public static final String URL_GET_NSQ_STOCK_HISTORIC_CHART_DETAILS = "%sstock/%s/chart/%s?%s";
    public static final String URL_GET_NSQ_STOCK_HISTORIC_CHART_DETAILS = "https://ascetx.com/stockStalker/ssu/stock_data/getStockDetails.php?stock=%s&period=%s";
    public static final String URL_GET_NSQ_STOCK_1DM_CHART_DETAILS = "%sstock/%s/chart/dynamic?%s";
    public static final String URL_GET_NSQ_STOCK_QUOTE = "%sstock/%s/quote?%s";
    public static final String URL_GET_NSQ_STOCK_PROFILE = "%sstock/%s/company?%s";
    public static final String URL_GET_NSQ_STOCK_NEWS = "%sstock/%s/news/last/15?%s";

    public static final String URL_GET_NSQ_BATCH_STOCK_DETAILS = "%sstock/market/batch?types=quote&symbols=%s&%s";

    public static final String URL_GET_NSQ_TOP_GAINERS = "%sstock/market/list/gainers?%s";
    public static final String URL_GET_NSQ_TOP_LOSERS = "%sstock/market/list/losers?%s";
    public static final String URL_GET_NSQ_TOP_MOVERS = "%sstock/market/list/mostactive?%s";

//  RegisterActivity Urls
    public static final String URL_REGISTER = "http://jaeznet.pe.hu/ssu/user/register.php";

    //  Test Smart Banner Ad Unit Ids
//    public static final String MAIN_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String STOCK_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String NEWS_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String INTRADAY_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String HISTORICAL_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String TGLM_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String MOSTACTIVE_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String STOCK_PROFILE_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
//    public static final String STOCK_FRAGMENT_INTER_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    //  Prod Smart Banner Ad Unit Ids
    public static final String MAIN_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/9488121179";
    public static final String STOCK_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/3469507737";
    public static final String NEWS_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/9375285239";
    public static final String INTRADAY_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/3417536846";
    public static final String HISTORICAL_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/6717653458";
    public static final String TGLM_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/9064606420";
    public static final String MOSTACTIVE_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/1700683307";
    public static final String STOCK_PROFILE_FRAGMENT_AD_UNIT_ID = "ca-app-pub-3752168151808074/9677986491";
    public static final String STOCK_FRAGMENT_INTER_AD_UNIT_ID = "ca-app-pub-3752168151808074/2746431155";

}
