package info.ascetx.stockstalker.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.fragment.GainFragment;
import info.ascetx.stockstalker.fragment.LossFragment;
import info.ascetx.stockstalker.fragment.MostActiveFragment;
import info.ascetx.stockstalker.helper.StockTopGL;

/**
 * {@link RecyclerView.Adapter} that can display a {@link } and makes a call to the
 * specified {@link }.
 * TODO: Replace the implementation with code for your data type.
 */
public class TGLMRecyclerViewAdapter extends RecyclerView.Adapter<TGLMRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "TGLMRecyclerViewAdapter";
    private GainFragment.OnListFragmentInteractionListener mGListener;
    private LossFragment.OnListFragmentInteractionListener mLListener;
    private MostActiveFragment.OnListFragmentInteractionListener mMListener;
    private List<StockTopGL> stockTopGLValues;
    private SessionManager session;
    private String[] bgColors;
    private Context context;

    public TGLMRecyclerViewAdapter(List<StockTopGL> items, GainFragment.OnListFragmentInteractionListener listener) {
        stockTopGLValues = items;
        mGListener = listener;
        context = (Context) mGListener;
    }
    public TGLMRecyclerViewAdapter(List<StockTopGL> items, LossFragment.OnListFragmentInteractionListener listener) {
        stockTopGLValues = items;
        mLListener = listener;
        context = (Context) mLListener;
    }

    public TGLMRecyclerViewAdapter(List<StockTopGL> items, MostActiveFragment.OnListFragmentInteractionListener listener) {
        stockTopGLValues = items;
        mMListener = listener;
        context = (Context) mMListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_tglm, parent, false);
        session = new SessionManager(context);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Resources resources = context.getApplicationContext().getResources();
        int name = 0, same = 0, date = 0, rise = 0, fall = 0;

        if(session.getTheme() == 0){
            name = resources.getColor(R.color.stock_date);
            same = resources.getColor(R.color.stock_same_dark);
            date = resources.getColor(R.color.stock_date_dark);
            rise = resources.getColor(R.color.stock_rise_dark);
            fall = resources.getColor(R.color.stock_fall_dark);
            holder.add.setColorFilter(resources.getColor(R.color.fill_color_dark));
            bgColors = context.getApplicationContext().getResources().getStringArray(R.array.stock_list_bg_tint_dark);
        } else if (session.getTheme() == 1){
            name = resources.getColor(R.color.stock_name);
            same = resources.getColor(R.color.stock_same);
            date = resources.getColor(R.color.stock_date);
            rise = resources.getColor(R.color.stock_rise);
            fall = resources.getColor(R.color.stock_fall);
            holder.add.setColorFilter(same);
            bgColors = context.getApplicationContext().getResources().getStringArray(R.array.stock_list_bg_tint);
        }

        float fNo;
        try{
            fNo = Float.parseFloat(stockTopGLValues.get(position).getChg());
        }catch(Exception e){
            fNo = (float) 0.0;
        }

        if (fNo == 0 && 1/fNo > 0){
            //Black color
            holder.ltp.setTextColor(same);
            holder.chg.setTextColor(same);
            holder.chg_p.setTextColor(same);
            holder.pcls.setTextColor(same);
            holder.chg.setBackgroundColor(Color.TRANSPARENT);
            holder.chg_p.setBackgroundColor(Color.TRANSPARENT);
        }else if(1/fNo < 0){
            //Red color
            holder.ltp.setTextColor(fall);
            holder.chg.setTextColor(fall);
            holder.chg_p.setTextColor(fall);
//            chg.setTextColor(Color.WHITE);
//            chg_p.setTextColor(Color.WHITE);
            if(session.getTheme() == 0) {
                holder.chg.setBackground(resources.getDrawable(R.drawable.stock_fall_left_bg_dark));
                holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_fall_right_bg_dark));
            } else {
                holder.chg.setBackground(resources.getDrawable(R.drawable.stock_fall_left_bg));
                holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_fall_right_bg));
            }
            holder.pcls.setTextColor(fall);
        }
        else{
            //Green color
            holder.ltp.setTextColor(rise);
            holder.chg.setTextColor(rise);
            holder.chg_p.setTextColor(rise);
//            chg.setTextColor(Color.parseColor("#37474f"));
//            chg_p.setTextColor(Color.parseColor("#37474f"));
            if(session.getTheme() == 0) {
                holder.chg.setBackground(resources.getDrawable(R.drawable.stock_rise_left_bg_dark));
                holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_rise_right_bg_dark));
            } else {
                holder.chg.setBackground(resources.getDrawable(R.drawable.stock_rise_left_bg));
                holder.chg_p.setBackground(resources.getDrawable(R.drawable.stock_rise_right_bg));
            }
            holder.pcls.setTextColor(rise);
        }

        holder.name.setTextColor(same);
        holder.stock_id.setTextColor(name);
        holder.date.setTextColor(date);


        holder.mItem = stockTopGLValues.get(position);
        holder.stock_id.setText(stockTopGLValues.get(position).getStock());
        holder.name.setText(stockTopGLValues.get(position).getName());
        holder.ltp.setText(stockTopGLValues.get(position).getLtp());
        holder.chg.setText(stockTopGLValues.get(position).getChg());
        holder.chg_p.setText(stockTopGLValues.get(position).getChg_p()+"%");
        holder.pcls.setText(stockTopGLValues.get(position).getPcls());
        holder.vol.setText(stockTopGLValues.get(position).getVol());
        holder.date.setText(stockTopGLValues.get(position).getStamp());

        holder.exchange.setText(stockTopGLValues.get(position).getExchange());
        holder.mcap.setText(stockTopGLValues.get(position).getmCap());
        holder.pe.setText(stockTopGLValues.get(position).getPe());
        holder.ytdc.setText(stockTopGLValues.get(position).getYtd());
        holder.wh52.setText(stockTopGLValues.get(position).getWh52());
        holder.wl52.setText(stockTopGLValues.get(position).getWl52());

        /*SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
        Date date1 = null;
        try {
            date1 = dt.parse(stockDailyValues.get(position).getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String date2;
        if (session.getStockPeriod().equals("1M"))
            date2 = new SimpleDateFormat("yyyy MMM dd, HH:mm:ss", Locale.ENGLISH).format(date1);
        else
            date2 = new SimpleDateFormat("yyyy MMM dd", Locale.ENGLISH).format(date1);
        holder.date.setText(date2);*/

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                    LinearLayout linearLayout = v.findViewById(R.id.more_stock_details);
                    ImageButton add = v.findViewById(R.id.btn_add);
                    if (linearLayout.getVisibility() == View.VISIBLE) {
                        linearLayout.setVisibility(View.GONE);
                        add.setVisibility(View.GONE);

                    } else {
                        linearLayout.setVisibility(View.VISIBLE);
                        add.setVisibility(View.VISIBLE);
                    }
            }
        });

        holder.add.setOnClickListener(v -> {
            View rlv = (RelativeLayout) v.getParent();
            TextView stockName = rlv.findViewById(R.id.name);
            TextView stockId = rlv.findViewById(R.id.stock_id);
            String sName = stockName.getText().toString();
            String id = stockId.getText().toString();
            if (mGListener != null)
                mGListener.onListFragmentInteractionAddStock(sName, id);
            if (mLListener != null)
                mLListener.onListFragmentInteractionAddStock(sName, id);
            if (mMListener != null)
                mMListener.onListFragmentInteractionAddStock(sName, id);
        });

        String color = bgColors[position % bgColors.length];
        holder.relativeLayout.setBackgroundColor(Color.parseColor(color));

    }

    @Override
    public int getItemCount() {
        //        Log.e(TAG, "getItemCount");
        if (stockTopGLValues !=null)
            return stockTopGLValues.size();
        else
            return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView name;
        public final TextView stock_id;
        public final TextView ltp;
        public final TextView chg;
        public final TextView chg_p;
        public final TextView pcls;
        public final TextView date;
        public final TextView vol;
        public final TextView exchange, pe, ytdc, mcap, wh52, wl52;
        public final RelativeLayout relativeLayout;
        public final ImageButton add;
        public StockTopGL mItem;

        public ViewHolder(View view) {
            super(view);
            name= (TextView) view.findViewById(R.id.name);
            stock_id = (TextView) view.findViewById(R.id.stock_id);
            ltp = (TextView) view.findViewById(R.id.ltp);
            chg = (TextView) view.findViewById(R.id.chg);
            chg_p = (TextView) view.findViewById(R.id.chg_p);
            pcls = (TextView) view.findViewById(R.id.pcls);
            date = (TextView) view.findViewById(R.id.date);
            vol = (TextView) view.findViewById(R.id.vol);

            exchange = (TextView) view.findViewById(R.id.exchange);
            pe = (TextView) view.findViewById(R.id.pe);
            ytdc = (TextView) view.findViewById(R.id.ytdc);
            mcap = (TextView) view.findViewById(R.id.mcap);
            wh52 = (TextView) view.findViewById(R.id.wh52);
            wl52 = (TextView) view.findViewById(R.id.wl52);
            add = (ImageButton) view.findViewById(R.id.btn_add);

            relativeLayout = (RelativeLayout) view.findViewById(R.id.list_view_top_glm);
            mView = view;
        }
    }

}
