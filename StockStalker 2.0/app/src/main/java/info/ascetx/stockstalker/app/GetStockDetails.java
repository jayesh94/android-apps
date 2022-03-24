package info.ascetx.stockstalker.app;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import info.ascetx.stockstalker.helper.StockDaily;

/**
 * @author 10637166
 *
 * This main class will be having data about the interval (Time(seconds)): i
 * &
 * Data about the period to be specified: p
 *
 * i and p will be decided based on the button pressed which in turn will decide the url to be used
 *
 * Only the whole response will be sent over to Get details class nothing else*
 *
 * 1D - 1min
 * http://www.google.com/finance/getprices?q=GOOGL&i=60&p=2d&f=d,c,v,o,h,l
 *
 * 1D - 3min
 * http://www.google.com/finance/getprices?q=GOOGL&i=180&p=3d&f=d,c,v,o,h,l
 *
 * 1D - 5min
 * http://www.google.com/finance/getprices?q=GOOGL&i=300&p=5d&f=d,c,v,o,h,l
 *
 * 1D - 15min
 * http://www.google.com/finance/getprices?q=GOOGL&i=900&p=5d&f=d,c,v,o,h,l
 *
 * 1D - 30min
 * http://www.google.com/finance/getprices?q=GOOGL&i=1800&p=30d&f=d,c,v,o,h,l
 *
 * 1M - 30D- 1D - 60min
 * http://www.google.com/finance/getprices?q=GOOGL&i=3600&p=30d&f=d,c,v,o,h,l
 *
 * 6M - 1D
 * https://www.google.com/finance/getprices?q=GOOGL&i=86400&p=6M&f=d,c,v,o,h,l
 *
 * 1Y - 1D
 * https://www.google.com/finance/getprices?q=GOOGL&i=86400&p=1Y&f=d,c,v,o,h,l
 *
 * 10Y - 1W
 * https://www.google.com/finance/getprices?q=GOOGL&i=604800&p=10Y&f=d,c,v,o,h,l
 *
 * 20Y - 1W
 * https://www.google.com/finance/getprices?q=GOOGL&i=604800&p=20Y&f=d,c,v,o,h,l
 *
 * 1D 5D 1M 3M 6M 1Y 10Y
 *
 */
public class GetStockDetails {

    private List<StockDaily> stockDaily;
    private List<StockDaily> stockDetails;

    public GetStockDetails() {
        // TODO Auto-generated constructor stub
        stockDaily = new ArrayList<StockDaily>();
        stockDetails = new ArrayList<StockDaily>();
    }
    
    public List<StockDaily> getStockIntradayDetails(JSONArray jsonArray, String intraPcls, String stockPeriod){
        String open, high, low, close, vol, ts;
        String tOpen = "0.0", tHigh = "0.0", tLow = "0.0", tClose = "0.0";
        Float pcls, closef;
        int interval;

        stockDetails.clear();

        for (int i = 0; i < jsonArray.length(); i++){
            try {
                open = String.valueOf(jsonArray.getJSONObject(i).get("open"));
                high = String.valueOf(jsonArray.getJSONObject(i).get("high"));
                low = String.valueOf(jsonArray.getJSONObject(i).get("low"));
                close = String.valueOf(jsonArray.getJSONObject(i).get("close"));

//                Log.e("GetStockDetails",open);
                open = (open.equals("null") || open == null) ? tOpen: open;
                high = (high.equals("null") || high == null) ? tHigh: high;
                low = (low.equals("null") || low == null) ? tLow: low;
                close = (close.equals("null") || close == null) ? tClose: close;

                open = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(open));
                high = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(high));
                low = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(low));
                close = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(close));

                tOpen = open;
                tHigh = high;
                tLow = low;
                tClose = close;
                
                closef = Float.parseFloat(close);
                pcls = Float.parseFloat(intraPcls);

                vol = String.valueOf(jsonArray.getJSONObject(i).get("volume"));
                ts = String.valueOf(jsonArray.getJSONObject(i).get("label"));
                stockDetails.add(new StockDaily(close,high,low,open,ts,vol,String.format(Locale.ENGLISH,"%.2f", closef - pcls),String.format(Locale.ENGLISH,"%.2f", (((closef - pcls) / pcls) * 100)),intraPcls));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    open = String.valueOf(jsonArray.getJSONObject(i).get("marketOpen"));
                    high = String.valueOf(jsonArray.getJSONObject(i).get("marketHigh"));
                    low = String.valueOf(jsonArray.getJSONObject(i).get("marketLow"));
                    close = String.valueOf(jsonArray.getJSONObject(i).get("marketClose"));

                    open = (open.equals("null") || open == null) ? tOpen: open;
                    high = (high.equals("null") || high == null) ? tHigh: high;
                    low = (low.equals("null") || low == null) ? tLow: low;
                    close = (close.equals("null") || close == null) ? tClose: close;

                    open = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(open));
                    high = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(high));
                    low = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(low));
                    close = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(close));

                    if (Float.parseFloat(close) == 0f)
                        close = open;

                    tOpen = open;
                    tHigh = high;
                    tLow = low;
                    tClose = close;

                    closef = Float.parseFloat(close);
                    pcls = Float.parseFloat(intraPcls);

                    vol = String.valueOf(jsonArray.getJSONObject(i).get("marketVolume"));
                    ts = String.valueOf(jsonArray.getJSONObject(i).get("label"));
                    stockDetails.add(new StockDaily(close,high,low,open,ts,vol,String.format(Locale.ENGLISH,"%.2f", closef - pcls),String.format(Locale.ENGLISH,"%.2f", (((closef - pcls) / pcls) * 100)),intraPcls));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        switch(stockPeriod){
            case "1min":
                interval = 1;
                break;
            case "3min":
                interval = 3;
                break;
            case "5min":
                interval = 5;
                break;
            case "10min":
                interval = 10;
                break;
            case "15min":
                interval = 15;
                break;
            case "30min":
                interval = 30;
                break;
            default:
                interval = 1;
                break;
        }

        List<StockDaily> stockDailies = new ArrayList<StockDaily>();

        for (StockDaily sd : stockDetails){
            int inter = stockDetails.indexOf(sd);
            if (inter % interval == 0){
                stockDailies.add(sd);
            }
        }
        return stockDailies;
    }

    public List<StockDaily> getStockHistoricalDetails(JSONArray jsonArray) {
        String open, high, low, close, chg, chgp, vol, ts;

        stockDetails.clear();

        for (int i = 0; i < jsonArray.length(); i++){
            try {
                open = String.valueOf(jsonArray.getJSONObject(i).get("open"));
                high = String.valueOf(jsonArray.getJSONObject(i).get("high"));
                low = String.valueOf(jsonArray.getJSONObject(i).get("low"));
                close = String.valueOf(jsonArray.getJSONObject(i).get("close"));
                chg = String.valueOf(jsonArray.getJSONObject(i).get("change"));
                chgp = String.valueOf(jsonArray.getJSONObject(i).get("changePercent"));
                open = ((!open.equals("null")) ? open : "0");
                high = ((!high.equals("null")) ? high : "0");
                low = ((!low.equals("null")) ? low : "0");
                close = ((!close.equals("null")) ? close : "0");
                chg = ((!chg.equals("null")) ? chg : "0");
                chgp = ((!chgp.equals("null")) ? chgp : "0");
                open = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(open));
                high = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(high));
                low = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(low));
                close = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(close));
                chg = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(chg));
                chgp = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(chgp));

                vol = String.valueOf(jsonArray.getJSONObject(i).get("volume"));
                ts = String.valueOf(jsonArray.getJSONObject(i).get("label"));
                stockDetails.add(new StockDaily(close,high,low,open,ts,vol,chg,chgp,null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return stockDetails;
    }

    public List<StockDaily> getStockDetailsList(String response, String intraPcls) {

        stockDaily.clear();
        String line, formattedDate;
        int tzo = 0, i = 0, interval;
        Long uts = null;
        float cls, pcls = 0.0f, chg, chgp, totalChg, totalChgp;

        try (BufferedReader in = new BufferedReader(new StringReader(response))){
            for (int i1 = 0; i1<6; i1++){
                line = in.readLine().trim();
                if(line.contains("INTERVAL=")){
                    i = Integer.valueOf(line.trim().substring(line.trim().indexOf('=')+1));
                }
            }

            while ((line = in.readLine())!= null){
                if(line.contains("TIMEZONE_OFFSET=")){
                    tzo = Integer.valueOf(line.trim().substring(line.trim().indexOf('=')+1));
                }
                else if(line.substring(0,1).equals("a")){
//            		COLUMNS=DATE,CLOSE,HIGH,LOW,OPEN,VOLUME
                    String[] x = line.trim().split(",");
                    uts = Long.valueOf(x[0].substring(1));
                    java.util.Date time = new java.util.Date(uts*1000);
                    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if(tzo == 330)
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("IST"));
                    else
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"+String.valueOf(tzo/60)));
//                    System.out.println("GMT"+String.valueOf(tzo/60));
                    formattedDate = sdf.format(time);
                    stockDaily.add(new StockDaily(x[1],x[2],x[3],x[4],formattedDate,x[5]));
                } else {
                    String[] x = line.trim().split(",");
                    interval = Integer.valueOf(x[0]);
                    java.util.Date time = new java.util.Date((uts+(interval*i))*1000);
                    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if(tzo == 330)
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("IST"));
                    else
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"+String.valueOf(tzo/60)));
                    formattedDate = sdf.format(time);
                    stockDaily.add(new StockDaily(x[1],x[2],x[3],x[4],formattedDate,x[5]));
                }
            }

            if (intraPcls == null){
                for (StockDaily sd : stockDaily ){
                    cls = Float.parseFloat(sd.getCls());
                    chg = cls - pcls;
                    chgp = (chg/pcls)*100;
                    stockDetails.add(new StockDaily(sd.getCls(),sd.getHigh(),sd.getLow(),sd.getOpen(),sd.getDate(),sd.getVol(),String.format(Locale.ENGLISH,"%.2f", chg),String.format(Locale.ENGLISH,"%.2f", chgp),String.format(Locale.ENGLISH,"%.2f", pcls)));
                    pcls = cls;
                }
            }else{
                for (StockDaily sd : stockDaily ){
                    cls = Float.parseFloat(sd.getCls());
                    pcls = Float.parseFloat(intraPcls);
                    stockDetails.add(new StockDaily(sd.getCls(),sd.getHigh(),sd.getLow(),sd.getOpen(),sd.getDate(),sd.getVol(),String.format(Locale.ENGLISH,"%.2f", cls - pcls),String.format(Locale.ENGLISH,"%.2f", (((cls - pcls) / pcls) * 100)),String.format(Locale.ENGLISH,"%.2f", pcls)));
                }
            }
            Collections.reverse(stockDetails);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stockDetails;
    }
}