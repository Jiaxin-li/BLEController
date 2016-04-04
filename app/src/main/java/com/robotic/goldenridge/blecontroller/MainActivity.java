package com.robotic.goldenridge.blecontroller;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.robotic.goldenridge.blecontroller.JoystickView.OnJoystickMoveListener;

public class MainActivity extends AppCompatActivity { //
    public static final int angleError =10;

    public static JoystickView joystick;
    public static BluetoothConnection btc = null ;
    //test for BLE
    public static BLEConnection blc = null;

    public static TextView stv;
    public static TextView tvsteer;
    public static TextView tvspeed;
    public static TextView tvleft;
    public static TextView tvright;
    public static TextView tvcmd;
    public static ImageButton startbtn;
    public static ImageButton[] soundbtns;

    private final double RAD = 57.2957795;
    private byte[] cmdBytes;
    boolean OIstatus = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        stv= (TextView)findViewById(R.id.statusView);
        tvspeed = (TextView) findViewById(R.id.speed_value);
        tvsteer = (TextView) findViewById(R.id.steer_value);
        tvleft = (TextView) findViewById(R.id.front_value);
        tvright = (TextView) findViewById(R.id.rear_value);
        tvcmd =  (TextView) findViewById(R.id.decode_value);
        startbtn = (ImageButton) findViewById(R.id.startbtn);
        soundbtns = new ImageButton[4];
        soundbtns[0]= (ImageButton) findViewById(R.id.soundbtn0);
        soundbtns[1]= (ImageButton) findViewById(R.id.soundbtn1);
        soundbtns[2]= (ImageButton) findViewById(R.id.soundbtn2);
        soundbtns[3]= (ImageButton) findViewById(R.id.soundbtn3);


        addListenerOnButtons();

        joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                int RPWM = 0;
                int LPWM = 0;
                double angleRad= angle/RAD;
                // Scale speed
                /* // used for steering car
                if (angle < -90 || angle > 90) {
                    power *= -1;
                }
                // X to Y adapter
                if (angle > 90) {
                    angle = 180 - angle;
                } else if (angle < -90) {
                    angle = -(180 + angle);
                }*/
                // drive strait forward
                if(angle>= 0 - angleError && angle <= 0 + angleError ){
                    RPWM = power;
                    LPWM = power;
                }
                //drive strait backward
                else if(angle<= -180 + angleError || angle >= 180-angleError){
                    RPWM = power * -1;
                    LPWM = power * -1;
                }
                // turn right in position
                else if(angle > 90-angleError && angle < 90 + angleError){
                    RPWM = power * -1;
                    LPWM = power;
                }
                // turn left in position
                else if(angle > -90-angleError && angle < -90 + angleError){
                    RPWM = power ;
                    LPWM = power * -1;
                }
                else if(angle > 0){
                    RPWM = (int)(power * Math.cos(angleRad));
                    LPWM = power ;
                }
                else if(angle < 0){
                    RPWM = power ;
                    LPWM = (int) (power * Math.cos(angleRad));
                }
                cmdBytes = MessageHandler.PWMcmd(RPWM,LPWM);

                tvspeed.setText(String.valueOf(power));
                tvsteer.setText(String.valueOf(angle));
                tvright.setText(String.valueOf(RPWM));
                tvleft.setText(String.valueOf(LPWM));
                tvcmd.setText(MessageHandler.bytesToHex(cmdBytes));
                /* // standard bluetooth
                if (btc != null) {
                    //btc.sendString(angle + "," + power + "\n");//use plain String to send control
                    btc.sendControlBytes(cmdBytes);
                }*/
                if(blc != null){
                    blc.sendControlBytes(cmdBytes);
                }


            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void addListenerOnButtons() {
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                byte[] startcmd = {(byte) 0x80}; // start OI
                byte[] safemod ={(byte)0x83}; // turn into safe mode
                byte[] stopcmd ={(byte)0xAD}; // stop OI
                /*
                if (btc != null) {
                    if(OIstatus){// turn off
                        btc.sendControlBytes(stopcmd);
                        startbtn.setImageResource(android.R.drawable.button_onoff_indicator_off);
                        OIstatus =false;
                    }
                    else { //turn on
                        btc.sendControlBytes(startcmd);
                        //tvcmd.setText(MessageHandler.bytesToHex(startcmd));
                        btc.sendControlBytes(safemod);
                        //tvcmd.setText(MessageHandler.bytesToHex(safemod));
                        startbtn.setImageResource(android.R.drawable.button_onoff_indicator_on);
                        OIstatus =true;
                    }
                    */
                if (blc != null) {
                    if(OIstatus){// turn off
                        blc.sendControlBytes(stopcmd);
                        startbtn.setImageResource(android.R.drawable.button_onoff_indicator_off);
                        OIstatus =false;
                    }
                    else { //turn on
                        blc.sendControlBytes(startcmd);
                        //tvcmd.setText(MessageHandler.bytesToHex(startcmd));
                        blc.sendControlBytes(safemod);
                        //tvcmd.setText(MessageHandler.bytesToHex(safemod));
                        startbtn.setImageResource(android.R.drawable.button_onoff_indicator_on);
                        OIstatus =true;
                    }
                } else {
                    Toast.makeText(MainActivity.this,R.string.connection_err, Toast.LENGTH_SHORT).show();

                }

            }

        });
        for(int i=0;i<4;i++ ){
            final byte helper = (byte)i; // annoying inner claas can't access non-final value of outer class!
            soundbtns[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (btc != null) {
                        byte[] soundcmd = {(byte) 0x8D, helper };
                        btc.sendControlBytes(soundcmd);
                        tvcmd.setText(MessageHandler.bytesToHex(soundcmd));
                    } else {
                        Toast.makeText(MainActivity.this,R.string.connection_err, Toast.LENGTH_SHORT).show();
                    }

                }

            });
        }
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){

            case R.id.action_scan:
                //startActivity(new Intent(this,ScanActivity.class));// old standard scan
                startActivity(new Intent(this,DeviceScanActivity.class));
                break;
            case R.id.action_settings:
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }


}
