package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.tutorial

import android.graphics.Point
import androidx.annotation.IdRes
import com.github.amlcurran.showcaseview.targets.Target
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationItemTarget(private val bottomNavigationView: BottomNavigationView, @IdRes val menuItemId: Int) : Target {

    override fun getPoint(): Point {
        return ViewTarget(bottomNavigationView.findViewById(menuItemId)).point
    }

}