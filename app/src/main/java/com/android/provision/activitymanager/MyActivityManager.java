package com.android.provision.activitymanager;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Function
 * Created by lei.zhang on 2017/7/6.
 */

public class MyActivityManager {
    private List<Activity> activityList = new LinkedList<Activity>();
    private static MyActivityManager instance;

    private MyActivityManager() {
    }
    public static MyActivityManager getInstance() {
        if (null == instance) {
            instance = new MyActivityManager();
        }
        return instance;
    }
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }
    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }
}
