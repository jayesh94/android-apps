package app.mugup.mugup.fragment.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.mugup.mugup.R;
import app.mugup.mugup.adapter.OrderRecyclerViewAdapter;
import app.mugup.mugup.helper.OrderItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static app.mugup.mugup.app.Config.URL_GET_USER_ORDER_HISTORY;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class OrderFragment extends Fragment {
    private static final String TAG = OrderFragment.class.getSimpleName();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private OnListFragmentInteractionListener mListener;

    private ProgressDialog pDialog;
    private ArrayList<OrderItem> orderItems;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrderFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static OrderFragment newInstance(int columnCount) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        orderItems = new ArrayList<OrderItem>();
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showpDialog();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest sr = new StringRequest(Request.Method.POST, URL_GET_USER_ORDER_HISTORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.e(TAG, response);
                hidepDialog();
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    Boolean error = jsonObject.getBoolean("error");
                    String error_msg = jsonObject.getString("error_msg");

                    if(error){
                        showSnackBar(error_msg);
                    } else {

                        JSONArray jsonArray = new JSONArray(error_msg);
                        if (jsonArray.length() > 0) {
                            for (int n = 0; n < jsonArray.length(); n++) {
                                JSONObject json = jsonArray.getJSONObject(n);
                                String purchase_timestamp = json.getString("purchase_timestamp");
                                String amount = json.getString("amount");
                                String status = json.getString("status");
                                String subjects = json.getString("subjects");
                                String invoiceId = json.getString("invoice_id");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                try {
                                    Date d = sdf.parse(purchase_timestamp);
                                    sdf.applyPattern("dd MMM yyyy 'at' h:mm aaa");
                                    purchase_timestamp = sdf.format(d);
                                } catch (ParseException ex) {
                                    Log.v("Exception", ex.getLocalizedMessage());
                                }

                                orderItems.add(new OrderItem(purchase_timestamp, amount, status, subjects, invoiceId));
                            }

                            TextView textView = view.findViewById(R.id.tv_no_orders);
                            RecyclerView recyclerView = view.findViewById(R.id.list);

                            // Set the adapter
                            Context context = view.getContext();
                            recyclerView.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.GONE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                            recyclerView.setAdapter(new OrderRecyclerViewAdapter(orderItems, mListener));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
                hidepDialog();
                showSnackBar(getResources().getString(R.string.volley_error));
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                //params.put("uid", "90K0ULvgsEPNvqp6JogXAKEYYhy2");
                return params;
            }
        };
        queue.add(sr);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    void showSnackBar(String msg){
        Context context = (Context) mListener;
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        snackbar.show();

    }

}
