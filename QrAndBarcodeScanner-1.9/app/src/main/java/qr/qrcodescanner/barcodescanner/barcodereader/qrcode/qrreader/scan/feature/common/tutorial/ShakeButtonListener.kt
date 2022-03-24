package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.tutorial

import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.Button
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.BottomTabsActivity

class ShakeButtonListener(private val showcaseViewGuidedTutorial: ShowcaseViewGuidedTutorial, private val activity: BottomTabsActivity, private val nextButton: Button, private val step: Int) :

    SimpleShowcaseEventListener() {

    override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent) {

        val wobbleAnimation = AnimationUtils.loadAnimation(activity, R.anim.shake)
        nextButton.startAnimation(wobbleAnimation)

    }

    override fun onShowcaseViewShow(showcaseView: ShowcaseView) {
        when(step){
            1 -> activity.faLogEvents.logScreenViewEvent("ShowCaseStep1", "ShowCaseStep1")
            2 -> activity.faLogEvents.logScreenViewEvent("ShowCaseStep2", "ShowCaseStep2")
            3 -> activity.faLogEvents.logScreenViewEvent("ShowCaseStep3", "ShowCaseStep3")
            4 -> activity.faLogEvents.logScreenViewEvent("ShowCaseStep4", "ShowCaseStep4")
            5 -> activity.faLogEvents.logScreenViewEvent("ShowCaseStep5", "ShowCaseStep5")
        }
    }

    override fun onShowcaseViewDidHide(showcaseView: ShowcaseView) {

        if (activity.settings.appFirstLaunchIntroduction.not())
            return

        when (step){
            1 -> {
                showcaseViewGuidedTutorial.showShowCase2()
            }
            2 -> {
                showcaseViewGuidedTutorial.showShowCase3()
            }
            3 -> {
                showcaseViewGuidedTutorial.showShowCase4()
            }
            4 -> {
                showcaseViewGuidedTutorial.showShowCase5()
            }
            5 -> {
                activity.settings.appFirstLaunchIntroduction = false
            }
        }
    }

}