package info.ascetx.flashlight.app.updater

import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import info.ascetx.flashlight.MainActivity
import info.ascetx.flashlight.R
import java.lang.ref.WeakReference

class InAppUpdater(val activity: MainActivity) {

    companion object {

        private var appUpdateService: AppUpdateService? = null

        private var INSTANCE: InAppUpdater? = null

        @JvmStatic
        fun getInstance(mainActivity: MainActivity): InAppUpdater {
            return INSTANCE ?: InAppUpdater(mainActivity).apply { INSTANCE = this }
        }
    }

    fun checkInAppUpdates(activity: MainActivity) { // For showing immediate again
        Log.e("TAG IAU", "checkInAppUpdates")

        appUpdateService = AppUpdateService() // initializing this at onStart for relaunching immediate update.

        val contextWeakReference = WeakReference<Context>(activity)

        Log.e("TAG IAU", "checkInAppUpdates-contextWeakReference: " + contextWeakReference.get())
        Log.e("TAG IAU", "checkInAppUpdates-appUpdateService: $appUpdateService")

        contextWeakReference.get()?.let {weakContext ->
            appUpdateService?.setupAppUpdateManager(weakContext)
        }

        setupAppUpdateServiceObservers() // Placed here to avoid crash during Flexible update on onResume
    }

    /**
     * If the values of live data are still in place and I call this method then the .observe block will be executed.
     * This is making the app crash because this block is called from onResume everytime after a flexible update is dismissed.
     * Also, during a flexible update the onStop method is not called, hence, the live data is still preserved.
     * */
    private fun setupAppUpdateServiceObservers(){
        Log.e("TAG IAU", "setupAppUpdateServiceObservers")
        appUpdateService?.flexibleUpdateWithDays?.observe(activity, Observer {
            Log.e("TAG IAU", "flexibleUpdateWithDays: $it")
            if (it) {
                activity.faLogEvents.logInAppUpdateEvent("request_flexible_update_with_days")
                appUpdateService?.startFlexibleUpdateWithDays(
                    activity,
                    MainActivity.IN_APP_UPDATE_REQUEST_CODE
                )
            }
        })

        appUpdateService?.flexibleUpdateWithoutDays?.observe(activity, Observer {
            Log.e("TAG IAU", "flexibleUpdateWithoutDays: $it")
            if (it) {
                activity.faLogEvents.logInAppUpdateEvent("request_flexible_update_without_days")
                appUpdateService?.startFlexibleUpdateWithoutDays(
                    activity,
                    MainActivity.IN_APP_UPDATE_REQUEST_CODE
                )
            }
        })

        appUpdateService?.immediateUpdate?.observe(activity, Observer {
            Log.e("TAG IAU", "immediateUpdate: $it")
            if (it) {
                activity.faLogEvents.logInAppUpdateEvent("request_immediate_update")
                appUpdateService?.startImmediateUpdate(
                    activity,
                    MainActivity.IN_APP_UPDATE_REQUEST_CODE
                )
            }
        })

        appUpdateService?.updateDownloaded?.observe(activity, Observer {
            Log.e("TAG IAU", "appUpdateService: $it")
            if (it)
                notifyUser()
        })

        appUpdateService?.notifyUser?.observe(activity, Observer {
            Log.e("TAG IAU", "notifyUser: $it")
            if (it)
                notifyUser()
        })
    }

    private fun removeAppUpdateServiceObservers(){
        Log.e("TAG IAU", "removeAppUpdateServiceObservers")
        appUpdateService?.flexibleUpdateWithDays?.removeObservers(activity)
        appUpdateService?.flexibleUpdateWithoutDays?.removeObservers(activity)
        appUpdateService?.immediateUpdate?.removeObservers(activity)
        appUpdateService?.updateDownloaded?.removeObservers(activity)
        appUpdateService?.notifyUser?.removeObservers(activity)
    }


    private fun notifyUser(){
        activity.faLogEvents.logInAppUpdateEvent("update_downloaded_notify")
        popupSnackBarForCompleteUpdate()
    }

    private fun popupSnackBarForCompleteUpdate() { // Test

        Snackbar.make(
            activity.findViewById(android.R.id.content),
            activity.resources.getString(R.string.pupop_snackbar_in_app_update_message),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(activity.resources.getString(R.string.pupop_snackbar_in_app_update_button)) {
                activity.faLogEvents.logInAppUpdateEvent("update_downloaded_install_clicked")
                appUpdateService?.updateComplete()
                onStopCalled()
            }
            setActionTextColor(activity.resources.getColor(R.color.colorAccent))
            show()
        }
    }

    fun onResumeCalled() {
        Log.e("TAG IAU", "onResumeCalled")
        appUpdateService?.checkUpdateOnResume()
    }

    fun onStopCalled() {
        Log.e("TAG IAU", "onStopCalled")
        Log.e("TAG InAppUpdater", "unregisterUpdateListener")
        appUpdateService?.onStopCalled()
        removeAppUpdateServiceObservers()
        appUpdateService = null
    }

    // Found memory leak because of this static variable in Leak Canary.
    // Calling below in onStop method of activity resolves the issue. Saving 1.8mb of space
    fun clearActivityInstance(){
        Log.e("TAG IAU", "clearActivityInstance")
        INSTANCE = null
    }
}

