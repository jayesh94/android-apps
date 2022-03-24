package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.tutorial

import android.graphics.Point
import android.util.Log
import com.github.amlcurran.showcaseview.targets.Target

class ViewTargetPoint(
    screenWidth: Int,
    screenHeight: Int,
    navItemPosition: Int,
    midPointOfBottomNav: Int,
    softNavigationBarHeight: Int,
    viewTargetBuffer: Int
) : Target {

    var x: Int = 0
    var y: Int = 0

    init {
        Log.e("TAG", "screenWidth px = $screenWidth")
        Log.e("TAG", "screenHeight px = $screenHeight")
        Log.e("TAG", "midPointOfBottomNav = $midPointOfBottomNav")
        Log.e("TAG", "softNavigationBarHeight = $softNavigationBarHeight")
        Log.e("TAG", "viewTargetBuffer = $viewTargetBuffer")

        x = (screenWidth / 10) + ((screenWidth / 5) * navItemPosition)
        y = screenHeight - midPointOfBottomNav - softNavigationBarHeight + viewTargetBuffer

    }

    override fun getPoint(): Point {
        Log.e("TAG", "Point X = $x")
        Log.e("TAG", "Point Y = $y")
        return Point(x, y)
    }
}