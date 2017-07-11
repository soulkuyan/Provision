package com.android.provision.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.android.provision.DefaultActivity;
import com.android.provision.R;
import com.android.provision.activitymanager.MyActivityManager;

/**
 * Function Bluetooth settings
 * Created by lei.zhang on 2017/6/23
 * version 0.1
 */
public class BluetoothSettingActivity extends Activity {

    private CheckBox bluetoothBox;
    private BluetoothAdapter bluetoothAdapter;
    private Button finishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_setting);
        bluetoothBox = (CheckBox)findViewById(R.id.checkBox);
        finishBtn = (Button)findViewById(R.id.finish);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (bluetoothAdapter != null){
                        if (!bluetoothAdapter.isEnabled()){
                            bluetoothAdapter.enable();
                        }
                    }

                }
            }
        });

        bluetoothBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((CheckBox) v).isChecked()){
                    bluetoothAdapter.disable();
                }
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyActivityManager.getInstance().exit();
                Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);
                PackageManager pm = getPackageManager();
                ComponentName name = new ComponentName(BluetoothSettingActivity.this, DefaultActivity.class);
                pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                finish();

            }
        });
    }
}
