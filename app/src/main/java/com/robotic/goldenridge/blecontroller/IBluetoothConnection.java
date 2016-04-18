
/**
 * Created by jiaxin on 4/4/2016.
 */

package com.robotic.goldenridge.blecontroller;
public interface IBluetoothConnection{


    /**
     * Send given byte array to Robot.
     * @param bytes byte array of ROI commands to send
     * @return true on successful send
     */
    public boolean send(byte[] bytes);

    /**
     * Send a single byte to the Robot
     * (defined as int because of stupid java signed bytes)
     * @param b byte of an ROI command to send
     * @return true on successful send
     */
    public boolean send(byte b);

    /**
     * Connect to a bluetooth device
     * (for serial, portid is serial port name, for net, portid is url?)
     * @return true on successful connect, false otherwise
     */
    public abstract  boolean connect();
    /**
     * Disconnect from a bluetooth device, clean up any memory in use
     */
    public abstract  void disconnect();


}