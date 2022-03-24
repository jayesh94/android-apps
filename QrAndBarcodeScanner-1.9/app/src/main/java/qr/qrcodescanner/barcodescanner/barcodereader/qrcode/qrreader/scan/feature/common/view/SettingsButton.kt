package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import kotlinx.android.synthetic.main.layout_settings_button.view.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import androidx.core.graphics.drawable.DrawableCompat




class SettingsButton : FrameLayout {
    private val view: View

    private var mTrackingName: String? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        view = LayoutInflater
            .from(context)
            .inflate(R.layout.layout_settings_button, this, true)

        context.obtainStyledAttributes(attrs, R.styleable.SettingsButton).apply {
            showText(this)
            showHint(this)
            showSwitch(this)
            showAppIcon(this)
            setTrackingName(attrs)
            recycle()
        }
    }

    var hint: String
        get() = view.text_view_hint.text.toString()
        set(value) {
            view.text_view_hint.apply {
                text = value
                isVisible = text.isNullOrEmpty().not()
            }
        }

    var settingsText: String
        get() = view.settings_text.text.toString()
        set(value) {
            view.settings_text.apply {
                text = value
                isVisible = text.isNullOrEmpty().not()
            }
        }

    var settingsIcon: Drawable
        get() = view.settings_icon.drawable
        set(value) {
            view.settings_icon.apply {
                val wrappedDrawable = DrawableCompat.wrap(value)
                DrawableCompat.setTint(wrappedDrawable, resources.getColor(R.color.main_orange))
                setImageDrawable(value)
                isVisible = true
            }
        }

    var isChecked: Boolean
        get() = view.switch_button.isChecked
        set(value) { view.switch_button.isChecked = value }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        text_view_text.isEnabled = enabled
    }

    fun setCheckedChangedListener(listener: ((Boolean) -> Unit)?) {
        view.switch_button.setOnCheckedChangeListener { _, isChecked ->
            listener?.invoke(isChecked)
        }
    }

    private fun showText(attributes: TypedArray) {
        view.text_view_text.text = attributes.getString(R.styleable.SettingsButton_text).orEmpty()
    }

    private fun showHint(attributes: TypedArray) {
        hint = attributes.getString(R.styleable.SettingsButton_hint).orEmpty()
    }

    private fun showSwitch(attributes: TypedArray) {
        view.switch_button.isVisible = attributes.getBoolean(R.styleable.SettingsButton_isSwitchVisible, true)
        if (view.switch_button.isVisible) {
            view.setOnClickListener {
                view.switch_button.toggle()
            }
        }
    }

    private fun showAppIcon(attributes: TypedArray) {
        if (attributes.getResourceId(R.styleable.SettingsButton_ourAppIcon, -1) != -1){
            val iconResId = attributes.getResourceId(R.styleable.SettingsButton_ourAppIcon, -1)
            val icon = AppCompatResources.getDrawable(context, iconResId)
            view.app_icon.setImageDrawable(icon)
            view.app_icon.isVisible = true
        }
    }

    private fun setTrackingName(attrs: AttributeSet?) {
        if (attrs != null) {
            mTrackingName = try {
                view.resources.getResourceEntryName(view.id)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun performClick(): Boolean {
        val clickHasBeenPerformed = super.performClick()

//        Log.e("TAG", "SettingsButton-performClick: $mTrackingName")
        if (clickHasBeenPerformed && mTrackingName != null) {
            faLogEvents.logCustomButtonClickEvent("fragment_settings_$mTrackingName")
        }
        return clickHasBeenPerformed
    }
}