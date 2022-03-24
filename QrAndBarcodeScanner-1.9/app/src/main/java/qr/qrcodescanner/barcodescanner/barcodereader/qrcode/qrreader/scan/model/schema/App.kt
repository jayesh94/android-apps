package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema

import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.removePrefixIgnoreCase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.startsWithAnyIgnoreCase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy

class App(val url: String) : Schema {

    companion object {
        private val PREFIXES = listOf("market://details?id=", "market://search", "http://play.google.com/", "https://play.google.com/", "https://play.google.com/store/apps/details?id=")

        fun parse(text: String): App? {
            if (text.startsWithAnyIgnoreCase(PREFIXES).not()) {
                return null
            }
            return App(text)
        }

        fun fromPackage(packageName: String): App {
            return App(PREFIXES[4] + packageName)
        }
    }

    override val schema = BarcodeSchema.APP
    override fun toFormattedText(): String = url
    override fun toBarcodeText(): String = url

    val appPackage by unsafeLazy {
        url.removePrefixIgnoreCase(PREFIXES[4])
    }
}