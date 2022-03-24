package info.ascetx.flashlight.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import info.ascetx.flashlight.R;
import info.ascetx.flashlight.app.ColorSeekBar;
import info.ascetx.flashlight.app.FALogEvents;
import info.ascetx.flashlight.app.nativeAds.NativeAdManager;

import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;

public class ScreenLightActivity extends AppCompatActivity {

    private ImageView btnGoToFlash;
    private RelativeLayout rlScreenLight;
    private ColorSeekBar mColorSeekBar;
    private boolean touchToggle = false;

    private FirebaseAnalytics mFirebaseAnalytics;
    private FALogEvents faLogEvents;
    private NativeAdManager nativeAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_screen_light, null);
        view.setKeepScreenOn(true);
        setContentView(view);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        faLogEvents = new FALogEvents(mFirebaseAnalytics);

        final float[] marginLeft = new float[1];
        final float[] marginTop = new float[1];

        btnGoToFlash = (ImageView) findViewById(R.id.btnGoToFlash);
        rlScreenLight = findViewById(R.id.rl_screen_light);
        mColorSeekBar = findViewById(R.id.colorSlider);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int pixel_width =  metrics.widthPixels;
        final int pixel_height = metrics.heightPixels;
        float screen_density =getResources().getDisplayMetrics().density; // density factor: 0.75, 1, 1.5, 2.75
        //logError("width px:"+pixel_width+" height px:"+pixel_height+" screen density:"+screen_density);

        int barHeight = pixel_width/65;

        mColorSeekBar.setColorSeeds(R.array.text_colors);
        mColorSeekBar.setBarHeightPx(barHeight);
        mColorSeekBar.setPadding(barHeight, 0, barHeight, 0);;
        mColorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
                rlScreenLight.setBackgroundColor(mColorSeekBar.getColor());
            }
        });

        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = 1F;
        getWindow().setAttributes(lp);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnGoToFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG SLA", "button click - screen transition");
                faLogEvents.logButtonClickEvent("screen_light_transition");
                adjustBrightness();
                nativeAdManager.showScreenTransitionAd();
            }
        });

        ViewTreeObserver vto = btnGoToFlash.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {

                marginLeft[0] = (float) (pixel_width/4.0 - btnGoToFlash.getMeasuredWidth()/2);
                marginTop[0] = (float) ((float) (pixel_height - pixel_width)/2 + pixel_width/4.0 - btnGoToFlash.getMeasuredHeight()/2);
//                Log.e("ScreenLightActivity", "vto marginLeft: " + marginLeft[0] + " vto marginTop: " + marginTop[0]);
//                setIVLPMTMB(btnGoToFlash, marginTop[0], marginLeft[0]);
//                setIVLPWH(btnGoToFlash, (float) (pixel_width/5.0));

                return true;
            }
        });

        rlScreenLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButtons();
            }
        });
    }

    private void adjustBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = BRIGHTNESS_OVERRIDE_NONE;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.e("TAG ScreenLightActivity", "back press- screen transition");
        faLogEvents.logButtonClickEvent("screen_light_transition");
        adjustBrightness();
        nativeAdManager.showScreenTransitionAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        faLogEvents.logScreenViewEvent("Screen Light", "ScreenLightActivity");
        nativeAdManager = new NativeAdManager(this);
        nativeAdManager.loadNativeAd();
    }

    @Override
    protected void onDestroy() {
        nativeAdManager.destroyNativeAd();
        super.onDestroy();
    }

    private void setIVLPMTMB(ImageView view, float top, float left) {
        top = Math.round(top);
        left = Math.round(left);
//        Log.e("ScreenLightActivity", "top: " + top + " left: " +left);
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginParams.setMargins((int) left, (int) top, 0, 0);
//        logError("Height:"+view.getHeight()+" Width:"+view.getWidth());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        view.setLayoutParams(layoutParams);
    }

    private void setIVLPWH(ImageView view, float wh) {
        wh = Math.round(wh);
        ViewGroup.LayoutParams view_lp = view.getLayoutParams();
        view_lp.width = (int) wh; // Specify in pixels
        view_lp.height = (int) wh; // Specify in pixels
        view.setLayoutParams(view_lp);
    }

    private void toggleButtons(){
        if(touchToggle){
            btnGoToFlash.setVisibility(View.GONE);
            mColorSeekBar.setVisibility(View.GONE);
            touchToggle = false;
        } else {
            btnGoToFlash.setVisibility(View.VISIBLE);
            mColorSeekBar.setVisibility(View.VISIBLE);
            touchToggle = true;
        }
    }
}
