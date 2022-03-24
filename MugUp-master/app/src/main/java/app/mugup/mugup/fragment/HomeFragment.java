package app.mugup.mugup.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import app.mugup.mugup.R;
import android.widget.AdapterView;
import app.mugup.mugup.adapter.CourseSelectorAdapter;
import app.mugup.mugup.app.Config;
import app.mugup.mugup.app.SubjectCardShadowTransformer;
import app.mugup.mugup.adapter.SubjectCardFragmentPagerAdapter;
import app.mugup.mugup.adapter.SubjectsAdapterSubject;
import app.mugup.mugup.helper.SubjectCard;
import app.mugup.mugup.helper.SubjectData;

import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.RelativeLayout.LayoutParams;

public class HomeFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String TAG = HomeFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;
    private ProgressDialog pDialog;

    static int coursePosition;
    private OnFragmentInteractionListener mListener;

    private View mView;
    TextView semesterName, mailid;
    LayoutParams layoutparams;

    private SubjectsAdapterSubject subjectsAdapter;
    private SubjectCardShadowTransformer mCardSubjectCardShadowTransformer;
    private SubjectCardFragmentPagerAdapter mFragmentCardAdapter;
    private SubjectCardShadowTransformer mFragmentCardSubjectCardShadowTransformer;

    private Spinner courseSelector;
    protected List<SubjectData> spinnerData;

    private RequestQueue queue;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        subjectsAdapter = new SubjectsAdapterSubject();

        mFragmentCardAdapter = new SubjectCardFragmentPagerAdapter(getActivity().getSupportFragmentManager(),
                dpToPixels(2, getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mView = view ;

//        View view2 = inflater.inflate(R.layout.activity_main, container, false);
//        Toolbar toolbar = (Toolbar) view2.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        queue = Volley.newRequestQueue((Context) mListener);
        requestJsonObject();
        return view;
    }

    private void requestJsonObject(){
        showpDialog();
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Config.GET_COURSES, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                GsonBuilder builder = new GsonBuilder();
                Gson mGson = builder.create();
                spinnerData = Arrays.asList(mGson.fromJson(response, SubjectData[].class));
                if(null != spinnerData){
                    courseSelector = (Spinner) mView.findViewById(R.id.courseSelector);
                    assert courseSelector != null;
                    courseSelector.setVisibility(View.VISIBLE);
                    CourseSelectorAdapter courseSelectorAdapter = new CourseSelectorAdapter((Context) mListener, spinnerData);
                    courseSelector.setAdapter(courseSelectorAdapter);

                    courseSelector.setSelection(coursePosition);

                    courseSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                            showpDialog();
                            coursePosition = position;

                            final int courseId = Integer.parseInt(spinnerData.get(position).getId());

                            StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Config.GET_SUBJECTS, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    List<SubjectData> cardData = Arrays.asList();
                                    GsonBuilder builder = new GsonBuilder();
                                    Gson mGson = builder.create();
                                    if(response.equals("ERROR"))
                                    {
                                        Log.e(TAG,response);
                                        RelativeLayout frameLayout = (RelativeLayout) mView.findViewById(R.id.homefragframlay2);
                                        frameLayout.removeAllViews();
                                        int prevId = R.id.homefragframlay2;

                                        TextView textview = new TextView(getContext());
                                        textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                        textview.setText(getResources().getString(R.string.error_books_unavailable));
                                        textview.setId(View.generateViewId());
                                        LayoutParams layoutparams2;

                                        layoutparams2 = new LayoutParams(
                                                LayoutParams.WRAP_CONTENT,
                                                LayoutParams.WRAP_CONTENT
                                        );

                                        layoutparams2.addRule(RelativeLayout.BELOW,prevId);
                                        textview.setLayoutParams(layoutparams2);
                                        textview.setTypeface(textview.getTypeface(), Typeface.BOLD);
                                        textview.setTextColor((int) getResources().getColor(R.color.primary_dark));

                                        frameLayout.addView(textview);
                                    }
                                    else
                                    {
                                        if (mGson.fromJson(response, SubjectData[].class) != null)
                                        {
                                            cardData = Arrays.asList(mGson.fromJson(response, SubjectData[].class));
                                        }
                                    }
                                    if(cardData.size() > 0)
                                    {
                                        Log.e(TAG,response);
                                        Log.e(TAG, String.valueOf(cardData));
                                        RelativeLayout frameLayout = (RelativeLayout) mView.findViewById(R.id.homefragframlay2);
                                        frameLayout.removeAllViews();

                                        ArrayList<String> semesterList = new ArrayList<String>();
                                        for(int l = 0; l<= cardData.size()-1; l++)
                                        {
                                            if(!semesterList.contains(cardData.get(l).getSemesterId()))
                                            {
                                                semesterList.add(cardData.get(l).getSemesterId());
                                            }
                                        }

                                        int prevId = R.id.homefragframlay2;

                                        for(int z = 0; z<= semesterList.size()-1; z++)
                                        {
                                            TextView textview = new TextView(getContext());
                                            textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                            textview.setText("Semester "+semesterList.get(z));
                                            textview.setId(View.generateViewId());
                                            LayoutParams layoutparams2;

                                            layoutparams2 = new LayoutParams(
                                                    LayoutParams.WRAP_CONTENT,
                                                    LayoutParams.WRAP_CONTENT
                                            );

                                            layoutparams2.addRule(RelativeLayout.BELOW,prevId);
                                            textview.setLayoutParams(layoutparams2);
                                            int primaryColor = (int) getResources().getColor(R.color.primary_dark);

                                            textview.setTypeface(textview.getTypeface(), Typeface.BOLD);
                                            textview.setTextColor(primaryColor);

                                            frameLayout.addView(textview);
                                            prevId = textview.getId();

                                            ViewPager viewPager = new ViewPager(getContext());

                                            int height = (int) getResources().getDimension(R.dimen.viewPagerHeight);
                                            int marginTop = (int) getResources().getDimension(R.dimen.viewPagerMarginTop);
                                            int paddingEndRight = (int) getResources().getDimension(R.dimen.viewPagerPaddingEndRight);
                                            int paddingBottomTop = (int) getResources().getDimension(R.dimen.viewPagerPaddingBottomTop);

                                            layoutparams = new LayoutParams(
                                                    LayoutParams.WRAP_CONTENT,
                                                    height
                                            );

                                            layoutparams.setMargins(0, marginTop, 0, 0);

                                            layoutparams.addRule(RelativeLayout.BELOW,prevId);
                                            viewPager.setVisibility(View.VISIBLE);
//                                            viewPager.setPadding(0, paddingBottomTop, paddingEndRight, paddingBottomTop);
//                                            viewPager.setPaddingRelative(0,0,paddingEndRight,0);
                                            viewPager.setLayoutParams(layoutparams);
                                            viewPager.setClipToPadding(false);
                                            viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
                                            viewPager.setOffscreenPageLimit(3);

                                            subjectsAdapter = new SubjectsAdapterSubject();

                                            for (int l = 0; l <= cardData.size() - 1; l++)
                                            {
                                                if(semesterList.get(z).equals(cardData.get(l).getSemesterId()))
                                                {
                                                    subjectsAdapter.addCardItem(new SubjectCard(cardData.get(l).getAuthorName(), cardData.get(l).getCourseId(), cardData.get(l).getBookCover(), cardData.get(l).getId(), cardData.get(l).getCourseId()));
                                                }
                                            }

                                            mCardSubjectCardShadowTransformer = new SubjectCardShadowTransformer(viewPager, subjectsAdapter);
                                            mFragmentCardSubjectCardShadowTransformer = new SubjectCardShadowTransformer(viewPager, mFragmentCardAdapter);

                                            viewPager.setAdapter(subjectsAdapter);
//                                            viewPager.setPageTransformer(false, mCardSubjectCardShadowTransformer);
                                            viewPager.setId(View.generateViewId());
                                            frameLayout.addView(viewPager);
                                            prevId = viewPager.getId();
                                        }
                                    }
                                    hidepDialog();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, String.valueOf(error));
                                }
                            }) {
                                protected Map<String, String> getParams() {
                                    Map<String, String> MyData = new HashMap<String, String>();
                                    MyData.put("id", String.valueOf(courseId));
                                    return MyData;
                                }
                            };

                            queue.add(stringRequest2);
                        }
                        public void onNothingSelected(AdapterView<?> parent) {
                            semesterName.setText("");
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
                Log.e(TAG,"Volley error ");
            }
        });
        queue.add(stringRequest);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mCardSubjectCardShadowTransformer.enableScaling(b);
        mFragmentCardSubjectCardShadowTransformer.enableScaling(b);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}