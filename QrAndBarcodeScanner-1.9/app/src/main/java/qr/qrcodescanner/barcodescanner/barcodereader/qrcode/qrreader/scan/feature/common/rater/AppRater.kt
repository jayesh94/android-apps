package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater

import android.app.Activity
import android.util.Log
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.configs.RemoteConfigs
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.RatingDialog

class AppRater(val activity: Activity) {

//    private lateinit var remoteConfig: FirebaseRemoteConfig

    private val inAppRaterParams = RemoteConfigs.inAppRaterConfigs()

    fun isShowAppRater(): Boolean {

//        val remoteConfigsDataClass: RemoteConfigsDataClass = getRemoteConfigs()

        Log.e("TAG AppRater", "daysUntilPrompt: \n" + inAppRaterParams.appRater.daysUntilPrompt)
        Log.e("TAG AppRater", "appLaunchesUntilPrompt: \n" + inAppRaterParams.appRater.appLaunchesUntilPrompt)
        Log.e("TAG AppRater", "daysUntilNextPrompt: \n" + inAppRaterParams.appRater.daysUntilNextPrompt)

        val DAYS_UNTIL_PROMPT = inAppRaterParams.appRater.daysUntilPrompt //Min 5 number of days. Only valid for first time users
        val APP_LAUNCHES_UNTIL_PROMPT = inAppRaterParams.appRater.appLaunchesUntilPrompt //Min 4 number of launches
        val DAYS_UNTIL_NEXT_PROMPT = inAppRaterParams.appRater.daysUntilNextPrompt //Min 3 number of days after review ask

        val appFirstLaunchDateAppRater = activity.settings.appFirstLaunchDateAppRater // One Time Use

        val appRaterLastLaunchDate = activity.settings.appRaterLastLaunchDate
        val appLaunchCountAppRater = activity.settings.appLaunchCountAppRater
        val appRaterLaunchDaysExtension = activity.settings.appRaterLaunchDaysExtension

        Log.e("TAG AppRater", "appLaunchCountAppRater: $appLaunchCountAppRater")
        Log.e("TAG AppRater", "appRaterLaunchDaysExtension: $appRaterLaunchDaysExtension")

        Log.e("TAG AppRater", "appFirstLaunchDateAppRater: $appFirstLaunchDateAppRater")
        Log.e("TAG AppRater", "appRaterLastLaunchDate: $appRaterLastLaunchDate")

        return if (appLaunchCountAppRater >= APP_LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= appFirstLaunchDateAppRater + getDateInMilliseconds(DAYS_UNTIL_PROMPT)
            ) {
                appRaterLastLaunchDate == 0L || System.currentTimeMillis() >= (appRaterLastLaunchDate +
                        getDateInMilliseconds(DAYS_UNTIL_NEXT_PROMPT) + getDateInMilliseconds(appRaterLaunchDaysExtension))
            } else {
                false
            }
        } else {
            false
        }
    }

    fun showAppRater(autoRate: Boolean = false) {

        if (activity.isFinishing) return

        Log.e("TAG AppRater", "mayBeLaterExtensionDays: \n" + inAppRaterParams.appRater.mayBeLaterExtensionDays)
        Log.e("TAG AppRater", "cancelExtensionDays: \n" + inAppRaterParams.appRater.cancelExtensionDays)
        Log.e("TAG AppRater", "submitFormExtensionDays: \n" + inAppRaterParams.appRater.submitFormExtensionDays)
        Log.e("TAG AppRater", "openPlayStoreExtensionDays: \n" + inAppRaterParams.appRater.openPlayStoreExtensionDays)
        Log.e("TAG AppRater", "playStoreRatingThreshold: \n" + inAppRaterParams.appRater.playStoreRatingThreshold)

        val mayBeLaterExtensionDays = inAppRaterParams.appRater.mayBeLaterExtensionDays
        val cancelExtensionDays = inAppRaterParams.appRater.cancelExtensionDays
        val submitFormExtensionDays = inAppRaterParams.appRater.submitFormExtensionDays
        val openPlayStoreExtensionDays = inAppRaterParams.appRater.openPlayStoreExtensionDays

        val ratingDialog: RatingDialog = RatingDialog.Builder(activity)
            .threshold(inAppRaterParams.appRater.playStoreRatingThreshold)
            .title(activity.getString(R.string.review_app_title))
            .msg(activity.getString(R.string.review_app_msg))
            .positiveButtonText(activity.getString(R.string.rating_dialog_maybe_later))
            .negativeButtonText(activity.getString(R.string.rating_dialog_never))
            .formTitle(activity.getString(R.string.rating_dialog_feedback_title))
            .formSubmitText(activity.getString(R.string.rating_dialog_submit))
            .formCancelText(activity.getString(R.string.rating_dialog_cancel))
            .formHint(activity.getString(R.string.rating_dialog_suggestions))
            .noPlayStoreError(activity.getString(R.string.rating_dialog_no_playstore_error))
            .ratingBarColor(R.color.rate_star)
            .playstoreUrl("https://play.google.com/store/apps/details?id=" + activity.packageName)
            .onRatingChanged(object : RatingDialog.Builder.RatingDialogListener {
                override fun onRatingSelected(rating: Float, thresholdCleared: Boolean) {}
            })
            .onRatingDialogCancelled(object : RatingDialog.Builder.RatingDialogCancelListener {
                override fun onRatingDialogCancelled() {
                    Log.e("TAG AppRater", "onRatingDialogCancelled")
                    activity.faLogEvents.logCustomButtonClickEvent("rating_dialog_cancel")

                    if (autoRate) activity.settings.appRaterLaunchDaysExtension = cancelExtensionDays
                }
            })
            .onRatingDialogMayBeLaterClicked(object: RatingDialog.Builder.RatingDialogMayBeLaterListener{
                override fun onRatingDialogMayBeLaterClicked() {
                    Log.e("TAG AppRater", "onRatingDialogMayBeLaterClicked")
                    activity.faLogEvents.logCustomButtonClickEvent("rating_dialog_maybe_later")
                    if (autoRate) activity.settings.appRaterLaunchDaysExtension = mayBeLaterExtensionDays
                }
            })
            .onRatingThresholdClearedLog(object: RatingDialog.Builder.RatingThresholdClearedLogListener {
                override fun onRatingThresholdClearedLog() {
                    Log.e("TAG AppRater", "onRatingThresholdClearedLog")
                    activity.faLogEvents.logCustomButtonClickEvent("rating_dialog_open_playstore")
                    if (autoRate) activity.settings.appRaterLaunchDaysExtension = openPlayStoreExtensionDays
                }
            })
            .onRatingBarFormSumbit(object : RatingDialog.Builder.RatingDialogFormListener {
                override fun onFormSubmitted(feedback: String?) {
                    Log.e("TAG AppRater", "onFormSubmitted")
                    activity.faLogEvents.logCustomButtonClickEvent("rating_dialog_submit_feedback")
                    if (autoRate) activity.settings.appRaterLaunchDaysExtension = submitFormExtensionDays
                    SendFeedback(activity, feedback)
                }
            })
            .onRatingDialogShown(object: RatingDialog.Builder.RatingDialogShownListener {
                override fun onRatingDialogShown() {
                    Log.e("TAG AppRater", "NewRatingDialog Shown")

                    if (autoRate) {
                        activity.settings.appRaterLastLaunchDate = System.currentTimeMillis()
                        activity.settings.appLaunchCountAppRater = 0
                        activity.faLogEvents.logScreenViewEvent("AutoShownNewRatingDialog", "AutoShownNewRatingDialog")
                    } else {
                        activity.faLogEvents.logScreenViewEvent("NewRatingDialog", "NewRatingDialog")
                    }
                }
            })
            .build()

        ratingDialog.show()
    }

    private fun getDateInMilliseconds(days: Int) : Long {
        return (days * 24 * 60 * 60 * 1000).toLong()
    }
}