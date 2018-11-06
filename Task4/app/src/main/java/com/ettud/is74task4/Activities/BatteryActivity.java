package com.ettud.is74task4.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ettud.is74task4.Presenters.BatteryPresenter;
import com.ettud.is74task4.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static android.util.TypedValue.applyDimension;

public class BatteryActivity extends AppCompatActivity {

    private TextView mTextBatteryCurrent;
    private TextView mTextBatteryTemp;
    private TextView mTextProgressBar;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mBatteryChangeReceiver;
    private BatteryPresenter mBatteryPresenter;
    private Timer mUpdateProgressBarTimer;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        mTextBatteryCurrent = (TextView) findViewById(R.id.text_battery_current);
        mTextBatteryTemp = (TextView) findViewById(R.id.text_battery_temp);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextProgressBar = (TextView)findViewById(R.id.text_progressBar);
        mImageView = (ImageView)findViewById(R.id.imageView);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            params.width = (int) applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());;
            params.height = (int) applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());;
            mImageView.setLayoutParams(params);
        }
        mBatteryPresenter = null;
        mProgressBar.setScaleY(5f);
        mUpdateProgressBarTimer = new Timer();
        mBatteryChangeReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                mBatteryPresenter = new BatteryPresenter(context, intent);
                final Double temp = mBatteryPresenter.getCelciusTemp();
                final Integer currentCurrency = mBatteryPresenter.getCurrentCurrency();
                final Integer averageCurrency = mBatteryPresenter.getAverageCurrency();
                runOnUiThread(() -> {
                    if(mTextBatteryTemp != null){
                        if(temp != null){
                            mTextBatteryTemp.setText(temp + "°C");
                        }
                        else{
                            mTextBatteryTemp.setText(getResources().getString(R.string.battery_temp_unknown));
                        }
                    }
                    if(mTextBatteryCurrent != null){
                        if (averageCurrency != null) {
                            mTextBatteryCurrent.setText(getResources().getString(R.string.battery_aver_current_label) + ": " + averageCurrency);
                        } else {
                            if (currentCurrency != null) {
                                mTextBatteryCurrent.setText(getResources().getString(R.string.battery_cur_current_label) + ": " + currentCurrency);
                            } else {
                                mTextBatteryCurrent.setText(getResources().getString(R.string.battery_current_unkown));
                            }
                        }
                    }
                });
                updateView();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBatteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        final TimerTask timerTask = new TimerTask(){
            @Override
            public void run(){
                runOnUiThread(() ->{
                    if(mTextProgressBar != null) {
                        if (mBatteryPresenter != null) {
                            final CharSequence curProgressBarText = mTextProgressBar.getText();
                            final Pattern fullInfo = Pattern.compile("^[0-9]+/[0-9]+.*$");
                            if (fullInfo.matcher(curProgressBarText).matches()) {
                                String text = "";
                                final Double percentage = mBatteryPresenter.getBatteryLevel();
                                if(percentage != null){
                                    text = percentage.toString() + "%";
                                }
                                mTextProgressBar.setText(text);
                            } else {
                                String text = "";
                                final Double currentCapacity = mBatteryPresenter.getCurrentCapacity();
                                final Double maxCapacity = mBatteryPresenter.getMaxCapacity();
                                if(currentCapacity != null &&
                                        maxCapacity != null){
                                    text = new Integer((int)currentCapacity.doubleValue()).toString() +
                                            "/" +
                                            new Integer((int)maxCapacity.doubleValue()).toString() +
                                            " mA";
                                }
                                mTextProgressBar.setText(text);
                            }
                        } else{
                            mTextProgressBar.setText("");
                        }
                    }
                });
            }
        };
        mUpdateProgressBarTimer.schedule(timerTask, 0, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBatteryChangeReceiver);
        mUpdateProgressBarTimer.purge();
    }

    private void updateView(){
        if(mProgressBar != null){
            if(mBatteryPresenter != null) {
                runOnUiThread(() -> {
                    final Boolean isCharging = mBatteryPresenter.isCharging();
                    final Double currentCapacity = mBatteryPresenter.getCurrentCapacity();
                    final Double maxCapacity = mBatteryPresenter.getMaxCapacity();
                    if (isCharging == null || mBatteryPresenter.getBatteryLevel() == null) {
                        mImageView.setImageResource(0);
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
                        mProgressBar.getProgressDrawable().setColorFilter(typedValue.data, PorterDuff.Mode.SRC_IN);
                    } else if (isCharging == true) {
                        if(mBatteryPresenter.isChargingWireless()){
                            mImageView.setImageResource(R.drawable.wireless_charge);
                        }
                        else {
                            mImageView.setImageResource(R.drawable.charge);
                        }
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
                        mProgressBar.getProgressDrawable().setColorFilter(typedValue.data, PorterDuff.Mode.SRC_IN);
                        mProgressBar.setProgress(mBatteryPresenter.getBatteryLevel().intValue());
                        mTextProgressBar.setText(mProgressBar.getProgress() + "%");
                    } else if (isCharging == false) {
                        mImageView.setImageResource(R.drawable.battery);
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                        mProgressBar.getProgressDrawable().setColorFilter(typedValue.data, PorterDuff.Mode.SRC_IN);
                        mProgressBar.setProgress(mBatteryPresenter.getBatteryLevel().intValue());
                        mTextProgressBar.setText(mProgressBar.getProgress() + "%");
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void updateImage(){

    }
}
