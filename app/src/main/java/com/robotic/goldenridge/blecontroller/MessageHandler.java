package com.robotic.goldenridge.blecontroller;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;



import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jiaxin on 10/18/2015.
 * communication protocol compatible to iRobot open Interface
 * http://www.irobot.com/filelibrary/pdfs/hrd/create/Create%20Open%20Interface_v2.pdf
 */


public class MessageHandler {
    /** distance between wheels on the roomba, in millimeters */
    public static final int wheelbase = 258;
    /** mm/deg is circumference distance divided by 360 degrees */
    public static final float millimetersPerDegree = (float)(wheelbase * Math.PI / 360.0);
    private static final int STRAIT = 0x8000;
    private static final int START = 128;
    private static final int SAFE = 131;
    private static final int FULL = 132;
    private static final int STOP = 173;
    private static final int RESET = 7;
    private static final int DOCK = 143;
    private static String TAG =  MessageHandler.class.getSimpleName();
    public void handleFeedback(InputStream in) throws IOException {


    // place holder
    }

    public static void startcmd(){
        sendBytes(new byte[]{(byte)START});
        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        changeMode(SAFE);
    }

    public static void resetcmd(){
        sendBytes(new byte[]{(byte) RESET});
    }

    public static void changeMode(int mode){
        sendBytes(new byte[]{(byte)mode});
    }

    public static void stopcmd(){
        sendBytes(new byte[]{(byte)STOP});
    }

    public static void dock(){
        sendBytes(new byte[]{(byte)DOCK});
    }


    public static void drivePWM(int RPWM,int LPWM){
        sendBytes(PWMcmd(RPWM, LPWM));
    }

    private static void sendBytes(byte[] cmd){
        // standard bluetooth
        if (!MainActivity.isLE( ) && MainActivity.btc != null) {
            MainActivity.btc.sendControlBytes(cmd);
        }
        // BLE
        else if(MainActivity.isLE( ) && MainActivity.blc != null){
            MainActivity.blc.sendControlBytes(cmd);
        }
        else{ // throw exception not need
            Log.e(TAG,"Not connect");
        }
    }

    public static void stop(){
        drivePWM(0,0);
    }
    public static void forwardLeft(){
        sendBytes(drivecmd(MainActivity.getSpeed(), MainActivity.getRadius()));
    }
    public static void forward(){
        sendBytes(drivecmd(MainActivity.getSpeed(), STRAIT));
    }
    public static void forwardRight(){
        sendBytes(drivecmd(MainActivity.getSpeed(), -1 * MainActivity.getRadius()));
    }
    public static void backwardLeft(){
        sendBytes(drivecmd(-1 * MainActivity.getSpeed(), MainActivity.getRadius()));
    }
    public static void backward(){
        sendBytes(drivecmd(-1 * MainActivity.getSpeed(), STRAIT));
    }
    public static void backwardRight(){
        sendBytes(drivecmd(-1 * MainActivity.getSpeed(), -1 * MainActivity.getRadius()));
    }
    public static void spinCCW(){
        sendBytes(drivecmd(MainActivity.getSpeed(), 1));
    }
    public static void spinCW(){
        sendBytes(drivecmd(MainActivity.getSpeed(), -1));
    }


    //generate Drive PWM cmd OPCode 146
    private static byte[] PWMcmd(int RPWM,int LPWM){
        byte[] cmd  = new byte[5];
        cmd[0] =(byte)0x92;
        cmd[1] = (byte)(RPWM >>> 8);//(RPWM >> 8 & 0xFF);// 	>>> (zero fill right shift)
        cmd[2] = (byte)(RPWM & 0xFF);
        cmd[3] = (byte)(LPWM >>> 8);//(LPWM >> 8 & 0xFF);
        cmd[4] = (byte)(LPWM & 0xFF);
        return cmd;
    }

    //generate Drive  cmd OPCode 137
    private static byte[] drivecmd(int velocity, int radius){
        byte[] cmd  = new byte[5];
        cmd[0] = (byte)0x89;
        cmd[1] = (byte)(velocity >>> 8);
        cmd[2] = (byte)(velocity & 0xFF);
        cmd[3] = (byte)(radius >>> 8);//
        cmd[4] = (byte)(radius & 0xFF);
        return cmd;
    }



    //drive Velocity (-500 – 500 mm/s) Radius (-2000 – 2000 mm)
    //Magic!
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
