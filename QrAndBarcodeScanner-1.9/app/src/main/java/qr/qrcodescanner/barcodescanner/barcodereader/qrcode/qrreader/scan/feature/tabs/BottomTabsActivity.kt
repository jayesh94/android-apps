package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.forEach
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.install.model.ActivityResult
import io.reactivex.disposables.CompositeDisposable
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.BuildConfig
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.databinding.ActivityBottomTabs2Binding
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.permissionsHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.WelcomeMessageDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.AppRater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.history.BarcodeHistoryFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.history.BarcodeHistoryListFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.scan.ScanBarcodeFromCameraFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.scan.ScanBarcodeFromCameraOrFileFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.scan.file.ScanBarcodeFromFileActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.SettingsFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.tutorial.ShowcaseViewGuidedTutorial
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Logger
import java.util.*
import android.view.*

import android.view.Display
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.AssistActionBuilder
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig

import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.inAppUpdater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.configs.RemoteConfigs


class BottomTabsActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, ScanBarcodeFromCameraOrFileFragment.Listener,
    BarcodeHistoryListFragment.Listener, WelcomeMessageDialogFragment.Listener {

    lateinit var binding: ActivityBottomTabs2Binding
    private val disposable = CompositeDisposable()

    private lateinit var remoteConfig: FirebaseRemoteConfig

    var screenWidth = 0
    var screenHeight = 0

    companion object {
        private const val ACTION_CREATE_BARCODE = "${BuildConfig.APPLICATION_ID}.CREATE_BARCODE"
        private const val ACTION_HISTORY = "${BuildConfig.APPLICATION_ID}.HISTORY"
        private const val ACTION_SCAN = "${BuildConfig.APPLICATION_ID}.SCAN_FROM_CAMERA"
        private const val ACTION_SETTINGS = "${BuildConfig.APPLICATION_ID}.SETTINGS"

        private const val actionTokenExtra = "actions.fulfillment.extra.ACTION_TOKEN"

        const val IN_APP_UPDATE_REQUEST_CODE = 404

        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG BTA", "onCreate")
        setCrashlyticsCollection()

        try { // To be safe in case bad data is sent by mistake or proguard issue in different devices
            setFirebaseRemoteConfigs()
        } catch (error: Exception) {
           Logger.log(error)
        }

        if ((settings.language == "").not()){
            setAppLocale()
        }

        binding = ActivityBottomTabs2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //supportEdgeToEdge()

        initFabButton()
        initBottomNavigationView()

        initAppRaterParams()

        checkGuidedTutorial()

        if (savedInstanceState == null) {
            showInitialFragment()
        }

    }

    override fun onStart() {
        super.onStart()
        Log.e("TAG BTA", "onStart")
        try { // To be safe in case bad data is sent by mistake or proguard issue in different devices
            inAppUpdater.checkInAppUpdates(this)
        } catch (error: Exception) {
            Log.e("TAG BTA", "APP CRASHED")
            Logger.log(error)
        }
    }

    private fun setFirebaseRemoteConfigs() {
        remoteConfig = Firebase.remoteConfig

        RemoteConfigs.setDefaultRemoteConfigs(remoteConfig, this)
    }


    private fun setCrashlyticsCollection() {
        Logger.setCrashlyticsCollection(settings.areErrorReportsEnabled)
    }

    private fun initAppRaterParams() {
        if (settings.appFirstLaunchDateAppRater == 0L)
            settings.appFirstLaunchDateAppRater = System.currentTimeMillis()

        settings.appLaunchCountAppRater = settings.appLaunchCountAppRater + 1 // Increment App Launch Counter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("TAG BTA", "onActivityResult1")
        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            Log.e("TAG BTA", "onActivityResult2")
            when(requestCode){
                RESULT_OK -> {
                    Log.e("TAG BTA", "Use accepted flexible update! Result code: $resultCode")
                    faLogEvents.logInAppUpdateEvent("user_accepted_flexible_update")
                }
                RESULT_CANCELED -> {
                    Log.e("TAG BTA", "User cancelled the update! Result code: $resultCode")
                    faLogEvents.logInAppUpdateEvent("user_cancelled_update")
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Log.e("TAG BTA", "Update flow failed: Unknown reason! Result code: $resultCode")
                    faLogEvents.logInAppUpdateEvent("update_flow_failed_unknown_reason")
                }
                else -> {
                    Log.e("TAG BTA", "Update flow failed! Result code: $resultCode")
                    faLogEvents.logInAppUpdateEvent("update_flow_failed")
                }
            }
        }
    }

    private fun checkGuidedTutorial() {

        val dialogFragment = (supportFragmentManager.findFragmentByTag("WelcomeMessageDialogFragment") as? DialogFragment)
        if (settings.appFirstLaunchIntroduction) {
            if (dialogFragment == null)
                showWelcomeMessage()
        }
    }


    private fun showWelcomeMessage() {

        screenWidth = getWidth(this)
        screenHeight = getHeight(this)

        val welcomeDialog = WelcomeMessageDialogFragment()
        welcomeDialog.show(supportFragmentManager, "WelcomeMessageDialogFragment")
    }

    fun getWidth(context: Context): Int {
        var width:Int = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayMetrics = DisplayMetrics()
            val display: Display? = context.getDisplay()
            display!!.getRealMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }else{
            val displayMetrics = DisplayMetrics()
            this.windowManager.defaultDisplay.getMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            return width
        }
    }

    fun getHeight(context: Context): Int {
        var height: Int = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayMetrics = DisplayMetrics()
            val display = context.display
            display!!.getRealMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }else {
            val displayMetrics = DisplayMetrics()
            this.windowManager.defaultDisplay.getMetrics(displayMetrics)
            height = displayMetrics.heightPixels
            return height
        }
    }

    private fun startGuidedTutorial() {

        val midPointOfBottomNav = dpToPx(28)

        val softNavigationBarHeight = if (hasNavBar(this))
            getSoftNavigationBarHeight()
        else {
            0
        }

        val viewTargetBuffer = getStatusBarHeight() + getSoftNavigationBarHeight()


        Log.e("TAG BTA", "hasNavBar: " + hasNavBar(this))
        Log.e("TAG BTA", "getSoftNavigationBarHeight: " + getSoftNavigationBarHeight())
        Log.e("TAG BTA", "getStatusBarHeight: " + getStatusBarHeight())
        Log.e("TAG BTA", "getStatusBarHeight: " + getStatusBarHeight())

        ShowcaseViewGuidedTutorial(screenWidth, screenHeight, midPointOfBottomNav,
            softNavigationBarHeight, viewTargetBuffer, binding, this)
    }

    private fun getSoftNavigationBarHeight(): Int {
        // navigation bar height
        var navigationBarHeight = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
            navigationBarHeight
        } else {
            0
        }
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun hasNavBar(activity: Activity): Boolean {
        val temporaryHidden = activity.window.decorView.visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0
        if (temporaryHidden) return false
        val decorView = activity.window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.rootWindowInsets?.let{
                return it.stableInsetBottom != 0
            }
        }
        return true
    }

    private fun initFabButton() {
        binding.fab.setOnClickListener{
            faLogEvents.logCustomButtonClickEvent("bottom_tabs_activity_main_scan_button")
            navigateToMainCameraScanScreen()
        }
    }

    fun pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private inline fun View.getDimensions(crossinline onDimensionsReady: (Int, Int) -> Unit) {
        lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
        layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            onDimensionsReady(width, height)
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG BTA", "onResume")
        faLogEvents.logScreenViewEvent("BottomTabsActivity", "BottomTabsActivity")
        inAppUpdater.onResumeCalled()
    }

    override fun onPause() {
        super.onPause()
        Log.e("TAG BTA", "onPause")
    }

    private fun setAppLocale() {
        val lang = settings.language
        var language = lang
        var myLocale = Locale(language)

        if(lang.length > 2){
            language = lang.split("-r")[0]
            val country = lang.split("-r")[1]

            myLocale = Locale(language, country)
        }

        val config = resources.configuration

        Locale.setDefault(myLocale)
        config.setLocale(myLocale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun navigateToMainCameraScanScreen() {

        val fabIcon = AppCompatResources.getDrawable(this, R.drawable.fab_scan_button_active)
        binding.fab.setImageDrawable(fabIcon)

        binding.bottomNavigationView.menu.forEach {
            if (it.isChecked){
                showFragment(R.id.item_scan)
            }
        }

        uncheckAllBottomMenuItems()
    }

    private fun navigateToScanFromFileScreen() {
        ScanBarcodeFromFileActivity.start(this)
    }

    @SuppressLint("RestrictedApi")
    private fun uncheckAllBottomMenuItems() {
        // Uncheck all other bottom menu item
        for(i in 0 until binding.bottomNavigationView.menu.size()) {
            (binding.bottomNavigationView.menu.getItem(i) as? MenuItemImpl)?.let {
                it.isExclusiveCheckable = false
                it.isChecked = false
                it.isExclusiveCheckable = true
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == binding.bottomNavigationView.selectedItemId) {
        if (item.isChecked) {
            return false
        }

        val fabIcon = AppCompatResources.getDrawable(this, R.drawable.fab_scan_button_inactive)
        binding.fab.setImageDrawable(fabIcon)

        val idName = this.resources.getResourceEntryName(item.itemId)
        faLogEvents.logCustomButtonClickEvent("bottom_tabs_activity_menu_$idName")

        if(item.itemId == R.id.item_scan)
            showFragment(item.itemId, true)
        else
            showFragment(item.itemId)

        return true
    }

    override fun onBackPressed() {
        binding.bottomNavigationView.menu.forEach {
            if (it.isChecked){
                navigateToMainCameraScanScreen()
                return
            }
        }

        val appRater = AppRater(this)
        if(appRater.isShowAppRater())
            appRater.showAppRater(true)
        else
            super.onBackPressed()
    }

    private fun supportEdgeToEdge() {
        binding.bottomNavigationView.applySystemWindowInsets(applyBottom = true)
    }

    private fun initBottomNavigationView() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.apply {
            setOnNavigationItemSelectedListener(this@BottomTabsActivity)
        }
    }

    private fun showInitialFragment() {

        Log.e("TAG BTA", "Intent Data URI: " + intent?.dataString)
        Log.e("TAG BTA", "Intent Action: " + intent?.action)

        checkFirebaseDynamicLink()

        val eventPrefix = "shortcuts_"

        when (intent?.action) {
            ACTION_CREATE_BARCODE -> {
                faLogEvents.logAppActionEvent(eventPrefix + "action_create_barcode")
                binding.bottomNavigationView.selectedItemId = R.id.item_create
            }
            ACTION_HISTORY -> {
                faLogEvents.logAppActionEvent(eventPrefix + "action_history")
                binding.bottomNavigationView.selectedItemId = R.id.item_history
            }
            ACTION_SCAN -> {
                faLogEvents.logAppActionEvent(eventPrefix + "action_scan_camera")
                navigateToMainCameraScanScreen()
            }
            ACTION_SETTINGS -> {
                faLogEvents.logAppActionEvent("settings_fragment_action_change_language")
                binding.bottomNavigationView.selectedItemId = R.id.item_settings
            }
//            else -> showFragment(R.id.item_scan)
            else -> {
                if(intent?.action == Intent.ACTION_VIEW ){

                    // Intent Data URI: https://ascetx.com/qr-scanner
                    // https://ascetx.com/google-assistant-qr-scan
                    // TAG BTA: Intent Action: android.intent.action.VIEW

                    when {
                        intent?.dataString?.contains("qr-scanner") == true -> {
                            faLogEvents.logAppActionEvent("web_app_link_action_scan_camera")
                            Log.e("TAG", "App Action from Web Link")
//                            googleIndexUrl(intent?.dataString!!)
                        }
                        intent?.dataString?.contains("google-assistant") == true -> {
                            faLogEvents.logAppActionEvent("google_assistant_action_scan_camera")
                            Log.e("TAG", "App Action from Google Assistant")
                            // On Action success
                            notifyActionStatus(Action.Builder.STATUS_TYPE_COMPLETED)
//                            googleIndexUrl(intent?.dataString!!)
                        }
                        else -> {
                            notifyActionStatus(Action.Builder.STATUS_TYPE_FAILED)
                        }
                    }
                }
                navigateToMainCameraScanScreen()
            }
        }
    }

    private fun checkFirebaseDynamicLink() {

        FirebaseAnalytics.getInstance(this)

        // [START get_deep_link]
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                // [START_EXCLUDE]
                // Display deep link in the UI
                if (deepLink != null) {
                    Log.e("TAG BTA", "Got the deep link!: $deepLink")
                    faLogEvents.logAppActionEvent("open_dynamic_link_action_share")

                } else {
                    Log.e("TAG BTA", "getDynamicLink: no link found")
                }
                // [END_EXCLUDE]
            }
            .addOnFailureListener(this) { e -> Log.e("TAG BTA", "getDynamicLink:onFailure", e) }
        // [END get_deep_link]
    }

    /**
     *  Initialized and executed only once as an attempt to index both the URLs
     * **/
    private fun googleIndexUrl(url: String){
        val appIndex = FirebaseAppIndex.getInstance(applicationContext)
        val recipe = Indexable.Builder()
            .setName("QR Code Scanner - Barcode Reader - AscetX")
            .setUrl(url)
            .build()
        appIndex.update(recipe)
    }

    private fun notifyActionStatus(status: String) {
        val actionToken = intent.getStringExtra(actionTokenExtra)
        val action = AssistActionBuilder()
            .setActionToken(actionToken.toString())
            .setActionStatus(status)
            .build()
        FirebaseUserActions.getInstance(this).end(action)
    }

    private fun showFragment(bottomItemId: Int, barcodeScan: Boolean = false) {
        val fragment = when (bottomItemId) {
            R.id.item_scan -> {
                if (checkPermissions()) {
                    ScanBarcodeFromCameraFragment(barcodeScan)
                } else {
                    ScanBarcodeFromCameraOrFileFragment()
                }
            }
            R.id.item_create -> CreateBarcodeFragment()
            R.id.item_history -> BarcodeHistoryFragment()
            R.id.item_settings -> SettingsFragment()
            else -> null
        }
        fragment?.apply(::replaceFragment)
    }

    private fun checkPermissions(): Boolean {
        return permissionsHelper.checkPermissions(this, PERMISSIONS)
    }

    private fun replaceFragment(fragment: Fragment, isMainScanFragment: Boolean = false) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    override fun onDestroy() {

        super.onDestroy()
        Log.e("TAG BTA", "onDestroy")

        disposable.clear()
    }

    override fun onStop() {
        Log.e("TAG BTA", "onStop")
        inAppUpdater.onStopCalled()
        inAppUpdater.clearActivityInstance()
        super.onStop()
    }

    override fun uncheckBottomNavItemsOpenScanBarcodeFromCameraFragment() {
        uncheckAllBottomMenuItems()
        replaceFragment(ScanBarcodeFromCameraFragment())
    }

    override fun startCreateBarcodeFragment() {
        binding.bottomNavigationView.selectedItemId = R.id.item_create
//        replaceFragment(CreateBarcodeFragment())
    }

    override fun showTutorial() {
        Log.e("TAG", "showTutorial")
        (supportFragmentManager.findFragmentByTag("WelcomeMessageDialogFragment") as? DialogFragment)?.dismiss()
        startGuidedTutorial()
    }

    override fun skipTutorial() {
        Log.e("TAG", "skipTutorial")
        settings.appFirstLaunchIntroduction = false
        (supportFragmentManager.findFragmentByTag("WelcomeMessageDialogFragment") as? DialogFragment)?.dismiss()
    }
}