package info.ascetx.stockstalker.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.adapter.NewsRecyclerViewAdapter;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.NewsRecyclerTouchListener;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.helper.News;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.CURRENT_TAG;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_NEWS_FRAME;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_NEWS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StockNewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockNewsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String TAG = "StockNewsFragment";

    // The number of native ads to load and display.
    private static final int NUMBER_OF_ADS = 5;

    private NewsRecyclerViewAdapter adapter;

    private View mView, mProgressView;
    private TextView latestNewsTitle;
    private SessionManager session;
    private OnStockNewsFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private SnackBarDisplay snackBarDisplay;
    private FbLogAdEvents fbLogAdEvents;

    // The AdLoader used to load ads.
    private AdLoader adLoader;

    // List of MenuItems and native ads that populate the RecyclerView.
    private List<Object> newsArrayList = new ArrayList<>();

    // List of native ads that have been successfully loaded.
    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();

    public StockNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StockNewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StockNewsFragment newInstance(String param1, String param2) {
        StockNewsFragment fragment = new StockNewsFragment();
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

        session = new SessionManager((Context) mListener);
        snackBarDisplay = new SnackBarDisplay((Context) mListener);
        fbLogAdEvents = new FbLogAdEvents((Context) mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stock_news, container, false);
        mView = view ;
        mProgressView = view.findViewById(R.id.news_progress);

        latestNewsTitle = (TextView) view.findViewById(R.id.latest_news_stock_title);
        latestNewsTitle.setPaintFlags(latestNewsTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        latestNewsTitle.setText(String.format(getActivity().getResources().getString(R.string.latest_news_stock_title), session.getStockName()));

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        newsArrayList = new ArrayList<>();
        adapter = new NewsRecyclerViewAdapter(this.getContext(), newsArrayList);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new NewsRecyclerTouchListener(this.getContext(), recyclerView, new NewsRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
//                Toast.makeText(getContext(), newsArrayList.get(position).getUrl() + " is selected!", Toast.LENGTH_SHORT).show();
                if(newsArrayList.get(position) instanceof News){
                    Log.e(TAG,((News) newsArrayList.get(position)).getUrl());
                    NewsFragment.postUrl = ((News) newsArrayList.get(position)).getUrl();
                    CURRENT_TAG = TAG_STOCK_NEWS_FRAME;
                    mListener.onStockFragmentInteraction(StockNewsFragment.this);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        createNewsListData();

        return view;
    }

    private void createNewsListData() {
        showProgress(true);
        String url = String.format(URL_GET_NSQ_STOCK_NEWS, BASE_URL, session.getStockName(), PROD_TOKEN);
        Log.e(TAG, url);
        String tag_string_req = "tag_stock_news";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, jsonArray -> {
            String headline, source, surl, summary, image;
            long datetime;
            if (jsonArray.length() > 0) {
//                Log.e(TAG, String.valueOf(jsonArray));
                for (int i = 0; i < jsonArray.length(); i++){
                    try {
                        datetime = (long) jsonArray.getJSONObject(i).get("datetime");
                        /*datetime = datetime.substring(0, datetime.length() - 6);
                        Log.e(TAG,datetime);
                        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
                        String outputPattern = "dd-MMM-yyyy h:mm a";
                        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

                        Date date = null;
                        String str = null;

                        try {
                            date = inputFormat.parse(datetime);
                            str = outputFormat.format(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        datetime = str;*/

                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy h:mm a");
                        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                        String dateString = formatter.format(new Date(datetime));

                        headline = String.valueOf(jsonArray.getJSONObject(i).get("headline"));
                        source = String.valueOf(jsonArray.getJSONObject(i).get("source"));
                        surl = String.valueOf(jsonArray.getJSONObject(i).get("url"));
                        summary = String.valueOf(jsonArray.getJSONObject(i).get("summary"));
                        image = String.valueOf(jsonArray.getJSONObject(i).get("image"));

                        newsArrayList.add(new News(dateString, headline, source, surl, summary, image));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showProgress(false);
                    }
                }

                if(!session.isPremiumUser())
                    loadNativeAds();
                else
                    loadMenu();
            }
        },volleyError -> {
            VolleyLog.d(TAG, "Volley Error: " + volleyError.getMessage());
            showProgress(false);
            snackBarDisplay.volleyErrorOccurred(TAG, mView, volleyError);
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag_string_req);

    }

    private void insertAdsInMenuItems() {
        if (mNativeAds.size() <= 0) {
            loadMenu();
            return;
        }
        int offset = (newsArrayList.size() / mNativeAds.size()) + 1;
        int index = 1;
        for (UnifiedNativeAd ad : mNativeAds) {
            newsArrayList.add(index, ad);
            index = index + offset;
        }
        loadMenu();
    }

    private void loadNativeAds() {

        AdLoader.Builder builder = new AdLoader.Builder(Objects.requireNonNull(this.getContext()), getString(R.string.native_ad_unit_id));
        adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
//                      isAdded() check if the fragment is still attached to the activity
//                      Fix for error:
//                      IllegalStateException: Fragment not attached to a context.
//                      Log.e(TAG, "isAdded: "+isAdded());
                        if (!isAdded()) {
                            return;
                        }
                        // A native ad loaded successfully, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        mNativeAds.add(unifiedNativeAd);
                        Log.e(TAG, String.valueOf(mNativeAds.size()));
                        if (!adLoader.isLoading()) {
                            insertAdsInMenuItems();
                            Log.e(TAG, "All native ads finished loading.");
                            fbLogAdEvents.logAdImpressionEvent("stockframe_native");
                        }
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        Log.e(TAG, "onAdClicked native ad");
                        fbLogAdEvents.logAdClickEvent("stockframe_native");
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Log.e(TAG, "The previous native ad failed to load. Attempting to"
                                + " load another.");
                        if (!adLoader.isLoading()) {
                            insertAdsInMenuItems();
                        }
                    }
                }).build();

        // Load the Native ads.
        adLoader.loadAds(new AdRequest.Builder().build(), NUMBER_OF_ADS);
    }

    private void loadMenu() {
        showProgress(false);
        adapter.notifyDataSetChanged();
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        recyclerView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        if (!show){
            if(animationDrawable!=null)
                animationDrawable.stop();
        }
    }

    public void onRefresh(){
        createNewsListData();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStockNewsFragmentInteractionListener) {
            mListener = (OnStockNewsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStockFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG,"onDestroyView()");
        super.onDestroyView();
    }

    public interface OnStockNewsFragmentInteractionListener {
        // TODO: Update argument type and name
        void onStockFragmentInteraction(Fragment stockNewsFragment);
    }
}
