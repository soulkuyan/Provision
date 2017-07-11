package com.android.provision.wifi.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.provision.R;
import com.android.provision.wifi.component.OnNetworkChangeListener;
import com.android.provision.wifi.utils.WifiAdminUtils;
import com.android.provision.wifi.utils.WifiConnectUtils;

import java.util.ArrayList;

/**
 * Function add Network dialog
 * Created by lei.zhang on 2017/6/26.
 * version 0.1
 */
public class WifiAddDialog extends Dialog {
    private Context context;
    private EditText wifiEt;
    private Spinner secSpinner;
    private LinearLayout passwordll;
    private TextView txtBtnConn;
    private TextView txtBtnCancel;
    private EditText edtPassword;
    private CheckBox cbxShowPass;
    private EditText wifiSSIDEt;

    private ArrayList secList = new ArrayList();
    private ArrayAdapter<String> adapter;
    private int selectPosition = 0;

    public WifiAddDialog(Context context) {
        super(context);
        this.context = context;
    }

    public WifiAddDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wifi_add);
        initView();
        initData();
        initListener();
    }

    private void initView(){
        wifiEt = (EditText)findViewById(R.id.edit_ssid);
        secSpinner = (Spinner)findViewById(R.id.security_mode);
        passwordll = (LinearLayout)findViewById(R.id.password_linearlayout);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        cbxShowPass = (CheckBox) findViewById(R.id.cbx_show_pass);
        txtBtnCancel = (TextView) findViewById(R.id.txt_btn_cancel);
        txtBtnConn = (TextView) findViewById(R.id.txt_btn_connect);
        wifiSSIDEt = (EditText)findViewById(R.id.edit_ssid);
    }

    private void initData(){
        secList.add(context.getResources().getString(R.string.nothing));
        secList.add(context.getResources().getString(R.string.wep));
        secList.add(context.getResources().getString(R.string.wpa_wpa2));
        secList.add(context.getResources().getString(R.string.eap));
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, secList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secSpinner.setAdapter(adapter);

    }

    private void initListener(){
        secSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPosition = position;
                if (position == 0){
                    passwordll.setVisibility(View.GONE);
                }else {
                    passwordll.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        wifiSSIDEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    txtBtnConn.setEnabled(false);
                    cbxShowPass.setEnabled(false);

                } else {
                    txtBtnConn.setEnabled(true);
                    cbxShowPass.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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
                                          int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
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
                System.out.println("txtBtnCancel");
                WifiAddDialog.this.dismiss();
            }
        });

        txtBtnConn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                WifiConnectUtils.WifiCipherType type = null;
                if (selectPosition == 2) {
                    type = WifiConnectUtils.WifiCipherType.WIFICIPHER_WPA;
                } else if (selectPosition == 1) {
                    type = WifiConnectUtils.WifiCipherType.WIFICIPHER_WEP;
                } else {
                    type = WifiConnectUtils.WifiCipherType.WIFICIPHER_NOPASS;
                }
                WifiAdminUtils mWifiAdmin = new WifiAdminUtils(context);
                if (WifiAddDialog.this != null) {
                    dismiss();
                }
                boolean isConnect = mWifiAdmin.connect(wifiSSIDEt.getText().toString().trim(),
                                                    edtPassword.getText().toString().trim(), type);
                if (isConnect) {
                    onNetworkChangeListener.onNetWorkConnect();
                } else {
                    onNetworkChangeListener.onNetWorkDisConnect();
                }
            }
        });
    }

    @Override
    public void show() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
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
