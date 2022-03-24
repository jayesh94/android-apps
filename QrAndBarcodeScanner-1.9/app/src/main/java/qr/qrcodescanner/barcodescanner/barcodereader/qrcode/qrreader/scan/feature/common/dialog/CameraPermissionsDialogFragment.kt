package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents


class CameraPermissionsDialogFragment(private val permissionDeniedBefore: Boolean, private val shouldShowRequestPermission: Boolean = false) : DialogFragment() {


    interface Listener {
        fun showCameraPermissionDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = parentFragment as? Listener

        val builder = AlertDialog.Builder(requireActivity(), R.style.DialogTheme)
        builder.setTitle(getString(R.string.camera_permissions_title))
        builder.setIcon(R.drawable.ic_baseline_camera)

        return if (permissionDeniedBefore && !shouldShowRequestPermission) {
            setDialogWhenDontAskAgain(builder)
        } else{
            setDialogWhenDenied(builder, listener)
        }

    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("CameraPermissionsDialogFragment", "CameraPermissionsDialogFragment")
    }

    private fun setDialogWhenDenied(builder: AlertDialog.Builder, listener: Listener?): AlertDialog {
        builder.setMessage(R.string.camera_permissions_request_permissions)
        builder.setPositiveButton(R.string.camera_permissions_request_permissions_button_text) { _, _ -> listener?.showCameraPermissionDialog() }
        val dialog = builder.create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_orange))
        }

        return dialog
    }

    private fun setDialogWhenDontAskAgain(builder: AlertDialog.Builder): AlertDialog {
        builder.setMessage(
            getString(R.string.camera_permissions_app_settings_l1) + "\n\n" +
                    getString(R.string.camera_permissions_app_settings_l2) + "\n" +
                    getString(R.string.camera_permissions_app_settings_l3) + "\n" +
                    getString(R.string.camera_permissions_app_settings_l4)
        )
        builder.setPositiveButton(R.string.camera_permissions_app_settings_button_text) { _, _ ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent) }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_orange))

            // if you do the following it will be left aligned, doesn't look
            // correct
            // button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play,
            // 0, 0, 0);


            val drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_arrow_right);
            if (drawable != null) {
                DrawableCompat.setTint(drawable, ContextCompat.getColor(requireContext(), R.color.main_orange))
            }

            // set the bounds to place the drawable a bit right
            drawable?.setBounds(
                (drawable.intrinsicWidth * 0.3).toInt(),
                0, (drawable.intrinsicWidth * 1.5).toInt(),
                drawable.intrinsicHeight
            )
            positiveButton.setCompoundDrawables(null, null, drawable, null)

            // could modify the placement more here if desired
//             positiveButton.setCompoundDrawablePadding();
        }

        return dialog
    }
}