package com.ettud.is74task2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mBatteryButton;
    private Button mBatteryGeopositionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBatteryButton = (Button) findViewById(R.id.button_battery);
        mBatteryGeopositionButton = (Button) findViewById(R.id.button_geoposition);
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
    }
}
