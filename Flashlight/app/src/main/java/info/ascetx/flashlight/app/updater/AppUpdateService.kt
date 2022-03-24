package info.ascetx.flashlight.app.updater

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import info.ascetx.flashlight.BuildConfig
import info.ascetx.flashlight.app.configs.RemoteConfigs

/**
 * This class was designed to resolve memory leak issue with InstallStateUpdatedListener
 * */
class AppUpdateService : InstallStateUpdatedListener {

    val flexibleUpdateWithDays: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val flexibleUpdateWithoutDays: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val immediateUpdate: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val updateDownloaded: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val notifyUser: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    private val updateInfo: MutableLiveData<AppUpdateInfo> by lazy { MutableLiveData<AppUpdateInfo>() }

    private var appUpdateManager : AppUpdateManager? = null
    private var appUpdateInfoTask: Task<AppUpdateInfo>? = null

    override fun onStateUpdate(state: InstallState) {
        Log.e("TAG AUS", "onStateUpdate")
        // (Optional) Provide a download progress bar.
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            // Show update progress bar.
        }

        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            Log.e("TAG AUS", "onStateUpdate DOWNLOADED")
            notifyUser.value = true
        }
        // Log state or install the update.
    }

    fun setupAppUpdateManager(context: Context){
        Log.e("TAG AUS", "setupAppUpdateManager")
        appUpdateManager = AppUpdateManagerFactory.create(context)
        appUpdateManager?.registerListener(this)
        checkForUpdate()
    }

    fun onStopCalled(){
        Log.e("TAG AUS", "onStopCalled")
        appUpdateManager?.unregisterListener(this)
        appUpdateInfoTask = null
        appUpdateManager = null
    }

    fun checkForUpdate(){
        Log.e("TAG AUS", "checkForUpdate")
        Log.e("TAG AUS", "getHighestUpdatePriority BAT: \n " + getHighestUpdatePriority())

        val remoteConfigsUpdaterData = RemoteConfigs.inAppUpdaterConfigs().inAppUpdater
        val daysForFlexibleUpdate = remoteConfigsUpdaterData.daysForFlexibleUpdate
        val priorityThresholdFlexibleUpdate = remoteConfigsUpdaterData.priorityThresholdFlexibleUpdate
        val priorityThresholdImmediateUpdate = remoteConfigsUpdaterData.priorityThresholdImmediateUpdate

        Log.e("TAG AUS", "daysForFlexibleUpdate BAT: \n $daysForFlexibleUpdate")
        Log.e("TAG AUS",
            "priorityThresholdFlexibleUpdate BAT: \n $priorityThresholdFlexibleUpdate"
        )
        Log.e("TAG AUS",
            "priorityThresholdImmediateUpdate BAT: \n $priorityThresholdImmediateUpdate"
        )

        appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->


            val highestUpdatePriority = getHighestUpdatePriority()
            Log.e("TAG AUS", "highestUpdatePriority: \n $highestUpdatePriority")

            when {
                highestUpdatePriority < priorityThresholdFlexibleUpdate -> return@addOnSuccessListener
                (highestUpdatePriority in priorityThresholdFlexibleUpdate until (priorityThresholdImmediateUpdate - 1))->
                    requestFlexibleUpdateWithDays(appUpdateInfo)
                highestUpdatePriority == (priorityThresholdImmediateUpdate - 1) -> requestFlexibleUpdateWithoutDays(appUpdateInfo)
                highestUpdatePriority >= priorityThresholdImmediateUpdate -> requestImmediateUpdate(appUpdateInfo)
            }
        }
    }

    private fun getHighestUpdatePriority(): Int {
        Log.e("TAG AUS", "getHighestUpdatePriority")
        val currentAppVersionCode = BuildConfig.VERSION_CODE
        val updateInfo = RemoteConfigs.updateInfoConfigs()
        val availableUpdates = updateInfo.updates
            .filter { it.versionCode > currentAppVersionCode }
            .sortedByDescending { it.updatePriority }

        Log.e("TAG AUS", "currentAppVersionCode: \n $currentAppVersionCode")

        return if (availableUpdates.isNotEmpty())
            availableUpdates[0].updatePriority
        else
            0
    }

    fun startFlexibleUpdateWithDays(activity: Activity, code: Int){
        Log.e("TAG AUS", "startFlexibleUpdateWithDays")
        updateInfo.value?.let { appUpdateManager?.startUpdateFlowForResult(it, AppUpdateType.FLEXIBLE, activity, code) }
    }

    fun startFlexibleUpdateWithoutDays(activity: Activity, code: Int){
        Log.e("TAG AUS", "startFlexibleUpdateWithoutDays")
        updateInfo.value?.let { appUpdateManager?.startUpdateFlowForResult(it, AppUpdateType.FLEXIBLE, activity, code) }
    }

    fun startImmediateUpdate(activity: Activity, code: Int){
        Log.e("TAG AUS", "startImmediateUpdate")
        updateInfo.value?.let { appUpdateManager?.startUpdateFlowForResult(it, AppUpdateType.IMMEDIATE, activity, code) }
    }

    private fun requestImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        Log.e("TAG AUS", "requestImmediateUpdate")

        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            Log.e("TAG AUS", "requestImmediateUpdate2")
            updateInfo.value = appUpdateInfo
            immediateUpdate.value = true

        } else {
            Log.e("TAG AUS", "requestImmediateUpdate3")
            updateInfo.value = null
            immediateUpdate.value = false
        }
    }

    private fun requestFlexibleUpdateWithoutDays(appUpdateInfo: AppUpdateInfo) {
        Log.e("TAG AUS", "requestFlexibleUpdateWithoutDays1")
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            Log.e("TAG AUS", "requestFlexibleUpdateWithoutDays2")
            updateInfo.value = appUpdateInfo
            flexibleUpdateWithoutDays.value = true

        } else {
            Log.e("TAG AUS", "requestFlexibleUpdateWithoutDays3")
            updateInfo.value = null
            flexibleUpdateWithoutDays.value = false
        }
    }

    private fun requestFlexibleUpdateWithDays(appUpdateInfo: AppUpdateInfo) {
        Log.e("TAG AUS", "requestFlexibleUpdateWithDays1")
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            && (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= RemoteConfigs.inAppUpdaterConfigs().inAppUpdater.daysForFlexibleUpdate
//              Above code (clientVersionStalenessDays) checks for how many days the next popup be delayed from
//              the Day the User was Notified about the update. This includes the first time too!
            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            Log.e("TAG AUS", "requestFlexibleUpdateWithDays2")
            updateInfo.value = appUpdateInfo
            flexibleUpdateWithDays.value = true

        } else {
            Log.e("TAG AUS", "requestFlexibleUpdateWithDays3")
            updateInfo.value = null
            flexibleUpdateWithDays.value = false
        }
    }

    fun updateComplete(){
        Log.e("TAG AUS", "updateComplete")
        appUpdateManager?.completeUpdate()
        appUpdateManager?.unregisterListener(this)
    }

    fun checkUpdateOnResume(){
        Log.e("TAG AUS", "checkUpdateOnResume")
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                Log.e("TAG AUS", "checkUpdateOnResume DOWNLOADED")
                updateDownloaded.value = true
            }
            if(it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                // If an in-app update is already running, resume the update.
                Log.e("TAG AUS", "checkUpdateOnResume DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS")
                requestImmediateUpdate(it)
            }
        }
    }

}