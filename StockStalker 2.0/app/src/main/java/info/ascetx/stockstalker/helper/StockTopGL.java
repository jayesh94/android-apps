package info.ascetx.stockstalker.helper;

/**
 * Created by JAYESH on 05-03-2017.
 */
public class StockTopGL {
    public String name;
    public String stamp;
    public String ltp;
    public String chg;
    public String chg_p;
    public String pcls;
    public String vol;
    public String stock;
    public String exchange;
    public String mCap;
    public String pe;
    public String wh52;
    public String wl52;
    public String ytd;

    public StockTopGL(String stock, String name, String ltp, String chg, String chg_p, String pcls, String vol, String stamp,
                      String exchange, String mCap, String pe, String wh52, String wl52, String ytd) {
        this.name = name;
        this.stamp = stamp;
        this.ltp = ltp;
        this.chg = chg;
        this.chg_p = chg_p;
        this.pcls = pcls;
        this.vol = vol;
        this.stock = stock;
        this.exchange = exchange;
        this.mCap = mCap;
        this.pe = pe;
        this.wh52 = wh52;
        this.wl52 = wl52;
        this.ytd = ytd;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStamp(String stamp) {
        this.stamp = stamp;
    }

    public void setLtp(String ltp) {
        this.ltp = ltp;
    }

    public void setChg(String chg) {
        this.chg = chg;
    }

    public void setChg_p(String chg_p) {
        this.chg_p = chg_p;
    }

    public void setPcls(String pcls) {
        this.pcls = pcls;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setmCap(String mCap) {
        this.mCap = mCap;
    }

    public void setPe(String pe) {
        this.pe = pe;
    }

    public void setWh52(String wh52) {
        this.wh52 = wh52;
    }

    public void setWl52(String wl52) {
        this.wl52 = wl52;
    }

    public void setYtd(String ytd) {
        this.ytd = ytd;
    }

    public String getExchange() {
        return exchange;
    }

    public String getmCap() {
        return mCap;
    }

    public String getPe() {
        return pe;
    }

    public String getWh52() {
        return wh52;
    }

    public String getWl52() {
        return wl52;
    }

    public String getYtd() {
        return ytd;
    }

    public String getName() {
        return name;
    }

    public String getStamp() {
        return stamp;
    }

    public String getLtp() {
        return ltp;
    }

    public String getChg() {
        return chg;
    }

    public String getChg_p() {
        return chg_p;
    }

    public String getPcls() {
        return pcls;
    }

    public String getVol() {
        return vol;
    }

    public String getStock() {
        return stock;
    }
}
