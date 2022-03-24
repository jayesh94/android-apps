package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_settings.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.BuildConfig
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.barcodeDatabase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.packageManager
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.showError
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.DeleteConfirmationDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.camera.ChooseCameraActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.formats.SupportedFormatsActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.permissions.AllPermissionsActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.search.ChooseSearchEngineActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.theme.ChooseThemeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.SearchEngine
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Settings
import android.util.DisplayMetrics
import android.util.Log
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.AppRater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.BottomTabsActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Languages
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.SendFeedback
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Logger
import java.util.*


class SettingsFragment : Fragment(), DeleteConfirmationDialogFragment.Listener {
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //supportEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()

        faLogEvents.logScreenViewEvent("SettingsFragment", "SettingsFragment")

        handleButtonCheckedChanged()
        handleButtonClicks()
        showSettings()
        showSettingsText()
        showSettingsIcons()
        showAppVersion()
    }

    override fun onDeleteConfirmed(barcode: Barcode?) {
        clearHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    fun supportEdgeToEdge() {
        app_bar_layout.applySystemWindowInsets(applyTop = true)
    }

    private fun handleButtonCheckedChanged() {
        button_inverse_barcode_colors_in_dark_theme.setCheckedChangedListener { settings.areBarcodeColorsInversed = it }
        button_open_links_automatically.setCheckedChangedListener { settings.openLinksAutomatically = it }
        button_copy_to_clipboard.setCheckedChangedListener { settings.copyToClipboard = it }
        button_simple_auto_focus.setCheckedChangedListener { settings.simpleAutoFocus = it }
        button_flashlight.setCheckedChangedListener { settings.flash = it }
        button_vibrate.setCheckedChangedListener { settings.vibrate = it }
        button_continuous_scanning.setCheckedChangedListener { settings.continuousScanning = it }
        button_confirm_scans_manually.setCheckedChangedListener { settings.confirmScansManually = it }
        button_save_scanned_barcodes.setCheckedChangedListener { settings.saveScannedBarcodesToHistory = it }
        button_save_created_barcodes.setCheckedChangedListener { settings.saveCreatedBarcodesToHistory = it }
        button_do_not_save_duplicates.setCheckedChangedListener { settings.doNotSaveDuplicates = it }
        button_enable_error_reports.setCheckedChangedListener { settings.areErrorReportsEnabled = it }
    }

    private fun handleButtonClicks() {
        button_choose_theme.setOnClickListener { ChooseThemeActivity.start(requireActivity()) }
        button_choose_language.setOnClickListener { setAppLanguage() }
        button_choose_camera.setOnClickListener { ChooseCameraActivity.start(requireActivity()) }
        button_select_supported_formats.setOnClickListener { SupportedFormatsActivity.start(requireActivity()) }
        button_clear_history.setOnClickListener { showDeleteHistoryConfirmationDialog() }
        button_choose_search_engine.setOnClickListener { ChooseSearchEngineActivity.start(requireContext()) }
        button_permissions.setOnClickListener { AllPermissionsActivity.start(requireActivity()) }
        button_introduction.setOnClickListener { startAppIntroduction() }
        button_check_updates.setOnClickListener { showAppInMarket() }
        button_our_app_flashlight.setOnClickListener { showOurFlashlightApp() }
        button_privacy_policy.setOnClickListener { showPrivacyPolicy() }
        button_share.setOnClickListener { shareApp() }
        button_feedback.setOnClickListener { SendFeedback(requireContext()) }
        button_rate_us.setOnClickListener { AppRater(requireActivity()).showAppRater()}
    }

    private fun startAppIntroduction() {
        settings.appFirstLaunchIntroduction = true
//        requireActivity().recreate()
        val refresh = Intent(requireContext(), BottomTabsActivity::class.java)
        requireActivity().finish()
        startActivity(refresh)
    }

    private fun setAppLanguage() {

        var locale = ""
        val languageMap = Languages.supportedLanguages().toSortedMap()
        val listLanguages = ArrayList(languageMap.keys.toList())
        val languages: Array<String> = listLanguages.toArray(arrayOfNulls(listLanguages.size))

        try {
            throw IllegalArgumentException("message")
        } catch (e: Exception){
            Logger.log(e)
        }

        val mBuilder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        mBuilder.setSingleChoiceItems(languages, -1) { dialogInterface, i ->
            locale = languageMap.getValue(languages[i])
        }
        mBuilder.setTitle(R.string.fragment_settings_language_options)
        mBuilder.setPositiveButton(resources.getString(R.string.language_select_positive)) { dialog, which ->

            if ((settings.language == locale || locale == "").not()) {
                settings.language = locale
                setLocale(locale)
            }
        }

        // Set the neutral/cancel button click listener
        mBuilder.setNegativeButton(resources.getString(R.string.language_select_negative)) { dialog, _ ->
            // Do something when click the neutral button
            dialog.cancel()
        }

        val mDialog = mBuilder.create()

        mDialog.setOnShowListener {
            faLogEvents.logScreenViewEvent("LanguageOptionsAlertDialog", "LanguageOptionsAlertDialog")
        }

        mDialog.show()
    }

    private fun getCurrentLocale(context: Context): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    private fun setLocale(lang: String) {
        var language = lang
        var myLocale = Locale(language)


        if(lang.length > 2){
            language = lang.split("-r")[0]
            val country = lang.split("-r")[1]

            myLocale = Locale(language, country)
            faLogEvents.logCustomButtonClickEvent("settings_fragment_set_language_$language"+"_"+ country)
        } else {
            faLogEvents.logCustomButtonClickEvent("settings_fragment_set_language_$language")
        }

        val dm: DisplayMetrics = resources.displayMetrics
        val conf: Configuration = resources.configuration

        conf.locale = myLocale
        resources.updateConfiguration(conf, dm)

        val refresh = Intent(requireContext(), BottomTabsActivity::class.java)
        refresh.action = "${BuildConfig.APPLICATION_ID}.SETTINGS"
        requireActivity().finish()
        startActivity(refresh)
    }

    private fun clearHistory() {
        button_clear_history.isEnabled = false

        faLogEvents.logCustomButtonClickEvent("settings_fragment_all_barcodes_deleted")

        barcodeDatabase.deleteAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    button_clear_history.isEnabled = true
                },
                { error ->
                    button_clear_history.isEnabled = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showSettings() {
        settings.apply {
            button_inverse_barcode_colors_in_dark_theme.isChecked = areBarcodeColorsInversed
            button_open_links_automatically.isChecked = openLinksAutomatically
            button_copy_to_clipboard.isChecked = copyToClipboard
            button_simple_auto_focus.isChecked = simpleAutoFocus
            button_flashlight.isChecked = flash
            button_vibrate.isChecked = vibrate
            button_continuous_scanning.isChecked = continuousScanning
            button_confirm_scans_manually.isChecked = confirmScansManually
            button_save_scanned_barcodes.isChecked = saveScannedBarcodesToHistory
            button_save_created_barcodes.isChecked = saveCreatedBarcodesToHistory
            button_do_not_save_duplicates.isChecked = doNotSaveDuplicates
            button_enable_error_reports.isChecked = areErrorReportsEnabled
        }
    }

    private fun showDeleteHistoryConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_clear_history_message)
        dialog.show(childFragmentManager, "")
    }

    private fun showAppInMarket() {
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun showOurFlashlightApp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://brightestflashlight.page.link/qr_flash"))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun showPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ascetx.com/privacy-policy/"))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun shareApp() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            resources.getString(R.string.share_app_message_subject)
        )
        sharingIntent.putExtra(
            Intent.EXTRA_TEXT,
            resources.getString(R.string.share_app_message_body)
        )
        startActivity(Intent.createChooser(sharingIntent, requireActivity().resources.getString(R.string.share_via)))
    }

    private fun showSettingsText() {
//        Log.e("TAG", Locale.forLanguageTag("zh-Hans-CN").getDisplayName(Locale.CHINA))
//        Log.e("TAG", Locale.forLanguageTag("zh-Hans-SG").getDisplayName(Locale.CHINA))
//        Log.e("TAG", Locale.forLanguageTag("zh-Hant-HK").getDisplayName(Locale.CHINA))
//        Log.e("TAG", Locale.forLanguageTag("zh-Hant-TW").getDisplayName(Locale.CHINA))
//        Log.e("TAG", Locale("pt","BR").displayName)
//        Log.e("TAG", getCurrentLocale(requireContext())!!.language)

        val languageMap = Languages.supportedLanguages().toSortedMap()
        val listLangCodes = ArrayList(languageMap.values.toList())
        val languageCodes: Array<String> = listLangCodes.toArray(arrayOfNulls(listLangCodes.size))
        val currentLanguageCode = getCurrentLocale(requireContext())?.language

        if (currentLanguageCode ?: "en" in languageCodes){
            // Below will write English in the currentLocale. Eg. if locale is Hindi then English will be displayed in Hindi
            // button_choose_language.settingsText = getCurrentLocale(requireContext())?.displayName.toString()
            val keys = languageMap.filterValues { it == currentLanguageCode }.keys.first()
            button_choose_language.settingsText = keys
        }
        else
            button_choose_language.settingsText = languageMap.filterValues { it == settings.language }.keys.first()

        val str: String = when (settings.theme){
            Settings.THEME_SYSTEM -> resources.getString(R.string.activity_choose_theme_system)
            Settings.THEME_LIGHT -> resources.getString(R.string.activity_choose_theme_light)
            Settings.THEME_DARK -> resources.getString(R.string.activity_choose_theme_dark)
            else -> ""
        }

        if(str != ""){
            button_choose_theme.settingsText = str
        }

        if (settings.isBackCamera){
            button_choose_camera.settingsText = resources.getString(R.string.activity_choose_camera_back)
        } else {
            button_choose_camera.settingsText = resources.getString(R.string.activity_choose_camera_front)
        }

        val strSearchEngine: String = when (settings.searchEngine) {
            SearchEngine.NONE -> resources.getString(R.string.activity_choose_search_engine_none)
            SearchEngine.ASK_EVERY_TIME -> resources.getString(R.string.activity_choose_search_engine_ask_every_time)
            SearchEngine.BING -> resources.getString(R.string.activity_choose_search_engine_bing)
            SearchEngine.DUCK_DUCK_GO -> resources.getString(R.string.activity_choose_search_engine_duck_duck_go)
            SearchEngine.GOOGLE -> resources.getString(R.string.activity_choose_search_engine_google)
            SearchEngine.QWANT -> resources.getString(R.string.activity_choose_search_engine_qwant)
            SearchEngine.YAHOO -> resources.getString(R.string.activity_choose_search_engine_yahoo)
            SearchEngine.YANDEX -> resources.getString(R.string.activity_choose_search_engine_yandex)
        }

        if(strSearchEngine != ""){
            button_choose_search_engine.settingsText = strSearchEngine
        }
    }

    private fun showSettingsIcons() {
        button_share.settingsIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_share_24)!!
        button_rate_us.settingsIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_star_rate_24)!!
        button_privacy_policy.settingsIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_privacy_tip_24)!!
        button_feedback.settingsIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_feedback_24)!!
        button_enable_error_reports.settingsIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_bug_report_24)!!
    }

    private fun showAppVersion() {
        button_app_version.hint = BuildConfig.VERSION_NAME
    }

}
