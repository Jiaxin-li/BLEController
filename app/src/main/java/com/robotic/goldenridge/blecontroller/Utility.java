package com.robotic.goldenridge.blecontroller;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jiaxin on 4/18/2016.
 */
public class Utility {
    /**
     *
     */
    static public final short toShort(byte hi, byte lo) {
        return (short)((hi << 8) | (lo & 0xff));
    }
    /**
     *
     */
    static public final int toUnsignedShort(byte hi, byte lo) {
        return (int)(hi & 0xff) << 8 | lo & 0xff;
    }



    public void println(String s) {
        System.out.println(s);
    }

    public String hex(byte b) {
        return Integer.toHexString(b&0xff);
    }

    public String hex(int i) {
        return Integer.toHexString(i);
    }


    public String binary(int i) {
        return Integer.toBinaryString(i);
    }

    /**
     * just a little debug
     */
    public void logmsg(String msg) {

            System.err.println("log ("+System.currentTimeMillis()+"):"+msg);
            System.err.flush();

    }

    /**
     * General error reporting, all corraled here just in case
     * I think of something slightly more intelligent to do.
     */
    public void errorMessage(String where, Throwable e) {
        e.printStackTrace();
        throw new RuntimeException("Error inside Serial." + where + "()");
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

    public static boolean isLE(){
        return  MainActivity.prefs.getBoolean("isBLE", true);
    }

    public static int getRadius(){ // getInt() Throws ClassCastException if there is a preference with this name that is not an int.
        return constrain(Integer.valueOf(MainActivity.prefs.getString("radius", "100")), 0, 2000);
    }

    public static int getSpeed(){
        // constrain the value from 0 - 500
        return constrain(Integer.valueOf(MainActivity.prefs.getString("speed", "0")),0,500);
    }

    private static int constrain(int ori, int min, int max){
        if( ori < min) ori = min;
        if( ori > max ) ori = max;
        return ori;
    }

}
