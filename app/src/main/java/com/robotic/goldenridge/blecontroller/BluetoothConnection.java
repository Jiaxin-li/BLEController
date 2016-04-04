/**
 * The bluetooth-arduino connection part uses code from the following site
 * https://bellcode.wordpress.com/2012/01/02/android-and-arduino-bluetooth-communication/
 */

package com.robotic.goldenridge.blecontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;
import android.bluetooth.*;
import android.util.Log;


public class BluetoothConnection {
    static boolean isLowenergy;
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


    public BluetoothConnection(String address ){
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

    public void disconnect() {

        try {
            stop = true;
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
