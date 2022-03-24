package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema

enum class BarcodeSchema {
    APP,
    BOOKMARK,
    CRYPTOCURRENCY,
    EMAIL,
    GEO,
    GOOGLE_MAPS,
    MMS,
    MECARD,
    OTP_AUTH,
    PHONE,
    SMS,
    URL,
    VEVENT,
    VCARD,
    WIFI,
    YOUTUBE,
    UPI,
    NZCOVIDTRACER,
    OTHER;
}

interface Schema {
    val schema: BarcodeSchema
    fun toFormattedText(): String
    fun toBarcodeText(): String
}