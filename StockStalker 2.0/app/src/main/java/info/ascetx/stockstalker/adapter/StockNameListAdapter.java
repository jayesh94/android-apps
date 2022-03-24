package info.ascetx.stockstalker.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.helper.StockName;

import java.util.List;

/**
 * Created by JAYESH on 02-03-2017.
 */
public class StockNameListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<StockName> stockNameDate;
    private SessionManager session;
    private int name, same, date, rise, fall;

    public StockNameListAdapter(Activity activity, List<StockName> stockNameDate) {
        this.activity = activity;
        this.stockNameDate = stockNameDate;
        // session manager
        session = new SessionManager(activity);

        if(session.getTheme() == 0){
            name = activity.getResources().getColor(R.color.stock_date);
            same = activity.getResources().getColor(R.color.stock_same_dark);
            date = activity.getResources().getColor(R.color.stock_date_dark);
            rise = activity.getResources().getColor(R.color.stock_rise_dark);
            fall = activity.getResources().getColor(R.color.stock_fall_dark);
        } else if (session.getTheme() == 1){
            name = activity.getResources().getColor(R.color.stock_name);
            same = activity.getResources().getColor(R.color.stock_same);
            date = activity.getResources().getColor(R.color.stock_date);
            rise = activity.getResources().getColor(R.color.stock_rise);
            fall = activity.getResources().getColor(R.color.stock_fall);
        }
    }

    @Override
    public int getCount() {
        return stockNameDate.size();
    }

    @Override
    public Object getItem(int location) {
        return stockNameDate.get(getCount() - location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (convertView == null)
//            convertView = inflater.inflate(R.layout.stock_name_list_light, null);
        if(session.getTheme() == 0)
            convertView = inflater.inflate(R.layout.stock_name_list_dark, null);
        else if(session.getTheme() == 1)
            convertView = inflater.inflate(R.layout.stock_name_list_light, null);

        TextView name_tv = (TextView) convertView.findViewById(R.id.name);
        TextView stock_name = (TextView) convertView.findViewById(R.id.stock_name);
        TextView date_tv = (TextView) convertView.findViewById(R.id.date);
        TextView ltp = (TextView) convertView.findViewById(R.id.ltp);
        TextView chg = (TextView) convertView.findViewById(R.id.chg);
        TextView chg_p = (TextView) convertView.findViewById(R.id.chg_p);
        TextView pcls = (TextView) convertView.findViewById(R.id.pcls);
//        LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.linear_stock_list);

        stock_name.setTextColor(name);

        float fNo;
        try{
            fNo = Float.parseFloat(stockNameDate.get(position).chg_p);
        }catch(Exception e){
            fNo = (float) 0.0;
        }

        if (fNo == 0 && 1/fNo > 0){
            //Black color
            ltp.setTextColor(same);
            chg.setTextColor(same);
            chg_p.setTextColor(same);
            pcls.setTextColor(same);
            chg.setBackgroundColor(Color.TRANSPARENT);
            chg_p.setBackgroundColor(Color.TRANSPARENT);
        }else if(1/fNo < 0){
            //Red color
            ltp.setTextColor(fall);
            chg.setTextColor(fall);
            chg_p.setTextColor(fall);
//            chg.setTextColor(Color.WHITE);
//            chg_p.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if(session.getTheme() == 0) {
                    chg.setBackground(activity.getResources().getDrawable(R.drawable.stock_fall_left_bg_dark));
                    chg_p.setBackground(activity.getResources().getDrawable(R.drawable.stock_fall_right_bg_dark));
                } else {
                    chg.setBackground(activity.getResources().getDrawable(R.drawable.stock_fall_left_bg));
                    chg_p.setBackground(activity.getResources().getDrawable(R.drawable.stock_fall_right_bg));
                }
            }else {
                chg.setBackgroundColor(fall);
                chg_p.setBackgroundColor(fall);
            }
            pcls.setTextColor(fall);
        }
        else{
            //Green color
            ltp.setTextColor(rise);
            chg.setTextColor(rise);
            chg_p.setTextColor(rise);
//            chg.setTextColor(Color.parseColor("#37474f"));
//            chg_p.setTextColor(Color.parseColor("#37474f"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if(session.getTheme() == 0) {
                    chg.setBackground(activity.getResources().getDrawable(R.drawable.stock_rise_left_bg_dark));
                    chg_p.setBackground(activity.getResources().getDrawable(R.drawable.stock_rise_right_bg_dark));
                } else {
                    chg.setBackground(activity.getResources().getDrawable(R.drawable.stock_rise_left_bg));
                    chg_p.setBackground(activity.getResources().getDrawable(R.drawable.stock_rise_right_bg));
                }
            }else {
                chg.setBackgroundColor(Color.parseColor("#76ff03"));
                chg_p.setBackgroundColor(Color.parseColor("#76ff03"));
            }
            pcls.setTextColor(rise);
        }

        date_tv.setTextColor(date);

        if (stockNameDate.get(position).ltp != null) {
            // ltp
            ltp.setText(stockNameDate.get(position).ltp);
            // chg
            chg.setText(stockNameDate.get(position).chg);
            // chg_p
            chg_p.setText(stockNameDate.get(position).chg_p + " %");
            // cls
            pcls.setText(stockNameDate.get(position).pcls);
            // date_tv
            date_tv.setText(stockNameDate.get(position).date);
            // name
            name_tv.setText(stockNameDate.get(position).name);
            // Id
            stock_name.setText(stockNameDate.get(position).stock);
        } else {
            // ltp
            ltp.setText(null);
            // chg
            chg.setText(null);
            // chg_p
            chg_p.setText(null);
            // cls
            pcls.setText(null);
            // date_tv
            date_tv.setText(null);
            // name
            name_tv.setText(null);
            // Id
            stock_name.setText(null);
        }

//        String color = bgColors[position % bgColors.length];
//        linearLayout.setBackgroundColor(Color.parseColor(color));
//        stock_name.setBackgroundColor(Color.parseColor(color));
//        date.setBackgroundColor(Color.parseColor(color));

        return convertView;
    }
}
