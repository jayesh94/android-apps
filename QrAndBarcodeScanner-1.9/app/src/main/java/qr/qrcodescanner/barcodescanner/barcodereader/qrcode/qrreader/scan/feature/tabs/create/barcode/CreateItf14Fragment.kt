package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.barcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.textString
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.BaseCreateBarcodeFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Other
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import kotlinx.android.synthetic.main.fragment_create_itf_14.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class CreateItf14Fragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_itf_14, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_text.requestFocus()
        edit_text.addTextChangedListener {
            parentActivity.isCreateBarcodeButtonEnabled = edit_text.text?.length?.rem(2) == 0
            generate_code_button.isEnabled = edit_text.text?.length?.rem(2) == 0
        }
		generate_code_button.setOnClickListener() {
            (activity as CreateBarcodeActivity).createBarcode()
        }
    }

    override fun getBarcodeSchema(): Schema {
        return Other(edit_text.textString)
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateItf14Fragment", "CreateItf14Fragment")
    }
}