package info.ascetx.stockstalker.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.fragment.HistoricalFragment.OnListFragmentInteractionListener;
import info.ascetx.stockstalker.helper.StockDaily;

/**
 * {@link RecyclerView.Adapter} that can display a {@link StockDaily} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HistoricalRecyclerViewAdapter extends RecyclerView.Adapter<HistoricalRecyclerViewAdapter.ViewHolder> {

    private List<StockDaily> stockDailyValues;
    private final OnListFragmentInteractionListener mListener;
    private String[] bgColors;
    private static String TAG = "HistoricalRecyclerViewAdapter";
    private SessionManager session;
    private Context context;

    public HistoricalRecyclerViewAdapter(List<StockDaily> items, OnListFragmentInteractionListener listener) {
        stockDailyValues = items;
        mListener = listener;
        context = (Context) mListener;
        Log.e(TAG, "HistoricalRecyclerViewAdapter Constructor");
    }

    /**
     * Called First
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_historical_list, parent, false);
        Log.e(TAG, "onCreateViewHolder");
        session = new SessionManager((Context) mListener);
        return new ViewHolder(view);
    }

    /**
     * Called Third
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
//        Log.e(TAG, "onBindViewHolder");
        Resources resources = context.getApplicationContext().getResources();
        int same = 0, date = 0, rise = 0, fall = 0;

        if(session.getTheme() == 0){
            same = resources.getColor(R.color.stock_same_dark);
            date = resources.getColor(R.color.stock_date_dark);
            rise = resources.getColor(R.color.stock_rise_dark);
            fall = resources.getColor(R.color.stock_fall_dark);
            bgColors = context.getApplicationContext().getResources().getStringArray(R.array.stock_list_bg_tint_dark);
        } else if (session.getTheme() == 1){
            same = resources.getColor(R.color.stock_same);
            date = resources.getColor(R.color.stock_date);
            rise = resources.getColor(R.color.stock_rise);
            fall = resources.getColor(R.color.stock_fall);
            bgColors = context.getApplicationContext().getResources().getStringArray(R.array.stock_list_bg_tint);
        }

        float fNo;
        try{
            fNo = Float.parseFloat(stockDailyValues.get(position).getChg());
        }catch(Exception e){
            fNo = (float) 0.0;
        }

        if (fNo == 0 && 1/fNo > 0){
            //Black color
            holder.open.setTextColor(same);
            holder.high.setTextColor(same);
            holder.low.setTextColor(same);
            holder.chg.setTextColor(same);
            holder.chg_p.setTextColor(same);
            holder.close.setTextColor(same);
            holder.chg.setBackgroundColor(Color.TRANSPARENT);
            holder.chg_p.setBackgroundColor(Color.TRANSPARENT);
        }else if(1/fNo < 0){
            //Red color
            holder.open.setTextColor(fall);
            holder.high.setTextColor(fall);
            holder.low.setTextColor(fall);
            holder.chg.setTextColor(fall);
            holder.chg_p.setTextColor(fall);
//            chg.setTextColor(Color.WHITE);
//            chg_p.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if(session.getTheme() == 0) {
                    holder.chg.setBackground(resources.getDrawable(R.drawable.stock_fall_left_bg_dark));
                    holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_fall_right_bg_dark));
                } else {
                    holder.chg.setBackground(resources.getDrawable(R.drawable.stock_fall_left_bg));
                    holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_fall_right_bg));
                }
            }else {
                holder.chg.setBackgroundColor(fall);
                holder.chg_p.setBackgroundColor(fall);
            }
            holder.close.setTextColor(fall);
        }
        else{
            //Green color
            holder.open.setTextColor(rise);
            holder.high.setTextColor(rise);
            holder.low.setTextColor(rise);
            holder.chg.setTextColor(rise);
            holder.chg_p.setTextColor(rise);
//            chg.setTextColor(Color.parseColor("#37474f"));
//            chg_p.setTextColor(Color.parseColor("#37474f"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if(session.getTheme() == 0) {
                    holder.chg.setBackground(resources.getDrawable(R.drawable.stock_rise_left_bg_dark));
                    holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_rise_right_bg_dark));
                } else {
                    holder.chg.setBackground(resources.getDrawable(R.drawable.stock_rise_left_bg));
                    holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_rise_right_bg));
                }
            }else {
                holder.chg.setBackgroundColor(Color.parseColor("#76ff03"));
                holder.chg_p.setBackgroundColor(Color.parseColor("#76ff03"));
            }
            holder.close.setTextColor(rise);
        }

        holder.date.setTextColor(date);

        holder.mItem = stockDailyValues.get(position);
        if (stockDailyValues.get(position).getOpen() != null) {
            holder.open.setText(String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(stockDailyValues.get(position).getOpen())));
            holder.high.setText(String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(stockDailyValues.get(position).getHigh())));
            holder.low.setText(String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(stockDailyValues.get(position).getLow())));
            holder.chg.setText(stockDailyValues.get(position).getChg());
            holder.chg_p.setText(stockDailyValues.get(position).getChg_p() + "%");
            holder.close.setText(stockDailyValues.get(position).getCls());
            holder.date.setText(stockDailyValues.get(position).getDate());
        } else {
            holder.open.setText(null);
            holder.high.setText(null);
            holder.low.setText(null);
            holder.chg.setText(null);
            holder.chg_p.setText(null);
            holder.close.setText(null);
            holder.date.setText(null);
        }
//        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
//        Date date1 = null;
//        try {
//            date1 = dt.parse(stockDailyValues.get(position).getDate());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        String date2;
//        if (session.getStockPeriod().equals("1M"))
//            date2 = new SimpleDateFormat("yyyy MMM dd, HH:mm:ss", Locale.ENGLISH).format(date1);
//        else
//            date2 = new SimpleDateFormat("yyyy MMM dd", Locale.ENGLISH).format(date1);
//        holder.date.setText(date2);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction();
                }
            }
        });

        String color = bgColors[position % bgColors.length];
        holder.relativeLayout.setBackgroundColor(Color.parseColor(color));

    }

    /**
     * Called Very first and runs as async
     */
    @Override
    public int getItemCount() {
//        Log.e(TAG, "getItemCount");
        if (stockDailyValues!=null)
            return stockDailyValues.size();
        else
            return 0;
    }

    /**
     * Called Second
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView open;
        public final TextView high;
        public final TextView low;
        public final TextView close;
        public final TextView chg;
        public final TextView chg_p;
        public final TextView total_chg_p;
        public final TextView date;
        public final RelativeLayout relativeLayout;
        public StockDaily mItem;

        public ViewHolder(View view) {
            super(view);
            open = (TextView) view.findViewById(R.id.open);
            high = (TextView) view.findViewById(R.id.high);
            low = (TextView) view.findViewById(R.id.low);
            close = (TextView) view.findViewById(R.id.close);
            chg = (TextView) view.findViewById(R.id.close_chg);
            chg_p = (TextView) view.findViewById(R.id.close_cp);
            total_chg_p = (TextView) view.findViewById(R.id.total_chg_p);
            date = (TextView) view.findViewById(R.id.date);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.list_view_stock_day);
            mView = view;
        }
    }
}
