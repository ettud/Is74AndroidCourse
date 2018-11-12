package com.ettud.is74task4.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.ettud.is74task4.R;

public class MainActivity extends AppCompatActivity {

    private Button mBatteryButton;
    private Button mBatteryGeopositionButton;
    private Button mDictionaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBatteryButton = (Button) findViewById(R.id.button_battery);
        mBatteryGeopositionButton = (Button) findViewById(R.id.button_geoposition);
        mDictionaryButton = (Button) findViewById(R.id.button_dictionary);
        mBatteryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BatteryActivity.class);
            startActivity(intent);
        });
        mBatteryGeopositionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GeopositionActivity.class);
            startActivity(intent);
        });
        mDictionaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DictionaryActivity.class);
            startActivity(intent);
        });
    }
}
