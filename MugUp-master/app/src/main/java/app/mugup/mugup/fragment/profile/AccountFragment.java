package app.mugup.mugup.fragment.profile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import app.mugup.mugup.R;
import app.mugup.mugup.activity.FirstActivity;

import static app.mugup.mugup.activity.FirstActivity.googleSignInAccount;
import static app.mugup.mugup.activity.FirstActivity.mGoogleApiClient;
import static app.mugup.mugup.activity.FirstActivity.mGoogleSignInClient;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AccountFragment extends Fragment {

    private static final String TAG = AccountFragment.class.getSimpleName();
    
    private OnFragmentInteractionListener mListener;

    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, remove, signOut;
    private View mView;
    private EditText oldEmail, newEmail, password, newPassword;
    private ProgressBar progressBar;
    private ScrollView emailAccountLayout;

    private Button btnSignOut, btnRevokeAccess;
    private ScrollView googleAccountLayout;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail, txtPhoneNumber, txtPhoneNumber2, txtName2, txtEmail2;

    private ListView dataList;
    private RatingBar ratingBar;

    private FirebaseAuth auth;
    private FirebaseUser user;
    
    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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

        // Fragment locked in landscape screen orientation
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Fragment screen orientation normal both portait and landscape
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        mView = view ;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnChangeEmail = (Button) view.findViewById(R.id.change_email_button);
        btnChangePassword = (Button) view.findViewById(R.id.change_password_button);
        btnSendResetEmail = (Button) view.findViewById(R.id.sending_pass_reset_button);
//        btnRemoveUser = (Button) view.findViewById(R.id.remove_user_button);
        changeEmail = (Button) view.findViewById(R.id.changeEmail);
        changePassword = (Button) view.findViewById(R.id.changePass);
        sendEmail = (Button) view.findViewById(R.id.send);
        remove = (Button) view.findViewById(R.id.remove);
        signOut = (Button) view.findViewById(R.id.sign_out);
        emailAccountLayout = (ScrollView) view.findViewById(R.id.emailAccount);
        oldEmail = view.findViewById(R.id.old_email);
        newEmail = view.findViewById(R.id.new_email);
        password = view.findViewById(R.id.password);
        newPassword = view.findViewById(R.id.newPassword);

        btnSignOut = view.findViewById(R.id.btn_sign_out);
//        btnRevokeAccess = view.findViewById(R.id.btn_revoke_access);
        googleAccountLayout = view.findViewById(R.id.googleAccount);
        imgProfilePic = view.findViewById(R.id.imgProfilePic);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtName2 = view.findViewById(R.id.txtName2);
        txtEmail2 = view.findViewById(R.id.txtEmail2);
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber);
        txtPhoneNumber2 = view.findViewById(R.id.txtPhoneNumber2);

        String loginType = "";

        for (UserInfo user: user.getProviderData()) {
            Log.e(TAG, "getProviderId: " + user.getProviderId());
            if (user.getProviderId().equals("google.com")) {
                loginType = "google";
                break;
            } else if (user.getProviderId().equals("password")) {
                loginType = "email";
                break;
            } else if (user.getProviderId().equals("facebook.com")) {
                loginType = "facebook";
                break;
            }
        }

        if (loginType.equals("google")) {
            googleAccountLayout.setVisibility(View.VISIBLE);
            emailAccountLayout.setVisibility(View.GONE);

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

            /*btnRevokeAccess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    revokeAccess();
                }
            });*/

            String personName = user.getDisplayName();
            String personPhotoUrl = user.getPhotoUrl().toString();
            String email = user.getEmail();

            Log.e(TAG, "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);

            txtName.setText(personName);
            txtEmail.setText(email);
            txtPhoneNumber.setText(user.getPhoneNumber());
            Glide.with(getContext())
                    .load(personPhotoUrl)
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(imgProfilePic);

        } else if (loginType.equals("email")) {

            googleAccountLayout.setVisibility(View.GONE);
            emailAccountLayout.setVisibility(View.VISIBLE);

            oldEmail.setVisibility(View.GONE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.GONE);
            remove.setVisibility(View.GONE);

            progressBar = view.findViewById(R.id.progressBar);

            txtName2.setText(user.getDisplayName());
            txtEmail2.setText(user.getEmail());
            txtPhoneNumber2.setText(user.getPhoneNumber());

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            btnChangeEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oldEmail.setVisibility(View.GONE);
                    newEmail.setVisibility(View.VISIBLE);
                    password.setVisibility(View.GONE);
                    newPassword.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.VISIBLE);
                    changePassword.setVisibility(View.GONE);
                    sendEmail.setVisibility(View.GONE);
                    remove.setVisibility(View.GONE);
                }
            });

            changeEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (user != null && !newEmail.getText().toString().trim().equals("")) {
                        user.updateEmail(newEmail.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getContext(), "Failed to update email!", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    } else if (newEmail.getText().toString().trim().equals("")) {
                        newEmail.setError("Enter email");
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oldEmail.setVisibility(View.GONE);
                    newEmail.setVisibility(View.GONE);
                    password.setVisibility(View.GONE);
                    newPassword.setVisibility(View.VISIBLE);
                    changeEmail.setVisibility(View.GONE);
                    changePassword.setVisibility(View.VISIBLE);
                    sendEmail.setVisibility(View.GONE);
                    remove.setVisibility(View.GONE);
                }
            });

            changePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (user != null && !newPassword.getText().toString().trim().equals("")) {
                        if (newPassword.getText().toString().trim().length() < 6) {
                            newPassword.setError("Password too short, enter minimum 6 characters");
                            progressBar.setVisibility(View.GONE);
                        } else {
                            user.updatePassword(newPassword.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                                                signOut();
                                                progressBar.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(getContext(), "Failed to update password!", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    } else if (newPassword.getText().toString().trim().equals("")) {
                        newPassword.setError("Enter password");
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

            btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oldEmail.setVisibility(View.VISIBLE);
                    newEmail.setVisibility(View.GONE);
                    password.setVisibility(View.GONE);
                    newPassword.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.GONE);
                    changePassword.setVisibility(View.GONE);
                    sendEmail.setVisibility(View.VISIBLE);
                    remove.setVisibility(View.GONE);
                }
            });

            sendEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (!oldEmail.getText().toString().trim().equals("")) {
                        auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Reset password email is sent!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getContext(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    } else {
                        oldEmail.setError("Enter email");
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

            /*btnRemoveUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (user != null) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getContext(), FirstActivity.class);
                                            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getContext(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            });*/

            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signOut();
                }
            });
        }
        return view;
    }

    private void revokeAccess() {
            googleSignInAccount = null;
        FirebaseAuth.getInstance().signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(getActivity(),
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getActivity(), FirstActivity.class);
                        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                });
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
        txtEmail.setText(user.getEmail());
        txtName2.setText(user.getDisplayName());
        txtEmail2.setText(user.getEmail());
        txtPhoneNumber.setText(user.getPhoneNumber());
        txtPhoneNumber2.setText(user.getPhoneNumber());
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
}
