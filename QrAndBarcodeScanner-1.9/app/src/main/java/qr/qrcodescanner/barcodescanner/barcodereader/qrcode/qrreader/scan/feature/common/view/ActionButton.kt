package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents

class ActionButton : MaterialButton {

    private var mTrackingName: String? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.obtainStyledAttributes(attrs, R.styleable.ActionButton).apply {
            mTrackingName = this.getString(R.styleable.ActionButton_trackingName).orEmpty()
            recycle()
        }
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun performClick(): Boolean {
        //Make sure the view has an onClickListener that listened the click event,
        //so that we don't report click on passive elements
//        Log.e("TAG", "performClick: $mTrackingName")
        val clickHasBeenPerformed = super.performClick()
        if (clickHasBeenPerformed && mTrackingName != null) {
            faLogEvents.logCustomButtonClickEvent(mTrackingName)
        }
        return clickHasBeenPerformed
    }
}