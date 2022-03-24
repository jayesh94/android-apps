package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

fun Double?.orZero(): Double {
    return this ?: 0.0
}