package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.isNotBlank
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.textString
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.BaseCreateBarcodeFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Email
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import kotlinx.android.synthetic.main.fragment_create_qr_code_email.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class CreateQrCodeEmailFragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitleEditText()
        handleTextChanged()

        generate_code_button.setOnClickListener {
            (activity as CreateBarcodeActivity).createBarcode()
        }
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateQrCodeEmailFragment", "CreateQrCodeEmailFragment")
    }

    override fun getBarcodeSchema(): Schema {
        return Email(
            email = edit_text_email.textString,
            subject = edit_text_subject.textString,
            body = edit_text_message.textString
        )
    }

    private fun initTitleEditText() {
        edit_text_email.requestFocus()
    }

    private fun handleTextChanged() {
        edit_text_email.addTextChangedListener { toggleCreateBarcodeButton() }
        edit_text_subject.addTextChangedListener { toggleCreateBarcodeButton() }
        edit_text_message.addTextChangedListener { toggleCreateBarcodeButton() }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = edit_text_email.isNotBlank() || edit_text_subject.isNotBlank() || edit_text_message.isNotBlank()
        generate_code_button.isEnabled = edit_text_email.isNotBlank() || edit_text_subject.isNotBlank() || edit_text_message.isNotBlank()
    }
}