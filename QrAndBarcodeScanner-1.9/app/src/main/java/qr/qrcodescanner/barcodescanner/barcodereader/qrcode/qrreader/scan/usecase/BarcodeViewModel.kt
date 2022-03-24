package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import androidx.lifecycle.ViewModel
import io.reactivex.Flowable

/**
 * View Model for the [BarcodeHistoryListFragment]
 */
class BarcodeViewModel(private val dataSource: BarcodeDatabase) : ViewModel() {

    /**
     * Get the count of all history.
     * @return a [Flowable] that will emit every time the count changes
     */
    // for every emission of the user, get the user name
    fun getAllCount(): Flowable<Int> {
        return dataSource.getAllCount()
    }
    /**
     * Get the count of all favorites.
     * @return a [Flowable] that will emit every time the count changes
     */
    // for every emission of the user, get the user name
    fun getFavoritesCount(): Flowable<Int> {
        return dataSource.getFavoritesCount()
    }
}