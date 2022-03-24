package app.mugup.mugup.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import app.mugup.mugup.activity.FirstActivity;

public class InstallReferrerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("InstallReferrerReceiver", "In InstallReferrerReceiver");
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        String referrerId = null;

        try {
            if (extras != null) {
                referrerId = extras.getString("referrer");
            }
            // Register that the install event now has been sent
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(FirstActivity.PREF_INSTALL_REFERRER_CODE, referrerId).apply();
        } catch (Exception e) {
            // Precautionary
            Log.e("InstallReferrerReceiver", "Unexpected error caught in INSTALL_REFERRER intent receiver. Install event will not be sent. " + e.getMessage());
        }

        if (referrerId == null) {
            return;
        }

        Log.e("InstallReferrerReceiver", referrerId);
    }
}