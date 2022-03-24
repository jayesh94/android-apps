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
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Geo
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import kotlinx.android.synthetic.main.fragment_create_qr_code_location.*
import kotlinx.android.synthetic.main.fragment_create_qr_code_location.generate_code_button
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class CreateQrCodeLocationFragment : BaseCreateBarcodeFragment() {

    override val latitude: Double?
        get() = edit_text_latitude.textString.toDoubleOrNull()

    override val longitude: Double?
        get() = edit_text_longitude.textString.toDoubleOrNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_location, container, false)
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateQrCodeLocationFragment", "CreateQrCodeLocationFragment")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLatitudeEditText()
        handleTextChanged()

        generate_code_button.setOnClickListener() {
            (activity as CreateBarcodeActivity).createBarcode()
        }
    }

    override fun getBarcodeSchema(): Schema {
       return Geo(
           latitude = edit_text_latitude.textString,
           longitude = edit_text_longitude.textString,
           altitude = edit_text_altitude.textString
       )
    }

    override fun showLocation(latitude: Double?, longitude: Double?) {
        latitude?.apply {
            edit_text_latitude.setText(latitude.toString())
        }
        longitude?.apply {
            edit_text_longitude.setText(longitude.toString())
        }
    }

    private fun initLatitudeEditText() {
        edit_text_latitude.requestFocus()
    }

    private fun handleTextChanged() {
        edit_text_latitude.addTextChangedListener { toggleCreateBarcodeButton() }
        edit_text_longitude.addTextChangedListener { toggleCreateBarcodeButton() }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = edit_text_latitude.isNotBlank() && edit_text_longitude.isNotBlank()
        generate_code_button.isEnabled = edit_text_latitude.isNotBlank() && edit_text_longitude.isNotBlank()
    }
}