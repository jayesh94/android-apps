package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.formats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.SupportedBarcodeFormats
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.activity_supported_formats.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class SupportedFormatsActivity : BaseActivity(), FormatsAdapter.Listener {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SupportedFormatsActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val formats by unsafeLazy { SupportedBarcodeFormats.FORMATS }
    private val formatSelection by unsafeLazy { formats.map(settings::isFormatSelected) }
    private val formatsAdapter by unsafeLazy { FormatsAdapter(this, formats, formatSelection) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supported_formats)
        //supportEdgeToEdge()
        initRecyclerView()
        handleToolbarBackClicked()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("SupportedFormatsActivity", "SupportedFormatsActivity")
    }

    override fun onFormatChecked(format: BarcodeFormat, isChecked: Boolean) {
        settings.setFormatSelected(format, isChecked)
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initRecyclerView() {
        recycler_view_formats.apply {
            layoutManager = LinearLayoutManager(this@SupportedFormatsActivity)
            adapter = formatsAdapter
        }
    }

    private fun handleToolbarBackClicked() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}