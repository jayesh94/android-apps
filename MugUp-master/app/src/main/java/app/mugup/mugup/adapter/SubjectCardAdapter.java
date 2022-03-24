package app.mugup.mugup.adapter;


import androidx.cardview.widget.CardView;

public interface SubjectCardAdapter
{

    int MAX_ELEVATION_FACTOR = 8;

    float getBaseElevation();

    CardView getCardViewAt(int position);

    int getCount();
}