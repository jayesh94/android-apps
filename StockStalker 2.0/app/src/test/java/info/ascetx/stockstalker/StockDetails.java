package info.ascetx.stockstalker;

/**
 * Created by JAYESH on 06-03-2017.
 */
public class StockDetails {
    public String open;
    public String high;
    public String low;
    public String close;
    public String vol;
    public String date;


    public StockDetails(String date, String close, String high, String low, String open, String vol ){
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.vol = vol;
        this.date = date;

    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getvol() {
        return vol;
    }

    public void setvol(String vol) {
        this.vol = vol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
