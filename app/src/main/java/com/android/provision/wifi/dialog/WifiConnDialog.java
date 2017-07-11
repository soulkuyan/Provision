package com.android.provision.wifi.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.provision.R;
import com.android.provision.wifi.adapter.MyListViewAdapter;
import com.android.provision.wifi.component.OnNetworkChangeListener;
import com.android.provision.wifi.utils.WifiAdminUtils;
import com.android.provision.wifi.utils.WifiConnectUtils;

import java.util.List;

public class WifiConnDialog extends Dialog {
    private Context context;
    private ScanResult scanResult;
    private TextView txtWifiName;
    private TextView txtSinglStrength;
    private TextView txtSecurityLevel;
    private TextView txtBtnConn;
    private TextView txtBtnCancel;
    private EditText edtPassword;
    private CheckBox cbxShowPass;
    private ListView mListView;
    private int mPosition;
    private MyListViewAdapter mAdapter;
    private List<ScanResult> mScanResultList;
    private String wifiName;
    private String securigyLevel;
    private int level;

    public WifiConnDialog(Context context, int theme) {
        super(context, theme);
    }

    private WifiConnDialog(Context context, int theme, String wifiName,
                           int singlStren, String securityLevl) {
        super(context, theme);
        this.context = context;
        this.wifiName = wifiName;
        this.level = singlStren;
        this.securigyLevel = securityLevl;
    }

    public WifiConnDialog(Context context, int theme, ListView mListView,
                          int mPosition, MyListViewAdapter mAdapter, ScanResult scanResult,
                          List<ScanResult> mScanResultList,
                          OnNetworkChangeListener onNetworkChangeListener) {
        this(context, theme, scanResult.SSID, scanResult.level,
                scanResult.capabilities);
        this.mListView = mListView;
        this.mPosition = mPosition;
        this.mAdapter = mAdapter;
        this.scanResult = scanResult;
        this.mScanResultList = mScanResultList;
        this.onNetworkChangeListener = onNetworkChangeListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_conn);
        setCanceledOnTouchOutside(false);
        initView();
        setListener();
    }

    private void setListener() {

        edtPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (TextUtils.isEmpty(s)) {
                    txtBtnConn.setEnabled(false);
                    cbxShowPass.setEnabled(false);

                } else {
                    txtBtnConn.setEnabled(true);
                    cbxShowPass.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cbxShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Editable etable = edtPassword.getText();
                    Selection.setSelection(etable, etable.length());

                } else {
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Editable etable = edtPassword.getText();
                    Selection.setSelection(etable, etable.length());

                }
            }
        });

        txtBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiConnDialog.this.dismiss();
            }
        });

        txtBtnConn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                WifiConnectUtils.WifiCipherType type = null;
                if (scanResult.capabilities.toUpperCase().contains("WPA")) {
                    type = WifiConnectUtils.WifiCipherType.WIFICIPHER_WPA;
                } else if (scanResult.capabilities.toUpperCase().contains("WEP")) {
                    type = WifiConnectUtils.WifiCipherType.WIFICIPHER_WEP;
                } else {
                    type = WifiConnectUtils.WifiCipherType.WIFICIPHER_NOPASS;
                }
                WifiAdminUtils mWifiAdmin = new WifiAdminUtils(context);
                if (WifiConnDialog.this != null) {
                    dismiss();
                }
                boolean isConnect = mWifiAdmin.connect(scanResult.SSID,
                                                    edtPassword.getText().toString().trim(), type);
                if (isConnect) {
                    onNetworkChangeListener.onNetWorkConnect();
                } else {
                    onNetworkChangeListener.onNetWorkDisConnect();
                }
            }
        });
    }

    private void initView() {
        txtWifiName = (TextView) findViewById(R.id.txt_wifi_name);
        txtSinglStrength = (TextView) findViewById(R.id.txt_signal_strength);
        txtSecurityLevel = (TextView) findViewById(R.id.txt_security_level);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        cbxShowPass = (CheckBox) findViewById(R.id.cbx_show_pass);
        txtBtnCancel = (TextView) findViewById(R.id.txt_btn_cancel);
        txtBtnConn = (TextView) findViewById(R.id.txt_btn_connect);
        txtWifiName.setText(wifiName);
        txtSinglStrength.setText(WifiAdminUtils.singlLevToStr(context, level));
        txtSecurityLevel.setText(securigyLevel);
        txtBtnConn.setEnabled(false);
        cbxShowPass.setEnabled(false);

    }

    @Override
    public void show() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);

        super.show();
        getWindow().setLayout((int) (size.x * 9 / 10),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    private void showShortToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private OnNetworkChangeListener onNetworkChangeListener;
}
