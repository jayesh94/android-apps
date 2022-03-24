package info.ascetx.flashlight.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import info.ascetx.flashlight.MainActivity;
import info.ascetx.flashlight.R;

public class LaunchPlayStoreConfirmationDialog extends Dialog {

    private final MainActivity activity;

    public LaunchPlayStoreConfirmationDialog(final MainActivity activity) {
        super(activity);
        this.activity = activity;
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                activity.faLogEvents.logScreenViewEvent("LaunchPlayStoreConfirmationDialog", "LaunchPlayStoreConfirmationDialog");
            }
        });

        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.new_app_dialog);

        setLaterButton();
        setYesButton();
    }

    private void setYesButton() {

        Button yes = findViewById(R.id.button_yes);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.faLogEvents.logButtonClickEvent("launch_play_store_dialog_yes");
                dismiss();
                final Uri marketUri = Uri.parse(Config.qrScannerPlaystoreUrl);
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, marketUri));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, "Couldn't find PlayStore on this device", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setLaterButton() {
        Button later = findViewById(R.id.button_later);

        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.faLogEvents.logButtonClickEvent("launch_play_store_dialog_later");
                dismiss();
            }
        });

    }
}
