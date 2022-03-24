package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.otp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.view.isVisible
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.otpGenerator
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.orZero
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.OtpAuth
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_barcode_otp.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater.AppRater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.SetStatusBar
import java.util.concurrent.TimeUnit

class OtpActivity : BaseActivity() {

    companion object {
        private const val OTP_KEY = "OTP_KEY"

        fun start(context: Context, opt: OtpAuth) {
            val intent = Intent(context, OtpActivity::class.java).apply {
                putExtra(OTP_KEY, opt)
            }
            context.startActivity(intent)
        }
    }

    private val disposable = CompositeDisposable()
    private lateinit var otp: OtpAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_otp)
        enableSecurity()
        //supportEdgeToEdge()
        parseOtp()
        handleToolbarBackClicked()
        handleRefreshOtpClicked()
        showOtp()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("OtpActivity", "OtpActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun enableSecurity() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun parseOtp() {
        otp = intent?.getSerializableExtra(OTP_KEY) as OtpAuth
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

    private fun handleRefreshOtpClicked() {
        button_refresh.setOnClickListener {
            refreshOtp()
        }
    }

    private fun refreshOtp() {
        otp = otp.copy(counter = otp.counter.orZero() + 1L)
        showOtp()
    }

    private fun showOtp() {
        when (otp.type) {
            OtpAuth.HOTP_TYPE -> showHotp()
            OtpAuth.TOTP_TYPE -> showTotp()
        }
        text_view_password.text = otpGenerator.generateOTP(otp) ?: getString(R.string.activity_barcode_otp_unable_to_generate_otp)
    }

    private fun showHotp() {
        button_refresh.isVisible = true
        text_view_counter.isVisible = true
        text_view_counter.text = getString(R.string.activity_barcode_otp_counter, otp.counter.orZero().toString())
    }

    private fun showTotp() {
        text_view_timer.isVisible = true
        startTimer()
    }

    private fun startTimer() {
        val period = otp.period ?: 30
        val currentTimeInSeconds = System.currentTimeMillis() / 1000
        val secondsPassed = currentTimeInSeconds % period
        val secondsLeft = period - secondsPassed

        Observable
            .interval(1, TimeUnit.SECONDS)
            .map { it + 1 }
            .take(secondsLeft)
            .map { secondsLeft - it }
            .startWith(secondsLeft)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { showOtp() }
            .subscribe(::showTime)
            .addTo(disposable)
    }

    private fun showTime(secondsLeft: Long) {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        text_view_timer.text = getString(R.string.activity_barcode_otp_timer, minutes.toTime(), seconds.toTime())
    }

    private fun Long.toTime(): String {
        return if (this >= 10) {
            this.toString()
        } else {
            "0$this"
        }
    }
}