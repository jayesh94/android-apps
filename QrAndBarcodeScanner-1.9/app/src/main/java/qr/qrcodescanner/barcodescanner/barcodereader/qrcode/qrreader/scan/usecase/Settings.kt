package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.SearchEngine
import com.google.zxing.BarcodeFormat
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.BuildConfig

class Settings(private val context: Context) {

    companion object {
        const val THEME_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        const val THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        const val THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES

        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
        private var INSTANCE: Settings? = null

        fun getInstance(context: Context): Settings {
            return INSTANCE ?: Settings(context.applicationContext).apply { INSTANCE = this }
        }
    }

    private enum class Key {
        THEME,
        INVERSE_BARCODE_COLORS,
        OPEN_LINKS_AUTOMATICALLY,
        COPY_TO_CLIPBOARD,
        SIMPLE_AUTO_FOCUS,
        FLASHLIGHT,
        VIBRATE,
        CONTINUOUS_SCANNING,
        CONFIRM_SCANS_MANUALLY,
        IS_BACK_CAMERA,
        SAVE_SCANNED_BARCODES_TO_HISTORY,
        SAVE_CREATED_BARCODES_TO_HISTORY,
        DO_NOT_SAVE_DUPLICATES,
        SEARCH_ENGINE,
        ERROR_REPORTS,
        LANGUAGE,
        FIRST_LAUNCH_FOR_INTRODUCTION,
        APP_FIRST_LAUNCH_DATE_FOR_APP_RATER,
        APP_RATER_LAST_LAUNCH_DATE,
        APP_LAUNCH_COUNT_APP_RATER,
        APP_RATER_LAUNCH_DAYS_EXTENSION
    }

    private val sharedPreferences by unsafeLazy {
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var theme: Int
        get() = get(Key.THEME, THEME_SYSTEM)
        set(value) {
            set(Key.THEME, value)
            applyTheme(value)
        }

    val isDarkTheme: Boolean
        get() = theme == THEME_DARK || (theme == THEME_SYSTEM && isSystemDarkModeEnabled())

    var areBarcodeColorsInversed: Boolean
        get() = get(Key.INVERSE_BARCODE_COLORS, false)
        set(value) = set(Key.INVERSE_BARCODE_COLORS, value)

    val barcodeContentColor: Int
        get() = when  {
            isDarkTheme && areBarcodeColorsInversed -> Color.WHITE
            else -> Color.BLACK
        }

    val barcodeBackgroundColor: Int
        get() = when {
            isDarkTheme && areBarcodeColorsInversed.not() -> Color.WHITE
            else -> Color.TRANSPARENT
        }

    var openLinksAutomatically: Boolean
        get() = get(Key.OPEN_LINKS_AUTOMATICALLY, false)
        set(value) = set(Key.OPEN_LINKS_AUTOMATICALLY, value)

    var copyToClipboard: Boolean
        get() = get(Key.COPY_TO_CLIPBOARD, true)
        set(value) = set(Key.COPY_TO_CLIPBOARD, value)

    var simpleAutoFocus: Boolean
        get() = get(Key.SIMPLE_AUTO_FOCUS, true)
        set(value) = set(Key.SIMPLE_AUTO_FOCUS, value)

    var flash: Boolean
        get() = get(Key.FLASHLIGHT, false)
        set(value) = set(Key.FLASHLIGHT, value)

    var vibrate: Boolean
        get() = get(Key.VIBRATE, true)
        set(value) = set(Key.VIBRATE, value)

    var continuousScanning: Boolean
        get() = get(Key.CONTINUOUS_SCANNING, false)
        set(value) = set(Key.CONTINUOUS_SCANNING, value)

    var confirmScansManually: Boolean
        get() = get(Key.CONFIRM_SCANS_MANUALLY, false)
        set(value) = set(Key.CONFIRM_SCANS_MANUALLY, value)

    var isBackCamera: Boolean
        get() = get(Key.IS_BACK_CAMERA, true)
        set(value) = set(Key.IS_BACK_CAMERA, value)

    var saveScannedBarcodesToHistory: Boolean
        get() = get(Key.SAVE_SCANNED_BARCODES_TO_HISTORY, true)
        set(value) = set(Key.SAVE_SCANNED_BARCODES_TO_HISTORY, value)

    var saveCreatedBarcodesToHistory: Boolean
        get() = get(Key.SAVE_CREATED_BARCODES_TO_HISTORY, true)
        set(value) = set(Key.SAVE_CREATED_BARCODES_TO_HISTORY, value)

    var doNotSaveDuplicates: Boolean
        get() = get(Key.DO_NOT_SAVE_DUPLICATES, false)
        set(value) = set(Key.DO_NOT_SAVE_DUPLICATES, value)

    var searchEngine: SearchEngine
        get() = get(Key.SEARCH_ENGINE, SearchEngine.NONE)
        set(value) = set(Key.SEARCH_ENGINE, value)

    var areErrorReportsEnabled: Boolean
        get() = get(Key.ERROR_REPORTS, BuildConfig.ERROR_REPORTS_ENABLED_BY_DEFAULT)
        set(value) {
            set(Key.ERROR_REPORTS, value)
            Logger.isEnabled = value // Possible issue as the value is not being checked always. I was correct.
            // Assuming that below will also handle Firebase recordException
            Logger.setCrashlyticsCollection(value)
        }

    var appFirstLaunchIntroduction: Boolean
        get() = get(Key.FIRST_LAUNCH_FOR_INTRODUCTION, true)
        set(value) = set(Key.FIRST_LAUNCH_FOR_INTRODUCTION, value)

    var language: String
        get() = get(Key.LANGUAGE, "en")
        set(value) = set(Key.LANGUAGE, value)

    var appFirstLaunchDateAppRater: Long
        get() = get(Key.APP_FIRST_LAUNCH_DATE_FOR_APP_RATER, 0L)
        set(value) = set(Key.APP_FIRST_LAUNCH_DATE_FOR_APP_RATER, value)

    var appRaterLastLaunchDate: Long
        get() = get(Key.APP_RATER_LAST_LAUNCH_DATE, 0L)
        set(value) = set(Key.APP_RATER_LAST_LAUNCH_DATE, value)

    var appLaunchCountAppRater: Int
        get() = get(Key.APP_LAUNCH_COUNT_APP_RATER, 0)
        set(value) = set(Key.APP_LAUNCH_COUNT_APP_RATER, value)

    var appRaterLaunchDaysExtension: Int
        get() = get(Key.APP_RATER_LAUNCH_DAYS_EXTENSION, 0)
        set(value) = set(Key.APP_RATER_LAUNCH_DAYS_EXTENSION, value)


    fun isFormatSelected(format: BarcodeFormat): Boolean {
        return sharedPreferences.getBoolean(format.name, true)
    }

    fun setFormatSelected(format: BarcodeFormat, isSelected: Boolean) {
        sharedPreferences.edit()
            .putBoolean(format.name, isSelected)
            .apply()
    }

    fun reapplyTheme() {
        applyTheme(theme)
    }

    private fun get(key: Key, default: Long): Long {
        return sharedPreferences.getLong(key.name, default)
    }

    private fun set(key: Key, value: Long) {
        return sharedPreferences.edit()
            .putLong(key.name, value)
            .apply()
    }

    private fun get(key: Key, default: Int): Int {
        return sharedPreferences.getInt(key.name, default)
    }

    private fun set(key: Key, value: Int) {
        return sharedPreferences.edit()
            .putInt(key.name, value)
            .apply()
    }

    private fun get(key: Key, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key.name, default)
    }

    private fun set(key: Key, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key.name, value)
            .apply()
    }

    private fun get(key: Key, default: String): String {
        return sharedPreferences.getString(key.name, default).toString()
    }

    private fun set(key: Key, value: String?) {
        sharedPreferences.edit()
            .putString(key.name, value)
            .apply()
    }

    private fun get(key: Key, default: SearchEngine = SearchEngine.NONE): SearchEngine {
        val rawValue = sharedPreferences.getString(key.name, null) ?: default.name
        return SearchEngine.valueOf(rawValue)
    }

    private fun set(key: Key, value: SearchEngine) {
        sharedPreferences.edit()
            .putString(key.name, value.name)
            .apply()
    }

    private fun applyTheme(theme: Int) {
        when (theme) {
            AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES -> {
                Log.e("TAG Settings", "NIGHT NO YES $theme")
                AppCompatDelegate.setDefaultNightMode(theme)
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Log.e("TAG Settings", "MODE_NIGHT_FOLLOW_SYSTEM $theme")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    Log.e("TAG Settings", "MODE_NIGHT_AUTO_BATTERY $theme")
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

    private fun isSystemDarkModeEnabled(): Boolean {
        val mode = context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}