package app.mugup.mugup.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import app.mugup.mugup.R;
import app.mugup.mugup.adapter.SubjectCardAdapter;

public class SubjectsFragment extends Fragment {

    private CardView mCardView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);
        //mCardView = (CardView) view.findViewById(R.id.subjectsFragment);
        mCardView.setMaxCardElevation(mCardView.getCardElevation()
                * SubjectCardAdapter.MAX_ELEVATION_FACTOR);
        return view;
    }

    public CardView getCardView() {
        return mCardView;
    }
}