package app.mugup.mugup.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.android.volley.toolbox.JsonArrayRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import app.mugup.mugup.R;
import app.mugup.mugup.adapter.SubjectSelectionListAdapter;
import app.mugup.mugup.app.Config;
import app.mugup.mugup.helper.SubjectData;
import app.mugup.mugup.helper.SubjectSelectionListRow;
import static app.mugup.mugup.MainActivity.CURRENT_TAG;
import static app.mugup.mugup.MainActivity.TAG_SUBJECT_SELECTION_FRAME;
import static app.mugup.mugup.app.Config.PAYTM_CALLBACK_URL;
import static app.mugup.mugup.app.Config.PAYTM_INDUSTRY_TYPE_ID;

public class SubjectSelectionFragment extends DialogFragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static String TAG = SubjectSelectionFragment.class.getSimpleName();

    private String mParam1;
    private String mParam2;

    private View mView;

    private OnFragmentInteractionListener mListener;
    float finalAmount;
    private ProgressDialog pDialog;
    private SubjectSelectionListAdapter adapter;
    List<List<String>> selectedBooksMatrix = new ArrayList<>();
    List<List<String>> discountMatrix = new ArrayList<>();

    public SubjectSelectionFragment()
    {

    }

    public static SubjectSelectionFragment newInstance(String param1, String param2) {
        SubjectSelectionFragment fragment = new SubjectSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        CURRENT_TAG = TAG_SUBJECT_SELECTION_FRAME;
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Please Wait...");
        pDialog.setCancelable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidepDialog();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subject_selection, container, false);
        mView = rootView;
        getDialog().setTitle("Simple Dialog");

        getDiscountDetails();
        return rootView;
    }

    private void getDiscountDetails()
    {
        showpDialog();
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);
        final String courseId = getArguments().getString("courseId");
        final String subjectId = getArguments().getString("subjectId");

        JsonArrayRequest getDiscountDetailsRequest = new JsonArrayRequest(
                Request.Method.GET,
                Config.GET_DISCOUNTS,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try
                        {
                            for(int i=0;i<response.length();i++){
                                JSONObject student = response.getJSONObject(i);
                                String start_range = student.getString("start_range");
                                String end_range = student.getString("end_range");
                                String percent = student.getString("percent");
                                String start_date = student.getString("start_date");
                                String end_date = student.getString("end_date");

                                List<String> row = new ArrayList<>();
                                row.add(start_range);
                                row.add(end_range);
                                row.add(percent);
                                row.add(start_date);
                                row.add(end_date);
                                discountMatrix.add(row);
                            }
                            subjectListBuilder(subjectId, courseId);
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                    }
                }
        );

        queue.add(getDiscountDetailsRequest);
    }

    private void subjectListBuilder(final String subjectId, final String courseId)
    {
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);
        final TextView totalText = (TextView) mView.findViewById(R.id.totalText);
        final TextView totalAmount = (TextView) mView.findViewById(R.id.totalAmount);
        final TextView discountedAmount = (TextView) mView.findViewById(R.id.discountedAmount);
        final TextView percentOffText = (TextView) mView.findViewById(R.id.percentOffText);
        final Button bookSummaryButton = (Button) mView.findViewById(R.id.buyButton);
        final ListView listView = (ListView) mView.findViewById(R.id.list);

        final List<SubjectSelectionListRow> subjectSelectionListRowList = new ArrayList<SubjectSelectionListRow>();
        final SubjectSelectionListAdapter adapter = new SubjectSelectionListAdapter(getActivity(), subjectSelectionListRowList);

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        StringRequest subjectListBuilderRequest = new StringRequest(Request.Method.POST, Config.GET_SUBJECTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                List<SubjectData> cardData = null;

                Log.e(TAG,response);
                GsonBuilder builder = new GsonBuilder();
                Gson mGson = builder.create();
                if(mGson.fromJson(response, SubjectData[].class)!= null) {
                    cardData = Arrays.asList(mGson.fromJson(response, SubjectData[].class));
                }
                if(null != cardData)
                {
                    int selectedBook = 0;

                    for (int i = 0; i < cardData.size(); i++)
                    {
                        if(cardData.get(i).getId().equals(subjectId))
                        {
                            selectedBook = i;
                            break;
                        }
                    }

                    SubjectSelectionListRow subjectSelectionListRow = new SubjectSelectionListRow();
                    subjectSelectionListRow.setTitle(cardData.get(selectedBook).getBookName());
                    subjectSelectionListRow.setThumbnailUrl(cardData.get(selectedBook).getBookCover());
                    subjectSelectionListRow.setRating(cardData.get(selectedBook).getAuthorName());
                    subjectSelectionListRow.setGenre(cardData.get(selectedBook).getPrice());
                    subjectSelectionListRow.setBookId(cardData.get(selectedBook).getId());

                    Log.e(TAG,"s id "+subjectId);
                    Log.e(TAG,"s id 2 "+cardData.get(selectedBook).getId());
                    Log.e(TAG, String.valueOf(cardData.get(selectedBook).getId().equals(subjectId)));

                    if(cardData.get(selectedBook).getId().equals(subjectId))
                    {
                        listView.setItemChecked(0, true);
                        subjectSelectionListRow.setYear(true);
                        List<String> row = new ArrayList<>();
                        row.add(cardData.get(selectedBook).getId());
                        row.add(cardData.get(selectedBook).getPrice());
                        selectedBooksMatrix.add(row);
                        totalText.setVisibility(View.VISIBLE);
                        totalAmount.setVisibility(View.VISIBLE);
                        totalAmount.setText("₹"+selectedBooksMatrix.get(0).get(1));
                        bookSummaryButton.setVisibility(View.VISIBLE);

                        int totalSum = Integer.parseInt(selectedBooksMatrix.get(0).get(1));
                        float discountedSum = 0;

                        for(int i=0;i<discountMatrix.size();i++)
                        {
                            int start_range = Integer.parseInt(discountMatrix.get(i).get(0));
                            int end_range = Integer.parseInt(discountMatrix.get(i).get(1));
                            float percent = Float.parseFloat(discountMatrix.get(i).get(2));

                            if(totalSum>=start_range && totalSum<=end_range)
                            {
                                discountedSum = totalSum*(1-percent/100);
                                finalAmount = discountedSum;
                                discountedAmount.setVisibility(View.VISIBLE);
                                discountedAmount.setText(" ₹"+discountedSum);
                                percentOffText.setVisibility(View.VISIBLE);
                                percentOffText.setText("  ("+percent+"% Off)");
                                totalAmount.setPaintFlags(totalAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                        }
                    }
                    subjectSelectionListRowList.add(subjectSelectionListRow);

                    for (int i = 0; i < cardData.size(); i++)
                    {
                        if(i != selectedBook)
                        {
                            SubjectSelectionListRow subjectSelectionListRow2 = new SubjectSelectionListRow();
                            subjectSelectionListRow2.setTitle(cardData.get(i).getBookName());
                            subjectSelectionListRow2.setThumbnailUrl(cardData.get(i).getBookCover());
                            subjectSelectionListRow2.setRating(cardData.get(i).getAuthorName());
                            subjectSelectionListRow2.setGenre(cardData.get(i).getPrice());
                            subjectSelectionListRow2.setBookId(cardData.get(i).getId());
                            if (cardData.get(i).getId().equals(subjectId)) {
                                subjectSelectionListRow2.setYear(true);
                            }
                            subjectSelectionListRowList.add(subjectSelectionListRow2);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                getUserCredits(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("id", String.valueOf(courseId));
                return MyData;
            }
        };
        queue.add(subjectListBuilderRequest);
    }

    private void getUserCredits(final SubjectSelectionListAdapter adapter)
    {
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);
        final String userIdFirebase = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StringRequest getUserCreditsRequest = new StringRequest(Request.Method.POST, Config.GET_USER_CREDITS, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONArray jsonarray = new JSONArray(response);
                    for(int i=0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        Float availableCredits = Float.valueOf(jsonobject.getString("availableCredits"));
                        useCreditsAndContinueHandler(availableCredits, adapter);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, String.valueOf(error));
            }
        }) {
            protected Map<String, String> getParams()
            {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("userIdFirebase", userIdFirebase);
                return MyData;
            }
        };
        queue.add(getUserCreditsRequest);
    }

    private float priceCalculator (float userCredits, boolean useCreditsFlag)
    {
        final TextView totalText = (TextView) mView.findViewById(R.id.totalText);
        final TextView totalAmount = (TextView) mView.findViewById(R.id.totalAmount);
        final TextView discountedAmount = (TextView) mView.findViewById(R.id.discountedAmount);
        final TextView percentOffText = (TextView) mView.findViewById(R.id.percentOffText);
        final CheckBox useCreditCheckbox = (CheckBox) mView.findViewById(R.id.useCredit);

        int totalSum = 0;
        float discountedSum = 0;
        float creditUsed = 0;

        Log.e(TAG, String.valueOf("userAvailableCredits "+userCredits));

        for(int i=0;i<selectedBooksMatrix.size();i++)
        {
            totalSum = totalSum + Integer.parseInt(selectedBooksMatrix.get(i).get(1));
        }

        totalText.setVisibility(View.VISIBLE);
        totalAmount.setVisibility(View.VISIBLE);

        for(int i=0;i<discountMatrix.size();i++)
        {
            int start_range = Integer.parseInt(discountMatrix.get(i).get(0));
            int end_range = Integer.parseInt(discountMatrix.get(i).get(1));
            float percent = Float.parseFloat(discountMatrix.get(i).get(2));

            if(totalSum>=start_range && totalSum<=end_range)
            {
                if(useCreditsFlag)
                {
                    discountedSum = totalSum * (1 - percent / 100) - userCredits;
                    if (discountedSum <= 0)
                    {
                        if((totalSum * (1 - percent / 100)) <= userCredits)
                        {
                            creditUsed = totalSum * (1 - percent / 100);
                        }
                        else
                        {
                            creditUsed = userCredits;
                        }
                        discountedSum = 0;
                        useCreditCheckbox.setText("Use Upto ₹"+String.valueOf(userCredits)+" Credits? (Used ₹"+creditUsed+" Credits)");
                    }
                    else
                    {
                        if((totalSum * (1 - percent / 100)) <= userCredits)
                        {
                            creditUsed = totalSum * (1 - percent / 100);
                        }
                        else
                        {
                            creditUsed = userCredits;
                        }
                        useCreditCheckbox.setText("Use Upto ₹"+String.valueOf(userCredits)+" Credits? (Used ₹"+creditUsed+" Credits)");
                    }
                }
                else
                {
                    discountedSum = totalSum * (1 - percent / 100);
                    useCreditCheckbox.setText("Use ₹"+String.valueOf(userCredits)+" Credits?");
                }
                finalAmount = discountedSum;
                discountedAmount.setVisibility(View.VISIBLE);
                discountedAmount.setText(" ₹"+discountedSum);
                percentOffText.setVisibility(View.VISIBLE);
                percentOffText.setText(" ("+percent+"% Off)");
                totalAmount.setText("₹"+totalSum);
                totalAmount.setPaintFlags(totalAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                totalAmount.setTypeface(null, Typeface.NORMAL);
                totalAmount.setTextColor(getResources().getColor(R.color.primary_text));
                break;
            }
            else
            {
                if(useCreditsFlag)
                {
                    finalAmount = totalSum - userCredits;
                    if (finalAmount <= 0)
                    {
                        if(totalSum <= userCredits)
                        {
                            creditUsed = totalSum;
                        }
                        else
                        {
                            creditUsed = userCredits;
                        }
                        finalAmount = 0;
                    }
                    else
                    {
                        creditUsed = userCredits;
                    }
                    useCreditCheckbox.setText("Use Upto ₹"+String.valueOf(userCredits)+" Credits? (Used ₹"+creditUsed+" Credits)");
                }
                else
                {
                    finalAmount = totalSum;
                    useCreditCheckbox.setText("Use ₹"+String.valueOf(userCredits)+" Credits?");
                }
                discountedAmount.setVisibility(View.INVISIBLE);
                discountedAmount.setText(" ₹"+discountedSum);
                percentOffText.setVisibility(View.INVISIBLE);
                percentOffText.setText(" (0% Off)");
                totalAmount.setText("₹"+finalAmount);
                totalAmount.setPaintFlags(totalAmount.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                totalAmount.setTypeface(null, Typeface.BOLD);
                totalAmount.setTextColor(getResources().getColor(R.color.primary));
            }
        }
        return creditUsed;
    }

    private void useCreditsAndContinueHandler(final Float availableCredits, SubjectSelectionListAdapter adapter)
    {
        final String courseId = getArguments().getString("courseId");
        final String subjectId = getArguments().getString("subjectId");
        final String userIdFirebase = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final Button bookSummaryButton = (Button) mView.findViewById(R.id.buyButton);
        final CheckBox useCreditCheckbox = (CheckBox) mView.findViewById(R.id.useCredit);
        final ListView listView = (ListView) mView.findViewById(R.id.list);

        final float[] creditUsed = new float[1];

        if(availableCredits > 0)
        {
            useCreditCheckbox.setVisibility(View.VISIBLE);
            useCreditCheckbox.setText("Use ₹"+String.valueOf(availableCredits)+" Credits?");
        }
        else
        {
            useCreditCheckbox.setVisibility(View.GONE);
        }

        useCreditCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
             @Override
             public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
             {
                 creditUsed[0] = priceCalculator(availableCredits, useCreditCheckbox.isChecked());
             }
        }
        );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckBox bookSelection = (CheckBox) view.findViewById(R.id.bookSelection);
                String bookId = ((TextView) view.findViewById(R.id.bookId)).getText().toString();
                String bookPrice = ((TextView) view.findViewById(R.id.bookPrice)).getText().toString();

                bookSelection.setChecked(listView.isItemChecked(position));

                SparseBooleanArray positions = listView.getCheckedItemPositions();
                Log.e("TAG", "onItemSelected: " + positions.toString());

                if(listView.getCheckedItemCount()>0)
                {
                    bookSummaryButton.setVisibility(View.VISIBLE);
                    bookSummaryButton.setClickable(true);
                    bookSummaryButton.getBackground().clearColorFilter();
                }
                else
                {
                    bookSummaryButton.setClickable(false);
                    bookSummaryButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                }

                bookPrice  = bookPrice.replaceAll("[^0-9]", "");

                if(listView.isItemChecked(position)) {
                    List<String> row = new ArrayList<>();
                    row.add(bookId);
                    row.add(bookPrice);
                    selectedBooksMatrix.add(row);
                }

                if(!listView.isItemChecked(position)) {
                    List<String> row = new ArrayList<>();
                    row.add(bookId);
                    row.add(bookPrice);
                    selectedBooksMatrix.remove(row);
                }
                creditUsed[0] = priceCalculator(availableCredits, useCreditCheckbox.isChecked());
            }
        });

        bookSummaryButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                final String status;
                if(finalAmount==0)
                {
                    status = "purchase";
                }
                else
                {
                    showpDialog();
                    status = "checkout";
                }

                initiateTransaction(subjectId, courseId, userIdFirebase, finalAmount, status, creditUsed[0]);
            }
        });
        hidepDialog();
    }

    private void initiateTransaction(final String subjectId, final String courseId, final String userIdFirebase, final Float amount, final String status, final Float creditUsed)
    {
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);

        StringRequest movieReq = new StringRequest(Request.Method.POST, Config.TRANSACTION_RECORD_ENTRY, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, String.valueOf(response));

                try
                {
                    JSONArray jsonarray = new JSONArray(response);
                    JSONObject student = jsonarray.getJSONObject(0);

                    final String orderId = student.getString("id");
                    final String invoiceId = student.getString("invoice_id");
                    final String userId = student.getString("user_id");
                    final String amount = student.getString("amount");
                    final String email = student.getString("email");
                    final String phoneNumber = student.getString("phone_number");

                    if(Float.parseFloat(amount) > 0)
                    {
                        generatePaytmChecksum(invoiceId, userId, amount, email, phoneNumber);
                    }
                    else
                    {
                        getDialog().dismiss();
                        Fragment fragment = new OrderConfirmationFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                        Bundle bundle2 = new Bundle();
                        bundle2.putString("ORDERID", invoiceId);
                        bundle2.putString("STATUS", "SUCCESS");
                        fragment.setArguments(bundle2);

                        fragmentTransaction.replace(R.id.frame, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commitAllowingStateLoss();
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("userIdFirebase", userIdFirebase);
                MyData.put("amount", String.valueOf(amount));
                MyData.put("status", String.valueOf(status));
                MyData.put("creditsUsed", Float.toString(creditUsed));
                JSONArray finalArray = new JSONArray(); // create your jsonarray

                for (int i = 0; i < selectedBooksMatrix.size(); i++) {

                    try
                    {
                        JSONObject JSONcontacts = new JSONObject();
                        JSONcontacts.put("id",selectedBooksMatrix.get(i).get(0));
                        JSONcontacts.put("price",selectedBooksMatrix.get(i).get(1));
                        finalArray.put(JSONcontacts);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                MyData.put("matrix", finalArray.toString());
                return MyData;
            }
        };
        queue.add(movieReq);
    }

    private void generatePaytmChecksum(final String orderId, final String userId, final String amount, final String email, final String phoneNumber)
    {
        RequestQueue queue = Volley.newRequestQueue((Context) mListener);

        StringRequest generateChecksumRequest = new StringRequest(Request.Method.POST, Config.GENERATE_PAYTM_CHECKSUM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                try
                {
/*                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
                    }*/
                    JSONObject jsonObject = new JSONObject(response);
                    String ChecksumHash =  jsonObject.getString("CHECKSUMHASH");
                    initiatePaytmTransaction(orderId, userId, amount, email, phoneNumber, ChecksumHash);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("MID", Config.PAYTM_MERCHANT_ID);
                MyData.put("ORDER_ID", orderId);
                MyData.put("CUST_ID", userId);
                MyData.put("INDUSTRY_TYPE_ID", PAYTM_INDUSTRY_TYPE_ID);
                MyData.put("CHANNEL_ID", Config.PAYTM_CHANNEL_ID);
                MyData.put("TXN_AMOUNT", amount);
                MyData.put("WEBSITE", Config.PAYTM_WEBSITE);
                MyData.put( "MOBILE_NO" , phoneNumber);
                MyData.put( "EMAIL" , email);
                MyData.put( "CALLBACK_URL", PAYTM_CALLBACK_URL+orderId);
                return MyData;
            }
        };
        queue.add(generateChecksumRequest);
    }

    private void initiatePaytmTransaction(final String orderId, final String userId, final String amount, final String email, final String phoneNumber, final String ChecksumHash)
    {
        PaytmPGService PaytmService = PaytmPGService.getProductionService();

        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("MID", Config.PAYTM_MERCHANT_ID);
        paramMap.put("ORDER_ID", orderId);
        paramMap.put("CUST_ID", userId);
        paramMap.put("MOBILE_NO", phoneNumber);
        paramMap.put("EMAIL", email);
        paramMap.put("CHANNEL_ID", Config.PAYTM_CHANNEL_ID);
        paramMap.put("TXN_AMOUNT", amount);
        paramMap.put("WEBSITE", Config.PAYTM_WEBSITE);
        paramMap.put("INDUSTRY_TYPE_ID", PAYTM_INDUSTRY_TYPE_ID);
        paramMap.put("CALLBACK_URL", PAYTM_CALLBACK_URL + orderId);
        paramMap.put("CHECKSUMHASH", ChecksumHash);

        PaytmOrder order = new PaytmOrder(paramMap);
        PaytmService.initialize(order, null);

        hidepDialog();

        //finally starting the payment transaction
        PaytmService.startPaymentTransaction(getActivity(), true, true, new PaytmPaymentTransactionCallback()
        {

            public void someUIErrorOccurred(String inErrorMessage)
            {
                Toast.makeText(getContext(), "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
            }

            public void onTransactionResponse(Bundle inResponse)
            {
                Log.e(TAG, String.valueOf(inResponse));

                //Toast.makeText(getContext(), "Please Wait while we ready your order!", Toast.LENGTH_LONG).show();
                String STATUS = inResponse.getString("STATUS");
                String CHECKSUMHASH = inResponse.getString("CHECKSUMHASH");
                String BANKNAME = inResponse.getString("BANKNAME");
                String ORDERID = inResponse.getString("ORDERID");
                String TXNAMOUNT = inResponse.getString("TXNAMOUNT");
                String TXNDATE = inResponse.getString("TXNDATE");
                String MID = inResponse.getString("MID");
                String TXNID = inResponse.getString("TXNID");
                String RESPCODE = inResponse.getString("RESPCODE");
                String PAYMENTMODE = inResponse.getString("PAYMENTMODE");
                String BANKTXNID = inResponse.getString("BANKTXNID");
                String CURRENCY = inResponse.getString("CURRENCY");
                String GATEWAYNAME = inResponse.getString("GATEWAYNAME");
                String RESPMSG = inResponse.getString("RESPMSG");

                verifyPaytmTransaction(STATUS, CHECKSUMHASH, BANKNAME, orderId, amount, TXNDATE, Config.PAYTM_MERCHANT_ID, TXNID, RESPCODE, PAYMENTMODE, BANKTXNID, CURRENCY, GATEWAYNAME, RESPMSG);

            }

            public void networkNotAvailable()
            {
                Toast.makeText(getContext(), "Network connection error. Please check your internet connectivity!", Toast.LENGTH_LONG).show();
            }

            public void clientAuthenticationFailed(String inResponse)
            {
                Toast.makeText(getContext(), "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inResponse, String inFailingUrl)
            {
                Toast.makeText(getContext(), "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
            }

            public void onBackPressedCancelTransaction()
            {
                Toast.makeText(getContext(), "Transaction cancelled. Please try again to continue with your order!", Toast.LENGTH_LONG).show();
            }

            public void onTransactionCancel(String inErrorMessage, Bundle inResponse)
            {
                Log.e(TAG, "OTRRRR"+String.valueOf(inResponse));

            }
        });
    }

    private void verifyPaytmTransaction(final String STATUS, final String CHECKSUMHASH, final String BANKNAME, final String ORDERID, final String TXNAMOUNT, final String TXNDATE, final String MID, final String TXNID, final String RESPCODE, final String PAYMENTMODE, final String BANKTXNID, final String CURRENCY, final String GATEWAYNAME, final String RESPMSG)
    {
        showpDialog();
        RequestQueue queue = Volley.newRequestQueue((Context) mListener);

        StringRequest verifyPaytmChecksumRequest = new StringRequest(Request.Method.POST, Config.VERIFY_PAYTM_CHECKSUM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG,"as");
                try
                {
                    JSONObject jsonObject = new JSONObject(response);

                    String orderId =  jsonObject.getString("ORDERID");
                    String paytmStatus =  jsonObject.getString("STATUS");
                    String transactionAmount =  jsonObject.getString("TXNAMOUNT");
                    String transactionId =  jsonObject.getString("TXNID");

                    Float TransactionAmountPassed= Float.parseFloat(TXNAMOUNT);
                    Float TransactionAmountReceived =  Float.parseFloat(transactionAmount);

                    updateTransaction(orderId, paytmStatus, transactionAmount, transactionId);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("ORDERID", ORDERID);
                MyData.put("MID", MID);
                return MyData;
            }
        };
        queue.add(verifyPaytmChecksumRequest);
    }

    private void updateTransaction(final String orderId, final String paytmStatus, final String transactionAmount, final String transactionId)
    {
        final RequestQueue queue = Volley.newRequestQueue((Context) mListener);

        StringRequest movieReq = new StringRequest(Request.Method.POST, Config.TRANSACTION_RECORD_UPDATE, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, String.valueOf(response));

                getDialog().dismiss();
                hidepDialog();
                Fragment fragment = new OrderConfirmationFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                Bundle bundle2 = new Bundle();
                bundle2.putString("ORDERID", orderId);
                bundle2.putString("STATUS", String.valueOf(response));
                fragment.setArguments(bundle2);

                fragmentTransaction.replace(R.id.frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commitAllowingStateLoss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("orderId", orderId);
                MyData.put("paytmStatus", paytmStatus);
                MyData.put("transactionAmount", transactionAmount);
                MyData.put("transactionId", transactionId);
                return MyData;
            }
        };
        queue.add(movieReq);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}