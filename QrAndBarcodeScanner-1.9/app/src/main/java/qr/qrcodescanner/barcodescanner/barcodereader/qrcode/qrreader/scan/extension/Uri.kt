package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

import android.net.Uri

fun Uri.Builder.appendQueryParameterIfNotNullOrBlank(key: String, value: String?): Uri.Builder {
    if (value.isNullOrBlank().not()) {
        appendQueryParameter(key, value)
    }
    return this
}