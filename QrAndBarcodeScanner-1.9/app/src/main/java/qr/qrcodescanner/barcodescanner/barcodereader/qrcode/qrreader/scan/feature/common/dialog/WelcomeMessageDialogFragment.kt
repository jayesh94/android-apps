package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView


class WelcomeMessageDialogFragment() : DialogFragment() {


    interface Listener {
        fun showTutorial()
        fun skipTutorial()
    }

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.dialog_fragment_welcome_message, container);
    }

    // Below onViewCreated is used when you have to show a Full Custom Dialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listener = requireActivity() as Listener
        val activity = requireActivity()

        val showMeAroundButton = view.findViewById<View>(R.id.show_me_around_button)
        val skipTutorialButton = view.findViewById<View>(R.id.skip_text_view)
        val welcomeImage = view.findViewById<View>(R.id.imageView)
        val welcomeTitle: TextView = view.findViewById<View>(R.id.welcome_title) as TextView
        val width = activity.resources.displayMetrics.widthPixels

        welcomeTitle.text = String.format(activity.resources.getString(R.string.welcome_to_app_title), activity.resources.getString(R.string.app_name))
        welcomeImage.layoutParams.width = (width * 0.8).toInt()

        showMeAroundButton.setOnClickListener {
            faLogEvents.logCustomButtonClickEvent("welcome_df_show_around")
            listener.showTutorial()
        }

        skipTutorialButton.setOnClickListener {
            faLogEvents.logCustomButtonClickEvent("welcome_df_skip")
            listener.skipTutorial()
        }

        isCancelable = false // The DialogFragment will not close when clicked outside
    }

   /* override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = requireActivity() as Listener

        val activity = requireActivity()

        val view: View = layoutInflater.inflate(R.layout.dialog_fragment_welcome_message, null)

        val showMeAroundButton = view.findViewById<View>(R.id.show_me_around_button)
        val skipTutorialButton = view.findViewById<View>(R.id.skip_text_view)
        val welcomeImage = view.findViewById<View>(R.id.imageView)
        val welcomeTitle: TextView = view.findViewById<View>(R.id.welcome_title) as TextView

        val height = activity.resources.displayMetrics.heightPixels
        val width = activity.resources.displayMetrics.widthPixels

        Log.e("TAG WelcomeDF", "Height $height")
        Log.e("TAG WelcomeDF", "Width $width")

        val builder = AlertDialog.Builder(requireActivity(), R.style.WelcomeDialogTheme)
        builder.setView(view)

        val dialog = builder.create()

        isCancelable = false


        welcomeTitle.text = String.format(activity.resources.getString(R.string.welcome_to_app_title), activity.resources.getString(R.string.app_name))

        welcomeImage.layoutParams.width = (width * 0.8).toInt()

        showMeAroundButton.setOnClickListener {
            faLogEvents.logCustomButtonClickEvent("welcome_df_show_around")
            listener.showTutorial()
        }

        skipTutorialButton.setOnClickListener {
            faLogEvents.logCustomButtonClickEvent("welcome_df_skip")
            listener.skipTutorial()
        }

        return dialog

    }*/

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("WelcomeMessageDialogFragment", "WelcomeMessageDialogFragment")
    }

}