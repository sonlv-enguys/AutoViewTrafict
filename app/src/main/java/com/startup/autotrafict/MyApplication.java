package com.startup.autotrafict;

import android.app.Application;

import com.startup.autotrafict.db.DbSupport;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DbSupport.init(this);
    }

}


