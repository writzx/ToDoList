package com.github.writzx.todolist;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class TheApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
