package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.rater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode

/**
 * Email client intent to send support mail
 * Appends the necessary device information to email body
 * useful when providing support
 */
class SendFeedback (val context: Context, private val feedback: String? = context.resources.getString(R.string.email_feedback_hint_message)) {

    init {
        sendFeedback()
    }
/*
String Learning - Here only the App Name was converted in spanish but the rest of the strings were Only in English.
Expectation - When I change locale to Spanish in the App, the message should have converted only the App Name to Spanish and rest strings
would have remained in English.
Result - All strings were in English. The Spanish text of App Name was not respected.
Test 2 - I added all the strings in the file in locale pt-br.
Test 2 Result - I was able to see the mail in pt-br
Solution/Learning - All the strings on this file MUST be converted to the particular Locale for it to be converted.

Possible future issue - After you publish the App as an App Bundle, Google will only install the app in user's default locale.
 Then user will try to change the locale in other language using in-app feature,
 and if the User's device does not have a support for a particular locale which your app provides then the app will crash. Otherwise,
 if the user's device does support that locale (User's device has that language installed), Google will install it then.

 */
    private fun sendFeedback() {
        var body: String? = null
        val deviceDetailsMessage = getFeedbackString(R.string.email_feedback_device_details_message)
        val emailId = getFeedbackString(R.string.email_feedback_email_id)
        val subjectStarting = getFeedbackString(R.string.email_feedback_starting_subject)
        val appName: String = getFeedbackString(R.string.app_name)

        try {
            body = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            body = """$feedback
                
-----------------------------
$deviceDetailsMessage
Device OS: Android 
Device OS version: ${Build.VERSION.RELEASE}
App Name: $appName
App Version: $body
Device Brand: ${Build.BRAND}
Device Model: ${Build.MODEL}
Device Manufacturer: ${Build.MANUFACTURER}"""
        } catch (e: PackageManager.NameNotFoundException) {
        }
        val emailSelectorIntent = Intent(Intent.ACTION_SENDTO)
        emailSelectorIntent.data = Uri.parse("mailto:")
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "$subjectStarting - $appName")
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        emailIntent.selector = emailSelectorIntent
        if (emailIntent.resolveActivity(context.packageManager) != null)
            context.startActivity(emailIntent)
    }

    private fun getFeedbackString(stringId: Int): String {
        return context.resources.getString(stringId)
    }
}