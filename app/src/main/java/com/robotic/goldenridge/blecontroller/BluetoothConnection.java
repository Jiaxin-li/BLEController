/**
 * The bluetooth-arduino connection part uses code from the following site
 * https://bellcode.wordpress.com/2012/01/02/android-and-arduino-bluetooth-communication/
 */

package com.robotic.goldenridge.blecontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class BluetoothConnection {
    static BluetoothDevice MiDevice;
    static BluetoothSocket socket;
    BluetoothAdapter adapt;
    InputStream in;
    static OutputStream out;
    //static PrintWriter writer;
    String address;
    Netstrings nt = new Netstrings();
    String returnResult ="";
    //String carduino = "98:D3:31:70:22:71";
    CarControlProtos.CarControl.Builder ccb=CarControlProtos.CarControl.newBuilder();
    CarControlProtos.CarControl ctrl;

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
       // writer = new PrintWriter(out);
//      data.setText("connection established");

        //gets data
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stop = false;
        position = 0;
        read = new byte[1024];
        BlueToothThread = new Thread(new Runnable() {

            public void run() {

                while(!Thread.currentThread().isInterrupted() && !stop) {

                    try {

                        int bytesAvailable = in.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            in.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                               // handel
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[position];
                                    System.arraycopy(read, 0, encodedBytes, 0, encodedBytes.length);
                                    returnResult = nt.decodedNetstring(new String(encodedBytes, "US-ASCII"));//new String(encodedBytes, "US-ASCII");//
                                    updateText(MainActivity.activity,R.id.statusView,returnResult);
                                    position = 0;

                                }
                                else{
                                    read[position++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                            stop = true;
                    }
                }
            }
        });

        BlueToothThread.start();

    }


    public  void sendString(String msg) {
        try {


                if(!msg.isEmpty()) {
                    if (socket.isConnected()) {
                        out.write(nt.encodedNetstring(msg).getBytes());
                    }
                }
            } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendControl(int speed, int steer) {
        try {



                if (socket.isConnected()) {
                    ccb.setSpeed(speed);
                    ccb.setSteer(steer);
                    ctrl = ccb.build();
                    String length= Integer.toString(ctrl.getSerializedSize());
                    out.write(length.getBytes());
                    out.write((byte)':');
                    ctrl.writeTo(out);
                    out.write((byte)',');


                    //String cmd = nt.encodedNetstring(ccb.build().toByteArray());

                    //ccb.build().writeTo(out); //use Car control proto
                    //out.write(HEADER); // add byte header
                    //Log.d ("ENC", cmd);
                    //out.write(cmd.getBytes());// getBytes add [B@ at begining
                   // writer.println(cmd);
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

    public static void updateText(Activity act,int ID, final String text)
    {

        final TextView loadingText = (TextView) act.findViewById(ID);
        act.runOnUiThread(new Runnable()
        {

            public void run()
            {
                loadingText.setText(text);

            }

        });
    }


}
