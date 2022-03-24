package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.rotationHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.SetStatusBar

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        rotationHelper.lockCurrentOrientationIfNeeded(this)
        SetStatusBar(this)
    }
}