package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan

import androidx.multidex.MultiDexApplication
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Logger
import io.reactivex.plugins.RxJavaPlugins

class   App : MultiDexApplication() {

    override fun onCreate() {
        handleUnhandledRxJavaErrors()
        applyTheme()
        super.onCreate()
    }

    private fun applyTheme() {
        settings.reapplyTheme()
    }

    private fun handleUnhandledRxJavaErrors() {
        RxJavaPlugins.setErrorHandler { error ->
            Logger.log(error)
        }
    }
}