package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema

import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.joinToStringNotNullOrBlankWithLineSeparator
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.removePrefixIgnoreCase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.startsWithIgnoreCase

class Mms(
    val phone: String? = null,
    val subject: String? = null,
    val message: String? = null
) : Schema {

    companion object {
        private const val PREFIX = "mmsto:"
        private const val SEPARATOR = ":"

        fun parse(text: String): Mms? {
            if (text.startsWithIgnoreCase(PREFIX).not()) {
                return null
            }

            val parts = text.removePrefixIgnoreCase(PREFIX).split(SEPARATOR)
            return Mms(
                phone = parts.getOrNull(0),
                subject = parts.getOrNull(1),
                message = parts.getOrNull(2)
            )
        }
    }

    override val schema = BarcodeSchema.MMS

    override fun toFormattedText(): String {
        return listOf(phone, subject, message).joinToStringNotNullOrBlankWithLineSeparator()
    }

    override fun toBarcodeText(): String {
        return PREFIX +
                phone.orEmpty() +
                "$SEPARATOR${subject.orEmpty()}" +
                "$SEPARATOR${message.orEmpty()}"
    }
}