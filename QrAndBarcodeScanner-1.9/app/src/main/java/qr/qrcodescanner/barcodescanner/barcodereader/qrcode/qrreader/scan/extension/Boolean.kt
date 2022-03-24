package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

fun Boolean?.orFalse(): Boolean {
    return this ?: false
}