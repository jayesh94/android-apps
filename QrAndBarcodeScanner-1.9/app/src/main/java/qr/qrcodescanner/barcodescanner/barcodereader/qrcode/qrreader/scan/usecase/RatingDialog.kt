package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.RatingDialog.Builder.RatingThresholdClearedListener
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.RatingDialog.Builder.RatingThresholdFailedListener

/**
 * Created by Jayesh on 17-10-2020.
 */
class RatingDialog(context: Context, private val builder: Builder) : AppCompatDialog(
    context
), OnRatingBarChangeListener, View.OnClickListener {
    private val MyPrefs = "RatingDialog"
    private var sharedpreferences: SharedPreferences? = null
    private var tvMsg: TextView? = null
    var titleTextView: TextView? = null
        private set
    var negativeButtonTextView: TextView? = null
        private set
    var positiveButtonTextView: TextView? = null
        private set
    var formTitleTextView: TextView? = null
        private set
    var formSumbitTextView: TextView? = null
        private set
    var formCancelTextView: TextView? = null
        private set
    var ratingBarView: RatingBar? = null
        private set
    private var fingerClick: FrameLayout? = null
    var iconImageView: ImageView? = null
        private set
    private var etFeedback: EditText? = null
    private var ratingButtons: LinearLayout? = null
    private var feedbackButtons: LinearLayout? = null
    private var noPlayStoreError: String? = null
    private val threshold: Float
    private val session: Int
    private var thresholdPassed = true
//    private val faLogEvents: FALogEvents


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_rating)
        titleTextView = findViewById<View>(R.id.dialog_rating_title) as TextView?
        tvMsg = findViewById<View>(R.id.dialog_rating_msg) as TextView?
        negativeButtonTextView = findViewById<View>(R.id.dialog_rating_button_negative) as TextView?
        positiveButtonTextView = findViewById<View>(R.id.dialog_rating_button_positive) as TextView?
        formTitleTextView = findViewById<View>(R.id.dialog_rating_feedback_title) as TextView?
        formSumbitTextView =
            findViewById<View>(R.id.dialog_rating_button_feedback_submit) as TextView?
        formCancelTextView =
            findViewById<View>(R.id.dialog_rating_button_feedback_cancel) as TextView?
        ratingBarView = findViewById<View>(R.id.dialog_rating_rating_bar_rb) as RatingBar?
        fingerClick = findViewById<View>(R.id.finger_click) as FrameLayout?
        iconImageView = findViewById<View>(R.id.dialog_rating_icon) as ImageView?
        etFeedback = findViewById<View>(R.id.dialog_rating_feedback) as EditText?
        ratingButtons = findViewById<View>(R.id.dialog_rating_buttons) as LinearLayout?
        feedbackButtons = findViewById<View>(R.id.dialog_rating_feedback_buttons) as LinearLayout?
        init()
    }

    private fun init() {
        titleTextView!!.text = builder.title
        tvMsg!!.text = builder.msg
        positiveButtonTextView!!.text = builder.positiveText
        negativeButtonTextView!!.text = builder.negativeText
        formTitleTextView!!.text = builder.formTitle
        formSumbitTextView!!.text = builder.submitText
        formCancelTextView!!.text = builder.cancelText
        etFeedback!!.hint = builder.feedbackFormHint
        noPlayStoreError = builder.noPlayStoreError
        val slide = AnimationUtils.loadAnimation(
            context, R.anim.slide
        )
        fingerClick!!.startAnimation(slide)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        val color = typedValue.data
        titleTextView!!.setTextColor(
            if (builder.titleTextColor != 0) ContextCompat.getColor(
                context,
                builder.titleTextColor
            ) else ContextCompat.getColor(
                context, R.color.black
            )
        )
        tvMsg!!.setTextColor(
            if (builder.titleTextColor != 0) ContextCompat.getColor(
                context,
                builder.titleTextColor
            ) else ContextCompat.getColor(
                context, R.color.black
            )
        )
        positiveButtonTextView!!.setTextColor(
            if (builder.positiveTextColor != 0) ContextCompat.getColor(
                context, builder.positiveTextColor
            ) else color
        )
        negativeButtonTextView!!.setTextColor(
            if (builder.negativeTextColor != 0) ContextCompat.getColor(
                context, builder.negativeTextColor
            ) else ContextCompat.getColor(context, R.color.grey_500)
        )
        formTitleTextView!!.setTextColor(
            if (builder.titleTextColor != 0) ContextCompat.getColor(
                context, builder.titleTextColor
            ) else ContextCompat.getColor(context, R.color.black)
        )
        formSumbitTextView!!.setTextColor(
            if (builder.positiveTextColor != 0) ContextCompat.getColor(
                context, builder.positiveTextColor
            ) else color
        )
        formCancelTextView!!.setTextColor(
            if (builder.negativeTextColor != 0) ContextCompat.getColor(
                context, builder.negativeTextColor
            ) else ContextCompat.getColor(context, R.color.grey_500)
        )
        if (builder.feedBackTextColor != 0) {
            etFeedback!!.setTextColor(ContextCompat.getColor(context, builder.feedBackTextColor))
        }
        if (builder.positiveBackgroundColor != 0) {
            positiveButtonTextView!!.setBackgroundResource(builder.positiveBackgroundColor)
            formSumbitTextView!!.setBackgroundResource(builder.positiveBackgroundColor)
        }
        if (builder.negativeBackgroundColor != 0) {
            negativeButtonTextView!!.setBackgroundResource(builder.negativeBackgroundColor)
            formCancelTextView!!.setBackgroundResource(builder.negativeBackgroundColor)
        }
        if (builder.ratingBarColor != 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                val stars = ratingBarView!!.progressDrawable as LayerDrawable
                stars.getDrawable(2).setColorFilter(
                    ContextCompat.getColor(context, builder.ratingBarColor),
                    PorterDuff.Mode.SRC_ATOP
                )
                stars.getDrawable(1).setColorFilter(
                    ContextCompat.getColor(context, builder.ratingBarColor),
                    PorterDuff.Mode.SRC_ATOP
                )
                val ratingBarBackgroundColor =
                    if (builder.ratingBarBackgroundColor != 0) builder.ratingBarBackgroundColor else R.color.grey_200
                stars.getDrawable(0).setColorFilter(
                    ContextCompat.getColor(context, ratingBarBackgroundColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            } else {
                val stars = ratingBarView!!.progressDrawable
                DrawableCompat.setTint(
                    stars,
                    ContextCompat.getColor(context, builder.ratingBarColor)
                )
            }
        }
        val d = context.packageManager.getApplicationIcon(context.applicationInfo)
        iconImageView!!.setImageDrawable(if (builder.drawable != null) builder.drawable else d)
        ratingBarView!!.onRatingBarChangeListener = this
        positiveButtonTextView!!.setOnClickListener(this)
        negativeButtonTextView!!.setOnClickListener(this)
        formSumbitTextView!!.setOnClickListener(this)
        formCancelTextView!!.setOnClickListener(this)
        if (session == 1) {
            negativeButtonTextView!!.visibility = View.GONE
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.dialog_rating_button_negative) {
            dismiss()
//            showNever()
        } else if (view.id == R.id.dialog_rating_button_positive) {
            builder.ratingDialogMayBeLaterListener!!.onRatingDialogMayBeLaterClicked()
            dismiss()
        } else if (view.id == R.id.dialog_rating_button_feedback_submit) {
            val feedback = etFeedback!!.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(feedback)) {
                val shake = AnimationUtils.loadAnimation(
                    context, R.anim.shake
                )
                etFeedback!!.startAnimation(shake)
                return
            }
            if (builder.ratingDialogFormListener != null) {
                builder.ratingDialogFormListener!!.onFormSubmitted(feedback)
            }
            dismiss()
//            showNever()
        } else if (view.id == R.id.dialog_rating_button_feedback_cancel) {
            builder.ratingDialogCancelListener!!.onRatingDialogCancelled()
            dismiss()
        }
    }

    override fun onRatingChanged(ratingBar: RatingBar, v: Float, b: Boolean) {
        if (ratingBar.rating >= threshold) {
            thresholdPassed = true
            if (builder.ratingThresholdClearedListener == null) {
                setRatingThresholdClearedListener()
            }
            builder.ratingThresholdClearedLogListener!!.onRatingThresholdClearedLog()
            builder.ratingThresholdClearedListener!!.onThresholdCleared(
                this,
                ratingBar.rating,
                thresholdPassed
            )
        } else {
            thresholdPassed = false
            if (builder.ratingThresholdFailedListener == null) {
                setRatingThresholdFailedListener()
            }
            builder.ratingThresholdFailedListener!!.onThresholdFailed(
                this,
                ratingBar.rating,
                thresholdPassed
            )
        }
        if (builder.ratingDialogListener != null) {
            builder.ratingDialogListener!!.onRatingSelected(ratingBar.rating, thresholdPassed)
        }
//        showNever()
    }

    private fun setRatingThresholdClearedListener() {
        builder.ratingThresholdClearedListener = object : RatingThresholdClearedListener {
            override fun onThresholdCleared(
                ratingDialog: RatingDialog?,
                rating: Float,
                thresholdCleared: Boolean
            ) {
                openPlaystore(context)
                dismiss()
            }
        }
    }

    private fun setRatingThresholdFailedListener() {
        builder.ratingThresholdFailedListener = object : RatingThresholdFailedListener {
            override fun onThresholdFailed(
                ratingDialog: RatingDialog?,
                rating: Float,
                thresholdCleared: Boolean
            ) {
                openForm()
            }
        }
    }

    private fun openForm() {
        formTitleTextView!!.visibility = View.VISIBLE
        etFeedback!!.visibility = View.VISIBLE
        feedbackButtons!!.visibility = View.VISIBLE
        ratingButtons!!.visibility = View.GONE
        iconImageView!!.visibility = View.GONE
        titleTextView!!.visibility = View.GONE
        tvMsg!!.visibility = View.GONE
        ratingBarView!!.visibility = View.GONE
        fingerClick!!.visibility = View.GONE
    }

    private fun openPlaystore(context: Context) {
        val marketUri = Uri.parse(builder.playstoreUrl)
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, noPlayStoreError, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun show() {
//        if (checkIfSessionMatches(session)) {
            builder.ratingDialogShownListener!!.onRatingDialogShown()
            super.show()
//        }
    }

    /*private fun checkIfSessionMatches(session: Int): Boolean {
        if (session == 1) {
            return true
        }
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE)
        if (sharedpreferences.getBoolean(SHOW_NEVER, false)) {
            return false
        }
        var count = sharedpreferences.getInt(SESSION_COUNT, 1)
        return if (session == count) {
            val editor = sharedpreferences.edit()
            editor.putInt(SESSION_COUNT, 1)
            editor.apply()
            true
        } else if (session > count) {
            count++
            val editor = sharedpreferences.edit()
            editor.putInt(SESSION_COUNT, count)
            editor.apply()
            false
        } else {
            val editor = sharedpreferences.edit()
            editor.putInt(SESSION_COUNT, 2)
            editor.apply()
            false
        }
    }*/

    /*private fun showNever() {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        editor.putBoolean(SHOW_NEVER, true)
        editor.apply()
    }*/

    class Builder(private val context: Context) {
        var msg: String? = null
        var title: String? = null
        var positiveText: String? = null
        var negativeText: String? = null
        var playstoreUrl: String
        var formTitle: String? = null
        var submitText: String? = null
        var cancelText: String? = null
        var feedbackFormHint: String? = null
        var noPlayStoreError: String? = null
        var positiveTextColor = 0
        var negativeTextColor = 0
        var titleTextColor = 0
        var ratingBarColor = 0
        var ratingBarBackgroundColor = 0
        var feedBackTextColor = 0
        var positiveBackgroundColor = 0
        var negativeBackgroundColor = 0
        var ratingThresholdClearedListener: RatingThresholdClearedListener? = null
        var ratingThresholdFailedListener: RatingThresholdFailedListener? = null
        var ratingDialogFormListener: RatingDialogFormListener? = null
        var ratingDialogListener: RatingDialogListener? = null
        var ratingDialogCancelListener: RatingDialogCancelListener? = null
        var ratingDialogMayBeLaterListener: RatingDialogMayBeLaterListener? = null
        var ratingDialogShownListener: RatingDialogShownListener? = null
        var ratingThresholdClearedLogListener: RatingThresholdClearedLogListener? = null
        var drawable: Drawable? = null
        var session = 1
        var threshold = 1f

        interface RatingThresholdClearedListener {
            fun onThresholdCleared(
                ratingDialog: RatingDialog?,
                rating: Float,
                thresholdCleared: Boolean
            )
        }

        interface RatingThresholdFailedListener {
            fun onThresholdFailed(
                ratingDialog: RatingDialog?,
                rating: Float,
                thresholdCleared: Boolean
            )
        }

        interface RatingDialogFormListener {
            fun onFormSubmitted(feedback: String?)
        }

        interface RatingDialogListener {
            fun onRatingSelected(rating: Float, thresholdCleared: Boolean)
        }

        interface RatingDialogCancelListener {
            fun onRatingDialogCancelled()
        }

        interface RatingDialogMayBeLaterListener {
            fun onRatingDialogMayBeLaterClicked()
        }

        interface RatingDialogShownListener {
            fun onRatingDialogShown()
        }

        interface RatingThresholdClearedLogListener {
            fun onRatingThresholdClearedLog()
        }

        private fun initText() {
//            title = context.getString(R.string.review_app_title)
//            msg = context.getString(R.string.review_app_msg)
//            positiveText = context.getString(R.string.rating_dialog_maybe_later)
//            negativeText = context.getString(R.string.rating_dialog_never)
//            formTitle = context.getString(R.string.rating_dialog_feedback_title)
//            submitText = context.getString(R.string.rating_dialog_submit)
//            cancelText = context.getString(R.string.rating_dialog_cancel)
//            feedbackFormHint = context.getString(R.string.rating_dialog_suggestions)
        }

        fun session(session: Int): Builder {
            this.session = session
            return this
        }

        fun threshold(threshold: Float): Builder {
            this.threshold = threshold
            return this
        }

        fun title(title: String?): Builder {
            this.title = title
            return this
        }

        fun msg(msg: String?): Builder {
            this.msg = msg
            return this
        }

        /*public Builder icon(int icon) {
            this.icon = icon;
            return this;
        }*/
        fun icon(drawable: Drawable?): Builder {
            this.drawable = drawable
            return this
        }

        fun positiveButtonText(positiveText: String?): Builder {
            this.positiveText = positiveText
            return this
        }

        fun negativeButtonText(negativeText: String?): Builder {
            this.negativeText = negativeText
            return this
        }

        fun titleTextColor(titleTextColor: Int): Builder {
            this.titleTextColor = titleTextColor
            return this
        }

        fun positiveButtonTextColor(positiveTextColor: Int): Builder {
            this.positiveTextColor = positiveTextColor
            return this
        }

        fun negativeButtonTextColor(negativeTextColor: Int): Builder {
            this.negativeTextColor = negativeTextColor
            return this
        }

        fun positiveButtonBackgroundColor(positiveBackgroundColor: Int): Builder {
            this.positiveBackgroundColor = positiveBackgroundColor
            return this
        }

        fun negativeButtonBackgroundColor(negativeBackgroundColor: Int): Builder {
            this.negativeBackgroundColor = negativeBackgroundColor
            return this
        }

        fun onThresholdCleared(ratingThresholdClearedListener: RatingThresholdClearedListener?): Builder {
            this.ratingThresholdClearedListener = ratingThresholdClearedListener
            return this
        }

        fun onThresholdFailed(ratingThresholdFailedListener: RatingThresholdFailedListener?): Builder {
            this.ratingThresholdFailedListener = ratingThresholdFailedListener
            return this
        }

        fun onRatingChanged(ratingDialogListener: RatingDialogListener?): Builder {
            this.ratingDialogListener = ratingDialogListener
            return this
        }

        fun onRatingBarFormSumbit(ratingDialogFormListener: RatingDialogFormListener?): Builder {
            this.ratingDialogFormListener = ratingDialogFormListener
            return this
        }

        fun onRatingDialogMayBeLaterClicked(ratingDialogMayBeLaterListener: RatingDialogMayBeLaterListener?): Builder {
            this.ratingDialogMayBeLaterListener = ratingDialogMayBeLaterListener
            return this
        }

        fun onRatingDialogCancelled(ratingDialogCancelListener: RatingDialogCancelListener?): Builder {
            this.ratingDialogCancelListener = ratingDialogCancelListener
            return this
        }

        fun onRatingDialogShown(ratingDialogShownListener: RatingDialogShownListener?): Builder {
            this.ratingDialogShownListener = ratingDialogShownListener
            return this
        }

        fun onRatingThresholdClearedLog(ratingThresholdClearedLogListener: RatingThresholdClearedLogListener?): Builder {
            this.ratingThresholdClearedLogListener = ratingThresholdClearedLogListener
            return this
        }

        fun formTitle(formTitle: String?): Builder {
            this.formTitle = formTitle
            return this
        }

        fun formHint(formHint: String?): Builder {
            this.feedbackFormHint = formHint
            return this
        }

        fun noPlayStoreError(noPlayStoreError: String?): Builder {
            this.noPlayStoreError = noPlayStoreError
            return this
        }

        fun formSubmitText(submitText: String?): Builder {
            this.submitText = submitText
            return this
        }

        fun formCancelText(cancelText: String?): Builder {
            this.cancelText = cancelText
            return this
        }

        fun ratingBarColor(ratingBarColor: Int): Builder {
            this.ratingBarColor = ratingBarColor
            return this
        }

        fun ratingBarBackgroundColor(ratingBarBackgroundColor: Int): Builder {
            this.ratingBarBackgroundColor = ratingBarBackgroundColor
            return this
        }

        fun feedbackTextColor(feedBackTextColor: Int): Builder {
            this.feedBackTextColor = feedBackTextColor
            return this
        }

        fun playstoreUrl(playstoreUrl: String): Builder {
            this.playstoreUrl = playstoreUrl
            return this
        }

        fun build(): RatingDialog {
            return RatingDialog(context, this)
        }

        init {
            // Set default PlayStore URL
            playstoreUrl = "market://details?id=" + context.packageName
            initText()
        }
    }

    companion object {
        private const val SESSION_COUNT = "session_count"
        private const val SHOW_NEVER = "show_never"
    }

    init {
        session = builder.session
        threshold = builder.threshold
    }
}