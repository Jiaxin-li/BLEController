
/**
 * Created by jiaxin on 4/4/2016.
 */

package com.robotic.goldenridge.blecontroller;
public interface IBluetoothConnection{

    public void sendControlBytes(byte[] ctrl) ;

    public boolean connect();
    public boolean disconnect();
}