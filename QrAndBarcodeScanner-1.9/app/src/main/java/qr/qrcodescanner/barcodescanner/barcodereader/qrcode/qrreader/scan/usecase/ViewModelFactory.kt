package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for ViewModels
 */
class ViewModelFactory(private val dataSource: BarcodeDatabase) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarcodeViewModel::class.java)) {
            return BarcodeViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}