package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import kotlinx.android.synthetic.main.layout_icon_button_with_delimiter.view.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents


class IconButtonWithDelimiter : FrameLayout {
    private val view: View = LayoutInflater
        .from(context)
        .inflate(R.layout.layout_icon_button_with_delimiter, this, true)

    private var mTrackingName: String? = null
    private var isActivityCreateAllBarcode: Boolean = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        context.obtainStyledAttributes(attrs, R.styleable.IconButtonWithDelimiter).apply {

            mTrackingName = this.getString(R.styleable.IconButtonWithDelimiter_trackingName).orEmpty()

            showIcon(this)
            showIconBackgroundColor(this)
            showText(this)
            showArrow(this)
            showDelimiter(this)
            clipBackground()
            if (attrs != null) {
                if (mTrackingName.isNullOrEmpty()) {
                    mTrackingName = view.resources.getResourceEntryName(view.id)
                    isActivityCreateAllBarcode = true
                }
            }
            recycle()
        }
    }

    private fun clipBackground() {
        val drawable: Drawable = layout_image2.background
        if (drawable is ClipDrawable) {
            drawable.level = 5000
        }
    }

    private fun showIcon(attributes: TypedArray) {
        val iconResId = attributes.getResourceId(R.styleable.IconButtonWithDelimiter_icon, -1)
        val icon = AppCompatResources.getDrawable(context, iconResId)
        view.image_view_schema.setImageDrawable(icon)
    }

    private fun showIconBackgroundColor(attributes: TypedArray) {
        val color = attributes.getColor(R.styleable.IconButtonWithDelimiter_iconBackground, view.context.resources.getColor(R.color.green))
        (view.layout_image.background.mutate() as GradientDrawable).setColor(color)
    }

    private fun showText(attributes: TypedArray) {
        view.text_view.text = attributes.getString(R.styleable.IconButtonWithDelimiter_text).orEmpty()
    }

    private fun showArrow(attributes: TypedArray) {
        view.image_view_arrow.isVisible = attributes.getBoolean(R.styleable.IconButtonWithDelimiter_isArrowVisible, false)
    }

    private fun showDelimiter(attributes: TypedArray) {
        view.delimiter.isInvisible = attributes.getBoolean(R.styleable.IconButtonWithDelimiter_isDelimiterVisible, true).not()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        view.image_view_schema.isEnabled = enabled
        view.text_view.isEnabled = enabled
    }

    override fun performClick(): Boolean {
        val clickHasBeenPerformed = super.performClick()

        Log.e("TAG", "IconButtonWithDelimiter-performClick: $mTrackingName")
        if (clickHasBeenPerformed && mTrackingName != null) {
            if (isActivityCreateAllBarcode)
                faLogEvents.logCustomButtonClickEvent("activity_create_barcode_all_$mTrackingName")
            else
                faLogEvents.logCustomButtonClickEvent(mTrackingName)
        }
        return clickHasBeenPerformed
    }
}