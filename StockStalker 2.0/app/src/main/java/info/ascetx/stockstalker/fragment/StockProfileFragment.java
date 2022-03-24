package info.ascetx.stockstalker.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.FbLogAdEvents;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.STOCK_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.STOCK_PROFILE_FRAGMENT_AD_UNIT_ID;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_PROFILE;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_QUOTE;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StockProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String TAG = "StockProfileFragment";
    private static String adUnitId = STOCK_PROFILE_FRAGMENT_AD_UNIT_ID;

    private View mView;
    private AdView mAdView;
    private SessionManager session;
    private OnStockProfileFragmentInteractionListener mListener;
    private SnackBarDisplay snackBarDisplay;
    private FbLogAdEvents fbLogAdEvents;
    private TextView symbol, cname, exch, ind, web, desc, ceo, ist, sec, sic, emp, add, add2, state, city, zip, cntry, ph;
    private DecimalFormat decimalFormat;

    public StockProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StockProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StockProfileFragment newInstance(String param1, String param2) {
        StockProfileFragment fragment = new StockProfileFragment();
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
        decimalFormat = new DecimalFormat("###,###,###,###.##");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stock_profile, container, false);
        mView = view ;

        MobileAds.initialize(view.getContext(), adUnitId);
        mAdView = (AdView) view.findViewById(R.id.adView);
        if(!session.isPremiumUser()) {
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(TAG,"onAdLoaded");
                    mAdView.setVisibility(View.VISIBLE);
                    fbLogAdEvents.logAdImpressionEvent("stock_profile_frame_banner");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("stock_profile_frame_banner");
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        symbol = (TextView) view.findViewById(R.id.symbol);
        cname  = (TextView) view.findViewById(R.id.companyName);
        exch   = (TextView) view.findViewById(R.id.exchange);
        ind    = (TextView) view.findViewById(R.id.industry);
        web    = (TextView) view.findViewById(R.id.website);
        desc   = (TextView) view.findViewById(R.id.description);
        ceo    = (TextView) view.findViewById(R.id.CEO);
        ist    = (TextView) view.findViewById(R.id.issueType);
        sec    = (TextView) view.findViewById(R.id.sector);
        sic    = (TextView) view.findViewById(R.id.primarySicCode);
        emp    = (TextView) view.findViewById(R.id.employees);
        add    = (TextView) view.findViewById(R.id.address);
        add2   = (TextView) view.findViewById(R.id.address2);
        state  = (TextView) view.findViewById(R.id.state);
        city   = (TextView) view.findViewById(R.id.city);
        zip    = (TextView) view.findViewById(R.id.zip);
        cntry  = (TextView) view.findViewById(R.id.country);
        ph     = (TextView) view.findViewById(R.id.phone);

        displayStockProfile();

        return view;
    }

    private void displayStockProfile() {
        String url = String.format(URL_GET_NSQ_STOCK_PROFILE, BASE_URL, session.getStockName(), PROD_TOKEN);
        Log.e(TAG,url);
        String tag_string_req = "tag_stock_details";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                (JSONObject response) -> {
                    Log.e(TAG, String.valueOf(response));
                    String symbol, cname, exch, ind, web, desc, ceo, ist, sec, sic, emp, add, add2, state, city, zip, cntry, ph;
                    try {
                        symbol =  String.valueOf((response.get("symbol").equals(null))?"-":response.get("symbol"));
                        cname  =  String.valueOf((response.get("companyName").equals(null))?"-":response.get("companyName"));
                        exch   =  String.valueOf((response.get("exchange").equals(null))?"-":response.get("exchange"));
                        ind    =  String.valueOf((response.get("industry").equals(null))?"-":response.get("industry"));
                        web    =  String.valueOf((response.get("website").equals(null))?"-":response.get("website"));
                        desc   =  String.valueOf((response.get("description").equals(null))?"-":response.get("description"));
                        ceo    =  String.valueOf((response.get("CEO").equals(null))?"-":response.get("CEO"));
                        ist    =  String.valueOf((response.get("issueType").equals(null))?"-":response.get("issueType"));
                        sec    =  String.valueOf((response.get("sector").equals(null))?"-":response.get("sector"));
                        sic    =  String.valueOf((response.get("primarySicCode").equals(null))?"-":response.get("primarySicCode"));
                        emp    =  String.valueOf((response.get("employees").equals(null))?"-":response.get("employees"));
                        add    =  String.valueOf((response.get("address").equals(null))?"-":response.get("address"));
                        add2   =  String.valueOf((response.get("address2").equals(null))?"-":response.get("address2"));
                        state  =  String.valueOf((response.get("state").equals(null))?"-":response.get("state"));
                        city   =  String.valueOf((response.get("city").equals(null))?"-":response.get("city"));
                        zip    =  String.valueOf((response.get("zip").equals(null))?"-":response.get("zip"));
                        cntry  =  String.valueOf((response.get("country").equals(null))?"-":response.get("country"));
                        ph     =  String.valueOf((response.get("phone").equals(null))?"-":response.get("phone"));

                        ist = setIssueType(ist);

                        if (!emp.equals("-"))
                            emp = decimalFormat.format(Float.parseFloat(emp));

                        setStockDetails(this.symbol,"Symbol: ",symbol);
                        setStockDetails(this.cname,"Company: ",cname);
                        setStockDetails(this.exch,"Exchange: ",exch);
                        setStockDetails(this.ind,"Industry: ",ind);
                        setStockDetails(this.web,"Website: ",web);
                        setStockDetails(this.desc,"Description: ",desc);
                        setStockDetails(this.ceo,"CEO: ",ceo);
                        setStockDetails(this.ist,"Issue Type: ",ist);
                        setStockDetails(this.sec,"Sector: ",sec);
                        setStockDetails(this.sic,"SIC Code: ",sic);
                        setStockDetails(this.emp,"Employees: ",emp);
                        setStockDetails(this.add,"Address: ",add);
                        setStockDetails(this.add2,"Address2: ",add2);
                        setStockDetails(this.state,"State: ",state);
                        setStockDetails(this.city,"City: ",city);
                        setStockDetails(this.zip,"Zip: ",zip);
                        setStockDetails(this.cntry,"Country: ",cntry);
                        setStockDetails(this.ph,"Phone: ",ph);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    stopRefresh();
                }, volleyError -> {
            stopRefresh();
            VolleyLog.d(TAG, "Volley Error: " + volleyError.getMessage());
            snackBarDisplay.volleyErrorOccurred(TAG, mView, volleyError);
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_string_req);
    }

    private String setIssueType(String ist) {
        switch (ist){
            case "ad":
                ist = "American Depository Receipt (ADR’s)";
                break;
            case "re":
                ist = "Real Estate Investment Trust (REIT’s)";
                break;
            case "ce":
                ist = "Closed end fund (Stock and Bond Fund)";
                break;
            case "si":
                ist = "Secondary Issue";
                break;
            case "sp":
                ist = "Limited Partnerships";
                break;
            case "cs":
                ist = "Common Stock";
                break;
            case "et":
                ist = "Exchange Traded Fund (ETF)";
                break;
            case "wt":
                ist = "Warrant";
                break;
            case "rt":
                ist = "Right";
                break;
            case "":
                ist = "Not Available, i.e., Note, or (non-filing) Closed Ended Funds";
                break;
            case "ut":
                ist = "Unit";
                break;
            case "temp":
                ist = "Temporary";
                break;
            default:
                return ist;
        }
        return ist;
    }


    private void setStockDetails(TextView tv, String s, String detail) {
        // a SpannableStringBuilder containing text to display
        SpannableStringBuilder sb = new SpannableStringBuilder(s + detail);

// create a bold StyleSpan to be used on the SpannableStringBuilder
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold

// set only the name part of the SpannableStringBuilder to be bold
        sb.setSpan(b, 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold

        tv.setText(sb);
        Linkify.addLinks(tv, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
    }

    private String getTime(String time) {
        long seconds = Long.parseLong(time);
        Date date = new Date(seconds * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("d EEE, h:mma");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return sdf.format(date);
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
        displayStockProfile();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStockProfileFragmentInteractionListener) {
            mListener = (OnStockProfileFragmentInteractionListener) context;
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

    private void stopRefresh(){
        if(animationDrawable!=null)
            animationDrawable.stop();
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
                    fbLogAdEvents.logAdImpressionEvent("stock_profile_frame_banner");
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Log.e(TAG,"onAdOpened");
                    fbLogAdEvents.logAdClickEvent("stock_profile_frame_banner");
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

    public interface OnStockProfileFragmentInteractionListener {
        // TODO: Update argument type and name
        void onStockFragmentInteraction(Fragment stockNewsFragment);
    }

}
