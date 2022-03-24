package info.ascetx.stockstalker.helper;

/**
 * Created by JAYESH on 06-03-2017.
 */
public class StockDaily {

    public String cls;
    public String high;
    public String low;
    public String open;
    public String date;
    public String vol;
    public String chg;
    public String chg_p;
    public String pcls;
    public String name;

    public StockDaily(String cls, String high, String low, String open, String date, String vol, String chg, String chg_p, String pcls){
        this.chg = chg;
        this.chg_p = chg_p;
        this.cls = cls;
        this.high = high;
        this.low = low;
        this.open = open;
        this.date = date;
        this.vol = vol;
        this.pcls = pcls;
    }

    public StockDaily(String cls, String high, String low, String open, String date, String vol){
        this.cls = cls;
        this.high = high;
        this.low = low;
        this.open = open;
        this.date = date;
        this.vol = vol;
    }

    public StockDaily() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPcls() {
        return pcls;
    }

    public void setPcls(String pcls) {
        this.pcls = pcls;
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

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getVol() {
        return vol;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public String getChg() {
        return chg;
    }

    public void setChg(String chg) {
        this.chg = chg;
    }

    public String getChg_p() {
        return chg_p;
    }

    public void setChg_p(String chg_p) {
        this.chg_p = chg_p;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
