package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

import android.widget.EditText

fun EditText.isNotBlank(): Boolean {
    return text.isNotBlank()
}

val EditText.textString: String
    get() = text.toString()
