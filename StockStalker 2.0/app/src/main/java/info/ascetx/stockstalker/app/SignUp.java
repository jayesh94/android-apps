package info.ascetx.stockstalker.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import info.ascetx.stockstalker.activity.SignupActivity;
import info.ascetx.stockstalker.dbhandler.DatabaseHandler;
import info.ascetx.stockstalker.dbhandler.LoginHandler;


/**
 * Created by JAYESH on 22-06-2017.
 */

public class SignUp {
    private DatabaseHandler db;
    private static LoginHandler dbl;
    private Activity activity;
    private SessionManager session;
    private ProgressDialog pDialog;

    public SignUp(Activity activity) {
        this.activity = activity;
        // Progress dialog
        pDialog = new ProgressDialog(this.activity);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        dbl = new LoginHandler(activity);
        db = new DatabaseHandler(activity);
        session = new SessionManager(activity);
        signUp();
    }

    private void signUp() {
        session.setLogin(false);
        session.setFirstLoginReg(false);
        showDialog();
        dbl.deleteUsers();
        hideDialog();
        // Launching the login activity
        Intent intent = new Intent(activity, SignupActivity.class);
        // To start a new task by clearing all activity stack and making login activity the root activity
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
//        activity.finish();
    }


    private void showDialog() {

        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (this.activity.isDestroyed()) {
            return;
        }
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
