package info.ascetx.stockstalker;

/**
 * Created by JAYESH on 06-03-2017.
 */
public class StockDaily {
    public String ltp;
    public String chg;
    public String chg_p;
    public String pcls;
    public String date;

    public StockDaily(String ltp, String chg, String chg_p, String pcls, String date){
        this.ltp = ltp;
        this.chg = chg;
        this.chg_p = chg_p;
        this.pcls = pcls;
        this.date = date;

    }

    public StockDaily() {

    }

    public String getLtp() {
        return ltp;
    }

    public void setLtp(String ltp) {
        this.ltp = ltp;
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

    public String getPcls() {
        return pcls;
    }

    public void setPcls(String pcls) {
        this.pcls = pcls;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
