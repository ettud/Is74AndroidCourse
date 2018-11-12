package com.ettud.is74task4;

import com.ettud.is74task4.Models.database.MyObjectBox;

import io.objectbox.BoxStore;

public class App extends android.app.Application {
    private static App sApp;
    private BoxStore mBoxStore;

    @Override
    public void onCreate(){
        super.onCreate();
        sApp = this;
        mBoxStore = MyObjectBox.builder().androidContext(this).build();
    }

    public static App getApp() {
        return sApp;
    }

    public BoxStore getBoxStore() {
        return mBoxStore;
    }
}
