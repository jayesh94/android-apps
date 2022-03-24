package app.mugup.mugup.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import app.mugup.mugup.R;
import app.mugup.mugup.app.AppController;
import app.mugup.mugup.helper.SubjectSelectionListRow;

public class SubjectSelectionListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<SubjectSelectionListRow> subjectSelectionListRowItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public SubjectSelectionListAdapter(Activity activity, List<SubjectSelectionListRow> subjectSelectionListRowItems) {
        this.activity = activity;
        this.subjectSelectionListRowItems = subjectSelectionListRowItems;
    }

    @Override
    public int getCount() {
        return subjectSelectionListRowItems.size();
    }

    @Override
    public Object getItem(int location) {
        return subjectSelectionListRowItems.get(location);
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
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);
        TextView bookName = (TextView) convertView.findViewById(R.id.bookName);
        TextView bookId = (TextView) convertView.findViewById(R.id.bookId);
        TextView authorName = (TextView) convertView.findViewById(R.id.authorName);
        TextView bookPrice = (TextView) convertView.findViewById(R.id.bookPrice);
        CheckBox bookSelection = (CheckBox) convertView.findViewById(R.id.bookSelection);

        // getting movie data for the row
        SubjectSelectionListRow m = subjectSelectionListRowItems.get(position);

        // thumbnail image
        thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);

        // title
        bookName.setText(m.getTitle());

        // bookId
        bookId.setText(m.getBookId());

        // rating
        authorName.setText("By " + String.valueOf(m.getRating()));

        // genre
        bookPrice.setText("₹" + m.getGenre());

        // release year
        bookSelection.setChecked(m.getYear());

        return convertView;
    }

}