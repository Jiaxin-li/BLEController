package com.robotic.goldenridge.blecontroller;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by jiaxin on 4/18/2016.
 * represent the Robot behaviour
 */
public class Robot {
    private static final String TAG =  Robot.class.getSimpleName();

    /** distance between wheels on the roomba, in millimeters */
    public static final int wheelbase = 258;
    /** mm/deg is circumference distance divided by 360 degrees */
    public static final float millimetersPerDegree = (float)(wheelbase * Math.PI / 360.0);
    public static final float millimetersPerRadian = (float)(wheelbase/2);

    /** default speed for movement operations if speed isn't specified */
    public static final int defaultSpeed  =  200;

    /** default update time in ms for auto sensors update */
    public static final int defaultSensorsUpdateTime = 200;

    /** default update time in ms for tasks */
    public static final int defaultTaskduration = 10;

    /** current mode, if known */
    int mode;

    /** current speed for movement operations that don't take a speed */
    public int speed = defaultSpeed;

    /** computed boolean for when Roomba is errored out of safe mode */
    boolean safetyFault = false;
    /** if sensor variables have been updated successfully */
    protected boolean sensorsValid = false;
    /** Set to true to make sensors auto-update (at expense of serial b/w) */
    boolean sensorsAutoUpdate = false;
    /** Time in milliseconds between sensor updates  */
    int sensorsUpdateTime = 200;
    /** last time (System.currentTimeMillis) that the sensors were updated */
    private long sensorsLastUpdateTime;
    /** how many bytes we expect to read from the sensor command */
    private int readRequestLength;

    /** internal storage for all roomba sensor data */
    private byte[] sensor_bytes = new byte[1024];

    /** connected to a serial port or not, not necessarily to roomba */
    boolean connected = false;

    // possible modes
    public static final int MODE_UNKNOWN = 0;
    public static final int MODE_PASSIVE = 1;
    public static final int MODE_SAFE    = 2;
    public static final int MODE_FULL    = 3;

    // Roomba ROI opcodes
    // these should all be bytes, but Java bytes are signed, sucka
    public static final int START   =  128;  // 0
    public static final int BAUD    =  129;  // 1
    public static final int CONTROL =  130;  // 0
    public static final int SAFE    =  131;  // 0
    public static final int FULL    =  132;  // 0
    public static final int POWER   =  133;  // 0
    public static final int SPOT    =  134;  // 0
    public static final int CLEAN   =  135;  // 0
    public static final int MAX     =  136;  // 0
    public static final int DRIVE   =  137;  // 4
    public static final int MOTORS  =  138;  // 1
    public static final int LEDS    =  139;  // 3
    public static final int SONG    =  140;  // 2N+2
    public static final int PLAY    =  141;  // 1
    public static final int SENSORS =  142;  // 1
    public static final int DOCK    =  143;  // 0
    public static final int PWMMOTORS = 144; // 3
    public static final int DRIVEWHEELS = 145; 	// 4
    public static final int DRIVEPWM = 146;  // 4
    public static final int STREAM  =  148;  // N+1
    public static final int QUERYLIST = 149; // N+1
    public static final int STOPSTARTSTREAM = 150;  // 1
    public static final int SCHEDULINGLEDS = 162; 	// 2
    public static final int DIGITLEDSRAW = 163; 	// 4
    public static final int DIGITLEDSASCII = 164;	// 4
    public static final int BUTTONSCMD  =  165; // 1
    public static final int SCHEDULE =  167;  // n
    public static final int SETDAYTIME = 168; // 3
    public static final int STOP = 173;
    // offsets into sensor_bytes data
    public static final int BUMPSWHEELDROPS     = 0;
    public static final int WALL                = 1;
    public static final int CLIFFLEFT           = 2;
    public static final int CLIFFFRONTLEFT      = 3;
    public static final int CLIFFFRONTRIGHT     = 4;
    public static final int CLIFFRIGHT          = 5;
    public static final int VIRTUALWALL         = 6;
    public static final int MOTOROVERCURRENTS   = 7;
    public static final int DIRTLEFT            = 8;
    public static final int DIRTRIGHT           = 9;
    public static final int REMOTEOPCODE        = 10;
    public static final int BUTTONS             = 11;
    public static final int DISTANCE_HI         = 12;
    public static final int DISTANCE_LO         = 13;
    public static final int ANGLE_HI            = 14;
    public static final int ANGLE_LO            = 15;
    public static final int CHARGINGSTATE       = 16;
    public static final int VOLTAGE_HI          = 17;
    public static final int VOLTAGE_LO          = 18;
    public static final int CURRENT_HI          = 19;
    public static final int CURRENT_LO          = 20;
    public static final int TEMPERATURE         = 21;
    public static final int CHARGE_HI           = 22;
    public static final int CHARGE_LO           = 23;
    public static final int CAPACITY_HI         = 24;
    public static final int CAPACITY_LO         = 25;

    // bitmasks for various thingems
    public static final int WHEELDROP_MASK      = 0x1C;
    public static final int BUMP_MASK           = 0x03;
    public static final int BUMPRIGHT_MASK      = 0x01;
    public static final int BUMPLEFT_MASK       = 0x02;
    public static final int WHEELDROPRIGHT_MASK = 0x04;
    public static final int WHEELDROPLEFT_MASK  = 0x08;
    public static final int WHEELDROPCENT_MASK  = 0x10;

    public static final int MOVERDRIVELEFT_MASK = 0x10;
    public static final int MOVERDRIVERIGHT_MASK= 0x08;
    public static final int MOVERMAINBRUSH_MASK = 0x04;
    public static final int MOVERVACUUM_MASK    = 0x02;
    public static final int MOVERSIDEBRUSH_MASK = 0x01;

    public static final int POWERBUTTON_MASK    = 0x08;
    public static final int SPOTBUTTON_MASK     = 0x04;
    public static final int CLEANBUTTON_MASK    = 0x02;
    public static final int MAXBUTTON_MASK      = 0x01;

    // which sensor packet, argument for sensors(int)
    public static final int SENSORS_ALL         = 0;
    public static final int SENSORS_PHYSICAL    = 1;
    public static final int SENSORS_INTERNAL    = 2;
    public static final int SENSORS_POWER       = 3;

    public static final int REMOTE_NONE         = 0xff;
    public static final int REMOTE_POWER        = 0x8a;
    public static final int REMOTE_PAUSE        = 0x89;
    public static final int REMOTE_CLEAN        = 0x88;
    public static final int REMOTE_MAX          = 0x85;
    public static final int REMOTE_SPOT         = 0x84;
    public static final int REMOTE_SPINLEFT     = 0x83;
    public static final int REMOTE_FORWARD      = 0x82;
    public static final int REMOTE_SPINRIGHT    = 0x81;

    // taskQueue
    private LinkedList<Runnable> tasks;
    private Thread thread;
    private boolean taskRunning;
    private Runnable internalRunnable;



    private static final int STRAIT = 0x8000;

    private boolean send (byte []b){
        return  MessageHandler.send(b);
    }

    private boolean send (byte b){
        return  MessageHandler.send(b);
    }

    public Robot (){
        taskRunning =false;
        tasks = new LinkedList<Runnable>();
        internalRunnable = new InternalRunnable();
    }

    private int getDesireSpeed(){
        return Utility.getSpeed();
    }

    /********************taskQueue***************************************/
    private class InternalRunnable implements Runnable {
        public void run() {
            internalRun();
        }
    }

    private class RobotTask implements Runnable {
        private byte[] b;
        private int duration = defaultTaskduration;
        private RobotTask (byte[]cmd,int duration){
            this.b = cmd;
            this.duration = duration;
        }
        private RobotTask (byte cmd,int duration){
            this.b = new byte[]{cmd};
            this.duration = duration;

        }
        public void run() {
            executeTask();
        }
        private void executeTask(){
            send(b);
            try {
                Thread.sleep(duration);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }

    public void startTask() {
        if (!taskRunning) {
            thread = new Thread(internalRunnable);
            thread.setDaemon(true);
            taskRunning = true;
            thread.start();
        }
    }

    public void stopTask() {
        taskRunning = false;
    }

    public void addTask(Runnable task) {
        Log.d(TAG, "addTask");
        synchronized(tasks) {
            tasks.addLast(task);
            tasks.notify(); // notify any waiting threads
        }
    }

    private Runnable getNextTask() {
        Log.d(TAG, "getNextTask");
        synchronized(tasks) {
            if (tasks.isEmpty()) {
                try {
                    tasks.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Task interrupted", e);
                    stopTask();
                }
            }
            return tasks.removeFirst();
        }
    }

    private void internalRun() {
        while(taskRunning) {
            Runnable task = getNextTask();
            try {
                task.run();
                Thread.yield();
            } catch (Throwable t) {
                Log.e(TAG, "Task threw an exception", t);
            }
        }
    }






    /********************mode setting***************************************/
    /**
     * Put Roomba in safe mode.
     * As opposed to full mode.  Safe mode is the preferred working state
     * when playing with the Roomba as it provides some measure of 
     * autonomous self-preservation if it encounters a cliff or is picked up
     * If that happens it goes into passive mode and must be 'reset()'.
     * @see #reset()
     */
    public void startup() {

        start();
        try {
            Thread.sleep(100);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
       safe();
    }
    /**
     * Put Roomba in stop mode.
     *
     * @see #reset()
     */

    public void stopSCI() {
        send( (byte)STOP );
    }

    /**
     * Reset Roomba after a fault.  This takes it out of whatever mode it was
     * in and puts it into safe mode.
     * This command also syncs the object's sensor state with the Roomba's
     * by calling updateSensors()
     * @see #startup()
     * @see #updateSensors()
     */
    public void reset() {
        stop();
        startup();
        control();
        updateSensors();
    }

    /**  Send START command  */
    public void start() {
        mode = MODE_PASSIVE;
        send( (byte)START );
    }
    /**  Send CONTROL command  */
    public void control() {

        mode = MODE_SAFE;
        send( (byte)CONTROL );
        // set blue dirt LED on so we know roomba is powered on & under control
        // (and we don't forget to turn it off, and run it's batteries flat)
        // FIXME: first time after a poweron, the lights flash then turn off
        //setLEDs(false, false, false, false, false, true, 128, 255);
    }
    /**  Send SAFE command  */
    public void safe() {

        mode = MODE_SAFE;
        send( (byte)SAFE );
    }
    /**  Send FULL command  */
    public void full() {

        mode = MODE_FULL;
        send( (byte)FULL );
    }

    /**
     * Power off the Roomba.  Once powered off, the only way to wake it
     * is via wakeup() (if implemented) or via a physically pressing
     * the Power button
     *
     */
    public void powerOff() {
        mode = MODE_UNKNOWN;
        send((byte) POWER );
    }

    /** Send the SPOT command */
    public void spot() {

        mode = MODE_PASSIVE;
        send((byte)  SPOT );
    }
    /** Send the CLEAN command */
    public void clean() {

        mode = MODE_PASSIVE;
        send((byte)  CLEAN );
    }
    /** Send the max command */
    public void max() {

        mode = MODE_PASSIVE;
        send((byte)  MAX );
    }
    /** Send the max command */
    public void dock() {

        mode = MODE_PASSIVE;
//        send( CLEAN );
        send( (byte) DOCK );
    }

    /********************sensors********************************/
    /**
     * Send the SENSORS command
     * with one of the SENSORS_ arguments
     * Typically, one does "sensors(SENSORS_ALL)" to get all sensor data
     * @param packetcode one of SENSORS_ALL, SENSORS_PHYSICAL,
     *                   SENSORS_INTERNAL, or SENSORS_POWER, or for roomba 5xx, it
     *                   is the sensor packet number (from the spec)
     */
    public void sensors(int packetcode ) {
        sensorsValid = false;
        //logmsg("sensors:"+packetcode);
        switch (packetcode) {
            case 0: readRequestLength = 26; break;
            case 1: readRequestLength = 10; break;
            case 2: readRequestLength = 6; break;
            case 3: readRequestLength = 10; break;
            case 4: readRequestLength = 14; break;
            case 5: readRequestLength = 12; break;
            case 6: readRequestLength = 52; break;
            case 100: readRequestLength = 80; break;
            case 101: readRequestLength = 28; break;
            case 106: readRequestLength = 12; break;
            case 107: readRequestLength = 9; break;
            case 19:
            case 20:
            case 22:
            case 23:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 54:
            case 55:
            case 56:
            case 57: readRequestLength = 2; break;
            default: readRequestLength = 1; break;
        }

        byte cmd[] = { (byte)SENSORS, (byte)packetcode};
        //send(cmd);
    }

    /**
     * get all sensor data
     */
    public void sensors() {
        readRequestLength = 26;
        sensors( SENSORS_ALL );
    }
    /**
     * Read roomba 26-byte sensor record using robotConnection. Tries once to read valid data, allowing 100ms
     * timeout on each attempt.
     * @return true if read 26 bytes of valid data. Data has been stored in sensor_bytes. False otherwise
     */
    public boolean updateSensors()
    {
        return updateSensors(SENSORS_ALL);
    }

    public boolean updateSensors(int sensorGroup)
    {
        int sensorGroupSize;

        //if (robotConnection == null) {
        //    System.out.println("Error at ArduinoBot.updateSensors(): no connection object for robot");
        //    return false;
        //}

        switch(sensorGroup) {
            case SENSORS_ALL: sensorGroupSize = 26; break;
            case 100: sensorGroupSize = 80; break;
            default:
                System.err.println("Invalid sensor group in updateSensors(): " + sensorGroup);
                return false;
        }
        sensors(sensorGroup);
        return getSensorData(sensorGroupSize);
    }

    /**
     * Query a list of sensors. This is a roomba 5xx only command.
     * @param sensorList A byte array containing the sensor groups requested to be read
     * @param returnLen The number of bytes of data expected to be returned from roomba
     */
    public void queryList(byte[] sensorList, int returnLen)
    {
        int i = 0;
        int j;

        sensorsValid = false;
        readRequestLength = returnLen;
        byte cmd[] = new byte[2+sensorList.length];
        cmd[i++] = (byte) QUERYLIST;
        cmd[i++] = (byte)sensorList.length;
        for (j=0; j<sensorList.length; j++)
            cmd[i++] = sensorList[j];
        //send(cmd);
    }

    /**
     * @param sensorGroupSize
     */
    public boolean getSensorData(int sensorGroupSize) {
        byte [] readData = null;

        // try once to read valid sensor data before giving up
        //startTime = System.currentTimeMillis();
        /*
        try {
            readData = robotConnection.readBot(sensorGroupSize);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/

        // readBot either read requested # of bytes or returned a null (indicating timeout.
        // If null or invalid data and group 0 (26 bytes expected), try again
        if( readData != null ) {
            if (((readData[1] > 1) || (readData[1] < 0)) && (sensorGroupSize == 26)) {
                sensorsValid = false;
               // logmsg("updateSensors: received invalid data while attempting to read Roomba sensors!");
            } else {
                sensorsValid = true;
                System.arraycopy(readData, 0, sensor_bytes, 0, sensorGroupSize);
                //logmsg("updateSensors: sensorsValid!");
                //elapsedTime = System.currentTimeMillis() - startTime;
                return true;
            }
        }
        System.out.println("Error: timeout on sensor read");
        return false;
    }

    // lower-level sensor access
    //
    /** lower-level func, returns raw byte */
    public int bumps_wheeldrops() {
        return sensor_bytes[BUMPSWHEELDROPS];
    }
    /** lower-level func, returns raw byte */
    public int cliff_left() {
        return sensor_bytes[CLIFFLEFT];
    }
    /** lower-level func, returns raw byte */
    public int cliff_frontleft() {
        return sensor_bytes[CLIFFFRONTLEFT];
    }
    /** lower-level func, returns raw byte */
    public int cliff_frontright() {
        return sensor_bytes[CLIFFFRONTRIGHT];
    }
    /** lower-level func, returns raw byte */
    public int cliff_right() {
        return sensor_bytes[CLIFFRIGHT];
    }
    /** lower-level func, returns raw byte */
    public int virtual_wall() {
        return sensor_bytes[VIRTUALWALL];
    }
    /** lower-level func, returns raw byte */
    public int motor_overcurrents() {
        return sensor_bytes[MOTOROVERCURRENTS];
    }
    /**  */
    public int dirt_left() {
        return sensor_bytes[DIRTLEFT] & 0xff;
    }
    /** */
    public int dirt_right() {
        return sensor_bytes[DIRTRIGHT] & 0xff;
    }
    /** lower-level func, returns raw byte */
    public int remote_opcode() {
        return sensor_bytes[REMOTEOPCODE];
    }
    /** lower-level func, returns raw byte */
    public int buttons() {
        return sensor_bytes[BUTTONS];
    }

    /**
     * Distance traveled since last requested
     * units: mm
     * range: -32768 - 32767
     */
    public short distance() {
        return Utility.toShort(sensor_bytes[DISTANCE_HI],
                sensor_bytes[DISTANCE_LO]);
    }
    /**
     * Angle traveled since last requested
     * units: mm, diff in distance traveled by two drive wheels
     * range: -32768 - 32767
     */
    public short angle() {
        return Utility.toShort(sensor_bytes[ANGLE_HI],
                sensor_bytes[ANGLE_LO]);
    }
    /**
     * angle since last read, but in degrees
     */
    // FIXME I think this should be (360 * angle())/(258 * PI)
    public float angleInDegrees() {
        return (float) angle() / millimetersPerDegree;
    }

    /**
     * angle since last read, but in radians
     */
    // FIXME I think this should be (2 * angle())/258
    public float angleInRadians() {
        return (float) angle() / millimetersPerRadian;
    }

    /**
     * Charging state
     * units: enumeration
     * range:
     */
    public int charging_state() {
        return sensor_bytes[CHARGINGSTATE] & 0xff;
    }
    /**
     * Voltage of battery
     * units: mV
     * range: 0 - 65535
     */
    public int voltage() {
        return Utility.toUnsignedShort(sensor_bytes[VOLTAGE_HI],
                sensor_bytes[VOLTAGE_LO]);
    }
    /**
     * Current flowing in or out of battery
     * units: mA
     * range: -332768 - 32767
     */
    public short current() {
        return Utility.toShort(sensor_bytes[CURRENT_HI],
                sensor_bytes[CURRENT_LO]);
    }
    /**
     * temperature of battery
     * units: degrees Celcius
     * range: -128 - 127
     */
    public byte temperature() {
        return sensor_bytes[TEMPERATURE];
    }
    public byte temperatureF() {
        int c = sensor_bytes[TEMPERATURE];
        return (byte) ((9.0/5.0)*c + 32);
    }
    /**
     * Current charge of battery
     * units: mAh
     * range: 0-65535
     */
    public int charge() {
        return Utility.toUnsignedShort(sensor_bytes[CHARGE_HI],
                sensor_bytes[CHARGE_LO]);
    }
    /**
     * Estimated charge capacity of battery
     * units: mAh
     * range: 0-65535
     */
    public int capacity() {
        return Utility.toUnsignedShort(sensor_bytes[CAPACITY_HI],
                sensor_bytes[CAPACITY_LO]);
    }

    /********************actuators********************************/
    /**
     * Move the Roomba via the low-level velocity + radius method.
     * See the 'Drive' section of the Roomba ROI spec for more details.
     * Low-level command.
     * @param velocity  speed in millimeters/second,
     *                  positive forward, negative backward
     * @param radius    radius of turn in millimeters
     */
    public void drive( int velocity, int radius ) {
        byte cmd[] = { (byte)DRIVE,(byte)(velocity>>>8),(byte)(velocity&0xff),
                (byte)(radius >>> 8), (byte)(radius & 0xff) };

        send(cmd);
    }
    /**
     * Move the Roomba via the low-level leftPWM RightPWM method.
     * See the 'Drive PWM' section of the Roomba ROI spec for more details.
     * Low-level command.
     * @param RPWM  right wheel speed in PWM persentage -255 ~ 255
     * @param LPWM  right wheel speed in PWM persentage -255 ~ 255
     */
    public  void drivePWM(int RPWM ,int LPWM){
        byte cmd[] = { (byte)DRIVEPWM,(byte)(RPWM>>>8),(byte)(RPWM&0xff),
                (byte)(LPWM >>> 8), (byte)(LPWM & 0xff) };

        send(cmd);

    }

    /**
     * Play a musical note
     * Does it via the hacky method of defining a one-note song & playing it
     * Uses up song slot 15.
     * If another note is played before one is finished, the new note cuts off
     * the old one.
     * @param note     a note number from 31 (G0) to 127 (G8)
     * @param duration duration of note in 1/64ths of a second
     */
    public void playNote( int note, int duration ) {

        byte cmd[] = {
                (byte)SONG, 3, 1, (byte)note, (byte)duration,  // define song
                (byte)PLAY, 3 };                               // play it back
        send(cmd);
    }

    public void playSong( int songnum ) {
        byte cmd[] = { (byte)PLAY, (byte)songnum };
        send(cmd);
    }

    /********************predefined tasks********************************/
    public void stop(){
        drive(0, 0);
    }
    public void forwardLeft(){
        drive(getDesireSpeed(), Utility.getRadius());
    }
    public void forward(){
        drive(getDesireSpeed(), STRAIT);
    }
    public void forwardRight(){
        drive(getDesireSpeed(), -1 * Utility.getRadius());
    }
    public void backwardLeft(){
        drive(-1 * getDesireSpeed(), Utility.getRadius());
    }
    public void backward(){
        drive(-1 * getDesireSpeed(), STRAIT);
    }
    public void backwardRight(){
        drive(-1 * getDesireSpeed(), -1 * Utility.getRadius());
    }
    public void spinCCW(){
        drive(getDesireSpeed(), 1);
    }
    public void spinCW(){
        drive(getDesireSpeed(), -1);
    }

    public void stepDrive(int velocity ,int radius,int pausetime){
        byte cmd[] = { (byte)DRIVE,(byte)(velocity>>>8),(byte)(velocity&0xff),
                (byte)(radius >>> 8), (byte)(radius & 0xff) };
        addTask(new RobotTask(cmd, pausetime));
        byte stopcmd[]  = { (byte)DRIVE,0,0,0,0};
        addTask(new RobotTask(stopcmd, defaultTaskduration));//stop() is NOT STOP !!!
    }

    /**
     * Go straight at the current speed for a specified distance.
     * Positive distance moves forward, negative distance moves backward.
     * This method blocks until the action is finished.
     * @param distance distance in millimeters, positive or negative
     */
    public void goStraight( int distance ) {
        int velocity = getDesireSpeed();
        int radius = STRAIT;
        int pausetime = Math.abs(distance / velocity )*1000;  // mm/(mm/sec)*1000 = millisecond
        stepDrive(velocity, radius, pausetime);
    }

    /**
     * Spin right or spin left a particular number of degrees
     * @param angle angle in degrees,
     *              positive to spin left, negative to spin right
     */
    public void spin( int angle ) {
        int velocity = getDesireSpeed();
        int radius ;
        if(angle>0){
            radius =1;// ccw
        }
        else{
            radius =-1;//cw
        }

        int pausetime = (int)Math.abs(millimetersPerDegree * angle / velocity)*1000;  //  millisecond
        stepDrive(velocity,radius,pausetime);
    }

    public void testSquare(int a, int b){
        goStraight(a);
        spin(90);
        goStraight(b);
        spin(90);
        goStraight(a);
        spin(90);
        goStraight(b);
        spin(90);
        startTask();

    }



}
/**************************settings***************************/

