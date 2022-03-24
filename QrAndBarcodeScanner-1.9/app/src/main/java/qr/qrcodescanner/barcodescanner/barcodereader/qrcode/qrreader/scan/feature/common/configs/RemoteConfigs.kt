package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.configs

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.BuildConfig
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.BottomTabsActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.remoteConfigs.InAppRaterParams
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.remoteConfigs.InAppUpdaterParams
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.remoteConfigs.UpdateInfo

object RemoteConfigs {

   const val IN_APP_RATER_CONFIG_KEY = "in_app_rater"
   const val IN_APP_UPDATER_CONFIG_KEY = "in_app_updater"
   const val UPDATE_INFO_CONFIG_KEY = "update_info"

    fun setDefaultRemoteConfigs(
        remoteConfig: FirebaseRemoteConfig,
        activity: BottomTabsActivity
    ) {
        Log.e("TAG RemoteConfigs", "setDefaultRemoteConfigs")

        if (BuildConfig.DEBUG) { // Remove this check when testing release version using In-App sharing
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
        }

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.e("TAG RemoteConfigs", "Config params updated: $updated")
                } else {
                    Log.e("TAG RemoteConfigs", "Config params Fetch failed")
                }
            }
    }

    fun inAppRaterConfigs(): InAppRaterParams {

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val json = remoteConfig.getString(IN_APP_RATER_CONFIG_KEY)
        val gson = Gson()
        val remoteConfigsInAppRaterParams: InAppRaterParams = gson.fromJson(json, InAppRaterParams::class.java)

        Log.e("TAG RemoteConfigs BF", "remoteConfigsInAppRaterParams: \n $remoteConfigsInAppRaterParams")
        Log.e("TAG RemoteConfigs BF", "appLaunchesUntilPrompt: \n" + remoteConfigsInAppRaterParams.appRater.appLaunchesUntilPrompt)

        return remoteConfigsInAppRaterParams
    }

    fun inAppUpdaterConfigs(): InAppUpdaterParams {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val json = remoteConfig.getString(IN_APP_UPDATER_CONFIG_KEY)
        val gson = Gson()
        val remoteConfigsInAppUpdaterParams: InAppUpdaterParams = gson.fromJson(json, InAppUpdaterParams::class.java)

        Log.e("TAG RemoteConfigs BF", "remoteConfigsInAppUpdaterParams: \n $remoteConfigsInAppUpdaterParams")
        Log.e("TAG RemoteConfigs BF", "daysForFlexibleUpdate: \n" + remoteConfigsInAppUpdaterParams.inAppUpdater.daysForFlexibleUpdate)

        return remoteConfigsInAppUpdaterParams
    }

    fun updateInfoConfigs(): UpdateInfo {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val json = remoteConfig.getString(UPDATE_INFO_CONFIG_KEY)
        val gson = Gson()
        val remoteConfigsUpdateInfoParams: UpdateInfo = gson.fromJson(json, UpdateInfo::class.java)

        Log.e("TAG RemoteConfigs BF", "remoteConfigsUpdateInfoParams: \n $remoteConfigsUpdateInfoParams")
        Log.e("TAG RemoteConfigs BF", "all updates: \n" + remoteConfigsUpdateInfoParams.updates)

        return remoteConfigsUpdateInfoParams
    }

}