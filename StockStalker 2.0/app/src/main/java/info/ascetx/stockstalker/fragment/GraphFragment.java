package info.ascetx.stockstalker.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.app.GetStockDetails;
import info.ascetx.stockstalker.app.MyMarkerView;
import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.app.SnackBarDisplay;
import info.ascetx.stockstalker.helper.StockDaily;

import static info.ascetx.stockstalker.MainActivity.BASE_URL;
import static info.ascetx.stockstalker.MainActivity.animationDrawable;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_HISTORIC_CHART_DETAILS;
import static info.ascetx.stockstalker.app.Config.URL_GET_NSQ_STOCK_INTRA_CHART_DETAILS;
import static info.ascetx.stockstalker.app.KSEncryptDecrypt.PROD_TOKEN;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GraphFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String TAG = "GraphFragment";
    private List<StockDaily> stockDaily;
    private List<CandleEntry> candleEntries;
    private List<BarEntry> barEntries;
    private List<Entry> entries;
    private GetStockDetails details;
    private CombinedChart combinedChart;
    private ProgressDialog pDialog;
    private SessionManager session;
    private View mView;
    private LineDataSet lineDataSet;
    private CandleDataSet candleDataSet;
    private float scaleX = 2f;
    private SnackBarDisplay snackBarDisplay;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GraphFragment newInstance(String param1, String param2) {
        GraphFragment fragment = new GraphFragment();
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
        candleEntries = new ArrayList<CandleEntry>();
        barEntries = new ArrayList<BarEntry>();
        entries = new ArrayList<Entry>();
        stockDaily = new ArrayList<>();
        details = new GetStockDetails();
        pDialog = new ProgressDialog((Context) mListener);
        session = new SessionManager(getActivity());
        snackBarDisplay = new SnackBarDisplay((Context) mListener);

        pDialog.setMessage("Please Wait...");
        pDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        mView = view;

        combinedChart = (CombinedChart) view.findViewById(R.id.combined_chart);
        AppCompatSpinner spinnerChartType = (AppCompatSpinner) view.findViewById(R.id.spinner_chart_type);
        AppCompatSpinner spinnerChartPeriod = (AppCompatSpinner) view.findViewById(R.id.spinner_chart_period);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapterChartType = new ArrayAdapter <String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.chart_array));
        ArrayAdapter<String> dataAdapterChartPeriod = new ArrayAdapter <String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.period_array));

        // Drop down layout style - list view with radio button
        dataAdapterChartType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapterChartPeriod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerChartType.setAdapter(dataAdapterChartType);
        spinnerChartPeriod.setAdapter(dataAdapterChartPeriod);

        spinnerChartType.setSelection(session.getChartType());
        spinnerChartPeriod.setSelection(session.getChartPeriod());

        spinnerChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                ((TextView) parent.getChildAt(0)).setTextSize(getResources().getDimension(R.dimen.text_nano));
                Log.e(TAG,"Spinner chart type onItemSelected: " + position);
                session.setChartType(position);
                if(lineDataSet != null || candleDataSet != null)
                    toggleChart();
                combinedChart.getDescription().setText(session.getStockName()+" "+getResources().getStringArray(R.array.period_array)[session.getChartPeriod()]+" "+getResources().getStringArray(R.array.chart_array)[session.getChartType()]+ " Chart");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        spinnerChartPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                ((TextView) parent.getChildAt(0)).setTextSize(getResources().getDimension(R.dimen.text_nano));
                Log.e(TAG,"Spinner chart period onItemSelected: " + position);
                session.setChartPeriod(position);
                scaleX = 2f;
                if(combinedChart!=null){
                    entries.clear();
                    barEntries.clear();
                    candleEntries.clear();
                    combinedChart.fitScreen();
                    combinedChart.invalidate();
                    combinedChart.clear();
                }
                try {
                    setCombinedChartParams();
                    displayCombinedChart();
                }catch (NullPointerException e){
                    Log.e(TAG,e.getMessage());
                    if(combinedChart!=null){
                        entries.clear();
                        barEntries.clear();
                        candleEntries.clear();
                        combinedChart.invalidate();
                        combinedChart.clear();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        return view;
    }

    public void onRefresh(){
        if(combinedChart!=null){
            entries.clear();
            barEntries.clear();
            candleEntries.clear();
            ViewPortHandler viewPortHandler = combinedChart.getViewPortHandler();
            scaleX = viewPortHandler.getScaleX();
//            Log.e(TAG, scaleX + " " + scaleY);
            combinedChart.fitScreen();
            combinedChart.invalidate();
            combinedChart.clear();
        }
        try {
            setCombinedChartParams();
            displayCombinedChart();
        }catch (NullPointerException e){
            Log.e(TAG,e.getMessage());
            if(combinedChart!=null){
                entries.clear();
                barEntries.clear();
                candleEntries.clear();
                combinedChart.invalidate();
                combinedChart.clear();
            }
        }
    }

    private void setCombinedChartParams(){
        combinedChart.setDrawGridBackground(false);
        combinedChart.getLegend().setEnabled(false);
        if (session.getTheme() == 0){
            combinedChart.getLegend().setTextColor(getResources().getColor(R.color.stock_same_dark));
            combinedChart.getXAxis().setTextColor(getResources().getColor(R.color.stock_same_dark));
            combinedChart.getAxisRight().setTextColor(getResources().getColor(R.color.stock_same_dark));
            combinedChart.getAxisLeft().setTextColor(getResources().getColor(R.color.stock_same_dark));
        }else if(session.getTheme() == 1){
            combinedChart.getLegend().setTextColor(getResources().getColor(R.color.stock_same));
            combinedChart.getXAxis().setTextColor(getResources().getColor(R.color.stock_same));
            combinedChart.getAxisRight().setTextColor(getResources().getColor(R.color.stock_same));
            combinedChart.getAxisLeft().setTextColor(getResources().getColor(R.color.stock_same));
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
//                        if (session.getChartPeriod() == 10 || session.getChartPeriod() == 11)
//                            dt1 = new SimpleDateFormat("yy MMM dd", Locale.ENGLISH);
//                        else if(session.getChartPeriod() == 9 || session.getChartPeriod() == 8 || session.getChartPeriod() == 7)
//                            dt1 = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
//                        else
//                            dt1 = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
//                        date = dt1.format(date1);
////                        date = date.substring(date.length() - 8);
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
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        });
        Legend l = combinedChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        combinedChart.getDescription().setText(session.getStockName()+" "+getResources().getStringArray(R.array.period_array)[session.getChartPeriod()]+" "+getResources().getStringArray(R.array.chart_array)[session.getChartType()]+ " Chart");
        if (session.getTheme() == 0)
            combinedChart.getDescription().setTextColor(getResources().getColor(R.color.stock_same_dark));
        else if(session.getTheme() == 1)
            combinedChart.getDescription().setTextColor(getResources().getColor(R.color.stock_same));

        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(combinedChart);
        combinedChart.setMarker(mv);
    }

    public void displayCombinedChart() {
        String stockPeriod, url;
        stockPeriod = getResources().getStringArray(R.array.period_array)[session.getChartPeriod()];

//        String url = URL_GET_NASDAQ_STOCKS_CHART_DETAILS+"q="+ session.getStockName() +"&i="+ i +"&p="+ p +"&f=d,c,v,o,h,l";

        if (session.getChartPeriod() > 5)
            url = String.format(URL_GET_NSQ_STOCK_HISTORIC_CHART_DETAILS, session.getStockName(), stockPeriod);
        else
            url = String.format(URL_GET_NSQ_STOCK_INTRA_CHART_DETAILS, BASE_URL, session.getStockName(), PROD_TOKEN);

        String tag_string_req = "tag_stock_details";

        showpDialog();

        Log.e(TAG, url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, jsonArray -> {
            float high, low, open, close, vol;

//                Log.e(TAG, "User Stock Response: " + response);
            if (jsonArray.length() > 0) {

                stockDaily.clear();

                if (session.getChartPeriod() > 5)
                    stockDaily = details.getStockHistoricalDetails(jsonArray);
                else
                    stockDaily = details.getStockIntradayDetails(jsonArray, "0.0", stockPeriod);

//                Collections.reverse(stockDaily);

                for (int x = 0; x < stockDaily.size(); x++ ){
//                        System.out.println(stockDaily.get(x).getDate()+" "+stockDaily.get(x).getLtp()+" "+stockDaily.get(x).getCls()+" "+stockDaily.get(x).getChg()+" "+stockDaily.get(x).getChg_p());

                    high = Float.parseFloat(stockDaily.get(x).getHigh());
                    low = Float.parseFloat(stockDaily.get(x).getLow());
                    open = Float.parseFloat(stockDaily.get(x).getOpen());
                    close = Float.parseFloat(stockDaily.get(x).getCls());
                    vol = Float.parseFloat(stockDaily.get(x).getVol());

                    candleEntries.add(new CandleEntry(x,high,low,open,close,stockDaily.get(x).getDate()));
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
//                        lineDataSet.setCircleColor(getResources().getColor(R.color.line_chart_dark));
                    lineDataSet.setValueTextColor(getResources().getColor(R.color.stock_same_dark));
                    lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_fill_dark));

                }else if(session.getTheme() == 1){
//                        lineDataSet.setValueTextColor(getResources().getColor(R.color.stock_same));
                    lineDataSet.setColor(getResources().getColor(R.color.line_chart));
//                        lineDataSet.setCircleColor(getResources().getColor(R.color.line_chart));
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
//************************************* Start Set Candle Chart Data *************************************************************************************

                candleDataSet = new CandleDataSet(candleEntries,"Candlestick Chart");
                candleDataSet.setShadowColorSameAsCandle(true);
                candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
                candleDataSet.setIncreasingColor(getResources().getColor(R.color.stock_rise));
                candleDataSet.setDecreasingColor(getResources().getColor(R.color.stock_fall));

                if (session.getTheme() == 0){
                    candleDataSet.setNeutralColor(getResources().getColor(R.color.stock_same_dark));
                    candleDataSet.setValueTextColor(getResources().getColor(R.color.stock_same_dark));

                }else if(session.getTheme() == 1){
                    candleDataSet.setNeutralColor(getResources().getColor(R.color.stock_same));
//                        candleDataSet.setValueTextColor(getResources().getColor(R.color.stock_same));
                }
                candleDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

                CandleData candleData = new CandleData(candleDataSet);

                candleData.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return new DecimalFormat("#.##").format(value);
                    }
                });
//************************************* End Set Candle Chart Data *************************************************************************************

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

                float barYMax = barData.getYMax();
                float candleYMin = candleData.getYMin();

                // to make bar appears at bottom
                YAxis leftAxis = combinedChart.getAxisLeft();
//                    leftAxis.setAxisMaximum(barYMax * 2);

                // to make line appears at top
                YAxis rightAxis = combinedChart.getAxisRight();
                rightAxis.setAxisMinimum(candleYMin * 0.99f);

                XAxis xAxis = combinedChart.getXAxis();
                xAxis.setSpaceMin(barData.getBarWidth() / 2f);
                xAxis.setSpaceMax(barData.getBarWidth() / 2f);

                CombinedData combinedData = new CombinedData();

                combinedData.setData(lineData);
                combinedData.setData(candleData);
                combinedData.setData(barData);


                combinedChart.setData(combinedData);
                combinedChart.zoom(scaleX,0,entries.size() - 1,0,rightAxis.getAxisDependency() );

                toggleChart();
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

    private void stopRefresh(){
        if(animationDrawable!=null)
            animationDrawable.stop();
        hidepDialog();
    }

    private void toggleChart() {
        if(session.getChartType() == 0){
            lineDataSet.setVisible(true);
            lineDataSet.setHighlightEnabled(true);
            candleDataSet.setHighlightEnabled(false);
            candleDataSet.setVisible(false);
        }else if(session.getChartType() == 1){
            candleDataSet.setVisible(true);
            candleDataSet.setHighlightEnabled(true);
            lineDataSet.setHighlightEnabled(false);
            lineDataSet.setVisible(false);
        }
        combinedChart.notifyDataSetChanged();
        combinedChart.invalidate();
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
    public void onDetach(){
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }
}