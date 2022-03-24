package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

fun StringBuilder.appendIfNotNullOrBlank(prefix: String = "", value: String?, suffix: String = ""): StringBuilder {
    if (value.isNullOrBlank().not()) {
        append(prefix)
        append(value)
        append(suffix)
    }
    return this
}