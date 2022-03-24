package info.ascetx.stockstalker.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import info.ascetx.stockstalker.MainActivity;
import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.adapter.StockNameListAdapter;
import info.ascetx.stockstalker.app.AddRemoveUserStock;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.GetStockDetails;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.dbhandler.LoginHandler;
import info.ascetx.stockstalker.helper.StockName;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.CURRENT_TAG;
import static info.ascetx.stockstalker.MainActivity.TAG_STOCK_INFO_FRAME;
import static info.ascetx.stockstalker.MainActivity.TAG_UPGRADE_PREMIUM_FRAME;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.MAIN_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_BATCH_STOCK_DETAILS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static String TAG = "MainFragment";
    private static String adUnitId = MAIN_FRAGMENT_AD_UNIT_ID;

    public AdView mAdView;
    public LinearLayout upgrade_premium;
    private TextView s_symbol, s_ltp, s_chg, s_chg_p;
    private ListView listView;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<StockName> stockNameDate;
    private StockNameListAdapter adapter;
    private DatabaseHandler db;
    private static LoginHandler dbl;
    private SessionManager session;
    private GetStockDetails details;
    private View mView;
    private ScheduledExecutorService executorService;
    private FloatingActionButton fab;
    private SnackBarDisplay snackBarDisplay;
    private AddRemoveUserStock addRemoveUserStock;
    private FbLogAdEvents fbLogAdEvents;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        pDialog = new ProgressDialog((Context) mListener);
        stockNameDate = new ArrayList<>();
        adapter = new StockNameListAdapter((Activity) mListener, stockNameDate);
        db = new DatabaseHandler((Context) mListener);
        dbl = new LoginHandler((Context) mListener);
        session = new SessionManager((Context) mListener);
        details = new GetStockDetails();
        snackBarDisplay = new SnackBarDisplay((Context) mListener);
        addRemoveUserStock = new AddRemoveUserStock((Context) mListener);
        fbLogAdEvents = new FbLogAdEvents((Context) mListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mView = view;

        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        MobileAds.initialize(view.getContext(), adUnitId);
        mAdView = (AdView) view.findViewById(R.id.adView);
        if(!session.isPremiumUser()) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(TAG,"onAdLoaded");
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("mainframe_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("mainframe_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        s_symbol = (TextView) view.findViewById(R.id.sort_stock_name);
        s_ltp = (TextView) view.findViewById(R.id.sort_ltp);
        s_chg = (TextView) view.findViewById(R.id.sort_chg);
        s_chg_p = (TextView) view.findViewById(R.id.sort_chg_p);
        upgrade_premium = view.findViewById(R.id.upgrade_premium);

        if (session.isPremiumUser()) {
            upgrade_premium.setVisibility(View.GONE);
        } else {
            upgrade_premium.setVisibility(View.VISIBLE);
        }

        listView = (ListView) view.findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView stock = (TextView) view.findViewById(R.id.stock_name);
                session.setStockName(stock.getText().toString());
                session.setStockPeriod(null);
                Log.e(TAG,"stock value: "+stock.getText().toString());
                if (!stock.getText().toString().equals("")) {
                    CURRENT_TAG = TAG_STOCK_INFO_FRAME;
                    mListener.onFragmentInteraction();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView stock = (TextView) view.findViewById(R.id.stock_name);
                session.setStockName(stock.getText().toString());
                if (!stock.getText().toString().equals(""))
                    removeStockAlertDialog(mView);
                return true;
            }
        });

        upgrade_premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_TAG = TAG_UPGRADE_PREMIUM_FRAME;
                mListener.onMainFragmentUPInteraction(MainFragment.this);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentFabClick(view);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        updateStocksDetailsAppDB();
                                    }
                                }
        );

//        if (session.isFirstLoginReg()) {
//////            Log.e(TAG,"First time");
//            updateUserAppDB();
//        }else {
//////            Log.e(TAG,"NOT First time");
//            updateStockList(); // Commented this as it cause NPE on first install and onCreate
//        }

//        List<StockName> stockName = db.getAllStockNames((Context) mListener);
//        if (stockName.get(0).getLtp() != null)
//            updateStockList();

        toggleSpinner();

        s_symbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockNameDate.clear();
                session.setToggle(!session.isToggle());
                session.setSortBy("nsq_stock_id");
                updateStockList();
                toggleSpinner();
            }
        });
        s_ltp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockNameDate.clear();
                session.setToggle(!session.isToggle());
                session.setSortBy("ltp");
                updateStockList();
                toggleSpinner();
            }
        });
        s_chg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockNameDate.clear();
                session.setToggle(!session.isToggle());
                session.setSortBy("chg");
                updateStockList();
                toggleSpinner();
            }
        });
        s_chg_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockNameDate.clear();
                session.setToggle(!session.isToggle());
                session.setSortBy("chg_p");
                updateStockList();
                toggleSpinner();
            }
        });

        return view;
    }

    private void toggleSpinner() {
        TextView spinnerTV = null;
        TextView[] spinnerTVArray = new TextView[]{s_symbol, s_ltp, s_chg, s_chg_p};
        switch (session.isSortBy()) {
            case "nsq_stock_id":
                spinnerTV = s_symbol;
                break;
            case "ltp":
                spinnerTV = s_ltp;
                break;
            case "chg":
                spinnerTV = s_chg;
                break;
            case "chg_p":
                spinnerTV = s_chg_p;
                break;
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (session.isToggle()) {
                spinnerTV.setBackgroundResource(R.drawable.spinner_background_material_border_up);
            } else {
                spinnerTV.setBackgroundResource(R.drawable.spinner_background_material_border_down);
            }

            for (TextView spinTV : spinnerTVArray)
                if (spinTV != spinnerTV) {
                    spinTV.setBackgroundResource(R.drawable.spinner_background_material_down);
                }
        }
    }

    private void removeStockAlertDialog(View view) {
        String stockName = session.getStockName();
        /* Alert Dialog Code Start*/
        AlertDialog.Builder alert = new AlertDialog.Builder((Context) mListener);
        alert.setTitle("Remove "+stockName); //Set Alert dialog title here
        alert.setMessage("Do you want to remove "+stockName+" from your stock stalking list?"); //Message here

        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //You will get as string input data in this variable.
                // here we convert the input to a string and show in a toast.

//                Toast.makeText(MainActivity.this, message+" was removed successfully.", Toast.LENGTH_LONG).show();
//                if(!session.isSkip())
                removeStockAppDb(stockName, view);
//                    removeStock(view);
//                else
//                    skipAlert(getResources().getString(R.string.skip_remove));

            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
       /* Alert Dialog Code End*/
    }

    private void removeStockAppDb(String stock, View view) {
        pDialog.setMessage("Removing...");
        pDialog.setCancelable(false);
        showpDialog();
        if(!session.isSkip()) {
            HashMap<String, String> user = dbl.getUserDetails();
            try {
                Log.e(TAG, user.get("name"));
                Log.e(TAG, user.get("email"));
                String email = user.get("email");
                addRemoveUserStock.arUserStock(email, stock, "name", "remove");
            } catch (NullPointerException e){
                Log.e(TAG, String.valueOf(e));
            }
        } else {
            addRemoveStock(stock, "name", false);
        }
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
            updateStocksDetailsAppDB();
        }
    }

    private void updateStocksDetailsAppDB(){
        StringBuilder params = new StringBuilder();
        String url;
        boolean isStringIndexOutOfBoundsException = false;

        // Tag used to cancel the request
        String tag_string_req = "tag_updateUserAppDB";
        // Fetching user details from sqlite
        List<StockName> stockName = db.getAllStockNames((Context) mListener);

        for (StockName sn : stockName){
            params.append(sn.getStock()).append(",");
        }
        try {
            params = new StringBuilder(params.substring(0, params.length() - 1));
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            hidepDialog();
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
            isStringIndexOutOfBoundsException = true;
            showSnackBar(getResources().getString(R.string.msg_add_stock));
            updateStockList();
        }
        try {
            params = new StringBuilder(URLEncoder.encode(params.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "params: " + params);
        url = String.format(URL_GET_NSQ_BATCH_STOCK_DETAILS, BASE_URL, params.toString(), PROD_TOKEN);
        Log.e(TAG, "url: " + url);

        if (!isStringIndexOutOfBoundsException) {

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                    (JSONObject response) -> {
                        String ltp = null, chg = null, chgp = null, pcls = null, vol = null, ts = null;
                        JSONObject quoteJsonObj;
//                Log.e(TAG, String.valueOf(response));

                        Iterator<?> keys = response.keys();

                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            try {
                                if (response.get(key) instanceof JSONObject) {
//                            Log.e(TAG, key);
//                            Log.e(TAG, String.valueOf(response.get(key)));
                                    quoteJsonObj = (JSONObject) ((JSONObject) response.get(key)).get("quote");
                                    ltp = String.valueOf(quoteJsonObj.get("latestPrice"));
                                    chg = String.valueOf(quoteJsonObj.get("change"));
                                    chgp = String.valueOf(quoteJsonObj.get("changePercent"));
                                    pcls = String.valueOf(quoteJsonObj.get("previousClose"));
                                    ltp = ((!ltp.equals("null")) ? ltp : "0");
                                    chg = ((!chg.equals("null")) ? chg : "0");
                                    chgp = ((!chgp.equals("null")) ? chgp : "0");
                                    pcls = ((!pcls.equals("null")) ? pcls : "0");
                                    ltp = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(ltp));
                                    chg = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(chg));
                                    chgp = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(chgp) * 100);
                                    pcls = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(pcls));
                                    vol = String.valueOf(quoteJsonObj.get("latestVolume"));
                                    ts = String.valueOf(quoteJsonObj.get("latestTime"));
                                }
                                db.updateStockDetails(key, ltp, chg, chgp, pcls, vol, ts);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        updateStockList();

                    }, error -> {
                logVolleyError(error);
                updateStockList();
                snackBarDisplay.volleyErrorOccurred(TAG, mView, error);
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_string_req);
        }
    }
    
    private void logVolleyError(VolleyError error){
        if (error == null || error.networkResponse == null) {
            return;
        }
        if(error instanceof NoConnectionError){
            ConnectivityManager cm = (ConnectivityManager) getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }
            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()){
                Log.e(TAG, "Server is not connected to internet.");
            } else {
                Log.e(TAG, "Your device is not connected to internet.");
            }
        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException
                || (error.getCause().getMessage() != null
                && error.getCause().getMessage().contains("connection"))){
            Log.e(TAG, "Your device is not connected to internet.");
        } else if (error.getCause() instanceof MalformedURLException){
            Log.e(TAG, "Bad Request.");
        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                || error.getCause() instanceof JSONException
                || error.getCause() instanceof XmlPullParserException){
            Log.e(TAG, "Parse Error (because of invalid json or xml).");
        } else if (error.getCause() instanceof OutOfMemoryError){
            Log.e(TAG, "Out Of Memory Error.");
        }else if (error instanceof AuthFailureError){
            Log.e(TAG, "Server couldn't find the authenticated request.");
        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
            Log.e(TAG, "Server is not responding.");
        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                || error.getCause() instanceof ConnectTimeoutException
                || error.getCause() instanceof SocketException
                || (error.getCause().getMessage() != null
                && error.getCause().getMessage().contains("Connection timed out"))) {
            Log.e(TAG, "Connection timeout error");
        } else {
            Log.e(TAG, "An unknown error occurred.");
        }
    }

    /*private void updateStocksDetailsAppDB() {
//        Log.e(TAG,"updateStocksDetailsAppDB");
        // Tag used to cancel the request
        String tag_string_req = "tag_updateUserAppDB";
        // Fetching user details from sqlite
        List<StockName> stockName = db.getAllStockNames((Context) mListener);

        String url = Config.URL_GET_NASDAQ_STOCKS_DETAILS + "q=" + stockName.get(stockNumber).getStock();
        String stk = stockName.get(stockNumber).getStock();
        Log.e(TAG, url);

        StringRequest strReq = new StringRequest(
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
//                Log.e(TAG, "User Stock Response: " + response);
                if (response.length() > 0 && !response.contains("<")) {
                    String pcls = "0", tempDate = "", ts;
                    Float cls, pclsf;
                    stockNumber ++;
                    List<StockDaily> stockDaily = new ArrayList<StockDaily>();
                    stockDaily = details.getStockDetailsList(response,pcls);

                    for (StockDaily sd : stockDaily ) {
                        if (!tempDate.equals("")){
                            if (!sd.getDate().substring(0, 10).equals(tempDate.substring(0, 10))) {
                                pcls = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(sd.getCls()));
                                System.out.println(stk+" "+ sd.getDate() + " " + sd.getCls() + " " + sd.getPcls() + " " + sd.getChg() + " " + sd.getChg_p());
                                break;
                            }
                        }
                        tempDate = sd.getDate();
                    }

                    ts = stockDaily.get(0).getDate();
                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
                    Date date = null;
                    try {
                        date = dt.parse(ts);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    SimpleDateFormat dt1 = new SimpleDateFormat("MMM dd, h:mm:ss", Locale.ENGLISH);
                    ts = dt1.format(date);

                    cls = Float.parseFloat(stockDaily.get(0).getCls());
                    pclsf = Float.parseFloat(pcls);

                    db.updateStockDetails(stk, String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(stockDaily.get(0).getCls())),
                            String.format(Locale.ENGLISH,"%.2f", cls - pclsf),String.format(Locale.ENGLISH,"%.2f", (((cls - pclsf) / pclsf) * 100)),
                            pcls, stockDaily.get(0).getVol(),ts);

                    if (stockNumber < stockName.size())
                        updateStocksDetailsAppDB();
                    if (stockNumber == stockName.size()){
                        updateStockList();
                        stockNumber = 0;
                    }
                } else {
                    updateStockList();
                    showSnackBar(mView ,getResources().getString(R.string.msg_switch_internet));
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley Error: " + error.getMessage());
                updateStockList();
                showSnackBar(mView ,getResources().getString(R.string.msg_check_internet));
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }*/

    private void updateStockList() {
        List<StockName> stockName = db.getAllStockNames((Context) mListener);
        if(stockName.size()!=0) {
            if (stockName.get(0).getPcls() != null && stockName.get(0).getLtp() != null) {
//            Log.e(TAG,"updateStockList in: " + stockName.get(0).getPcls());
                stockNameDate.clear();
                for (StockName m : stockName) {
                    stockNameDate.add(0, m);
                }
                stockNameDate.add(new StockName(null, null, null, null, null, null, null));
            }
        } else {
            Log.e(TAG,"No stocks in stalk list");
            stockNameDate.clear();
            adapter.notifyDataSetChanged();
        }

        if(stockNameDate != null) {
            adapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        hidepDialog();
        if(animationDrawable!=null)
            animationDrawable.stop();
    }

    public void addStock(final String stock, final String name, boolean tglm) {

        if(!db.hasStock(stock)) {
//            if (!tglm) {
                pDialog.setMessage("Adding...");
                pDialog.setCancelable(false);
                showpDialog();
//            }
            Log.e(TAG,"Skip user?: "+session.isSkip());
            if (!session.isSkip()) {
                HashMap<String, String> user = dbl.getUserDetails();
                try {
                    Log.e(TAG, user.get("name"));
                    Log.e(TAG, user.get("email"));
                    String email = user.get("email");
                    addRemoveUserStock.arUserStock(email, stock, name, "add");
                } catch (NullPointerException e) {
                    Log.e(TAG, String.valueOf(e));
                }
            } else {
                addRemoveStock(stock, name, true);
            }
        } else {
            showSnackBar(getResources().getString(R.string.err_msg_stock_already_exists));
        }
    }

    public void addRemoveStock(String stock, String name, boolean add){
        if(add)
            db.addStockName(stock, name);
        else
            db.deleteStockName(session.getStockName());

        updateStocksDetailsAppDB();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!session.isPremiumUser()) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mAdView.getLayoutParams();
            LinearLayout parent = (LinearLayout) mAdView.getParent();
//            Log.e("Parent: ", String.valueOf(parent));
//            Log.e("adView: ", String.valueOf(mAdView));
//            Log.e("LayoutParams: ", String.valueOf(lp));
            parent.removeView(mAdView);
            mAdView = new AdView(mView.getContext());
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId(adUnitId);
            mAdView.setLayoutParams(lp);
            parent.addView(mAdView);
            mAdView.setVisibility(View.GONE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    mAdView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStockFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdView.destroy();
        AppController.getInstance().getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
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
        startPeriodicRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        stopPeriodicRefresh();
    }

    private void startPeriodicRefresh(){
        executorService = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                updateStocksDetailsAppDB();
            }
        };
        executorService.scheduleAtFixedRate(task , 0, 30, TimeUnit.SECONDS);
    }
    private void stopPeriodicRefresh(){
        try {
            executorService.shutdown();
        } catch (NullPointerException e){
//            Log.e(TAG, String.valueOf(e));
        }
    }

    private void showSnackBar(String message){
        MainActivity mainActivity = (MainActivity) mListener;
        Snackbar snackbar = Snackbar.make(mainActivity.getWindow().getDecorView().findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).setAction("Action",null);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor((Context) mListener,R.color.snackbar_err_color));
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
    
   /* private void loadAd(AdView adView){
        if(!session.isPremiumUser()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    mAdView.setVisibility(View.GONE);
                }
            });
        }
    }*/

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name`
        void onFragmentInteraction();

        void onFragmentFabClick(View view);

        void onMainFragmentUPInteraction(MainFragment mainFragment);
    }
}
