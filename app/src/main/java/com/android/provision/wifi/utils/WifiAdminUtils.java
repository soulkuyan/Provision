package com.android.provision.wifi.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.android.provision.R;

import java.net.Inet4Address;
import java.util.List;

/**
 * Function:Wifi connect util
 * Created by george on 2017/6/21.
 */
public class WifiAdminUtils {
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mScanWifiList;
    private List<WifiConfiguration> mWifiConfigurations;
    private WifiManager.WifiLock mWifiLock;

    public WifiAdminUtils(Context mContext) {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();

    }

    public boolean closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            return mWifiManager.setWifiEnabled(false);
        }
        return false;
    }

    public int checkState() {
        return mWifiManager.getWifiState();
    }

    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    public void createWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("test");
    }

    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfigurations;
    }

    public void connetionConfiguration(int index) {
        if (index > mWifiConfigurations.size()) {
            return;
        }
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId,
                true);
    }

    public void startScan() {
        openWifi();
        mWifiManager.startScan();
        mScanWifiList = mWifiManager.getScanResults();
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();

    }

    public List<ScanResult> getWifiList() {
        return mScanWifiList;
    }

    public StringBuffer lookUpScan() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mScanWifiList.size(); i++) {
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            sb.append((mScanWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetWordId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    public void addNetWork(WifiConfiguration configuration) {
        int wcgId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId, true);
    }

    public void disConnectionWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public boolean openWifi() {
        boolean bRet = true;
        if (!mWifiManager.isWifiEnabled()) {
            bRet = mWifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    public boolean connect(String SSID, String Password, WifiConnectUtils.WifiCipherType Type) {
        if (!this.openWifi()) {
            return false;
        }
        while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }
        if (SSID == null || Password == null || SSID.equals("")) {
            return false;
        }
        WifiConfiguration wifiConfig = createWifiInfo(SSID, Password, Type);
        if (wifiConfig == null) {
            return false;
        }
        WifiConfiguration tempConfig = this.isExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        int netID = mWifiManager.addNetwork(wifiConfig);
        mWifiManager.disconnect();
        boolean mConnectConfig = mWifiManager.enableNetwork(netID, true);
        mWifiManager.reconnect();
        return mConnectConfig;
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiConnectUtils.WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiConnectUtils.WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiConnectUtils.WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WifiConnectUtils.WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);

        } else {
            return null;
        }
        return config;
    }

    public boolean isConnect(ScanResult result) {
        if (result == null) {
            return false;
        }

        mWifiInfo = mWifiManager.getConnectionInfo();
        String g2 = "\"" + result.SSID + "\"";
        if ((mWifiInfo.getSSID() != null) && (mWifiInfo.getSSID().endsWith(g2))) {
            return true;
        }
        return false;
    }

    public String ipIntToString(int ip) {
        try {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (0xff & ip);
            bytes[1] = (byte) ((0xff00 & ip) >> 8);
            bytes[2] = (byte) ((0xff0000 & ip) >> 16);
            bytes[3] = (byte) ((0xff000000 & ip) >> 24);
            return Inet4Address.getByAddress(bytes).getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    public int getConnNetId() {
        // result.SSID;
        mWifiInfo = mWifiManager.getConnectionInfo();
        return mWifiInfo.getNetworkId();
    }
    public static String singlLevToStr(Context context, int level) {

        String resuString = context.getResources().getString(R.string.wifi_no_level);

        if (Math.abs(level) > 100) {
        } else if (Math.abs(level) > 80) {
            resuString = context.getResources().getString(R.string.wifi_weak_level);
        } else if (Math.abs(level) > 70) {
            resuString = context.getResources().getString(R.string.wifi_strong_level);
        } else if (Math.abs(level) > 60) {
            resuString = context.getResources().getString(R.string.wifi_strong_level);
        } else if (Math.abs(level) > 50) {
            resuString = context.getResources().getString(R.string.wifi_stronger_level);
        } else {
            resuString = context.getResources().getString(R.string.wifi_strongest_level);
        }
        return resuString;
    }

    public boolean addNetwork(WifiConfiguration wcg) {
        if (wcg == null) {
            return false;
        }
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        mWifiManager.saveConfiguration();
        System.out.println(b);
        return b;
    }

    public boolean connectSpecificAP(ScanResult scan) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        boolean networkInSupplicant = false;
        boolean connectResult = false;
        // again connect
        mWifiManager.disconnect();
        for (WifiConfiguration w : list) {
            if (w.BSSID != null && w.BSSID.equals(scan.BSSID)) {
                connectResult = mWifiManager.enableNetwork(w.networkId, true);
                networkInSupplicant = true;
                break;
            }
        }
        if (!networkInSupplicant) {
            WifiConfiguration config = CreateWifiInfo(scan, "");
            connectResult = addNetwork(config);
        }

        return connectResult;
    }

    public WifiConfiguration CreateWifiInfo(ScanResult scan, String Password) {
        WifiConfiguration config = new WifiConfiguration();
        config.hiddenSSID = false;
        config.status = WifiConfiguration.Status.ENABLED;

        if (scan.capabilities.contains("WEP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);

            config.SSID = "\"" + scan.SSID + "\"";

            config.wepTxKeyIndex = 0;
            config.wepKeys[0] = Password;
        } else if (scan.capabilities.contains("PSK")) {
            //
            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = "\"" + Password + "\"";
        } else if (scan.capabilities.contains("EAP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = "\"" + Password + "\"";
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = null;
        }
        return config;
    }


}

