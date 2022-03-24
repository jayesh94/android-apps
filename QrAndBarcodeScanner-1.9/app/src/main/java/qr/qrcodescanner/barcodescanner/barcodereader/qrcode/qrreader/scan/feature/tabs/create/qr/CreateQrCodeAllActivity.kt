package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.qr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.create.CreateBarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.BarcodeSchema
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.activity_create_qr_code_all.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.AppRater

class CreateQrCodeAllActivity : BaseActivity() {

    companion object {

        fun start(context: Context) {
            val intent = Intent(context, CreateQrCodeAllActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_qr_code_all)
        //supportEdgeToEdge()
        handleToolbarBackClicked()
        handleButtonsClicked()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CreateQrCodeAllActivity", "CreateQrCodeAllActivity")
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
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

    private fun handleButtonsClicked() {
        button_text.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.OTHER) }
        button_url.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.URL) }
        button_wifi.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.WIFI) }
        button_location.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.GEO) }
//        button_otp.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.OTP_AUTH) }
        button_contact_vcard.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.VCARD) }
//        button_contact_mecard.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.MECARD) }
        button_event.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.VEVENT) }
        button_phone.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.PHONE) }
        button_email.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.EMAIL) }
        button_sms.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.SMS) }
//        button_mms.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.MMS) }
        button_cryptocurrency.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.CRYPTOCURRENCY) }
        button_bookmark.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.BOOKMARK) }
        button_app.setOnClickListener { CreateBarcodeActivity.start(this, BarcodeFormat.QR_CODE, BarcodeSchema.APP) }
    }
}