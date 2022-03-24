package info.ascetx.stockstalker.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.adapter.TGLMRecyclerViewAdapter;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.helper.StockTopGL;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.MOSTACTIVE_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_TOP_MOVERS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

public class MostActiveFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String TAG = "MostActiveFragment";
    private static String adUnitId = MOSTACTIVE_FRAGMENT_AD_UNIT_ID;

    private AdView mAdView;
    private View mView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TGLMRecyclerViewAdapter adapter;
    private SessionManager session;
    private List<StockTopGL> stockTopGL;
    private RecyclerView recyclerView;
    private SnackBarDisplay snackBarDisplay;
    private OnListFragmentInteractionListener mListener;
    private FbLogAdEvents fbLogAdEvents;

    public MostActiveFragment() {
        // Required empty public constructor
    }

    public static MostActiveFragment newInstance(String param1, String param2) {
        MostActiveFragment fragment = new MostActiveFragment();
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

        stockTopGL = new ArrayList<>();
        snackBarDisplay = new SnackBarDisplay((Context) mListener);
        session = new SessionManager((Context) mListener);
        fbLogAdEvents = new FbLogAdEvents((Context) mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_most_active, container, false);
        mView = view;

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        try {
                                            fetchMostActiveStockDetails();
                                        } catch (NumberFormatException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
        );

        // Set the adapter
        // Set the adapter
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        MobileAds.initialize(view.getContext(), adUnitId);
        mAdView = (AdView) view.findViewById(R.id.adView);
        if(!session.isPremiumUser()) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("mostactive_frame_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("mostactive_frame_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        return view;
    }

    private void fetchMostActiveStockDetails() {
        String tag_string_req = "tag_stock_details";
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###.##");
        String url = String.format(URL_GET_NSQ_TOP_MOVERS, BASE_URL, PROD_TOKEN);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String name, stock_id, ltp, chg, chgp, pcls, vol, ts, exch, mcap, pe, wh52, wl52, ytdc;
                        stockTopGL.clear();

                        // Process the JSON
                        try{
                            // Loop through the array elements
                            for(int i=0;i<response.length();i++){
                                // Get current json object
                                JSONObject stock = response.getJSONObject(i);
                                /*"primaryExchange": "SNADQA",
                                        "marketCap": 398969759,
                                        "peRatio": -4.32,
                                        "week52High": 4.07,
                                        "week52Low": 0.0538,
                                        "ytdChange": 13.30172,*/

                                // Get the current student (json object) data
                                name = stock.getString("companyName");
                                stock_id = stock.getString("symbol");
                                ltp = String.valueOf(stock.getString("latestPrice"));
                                chg = String.valueOf(stock.getString("change"));
                                chgp = String.valueOf(stock.getString("changePercent"));
                                pcls = String.valueOf(stock.getString("previousClose"));
                                vol = stock.getString("latestVolume");
                                exch = stock.getString("primaryExchange");
                                mcap = stock.getString("marketCap");
                                pe = String.valueOf(stock.getString("peRatio"));
                                wh52 = String.valueOf(stock.getString("week52High"));
                                wl52 = String.valueOf(stock.getString("week52Low"));
                                ytdc = String.valueOf(stock.getString("ytdChange"));

                                mcap = ((!mcap.equals("null")) ? mcap : "0");
                                pe = ((!pe.equals("null")) ? pe : "0");
                                wh52 = ((!wh52.equals("null")) ? wh52 : "0");
                                wl52 = ((!wl52.equals("null")) ? wl52 : "0");
                                ytdc = ((!ytdc.equals("null")) ? ytdc : "0");
                                vol = ((!vol.equals("null")) ? vol : "0");
                                ltp = ((!ltp.equals("null")) ? ltp : "0");
                                chg = ((!chg.equals("null")) ? chg : "0");
                                chgp = ((!chgp.equals("null")) ? chgp : "0");
                                pcls = ((!pcls.equals("null")) ? pcls : "0");

                                exch = (exch.equals("New York Stock Exchange")) ? "NYSE" : exch;
                                mcap = decimalFormat.format(Float.parseFloat(mcap));
                                pe = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(pe));
                                wh52 = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(wh52));
                                wl52 = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(wl52));
                                ytdc = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(ytdc));
                                vol = decimalFormat.format(Float.parseFloat(vol));
                                ltp = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(ltp));
                                chg = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(chg));
                                chgp = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(chgp)*100);
                                pcls = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(pcls));
                                ts = stock.getString("latestTime");

                                stockTopGL.add(new StockTopGL(stock_id, name, ltp, chg, chgp, pcls, vol, ts,
                                                            exch, mcap, pe, wh52, wl52, ytdc));
                            }
                            adapter = new TGLMRecyclerViewAdapter(stockTopGL, mListener);
                            recyclerView.setAdapter(adapter);
                            stopRefresh();
                        }catch (JSONException | NumberFormatException e){
                            snackBarDisplay.parseErrorOccurred(TAG, mView, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        VolleyLog.d(TAG, "Volley Error: " + error.getMessage());
                        snackBarDisplay.volleyErrorOccurred(TAG, mView, error);
                        stopRefresh();
                    }
                }
        );
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag_string_req);
    }

    private void stopRefresh(){
        if(animationDrawable!=null)
            animationDrawable.stop();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        Log.e(TAG,"onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        fetchMostActiveStockDetails();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!session.isPremiumUser()) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mAdView.getLayoutParams();
            FrameLayout parent = (FrameLayout) mAdView.getParent();
            parent.removeView(mAdView);
            mAdView = new AdView(mView.getContext());
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId(adUnitId);
            mAdView.setLayoutParams(lp);
            parent.addView(mAdView);
            mAdView.setVisibility(View.GONE);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("mostactive_frame_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG, "onAdOpened");
                    fbLogAdEvents.logAdClickEvent("mostactive_frame_banner");
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    mAdView.setVisibility(View.GONE);
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteractionAddStock(String name, String id);
    }
}
