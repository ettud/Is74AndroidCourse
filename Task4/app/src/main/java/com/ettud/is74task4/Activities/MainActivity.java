package com.ettud.is74task4.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ettud.is74task4.R;

public class MainActivity extends AppCompatActivity {

    private Button mBatteryButton;
    private Button mBatteryGeopositionButton;
    private Button mWikiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBatteryButton = (Button) findViewById(R.id.button_battery);
        mBatteryGeopositionButton = (Button) findViewById(R.id.button_geoposition);
        mWikiButton = (Button) findViewById(R.id.button_wiki);
        mBatteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BatteryActivity.class);
                startActivity(intent);
            }
        });
        mBatteryGeopositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GeopositionActivity.class);
                startActivity(intent);
            }
        });
        mWikiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WikiActivity.class);
                startActivity(intent);
            }
        });
    }
}
