package com.android.provision;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.internal.app.LocalePicker;
import com.android.provision.activitymanager.MyActivityManager;
import com.android.provision.language.WheelView;
import com.android.provision.wifi.WifiConnectSettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Function Entrance activity
 * Created by lei.zhang on 2017/6/13
 * version 0.1
 */
public class DefaultActivity extends Activity {
    private static final String TAG = DefaultActivity.class.getSimpleName();
    LocalePicker mLocalePicker;
    private WheelView wva;
    private TextView titleTv;
    private Button nextBtn;
    ArrayList<String> localeList;
    ArrayList<String> languagesname;
    List<LocalePicker.LocaleInfo> mlist;
    private static int defaultItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyActivityManager.getInstance().addActivity(this);
        wva = (WheelView) findViewById(R.id.main_wv);
        titleTv = (TextView)findViewById(R.id.select_language_title);
        nextBtn = (Button)findViewById(R.id.next);

        mLocalePicker = new LocalePicker();
        mlist = mLocalePicker.getAllAssetLocales(this, false);
        languagesname = new ArrayList<>();
        for ( LocalePicker.LocaleInfo info : mlist) {
            languagesname.add(info.toString());
        }
        wva.setOffset(1);
        wva.setItems(languagesname);
        wva.setSeletion(1);
        wva.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                defaultItem = selectedIndex - 1;
//                changeAppLanguage();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocalePicker.updateLocale(mlist.get(defaultItem).getLocale());
                startActivity(new Intent(DefaultActivity.this, WifiConnectSettingsActivity.class));
            }
        });
    }

    public void changeAppLanguage(){
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (language.equals("zh")) {
            conf.locale = Locale.SIMPLIFIED_CHINESE;
        } else if (language.equals("en")){
            conf.locale = Locale.ENGLISH;
        }else {
            conf.locale = Locale.ENGLISH;
        }
        res.updateConfiguration(conf, dm);
    }
}