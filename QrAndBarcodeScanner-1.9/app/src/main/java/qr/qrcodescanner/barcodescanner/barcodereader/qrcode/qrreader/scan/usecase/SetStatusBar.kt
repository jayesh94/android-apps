package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R

class SetStatusBar(val activity: Activity) {

    init {
        setStatusBarColor(activity)
    }

    private fun setStatusBarColor(activity: Activity) {
        val window: Window = activity.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(activity, R.color.new_primary_color)
//        window.navigationBarColor = ContextCompat.getColor(activity, R.color.new_secondary_color)
// may set navigation bar color in future. Currently it looks shitty and it can also prompt users to click on those buttons and move out of app
    }
}