package info.ascetx.stockstalker.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.adapter.IntradayRecyclerViewAdapter;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.GetStockDetails;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.helper.StockDaily;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.CURRENT_TAG;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_INTRADAY_FRAME;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.INTRADAY_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_INTRA_CHART_DETAILS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class IntradayFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static String TAG = "IntradayFragment";
    private static String adUnitId = INTRADAY_FRAGMENT_AD_UNIT_ID;

    private AdView mAdView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<StockDaily> stockDaily;
    private RecyclerView recyclerView;
    private IntradayRecyclerViewAdapter adapter;
    private DatabaseHandler db;
    private View mView;
    private GetStockDetails details;
    private SessionManager session;
    private SnackBarDisplay snackBarDisplay;
    private FbLogAdEvents fbLogAdEvents;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public IntradayFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static IntradayFragment newInstance(int columnCount) {
        IntradayFragment fragment = new IntradayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        stockDaily = new ArrayList<>();
        db = new DatabaseHandler((Context) mListener);
        details = new GetStockDetails();
        session = new SessionManager(getActivity().getApplicationContext());
        snackBarDisplay = new SnackBarDisplay((Context) mListener);
        fbLogAdEvents = new FbLogAdEvents((Context) mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG,"onCreateView");
        View view = inflater.inflate(R.layout.fragment_intraday_list, container, false);
        mView = view;

        MobileAds.initialize(view.getContext(), adUnitId);
        mAdView = (AdView) view.findViewById(R.id.adView);
        if(!session.isPremiumUser()) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("intraframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    fbLogAdEvents.logAdClickEvent("intraframe_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        fetchIntradayStockDetails();
                                    }
                                }
        );

        // Set the adapter
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
//        adapter = new IntradayRecyclerViewAdapter(stockDaily, mListener);
//        recyclerView.setAdapter(adapter);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        if(session.getTheme() == 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView.setBackground(getResources().getDrawable(R.drawable.ic_filter_list_dark_24dp));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                imageView.setBackground(getResources().getDrawable(R.drawable.ic_filter_list_light_24dp));
            }
        }
        imageView.setOnClickListener(v -> selectIntradayPeriod());

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    private void selectIntradayPeriod() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("Intraday Period");
        alt_bld.setSingleChoiceItems(getResources().getStringArray(R.array.intraday_period_array), -1, (dialog, item) -> {
//            Toast.makeText(getActivity().getApplicationContext(),
//                    "Group Name = "+getResources().getStringArray(R.array.intraday_period_array)[item], Toast.LENGTH_SHORT).show();
            session.setStockPeriod(getResources().getStringArray(R.array.intraday_period_array)[item]);
            dialog.dismiss();// dismiss the alertbox after chose option
            fetchIntradayStockDetails();
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    private void fetchIntradayStockDetails() {

        swipeRefreshLayout.setRefreshing(true);

        String stockName = session.getStockName();
        String stockPeriod = session.getStockPeriod();

        String url = String.format(URL_GET_NSQ_STOCK_INTRA_CHART_DETAILS, BASE_URL, session.getStockName(), PROD_TOKEN);
        String tag_string_req = "tag_stock_details";
        Log.e(TAG, stockName);
        Log.e(TAG, url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, jsonArray -> {
            String pcls = db.getStockPcls(stockName);
            int interval = 1;

            if (jsonArray.length() > 0) {
                stockDaily.clear();
//                Log.e(TAG, String.valueOf(jsonArray));
//                Log.e(TAG, String.valueOf(jsonArray.length()));

                stockDaily = details.getStockIntradayDetails(jsonArray, pcls, stockPeriod);

                Collections.reverse(stockDaily);

                stockDaily.add(new StockDaily(null,null,null,null,null,null,null,null,null));

                adapter = new IntradayRecyclerViewAdapter(stockDaily, mListener);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                mListener.onIntraHistFragmentInteraction();

                stopRefresh();
            }
        },volleyError -> {
            snackBarDisplay.volleyErrorOccurred(TAG, mView, volleyError);
            stopRefresh();
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag_string_req);
    }

    public void stopRefresh(){
        swipeRefreshLayout.setRefreshing(false);
        if(animationDrawable!=null)
            animationDrawable.stop();
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        fetchIntradayStockDetails();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
                    fbLogAdEvents.logAdImpressionEvent("intraframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    fbLogAdEvents.logAdClickEvent("intraframe_banner");
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onIntraHistFragmentInteraction();
    }
}
