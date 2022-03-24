package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.history

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isInvisible
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.toColorId
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.toImageId
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.toStringId
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import kotlinx.android.synthetic.main.activity_barcode.*
import kotlinx.android.synthetic.main.item_barcode_history.view.*
import kotlinx.android.synthetic.main.item_barcode_history.view.delimiter
import kotlinx.android.synthetic.main.item_barcode_history.view.image_view_schema
import kotlinx.android.synthetic.main.item_barcode_history.view.layout_image
import kotlinx.android.synthetic.main.layout_icon_button_with_delimiter.view.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import java.text.SimpleDateFormat
import java.util.*


class BarcodeHistoryAdapter(private val listener: Listener) : PagedListAdapter<Barcode, BarcodeHistoryAdapter.ViewHolder>(
    DiffUtilCallback
) {

    interface Listener {
        fun onBarcodeClicked(barcode: Barcode)
        fun onBarcodeEditTitleClicked(barcode: Barcode)
        fun onBarcodeDeleteClicked(barcode: Barcode)
        fun onBarcodeFavoriteClicked(barcode: Barcode)
        fun onBarcodeShareClicked(barcode: Barcode)
    }

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_barcode_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.also { barcode ->
            holder.show(barcode, position == itemCount.dec())
        }
    }

    private fun showMenu(view: View, menuRes: Int, barcode: Barcode) {
        val popup = PopupMenu(view.context, view.image_view_more_actions)
        popup.menuInflater.inflate(menuRes, popup.menu)

        //Set on click listener for the menu
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_rename_history_title_item -> listener.onBarcodeEditTitleClicked(barcode)
                R.id.item_clear_history_item -> listener.onBarcodeDeleteClicked(barcode)
                R.id.item_share_history_item -> listener.onBarcodeShareClicked(barcode)
            }

            return@setOnMenuItemClickListener true
        }

        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }

        // Show the popup menu.
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popup)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception){
            Log.e("Main", "Error showing menu icons.", e)
        } finally {
            popup.show()
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun show(barcode: Barcode, isLastItem: Boolean) {
            showDate(barcode)
            showFormat(barcode)
            showText(barcode)
            showImage(barcode)
            showImageBackgroundColor(barcode)
            showIsFavorite(barcode)
            showOrHideDelimiter(isLastItem)
            setClickListener(barcode)
            clipBackground()
        }

        private fun clipBackground() {
            val drawable: Drawable = itemView.history_layout_image2.background
            if (drawable is ClipDrawable) {
                drawable.level = 5000
            }
        }

        private fun showDate(barcode: Barcode) {
            itemView.text_view_date.text = dateFormatter.format(barcode.date)
        }

        private fun showFormat(barcode: Barcode) {
            itemView.text_view_format.setText(barcode.format.toStringId())
        }

        private fun showText(barcode: Barcode) {
            itemView.text_view_text.text = barcode.name ?: barcode.formattedText
        }

        private fun showImage(barcode: Barcode) {
            val imageId = barcode.schema.toImageId() ?: barcode.format.toImageId()
            val image = AppCompatResources.getDrawable(itemView.context, imageId)
            itemView.image_view_schema.setImageDrawable(image)
        }

        private fun showImageBackgroundColor(barcode: Barcode) {
            val colorId = barcode.format.toColorId()
            val color = itemView.context.resources.getColor(colorId)
            (itemView.layout_image.background.mutate() as GradientDrawable).setColor(color)
        }

        private fun showIsFavorite(barcode: Barcode) {
            val iconId = if (barcode.isFavorite) {
                R.drawable.ic_favorite_checked
            } else {
                R.drawable.ic_baseline_star_border_24
            }
            itemView.image_view_favorite.setImageResource(iconId)
        }

        private fun showOrHideDelimiter(isLastItem: Boolean) {
            itemView.delimiter.isInvisible = isLastItem
        }

        private fun setClickListener(barcode: Barcode) {

            itemView.image_view_more_actions.setOnClickListener {
                showMenu(itemView, R.menu.menu_barcode_history_item, barcode)
            }

            itemView.setOnClickListener {
                listener.onBarcodeClicked(barcode)
            }

            itemView.image_view_favorite.setOnClickListener {
                listener.onBarcodeFavoriteClicked(barcode)
            }

        }
    }

    private object DiffUtilCallback : DiffUtil.ItemCallback<Barcode>() {

        override fun areItemsTheSame(oldItem: Barcode, newItem: Barcode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Barcode, newItem: Barcode): Boolean {
            return oldItem == newItem
        }
    }
}