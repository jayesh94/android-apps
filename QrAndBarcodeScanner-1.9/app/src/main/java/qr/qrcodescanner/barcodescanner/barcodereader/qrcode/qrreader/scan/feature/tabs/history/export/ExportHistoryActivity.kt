package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.history.export

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.barcodeDatabase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.barcodeSaver
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.permissionsHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.isNotBlank
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.showError
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.textString
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.StoragePermissionsDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.PermissionsHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_export_history.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class ExportHistoryActivity : BaseActivity(), StoragePermissionsDialogFragment.Listener {
    private val disposable = CompositeDisposable()

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 101
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun start(context: Context) {
            val intent = Intent(context, ExportHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_history)
        //supportEdgeToEdge()
        initToolbar()
        initExportTypeSpinner()
        initFileNameEditText()
        initExportButton()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("ExportHistoryActivity", "ExportHistoryActivity")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsHelper.areAllPermissionsGranted(grantResults)) {
            faLogEvents.logCustomStoragePermissionEvent("export_history_granted")
            exportHistory()
        } else {
            // Set sharedPreference as true show that the permission was denied at least once.
            with (PermissionsHelper.sharedPreferences.edit()) {
                faLogEvents.logCustomStoragePermissionEvent("export_history_denied")
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

    private fun initExportTypeSpinner() {
        spinner_export_as.adapter = ArrayAdapter.createFromResource(
            this, R.array.activity_export_history_types, R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
    }

    private fun initFileNameEditText() {
        edit_text_file_name.addTextChangedListener {
            button_export.isEnabled = edit_text_file_name.isNotBlank()
        }
    }

    private fun initExportButton() {
        button_export.setOnClickListener {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (PermissionsHelper.checkPermissions(this, PERMISSIONS).not()) {
            permissionsHelper.requestFolderPermission(this, PERMISSIONS)
        } else {
            exportHistory()
        }
    }

    private fun exportHistory() {
        val fileName = edit_text_file_name.textString
        val saveFunc = when (spinner_export_as.selectedItemPosition) {
            0 -> barcodeSaver::saveBarcodeHistoryAsCsv
            1 -> barcodeSaver::saveBarcodeHistoryAsJson
            else -> return
        }

        showLoading(true)

        barcodeDatabase
            .getAllForExport()
            .flatMapCompletable { barcodes ->
                saveFunc(this, fileName, barcodes)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showHistoryExported()
                },
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

    private fun showHistoryExported() {
        Toast.makeText(this, R.string.activity_export_history_exported, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun showStoragePermissionDialog() {
        permissionsHelper.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE)
    }
}