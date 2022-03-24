package info.ascetx.stockstalker.helper;

/**
 * Created by JAYESH on 05-03-2017.
 */
public class StockName {
    public String name;
    public String stock;
    public String date;
    public String ltp;
    public String chg;
    public String chg_p;
    public String pcls;

    public StockName(String name, String stock, String date, String ltp, String chg, String chg_p, String pcls) {
        this.name = name;
        this.stock = stock;
        this.date = date;
        this.ltp = ltp;
        this.chg = chg;
        this.chg_p = chg_p;
        this.pcls = pcls;
    }

    public StockName() {

    }

    /*public StockName(String stock, String date, String name){
        this.name = name;
        this.stock = stock;
        this.date = date;
    }*/

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getDate() {
        return date;
    }

    public String getStock() {
        return stock;
    }
}
