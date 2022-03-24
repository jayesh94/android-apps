package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.theme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Settings
import kotlinx.android.synthetic.main.activity_choose_theme.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class ChooseThemeActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseThemeActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val buttons by unsafeLazy {
        listOf(button_system_theme, button_light_theme, button_dark_theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_theme)
        //supportEdgeToEdge()
        initToolbar()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("ChooseThemeActivity", "ChooseThemeActivity")
        showInitialSettings()
        handleSettingsChanged()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun showInitialSettings() {
        val theme = settings.theme
        button_system_theme.isChecked = theme == Settings.THEME_SYSTEM
        button_light_theme.isChecked = theme == Settings.THEME_LIGHT
        button_dark_theme.isChecked = theme == Settings.THEME_DARK
    }

    private fun handleSettingsChanged() {
        button_system_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }

            uncheckOtherButtons(button_system_theme)
            settings.theme = Settings.THEME_SYSTEM
            logSetTheme("system")
        }

        button_light_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }

            uncheckOtherButtons(button_light_theme)
            settings.theme = Settings.THEME_LIGHT
            logSetTheme("light")
        }

        button_dark_theme.setCheckedChangedListener { isChecked ->
            if (isChecked.not()) {
                return@setCheckedChangedListener
            }

            uncheckOtherButtons(button_dark_theme)
            settings.theme = Settings.THEME_DARK
            logSetTheme("dark")
        }
    }

    private fun logSetTheme(theme: String) {
        faLogEvents.logCustomButtonClickEvent("settings_fragment_set_theme_$theme")
    }

    private fun uncheckOtherButtons(checkedButton: View) {
        buttons.forEach { button ->
            if (checkedButton !== button) {
                button.isChecked = false
            }
        }
    }
}