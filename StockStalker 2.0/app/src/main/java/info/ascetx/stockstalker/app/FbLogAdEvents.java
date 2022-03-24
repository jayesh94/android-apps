package info.ascetx.stockstalker.app;

import android.content.Context;
import android.os.Bundle;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

public class FbLogAdEvents {
    private AppEventsLogger logger;

    public FbLogAdEvents(Context context) {
        logger = AppEventsLogger.newLogger(context);
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void logAdClickEvent (String adType) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_AD_TYPE, adType);
        logger.logEvent(AppEventsConstants.EVENT_NAME_AD_CLICK, params);
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void logAdImpressionEvent (String adType) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_AD_TYPE, adType);
        logger.logEvent(AppEventsConstants.EVENT_NAME_AD_IMPRESSION, params);
    }
}
