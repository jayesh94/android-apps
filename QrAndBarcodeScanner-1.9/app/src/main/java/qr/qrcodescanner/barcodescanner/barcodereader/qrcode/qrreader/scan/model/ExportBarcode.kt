package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model

import androidx.room.TypeConverters
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.BarcodeDatabaseTypeConverter
import com.google.zxing.BarcodeFormat

@TypeConverters(BarcodeDatabaseTypeConverter::class)
data class ExportBarcode(
    val date: Long,
    val format: BarcodeFormat,
    val text: String
)