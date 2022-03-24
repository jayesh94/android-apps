package app.mugup.mugup.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.cardview.widget.CardView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import app.mugup.mugup.fragment.SubjectsFragment;

public class SubjectCardFragmentPagerAdapter extends FragmentStatePagerAdapter implements SubjectCardAdapter
{

    private List<SubjectsFragment> mFragments;
    private float mBaseElevation;

    public SubjectCardFragmentPagerAdapter(FragmentManager fm, float baseElevation) {
        super(fm);
        mFragments = new ArrayList<>();
        mBaseElevation = baseElevation;

        for(int i = 0; i< 5; i++){
            addCardFragment(new SubjectsFragment());
        }
    }

    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mFragments.get(position).getCardView();
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object fragment = super.instantiateItem(container, position);
        mFragments.set(position, (SubjectsFragment) fragment);
        return fragment;
    }

    public void addCardFragment(SubjectsFragment fragment) {
        mFragments.add(fragment);
    }

}