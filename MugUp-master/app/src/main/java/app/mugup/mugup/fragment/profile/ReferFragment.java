package app.mugup.mugup.fragment.profile;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.mugup.mugup.R;

import static app.mugup.mugup.app.Config.URL_GET_USER_CREDITS_REFERRAL;
import static app.mugup.mugup.app.Config.URL_UPDATE_USER_REFERRED_BY_CODE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReferFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReferFragment extends Fragment {

    private static String TAG = ReferFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private String referralCode, referralCredit;
    private TextView tvCopyReferral, tvCreditMsg;
    private ProgressDialog pDialog;
    private View mView;

    public ReferFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReferFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReferFragment newInstance(String param1, String param2) {
        ReferFragment fragment = new ReferFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_refer, container, false);
        mView = view;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button inviteButton = view.findViewById(R.id.bt_invite);
        tvCopyReferral = view.findViewById(R.id.tv_referral_code);
        tvCreditMsg = view.findViewById(R.id.tv_credit_msg);

        getUserCreditsReferral();

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareReferral(getContext());
            }
        });

        tvCopyReferral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(tvCopyReferral.getText().toString());
                Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public void shareReferral(Context context) {

        String shareSubject = context.getResources().getString(R.string.share_referral_subject, FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        String shareBody = context.getResources().getString(R.string.share_referral_text, referralCode, referralCredit, getInvitationLink());
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.choose_messaging_client)));
    }

    private String getInvitationLink(){
        String playStoreLink = "https://play.google.com/store/apps/details?id=app.mugup.mugup&referrer=";
        return playStoreLink + referralCode;
    }

    private void getUserCreditsReferral(){

        showpDialog();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest sr = new StringRequest(Request.Method.POST, URL_GET_USER_CREDITS_REFERRAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, response);
                hidepDialog();
                try {

                    JSONObject json = new JSONObject(response);
                    Boolean error = json.getBoolean("error");
                    String error_msg = json.getString("error_msg");
                    if(error){
                        showSnackBar(error_msg);
                    } else {
                        JSONObject jsonMsg = new JSONObject(error_msg);
                        referralCode = jsonMsg.getString("referral_code");
                        referralCredit = jsonMsg.getString("credit");
                        tvCopyReferral.setText(referralCode);
                        tvCreditMsg.setText(getResources().getString(R.string.refer_benefit_msg, referralCredit));
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
                return params;
            }
        };
        queue.add(sr);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
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
        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        snackbar.show();

    }
}
