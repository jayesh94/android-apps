package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.schema.BarcodeSchema
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.BarcodeDatabaseTypeConverter
import com.google.zxing.BarcodeFormat
import java.io.Serializable

@Entity(tableName = "codes")
@TypeConverters(BarcodeDatabaseTypeConverter::class)
data class Barcode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String? = null,
    val text: String,
    val formattedText: String,
    val format: BarcodeFormat,
    val schema: BarcodeSchema,
    val date: Long,
    val isGenerated: Boolean = false,
    val isFavorite: Boolean = false,
    val errorCorrectionLevel: String? = null,
    val country: String? = null
) : Serializable