package app.mugup.mugup.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import app.mugup.mugup.R;
import app.mugup.mugup.helper.ProfileList;

public class CustomProfileListAdapter extends ArrayAdapter<ProfileList> {

    Context context;
    int layoutResourceId;
    ArrayList<ProfileList> data;

    public CustomProfileListAdapter(Context context, int layoutResourceId, ArrayList<ProfileList> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ImageHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ImageHolder();
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            row.setTag(holder);
        }
        else
        {
            holder = (ImageHolder)row.getTag();
        }

        ProfileList myImage = data.get(position);
        holder.txtTitle.setText(myImage.name);
        int outImage=myImage.image;
        holder.imgIcon.setImageResource(outImage);
        return row;

    }

    public ProfileList getItem(int position){
        return data.get(position);
    }

    static class ImageHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}
