package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.ErrorDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.Logger

val Fragment.packageManager: PackageManager
    get() = requireContext().packageManager

fun Fragment.showError(error: Throwable?) {
    try {
        val errorDialog = ErrorDialogFragment.newInstance(requireContext(), error)
        errorDialog.show(childFragmentManager, "")
    } catch (e: Exception){
        Logger.log(e)
    }
}
