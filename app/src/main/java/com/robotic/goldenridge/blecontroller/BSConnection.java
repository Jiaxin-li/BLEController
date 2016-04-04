package com.robotic.goldenridge.blecontroller;

/**
 * Created by jiaxin on 4/4/2016.
 * represent standard bluetooth connection
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;




public class BSConnection implements IBluetoothConnection {

    static BluetoothDevice MiDevice;
    static BluetoothSocket socket;
    BluetoothAdapter adapt;
    InputStream in;
    static OutputStream out;

    String address;


    //String carduino = "98:D3:31:70:22:71";


    Thread BlueToothThread;
    boolean stop = false;
    int position;
    byte read[];


    public BSConnection(String address ){
        this.address = address;
        try {
            runBT();
        }
        catch (IOException e ){
            e.printStackTrace();

        }


    }
    public void runBT() throws IOException, NullPointerException {

        //opens connection
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID 00001101-0000-1000-8000-00805F9B34FB

        if(MiDevice == null) {
            adapt = BluetoothAdapter.getDefaultAdapter();
            socket = adapt.getRemoteDevice(address).createRfcommSocketToServiceRecord(uuid);
        } else {
            socket = MiDevice.createRfcommSocketToServiceRecord(uuid);
        }

        socket.connect();
        out = socket.getOutputStream();
        in = socket.getInputStream();
        stop = false;
        position = 0;
        read = new byte[1024];
        BlueToothThread = new Thread(new Runnable() {

            public void run() {

                while(!Thread.currentThread().isInterrupted() && !stop) {
                    // Log.d("DEC","running");
                    /* // place holder for receiving
                    try {

                    } catch (IOException e) {
                        stop = true;
                        e.printStackTrace();
                    } */
                }
            }
        });

        BlueToothThread.start();

    }



    // send compiled command
    public void sendControlBytes(byte[] ctrl) {
        try {
            if (socket.isConnected()) {
                out.write(ctrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean connect() {
        return false;
    }

    public boolean disconnect() {

        try {
            stop = true;
            out.close();
            in.close();
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }




}
