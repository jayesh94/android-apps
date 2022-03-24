package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.fragment_create_qr_code_text.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.BaseCreateBarcodeFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Url
import kotlinx.android.synthetic.main.fragment_create_qr_code_url.*
import kotlinx.android.synthetic.main.fragment_create_qr_code_url.edit_text
import kotlinx.android.synthetic.main.fragment_create_qr_code_url.generate_code_button
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.barcodeParser
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.isNotBlank
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.textString

class CreateQrCodeUrlFragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_url, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showUrlPrefix()
        handleTextChanged()

        generate_code_button.setOnClickListener() {
            (activity as CreateBarcodeActivity).createBarcode()
        }
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateQrCodeUrlFragment", "CreateQrCodeUrlFragment")
    }

    override fun getBarcodeSchema(): Schema {
//        return Url(edit_text.textString)
        return barcodeParser.parseSchema(BarcodeFormat.QR_CODE, edit_text.textString)
    }

    private fun showUrlPrefix() {
        val prefix = "https://"
        edit_text.apply {
            setText(prefix)
            setSelection(prefix.length)
            requestFocus()
        }
    }

    private fun handleTextChanged() {
        edit_text.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = edit_text.isNotBlank()
            generate_code_button.isEnabled = edit_text.isNotBlank()
        }
    }
}