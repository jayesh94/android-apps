package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.tutorial

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.databinding.ActivityBottomTabs2Binding
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.BottomTabsActivity
import kotlin.math.roundToInt


class ShowcaseViewGuidedTutorial() {

//    private lateinit var targetPoint1: ViewTargetPoint
//    private lateinit var targetPoint2: ViewTargetPoint
//    private lateinit var targetPoint3: ViewTargetPoint
//    private lateinit var targetPoint4: ViewTargetPoint

    private var viewTargetBuffer: Int = 0
    private var screenHeight: Int = 0
    private var midPointOfBottomNav: Int = 0

    private lateinit var binding: ActivityBottomTabs2Binding
    lateinit var bottomTabsActivity: BottomTabsActivity

    private val showcaseViewStyle: Int = R.style.HoloShowcaseTheme

    private var titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var descriptionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    constructor (screenWidth: Int, screenHeight: Int, midPointOfBottomNav: Int, softNavigationBarHeight: Int,
                 viewTargetBuffer: Int, binding: ActivityBottomTabs2Binding, bottomTabsActivity: BottomTabsActivity) : this() {

        Log.e("TAG Tutorial", "width px = $screenWidth")
        Log.e("TAG Tutorial", "height px = $screenHeight")
        Log.e("TAG Tutorial", "midPointOfBottomNav = $midPointOfBottomNav")
        Log.e("TAG Tutorial", "softNavigationBarHeight = $softNavigationBarHeight")
        Log.e("TAG Tutorial", "viewTargetBuffer = $viewTargetBuffer")

        this.binding = binding
        this.bottomTabsActivity = bottomTabsActivity
        this.viewTargetBuffer = viewTargetBuffer
        this.screenHeight = screenHeight
        this.midPointOfBottomNav = midPointOfBottomNav

//        targetPoint1 = ViewTargetPoint(screenWidth, screenHeight,0, midPointOfBottomNav, softNavigationBarHeight, viewTargetBuffer)
//        targetPoint2 = ViewTargetPoint(screenWidth, screenHeight,1, midPointOfBottomNav, softNavigationBarHeight, viewTargetBuffer)
//        targetPoint3 = ViewTargetPoint(screenWidth, screenHeight,3, midPointOfBottomNav, softNavigationBarHeight, viewTargetBuffer)
//        targetPoint4 = ViewTargetPoint(screenWidth, screenHeight,4, midPointOfBottomNav, softNavigationBarHeight, viewTargetBuffer)

        titlePaint.color = bottomTabsActivity.resources.getColor(R.color.sv_title)
        titlePaint.textSize = bottomTabsActivity.resources.getDimension(R.dimen.default_text_size_very_large)
        titlePaint.typeface = ResourcesCompat.getFont(bottomTabsActivity, R.font.open_sans_bold)

        descriptionPaint.color = bottomTabsActivity.resources.getColor(R.color.sv_description)
        descriptionPaint.textSize = bottomTabsActivity.resources.getDimension(R.dimen.default_text_size_large)
        descriptionPaint.typeface = ResourcesCompat.getFont(bottomTabsActivity, R.font.open_sans_italic)

        showGuidedTutorial()
    }

    private fun showGuidedTutorial() {

        val step = 1
        val imageView: ImageView = getTutorialImage()
        val nextButton = getNextButton()

        val showcaseView = ShowcaseView.Builder(bottomTabsActivity)
            .withHoloShowcase()
            .setStyle(showcaseViewStyle)
            .replaceEndButton(nextButton)
            .setContentTextPaint(descriptionPaint)
            .setContentTitlePaint(titlePaint)
            .setTarget(ViewTarget(binding.fab as View))
            .setContentTitle(getStringFromRId(R.string.tutorial_step1_title))
            .setContentText(getStringFromRId(R.string.tutorial_step1_detail))
            .setShowcaseEventListener(ShakeButtonListener(this, bottomTabsActivity, nextButton, step) )
            .setParent(binding.activityBottomTabs2CoordinatorLayout,binding.activityBottomTabs2CoordinatorLayout.childCount)
            .build()

        setShowcaseViewLayoutParams(showcaseView)

        showcaseView.forceTextPosition(ShowcaseView.ABOVE_SHOWCASE)

        showcaseView.setButtonPosition(
            getNextButtonLayoutParams(step))

        showcaseView.addView(imageView)

        getSkipButton(showcaseView, step) { skipButton ->

            showcaseView.addView(skipButton)

            skipButton?.setOnClickListener {
                setFirstLaunchFalse(bottomTabsActivity)
                showcaseView.hide()
                Log.e("TAG Tutorial", "Skipped step 1")
                bottomTabsActivity.faLogEvents.logCustomButtonClickEvent("tutorial_skip_step1")
            }
        }
    }

    fun showShowCase2() {
        val step = 2
        val imageView: ImageView = getTutorialImage()
        val nextButton = getNextButton()

        val showcaseView = ShowcaseView.Builder(bottomTabsActivity)
            .withHoloShowcase()
            .setStyle(showcaseViewStyle)
            .setTarget(BottomNavigationItemTarget(binding.bottomNavigationView, R.id.item_scan))
            .replaceEndButton(nextButton)
            .setContentTextPaint(descriptionPaint)
            .setContentTitlePaint(titlePaint)
            .setContentTitle(getStringFromRId(R.string.tutorial_step2_title))
            .setContentText(getStringFromRId(R.string.tutorial_step2_detail))
            .setShowcaseEventListener(ShakeButtonListener(this, bottomTabsActivity, nextButton, step))
            .setParent(binding.activityBottomTabs2CoordinatorLayout,binding.activityBottomTabs2CoordinatorLayout.childCount)
            .build()

        setShowcaseViewLayoutParams(showcaseView)

        showcaseView.setButtonPosition(getNextButtonLayoutParams(step))
        showcaseView.addView(imageView)
        getSkipButton(showcaseView, step) { skipButton ->
            showcaseView.addView(skipButton)
            skipButton?.setOnClickListener {
                setFirstLaunchFalse(bottomTabsActivity)
                showcaseView.hide()
                Log.e("TAG Tutorial", "Skipped step 2")
                bottomTabsActivity.faLogEvents.logCustomButtonClickEvent("tutorial_skip_step2")
            }
        }
    }

    fun showShowCase3() {

        val step = 3
        val nextButton = getNextButton()

        val showcaseView = ShowcaseView.Builder(bottomTabsActivity)
            .withHoloShowcase()
            .setStyle(showcaseViewStyle)
            .setTarget(BottomNavigationItemTarget(binding.bottomNavigationView, R.id.item_create))
            .replaceEndButton(nextButton)
            .setContentTextPaint(descriptionPaint)
            .setContentTitlePaint(titlePaint)
            .setContentTitle(getStringFromRId(R.string.tutorial_step3_title))
            .setContentText(getStringFromRId(R.string.tutorial_step3_detail))
            .setShowcaseEventListener(ShakeButtonListener(this, bottomTabsActivity, nextButton, step))
            .setParent(binding.activityBottomTabs2CoordinatorLayout,binding.activityBottomTabs2CoordinatorLayout.childCount)
            .build()

        setShowcaseViewLayoutParams(showcaseView)

        showcaseView.setButtonPosition(getNextButtonLayoutParams(step))

        getSkipButton(showcaseView, step) { skipButton ->
            showcaseView.addView(skipButton)
            skipButton?.setOnClickListener {
                setFirstLaunchFalse(bottomTabsActivity)
                showcaseView.hide()
                Log.e("TAG Tutorial", "Skipped step 3")
                bottomTabsActivity.faLogEvents.logCustomButtonClickEvent("tutorial_skip_step3")
            }
        }
    }

    fun showShowCase4() {
        val step = 4
        val nextButton = getNextButton()

        val showcaseView = ShowcaseView.Builder(bottomTabsActivity)
            .withHoloShowcase()
            .setStyle(showcaseViewStyle)
            .replaceEndButton(nextButton)
            .setContentTextPaint(descriptionPaint)
            .setContentTitlePaint(titlePaint)
            .setTarget(BottomNavigationItemTarget(binding.bottomNavigationView, R.id.item_history))
            .setContentTitle(getStringFromRId(R.string.tutorial_step4_title))
            .setContentText(getStringFromRId(R.string.tutorial_step4_detail))
            .setShowcaseEventListener(ShakeButtonListener(this, bottomTabsActivity, nextButton, step))
            .setParent(binding.activityBottomTabs2CoordinatorLayout,binding.activityBottomTabs2CoordinatorLayout.childCount)
            .build()

        setShowcaseViewLayoutParams(showcaseView)

        showcaseView.setButtonPosition(getNextButtonLayoutParams(step))

        getSkipButton(showcaseView, step) { skipButton ->
            showcaseView.addView(skipButton)
            skipButton?.setOnClickListener {
                setFirstLaunchFalse(bottomTabsActivity)
                showcaseView.hide()
                Log.e("TAG Tutorial", "Skipped step 4")
                bottomTabsActivity.faLogEvents.logCustomButtonClickEvent("tutorial_skip_step4")
            }
        }
    }

    fun showShowCase5() {

        val step = 5
        val nextButton = getNextButton()
        nextButton.text = getStringFromRId(R.string.tutorial_done_button)

        val showcaseView = ShowcaseView.Builder(bottomTabsActivity)
            .withHoloShowcase()
            .setStyle(showcaseViewStyle)
            .replaceEndButton(nextButton)
            .setContentTextPaint(descriptionPaint)
            .setContentTitlePaint(titlePaint)
            .setTarget(BottomNavigationItemTarget(binding.bottomNavigationView, R.id.item_settings))
            .setContentTitle(getStringFromRId(R.string.tutorial_step5_title))
            .setContentText(getStringFromRId(R.string.tutorial_step5_detail))
            .setShowcaseEventListener(ShakeButtonListener(this, bottomTabsActivity, nextButton, step))
            .setParent(binding.activityBottomTabs2CoordinatorLayout,binding.activityBottomTabs2CoordinatorLayout.childCount)
            .build()

        showcaseView.forceTextPosition(ShowcaseView.ABOVE_SHOWCASE)

        setShowcaseViewLayoutParams(showcaseView)

        showcaseView.setButtonPosition(getNextButtonLayoutParams(step))
    }

    private fun getStringFromRId(stringResourceId: Int): String {
        return bottomTabsActivity.resources.getString(stringResourceId)
    }

    private fun setShowcaseViewLayoutParams(showcaseView: ShowcaseView?) {

        val lps = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT )

        Log.e("TAG Tutorial", "showcaseView?.fitsSystemWindows: " + showcaseView?.fitsSystemWindows)
        Log.e("TAG Tutorial", "showcaseView?.childCount: " + showcaseView?.childCount)

        showcaseView?.layoutParams = lps
    }

    private fun setFirstLaunchFalse(bottomTabsActivity: BottomTabsActivity) {
        bottomTabsActivity.settings.appFirstLaunchIntroduction = false
    }

    private fun getTutorialImage(): ImageView {

        val imageViewWidthHeight = screenHeight / 3
        val imageViewMarginBottom: Int = (screenHeight / 3.5).roundToInt()

        val imageView = ImageView(bottomTabsActivity)

        val image = AppCompatResources.getDrawable(imageView.context, R.drawable.tutorial_image)
        imageView.setImageDrawable(image)

        val lps = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        lps.addRule(RelativeLayout.CENTER_HORIZONTAL)

        lps.setMargins(0, 0, 0, imageViewMarginBottom)

        lps.width = imageViewWidthHeight
        lps.height = imageViewWidthHeight

        imageView.layoutParams = lps

        return imageView
    }

    private fun getNextButton(): Button {
        val inflater = LayoutInflater.from(bottomTabsActivity)

        val nextButton: Button =
            inflater.inflate(R.layout.showcase_view_custom_next_button, null) as Button

        return nextButton
    }

    private fun getNextButtonLayoutParams(step: Int): RelativeLayout.LayoutParams {
        val lps = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT )
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        when (step){
            4 -> lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            5 -> lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            else -> lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        }

        val margin = ((bottomTabsActivity.resources.displayMetrics.density * 16) as Number).toInt()
        lps.setMargins(margin, margin, margin, margin)

        return lps
    }

    private fun getSkipButton(showcaseView: ShowcaseView?, step: Int, myCallback: (button: Button?) -> Unit){

        val nextButton = showcaseView?.findViewById<Button>(R.id.showcase_view_button)

        val vto = nextButton?.viewTreeObserver

        vto?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val vlp = nextButton.layoutParams as MarginLayoutParams

                Log.e("TAG Tutorial", "topMargin: ${vlp.topMargin}")
                Log.e("TAG Tutorial", "bottomMargin: ${vlp.bottomMargin}")
                Log.e("TAG Tutorial", "leftMargin: ${vlp.leftMargin}")
                Log.e("TAG Tutorial", "rightMargin: ${vlp.rightMargin}")

                Log.e("TAG Tutorial", "buttonNext Height: ${nextButton.measuredHeight}")

                val obs = nextButton.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)

                val skipButton = setSkipButtonLayoutParams(showcaseView, step, nextButton.measuredHeight, vlp.bottomMargin)
                myCallback.invoke(skipButton)
            }
        })
    }

    private fun setSkipButtonLayoutParams(showcaseView: ShowcaseView?, step: Int, nextButtonHeight: Int = 0, bottomMargin: Int = 0): Button {
        val lps = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        when(step){
            1 -> {
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                lps.setMargins(bottomMargin, bottomMargin, bottomMargin, bottomMargin)
            }
            2 -> {
                lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                lps.setMargins(bottomMargin, bottomMargin, bottomMargin, nextButtonHeight + (bottomMargin * 3))
            }
            3 -> {
                lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                lps.setMargins(bottomMargin, bottomMargin, bottomMargin, nextButtonHeight + (bottomMargin * 3))
            }
            4 -> {
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                lps.setMargins(bottomMargin, bottomMargin, bottomMargin, nextButtonHeight + (bottomMargin * 3))
            }
        }

//        val inflater = LayoutInflater.from(bottomTabsActivity)
        val inflater = bottomTabsActivity.layoutInflater
        val skipButton: Button =
            inflater.inflate(R.layout.showcase_view_custom_skip_button, showcaseView, false) as Button
        skipButton.layoutParams = lps
        return skipButton
    }
}