package com.robotic.goldenridge.blecontroller;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;



import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jiaxin on 10/18/2015.
 * The abstract base for all Robot communications.
 * communication protocol compatible to iRobot open Interface
 * http://www.irobot.com/filelibrary/pdfs/hrd/create/Create%20Open%20Interface_v2.pdf
 */


public class MessageHandler {

    public static BSConnection btc = null ;
    public static BLEConnection blc = null;

    private static String TAG =  MessageHandler.class.getSimpleName();
    public void handleFeedback(InputStream in) throws IOException {


    // place holder
    }



    public static boolean send(byte[] cmd){
        // standard bluetooth
        if (!Utility.isLE() && btc != null) {
           return btc.send(cmd);
        }
        // BLE
        else if(Utility.isLE( ) && blc != null){
            return blc.send(cmd);
        }
        else{ // throw exception not need
            Log.e(TAG,"Not connect");
            return  false;
        }
    }

    public static boolean send(byte cmd){
        // standard bluetooth
        if (!Utility.isLE() && btc != null) {
            return btc.send(cmd);
        }
        // BLE
        else if(Utility.isLE( ) && blc != null){
            return blc.send(cmd);
        }
        else{ // throw exception not need
            Log.e(TAG,"Not connect");
            return  false;
        }
    }

}
