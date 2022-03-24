package app.mugup.mugup.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;

import app.mugup.mugup.R;
import app.mugup.mugup.app.AppController;
import app.mugup.mugup.app.Config;
import app.mugup.mugup.app.SubjectCardShadowTransformer;
import app.mugup.mugup.adapter.SubjectCardFragmentPagerAdapter;
import app.mugup.mugup.adapter.SubjectsAdapterSubject;
import app.mugup.mugup.helper.SubjectData;

import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.folioreader.FolioReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static app.mugup.mugup.MainActivity.TAG_SUBJECT_DETAILS_FRAME;
import static app.mugup.mugup.MainActivity.CURRENT_TAG;

public class SubjectDetailsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String TAG = SubjectDetailsFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;
    private ProgressDialog pDialog;
    FolioReader folioReader;

    private OnSubjectDetailsFragmentInteractionListener mListener;

    private View mView;
    TextView subjectName;
    TextView authorName;
    TextView bookPrice;
    TextView bookSummary;

    private ViewPager mViewPager;
    private SubjectsAdapterSubject mCardAdapter;
    private SubjectCardShadowTransformer mCardSubjectCardShadowTransformer;
    private SubjectCardFragmentPagerAdapter mFragmentCardAdapter;
    private SubjectCardShadowTransformer mFragmentCardSubjectCardShadowTransformer;

    private Spinner spinner;
    protected List<SubjectData> subjectData;
    private RequestQueue queue;

    public SubjectDetailsFragment() {
        // Required empty public constructor
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
        CURRENT_TAG = TAG_SUBJECT_DETAILS_FRAME;
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Please Wait...");
        pDialog.setCancelable(false);

        folioReader = FolioReader.get();

        mCardAdapter = new SubjectsAdapterSubject();

        mFragmentCardAdapter = new SubjectCardFragmentPagerAdapter(getActivity().getSupportFragmentManager(),
                dpToPixels(2, getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subject_details, container, false);
        mView = view ;

//        View view2 = inflater.inflate(R.layout.activity_main, container, false);
//        Toolbar toolbar = (Toolbar) view2.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        queue = Volley.newRequestQueue((Context) mListener);
        requestJsonObject();
        return view;
    }

    private void requestJsonObject(){
        showpDialog();
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);
        final String subjectId = getArguments().getString("subjectId");
        final String courseId = getArguments().getString("courseId");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.GET_SUBJECT_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e(TAG,response);
                GsonBuilder builder = new GsonBuilder();
                Gson mGson = builder.create();
                if(mGson.fromJson(response, SubjectData[].class)!= null) {
                    subjectData = Arrays.asList(mGson.fromJson(response, SubjectData[].class));
                }
                if(null != subjectData){

                    subjectName = (TextView) mView.findViewById(R.id.subjectName);
                    authorName = (TextView) mView.findViewById(R.id.authorName);
                    bookPrice = (TextView) mView.findViewById(R.id.bookPrice);
                    bookSummary = (TextView) mView.findViewById(R.id.bookSummary);
                    subjectName.setText(subjectData.get(0).getBookName());
                    authorName.setText("By "+subjectData.get(0).getAuthorName());
                    bookPrice.setText("₹"+subjectData.get(0).getPrice());
                    bookSummary.setText(subjectData.get(0).getBookSummary());

                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(subjectData.get(0).getBookName());

                    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
                    NetworkImageView bookCover = (NetworkImageView) mView.findViewById(R.id.bookCoverImage);

                    bookCover.setImageUrl(subjectData.get(0).getBookCover(), imageLoader);
                    Log.e(TAG, subjectData.get(0).getBookCover());

                    Button bookSummaryButton = (Button) mView.findViewById(R.id.summaryButton);
                    bookSummaryButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            //new DownloadFileFromURL().execute(file_url);
                            mListener.onSubjectDetailsFragmentInteractionListener(subjectData.get(0).getBookSampleUrl());
                            //new DownloadFileFromURL().execute(subjectData.get(0).getBookSampleUrl());
                        }
                    });

                    Button bookDownloadButton = (Button) mView.findViewById(R.id.downloadButton);
                    bookDownloadButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            FragmentManager fm = getFragmentManager();

                            SubjectSelectionFragment dialogFragment = new SubjectSelectionFragment ();
                            Bundle bundle = new Bundle();
                            bundle.putString("courseId", courseId);
                            bundle.putString("subjectId", subjectId);
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(fm, "Subject Selector");

                            /*Fragment fragment = new SubjectDetailsFragment();
                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                                    android.R.anim.slide_out_right);

                            Bundle bundle2 = new Bundle();
                            bundle2.putString("subjectId", subjectId);
                            bundle2.putString("courseId", courseId);
                            fragment.setArguments(bundle2);

                            fragmentTransaction.replace(R.id.frame, fragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commitAllowingStateLoss();*/
                        }
                    });
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
                MyData.put("id", String.valueOf(subjectId)); //Add the data you'd like to send to the server.
                return MyData;
            }
        };

        queue.add(stringRequest);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSubjectDetailsFragmentInteractionListener(subjectData.get(0).getBookSampleUrl());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubjectDetailsFragmentInteractionListener) {
            mListener = (OnSubjectDetailsFragmentInteractionListener) context;
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

    public interface OnSubjectDetailsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSubjectDetailsFragmentInteractionListener(String bookSampleUrl);
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