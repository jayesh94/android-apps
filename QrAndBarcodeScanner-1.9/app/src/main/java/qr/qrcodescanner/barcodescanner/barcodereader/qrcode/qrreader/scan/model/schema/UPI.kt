package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema

import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.startsWithAnyIgnoreCase

class UPI(val url: String) : Schema {

    companion object {
        private val PREFIXES = listOf("upi://pay")

        fun parse(text: String): UPI? {
            if (text.startsWithAnyIgnoreCase(PREFIXES).not()) {
                return null
            }
            return UPI(text)
        }
    }

    override val schema = BarcodeSchema.UPI
    override fun toFormattedText(): String = url
    override fun toBarcodeText(): String = url
}