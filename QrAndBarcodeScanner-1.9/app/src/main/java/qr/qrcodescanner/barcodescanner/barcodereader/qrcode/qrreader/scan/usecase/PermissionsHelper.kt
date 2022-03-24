package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.loge
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.CameraPermissionsDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.ContactsPermissionsDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.StoragePermissionsDialogFragment

const val PERMISSION_REQUEST_CODE = 101

object PermissionsHelper {

    const val sharedPrefCameraPermissionDeniedKey = "USER_ASKED_CAMERA_PERMISSION_DENIED_BEFORE"
    const val sharedPrefStoragePermissionDeniedKey = "USER_ASKED_FOLDER_PERMISSION_DENIED_BEFORE"
    const val sharedPrefContactsPermissionDeniedKey = "USER_ASKED_CONTACT_PERMISSION_DENIED_BEFORE"

    lateinit var sharedPreferences: SharedPreferences

    fun requestPermissions(activity: AppCompatActivity, permissions: Array<out String>, requestCode: Int) {
        loge("requestPermissions")

        sharedPreferences = activity.getSharedPreferences("SHARED_PREFERENCES_NAME", Context.MODE_PRIVATE)

        if (areAllPermissionsGranted(activity, permissions)) {
            activity.onRequestPermissionsResult(
                requestCode,
                permissions,
                IntArray(permissions.size) { PackageManager.PERMISSION_GRANTED }
            )
            return
        }

        val notGrantedPermissions = permissions.filterNot { isPermissionGranted(activity, it) }
        ActivityCompat.requestPermissions(activity, notGrantedPermissions.toTypedArray(), requestCode)
    }

    fun requestNotGrantedPermissions(activity: AppCompatActivity, permissions: Array<out String>, requestCode: Int) {
        loge("requestNotGrantedPermissions")
        val notGrantedPermissions = permissions.filterNot { isPermissionGranted(activity, it) }
        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, notGrantedPermissions.toTypedArray(), requestCode)
        }
    }

    fun areAllPermissionsGranted(context: Context, permissions: Array<out String>): Boolean {
        loge("areAllPermissionsGranted(context: Context, permissions: Array<out String>)")
        permissions.forEach { permission ->
            if (isPermissionGranted(context, permission).not()) {
                return false
            }
        }
        return true
    }

    fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        loge("areAllPermissionsGranted(grantResults: IntArray):")
        grantResults.forEach { result ->
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        loge("isPermissionGranted")
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }


    fun checkPermissions(activity: FragmentActivity, permissions: Array<out String>): Boolean {

        sharedPreferences = activity.getSharedPreferences("SHARED_PREFERENCES_NAME", Context.MODE_PRIVATE)

        // Check if the Camera permission has been granted
        return if (ActivityCompat.checkSelfPermission(activity, permissions[0]) ==
            PackageManager.PERMISSION_GRANTED) {
            loge("checkSelfPermissionCompat YES")
            // Permission is already available, start camera preview
            true
        } else {
            loge("checkSelfPermissionCompat NO")
            // Permission is missing and must be requested.
            false
        }
    }

    fun requestCameraPermission(
        fragmentManager: FragmentManager,
        activity: FragmentActivity,
        permissions: Array<out String>
    ) {
        loge("requestPermission")

        val permissionDeniedBefore: Boolean = sharedPreferences.getBoolean(
            sharedPrefCameraPermissionDeniedKey, false)

        loge("permissionDeniedBefore: $permissionDeniedBefore")

        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
            loge("shouldShowRequestPermissionRationaleCompat YES")
            loge("Dialog: Why we need Camera permission? With Allow as positive button. In Main UI")

            val dialog = CameraPermissionsDialogFragment(permissionDeniedBefore, true)
            dialog.show(fragmentManager, "")

        } else {
            loge("shouldShowRequestPermissionRationaleCompat NO")
            loge("permissionDeniedBefore: $permissionDeniedBefore")

            val dialog = CameraPermissionsDialogFragment(permissionDeniedBefore)
            dialog.show(fragmentManager, "")
        }
    }

    fun requestFolderPermission(
        activity: BaseActivity,
        permissions: Array<out String>
    ) {
        loge("requestPermission")

        val permissionDeniedBefore: Boolean = sharedPreferences.getBoolean(
            sharedPrefStoragePermissionDeniedKey, false)

        loge("permissionDeniedBefore: $permissionDeniedBefore")

        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
            loge("shouldShowRequestPermissionRationaleCompat YES")
            loge("Dialog: Why we need Camera permission? With Allow as positive button. In Main UI")

            val dialog = StoragePermissionsDialogFragment(permissionDeniedBefore, true)
            dialog.show(activity.supportFragmentManager, "")

        } else {
            loge("shouldShowRequestPermissionRationaleCompat NO")
            loge("permissionDeniedBefore: $permissionDeniedBefore")

            val dialog = StoragePermissionsDialogFragment(permissionDeniedBefore)
            dialog.show(activity.supportFragmentManager, "")
        }
    }

    fun requestContactPermission(activity: BaseActivity, permissions: Array<out String>) {
        loge("requestPermission")

        val permissionDeniedBefore: Boolean = sharedPreferences.getBoolean(
            sharedPrefContactsPermissionDeniedKey, false)

        loge("permissionDeniedBefore: $permissionDeniedBefore")

        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
            loge("shouldShowRequestPermissionRationaleCompat YES")
            loge("Dialog: Why we need Camera permission? With Allow as positive button. In Main UI")

            val dialog = ContactsPermissionsDialogFragment(permissionDeniedBefore, true)
            dialog.show(activity.supportFragmentManager, "")

        } else {
            loge("shouldShowRequestPermissionRationaleCompat NO")
            loge("permissionDeniedBefore: $permissionDeniedBefore")

            val dialog = ContactsPermissionsDialogFragment(permissionDeniedBefore)
            dialog.show(activity.supportFragmentManager, "")
        }
    }
}