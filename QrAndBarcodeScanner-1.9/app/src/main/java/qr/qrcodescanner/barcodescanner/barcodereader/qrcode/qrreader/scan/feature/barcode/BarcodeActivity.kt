package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.print.PrintHelper
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_barcode.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.otp.OtpActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.save.SaveBarcodeAsImageActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.save.SaveBarcodeAsTextActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.ChooseSearchEngineDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.DeleteConfirmationDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.EditBarcodeNameDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.AppRater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.updater.InAppUpdater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Marketplace
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.ParsedBarcode
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.SearchEngine
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.BarcodeSchema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.OtpAuth
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Logger
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.save
import java.text.SimpleDateFormat
import java.util.*


class BarcodeActivity : BaseActivity(), DeleteConfirmationDialogFragment.Listener, ChooseSearchEngineDialogFragment.Listener, EditBarcodeNameDialogFragment.Listener {

    companion object {
        private const val BARCODE_KEY = "BARCODE_KEY"
        private const val IS_CREATED = "IS_CREATED"
        private const val SAVED_TO_HISTORY = "SAVED_TO_HISTORY"
        private const val REQUEST_CODE_MY_PICK = 123

        fun start(
            context: Context,
            barcode: Barcode,
            isCreated: Boolean = false,
            savedToHistory: Boolean = false
        ) {
            val intent = Intent(context, BarcodeActivity::class.java).apply {
                putExtra(BARCODE_KEY, barcode)
                putExtra(IS_CREATED, isCreated)
                putExtra(SAVED_TO_HISTORY, savedToHistory)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)

    private val originalBarcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }

    private val isCreated by unsafeLazy {
        intent?.getBooleanExtra(IS_CREATED, false).orFalse()
    }

    private val savedToHistory by unsafeLazy {
        intent?.getBooleanExtra(SAVED_TO_HISTORY, false).orFalse()
    }

    private val barcode by unsafeLazy {
        ParsedBarcode(originalBarcode)
    }

    private val clipboardManager by unsafeLazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private var originalBrightness: Float = 0.5f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_barcode)
        //supportEdgeToEdge()
        saveOriginalBrightness()
        applySettings()

        handleToolbarBackPressed()
        handleToolbarMenuClicked()
        handleButtonsClicked()

        showBarcode()
        showOrHideButtons()
        showButtonText()

        checkSavedToHistory()
    }

    private fun checkSavedToHistory() {
        if(savedToHistory){
            showSavedToHistorySnackbar()
        }
    }

    private fun showSavedToHistorySnackbar() {

        Snackbar.make(
            root_view,
            resources.getString(R.string.pupop_snackbar_saved_code_to_history_message),
            5000
        ).apply {
            setAction(resources.getString(R.string.pupop_snackbar_saved_code_to_history_button)) {
                dismiss()
            }
            Log.e("TAG BA", "showSavedToHistorySnackbar: $duration")
            setActionTextColor(resources.getColor(R.color.new_secondary_color))
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("BarcodeActivity", "BarcodeActivity")
    }

    override fun onDeleteConfirmed(barcode: Barcode?) {
        deleteBarcode()
    }

    override fun onNameConfirmed(name: String, barcode: Barcode?) {
        updateBarcodeName(name)
    }

    override fun onSearchEngineSelected(searchEngine: SearchEngine) {
        performWebSearchUsingSearchEngine(searchEngine)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun saveOriginalBrightness() {
        originalBrightness = window.attributes.screenBrightness
    }

    private fun applySettings() {
        if (settings.copyToClipboard) {
            copyToClipboard(barcode.text)
        }

        if (settings.openLinksAutomatically.not() || isCreated) {
            return
        }

        when (barcode.schema) {
            BarcodeSchema.APP -> openInAppMarket()
            BarcodeSchema.BOOKMARK -> saveBookmark()
            BarcodeSchema.CRYPTOCURRENCY -> openBitcoinUrl()
            BarcodeSchema.EMAIL -> sendEmail(barcode.email)
            BarcodeSchema.GEO -> showLocation()
            BarcodeSchema.GOOGLE_MAPS -> showLocation()
            BarcodeSchema.MMS -> sendSmsOrMms(barcode.phone)
            BarcodeSchema.MECARD -> addToContacts()
            BarcodeSchema.OTP_AUTH -> openOtpInOtherApp()
            BarcodeSchema.PHONE -> callPhone(barcode.phone)
            BarcodeSchema.SMS -> sendSmsOrMms(barcode.phone)
            BarcodeSchema.URL -> openLink()
            BarcodeSchema.VEVENT -> addToCalendar()
            BarcodeSchema.VCARD -> addToContacts()
            BarcodeSchema.WIFI -> connectToWifi()
            BarcodeSchema.YOUTUBE -> openInYoutube()
            BarcodeSchema.UPI -> selectUPIPaymentGateway()
            BarcodeSchema.NZCOVIDTRACER -> openLink()
            else -> return
        }
    }


    private fun handleToolbarBackPressed() {
        toolbar.setNavigationOnClickListener {
            val appRater = AppRater(this)
            if(appRater.isShowAppRater())
                appRater.showAppRater(true)
            else
                finish()
        }
    }

    override fun onBackPressed() {
        val appRater = AppRater(this)
        if(appRater.isShowAppRater())
            appRater.showAppRater(true)
        else
            super.onBackPressed()
    }

    private fun handleToolbarMenuClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_increase_brightness -> {
                    increaseBrightnessToMax()
                    toolbar.menu.findItem(R.id.item_increase_brightness).isVisible = false
                    toolbar.menu.findItem(R.id.item_decrease_brightness).isVisible = true
                }
                R.id.item_decrease_brightness -> {
                    restoreOriginalBrightness()
                    toolbar.menu.findItem(R.id.item_increase_brightness).isVisible = true
                    toolbar.menu.findItem(R.id.item_decrease_brightness).isVisible = false
                }
                R.id.item_add_to_favorites -> toggleIsFavorite()
                R.id.item_show_barcode_image -> navigateToBarcodeImageActivity()
                R.id.item_save -> saveBarcode()
                R.id.item_delete -> showDeleteBarcodeConfirmationDialog()
            }

            val idName = this.resources.getResourceEntryName(item.itemId)
            faLogEvents.logCustomButtonClickEvent("activity_barcode_menu_$idName")

            return@setOnMenuItemClickListener true
        }
    }

    private fun handleButtonsClicked() {
        barcode_short_info.setOnClickListener {
            faLogEvents.logCustomButtonClickEvent("activity_barcode_barcode_short_info")
            navigateToBarcodeImageActivity()
        }

        button_edit_name.setOnClickListener {
            faLogEvents.logCustomButtonClickEvent("activity_barcode_button_edit_name")
            showEditBarcodeNameDialog()
        }

        button_search_on_web.setOnClickListener { searchBarcodeTextOnInternet() }
        button_search_on_amazon_com.setOnClickListener { searchBarcodeTextOnMarketplace(it.id) }
        button_search_on_ebay_com.setOnClickListener { searchBarcodeTextOnMarketplace(it.id) }
        button_search_on_open_food.setOnClickListener { searchBarcodeTextOnMarketplace(it.id) }
        button_search_on_amazon_in.setOnClickListener { searchBarcodeTextOnMarketplace(it.id) }
        button_search_on_flipkart_com.setOnClickListener { searchBarcodeTextOnMarketplace(it.id) }

        button_add_to_calendar.setOnClickListener { addToCalendar() }
        button_add_to_contacts.setOnClickListener { addToContacts() }
        button_show_location.setOnClickListener { showLocation() }
        button_connect_to_wifi.setOnClickListener { connectToWifi() }
        button_open_wifi_settings.setOnClickListener { openWifiSettings() }
        button_copy_network_name.setOnClickListener { copyNetworkNameToClipboard() }
        button_copy_network_password.setOnClickListener { copyNetworkPasswordToClipboard() }
        button_open_app.setOnClickListener { openApp() }
        button_open_in_app_market.setOnClickListener { openInAppMarket() }
        button_open_in_youtube.setOnClickListener { openInYoutube() }
        button_select_upi_payment_gateway.setOnClickListener { selectUPIPaymentGateway() }
        button_show_otp.setOnClickListener { showOtp() }
        button_open_otp.setOnClickListener { openOtpInOtherApp() }
        button_open_bitcoin_uri.setOnClickListener { openBitcoinUrl() }
        button_open_link.setOnClickListener { openLink() }
        button_save_bookmark.setOnClickListener { saveBookmark() }

        button_call_phone_1.setOnClickListener { callPhone(barcode.phone) }
        button_call_phone_2.setOnClickListener { callPhone(barcode.secondaryPhone) }
        button_call_phone_3.setOnClickListener { callPhone(barcode.tertiaryPhone) }

        button_send_sms_or_mms_1.setOnClickListener { sendSmsOrMms(barcode.phone) }
        button_send_sms_or_mms_2.setOnClickListener { sendSmsOrMms(barcode.secondaryPhone) }
        button_send_sms_or_mms_3.setOnClickListener { sendSmsOrMms(barcode.tertiaryPhone) }

        button_send_email_1.setOnClickListener { sendEmail(barcode.email) }
        button_send_email_2.setOnClickListener { sendEmail(barcode.secondaryEmail) }
        button_send_email_3.setOnClickListener { sendEmail(barcode.tertiaryEmail) }

        button_share_as_text.setOnClickListener { shareBarcodeAsText() }
        button_copy.setOnClickListener { copyBarcodeTextToClipboard() }
        button_search.setOnClickListener { searchBarcodeTextOnInternet() }
//        button_save_as_text.setOnClickListener { navigateToSaveBarcodeAsTextActivity() }
        button_share_as_image.setOnClickListener { shareBarcodeAsImage() }
        button_save_as_image.setOnClickListener { navigateToSaveBarcodeAsImageActivity() }
        button_print.setOnClickListener { printBarcode() }
    }


    private fun toggleIsFavorite() {
        val newBarcode = originalBarcode.copy(isFavorite = barcode.isFavorite.not())

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    barcode.isFavorite = newBarcode.isFavorite
                    showBarcodeIsFavorite(newBarcode.isFavorite)
                },
                {}
            )
            .addTo(disposable)
    }

    private fun updateBarcodeName(name: String) {
        if (name.isBlank()) {
            return
        }

        val newBarcode = originalBarcode.copy(
            id = barcode.id,
            name = name
        )

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    barcode.name = name
                    showBarcodeName(name)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun saveBarcode() {
        toolbar?.menu?.findItem(R.id.item_save)?.isVisible = false

        barcodeDatabase.save(originalBarcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    barcode.id = id
                    button_edit_name.isVisible = true
                    toolbar?.menu?.findItem(R.id.item_delete)?.isVisible = true
                },
                { error ->
                    toolbar?.menu?.findItem(R.id.item_save)?.isVisible = true
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun deleteBarcode() {
        showLoading(true)

        faLogEvents.logCustomButtonClickEvent("barcode_activity_single_barcode_deleted")

        barcodeDatabase.delete(barcode.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { finish() },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun addToCalendar() {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, barcode.eventSummary)
            putExtra(CalendarContract.Events.DESCRIPTION, barcode.eventDescription)
            putExtra(CalendarContract.Events.EVENT_LOCATION, barcode.eventLocation)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, barcode.eventStartDate)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, barcode.eventEndDate)
        }
        startActivityIfExists(intent)
    }

    private fun addToContacts() {
//        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE

            val fullName = "${barcode.firstName.orEmpty()} ${barcode.lastName.orEmpty()}"
            putExtra(ContactsContract.Intents.Insert.NAME, fullName)
            putExtra(ContactsContract.Intents.Insert.COMPANY, barcode.organization.orEmpty())
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, barcode.jobTitle.orEmpty())

            putExtra(ContactsContract.Intents.Insert.PHONE, barcode.phone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, barcode.phoneType.orEmpty().toPhoneType())

            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, barcode.secondaryPhone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, barcode.secondaryPhoneType.orEmpty().toPhoneType())

            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, barcode.tertiaryPhone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, barcode.tertiaryPhoneType.orEmpty().toPhoneType())

            putExtra(ContactsContract.Intents.Insert.EMAIL, barcode.email.orEmpty())
            putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, barcode.emailType.orEmpty().toEmailType())

            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, barcode.secondaryEmail.orEmpty())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, barcode.secondaryEmailType.orEmpty().toEmailType())

            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, barcode.tertiaryEmail.orEmpty())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, barcode.tertiaryEmailType.orEmpty().toEmailType())

            putExtra(ContactsContract.Intents.Insert.NOTES, barcode.note.orEmpty())
        }
        startActivityIfExists(intent)
    }
    /*private fun addToContacts() {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.Contacts.CONTENT_TYPE

            val fullName = "${barcode.firstName.orEmpty()} ${barcode.lastName.orEmpty()}"
            putExtra(ContactsContract.Intents.Insert.NAME, fullName)
            putExtra(ContactsContract.Intents.Insert.COMPANY, barcode.organization.orEmpty())
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, barcode.jobTitle.orEmpty())

            putExtra(ContactsContract.Intents.Insert.PHONE, barcode.phone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, barcode.phoneType.orEmpty().toPhoneType())

            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, barcode.secondaryPhone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, barcode.secondaryPhoneType.orEmpty().toPhoneType())

            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, barcode.tertiaryPhone.orEmpty())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, barcode.tertiaryPhoneType.orEmpty().toPhoneType())

            putExtra(ContactsContract.Intents.Insert.EMAIL, barcode.email.orEmpty())
            putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, barcode.emailType.orEmpty().toEmailType())

            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, barcode.secondaryEmail.orEmpty())
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, barcode.secondaryEmailType.orEmpty().toEmailType())

            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, barcode.tertiaryEmail.orEmpty())
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, barcode.tertiaryEmailType.orEmpty().toEmailType())

            putExtra(ContactsContract.Intents.Insert.NOTES, barcode.note.orEmpty())
        }
        startActivityIfExists(intent)
    }*/

    private fun callPhone(phone: String?) {
        val phoneUri = "tel:${phone.orEmpty()}"
        startActivityIfExists(Intent.ACTION_DIAL, phoneUri)
    }

    private fun sendSmsOrMms(phone: String?) {
        val uri = Uri.parse("sms:${phone.orEmpty()}")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", barcode.smsBody.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun sendEmail(email: String?) {
        val uri = Uri.parse("mailto:${email.orEmpty()}")
        val intent = Intent(Intent.ACTION_SEND, uri).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.orEmpty()))
            putExtra(Intent.EXTRA_SUBJECT, barcode.emailSubject.orEmpty())
            putExtra(Intent.EXTRA_TEXT, barcode.emailBody.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun showLocation() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.geoUri.orEmpty())
    }

    private fun connectToWifi() {
        showConnectToWifiButtonEnabled(false)

        wifiConnector
            .connect(
                    this,
                    barcode.networkAuthType.orEmpty(),
                    barcode.networkName.orEmpty(),
                    barcode.networkPassword.orEmpty(),
                    barcode.isHidden.orFalse(),
                    barcode.anonymousIdentity.orEmpty(),
                    barcode.identity.orEmpty(),
                    barcode.eapMethod.orEmpty(),
                    barcode.phase2Method.orEmpty()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showConnectToWifiButtonEnabled(true)
                    showToast(R.string.activity_barcode_connecting_to_wifi)
                },
                { error ->
                    showConnectToWifiButtonEnabled(true)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivityIfExists(intent)
    }

    private fun copyNetworkNameToClipboard() {
        copyToClipboard(barcode.networkName.orEmpty())
        showToast(R.string.activity_barcode_copied)
    }

    private fun copyNetworkPasswordToClipboard() {
        copyToClipboard(barcode.networkPassword.orEmpty())
        showToast(R.string.activity_barcode_copied)
    }

    private fun openApp() {
        val intent = packageManager?.getLaunchIntentForPackage(barcode.appPackage.orEmpty())
        if (intent != null) {
            startActivityIfExists(intent)
        }
    }

    private fun openInAppMarket() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.appMarketUrl.orEmpty())
    }

    private fun openInYoutube() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.youtubeUrl.orEmpty())
    }

    private fun selectUPIPaymentGateway() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.upiPaymentUrl.orEmpty())
    }

    private fun showOtp() {
        val otp = OtpAuth.parse(barcode.otpUrl.orEmpty()) ?: return
        OtpActivity.start(this, otp)
    }

    private fun openOtpInOtherApp() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.otpUrl.orEmpty())
    }

    private fun openBitcoinUrl() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.bitcoinUri.orEmpty())
    }

    private fun openLink() {
        startActivityIfExists(Intent.ACTION_VIEW, barcode.url.orEmpty())
    }

    private fun saveBookmark() {
        val intent = Intent(Intent.ACTION_INSERT, Uri.parse("content://browser/bookmarks")).apply {
            putExtra("title", barcode.bookmarkTitle.orEmpty())
            putExtra("url", barcode.url.orEmpty())
        }
        startActivityIfExists(intent)
    }

    private fun shareBarcodeAsText() {
        Log.e("TAG BA", "shareBarcodeAsText")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, barcode.text)
        }
        startActivityIfExists(intent)
    }

    private fun copyBarcodeTextToClipboard() {
        copyToClipboard(barcode.text)
        showToast(R.string.activity_barcode_copied)
    }

    private fun searchBarcodeTextOnInternet() {
        val searchEngine = settings.searchEngine
        when (searchEngine) {
           SearchEngine.NONE -> performWebSearch()
           SearchEngine.ASK_EVERY_TIME -> showSearchEnginesDialog()
           else -> performWebSearchUsingSearchEngine(searchEngine)
        }
    }

    private fun searchBarcodeTextOnMarketplace(id: Int) {
        val idName = resources.getResourceEntryName(id)
        performMarketplaceSearch(idName)
    }

    private fun performMarketplaceSearch(idName: String){
        var url = ""
        when {
            idName.contains("amazon_com") -> url = Marketplace.AMAZON_COM.templateUrl
            idName.contains("ebay_com") -> url = Marketplace.EBAY_COM.templateUrl
            idName.contains("open_food") -> url = Marketplace.OPEN_FOOD.templateUrl
            idName.contains("amazon_in") -> url = Marketplace.AMAZON_IN.templateUrl
            idName.contains("flipkart_com") -> url = Marketplace.FLIPKART_COM.templateUrl
        }

        url += barcode.text

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivityIfExists(browserIntent)
    }

    private fun performWebSearch() {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, barcode.text)
        }
        startActivityIfExists(intent)
    }

    private fun performWebSearchUsingSearchEngine(searchEngine: SearchEngine) {
        val url = searchEngine.templateUrl + barcode.text
        startActivityIfExists(Intent.ACTION_VIEW, url)
    }

    private fun shareBarcodeAsImage() {
        Log.e("TAG BA", "shareBarcodeAsImage")
        val imageUri = try {
            val image = barcodeImageGenerator.generateBitmap(originalBarcode, 200, 200, 1)
            barcodeImageSaver.saveImageToCache(this, image, barcode)
        } catch (ex: Exception) {
            Logger.log(ex)
            showError(ex)
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
// *****************************************************************************************
        /*
        // TODO An attempt to get the name of app chosen to share as image to log as "share" event in firebase analytics
        // First search for compatible apps with sharing (Intent.ACTION_SEND)
        val targetedShareIntents = arrayListOf<Intent>()

        val resInfo = packageManager.queryIntentActivities(intent, 0)
        if (!resInfo.isEmpty()) {
            for (info in resInfo) {
                val targetedShare = Intent(Intent.ACTION_SEND)
                targetedShare.type = "image/png" // put here your mime type
                targetedShare.setPackage(info.activityInfo.packageName.lowercase(Locale.getDefault()))
                targetedShareIntents.add(targetedShare)
            }
            // Then show the ACTION_PICK_ACTIVITY to let the user select it
            val intentPick = Intent()
            intentPick.action = Intent.ACTION_PICK_ACTIVITY
            // Set the title of the dialog
            intentPick.putExtra(Intent.EXTRA_TITLE, title)
            intentPick.putExtra(Intent.EXTRA_INTENT, intent)
            intentPick.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray())
            // Call StartActivityForResult so we can get the app name selected by the user
            this.startActivityForResult(intentPick, REQUEST_CODE_MY_PICK)
//            startActivityIfExists(intentPick, REQUEST_CODE_MY_PICK)
        }
        */
// *****************************************************************************************
        startActivityIfExists(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MY_PICK) {
            if (data != null && data.component != null && !TextUtils.isEmpty(
                    data.component!!.flattenToShortString()
                )
            ) {
                val appName = data.component!!.flattenToShortString()
                // Now you know the app being picked.
                // data is a copy of your launchIntent with this important extra info added.
                Log.e("TAG", "onActivityResult: $appName")
                // Start the selected activity
                startActivity(data)
            }
        }
    }


    private fun printBarcode() {
        val barcodeImage = try {
            barcodeImageGenerator.generateBitmap(originalBarcode, 1000, 1000, 3)
        } catch (ex: Exception) {
            Logger.log(ex)
            showError(ex)
            return
        }

        PrintHelper(this).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
            printBitmap("${barcode.format}_${barcode.schema}_${barcode.date}", barcodeImage)
        }
    }

    private fun navigateToBarcodeImageActivity() {
        BarcodeImageActivity.start(this, originalBarcode)
    }

    private fun navigateToSaveBarcodeAsTextActivity() {
        SaveBarcodeAsTextActivity.start(this, originalBarcode)
    }

    private fun navigateToSaveBarcodeAsImageActivity() {
        SaveBarcodeAsImageActivity.start(this, originalBarcode)
    }


    private fun showBarcode() {
        showBarcodeMenuIfNeeded()
        showBarcodeIsFavorite()
        showBarcodeImageIfNeeded()
        showBarcodeDate()
        showBarcodeFormat()
        showBarcodeName()
        showBarcodeText()
        showBarcodeCountry()
    }

    private fun showBarcodeMenuIfNeeded() {
        toolbar.inflateMenu(R.menu.menu_barcode)
        toolbar.menu.apply {
            findItem(R.id.item_increase_brightness).isVisible = isCreated
            findItem(R.id.item_add_to_favorites)?.isVisible = barcode.isInDb
            findItem(R.id.item_show_barcode_image)?.isVisible = isCreated.not()
            findItem(R.id.item_save)?.isVisible = barcode.isInDb.not()
            findItem(R.id.item_delete)?.isVisible = barcode.isInDb
        }
    }

    private fun showBarcodeIsFavorite() {
        showBarcodeIsFavorite(barcode.isFavorite)
    }

    private fun showBarcodeIsFavorite(isFavorite: Boolean) {
        val iconId = if (isFavorite) {
            R.drawable.ic_favorite_checked
        } else {
            R.drawable.ic_favorite_unchecked
        }
        toolbar.menu?.findItem(R.id.item_add_to_favorites)?.icon = ContextCompat.getDrawable(this, iconId)
    }

    private fun showBarcodeImageIfNeeded() {
        if (isCreated) {
            showBarcodeImage()
        }
    }

    private fun showBarcodeImage() {
        try {
            val bitmap = barcodeImageGenerator.generateBitmap(originalBarcode, 2000, 2000, 0, settings.barcodeContentColor, settings.barcodeBackgroundColor)
            layout_barcode_image_background.isVisible = true
            image_view_barcode.isVisible = true
            image_view_barcode.setImageBitmap(bitmap)
            image_view_barcode.setBackgroundColor(settings.barcodeBackgroundColor)
            layout_barcode_image_background.setBackgroundColor(settings.barcodeBackgroundColor)

            if (settings.isDarkTheme.not() || settings.areBarcodeColorsInversed) {
                layout_barcode_image_background.setPadding(0, 0, 0, 0)
            }
        } catch (ex: Exception) {
            Logger.log(ex)
            image_view_barcode.isVisible = false
        }
    }

    private fun showBarcodeDate() {
        text_view_date.text = dateFormatter.format(barcode.date)
    }

    private fun showBarcodeFormat() {
        val format = barcode.format.toStringId()
        toolbar.setTitle(format)
    }

    private fun showBarcodeName() {
        showBarcodeName(barcode.name)
    }

    private fun showBarcodeName(name: String?) {
        text_view_barcode_name.isVisible = name.isNullOrBlank().not()
        text_view_barcode_name.text = name.orEmpty()
    }

    private fun showBarcodeText() {
        text_view_barcode_text.text = if (isCreated) {
            barcode.text
        } else {
            barcode.formattedText
        }
    }

    private fun showBarcodeCountry() {
        val country = barcode.country ?: return
        when (country.contains('/')) {
            false -> showOneBarcodeCountry(country)
            true -> showTwoBarcodeCountries(country.split('/'))
        }
    }

    private fun showOneBarcodeCountry(country: String) { // Check for amazon In, Ebay In
        val fullCountryName = buildFullCountryName(country)
        showFullCountryName(fullCountryName)
    }

    private fun showTwoBarcodeCountries(countries: List<String>) {
        val firstFullCountryName = buildFullCountryName(countries[0])
        val secondFullCountryName = buildFullCountryName(countries[1])
        val fullCountryName = "$firstFullCountryName / $secondFullCountryName"
        showFullCountryName(fullCountryName)
    }

    private fun buildFullCountryName(country: String): String {
        val currentLocale = currentLocale ?: return ""
        val countryName = Locale("", country).getDisplayName(currentLocale)
        val countryEmoji = country.toCountryEmoji()
        return "$countryEmoji $countryName"
    }

    private fun showFullCountryName(fullCountryName: String) {
        text_view_country.apply {
            text = fullCountryName
            isVisible = fullCountryName.isBlank().not()
        }
    }

    private fun showOrHideButtons() {
        button_search.isVisible = isCreated.not()
        button_edit_name.isVisible = barcode.isInDb

        if (isCreated) {
            return
        }

        button_search_on_web.isVisible = barcode.isProductBarcode
        button_search_on_amazon_com.isVisible = barcode.isProductBarcode
        button_search_on_ebay_com.isVisible = barcode.isProductBarcode
        button_search_on_open_food.isVisible = barcode.isProductBarcode

        if (barcode.country?.contains("/") == false) { // For single country result
            if (barcode.country?.contains("IN") == true) {
                button_search_on_amazon_in.isVisible = barcode.isProductBarcode
                button_search_on_flipkart_com.isVisible = barcode.isProductBarcode
            }
        }

        button_search.isVisible = barcode.isProductBarcode.not()

        button_add_to_calendar.isVisible = barcode.schema == BarcodeSchema.VEVENT
        button_add_to_contacts.isVisible = barcode.schema == BarcodeSchema.VCARD || barcode.schema == BarcodeSchema.MECARD

        button_call_phone_1.isVisible = barcode.phone.isNullOrEmpty().not()
        button_call_phone_2.isVisible = barcode.secondaryPhone.isNullOrEmpty().not()
        button_call_phone_3.isVisible = barcode.tertiaryPhone.isNullOrEmpty().not()

        button_send_sms_or_mms_1.isVisible = barcode.phone.isNullOrEmpty().not() || barcode.smsBody.isNullOrEmpty().not()
        button_send_sms_or_mms_2.isVisible = barcode.secondaryPhone.isNullOrEmpty().not()
        button_send_sms_or_mms_3.isVisible = barcode.tertiaryPhone.isNullOrEmpty().not()

        button_send_email_1.isVisible = barcode.email.isNullOrEmpty().not() || barcode.emailSubject.isNullOrEmpty().not() || barcode.emailBody.isNullOrEmpty().not()
        button_send_email_2.isVisible = barcode.secondaryEmail.isNullOrEmpty().not()
        button_send_email_3.isVisible = barcode.tertiaryEmail.isNullOrEmpty().not()

        button_show_location.isVisible = barcode.geoUri.isNullOrEmpty().not()
        button_connect_to_wifi.isVisible = barcode.schema == BarcodeSchema.WIFI
        button_open_wifi_settings.isVisible = barcode.schema == BarcodeSchema.WIFI
        button_copy_network_name.isVisible = barcode.networkName.isNullOrEmpty().not()
        button_copy_network_password.isVisible = barcode.networkPassword.isNullOrEmpty().not()
        button_open_app.isVisible = barcode.appPackage.isNullOrEmpty().not() && isAppInstalled(barcode.appPackage)
        button_open_in_app_market.isVisible = barcode.appMarketUrl.isNullOrEmpty().not()
        button_open_in_youtube.isVisible = barcode.youtubeUrl.isNullOrEmpty().not()
        button_select_upi_payment_gateway.isVisible = barcode.upiPaymentUrl.isNullOrEmpty().not()
        button_show_otp.isVisible = barcode.otpUrl.isNullOrEmpty().not()
        button_open_otp.isVisible = barcode.otpUrl.isNullOrEmpty().not()
        button_open_bitcoin_uri.isVisible = barcode.bitcoinUri.isNullOrEmpty().not()
        button_open_link.isVisible = barcode.url.isNullOrEmpty().not()
        button_save_bookmark.isVisible = barcode.schema == BarcodeSchema.BOOKMARK
    }

    private fun showButtonText() {
        button_call_phone_1.text = getString(R.string.activity_barcode_call_phone, barcode.phone)
        button_call_phone_2.text = getString(R.string.activity_barcode_call_phone, barcode.secondaryPhone)
        button_call_phone_3.text = getString(R.string.activity_barcode_call_phone, barcode.tertiaryPhone)

        button_send_sms_or_mms_1.text = getString(R.string.activity_barcode_send_sms, barcode.phone)
        button_send_sms_or_mms_2.text = getString(R.string.activity_barcode_send_sms, barcode.secondaryPhone)
        button_send_sms_or_mms_3.text = getString(R.string.activity_barcode_send_sms, barcode.tertiaryPhone)

        button_send_email_1.text = getString(R.string.activity_barcode_send_email, barcode.email)
        button_send_email_2.text = getString(R.string.activity_barcode_send_email, barcode.secondaryEmail)
        button_send_email_3.text = getString(R.string.activity_barcode_send_email, barcode.tertiaryEmail)
    }

    private fun showConnectToWifiButtonEnabled(isEnabled: Boolean) {
        button_connect_to_wifi.isEnabled = isEnabled
    }

    private fun showDeleteBarcodeConfirmationDialog() {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_barcode_message)
        dialog.show(supportFragmentManager, "")
    }

    private fun showEditBarcodeNameDialog() {
        val dialog = EditBarcodeNameDialogFragment.newInstance(barcode.name)
        dialog.show(supportFragmentManager, "")
    }

    private fun showSearchEnginesDialog() {
        val dialog = ChooseSearchEngineDialogFragment()
        dialog.show(supportFragmentManager, "")
    }

    private fun showLoading(isLoading: Boolean) {
        progress_bar_loading.isVisible = isLoading
        scroll_view.isVisible = isLoading.not()
    }

    private fun startActivityIfExists(action: String, uri: String) {
        val intent = Intent(action, Uri.parse(uri))
        startActivityIfExists(intent)
    }

    private fun startActivityIfExists(intent: Intent) {
        intent.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                flags = flags or FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_REQUIRE_DEFAULT
            } else {
                flags = flags or FLAG_ACTIVITY_NEW_TASK
            }
        }

        if (intent.resolveActivity(packageManager) != null) {
            /*if (intent.component != null && !TextUtils.isEmpty(
                    intent.component!!.flattenToShortString()
                )
            ) {
                val appName = intent.component!!.flattenToShortString()
                // Now you know the app being picked.
                // data is a copy of your launchIntent with this important extra info added.
                Log.e("TAG", "startActivityIfExists: $appName")
            }*/

            try {
                startActivity(Intent.createChooser(intent, resources.getString(R.string.language_select_positive)))
                // Intent.createChooser is required for Devices with API 30+
            } catch (e: Exception) {
                Logger.log(e)
                Log.e("TAG BA", "Exception 1")
                showToast(R.string.activity_barcode_no_app)
            }
        } else {
            Log.e("TAG BA", "Exception 2")
            showToast(R.string.activity_barcode_no_app)
        }
    }

    private fun isAppInstalled(appPackage: String?): Boolean {
        return packageManager?.getLaunchIntentForPackage(appPackage.orEmpty()) != null
    }

    private fun copyToClipboard(text: String) {
        val clipData = ClipData.newPlainText("", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun showToast(stringId: Int) {
        Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show()
    }

    private fun increaseBrightnessToMax() {
        setBrightness(1.0f)
    }

    private fun restoreOriginalBrightness() {
        setBrightness(originalBrightness)
    }

    private fun setBrightness(brightness: Float) {
        window.attributes = window.attributes.apply {
            screenBrightness = brightness
        }
    }
}
