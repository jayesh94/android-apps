package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import kotlinx.android.synthetic.main.dialog_edit_barcode_name.view.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class EditBarcodeNameDialogFragment : DialogFragment() {

    interface Listener {
        fun onNameConfirmed(name: String, barcode : Barcode?= null)
    }

    companion object {
        private const val NAME_KEY = "NAME_KEY"
        private const val BARCODE_KEY = "BARCODE_KEY"

        fun newInstance(name: String?, barcode: Barcode?= null): EditBarcodeNameDialogFragment {
            return EditBarcodeNameDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(NAME_KEY, name)
                    putSerializable(BARCODE_KEY, barcode)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = requireActivity() as? Listener ?: parentFragment as? Listener
        val name = arguments?.getString(NAME_KEY).orEmpty()
        val barcode = arguments?.getSerializable(BARCODE_KEY)

        val view = LayoutInflater
            .from(requireContext())
            .inflate(R.layout.dialog_edit_barcode_name, null, false)

        val dialog = AlertDialog.Builder(requireActivity(), R.style.DialogTheme)
            .setTitle(R.string.dialog_edit_barcode_name_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_edit_barcode_name_positive_button) { _, _ ->
                val newName = view.edit_text_barcode_name.text.toString()
                listener?.onNameConfirmed(newName, barcode as Barcode?)
            }
            .setNegativeButton(R.string.dialog_edit_barcode_name_negative_button, null)
            .create()

        dialog.setOnShowListener {
            initNameEditText(view.edit_text_barcode_name, name)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.main_orange))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }

        return dialog
    }

    private fun initNameEditText(editText: EditText, name: String) {
        editText.apply {
            setText(name)
            setSelection(name.length)
            requestFocus()
        }

        val manager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        manager?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("EditBarcodeNameDialogFragment", "EditBarcodeNameDialogFragment")
    }
}