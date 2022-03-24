package info.ascetx.stockstalker.app;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;
import java.util.Locale;

import info.ascetx.stockstalker.R;

/**
 * Created by JAYESH on 27-07-2018.
 */

public class MyMarkerView extends MarkerView {
    private TextView tvContent;
    private DecimalFormat decimalFormat;
    private SessionManager session;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        session = new SessionManager(context);
        tvContent = (TextView) findViewById(R.id.tvContent);
        decimalFormat = new DecimalFormat("###,###,###,###.##");

        if (session.getTheme() == 0){
//            Log.e("Marker ","dark");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                tvContent.setBackground(getResources().getDrawable(R.drawable.ic_custom_marker_dark));
            }
        }
        else {
//            Log.e("Marker ","light");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                tvContent.setBackground(getResources().getDrawable(R.drawable.ic_custom_marker_light));
            }
        }
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String o, h, l, c;
        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;
            o = decimalFormat.format(ce.getOpen());
            h = decimalFormat.format(ce.getHigh());
            l = decimalFormat.format(ce.getLow());
            c = decimalFormat.format(ce.getClose());

            tvContent.setText(Html.fromHtml("<b>O:</b>"+o+" <br><b>H:</b>"+h+" <br><b>L:</b>"+l+" <br><b>C:</b>"+c));
        } else {
            tvContent.setText(decimalFormat.format(e.getY()));
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

}
