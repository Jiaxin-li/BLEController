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

    public void handleFeedback(InputStream in) throws IOException {


    // place holder
    }

    public static void startcmd(){
        byte[] startcmd = {(byte) 0x80}; // start OI
        byte[] safemod = {(byte) 0x83}; // turn into safe mode
        // standard bluetooth
        if (!MainActivity.isLE && MainActivity.btc != null) {
            MainActivity.btc.sendControlBytes(startcmd);
            MainActivity.btc.sendControlBytes(safemod);
        }
        // BLE
        else if(MainActivity.isLE && MainActivity.blc != null){
            MainActivity.blc.sendControlBytes(startcmd);
            MainActivity.blc.sendControlBytes(safemod);
        }

        else {
                        // exception

        }
    }


    public static void stopcmd(){
        byte[] stopcmd = {(byte) 0xAD}; // stop OI
        // standard bluetooth
        if (!MainActivity.isLE && MainActivity.btc != null) {
            MainActivity.btc.sendControlBytes(stopcmd);
        }
        // BLE
        else if(MainActivity.isLE && MainActivity.blc != null){
            MainActivity.blc.sendControlBytes(stopcmd);
        }

        else {
            // exception

        }
    }


    public static void drivePWM(int RPWM,int LPWM){
        byte[] PWMcmd = PWMcmd(RPWM, LPWM);
         // standard bluetooth
                if (!MainActivity.isLE && MainActivity.btc != null) {
                    //btc.sendString(angle + "," + power + "\n");//use plain String to send control
                    MainActivity.btc.sendControlBytes(PWMcmd);
                }
        // BLE
                else if(MainActivity.isLE && MainActivity.blc != null){
                    MainActivity.blc.sendControlBytes(PWMcmd);
                }
                else{ // throw exception

                }
    }
    //generate Drive PWM cmd OPCode 146
    public static byte[] PWMcmd(int RPWM,int LPWM){
        byte[] cmd  = new byte[5];
        cmd[0] =(byte)0x92;
        cmd[1] = (byte)(RPWM >> 8 & 0xFF);
        cmd[2] = (byte)(RPWM & 0xFF);
        cmd[3] = (byte)(LPWM >> 8 & 0xFF);
        cmd[4] = (byte)(LPWM & 0xFF);
        return cmd;
    }
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
