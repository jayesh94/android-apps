package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.save

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.barcodeSaver
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.permissionsHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.showError
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.StoragePermissionsDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.PermissionsHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_save_barcode_as_text.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets

class SaveBarcodeAsTextActivity : BaseActivity(), StoragePermissionsDialogFragment.Listener {

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 101
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        private const val BARCODE_KEY = "BARCODE_KEY"

        fun start(context: Context, barcode: Barcode) {
            val intent = Intent(context, SaveBarcodeAsTextActivity::class.java).apply {
                putExtra(BARCODE_KEY, barcode)
            }
            context.startActivity(intent)
        }
    }

    private val barcode by unsafeLazy {
        intent?.getSerializableExtra(BARCODE_KEY) as? Barcode ?: throw IllegalArgumentException("No barcode passed")
    }

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_barcode_as_text)
        //supportEdgeToEdge()
        initToolbar()
        initFormatSpinner()
        initSaveButton()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("SaveBarcodeAsTextActivity", "SaveBarcodeAsTextActivity")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsHelper.areAllPermissionsGranted(grantResults)) {
            faLogEvents.logCustomStoragePermissionEvent("save_barcode_as_text_granted")
            saveBarcode()
        } else {
            // Set sharedPreference as true show that the permission was denied at least once.
            with (PermissionsHelper.sharedPreferences.edit()) {
                faLogEvents.logCustomStoragePermissionEvent("save_barcode_as_text_denied")
                putBoolean(PermissionsHelper.sharedPrefStoragePermissionDeniedKey, true)
                apply()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initFormatSpinner() {
//        spinner_save_as.adapter = ArrayAdapter.createFromResource(
//            this, R.array.activity_save_barcode_as_text_formats, R.layout.item_spinner
//        ).apply {
//            setDropDownViewResource(R.layout.item_spinner_dropdown)
//        }

        val arrayAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.activity_save_barcode_as_text_formats)
        )

        val textView: AutoCompleteTextView = spinner_save_as
        textView.setAdapter(arrayAdapter)
        textView.setOnClickListener {
            textView.showDropDown()
        }
    }

    private fun initSaveButton() {
        button_save.setOnClickListener {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (PermissionsHelper.checkPermissions(this, PERMISSIONS).not()) {
            permissionsHelper.requestFolderPermission(this, PERMISSIONS)
        } else {
            saveBarcode()
        }
    }

    private fun saveBarcode() {
        val p: List<String> =
            resources.getStringArray(R.array.activity_save_barcode_as_text_formats).toList()

        val saveFunc = when (p.indexOf(spinner_save_as.text.toString())) {
            0 -> barcodeSaver::saveBarcodeAsCsv
            1 -> barcodeSaver::saveBarcodeAsJson
            else -> return
        }

        showLoading(true)

        saveFunc(this, barcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showBarcodeSaved() },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showLoading(isLoading: Boolean) {
        progress_bar_loading.isVisible = isLoading
        scroll_view.isVisible = isLoading.not()
    }

    private fun showBarcodeSaved() {
        Toast.makeText(this, R.string.activity_save_barcode_as_text_file_name_saved, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun showStoragePermissionDialog() {
        permissionsHelper.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE)
    }
}