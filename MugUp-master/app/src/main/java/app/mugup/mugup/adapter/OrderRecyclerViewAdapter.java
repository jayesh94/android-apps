package app.mugup.mugup.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.mugup.mugup.R;
import app.mugup.mugup.fragment.profile.OrderFragment.OnListFragmentInteractionListener;
import app.mugup.mugup.helper.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderRecyclerViewAdapter extends RecyclerView.Adapter<OrderRecyclerViewAdapter.ViewHolder> {

    private final List<OrderItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public OrderRecyclerViewAdapter(ArrayList<OrderItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Context context = (Context) mListener;
        holder.mItem = mValues.get(position);
        holder.tvItems.setText(mValues.get(position).getSubjects());
        holder.tvOrderedOn.setText(mValues.get(position).getPurchase_timestamp());
        holder.tvAmount.setText(context.getResources().getString(R.string.rupees, mValues.get(position).getAmount()));
        holder.tvInvoiceId.setText(mValues.get(position).getInvoiceId());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvItems;
        public final TextView tvOrderedOn;
        public final TextView tvAmount;
        public final TextView tvInvoiceId;
        public OrderItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvItems = (TextView) view.findViewById(R.id.tv_items);
            tvOrderedOn = (TextView) view.findViewById(R.id.tv_ordered_on);
            tvAmount = (TextView) view.findViewById(R.id.tv_amount);
            tvInvoiceId = (TextView) view.findViewById(R.id.tv_invoice_id);
        }
    }
}