package info.ascetx.stockstalker.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.io.InputStream;
import java.util.List;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.helper.News;
import info.ascetx.stockstalker.holder.UnifiedNativeAdViewHolder;

/**
 * Created by JAYESH on 13-12-2018.
 */

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // A menu item view type.
    private static final int MENU_ITEM_VIEW_TYPE = 0;

    // The unified native ad view type.
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

    public Context context;
    // The list of Native ads and menu items.
    private final List<Object> news;
    private SessionManager session;

    public NewsRecyclerViewAdapter(Context context, List<Object> news) {
        this.news = news;
        this.context = context;
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        private TextView txtHeadline, txtDatetime, txtSource, txtSummary;
        private ImageView imgStockImg;
        private CardView cardView;
        private NewsViewHolder(View view) {
            super(view);
            txtHeadline = (TextView) view.findViewById(R.id.txtHeadline);
            txtDatetime = (TextView) view.findViewById(R.id.txtDatetime);
            txtSource = (TextView) view.findViewById(R.id.txtSource);
            txtSummary = (TextView) view.findViewById(R.id.txtSummary);
            cardView = (CardView) view.findViewById(R.id.cardView);
            imgStockImg = (ImageView) view.findViewById(R.id.ivNews);
        }
    }

    public int getItemCount() {
        return news.size();
    }

    public int getItemViewType(int position) {

        Object recyclerViewItem = this.news.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return MENU_ITEM_VIEW_TYPE;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        session = new SessionManager(context);
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                View unifiedNativeLayoutView = LayoutInflater.from(context).inflate(R.layout.news_native_ad,
                        parent, false);
                return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
            case MENU_ITEM_VIEW_TYPE:
                // Fall through.
            default:
                View view = LayoutInflater.from(context).inflate(R.layout.news_item_row, parent, false);
                return new NewsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) this.news.get(position);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
                break;
            case MENU_ITEM_VIEW_TYPE:
                // fall through
            default:
                NewsViewHolder menuItemHolder = (NewsViewHolder) holder;
                News news = (News) this.news.get(position);

                if(session.getTheme() == 0)
                    menuItemHolder.txtHeadline.setTextColor(context.getResources().getColor(R.color.list_row_hover_end_color));
                else
                    menuItemHolder.txtHeadline.setTextColor(context.getResources().getColor(R.color.stock_same));

                menuItemHolder.txtHeadline.setText(news.getHeadline());
                menuItemHolder.txtDatetime.setText(news.getDatetime());
                menuItemHolder.txtSource.setText(news.getSource());
                menuItemHolder.txtSummary.setText(news.getSummary());

                Glide.with(context)
                        .asBitmap()
                        .fitCenter()
                        .override(250, 175)
                        .load(news.getImage())
                        .listener( new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                menuItemHolder.imgStockImg.setVisibility(View.GONE);
                                // important to return false so the error placeholder can be placed
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                // everything worked out, so probably nothing to do
                                return false;
                            }
                        })
                        .into(menuItemHolder.imgStockImg);
//                new DownloadImageTask(menuItemHolder.imgStockImg)
//                    .execute(news.getImage());
        }

    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        if(session.getTheme() == 0)
            ((TextView) adView.getHeadlineView()).setTextColor(context.getResources().getColor(R.color.list_row_hover_end_color));
        else
            ((TextView) adView.getHeadlineView()).setTextColor(context.getResources().getColor(R.color.stock_same));

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null) {
                int w = image.getWidth();//get width
                int h = image.getHeight();//get height
                int aspRat = w / h;//get aspect ratio
                int W = 250;//do whatever you want with width. Fixed, screen size, anything
                int H = 175;//set the height based on width and aspect ratio

                Bitmap scaledImage = Bitmap.createScaledBitmap(image, W, H, false);//scale the bitmap
                bmImage.setImageBitmap(scaledImage);
            } else {
                bmImage.setVisibility(View.GONE);
            }

        }
    }
}
