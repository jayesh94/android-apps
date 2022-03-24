package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.scan

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.databinding.FragmentScanBarcodeFromCameraOrFileBinding
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.permissionsHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.loge
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.CameraPermissionsDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.scan.file.ScanBarcodeFromFileActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.PermissionsHelper
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.PermissionsHelper.areAllPermissionsGranted


class ScanBarcodeFromCameraOrFileFragment : Fragment(), CameraPermissionsDialogFragment.Listener, ActivityCompat.OnRequestPermissionsResultCallback {

    interface Listener {
        fun uncheckBottomNavItemsOpenScanBarcodeFromCameraFragment()
    }

    private var _binding: FragmentScanBarcodeFromCameraOrFileBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBarcodeFromCameraOrFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleScanUsingCamera()
        handleScanUsingImage()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("ScanBarcodeFromCameraOrFileFragment", "ScanBarcodeFromCameraOrFileFragment")
        if(checkPermissions()){
            openScanBarcodeFromCameraFragment()
        }
    }

    private fun checkPermissions(): Boolean {
        return permissionsHelper.checkPermissions(requireActivity(), PERMISSIONS)
    }

    private fun handleScanUsingImage() {
        binding.scanUsingFileBtn.setOnClickListener() {
            navigateToScanFromFileScreen()
        }
    }

    private fun navigateToScanFromFileScreen() {
        ScanBarcodeFromFileActivity.start(requireActivity())
    }

    private fun handleScanUsingCamera() {
        binding.scanUsingCameraBtn.setOnClickListener() {
            loge("handleScanUsingCamera")
            permissionsHelper.requestCameraPermission(childFragmentManager, requireActivity(), PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        loge("onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && areAllPermissionsGranted(grantResults)) {
            loge("onRequestPermissionsResult Permission has been granted")
            faLogEvents.logCustomCameraPermissionEvent("scan_barcode_camera_or_file_granted")
            openScanBarcodeFromCameraFragment()
        } else {
            loge("onRequestPermissionsResult Permission request was denied.")
            faLogEvents.logCustomCameraPermissionEvent("scan_barcode_camera_or_file_denied")
            // Set sharedPreference as true show that the permission was denied at least once.
            with (PermissionsHelper.sharedPreferences.edit()) {
                putBoolean(PermissionsHelper.sharedPrefCameraPermissionDeniedKey, true)
                apply()
            }
        }
    }

    private fun openScanBarcodeFromCameraFragment() {
        val listener = requireActivity() as Listener
        listener.uncheckBottomNavItemsOpenScanBarcodeFromCameraFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showCameraPermissionDialog() {
        loge("showCameraPermissionDialog")
        requestPermissions(
            PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }
}