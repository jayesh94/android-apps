package info.ascetx.stockstalker.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.adapter.NewsRecyclerViewAdapter;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.GetStockDetails;
import info.ascetx.stockstalker.app.NewsRecyclerTouchListener;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.helper.News;
import info.ascetx.stockstalker.helper.StockDaily;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.CURRENT_TAG;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_CHART_FRAME;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_HISTORICAL_FRAME;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_INTRADAY_FRAME;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_NEWS_FRAME;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.STOCK_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_1DM_CHART_DETAILS;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_NEWS;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_QUOTE;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnStockFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StockFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String TAG = "StockFragment";
    private static String adUnitId = STOCK_FRAGMENT_AD_UNIT_ID;

    private final static int SECONDS_UNTIL_INTERSTITIAL_PROMPT = 60;//Seconds after which int Ad should be shown after int ad close or app launch

    // The number of native ads to load and display.
    private static final int NUMBER_OF_ADS = 5;

    private OnStockFragmentInteractionListener mListener;
    private SessionManager session;
    private List<BarEntry> barEntries;
    private List<Entry> entries;
    private List<StockDaily> stockDaily;
    private CombinedChart combinedChart;
    private GetStockDetails details;
    private LineDataSet lineDataSet;
    private View mView, btnView, mProgressView;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private ProgressDialog pDialog;
    private DatabaseHandler db;
    private DecimalFormat decimalFormat;
    private TextView ltp, chg, chg_p, pcls, date_tv, volume, stock_name, historical, intraday;
    private TextView exchg, mcap, o, h, l, c, lp, ot, ht, lt, ct, lpt, change, change_p, vol, avg_vol, pc, pv, bid, ask, bid_sz, ask_sz, pe, ytd, wh52, wl52;
    private ImageButton fullChart;
    private float scaleX = 2f;
    private SnackBarDisplay snackBarDisplay;
    private FbLogAdEvents fbLogAdEvents;

    public StockFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StockFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StockFragment newInstance(String param1, String param2) {
        StockFragment fragment = new StockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        session = new SessionManager((Context) mListener);
        barEntries = new ArrayList<BarEntry>();
        entries = new ArrayList<Entry>();
        stockDaily = new ArrayList<>();
        details = new GetStockDetails();
        pDialog = new ProgressDialog((Context) mListener);
        db = new DatabaseHandler((Context) mListener);
        decimalFormat = new DecimalFormat("###,###,###,###.##");
        snackBarDisplay = new SnackBarDisplay((Context) mListener);
        fbLogAdEvents = new FbLogAdEvents((Context) mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View view;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stock, container, false);
        mView = view ;

        mProgressView = view.findViewById(R.id.news_progress);

        MobileAds.initialize(view.getContext(), adUnitId);
        mAdView = (AdView) view.findViewById(R.id.adView);
        if(!session.isPremiumUser()) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(TAG,"onAdLoaded");
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("stockframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("stockframe_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
//        mAdView = (AdView) view.findViewById(R.id.adView);
//        if(!session.isPremiumUser()) {
//            mInterstitialAd = new InterstitialAd((Context) mListener);
//            mInterstitialAd.setAdUnitId(STOCK_FRAGMENT_INTER_AD_UNIT_ID);
//            mInterstitialAd.loadAd(new AdRequest.Builder().build());
//
//            mInterstitialAd.setAdListener(new AdListener() {
//
//                @Override
//                public void onAdOpened() {
//                    super.onAdOpened();
//                    Log.e(TAG,"onAdOpened: stockframe_interstitial");
//                    fbLogAdEvents.logAdImpressionEvent("stockframe_interstitial");
//                }
//
//                @Override
//                public void onAdLeftApplication() {
//                    super.onAdLeftApplication();
//                    Log.e(TAG,"onAdLeftApplication: stockframe_interstitial");
//                    fbLogAdEvents.logAdClickEvent("stockframe_interstitial");
//                }
//
//                @Override
//                public void onAdClosed() {
//                    // Increment launch counter
//                    long launch_count = session.getPurchLaunchCount() + 1;
//                    session.setPurchLaunchCount(launch_count);
//
//                    // Get date of first launch
//                    Long date_firstLaunch = session.getPurchDateFirstLaunch();
//                    if (date_firstLaunch == 0) {
//                        date_firstLaunch = System.currentTimeMillis();
//                        session.setPurchDateFirstLaunch(date_firstLaunch);
//                    }
//
//                    appLaunchTimeAndInterstitialAd = System.currentTimeMillis();

                    // Load the next interstitial.
//                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
//
//                    loadSelectedFragment(btnView);

                    //        final int random = new Random().nextInt((max - min) + 1) + min;
//                    final int random = new Random().nextInt((55 - 30) + 1) + 25;
//
//                    if(!session.isPurchDontShowAgain() &&
//                            session.getPurchLaunchCount() >= LAUNCHES_UNTIL_PROMPT &&
//                                System.currentTimeMillis() >= session.getPurchDateFirstLaunch() +
//                                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
//                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        //Do something here
//                                        mListener.onFragmentInteractionPurchPopUp();
//                                    }
//                                }, 1000 * random);
//                            }
//                }
//            });

            /*mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("stockframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("stockframe_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);*/
//        }

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

// will either be DENSITY_LOW, DENSITY_MEDIUM or DENSITY_HIGH
        int dpiClassification = dm.densityDpi;

// these will return the actual dpi horizontally and vertically
        float xDpi = dm.xdpi;
        float yDpi = dm.ydpi;

        Log.e(TAG, String.valueOf(xDpi));
        Log.e(TAG, String.valueOf(yDpi));

        combinedChart = (CombinedChart) view.findViewById(R.id.combined_chart);
        FrameLayout graphFrame = (FrameLayout) view.findViewById(R.id.graph_frame);
        graphFrame.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,Math.round(yDpi)*2));

        view.findViewById(R.id.intra_day_analysis).setOnClickListener(this::onClick);
        view.findViewById(R.id.historical_analysis).setOnClickListener(this::onClick);
        view.findViewById(R.id.full_chart).setOnClickListener(this::onClick);

        ltp = (TextView) view.findViewById(R.id.ltp);
        chg = (TextView) view.findViewById(R.id.chg);
        chg_p = (TextView) view.findViewById(R.id.chg_p);
        pcls = (TextView) view.findViewById(R.id.pcls);
        date_tv = (TextView) view.findViewById(R.id.date);
        volume = (TextView) view.findViewById(R.id.vol);
        stock_name = (TextView) view.findViewById(R.id.stock_name);
        historical = (TextView) view.findViewById(R.id.historical_analysis);
        intraday = (TextView) view.findViewById(R.id.intra_day_analysis);

        exchg	= (TextView) view.findViewById(R.id.primaryExchange);
        mcap    = (TextView) view.findViewById(R.id.marketCap);
        o       = (TextView) view.findViewById(R.id.open);
        h       = (TextView) view.findViewById(R.id.high);
        l       = (TextView) view.findViewById(R.id.low);
        c       = (TextView) view.findViewById(R.id.close);
        lp      = (TextView) view.findViewById(R.id.latestPrice);
        ot      = (TextView) view.findViewById(R.id.openTime);
        ht      = (TextView) view.findViewById(R.id.highTime);
        lt      = (TextView) view.findViewById(R.id.lowTime);
        ct      = (TextView) view.findViewById(R.id.closeTime);
        lpt     = (TextView) view.findViewById(R.id.latestTime);
        change  = (TextView) view.findViewById(R.id.change);
        change_p = (TextView) view.findViewById(R.id.changePercent);
        vol     = (TextView) view.findViewById(R.id.volume);
        avg_vol = (TextView) view.findViewById(R.id.avgTotalVolume);
        pc      = (TextView) view.findViewById(R.id.previousClose);
        pv      = (TextView) view.findViewById(R.id.previousVolume);
        bid     = (TextView) view.findViewById(R.id.iexBidPrice);
        ask     = (TextView) view.findViewById(R.id.iexAskPrice);
        bid_sz  = (TextView) view.findViewById(R.id.iexBidSize);
        ask_sz  = (TextView) view.findViewById(R.id.iexAskSize);
        pe      = (TextView) view.findViewById(R.id.peRatio);
        ytd     = (TextView) view.findViewById(R.id.ytdChange);
        wh52    = (TextView) view.findViewById(R.id.week52High);
        wl52   = (TextView) view.findViewById(R.id.week52Low);

        fullChart = view.findViewById(R.id.full_chart);

        StockDaily stockDaily = db.getSingleStockDetails(session.getStockName());
        stock_name.setText(stockDaily.getName());

        if(session.getTheme() == 0){
            historical.setBackground(getResources().getDrawable(R.drawable.button_bg_selector_dark));
            intraday.setBackground(getResources().getDrawable(R.drawable.button_bg_selector_dark));
        }

        pDialog.setMessage("    Please Wait...");
        pDialog.setCancelable(false);

        showpDialog();
        setCombinedChartParams();
        displayCombinedChart();

        return view;
    }

    private void displayStockDetails() {
        String url = String.format(URL_GET_NSQ_STOCK_QUOTE, BASE_URL, session.getStockName(), PROD_TOKEN);
        Log.e(TAG,url);
        String tag_string_req = "tag_stock_details";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                (JSONObject response) -> {
                    Log.e(TAG, String.valueOf(response));
                    String exchg, mcap, o, h, l, c, lp, ot, ht, lt, ct, lpt, change, change_p, vol, avg_vol, pc, pv, bid, ask, bid_sz, ask_sz, pe, ytd, wh52, wl52;
                        try {

                            exchg	  = String.valueOf((response.get("primaryExchange").equals(null)) ? "-" : response.get("primaryExchange"));
                            mcap      = String.valueOf((response.get("marketCap").equals(null)) ? "-" : response.get("marketCap"));
                            o         = String.valueOf((response.get("open").equals(null)) ? "-" : response.get("open"));
                            h         = String.valueOf((response.get("high").equals(null)) ? "-" : response.get("high"));
                            l         = String.valueOf((response.get("low").equals(null)) ? "-" : response.get("low"));
                            c         = String.valueOf((response.get("close").equals(null)) ? "-" : response.get("close"));
                            lp        = String.valueOf((response.get("latestPrice").equals(null)) ? "-" : response.get("latestPrice"));
                            ot        = String.valueOf((response.get("openTime").equals(null)) ? "-" : response.get("openTime"));
                            ht        = String.valueOf((response.get("highTime").equals(null)) ? "-" : response.get("highTime"));
                            lt        = String.valueOf((response.get("lowTime").equals(null)) ? "-" : response.get("lowTime"));
                            ct        = String.valueOf((response.get("closeTime").equals(null)) ? "-" : response.get("closeTime"));
                            lpt       = String.valueOf((response.get("latestTime").equals(null)) ? "-" : response.get("latestTime"));
                            change    = String.valueOf((response.get("change").equals(null)) ? "-" : response.get("change"));
                            change_p  = String.valueOf((response.get("changePercent").equals(null)) ? "-" : response.get("changePercent"));
                            vol       = String.valueOf((response.get("volume").equals(null)) ? "-" : response.get("volume"));
                            avg_vol   = String.valueOf((response.get("avgTotalVolume").equals(null)) ? "-" : response.get("avgTotalVolume"));
                            pc        = String.valueOf((response.get("previousClose").equals(null)) ? "-" : response.get("previousClose"));
                            pv        = String.valueOf((response.get("previousVolume").equals(null)) ? "-" : response.get("previousVolume"));
                            bid       = String.valueOf((response.get("iexBidPrice").equals(null)) ? "-" : response.get("iexBidPrice"));
                            ask       = String.valueOf((response.get("iexAskPrice").equals(null)) ? "-" : response.get("iexAskPrice"));
                            bid_sz    = String.valueOf((response.get("iexBidSize").equals(null)) ? "-" : response.get("iexBidSize"));
                            ask_sz    = String.valueOf((response.get("iexAskSize").equals(null)) ? "-" : response.get("iexAskSize"));
                            pe        = String.valueOf((response.get("peRatio").equals(null)) ? "-" : response.get("peRatio"));
                            ytd       = String.valueOf((response.get("ytdChange").equals(null)) ? "-" : response.get("ytdChange"));
                            wh52      = String.valueOf((response.get("week52High").equals(null)) ? "-" : response.get("week52High"));
                            wl52      = String.valueOf((response.get("week52Low").equals(null)) ? "-" : response.get("week52Low"));

                            exchg = (exchg.equals("New York Stock Exchange")) ? "NYSE" : exchg;
                            if(!ytd.equals("-"))
                                ytd = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(ytd) * 100) + "%";
                            if(!change_p.equals("-"))
                                change_p = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(change_p) * 100) + "%";
                            if(!mcap.equals("-"))  mcap = formatValue(Float.parseFloat(mcap));
                            if(!vol.equals("-")) vol = formatValue(Float.parseFloat(vol));
                            if(!avg_vol.equals("-")) avg_vol = formatValue(Float.parseFloat(avg_vol));
                            if(!pv.equals("-")) pv = formatValue(Float.parseFloat(pv));
                            if(!bid_sz.equals("-")) bid_sz = formatValue(Float.parseFloat(bid_sz));
                            if(!ask_sz.equals("-")) ask_sz = formatValue(Float.parseFloat(ask_sz));

                            if(!ot.equals("-")) ot = getTime(ot);
                            if(!ht.equals("-")) ht = getTime(ht);
                            if(!lt.equals("-")) lt = getTime(lt);
                            if(!ct.equals("-")) ct = getTime(ct);

                                this.exchg.setText(exchg);
                                this.mcap.setText(mcap);
                                this.o.setText(o);
                                this.h.setText(h);
                                this.l.setText(l);
                                this.c.setText(c);
                                this.lp.setText(lp);
                                this.ot.setText(ot);
                                this.ht.setText(ht);
                                this.lt.setText(lt);
                                this.ct.setText(ct);
                                this.lpt.setText(lpt);
                                this.change.setText(change);
                                this.change_p.setText(change_p);
                                this.vol.setText(vol);
                                this.avg_vol.setText(avg_vol);
                                this.pc.setText(pc);
                                this.pv.setText(pv);
                                this.bid.setText(bid);
                                this.ask.setText(ask);
                                this.bid_sz.setText(bid_sz);
                                this.ask_sz.setText(ask_sz);
                                this.pe.setText(pe);
                                this.ytd.setText(ytd);
                                this.wh52.setText(wh52);
                                this.wl52.setText(wl52);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }, volleyError -> {
            VolleyLog.d(TAG, "Volley Error: " + volleyError.getMessage());
            snackBarDisplay.volleyErrorOccurred(TAG, mView, volleyError);
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_string_req);
    }

    private String getTime(String time) {
        long seconds = Long.parseLong(time);
        Date date = new Date(seconds);
        SimpleDateFormat sdf = new SimpleDateFormat("d EEE, h:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        time = sdf.format(date).replace("am", "AM").replace("pm","PM");
        return time;
    }

    //    Display large numbers in K, M B (Thousand, million and billions)
    public String formatValue(float value) {
        String arr[] = {"", "K", "M", "B", "T", "P", "E"};
        int index = 0;
        while ((value / 1000) >= 1) {
            value = value / 1000;
            index++;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return String.format("%s%s", decimalFormat.format(value), arr[index]);
    }

    public void onRefresh(){
        showpDialog();
        if(combinedChart!=null){
            entries.clear();
            barEntries.clear();
            ViewPortHandler viewPortHandler = combinedChart.getViewPortHandler();
            scaleX = viewPortHandler.getScaleX();
            combinedChart.fitScreen();
            combinedChart.invalidate();
            combinedChart.clear();
        }
        setCombinedChartParams();
        displayCombinedChart();
    }

    private void setCombinedChartParams() {
        combinedChart.setDrawGridBackground(false);
        combinedChart.getLegend().setEnabled(false);
        if (session.getTheme() == 0){
            combinedChart.getLegend().setTextColor(getResources().getColor(R.color.stock_same_dark));
            combinedChart.getXAxis().setTextColor(getResources().getColor(R.color.stock_same_dark));
            combinedChart.getAxisRight().setTextColor(getResources().getColor(R.color.stock_same_dark));
            combinedChart.getAxisLeft().setTextColor(getResources().getColor(R.color.stock_same_dark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                fullChart.setBackground(getResources().getDrawable(R.drawable.ic_zoom_out_map_layer_list_dark_24dp));
            }
        }else if(session.getTheme() == 1){
            combinedChart.getLegend().setTextColor(getResources().getColor(R.color.stock_same));
            combinedChart.getXAxis().setTextColor(getResources().getColor(R.color.stock_same));
            combinedChart.getAxisRight().setTextColor(getResources().getColor(R.color.stock_same));
            combinedChart.getAxisLeft().setTextColor(getResources().getColor(R.color.stock_same));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                fullChart.setBackground(getResources().getDrawable(R.drawable.ic_zoom_out_map_layer_list_light_24dp));
            }
        }
        combinedChart.animateX(1500);
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            String date;
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
//                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
//                Date date1 = null;
                if (stockDaily.size() >= 0 && value >= 0) {
                    if (value < stockDaily.size()) {
                        date = stockDaily.get((int) value).getDate();
//                        try {
//                            date1 = dt.parse(date);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        SimpleDateFormat dt1;
//                        dt1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
//                        date = dt1.format(date1);
                    }
                }
                return date;
            }
        });
        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return new DecimalFormat("#.##").format(value);
            }
        });
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setValueFormatter(new LargeValueFormatter(){
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return super.getFormattedValue(value, entry, dataSetIndex, viewPortHandler);
            }
        });
        // draw bars behind lines
        combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });
        Legend l = combinedChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        combinedChart.getDescription().setText(session.getStockName() + " Dynamic Line Chart");
        if (session.getTheme() == 0)
            combinedChart.getDescription().setTextColor(getResources().getColor(R.color.stock_same_dark));
        else if(session.getTheme() == 1)
            combinedChart.getDescription().setTextColor(getResources().getColor(R.color.stock_same));
    }

    private void displayCombinedChart(){

        String url = String.format(URL_GET_NSQ_STOCK_1DM_CHART_DETAILS, BASE_URL, session.getStockName(), PROD_TOKEN);

        Log.e(TAG,url);
        String tag_string_req = "tag_stock_details";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                (JSONObject response) -> {
                    float close, vol;
                    String intraPcls = db.getStockPcls(session.getStockName());
                    Log.e(TAG, "intraPcls: "+intraPcls);
                    stockDaily.clear();
                    Iterator<?> keys = response.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        try {
                            if (response.get(key) instanceof JSONArray) {
                                stockDaily = details.getStockIntradayDetails((JSONArray) response.get(key), intraPcls, "");

                                for (int x = 0; x < stockDaily.size(); x++ ){
                                    close = Float.parseFloat(stockDaily.get(x).getCls());
                                    vol = Float.parseFloat(stockDaily.get(x).getVol());
                                    entries.add(new Entry(x,close));
                                    barEntries.add(new BarEntry(x,vol));
                                }

//************************************* Start Set Line Chart Data *************************************************************************************
                                lineDataSet = new LineDataSet(entries,"Line Chart");
                                lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//                    lineDataSet.setCircleRadius(3f);
                                lineDataSet.setLineWidth(2f);
                                lineDataSet.setValueTextSize(7.5f);
                                lineDataSet.setDrawFilled(true);
                                lineDataSet.setDrawCircles(false);

                                if (session.getTheme() == 0){
                                    lineDataSet.setColor(getResources().getColor(R.color.line_chart_dark));
                                    lineDataSet.setValueTextColor(getResources().getColor(R.color.stock_same_dark));
                                    lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_fill_dark));

                                }else if(session.getTheme() == 1){
//                        lineDataSet.setValueTextColor(getResources().getColor(R.color.stock_same));
                                    lineDataSet.setColor(getResources().getColor(R.color.line_chart));
                                    lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_fill));
                                }
                                LineData lineData = new LineData(lineDataSet);

                                lineData.setValueFormatter(new IValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                        return new DecimalFormat("#.##").format(value);
                                    }
                                });
//************************************* End Set Line Chart Data *************************************************************************************

                                BarDataSet barDataSet = new BarDataSet(barEntries,"Bar Chart (Vol)");

                                if (session.getTheme() == 0){
                                    barDataSet.setValueTextColor(getResources().getColor(R.color.stock_same_dark));
                                    barDataSet.setColor(getResources().getColor(R.color.stock_date_dark));
                                }else if(session.getTheme() == 1){
                                    barDataSet.setValueTextColor(getResources().getColor(R.color.stock_same));
                                    barDataSet.setColor(getResources().getColor(R.color.stock_date));
                                }
                                barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                                barDataSet.setDrawValues(false);

                                BarData barData = new BarData(barDataSet);

                                float lineDataYMin = lineData.getYMin();

                                // to make bar appears at bottom
                                YAxis leftAxis = combinedChart.getAxisLeft();
//                    leftAxis.setAxisMaximum(barYMax * 2);

                                // to make line appears at top
                                YAxis rightAxis = combinedChart.getAxisRight();
                                rightAxis.setAxisMinimum(lineDataYMin * 0.99f);

                                XAxis xAxis = combinedChart.getXAxis();
                                xAxis.setSpaceMin(barData.getBarWidth() / 2f);
                                xAxis.setSpaceMax(barData.getBarWidth() / 2f);

                                CombinedData combinedData = new CombinedData();

                                combinedData.setData(lineData);
                                combinedData.setData(barData);
                                combinedChart.setData(combinedData);
                                combinedChart.zoom(scaleX,0,entries.size() - 1,0,rightAxis.getAxisDependency() );

                                combinedChart.notifyDataSetChanged();
                                combinedChart.invalidate();
                                setStockTextView(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        }
                    displayStockDetails();
                }, volleyError -> {
                    VolleyLog.d(TAG, "Volley Error: " + volleyError.getMessage());
                    setStockTextView(true);
                    snackBarDisplay.volleyErrorOccurred(TAG, mView, volleyError);
                });

/*        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, jsonArray -> {
            if (jsonArray.length() > 0) {

                Log.e(TAG, "jsonArray: "+String.valueOf(jsonArray));
                float close, vol;
                String intraPcls = db.getStockPcls(session.getStockName());
                stockDaily.clear();

                stockDaily = details.getStockIntradayDetails(jsonArray, intraPcls, "");

//                Collections.reverse(stockDaily);

                for (int x = 0; x < stockDaily.size(); x++ ){
                    close = Float.parseFloat(stockDaily.get(x).getCls());
                    vol = Float.parseFloat(stockDaily.get(x).getVol());
                    entries.add(new Entry(x,close));
                    barEntries.add(new BarEntry(x,vol));
                }

//************************************* Start Set Line Chart Data *************************************************************************************
                lineDataSet = new LineDataSet(entries,"Line Chart");
                lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//                    lineDataSet.setCircleRadius(3f);
                lineDataSet.setLineWidth(2f);
                lineDataSet.setValueTextSize(7.5f);
                lineDataSet.setDrawFilled(true);
                lineDataSet.setDrawCircles(false);

                if (session.getTheme() == 0){
                    lineDataSet.setColor(getResources().getColor(R.color.line_chart_dark));
                    lineDataSet.setValueTextColor(getResources().getColor(R.color.stock_same_dark));
                    lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_fill_dark));

                }else if(session.getTheme() == 1){
//                        lineDataSet.setValueTextColor(getResources().getColor(R.color.stock_same));
                    lineDataSet.setColor(getResources().getColor(R.color.line_chart));
                    lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_fill));
                }
                LineData lineData = new LineData(lineDataSet);

                lineData.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return new DecimalFormat("#.##").format(value);
                    }
                });
//************************************* End Set Line Chart Data *************************************************************************************

                BarDataSet barDataSet = new BarDataSet(barEntries,"Bar Chart (Vol)");

                if (session.getTheme() == 0){
                    barDataSet.setValueTextColor(getResources().getColor(R.color.stock_same_dark));
                    barDataSet.setColor(getResources().getColor(R.color.stock_date_dark));
                }else if(session.getTheme() == 1){
                    barDataSet.setValueTextColor(getResources().getColor(R.color.stock_same));
                    barDataSet.setColor(getResources().getColor(R.color.stock_date));
                }
                barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                barDataSet.setDrawValues(false);

                BarData barData = new BarData(barDataSet);

                float lineDataYMin = lineData.getYMin();

                // to make bar appears at bottom
                YAxis leftAxis = combinedChart.getAxisLeft();
//                    leftAxis.setAxisMaximum(barYMax * 2);

                // to make line appears at top
                YAxis rightAxis = combinedChart.getAxisRight();
                rightAxis.setAxisMinimum(lineDataYMin * 0.99f);

                XAxis xAxis = combinedChart.getXAxis();
                xAxis.setSpaceMin(barData.getBarWidth() / 2f);
                xAxis.setSpaceMax(barData.getBarWidth() / 2f);

                CombinedData combinedData = new CombinedData();

                combinedData.setData(lineData);
                combinedData.setData(barData);
                combinedChart.setData(combinedData);
                combinedChart.zoom(scaleX,0,entries.size() - 1,0,rightAxis.getAxisDependency() );

                combinedChart.notifyDataSetChanged();
                combinedChart.invalidate();
                setStockTextView(false);
            }
        },volleyError -> {
            VolleyLog.d(TAG, "Volley Error: " + volleyError.getMessage());
            setStockTextView(true);
            showSnackBar(mView ,getResources().getString(R.string.msg_check_internet));
        });*/

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_string_req);
    }

    private void setStockTextView(Boolean error) {
        int same = 0, date = 0, rise = 0, fall = 0;
        if(session.getTheme() == 0){
            same = getResources().getColor(R.color.stock_same_dark);
            date = getResources().getColor(R.color.stock_date_dark);
            rise = getResources().getColor(R.color.stock_rise_dark);
            fall = getResources().getColor(R.color.stock_fall_dark);
        } else if (session.getTheme() == 1){
            same = getResources().getColor(R.color.stock_same);
            date = getResources().getColor(R.color.stock_date);
            rise = getResources().getColor(R.color.stock_rise);
            fall = getResources().getColor(R.color.stock_fall);
        }

        Log.e(TAG, String.valueOf(stockDaily.size()));

        float fNo;
        try{
            if (!error)
                fNo = Float.parseFloat(stockDaily.get(stockDaily.size() - 1).getChg());
            else {
                StockDaily stockDaily = db.getSingleStockDetails(session.getStockName());
                fNo = Float.parseFloat(stockDaily.getChg());
            }
        }catch(NullPointerException e){
            fNo = (float) 0.0;
        }

        if (fNo == 0 && 1/fNo > 0){
            //Black color
            ltp.setTextColor(same);
            chg.setTextColor(same);
            chg_p.setTextColor(same);
            pcls.setTextColor(same);
            chg.setBackgroundColor(Color.TRANSPARENT);
            chg_p.setBackgroundColor(Color.TRANSPARENT);
        }else if(1/fNo < 0){
            //Red color
            ltp.setTextColor(fall);
            chg.setTextColor(fall);
            chg_p.setTextColor(fall);
//            chg.setTextColor(Color.WHITE);
//            chg_p.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if(session.getTheme() == 0) {
                    chg.setBackground(getResources().getDrawable(R.drawable.stock_fall_left_bg_dark));
                    chg_p.setBackground(getResources().getDrawable(R.drawable.stock_fall_right_bg_dark));
                } else {
                    chg.setBackground(getResources().getDrawable(R.drawable.stock_fall_left_bg));
                    chg_p.setBackground(getResources().getDrawable(R.drawable.stock_fall_right_bg));
                }
            }else {
                chg.setBackgroundColor(fall);
                chg_p.setBackgroundColor(fall);
            }
            pcls.setTextColor(fall);
        }
        else{
            //Green color
            ltp.setTextColor(rise);
            chg.setTextColor(rise);
            chg_p.setTextColor(rise);
//            chg.setTextColor(Color.parseColor("#37474f"));
//            chg_p.setTextColor(Color.parseColor("#37474f"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if(session.getTheme() == 0) {
                    chg.setBackground(getResources().getDrawable(R.drawable.stock_rise_left_bg_dark));
                    chg_p.setBackground(getResources().getDrawable(R.drawable.stock_rise_right_bg_dark));
                } else {
                    chg.setBackground(getResources().getDrawable(R.drawable.stock_rise_left_bg));
                    chg_p.setBackground(getResources().getDrawable(R.drawable.stock_rise_right_bg));
                }
            }else {
                chg.setBackgroundColor(Color.parseColor("#76ff03"));
                chg_p.setBackgroundColor(Color.parseColor("#76ff03"));
            }
            pcls.setTextColor(rise);
        }

        date_tv.setTextColor(date);

        if (!error){
            ltp.setText(stockDaily.get(stockDaily.size() - 1).getCls());
            chg.setText(stockDaily.get(stockDaily.size() - 1).getChg());
            chg_p.setText(stockDaily.get(stockDaily.size() - 1).getChg_p());
            pcls.setText(stockDaily.get(stockDaily.size() - 1).getPcls());
            date_tv.setText(stockDaily.get(stockDaily.size() - 1).getDate());
            volume.setText("Vol: "+decimalFormat.format(Float.parseFloat(stockDaily.get(stockDaily.size() - 1).getVol())));

//            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
//            Date date1 = null;
//            try {
//                date1 = dt.parse(stockDaily.get(stockDaily.size() - 1).getDate());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            String date2 = new SimpleDateFormat("yyyy MMM dd, HH:mm:ss", Locale.ENGLISH).format(date1);
//            date_tv.setText(date2);
        } else {
            StockDaily stockDaily = db.getSingleStockDetails(session.getStockName());
            ltp.setText(stockDaily.getCls());
            chg.setText(stockDaily.getChg());
            chg_p.setText(stockDaily.getChg_p());
            pcls.setText(stockDaily.getPcls());
            if (stockDaily.getVol() != null && !stockDaily.getVol().equals("null"))
                volume.setText("Vol: "+decimalFormat.format(Float.parseFloat(stockDaily.getVol())));
            else
                volume.setText(null);
            date_tv.setText(stockDaily.getDate());
        }

        hidepDialog();
        if(animationDrawable!=null)
            animationDrawable.stop();
    }

    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String uri) {
        if (mListener != null) {
//            mListener.onMainFragmentUPInteraction(uri, null);
        }
    }*/

    @Override
    public void onClick(View v) {
        btnView = v;
        loadSelectedFragment(v);
        /*if (!session.isPremiumUser()) {
            if (mInterstitialAd.isLoaded()) {
                if (System.currentTimeMillis() >= appLaunchTimeAndInterstitialAd +
                        (SECONDS_UNTIL_INTERSTITIAL_PROMPT * 1000)) {
//                    mInterstitialAd.show();
                } else {
                    loadSelectedFragment(v);
                }
            } else {
                loadSelectedFragment(v);
            }
        } else {
            loadSelectedFragment(v);
        }*/
    }

    private void loadSelectedFragment(View v){
        switch (v.getId()) {
            case R.id.intra_day_analysis:
                selectIntradayPeriod();
                break;
            case R.id.full_chart:
                session.setStockPeriod("Chart");
                CURRENT_TAG = TAG_STOCK_CHART_FRAME;
                mListener.onStockFragmentInteraction(StockFragment.this);
                break;
            case R.id.historical_analysis:
                selectHistoricalPeriod();
        }
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
            CURRENT_TAG = TAG_STOCK_INTRADAY_FRAME;
            mListener.onStockFragmentInteraction(StockFragment.this);
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
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
            CURRENT_TAG = TAG_STOCK_HISTORICAL_FRAME;
            mListener.onStockFragmentInteraction(StockFragment.this);
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStockFragmentInteractionListener) {
            mListener = (OnStockFragmentInteractionListener) context;
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

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG,"onDestroyView()");
        super.onDestroyView();
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
                    fbLogAdEvents.logAdImpressionEvent("stockframe_banner");
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("stockframe_banner");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnStockFragmentInteractionListener {
        // TODO: Update argument type and name
        void onStockFragmentInteraction(Fragment stockFragment);
        void onFragmentInteractionPurchPopUp();
    }
}
