package info.ascetx.stockstalker.helper;

/**
 * Created by JAYESH on 06-03-2017.
 */
public class StockDetails {
    public String open;
    public String high;
    public String low;
    public String close;
    public String pcls;
    public String close_chg;
    public String close_cp;
    public String date;

    public StockDetails() {

    }

    public StockDetails(String open, String high, String low, String close, String pcls, String close_chg, String close_cp, String date){
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.pcls = pcls;
        this.close_chg = close_chg;
        this.close_cp = close_cp;
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

    public String getPcls() {
        return pcls;
    }

    public void setPcls(String pcls) {
        this.pcls = pcls;
    }

    public String getClose_chg() {
        return close_chg;
    }

    public void setClose_chg(String close_chg) {
        this.close_chg = close_chg;
    }

    public String getClose_cp() {
        return close_cp;
    }

    public void setClose_cp(String close_cp) {
        this.close_cp = close_cp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
