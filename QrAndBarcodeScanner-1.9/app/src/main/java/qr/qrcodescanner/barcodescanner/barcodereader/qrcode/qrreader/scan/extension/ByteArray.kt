package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

import org.apache.commons.codec.binary.Base32

fun ByteArray.encodeBase32(): String {
    return Base32().encodeAsString(this)
}