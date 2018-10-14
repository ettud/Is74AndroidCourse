package com.ettud.is74ltask1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String Tag = "LifeCycleLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(Tag, "onCreate is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast toast = Toast.makeText(getApplicationContext(), "40 минут уже прошло", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onStart() {
        Log.v(Tag, "onStart is called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.v(Tag, "onResume is called");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v(Tag, "onPause is called");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(Tag, "onStop is called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(Tag, "onDestroy is called");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.v(Tag, "onRestart is called");
        super.onRestart();
    }
}
