package app.mugup.mugup.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.mugup.mugup.activity.FirstActivity;
import app.mugup.mugup.R;
import app.mugup.mugup.adapter.CustomProfileListAdapter;
import app.mugup.mugup.helper.ProfileList;

import static app.mugup.mugup.MainActivity.TAG_ABOUT_FRAME;
import static app.mugup.mugup.MainActivity.TAG_ACCOUNT_FRAME;
import static app.mugup.mugup.MainActivity.TAG_ORDER_HISTORY_FRAME;
import static app.mugup.mugup.MainActivity.TAG_REFER_FRAME;
import static app.mugup.mugup.activity.FirstActivity.googleSignInAccount;
import static app.mugup.mugup.activity.FirstActivity.mGoogleApiClient;
import static app.mugup.mugup.activity.FirstActivity.mGoogleSignInClient;
import static app.mugup.mugup.app.Config.URL_GET_USER_CREDITS;
import static app.mugup.mugup.app.Config.URL_GET_USER_CREDITS_REFERRAL;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnProfileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private Button btnSignOut;
    private ImageView imgProfilePic;
    private TextView txtName;

    private ListView dataList;
    private RatingBar ratingBar;
    private String referralCredits;
    private ProgressDialog pDialog;
    private View mView;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private OnProfileFragmentInteractionListener mListener;

    ArrayList<ProfileList> profileListArray;
    CustomProfileListAdapter customProfileListAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Fragment locked in portrait screen orientation
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        profileListArray = new ArrayList<ProfileList>();

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mView = view;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        ratingBar = view.findViewById(R.id.rating);
        dataList = view.findViewById(R.id.list);
        btnSignOut = view.findViewById(R.id.bt_sign_out);
        imgProfilePic = view.findViewById(R.id.imageView1);
        txtName = view.findViewById(R.id.tv_full_name);

        // add image and text in arraylist
        profileListArray.add(new ProfileList(R.drawable.ic_account_circle_red_24dp, "Account", ""));
        profileListArray.add(new ProfileList(R.drawable.ic_credit_card_red_24dp, "Credits", ""));
        profileListArray.add(new ProfileList(R.drawable.ic_share_red_24dp, "Refer & Earn", ""));
        profileListArray.add(new ProfileList(R.drawable.ic_history_red_24dp, "Order History", ""));
        profileListArray.add(new ProfileList(R.drawable.ic_chat_red_24dp, "Support", ""));
        profileListArray.add(new ProfileList(R.drawable.ic_info_red_24dp, "About", ""));
        // add data in contact image adapter
        customProfileListAdapter = new CustomProfileListAdapter(getContext(), R.layout.list_profile, profileListArray);
        dataList.setAdapter(customProfileListAdapter);

        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Object item = adapter.getItemAtPosition(position);
                ProfileList profileList = (ProfileList) item;

                switch (profileList.getName()){
                    case "Account":
                        mListener.onProfileFragmentInteraction(TAG_ACCOUNT_FRAME);
                        break;
                    case "Credits":
                        break;
                    case "Refer & Earn":
                        mListener.onProfileFragmentInteraction(TAG_REFER_FRAME);
                        break;
                    case "Order History":
                        mListener.onProfileFragmentInteraction(TAG_ORDER_HISTORY_FRAME);
                        break;
                    case "Support":
                        sendFeedback(getContext());
                        break;
                    case "About":
                        mListener.onProfileFragmentInteraction(TAG_ABOUT_FRAME);
                        break;
                }
            }
        });

        getUserCredits();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String rateValue = String.valueOf(ratingBar.getRating());
                Toast.makeText(getContext(), "Rate for Module is: "+rateValue , Toast.LENGTH_LONG).show();
            }
        });

        if (googleSignInAccount != null) {

            btnSignOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                    // Google sign out
                    mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(),
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.e(TAG, "G Sign Out");
                                }
                            });
                }
            });

            GoogleSignInAccount acct = googleSignInAccount;

            Log.e(TAG, "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            String personPhotoUrl = acct.getPhotoUrl().toString();
            String email = acct.getEmail();

            Log.e(TAG, "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);

            txtName.setText(personName);
            Glide.with(getContext())
                    .load(personPhotoUrl)
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imgProfilePic);
        } else {

            txtName.setText(user.getDisplayName());

            btnSignOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                }
            });
        }
        return view;
    }

    private void getUserCredits(){
        showpDialog();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringRequest sr = new StringRequest(Request.Method.POST, URL_GET_USER_CREDITS, new Response.Listener<String>() {
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
                        View view = getViewByPosition(1,dataList);
                        TextView tv = view.findViewById(R.id.txtCredits);
                        tv.setText(getString(R.string.rupees,error_msg));
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

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mugupllp@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from MugUp App");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

    //sign out method
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();

        txtName.setText(user.getDisplayName());
        try {
            Glide.with(getContext())
                    .load(user.getPhotoUrl().toString())
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imgProfilePic);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onProfileFragmentInteraction("Fragment TAG");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileFragmentInteractionListener) {
            mListener = (OnProfileFragmentInteractionListener) context;
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
    public interface OnProfileFragmentInteractionListener {
        // TODO: Update argument type and name
        void onProfileFragmentInteraction(String tagAccountFrame);
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
