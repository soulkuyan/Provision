package com.android.provision.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.provision.R;
import com.android.provision.activitymanager.MyActivityManager;
import com.android.provision.bluetooth.BluetoothSettingActivity;
import com.android.provision.wifi.adapter.MyListViewAdapter;
import com.android.provision.wifi.component.OnNetworkChangeListener;
import com.android.provision.wifi.dialog.WifiAddDialog;
import com.android.provision.wifi.dialog.WifiConnDialog;
import com.android.provision.wifi.dialog.WifiStatusDialog;
import com.android.provision.wifi.utils.WifiAdminUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WifiConnectSettingsActivity extends Activity {
    protected static final String TAG = "WifiConnectActivity";
    private static final int REFRESH_CONN = 100;
    private WifiAdminUtils mWifiAdmin;
    private List<ScanResult> list = new ArrayList<ScanResult>();
    private ListView wifilistView;
    private MyListViewAdapter mAdapter;
    private int mPosition;
    private WifiReceiver mReceiver;
    private Button previousBtn;
    private Button skipBtn;

    private OnNetworkChangeListener mOnNetworkChangeListener = new OnNetworkChangeListener() {

        @Override
        public void onNetWorkDisConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNetWorkConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect_settings);
        MyActivityManager.getInstance().addActivity(this);
        initData();
        initView();
        setListener();
        refreshWifiStatusOnTime();
    }

    private void initData() {
        mWifiAdmin = new WifiAdminUtils(WifiConnectSettingsActivity.this);
        getWifiListInfo();
    }

    private void initView() {

        wifilistView = (ListView) findViewById(R.id.freelook_listview);
        previousBtn = (Button)findViewById(R.id.previous);
        skipBtn = (Button)findViewById(R.id.skip);
        mAdapter = new MyListViewAdapter(this, list);
        wifilistView.setAdapter(mAdapter);
        int wifiState = mWifiAdmin.checkState();
    }

    private void registerReceiver() {
        mReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void setListener() {
        wifilistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                if (pos != list.size()){
                    mPosition = pos;
                    ScanResult scanResult = list.get(mPosition);
                    String desc = "";
                    String descOri = scanResult.capabilities;
                    if (descOri.toUpperCase().contains("WPA-PSK")
                            && descOri.toUpperCase().contains("WPA2-PSK")) {
                        desc = "WPA/WPA2";
                    }else if (descOri.toUpperCase().contains("WPA-PSK")) {
                        desc = "WPA";
                    }else if (descOri.toUpperCase().contains("WPA2-PSK")) {
                        desc = "WPA2";
                    }else if (descOri.toUpperCase().contains("WEP")) {
                        desc = "WEP";
                    }else if (descOri.toUpperCase().contains("EAP")) {
                        desc = "EAP";
                    }

                    if (desc.equals("")) {
                        isConnectSelf(scanResult);
                        return;
                    }
                    isConnect(scanResult);
                }else {
                    WifiAddDialog addDialog = new WifiAddDialog(WifiConnectSettingsActivity.this, R.style.addDialog);
                    addDialog.show();
                }
            }

            /**
             * need password
             * @param scanResult
             */
            private void isConnect(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(
                            WifiConnectSettingsActivity.this, R.style.defaultDialogStyle,
                            scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    WifiConnDialog mDialog = new WifiConnDialog(
                            WifiConnectSettingsActivity.this, R.style.defaultDialogStyle, wifilistView, mPosition, mAdapter,
                            scanResult, list, mOnNetworkChangeListener);
                    mDialog.show();
                }
            }

            /**
             * no password
             * @param scanResult
             */
            private void isConnectSelf(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(
                            WifiConnectSettingsActivity.this, R.style.defaultDialogStyle,
                            scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    boolean iswifi = mWifiAdmin.connectSpecificAP(scanResult);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (iswifi) {
                        Toast.makeText(WifiConnectSettingsActivity.this, "connect success!",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(WifiConnectSettingsActivity.this, "connect error!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WifiConnectSettingsActivity.this, BluetoothSettingActivity.class));
            }
        });
    }

    /**
     * get wifi info
     */
    private void getWifiListInfo() {
        mWifiAdmin.startScan();
        List<ScanResult> tmpList = mWifiAdmin.getWifiList();
        if (tmpList == null) {
            list.clear();
        } else {
            list = tmpList;
        }
    }

    private Handler mHandler = new MyHandler(this);

    protected boolean isUpdate = true;

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private static class MyHandler extends Handler {

        private WeakReference<WifiConnectSettingsActivity> reference;

        public MyHandler(WifiConnectSettingsActivity activity) {
            this.reference = new WeakReference<WifiConnectSettingsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            WifiConnectSettingsActivity activity = reference.get();

            switch (msg.what) {
                case REFRESH_CONN:
                    activity.getWifiListInfo();
                    activity.mAdapter.setDatas(activity.list);
                    activity.mAdapter.notifyDataSetChanged();
                    break;

                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private void refreshWifiStatusOnTime() {
        new Thread() {
            public void run() {
                while (isUpdate) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(REFRESH_CONN);
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isUpdate = false;
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        protected static final String TAG = "WifiConnActivity";
        private boolean isDisConnected = false;
        private boolean isConnecting = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if (!isDisConnected) {
                        isDisConnected = true;
                    }
                } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                    if (!isConnecting) {
                        isConnecting = true;
                    }
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                }

            } else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
                switch (error) {
                    case WifiManager.ERROR_AUTHENTICATING:
                        Toast.makeText(getApplicationContext(), "Wifi password is error", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }

            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLING:
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        break;

                }
            }
        }

    }
}
