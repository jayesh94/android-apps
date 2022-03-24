package info.ascetx.stockstalker.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.ascetx.stockstalker.R;
import info.ascetx.stockstalker.app.AppController;
import info.ascetx.stockstalker.helper.Book;

import static info.ascetx.stockstalker.app.Config.URL_GET_US_NSQ_STOCKS;

/**
 * Created by JAYESH on 10-07-2018.
 */

public class BookAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private String TAG = "BookAutoCompleteAdapter";
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private View view;
    private List<Book> resultList = new ArrayList<Book>();

    public BookAutoCompleteAdapter(Context context, View autoComplete) {
        this.mContext = context;
        this.view = autoComplete;
//        this.progressBar = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Book getItem(int index) {
        return resultList.get(getCount() - index - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.text2)).setText(getItem(position).getTitle());
        ((TextView) convertView.findViewById(R.id.text3)).setText(getItem(position).getAuthor());
        return convertView;
    }

    @Override
    public Filter getFilter() {
//        Log.e(TAG, "getFiter ");
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    if (!constraint.toString().trim().equals("")) {
                        List<Book> books = findBooks(mContext, constraint.toString().trim());

                        // Assign the data to the FilterResults
                        filterResults.values = books;
                        filterResults.count = books.size();
                        resultList.clear();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Book>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    /**
     * Returns a search result for the given book title.
     */
    private List<Book> findBooks(final Context context, final String bookTitle) {
//        Log.e(TAG, "findBooks ");
        String tag_string_req = "req_stock";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_GET_US_NSQ_STOCKS, response -> {
    //                Log.e(TAG, "User Stock Response: " + response.toString());
                    if (response.length() > 0) {
                        try {
                            JSONArray j= new JSONArray(response);
                            // looping through json and adding to stock list
                            for (int i = 0; i < j.length(); i++) {
                                try {
                                    JSONObject stockObj = j.getJSONObject(i);

                                    String symbol = stockObj.getString("symbol");
                                    String type = stockObj.getString("type");
                                    String name = stockObj.getString("name");

                                    Book m = new Book(symbol, type, name);

    //                              int j = resultList.size();
    ////                              Log.d(TAG, Integer.toString(j));

                                    resultList.add(0,m);
    //                                Log.d(TAG, resultList.toString());

                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Object Parsing error: " + e.getMessage());
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Array Parsing error: " + e.getMessage());
                        }

                        notifyDataSetChanged();
                        view.findViewById(R.id.pb_loading_indicator).setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Volley Error: " + error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("stock", bookTitle);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        return resultList;
//        // GoogleBooksProtocol is a wrapper for the Google Books API
//        GoogleBooksProtocol protocol = new GoogleBooksProtocol(context, MAX_RESULTS);
//        return protocol.findBooks(bookTitle);
    }
}
