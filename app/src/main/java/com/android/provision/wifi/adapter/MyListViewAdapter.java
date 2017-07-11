package com.android.provision.wifi.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.provision.R;

import java.util.List;

/**
 * Function wifi listview
 * Created by lei.zhang on 2017/6/20
 * version 0.1
 */
public class MyListViewAdapter extends BaseAdapter {

    private List<ScanResult> datas;
    private Context context;
    private WifiManager mWifiManager;
    private ConnectivityManager cm;

    public void setDatas(List<ScanResult> datas) {
        this.datas = datas;
    }

    public MyListViewAdapter(Context context, List<ScanResult> datas) {
        super();
        this.datas = datas;
        this.context = context;
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public int getCount() {
        if (datas == null) {
            return 0;
        }
        return datas.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == datas.size()){
            return "";
        }else {
            return datas.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder tag = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.mylist_wifi_item, null);
            tag = new Holder();
            tag.txtWifiName = (TextView) convertView
                    .findViewById(R.id.txt_wifi_name);
            tag.txtWifiDesc = (TextView) convertView
                    .findViewById(R.id.txt_wifi_desc);
            tag.imgWifiLevelIco = (ImageView) convertView
                    .findViewById(R.id.img_wifi_level_ico);
            convertView.setTag(tag);
        }
        Holder holder = (Holder) convertView.getTag();
        if (position != datas.size()){
            holder.txtWifiName.setText(datas.get(position).SSID);
            String desc = "";
            String descOri = datas.get(position).capabilities;
            if (descOri.toUpperCase().contains("WPA-PSK")) {
                desc = "WPA";
            }
            if (descOri.toUpperCase().contains("WPA2-PSK")) {
                desc = "WPA2";
            }
            if (descOri.toUpperCase().contains("WPA-PSK")
                    && descOri.toUpperCase().contains("WPA2-PSK")) {
                desc = "WPA/WPA2";
            }
            int level = datas.get(position).level;
            if (TextUtils.isEmpty(desc)) {
                int imgId = R.drawable.ic_wifi_signal_0_dark;
                if (Math.abs(level) > 80) {
                    imgId = R.drawable.ic_wifi_signal_1_dark;
                }  else if (Math.abs(level) > 60) {
                    imgId = R.drawable.ic_wifi_signal_2_dark;
                } else if (Math.abs(level) > 50) {
                    imgId = R.drawable.ic_wifi_signal_3_dark;
                } else {
                    imgId = R.drawable.ic_wifi_signal_4_dark;
                }
                holder.imgWifiLevelIco.setImageResource(imgId);

            } else {
                int imgId = R.drawable.ic_wifi_lock_signal_0_dark;
                if (Math.abs(level) > 80) {
                    imgId = R.drawable.ic_wifi_lock_signal_1_dark;
                }  else if (Math.abs(level) > 60) {
                    imgId = R.drawable.ic_wifi_lock_signal_2_dark;
                } else if (Math.abs(level) > 50) {
                    imgId = R.drawable.ic_wifi_lock_signal_3_dark;
                } else {
                    imgId = R.drawable.ic_wifi_lock_signal_4_dark;
                }
                holder.imgWifiLevelIco.setImageResource(imgId);
            }
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            String g1 = wifiInfo.getSSID();
            String g2 = "\"" + datas.get(position).SSID + "\"";
            NetworkInfo.State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

            if (wifi == NetworkInfo.State.CONNECTING) {
                if (g2.endsWith(g1)) {
                    desc = context.getResources().getString(R.string.connecting);
                }
            } else if (wifi == NetworkInfo.State.CONNECTED) {
                if (g2.endsWith(g1)) {
                    desc = context.getResources().getString(R.string.connected);
                }
            }
            holder.txtWifiDesc.setText(desc);
        }else {
            holder.txtWifiName.setText(context.getResources().getString(R.string.add_other_network));
            holder.imgWifiLevelIco.setImageResource(R.drawable.load);
        }
        return convertView;
    }

    public static class Holder {
        public TextView txtWifiName;
        public TextView txtWifiDesc;
        public ImageView imgWifiLevelIco;
    }
}
