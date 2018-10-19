package com.ettud.is74task2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class BatteryActivity extends AppCompatActivity {

    private TextView mTextBatteryLevel;
    private BroadcastReceiver mBatteryChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        mTextBatteryLevel = (TextView) findViewById(R.id.text_battery_level);
        mBatteryChangeReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                runOnUiThread(() -> {
                    if(mTextBatteryLevel != null) {
                        if (level != -1) {
                            mTextBatteryLevel.setText(String.valueOf(level) + "%");
                        } else {
                            mTextBatteryLevel.setText("???");
                        }
                        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
                            mTextBatteryLevel.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else{
                            mTextBatteryLevel.setTextColor(Color.BLACK);
                        }
                    }
                });
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBatteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBatteryChangeReceiver);
    }
}
