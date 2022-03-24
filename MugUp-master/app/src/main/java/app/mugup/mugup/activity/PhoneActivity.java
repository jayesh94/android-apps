package app.mugup.mugup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import app.mugup.mugup.MainActivity;
import app.mugup.mugup.R;
import app.mugup.mugup.app.AppController;

import static app.mugup.mugup.MainActivity.TAG_HOME_FRAME;
import static app.mugup.mugup.app.Config.URL_REGISTER_USER;
import static app.mugup.mugup.app.Config.URL_UPDATE_USER_REFERRED_BY_CODE;

public class PhoneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = PhoneActivity.class.getSimpleName();
    // Progress dialog
    private ProgressDialog pDialog;
    EditText etPhone, etOtp, etReferral;
    Button btSendOtp, btResendOtp, btVerifyOtp, btEnterReferral, btSkipReferral;
    TextView tvOtpMsg1, tvOtpMsg2, tvOtpMsg3, tvReferralMsg;
    RelativeLayout rlSendOtp, rlResendOtp;

    CountDownTimer countDownTimer;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        initFields();
//      Enter referral code received from InstallReferrerReceiver
        etReferral.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(FirstActivity.PREF_INSTALL_REFERRER_CODE,""));

        mAuth = FirebaseAuth.getInstance();

        final EditText edt = (EditText) findViewById(R.id.et_phone);

        edt.setText("+91 ");
        Selection.setSelection(edt.getText(), edt.getText().length());


        edt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("+91 ")){
                    edt.setText("+91 ");
                    Selection.setSelection(edt.getText(), edt.getText().length());

                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                    Log.w(TAG, "FirebaseAuthInvalidCredentialsException", e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Log.w(TAG, "FirebaseTooManyRequestsException", e);
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };
    }

    void initFields() {
        etPhone = findViewById(R.id.et_phone);
        etOtp = findViewById(R.id.et_otp);
        etReferral = findViewById(R.id.et_referral_code);
        btSendOtp = findViewById(R.id.bt_send_otp);
        btResendOtp = findViewById(R.id.bt_resend_otp);
        btVerifyOtp = findViewById(R.id.bt_verify_otp);
        btEnterReferral = findViewById(R.id.bt_submit_referral);
        btSkipReferral = findViewById(R.id.bt_skip_referral);
        tvOtpMsg1 = findViewById(R.id.otp_msg1);
        tvOtpMsg2 = findViewById(R.id.otp_msg2);
        tvOtpMsg3 = findViewById(R.id.otp_msg3);
        tvReferralMsg = findViewById(R.id.referral_msg);
        rlSendOtp = findViewById(R.id.rl_bt_send_otp);
        rlResendOtp = findViewById(R.id.rl_bt_resend_otp);
        btResendOtp.setOnClickListener(this);
        btVerifyOtp.setOnClickListener(this);
        btSendOtp.setOnClickListener(this);
        btEnterReferral.setOnClickListener(this);
        btSkipReferral.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send_otp:
                if (etPhone.getText().toString().trim().length() < 14) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_phone_error), Toast.LENGTH_LONG).show();
                    showSnackBar(getResources().getString(R.string.et_phone_error));
                    return;
                }
                makeOtpViewsVisible();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        etPhone.getText().toString(),         // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        this,               // Activity (for callback binding)
                        mCallbacks);        // OnVerificationStateChangedCallbacks
                break;
            case R.id.bt_resend_otp:
                if (etPhone.getText().toString().trim().length() < 14) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_phone_error), Toast.LENGTH_LONG).show();
                    showSnackBar(getResources().getString(R.string.et_phone_error));
                    return;
                }
                startOtpTimer();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        etPhone.getText().toString(),         // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        this,               // Activity (for callback binding)
                        mCallbacks,         // OnVerificationStateChangedCallbacks
                        mResendToken);      // Force Resending Token from callbacks
                break;
            case R.id.bt_verify_otp:
                if (etOtp.getText().toString().trim().length() < 6) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_otp_error), Toast.LENGTH_LONG).show();
                    showSnackBar(getResources().getString(R.string.et_otp_error));
                    return;
                }
                try {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, etOtp.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_otp_error), Toast.LENGTH_LONG).show();
                    showSnackBar(getResources().getString(R.string.et_otp_error));
                    return;
                }
                break;
            case R.id.bt_submit_referral:
                if (etReferral.getText().toString().trim().length() < 6) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.et_referral_error), Toast.LENGTH_LONG).show();
                    showSnackBar(getResources().getString(R.string.et_referral_error));
                    return;
                }
                showpDialog();
                updateUserReferByCode();
                break;
            case R.id.bt_skip_referral:
                Intent intent = new Intent(PhoneActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("fragment",TAG_HOME_FRAME);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void updateUserReferByCode(){

        showpDialog();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST, URL_UPDATE_USER_REFERRED_BY_CODE, new Response.Listener<String>() {
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
                        tvOtpMsg3.setVisibility(View.GONE);
                        tvReferralMsg.setText(error_msg);
                        btSkipReferral.setText("CONTINUE");
                        btSkipReferral.setTypeface(btSkipReferral.getTypeface(), Typeface.BOLD);
                        btEnterReferral.setAlpha(0.5f);
                        btEnterReferral.setClickable(false);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
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
                params.put("uid", mAuth.getUid());
                params.put("referred_by_code",etReferral.getText().toString().trim());
                return params;
            }
        };
        queue.add(sr);

    }

    private void makeOtpViewsVisible(){
        tvOtpMsg1.setVisibility(View.VISIBLE);
        tvOtpMsg2.setVisibility(View.VISIBLE);
        etOtp.setVisibility(View.VISIBLE);
        btVerifyOtp.setVisibility(View.VISIBLE);
        startOtpTimer();
    }

    private void startOtpTimer(){
        btSendOtp.setAlpha(0.5f);
        btSendOtp.setClickable(false);
        btResendOtp.setAlpha(0.5f);
        btResendOtp.setClickable(false);
        tvOtpMsg1.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                String text = getResources().getString(R.string.otp_verification_msg1) +" "+ millisUntilFinished / 1000;

                Spannable spannable = new SpannableString(text);

                spannable.setSpan(new ForegroundColorSpan(Color.BLUE), (getResources().getString(R.string.otp_verification_msg1) +" ").length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                tvOtpMsg1.setText(spannable, TextView.BufferType.SPANNABLE);
            }

            public void onFinish() {
                btSendOtp.setAlpha(1.0f);
                btSendOtp.setClickable(true);
                btResendOtp.setAlpha(1.0f);
                btResendOtp.setClickable(true);
                rlSendOtp.setVisibility(View.GONE);
                rlResendOtp.setVisibility(View.VISIBLE);
                tvOtpMsg1.setVisibility(View.GONE);
            }
        }.start();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.getCurrentUser().updatePhoneNumber(credential).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    Log.d(TAG, "uid: "+ mAuth.getCurrentUser().getUid());
                    Log.d(TAG, "name: "+ mAuth.getCurrentUser().getDisplayName());
                    Log.d(TAG, "email: "+ mAuth.getCurrentUser().getEmail());
                    Log.d(TAG, "number: "+etPhone.getText().toString());

                    Toast.makeText(getApplicationContext(), "Mobile Verified Successfully.", Toast.LENGTH_LONG).show();

                    countDownTimer.cancel();

                    registerUser(mAuth.getCurrentUser());

                    makeReferralViewsVisible();

                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        showSnackBar(task.getException().getMessage());
                    } else {
                        showSnackBar(task.getException().getMessage());
                        }
                }
            }
        });
    }

    private void makeReferralViewsVisible(){
        etPhone.setVisibility(View.GONE);
        rlSendOtp.setVisibility(View.GONE);
        rlResendOtp.setVisibility(View.GONE);
        tvOtpMsg1.setVisibility(View.GONE);
        tvOtpMsg2.setVisibility(View.GONE);
        etOtp.setVisibility(View.GONE);
        btVerifyOtp.setVisibility(View.GONE);

        tvOtpMsg3.setVisibility(View.VISIBLE);
        etReferral.setVisibility(View.VISIBLE);
        btEnterReferral.setVisibility(View.VISIBLE);
        btSkipReferral.setVisibility(View.VISIBLE);

    }

    private void registerUser(final FirebaseUser user){
        showpDialog();
        final String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.e(TAG,android_id);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST, URL_REGISTER_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, response);
                hidepDialog();
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
                params.put("uid",user.getUid());
                params.put("name",user.getDisplayName());
                params.put("email",user.getEmail());
                params.put("number",user.getPhoneNumber());
                params.put("device_id",android_id);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

    void showSnackBar(String msg){
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        snackbar.show();

    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
