package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import com.google.zxing.Result

fun Result.equalTo(barcode: Barcode?): Boolean {
    return barcodeFormat == barcode?.format && text == barcode?.text
}