package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.textString
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.BaseCreateBarcodeFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.VEvent
import kotlinx.android.synthetic.main.fragment_create_qr_code_vevent.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class CreateQrCodeEventFragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_vevent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_text_title.requestFocus()
        parentActivity.isCreateBarcodeButtonEnabled = true
        generate_code_button.isEnabled = true
        generate_code_button.setOnClickListener() {
            (activity as CreateBarcodeActivity).createBarcode()
        }
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateQrCodeEventFragment", "CreateQrCodeEventFragment")
    }

    override fun getBarcodeSchema(): Schema {
        return VEvent(
            uid = edit_text_title.textString,
            organizer = edit_text_organizer.textString,
            summary = edit_text_summary.textString,
            startDate = button_date_time_start.dateTime,
            endDate = button_date_time_end.dateTime
        )
    }
}