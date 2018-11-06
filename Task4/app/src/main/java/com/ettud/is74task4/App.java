package com.ettud.is74task4;

import com.ettud.is74task4.Models.MyObjectBox;
import com.jakewharton.threetenabp.AndroidThreeTen;

import io.objectbox.BoxStore;
import io.objectbox.ModelBuilder;

public class App extends android.app.Application {
    private static App sApp;
    private BoxStore mBoxStore;

    @Override
    public void onCreate(){
        super.onCreate();
        sApp = this;
        AndroidThreeTen.init(this);
        mBoxStore = MyObjectBox.builder().androidContext(this).build();
    }

    public static App getApp() {
        return sApp;
    }

    public BoxStore getBoxStore() {
        return mBoxStore;
    }
}
