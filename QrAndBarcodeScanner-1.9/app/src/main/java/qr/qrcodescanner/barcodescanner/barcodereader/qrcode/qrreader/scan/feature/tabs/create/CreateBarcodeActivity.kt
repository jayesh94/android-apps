package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.showError
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.toStringId
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.BarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.ContactsPermissionsDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.App
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.BarcodeSchema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Logger
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.PermissionsHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.save
import com.google.zxing.BarcodeFormat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_barcode.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.AppRater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.barcode.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.qr.*


class CreateBarcodeActivity : BaseActivity(), AppAdapter.Listener, ContactsPermissionsDialogFragment.Listener {

    companion object {
        private const val BARCODE_FORMAT_KEY = "BARCODE_FORMAT_KEY"
        private const val BARCODE_SCHEMA_KEY = "BARCODE_SCHEMA_KEY"
        private const val DEFAULT_TEXT_KEY = "DEFAULT_TEXT_KEY"

        private const val CHOOSE_PHONE_REQUEST_CODE = 1
        private const val CHOOSE_CONTACT_REQUEST_CODE = 2

        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101
        private val CONTACTS_PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)

        fun start(context: Context, barcodeFormat: BarcodeFormat, barcodeSchema: BarcodeSchema? = null, defaultText: String? = null) {
            val intent = Intent(context, CreateBarcodeActivity::class.java).apply {
                putExtra(BARCODE_FORMAT_KEY, barcodeFormat.ordinal)
                putExtra(BARCODE_SCHEMA_KEY, barcodeSchema?.ordinal ?: -1)
                putExtra(DEFAULT_TEXT_KEY, defaultText)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()

    private val barcodeFormat by unsafeLazy {
        BarcodeFormat.values().getOrNull(intent?.getIntExtra(BARCODE_FORMAT_KEY, -1) ?: -1)
            ?: BarcodeFormat.QR_CODE
    }

    private val barcodeSchema by unsafeLazy {
        BarcodeSchema.values().getOrNull(intent?.getIntExtra(BARCODE_SCHEMA_KEY, -1) ?: -1)
    }

    private val defaultText by unsafeLazy {
        intent?.getStringExtra(DEFAULT_TEXT_KEY).orEmpty()
    }

    var isCreateBarcodeButtonEnabled: Boolean
        get() = false
        set(enabled) {
            val iconId = if (enabled) {
                R.drawable.ic_confirm_enabled
            } else {
                R.drawable.ic_confirm_disabled
            }

            toolbar.menu?.findItem(R.id.item_create_barcode)?.apply {
                icon = ContextCompat.getDrawable(this@CreateBarcodeActivity, iconId)
                isEnabled = enabled
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (createBarcodeImmediatelyIfNeeded()) {
            return
        }

        setContentView(R.layout.activity_create_barcode)
        //supportEdgeToEdge()
        handleToolbarBackClicked()
        handleToolbarMenuItemClicked()
        showToolbarTitle()
        showToolbarMenu()
        showFragment()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateBarcodeActivity", "CreateBarcodeActivity")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            CHOOSE_PHONE_REQUEST_CODE -> showChosenPhone(data)
            CHOOSE_CONTACT_REQUEST_CODE -> showChosenContact(data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE && permissionsHelper.areAllPermissionsGranted(grantResults)) {
            faLogEvents.logCustomContactsPermissionEvent("create_barcode_granted")
            chooseContact()
        } else {
            faLogEvents.logCustomContactsPermissionEvent("create_barcode_denied")
            // Set sharedPreference as true show that the permission was denied at least once.
            with (PermissionsHelper.sharedPreferences.edit()) {
                putBoolean(PermissionsHelper.sharedPrefContactsPermissionDeniedKey, true)
                apply()
            }
        }
    }

    override fun onAppClicked(packageName: String) {
        faLogEvents.logCustomButtonClickEvent("create_barcode_activity_create_app_barcode")
        createBarcode(App.fromPackage(packageName))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun createBarcodeImmediatelyIfNeeded(): Boolean {
        if (intent?.action != Intent.ACTION_SEND) {
            return false
        }

        return when (intent?.type) {
            "text/plain" -> {
                faLogEvents.logImportedDataEvent("create_barcode_activity_imported_text_plain")
//                Log.e("TAG", "create_barcode_activity_imported_text_plain")
                createBarcodeForPlainText()
                true
            }
            "text/x-vcard" -> {
                faLogEvents.logImportedDataEvent("create_barcode_activity_imported_text_vcard")
//                Log.e("TAG", "create_barcode_activity_imported_text_vcard")
                createBarcodeForVCard()
                true
            }
            else -> false
        }
    }

    private fun createBarcodeForPlainText() {
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }

    private fun createBarcodeForVCard() {
        val uri = intent?.extras?.get(Intent.EXTRA_STREAM) as? Uri ?: return
        val text = readDataFromVCardUri(uri).orEmpty()
        val schema = barcodeParser.parseSchema(barcodeFormat, text)
        createBarcode(schema, true)
    }

    private fun readDataFromVCardUri(uri: Uri): String? {
        val stream = try {
            contentResolver.openInputStream(uri) ?: return null
        } catch (e: Exception) {
            Logger.log(e)
            return null
        }

        val fileContent = StringBuilder("")

        var ch: Int
        try {
            while (stream.read().also { ch = it } != -1) {
                fileContent.append(ch.toChar())
            }
        } catch (e: Exception) {
            Logger.log(e)
        }
        stream.close()

        return fileContent.toString()
    }

    private fun handleToolbarBackClicked() {
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

    private fun handleToolbarMenuItemClicked() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_phone -> choosePhone()
                R.id.item_contacts -> requestContactsPermissions()
                R.id.item_create_barcode -> createBarcode()
            }

            val idName = this.resources.getResourceEntryName(item.itemId)
            faLogEvents.logCustomButtonClickEvent("create_barcode_activity_menu_$idName")

            return@setOnMenuItemClickListener true
        }
    }

    private fun showToolbarTitle() {
        val titleId = barcodeSchema?.toStringId() ?: barcodeFormat.toStringId()
        toolbar.setTitle(titleId)
    }

    private fun showToolbarMenu() {
        val menuId = when (barcodeSchema) {
            BarcodeSchema.APP -> return
            BarcodeSchema.PHONE, BarcodeSchema.SMS, BarcodeSchema.MMS -> R.menu.menu_create_qr_code_phone
            BarcodeSchema.VCARD, BarcodeSchema.MECARD -> R.menu.menu_create_qr_code_contacts
            else -> R.menu.menu_create_barcode
        }
        toolbar.inflateMenu(menuId)
    }

    private fun showFragment() {
        val fragment = when {
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTHER -> CreateQrCodeTextFragment.newInstance(defaultText)
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.URL -> CreateQrCodeUrlFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.BOOKMARK -> CreateQrCodeBookmarkFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.PHONE -> CreateQrCodePhoneFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.WIFI -> CreateQrCodeWifiFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.EMAIL -> CreateQrCodeEmailFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.SMS -> CreateQrCodeSmsFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MMS -> CreateQrCodeMmsFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.CRYPTOCURRENCY -> CreateQrCodeCryptocurrencyFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.GEO -> CreateQrCodeLocationFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.APP -> CreateQrCodeAppFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.OTP_AUTH -> CreateQrCodeOtpFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VEVENT -> CreateQrCodeEventFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.VCARD -> CreateQrCodeVCardFragment()
            barcodeFormat == BarcodeFormat.QR_CODE && barcodeSchema == BarcodeSchema.MECARD -> CreateQrCodeMeCardFragment()
            barcodeFormat == BarcodeFormat.DATA_MATRIX -> CreateDataMatrixFragment()
            barcodeFormat == BarcodeFormat.AZTEC -> CreateAztecFragment()
            barcodeFormat == BarcodeFormat.PDF_417 -> CreatePdf417Fragment()
            barcodeFormat == BarcodeFormat.CODABAR -> CreateCodabarFragment()
            barcodeFormat == BarcodeFormat.CODE_39 -> CreateCode39Fragment()
            barcodeFormat == BarcodeFormat.CODE_93 -> CreateCode93Fragment()
            barcodeFormat == BarcodeFormat.CODE_128 -> CreateCode128Fragment()
            barcodeFormat == BarcodeFormat.EAN_8 -> CreateEan8Fragment()
            barcodeFormat == BarcodeFormat.EAN_13 -> CreateEan13Fragment()
            barcodeFormat == BarcodeFormat.ITF -> CreateItf14Fragment()
            barcodeFormat == BarcodeFormat.UPC_A -> CreateUpcAFragment()
            barcodeFormat == BarcodeFormat.UPC_E -> CreateUpcEFragment()
            else -> return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    private fun choosePhone() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        startActivityForResultIfExists(intent, CHOOSE_PHONE_REQUEST_CODE)
    }

    private fun showChosenPhone(data: Intent?) {
        val phone = contactHelper.getPhone(this, data) ?: return
        getCurrentFragment().showPhone(phone)
    }

    private fun requestContactsPermissions() {
        if (PermissionsHelper.checkPermissions(this, CONTACTS_PERMISSIONS).not()) {
            permissionsHelper.requestContactPermission(this, CONTACTS_PERMISSIONS)
        } else {
            chooseContact()
        }
    }

    private fun chooseContact() {
//        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResultIfExists(intent, CHOOSE_CONTACT_REQUEST_CODE)
    }

    private fun showChosenContact(data: Intent?) {
        val contact = contactHelper.getContact(this, data) ?: return
        Log.e(
            "TAG Contact",
            contact.firstName.toString() + "\n "
                    + contact.middleName + "\n "
                    + contact.lastName + "\n "
                    + contact.country + "\n "
                    + contact.email + "\n "
                    + contact.poBox + "\n "
                    + contact.address + "\n "
                    + contact.city + "\n "
                    + contact.state + "\n "
                    + contact.street + "\n "
                    + contact.zipcode + "\n "
                    + contact.phone + "\n "
                    + contact.neighborhood + "\n "
                    + contact.formattedAddress + "\n "
                    + contact.label + "\n "
                    + contact.contactType
        )

        getCurrentFragment().showContact(contact)
    }

    private fun startActivityForResultIfExists(intent: Intent, requestCode: Int) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, requestCode)
        } else {
                Toast.makeText(this, R.string.activity_barcode_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    fun createBarcode() {
        val schema = getCurrentFragment().getBarcodeSchema()
        createBarcode(schema)
    }

    private fun createBarcode(schema: Schema, finish: Boolean = false) {
        val barcode = Barcode(
            text = schema.toBarcodeText(),
            formattedText = schema.toFormattedText(),
            format = barcodeFormat,
            schema = schema.schema,
            date = System.currentTimeMillis(),
            isGenerated = true
        )

        if (settings.saveCreatedBarcodesToHistory.not()) {
            navigateToBarcodeScreen(barcode, finish)
            return
        }

        barcodeDatabase.save(barcode, settings.doNotSaveDuplicates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { id ->
                    navigateToBarcodeScreen(barcode.copy(id = id), finish, true)
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun getCurrentFragment(): BaseCreateBarcodeFragment {
        return supportFragmentManager.findFragmentById(R.id.container) as BaseCreateBarcodeFragment
    }

    private fun navigateToBarcodeScreen(barcode: Barcode, finish: Boolean, savedToHistory: Boolean = false) {
        BarcodeActivity.start(this, barcode, true, savedToHistory)

        if (finish) {
            finish()
        }
    }

    override fun showContactsPermissionDialog() {
        permissionsHelper.requestPermissions(this, CONTACTS_PERMISSIONS, CONTACTS_PERMISSION_REQUEST_CODE)
    }
}