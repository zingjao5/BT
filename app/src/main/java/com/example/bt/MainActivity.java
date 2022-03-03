package com.example.bt;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    //android.bluetooth.device.action.BOND_STATE_CHANGED
    private BluetoothDevice device;
    private BluetoothAdapter adapter ;
    private BluetoothSocket socket;
    private static final int REQUEST_ENABLE_BT = 2;
    private String deviceName,deviceAddress;
    private ParcelUuid[] deviceUUid;
    private TextView showDevice;
    private EditText dataText;
    private BluetoothServerSocket serverSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showDevice = findViewById(R.id.textView);
        dataText = findViewById(R.id.editTextTextPersonName);
        //藍芽調配器
        adapter = BluetoothAdapter.getDefaultAdapter();
        // bluetooth抓到設備發送廣播
        IntentFilter filter = new IntentFilter("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
        registerReceiver(receiver, filter);//廣播
    }
    //廣播回傳
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("taggg",""+action);
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceName = device.getName();
            deviceAddress = device.getAddress(); // MAC address
            deviceUUid=device.getUuids();
            Log.d("UUid",""+deviceUUid[0].getUuid());
            showDevice.setText("配對裝置:"+deviceName+ "\n"+"位址:"+deviceAddress);
            //可能報錯
            try {
                //回傳的選擇裝置進行配對
                device.createBond();
            } catch (Exception e) {
                Log.e("CreateBondError",e.getMessage());
            }
        }
    };
    //配對按鈕
    public void pairDevice(View view) {
        //藍芽開啟
        if(!adapter.isEnabled()) {
            Toast.makeText(view.getContext(),"先開權限後再點擊按鈕",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(intent);
        }
        else{
            //藍芽scanner
            Toast.makeText(view.getContext(),"PairDevice",Toast.LENGTH_SHORT).show();
            Intent bluetoothPicker = new Intent("android.bluetooth.devicepicker.action.LAUNCH");
            startActivity(bluetoothPicker);
            /*
            Intent intentSettings = new Intent();
            intentSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intentSettings);
            */
        }
    }
    private OutputStream os;
    private InputStream is;
    //當按下發送資料
    public void sendData(View view){
        try {
            serverSocket=adapter.listenUsingInsecureRfcommWithServiceRecord("NAME",deviceUUid[0].getUuid());
            if(socket==null){
                //socket = serverSocket.accept(5000);
                socket=device.createRfcommSocketToServiceRecord(deviceUUid[0].getUuid());
                adapter.cancelDiscovery();
                os=socket.getOutputStream();
                is=socket.getInputStream();
                sleep(5000);
                socket.connect();//會跑error
            }
            Log.d("os tag",""+os);
            Log.d("is tag",""+is);
            Log.d("socket tag","Connecting..."+socket.isConnected());
        }
        catch (Exception e){
            Log.d("Socket Error",""+e);
        }
        //dataText.getText()
    }
}