package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

fun Long?.orZero(): Long {
    return this ?: 0L
}