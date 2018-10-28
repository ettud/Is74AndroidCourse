package com.ettud.is74task3.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ettud.is74task3.R;

import static android.util.TypedValue.applyDimension;

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
