package app.mugup.mugup.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import app.mugup.mugup.R;

import app.mugup.mugup.fragment.LibraryFragment;
import app.mugup.mugup.fragment.LibraryFragment.OnLibraryListFragmentInteractionListener;
import app.mugup.mugup.helper.LibraryItem;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class LibraryRecyclerViewAdapter extends RecyclerView.Adapter<LibraryRecyclerViewAdapter.ViewHolder> {

    private final List<LibraryItem> mValues;
    private final LibraryFragment.OnLibraryListFragmentInteractionListener mListener;

    public LibraryRecyclerViewAdapter(ArrayList<LibraryItem> items, OnLibraryListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Context context = (Context) mListener;
        holder.mItem = mValues.get(position);
        holder.tvSem.setText(mValues.get(position).getSemester());
        holder.tvBook.setText(mValues.get(position).getBookName());
        holder.tvAuthor.setText(mValues.get(position).getAuthorName());
        holder.tvExpiry.setText(mValues.get(position).getExpiry());
        Glide.with(context)
                .load(mValues.get(position).getBookCoverUrl())
                .thumbnail(0.5f)
                .transition(withCrossFade())
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(holder.ivBookCover);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if(!mValues.get(position).getExpiry().equals("Expired"))
                        mListener.onLibraryListFragmentInteraction(mValues.get(position).getBookUrl());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView tvSem, tvBook, tvAuthor, tvExpiry;
        final ImageView ivBookCover;
        LibraryItem mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            tvSem = view.findViewById(R.id.tv_sem);
            tvBook = view.findViewById(R.id.tv_book_name);
            tvAuthor = view.findViewById(R.id.tv_author_name);
            tvExpiry = view.findViewById(R.id.tv_expiry);
            ivBookCover = view.findViewById(R.id.iv_book_cover);
        }

    }
}
