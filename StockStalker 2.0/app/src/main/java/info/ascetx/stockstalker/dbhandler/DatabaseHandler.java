package info.ascetx.stockstalker.dbhandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import info.ascetx.stockstalker.app.SessionManager;
import info.ascetx.stockstalker.helper.StockDaily;
import info.ascetx.stockstalker.helper.StockDetails;
import info.ascetx.stockstalker.helper.StockName;
import info.ascetx.stockstalker.helper.StockTrend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JAYESH on 15-03-2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    private String TAG = "DatabaseHandler";

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "stocksManager";

    // Stocks table name
    private static final String TABLE_STOCKS = "NasdaqStocks";

    // Stocks Daily table name
    private static final String TABLE_STOCKS_DAILY = "Daily";

    // Stocks Trend table name
    private static final String TABLE_STOCKS_TREND = "Trend";

    // Stocks Day table name
    private static String TABLE_STOCKS_DAY;

    // Common Table names
    private static final String KEY_DATE = "date";

    // Stocks Table Columns names
    private static final String KEY_NAME = "nsq_stock_name";
    private static final String KEY_ID = "nsq_stock_id";

    // Stocks Day table column names
    private static final String KEY_OPEN = "open";
    private static final String KEY_HIGH = "high";
    private static final String KEY_LOW = "low";
    private static final String KEY_CLOSE = "close";
    private static final String KEY_PCLS = "cls";
    private static final String KEY_CLOSE_CHG = "close_chg";
    private static final String KEY_CLOSE_CP = "close_cp";

    // Stocks Trend table column names
    private static final String KEY_CLOSE1 = "close1";
    private static final String KEY_CLOSE_CHG1 = "close_chg1";
    private static final String KEY_CLOSE_CP1 = "close_cp1";
    private static final String KEY_DATE1 = "date1";
    private static final String KEY_CLOSE2 = "close2";
    private static final String KEY_CLOSE_CHG2 = "close_chg2";
    private static final String KEY_CLOSE_CP2 = "close_cp2";
    private static final String KEY_DATE2 = "date2";
    private static final String KEY_CLOSE3 = "close3";
    private static final String KEY_CLOSE_CHG3 = "close_chg3";
    private static final String KEY_CLOSE_CP3 = "close_cp3";
    private static final String KEY_DATE3 = "date3";
    private static final String KEY_CLOSE4 = "close4";
    private static final String KEY_CLOSE_CHG4 = "close_chg4";
    private static final String KEY_CLOSE_CP4 = "close_cp4";
    private static final String KEY_DATE4 = "date4";
    private static final String KEY_CLOSE5 = "close5";
    private static final String KEY_CLOSE_CHG5 = "close_chg5";
    private static final String KEY_CLOSE_CP5 = "close_cp5";
    private static final String KEY_DATE5 = "date5";

    // Stocks Daily table column names
    private static final String KEY_LTP = "ltp";
    private static final String KEY_CHG = "chg";
    private static final String KEY_CHG_P = "chg_p";
    private static final String KEY_P_CLOSE = "p_close";
    private static final String KEY_VOL = "vol";

    //Table create statements
    //Stocks table create statement
    private static final String CREATE_STOCKS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS + "("
            + KEY_ID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_LTP + " REAL," + KEY_CHG + " REAL," +
            KEY_CHG_P + " REAL," + KEY_P_CLOSE + " REAL," + KEY_VOL + " REAL," + KEY_DATE + " TEXT, " +
            "CONSTRAINT unique_key_id UNIQUE (" + KEY_ID + "))";

    //Stocks Daily table create statement
//    private static final String CREATE_STOCKS_DAILY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS_DAILY + "("
//            + KEY_NAME + " TEXT PRIMARY KEY," + KEY_DATE + " TEXT)";

    //Stocks Day table create statement
//    public static String CREATE_STOCKS_DAY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS_DAY + "("
//            + KEY_OPEN + " TEXT PRIMARY KEY," + KEY_HIGH + " TEXT," + KEY_LOW + " TEXT," + KEY_CLOSE + " TEXT,"
//            + KEY_PCLS + " TEXT," + KEY_CLOSE_CHG + " TEXT," + KEY_CLOSE_CP + " TEXT," + KEY_DATE + " TEXT)";

    // Stocks Day table create statement
    private static final String CREATE_STOCKS_TREND_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS_TREND + "("
            + KEY_NAME + " TEXT PRIMARY KEY," +
            KEY_CLOSE1 + " TEXT," + KEY_CLOSE_CHG1 + " TEXT," + KEY_CLOSE_CP1 + " TEXT," + KEY_DATE1 + " TEXT," +
            KEY_CLOSE2 + " TEXT," + KEY_CLOSE_CHG2 + " TEXT," + KEY_CLOSE_CP2 + " TEXT," + KEY_DATE2 + " TEXT," +
            KEY_CLOSE3 + " TEXT," + KEY_CLOSE_CHG3 + " TEXT," + KEY_CLOSE_CP3 + " TEXT," + KEY_DATE3 + " TEXT," +
            KEY_CLOSE4 + " TEXT," + KEY_CLOSE_CHG4 + " TEXT," + KEY_CLOSE_CP4 + " TEXT," + KEY_DATE4 + " TEXT," +
            KEY_CLOSE5 + " TEXT," + KEY_CLOSE_CHG5 + " TEXT," + KEY_CLOSE_CP5 + " TEXT," + KEY_DATE5 + " TEXT)";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "On Create db");
        db.execSQL(CREATE_STOCKS_TABLE);
        db.execSQL(CREATE_STOCKS_TREND_TABLE);

        db.execSQL("INSERT INTO " + TABLE_STOCKS+ "("+ KEY_NAME +", "+KEY_ID+" ) VALUES ('Microsoft Corporation', 'MSFT')");
        db.execSQL("INSERT INTO " + TABLE_STOCKS+ "("+ KEY_NAME +", "+KEY_ID+" ) VALUES ('Alphabet Inc.', 'GOOGL')");
//        db.execSQL("INSERT INTO " + TABLE_STOCKS+ "(NAME, A, B, SCALEFACTOR, FEASTING ) VALUES ('NAD 83',6378137.00,6356752.314,0.9996,500000)");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS_TREND);

        // Create tables again
        onCreate(db);
    }

    public void dbRefresh(){
        Log.e(TAG, "On Refresh db");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_STOCKS+ "("+ KEY_NAME +", "+KEY_ID+" ) VALUES ('Microsoft Corporation', 'MSFT')");
        db.execSQL("INSERT INTO " + TABLE_STOCKS+ "("+ KEY_NAME +", "+KEY_ID+" ) VALUES ('Alphabet Inc.', 'GOOGL')");
    }

    /****************************************************************************************************************
     *  Table methods for Trend
     */

    // Adding new stock trend
    public void addStockTrend(StockTrend stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, stock.getStock());

        values.put(KEY_CLOSE1, stock.getClose1());
        values.put(KEY_CLOSE_CHG1, stock.getChg1());
        values.put(KEY_CLOSE_CP1, stock.getCp1());
        values.put(KEY_DATE1, stock.getDate1());

        values.put(KEY_CLOSE2, stock.getClose2());
        values.put(KEY_CLOSE_CHG2, stock.getChg2());
        values.put(KEY_CLOSE_CP2, stock.getCp2());
        values.put(KEY_DATE2, stock.getDate2());

        values.put(KEY_CLOSE3, stock.getClose3());
        values.put(KEY_CLOSE_CHG3, stock.getChg3());
        values.put(KEY_CLOSE_CP3, stock.getCp3());
        values.put(KEY_DATE3, stock.getDate3());

        values.put(KEY_CLOSE4, stock.getClose4());
        values.put(KEY_CLOSE_CHG4, stock.getChg4());
        values.put(KEY_CLOSE_CP4, stock.getCp4());
        values.put(KEY_DATE4, stock.getDate4());

        values.put(KEY_CLOSE5, stock.getClose5());
        values.put(KEY_CLOSE_CHG5, stock.getChg5());
        values.put(KEY_CLOSE_CP5, stock.getCp5());
        values.put(KEY_DATE5, stock.getDate5());


        // Inserting Row
        db.insert(TABLE_STOCKS_TREND, null, values);
        db.close(); // Closing database connection
    }

    // Getting single stock
//    StockName getStockName(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_STOCKS, new String[] { KEY_ID,
//                        KEY_NAME, KEY_DATE }, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//        if (cursor != null)
//            cursor.moveToFirst();
//
//        StockName stock = new StockName(Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2));
//        // return stock
//        return stock;
//    }

    // Getting All Trend
    public List<StockTrend> getAllStockTrend() {
        List<StockTrend> stockList = new ArrayList<StockTrend>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS_TREND + " ORDER BY " + KEY_NAME + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StockTrend stock = new StockTrend();
                stock.setStock(cursor.getString(0));

                stock.setClose1(cursor.getString(1));
                stock.setChg1(cursor.getString(2));
                stock.setCp1(cursor.getString(3));
                stock.setDate1(cursor.getString(4));

                stock.setClose2(cursor.getString(5));
                stock.setChg2(cursor.getString(6));
                stock.setCp2(cursor.getString(7));
                stock.setDate2(cursor.getString(8));

                stock.setClose3(cursor.getString(9));
                stock.setChg3(cursor.getString(10));
                stock.setCp3(cursor.getString(11));
                stock.setDate3(cursor.getString(12));

                stock.setClose4(cursor.getString(13));
                stock.setChg4(cursor.getString(14));
                stock.setCp4(cursor.getString(15));
                stock.setDate4(cursor.getString(16));

                stock.setClose5(cursor.getString(17));
                stock.setChg5(cursor.getString(18));
                stock.setCp5(cursor.getString(19));
                stock.setDate5(cursor.getString(20));

                // Adding stock to list
                stockList.add(stock);
            } while (cursor.moveToNext());
        }

        // return stock list
        return stockList;
    }

    // Updating single stock
//    public int updateStockName(StockName stock) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, stock.getStock());
//        values.put(KEY_DATE, stock.getDate());
//
//        // updating row
//        return db.update(TABLE_STOCKS, values, KEY_ID + " = ?",
//                new String[] { String.valueOf(stock.getID()) });
//    }

    // Deleting single stock
//    public void deleteStockName(StockName stock) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_STOCKS, KEY_ID + " = ?",
//                new String[] { String.valueOf(stock.getID()) });
//        db.close();
//    }

    // Deleting one Trend name
    public void deleteStockTrend(String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS_TREND,KEY_NAME +"=?",new String[]{message});
        db.close();
    }

    // Deleting whole Trend table
    public void deleteStockTrendWhole() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS_TREND,null,null);
        db.close();
    }


    // Getting stocks Count
//    public int getStockNamesCount() {
//        String countQuery = "SELECT * FROM " + TABLE_STOCKS;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();
//
//        // return count
//        return cursor.getCount();
//    }

    /****************************************************************************************************************
     *  Table methods for NseStocks
     */

    // Adding new stock
    public void addStockName(String stock, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME,name); // StockName name
        values.put(KEY_ID, stock); // StockName ID
//        values.put(KEY_LTP, stock.getLtp());
//        values.put(KEY_CHG, stock.getChg());
//        values.put(KEY_CHG_P, stock.getChg_p());
//        values.put(KEY_P_CLOSE, stock.getCls());
//        values.put(KEY_DATE, stock.getDate()); // StockName Date

        // Inserting Row
        long ins = db.insert(TABLE_STOCKS, null, values);
        Log.e(TAG,"insert output: "+ins);
        db.close(); // Closing database connection
    }

    // Adding new stock details
    public void updateStockDetails(String stock, String ltp, String chg, String chgp, String pcls, String vol, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, stock); // StockName ID
        values.put(KEY_LTP, ltp);
        values.put(KEY_CHG, chg);
        values.put(KEY_CHG_P, chgp);
        values.put(KEY_P_CLOSE, pcls);
        values.put(KEY_VOL, vol);
        values.put(KEY_DATE, date); // StockName Date

        // updating stock details
        db.update(TABLE_STOCKS, values, KEY_ID + " = ?",
                new String[] { stock });
        db.close(); // Closing database connection
    }

    /*// Updating single stock
    public int updateStockDetails(String stock, String ltp, String vol, String chg, String chg_p, String ts) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LTP, ltp);
        values.put(KEY_CHG, chg);
        values.put(KEY_CHG_P, chg_p);
        values.put(KEY_DATE, ts);

        // updating row
        return db.update(TABLE_STOCKS, values, KEY_ID + " = ?",
                new String[] { stock });
    }*/

    // Getting single stock
    public String getStockPcls(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STOCKS, new String[] { KEY_P_CLOSE
                         }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        String pcls = cursor.getString(0);
        cursor.close();
//        StockName stock = new StockName(Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2));
        // return stock
        return pcls;
    }

    // Getting All StockNames
    public List<StockName> getAllStockNames(Context context) {
        List<StockName> stockList = new ArrayList<StockName>();

        SessionManager session = new SessionManager(context);
        String toggleBy = session.isToggle()? " DESC" : " ASC";
        String sortBy = session.isSortBy();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS + " ORDER BY " + sortBy + toggleBy;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StockName stock = new StockName();
                stock.setStock(cursor.getString(0));
                stock.setName(cursor.getString(1));
                stock.setLtp(cursor.getString(2));
                stock.setChg(cursor.getString(3));
                stock.setChg_p(cursor.getString(4));
                stock.setPcls(cursor.getString(5));
                stock.setDate(cursor.getString(7));
                // Adding stock to list
                stockList.add(stock);
            } while (cursor.moveToNext());
        }

        // return stock list
        return stockList;
    }

    // Check if Stock is already present in DB
    public boolean hasStock(String stock) {
        String Query = "SELECT * FROM " + TABLE_STOCKS + " WHERE " + KEY_ID + " = '"+ stock +"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // Getting Single stock details
    public StockDaily getSingleStockDetails(String stockID) {

        StockDaily stock = new StockDaily();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS + " WHERE " + KEY_ID +" = '"+ stockID+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                stock.setName(cursor.getString(1));
                stock.setCls(cursor.getString(2));
                stock.setChg(cursor.getString(3));
                stock.setChg_p(cursor.getString(4));
                stock.setPcls(cursor.getString(5));
                stock.setVol(cursor.getString(6));
                stock.setDate(cursor.getString(7));
            } while (cursor.moveToNext());
        }

        // return stock list
        return stock;
    }

    // Updating single stock
    public int updateStockName(StockName stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHG, stock.getChg());
        values.put(KEY_LTP, stock.getLtp());
        values.put(KEY_CHG_P, stock.getChg_p());
        values.put(KEY_P_CLOSE, stock.getPcls());
        values.put(KEY_DATE, stock.getDate());

        // updating row
        return db.update(TABLE_STOCKS, values, KEY_ID + " = ?",
                new String[] { stock.getStock() });
    }

    // Updating single stock
    public int updateStockNamePcls(String stock, String pcls) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_P_CLOSE, pcls);

        // updating row
        return db.update(TABLE_STOCKS, values, KEY_ID + " = ?",
                new String[] { stock });
    }

    // Deleting single stock
//    public void deleteStockName(StockName stock) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_STOCKS, KEY_ID + " = ?",
//                new String[] { String.valueOf(stock.getID()) });
//        db.close();
//    }

    // Deleting one NseStock name
    public void deleteStockName(String deleteStock) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS,KEY_ID +"=?",new String[]{deleteStock});
        db.close();
    }

    // Deleting whole NseStock table
    public void deleteStockWhole() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS,null,null);
        db.close();
    }


    // Getting stocks Count
//    public int getStockNamesCount() {
//        String countQuery = "SELECT * FROM " + TABLE_STOCKS;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();
//
//        // return count
//        return cursor.getCount();
//    }

    /***************************************************************************************************************
     * Table methods for Day Stocks
     */

    public void createDayStock(){
        SQLiteDatabase db = this.getWritableDatabase();

//        Log.d(TAG,"TABLE_STOCKS_DAY " + TABLE_STOCKS_DAY);

        String CREATE_STOCKS_DAY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS_DAY + "("
                + KEY_OPEN + " TEXT," + KEY_HIGH + " TEXT," + KEY_LOW + " TEXT," + KEY_CLOSE + " TEXT,"
                + KEY_PCLS + " TEXT," + KEY_CLOSE_CHG + " TEXT," + KEY_CLOSE_CP + " TEXT," + KEY_DATE + " TEXT)";
//        Log.d(TAG,"CREATE_STOCKS_DAY_TABLE " + CREATE_STOCKS_DAY_TABLE);

        db.execSQL(CREATE_STOCKS_DAY_TABLE);
    }

    // Adding new stock
    public void addStockDay(StockDetails stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OPEN, stock.getOpen());      // GetStockDetails Open
        values.put(KEY_HIGH, stock.getHigh());      // GetStockDetails Open
        values.put(KEY_LOW, stock.getLow());         // GetStockDetails Open
        values.put(KEY_CLOSE, stock.getClose());    // GetStockDetails Open
        values.put(KEY_PCLS, stock.getPcls());      // GetStockDetails Open
        values.put(KEY_CLOSE_CHG, stock.getClose_chg());    // GetStockDetails Open
        values.put(KEY_CLOSE_CP, stock.getClose_cp());      // GetStockDetails Open
        values.put(KEY_DATE, stock.getDate());              // GetStockDetails Date

        // Inserting Row
        db.insert(TABLE_STOCKS_DAY, null, values);
        db.close(); // Closing database connection
    }

    // Getting All StockNames
    public List<StockDetails> getAllStockDay() {
        List<StockDetails> stockList = new ArrayList<StockDetails>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS_DAY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StockDetails stock = new StockDetails();
                stock.setOpen(cursor.getString(0));
                stock.setHigh(cursor.getString(1));
                stock.setLow(cursor.getString(2));
                stock.setClose(cursor.getString(3));
                stock.setPcls(cursor.getString(4));
                stock.setClose_chg(cursor.getString(5));
                stock.setClose_cp(cursor.getString(6));
                stock.setDate(cursor.getString(7));
                // Adding stock to list
                stockList.add(stock);
            } while (cursor.moveToNext());
        }

        // return stock list
        return stockList;
    }

    // Deleting whole NseStock table
    public void deleteStockDay() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS_DAY,null,null);
        db.close();
    }

    /***************************************************************************************************************
     * Table methods for Daily Stocks
     */

    public void createDailyStock(){
        SQLiteDatabase db = this.getWritableDatabase();

//        Log.d(TAG,"TABLE_STOCKS_DAILY " + TABLE_STOCKS_DAILY);

        String CREATE_STOCKS_DAILY_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS_DAILY + "("
                + KEY_LTP + " TEXT," + KEY_CHG + " TEXT," + KEY_CHG_P + " TEXT," + KEY_P_CLOSE + " TEXT," + KEY_DATE + " TEXT)";

//        Log.d(TAG,"CREATE_STOCKS_DAILY_TABLE " + CREATE_STOCKS_DAILY_TABLE);

        db.execSQL(CREATE_STOCKS_DAILY_TABLE);
    }

    // Adding new stock daily data
    public void addStockDaily(StockDaily stock) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LTP, stock.getCls());
        values.put(KEY_CHG, stock.getChg());
        values.put(KEY_CHG_P, stock.getChg_p());
        values.put(KEY_P_CLOSE, stock.getCls());
        values.put(KEY_DATE, stock.getDate());

        // Inserting Row
        db.insert(TABLE_STOCKS_DAILY, null, values);
        db.close(); // Closing database connection
    }

    // Getting All Stock daily data
    public List<StockDaily> getAllStockDaily() {
        List<StockDaily> stockList = new ArrayList<StockDaily>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS_DAILY;

//        Log.d(TAG,"SELECT * FROM " + TABLE_STOCKS_DAILY);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                StockDaily stock = new StockDaily();
                stock.setCls(cursor.getString(0));
                stock.setChg(cursor.getString(1));
                stock.setChg_p(cursor.getString(2));
                stock.setCls(cursor.getString(3));
                stock.setDate(cursor.getString(4));

                // Adding stock to list
                stockList.add(stock);
            } while (cursor.moveToNext());
        }

        // return stock list
        return stockList;
    }

    // Deleting whole NseStock table
    public void deleteStockDaily() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS_DAILY,null,null);
        db.close();
    }
}
