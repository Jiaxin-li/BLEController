package com.robotic.goldenridge.blecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.StringRes;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class ScanActivity extends AppCompatActivity {


    private static final int REQUEST_ENABLE_BT = 1;
    private Button connectBtn;
    private Button findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    static String select;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        text = (TextView) findViewById(R.id.text);
        connectBtn = (Button)findViewById(R.id.connect);
        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {

            connectBtn.setEnabled(false);
            findBtn.setEnabled(false);
            Toast.makeText(this,R.string.bt_support_err,Toast.LENGTH_SHORT).show();
            //text.setText("Status: not supported");

//            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
//                    Toast.LENGTH_LONG).show();
        } else {
            // turn on bluetooth
            if (!myBluetoothAdapter.isEnabled()) {
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                //Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                //        Toast.LENGTH_LONG).show();
            }
            if(MainActivity.btc!= null){
                text.setText("Connect to :" + MainActivity.btc.address);
                connectBtn.setText(R.string.Disconnect );
            }
            else{
                text.setText("DisConnected");
                connectBtn.setText(R.string.Connect);
            }

            // get paired devices
            pairedDevices = myBluetoothAdapter.getBondedDevices();

            connectBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    connect(v);
                }
            });

            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            myListView = (ListView)findViewById(R.id.listView1);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            // put it's one to the adapter
            for(BluetoothDevice device : pairedDevices)
                BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
            myListView.setAdapter(BTArrayAdapter);


            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener( ) {
                public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                    select = parent.getItemAtPosition(position).toString();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void connect(View view){



            if(MainActivity.btc==null){
                if(select == null){
                    Toast.makeText(getApplicationContext(),"Please select a device to connect",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    String devname = select.split("\n")[select.split("\n").length - 1];
                    Toast.makeText(getApplicationContext(), devname,
                            Toast.LENGTH_SHORT).show();
                    MainActivity.btc = new BSConnection(devname);
                    text.setText("Connect to :" + MainActivity.btc.address);
                    connectBtn.setText(R.string.Disconnect );
                }
            }
            else{
                MainActivity.btc.disconnect();
                MainActivity.btc = null;
                text.setText("DisConnected");
                connectBtn.setText(R.string.Connect);
            }


        //Toast.makeText(getApplicationContext(),"Show Paired Devices",
         //       Toast.LENGTH_SHORT).show();

    }



    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }


/*
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(bReceiver != null){
            unregisterReceiver(bReceiver);
        }

    }
    */
}
