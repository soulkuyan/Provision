package com.android.provision.myapplication;

import android.app.Application;

import com.android.provision.wifi.utils.WifiAdminUtils;

/**
 * Created by lei.zhang on 2017/6/22.
 */

public class MyApplication extends Application {
    public WifiAdminUtils mWifiAdmin;
    @Override
    public void onCreate() {
        super.onCreate();
        mWifiAdmin = new WifiAdminUtils(getApplicationContext());
    }
}
