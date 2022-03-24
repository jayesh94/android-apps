package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase


object Logger {
    var isEnabled = qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.BuildConfig.ERROR_REPORTS_ENABLED_BY_DEFAULT

    fun log(error: Throwable) {
        Firebase.crashlytics.recordException(error)
    }

    fun setCrashlyticsCollection(value: Boolean) {
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(value)
    }
}