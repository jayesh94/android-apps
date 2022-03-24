package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.qr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.textString
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.BaseCreateBarcodeFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Contact
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.Schema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.VCard
import kotlinx.android.synthetic.main.fragment_create_qr_code_vcard.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class CreateQrCodeVCardFragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_vcard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_text_first_name.requestFocus()
        parentActivity.isCreateBarcodeButtonEnabled = true
        generate_code_button.isEnabled = true
        generate_code_button.setOnClickListener() {
            (activity as CreateBarcodeActivity).createBarcode()
        }
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateQrCodeVCardFragment", "CreateQrCodeVCardFragment")
    }

    override fun getBarcodeSchema(): Schema {
       return VCard(
           firstName = edit_text_first_name.textString,
           lastName = edit_text_last_name.textString,
           organization = edit_text_organization.textString,
           title = edit_text_job.textString,
           email = edit_text_email.textString,
           phone = edit_text_phone.textString,
//           secondaryPhone = edit_text_fax.textString,
           address = edit_text_address.textString,
           url = edit_text_website.textString
       )
    }

    override fun showContact(contact: Contact) {
        contact.firstName?.let { logError(it) }
        contact.lastName?.let { logError(it) }
        contact.email?.let { logError(it) }
        contact.phone?.let { logError(it) }
        contact.country?.let { logError(it) }
        contact.address?.let { logError(it) }
        contact.formattedAddress?.let { logError(it) }

//        TODO we are not getting complete data from contacts (Job et al); refer teacapps scanner app

        edit_text_first_name.setText(contact.firstName)
        edit_text_last_name.setText(contact.lastName)
        edit_text_email.setText(contact.email)
        edit_text_phone.setText(contact.phone)
        edit_text_address.setText(contact.formattedAddress)
    }

    private fun logError(msg : String){
        Log.e("CreateQrCVCardFragment:", msg)
    }
}