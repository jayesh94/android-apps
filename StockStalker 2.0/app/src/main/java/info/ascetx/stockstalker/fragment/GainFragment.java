package info.ascetx.stockstalker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

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
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.helper.StockTopGL;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_TOP_GAINERS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class GainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private static String TAG = "GainFragment";

    private View mView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TGLMRecyclerViewAdapter adapter;
    private List<StockTopGL> stockTopGL;
    private RecyclerView recyclerView;
    private SnackBarDisplay snackBarDisplay;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GainFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static GainFragment newInstance(int columnCount) {
        GainFragment fragment = new GainFragment();
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

        stockTopGL = new ArrayList<>();
        snackBarDisplay = new SnackBarDisplay((Context) mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_glm_list, container, false);
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
                                        fetchTopGainStockDetails();
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
//        adapter = new HistoricalRecyclerViewAdapter(stockDaily, mListener);
//        recyclerView.setAdapter(adapter);

        return view;
    }


    private void fetchTopGainStockDetails() {
        String tag_string_req = "tag_stock_details";
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###.##");
        String url = String.format(URL_GET_NSQ_TOP_GAINERS, BASE_URL, PROD_TOKEN);
        Log.e(TAG,url);
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
        fetchTopGainStockDetails();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.e(TAG,"isVisibleToUser");
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
        void onListFragmentInteractionAddStock(String name, String id);
    }
}
