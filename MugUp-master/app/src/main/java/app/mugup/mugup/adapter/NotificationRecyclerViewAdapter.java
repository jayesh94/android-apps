package app.mugup.mugup.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.mugup.mugup.R;
import app.mugup.mugup.fragment.NotificationFragment.OnListFragmentInteractionListener;
import app.mugup.mugup.helper.NotificationItem;

import java.util.List;

public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder> {

    private final List<NotificationItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public NotificationRecyclerViewAdapter(List<NotificationItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.tvTitle.setText(mValues.get(position).getTitle());
        holder.tvMessage.setText(mValues.get(position).getMessage());
        holder.tvTs.setText(mValues.get(position).getTs());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
         final View mView;
         final TextView tvTitle, tvMessage, tvTs;
         NotificationItem mItem;

         ViewHolder(View view) {
            super(view);
            mView = view;
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvMessage = (TextView) view.findViewById(R.id.tv_message);
            tvTs = (TextView) view.findViewById(R.id.tv_time_stamp);
        }

    }
}
