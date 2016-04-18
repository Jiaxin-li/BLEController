/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robotic.goldenridge.blecontroller;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Activity for scanning and displaying available Bluetooth LE and Standard devices.
 */
public class DeviceScanActivity extends ListActivity {
    private  String TAG = DeviceScanActivity.class.getSimpleName();
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean leScanning;
    private Handler mHandler;
    //integrating standard scanning
    private StandardDeviceListAdapter mStandardDeviceListAdapter;
    BluetoothDevice device;
    //replace deprecated method
    private BluetoothLeScanner les;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().setTitle(R.string.title_devices);
        // getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if(Utility.isLE()){
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                finish();
            }
        }


        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_support_err, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else{
            //les = mBluetoothAdapter.getBluetoothLeScanner();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        if (!leScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                clearListAdapter();
                setscanatate(true);
                break;
            case R.id.menu_stop:
                setscanatate(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        if(Utility.isLE()){
            mLeDeviceListAdapter = new LeDeviceListAdapter();
            setListAdapter(mLeDeviceListAdapter);

        }
        else{
            mStandardDeviceListAdapter = new StandardDeviceListAdapter();
            setListAdapter(mStandardDeviceListAdapter);
        }
        setscanatate(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setscanatate(false);
        clearListAdapter();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //// TODO: 4/3/2016
        Log.d(TAG,"listItem clicked");// DEBUG can't be reached when in standard mode
        if(Utility.isLE()){ //LEDevice
             device= mLeDeviceListAdapter.getDevice(position);
            if (device == null) return;
            MessageHandler.blc = new BLEConnection(device.getAddress(),MainActivity.mContext);
//            if (leScanning) {
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                leScanning = false;
//            }
        }
        else{ // Standard device
            // TODO
            device= mStandardDeviceListAdapter.getDevice(position);
            if (device == null) {
                Log.e(TAG,"device : null");
                return;
            }
            Log.d(TAG,device.getAddress());
            MessageHandler.btc = new BSConnection(device.getAddress());
        }
        setscanatate(false);

        //back to parrent
        NavUtils.navigateUpFromSameTask(this);
    }

    private void clearListAdapter(){
        if(mLeDeviceListAdapter!= null){
            mLeDeviceListAdapter.clear();
        }
        if(mStandardDeviceListAdapter!=null){
            mStandardDeviceListAdapter.clear();
        }
    }

    private void setscanatate(boolean state){
        if(Utility.isLE()){
            scanLeDevice(state);
        }
        else{
            scanStandardDevice(state);
        }
        leScanning =state;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    leScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //les.startScan(mLeScanCallback);// TODO: replace with startScan()
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            leScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            leScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private void scanStandardDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    leScanning = false;
                    mBluetoothAdapter.cancelDiscovery();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            leScanning = true;
            mBluetoothAdapter.startDiscovery();
        } else {
            leScanning = false;
            mBluetoothAdapter.cancelDiscovery();
        }
        invalidateOptionsMenu();
    }



    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Adapter for holding devices found through scanning.
    private class StandardDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mStandardDevices;
        private LayoutInflater mInflator;

        public StandardDeviceListAdapter() {
            super();
            Log.d(TAG,"StandardDeviceListAdapter");
            Set<BluetoothDevice> temp = mBluetoothAdapter.getBondedDevices();
            mStandardDevices = new ArrayList<BluetoothDevice>(temp); //get bond device
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mStandardDevices.contains(device)) {
                mStandardDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mStandardDevices.get(position);
        }

        public void clear() {
            mStandardDevices.clear();
        }

        @Override
        public int getCount() {
            return mStandardDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mStandardDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mStandardDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };




    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}