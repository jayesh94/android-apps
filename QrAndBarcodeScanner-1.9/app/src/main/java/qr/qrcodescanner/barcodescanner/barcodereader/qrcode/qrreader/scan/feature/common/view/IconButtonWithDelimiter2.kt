package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import kotlinx.android.synthetic.main.layout_icon_button_with_delimiter.view.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents


class IconButtonWithDelimiter2 : FrameLayout {
    private val view: View = LayoutInflater
        .from(context)
        .inflate(R.layout.layout_icon_button_with_delimiter2, this, true)

    private var mTrackingName: String? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        context.obtainStyledAttributes(attrs, R.styleable.IconButtonWithDelimiter2).apply {

//            mTrackingName = this.getString(R.styleable.IconButtonWithDelimiter2_trackingName).orEmpty()
            showIcon(this)
            showIconBackgroundColor(this)
            showText(this)
            clipBackground()
            if (attrs != null) {
                mTrackingName = view.resources.getResourceEntryName(view.id)
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
        val iconResId = attributes.getResourceId(R.styleable.IconButtonWithDelimiter2_icon, -1)
        val icon = AppCompatResources.getDrawable(context, iconResId)
        view.image_view_schema.setImageDrawable(icon)
    }

    private fun showIconBackgroundColor(attributes: TypedArray) {
        val color = attributes.getColor(R.styleable.IconButtonWithDelimiter2_iconBackground, view.context.resources.getColor(
            R.color.green))
        (view.layout_image.background.mutate() as GradientDrawable).setColor(color)
    }

    private fun showText(attributes: TypedArray) {
        view.text_view.text = attributes.getString(R.styleable.IconButtonWithDelimiter2_text).orEmpty()
    }


    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        view.image_view_schema.isEnabled = enabled
        view.text_view.isEnabled = enabled
    }

    override fun performClick(): Boolean {
        val clickHasBeenPerformed = super.performClick()

//        Log.e("TAG", "IconButton-performClick: $mTrackingName")
        if (clickHasBeenPerformed && mTrackingName != null) {
            faLogEvents.logCustomButtonClickEvent("activity_create_qr_code_all_$mTrackingName")
        }
        return clickHasBeenPerformed
    }
}