package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

fun Int?.orZero(): Int {
    return this ?: 0
}