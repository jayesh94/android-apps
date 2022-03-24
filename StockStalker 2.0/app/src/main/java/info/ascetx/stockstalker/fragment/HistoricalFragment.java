package info.ascetx.stockstalker.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.adapter.HistoricalRecyclerViewAdapter;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.GetStockDetails;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.helper.StockDaily;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.HISTORICAL_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_HISTORIC_CHART_DETAILS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HistoricalFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    private static final String KEY_RECYCLER_STATE = "recycler_state";

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static final String TAG = "HistoricalFragment";
    private static String adUnitId = HISTORICAL_FRAGMENT_AD_UNIT_ID;

    private AdView mAdView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<StockDaily> stockDaily;
    private RecyclerView recyclerView;
    private HistoricalRecyclerViewAdapter adapter;
    private DatabaseHandler db;
    private View mView;
    private GetStockDetails details;
    private SessionManager session;
    private TextView totalChg, totalChg_p;
    private SnackBarDisplay snackBarDisplay;
    private FbLogAdEvents fbLogAdEvents;
    private Bundle mBundleRecyclerViewState;
    private Parcelable mListState;
    private LinearLayoutManager mLinearLayoutManager;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoricalFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HistoricalFragment newInstance(int columnCount) {
        Log.e(TAG,"newInstance");
        HistoricalFragment fragment = new HistoricalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate");
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
        View view = inflater.inflate(R.layout.fragment_historical, container, false);
        mView = view;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.e(TAG,"ORIENTATION_LANDSCAPE");
        }else{
            Log.e(TAG,"ORIENTATION_PORTRAIT");
        }

        Log.e(TAG, "stockDaily.size(): "+ stockDaily.size());

        MobileAds.initialize(view.getContext(), adUnitId);
        mAdView = (AdView) view.findViewById(R.id.adView);
        if(!session.isPremiumUser()) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("histframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("histframe_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        totalChg_p = (TextView) view.findViewById(R.id.total_chg_p);
        totalChg = (TextView) view.findViewById(R.id.total_chg);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        if (savedInstanceState == null) {
            swipeRefreshLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e(TAG, "swipeRefreshLayout.post");
                                            if(stockDaily.size() == 0)
                                                fetchHistoricalStockDetails();
                                            else
                                                displayFetchedHistoricalStockDetails();
                                        }
                                    }
            );
        }

        // Set the adapter
        Context context = view.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        mLinearLayoutManager = new LinearLayoutManager(context);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(mLinearLayoutManager);
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
//        adapter = new HistoricalRecyclerViewAdapter(stockDaily, mListener);
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
        imageView.setOnClickListener(v -> selectHistoricalPeriod());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG,"onViewCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mListState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                    recyclerView.getLayoutManager().onRestoreInstanceState(mListState);

                }
            }, 50);
        }


        recyclerView.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        mBundleRecyclerViewState = new Bundle();

        mListState = recyclerView.getLayoutManager().onSaveInstanceState();

        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState");
    }

    private void selectHistoricalPeriod() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("Historical Period");
        alt_bld.setSingleChoiceItems(getResources().getStringArray(R.array.historical_period_array), -1, (dialog, item) -> {
//            Toast.makeText(getActivity().getApplicationContext(),
//                    "Group Name = "+getResources().getStringArray(R.array.historical_period_array)[item], Toast.LENGTH_SHORT).show();
            session.setStockPeriod(getResources().getStringArray(R.array.historical_period_array)[item]);
            dialog.dismiss();// dismiss the alertbox after chose option
            Log.e(TAG,"selectHistoricalPeriod");
            fetchHistoricalStockDetails();
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    private void fetchHistoricalStockDetails() {
        Log.e(TAG, "fetchHistoricalStockDetails");
        swipeRefreshLayout.setRefreshing(true);

        String stockPeriod = session.getStockPeriod();
        String url = String.format(URL_GET_NSQ_STOCK_HISTORIC_CHART_DETAILS, session.getStockName(), stockPeriod);

        String tag_string_req = "tag_stock_details";
//        Log.e(TAG, stockName);
        Log.e(TAG, url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, jsonArray -> {
            Float cls, pcls;
            int rise = 0, fall = 0;
            if (session.getTheme() == 0){
                rise = getResources().getColor(R.color.stock_rise_dark);
                fall = getResources().getColor(R.color.stock_fall_dark);
            } else if (session.getTheme() == 1){
                rise = getResources().getColor(R.color.stock_rise);
                fall = getResources().getColor(R.color.stock_fall);
            }
            if (jsonArray.length() > 0) {
                stockDaily.clear();
                stockDaily = details.getStockHistoricalDetails(jsonArray);
//                    for (StockDaily sd : stockDaily ){
//                        System.out.println(sd.getDate()+" "+sd.getCls()+" "+sd.getPcls()+" "+sd.getChg()+" "+sd.getChg_p());
//                    }

                Collections.reverse(stockDaily);

                cls = Float.parseFloat(stockDaily.get(0).getCls());
                pcls = Float.parseFloat(stockDaily.get(stockDaily.size()-1).getCls());

                if(1/(cls - pcls) < 0) {
                    totalChg_p.setTextColor(fall);
                    totalChg.setTextColor(fall);
                }
                else if(1/(cls - pcls) > 0) {
                    totalChg_p.setTextColor(rise);
                    totalChg.setTextColor(rise);
                }

                totalChg.setText(String.format(Locale.ENGLISH,"%.2f", cls - pcls));
                totalChg_p.setText(String.format(Locale.ENGLISH,"%.2f", ((cls - pcls)/pcls)*100) + "%");

                stockDaily.add(new StockDaily(null,null,null,null,null,null,null,null,null));

                adapter = new HistoricalRecyclerViewAdapter(stockDaily, mListener);
                recyclerView.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();

                mListener.onIntraHistFragmentInteraction();

                stopRefresh();
            }
        },volleyError -> {
            VolleyLog.d(TAG, "Volley Error: " + volleyError.getMessage());
            snackBarDisplay.volleyErrorOccurred(TAG, mView, volleyError);
            stopRefresh();
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonArrayRequest, tag_string_req);
    }

 private void displayFetchedHistoricalStockDetails() {
        Log.e(TAG, "displayFetchedHistoricalStockDetails");
        swipeRefreshLayout.setRefreshing(true);

        Float cls, pcls;
        int rise = 0, fall = 0;
        if (session.getTheme() == 0){
            rise = getResources().getColor(R.color.stock_rise_dark);
            fall = getResources().getColor(R.color.stock_fall_dark);
        } else if (session.getTheme() == 1){
            rise = getResources().getColor(R.color.stock_rise);
            fall = getResources().getColor(R.color.stock_fall);
        }

        cls = Float.parseFloat(stockDaily.get(0).getCls());
        pcls = Float.parseFloat(stockDaily.get(stockDaily.size()-2).getCls());

        if(1/(cls - pcls) < 0) {
            totalChg_p.setTextColor(fall);
            totalChg.setTextColor(fall);
        }
        else if(1/(cls - pcls) > 0) {
            totalChg_p.setTextColor(rise);
            totalChg.setTextColor(rise);
        }

        totalChg.setText(String.format(Locale.ENGLISH,"%.2f", cls - pcls));
        totalChg_p.setText(String.format(Locale.ENGLISH,"%.2f", ((cls - pcls)/pcls)*100) + "%");

        adapter = new HistoricalRecyclerViewAdapter(stockDaily, mListener);
        recyclerView.setAdapter(adapter);

        stopRefresh();
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
        Log.e(TAG,"onDetach");
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }

    @Override
    public void onRefresh() {
        Log.e(TAG,"onRefresh");
        fetchHistoricalStockDetails();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            try {
                assert getFragmentManager() != null;
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                if (Build.VERSION.SDK_INT >= 26) {
                    ft.setReorderingAllowed(false);
                }
                ft.detach(this).attach(this).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                    fbLogAdEvents.logAdImpressionEvent("histframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("histframe_banner");
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
