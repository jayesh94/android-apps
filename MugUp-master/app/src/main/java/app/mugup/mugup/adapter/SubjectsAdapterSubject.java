package app.mugup.mugup.adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import 	androidx.viewpager.widget.PagerAdapter;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import app.mugup.mugup.app.AppController;
import app.mugup.mugup.fragment.SubjectDetailsFragment;
import app.mugup.mugup.helper.SubjectCard;
import app.mugup.mugup.R;

public class SubjectsAdapterSubject extends PagerAdapter implements SubjectCardAdapter
{
    private Context mContext;

    private List<CardView> mViews;
    private List<SubjectCard> mData;
    private float mBaseElevation;
    private static String TAG = SubjectsAdapterSubject.class.getSimpleName();
    TextView result;
    TextView result2;

    private ImageLoader imageLoader;

    public SubjectsAdapterSubject() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(SubjectCard item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.fragment_subjects, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.subjectsFragment);

        cardView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.i(TAG, "This page was clicked: " + position);

                result = (TextView) view.findViewById(R.id.subjectId);
                String subjectId = result.getText().toString();
                result2 = (TextView) view.findViewById(R.id.courseId);
                String courseId = result2.getText().toString();

                Fragment fragment = new SubjectDetailsFragment();
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right);

                Bundle bundle = new Bundle();
                bundle.putString("subjectId", subjectId);
                bundle.putString("courseId", courseId);
                fragment.setArguments(bundle);

                fragmentTransaction.replace(R.id.frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(final SubjectCard item, View view) {
        TextView subjectTitle = (TextView) view.findViewById(R.id.subjectTitle);
        TextView subjectId = (TextView) view.findViewById(R.id.subjectId);
        TextView courseId = (TextView) view.findViewById(R.id.courseId);
        subjectTitle.setText(item.getTitle());
        subjectId.setText(item.getSubjectId());
        courseId.setText(item.getCourseId());

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView bookCover = (NetworkImageView) view.findViewById(R.id.subjectsImageView);
        bookCover.setImageUrl(item.getImage(), imageLoader);

    }

    @Override
    public float getPageWidth(int position) {
        return 0.35f;
    }

}
