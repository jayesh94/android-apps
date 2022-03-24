package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.orZero
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode

class DeleteConfirmationDialogFragment : DialogFragment() {

    companion object {
        private const val MESSAGE_ID_KEY = "MESSAGE_ID_KEY"
        private const val BARCODE_KEY = "BARCODE_KEY"

        fun newInstance(messageId: Int, barcode: Barcode?= null): DeleteConfirmationDialogFragment {
            return DeleteConfirmationDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(MESSAGE_ID_KEY, messageId)
                    putSerializable(BARCODE_KEY, barcode)
                }
                isCancelable = false
            }
        }
    }

    interface Listener {
        fun onDeleteConfirmed(barcode : Barcode?= null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = requireActivity() as? Listener ?: parentFragment as? Listener
        val messageId = arguments?.getInt(MESSAGE_ID_KEY).orZero()
        val barcode = arguments?.getSerializable(BARCODE_KEY)

        val dialog = AlertDialog.Builder(requireActivity(), R.style.DialogTheme)
            .setMessage(messageId)
            .setPositiveButton(R.string.dialog_delete_positive_button) { _, _ -> listener?.onDeleteConfirmed(
                barcode as Barcode?
            ) }
            .setNegativeButton(R.string.dialog_delete_negative_button, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.main_orange))
        }

        return dialog
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("DeleteConfirmationDialogFragment", "DeleteConfirmationDialogFragment")
    }

}