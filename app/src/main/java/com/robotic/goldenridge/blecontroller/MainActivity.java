package com.robotic.goldenridge.blecontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.robotic.goldenridge.blecontroller.JoystickView.OnJoystickMoveListener;

public class MainActivity extends AppCompatActivity { //
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int angleError =10;

    public static JoystickView joystick;
    public static BSConnection btc = null ;
    //test for BLE

    public static BLEConnection blc = null;
    public static Context mContext;

    // for settings
    private static final int RESULT_SETTINGS = 1;
    private static SharedPreferences prefs;

    private  ImageButton startbtn;
    private  ImageButton soundbtn;
    private  ImageButton forwardLeft;
    private  ImageButton forward;
    private  ImageButton forwardRight;
    private  ImageButton spinCCW;
    private  ImageButton spinCW;
    private  ImageButton backwardLeft;
    private  ImageButton back;
    private  ImageButton backwardRight;
    private  ImageButton dock;



    private final double RAD = 57.2957795;
    boolean OIstatus = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mContext= this.getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);


        startbtn = (ImageButton) findViewById(R.id.startbtn);
        soundbtn= (ImageButton) findViewById(R.id.soundbtn);
        forwardLeft =(ImageButton) findViewById(R.id.forwardLeft);
        forward = (ImageButton) findViewById(R.id.forward);
        forwardRight = (ImageButton) findViewById(R.id.forwardRight);
        spinCCW = (ImageButton) findViewById(R.id.ccw);
        spinCW =  (ImageButton) findViewById(R.id.cw);
        dock = (ImageButton) findViewById(R.id.park);
        backwardLeft = (ImageButton) findViewById(R.id.backleft);
        back = (ImageButton) findViewById(R.id.back);
        backwardRight = (ImageButton) findViewById(R.id.backright);


        // test for BLE
        startService(new Intent(getBaseContext(), BluetoothLeService.class));

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
                // logical independent solution
                MessageHandler.drivePWM(RPWM,LPWM);

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
                if (OIstatus) {// turn off
                    MessageHandler.stopcmd();
                    startbtn.setImageResource(R.drawable.ic_key_black_48dp);
                    OIstatus = false;
                } else { //turn on
                    MessageHandler.startcmd();
                    startbtn.setImageResource(R.drawable.ic_key_white_48dp);
                    OIstatus = true;
                }
            }
        });

        dock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageHandler.dock();
            }
        });

        forwardLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.forwardLeft();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                }
                return false;
            }
        });

        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.forward();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

        forwardRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.forwardRight();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

        forwardLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.forwardLeft();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                }
                return false;
            }
        });

        spinCW.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.spinCW();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

        spinCCW.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.spinCCW();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

        backwardLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.backwardLeft();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.backward();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

        backwardRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    MessageHandler.backwardRight();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MessageHandler.stop();
                } return false;
            }
        });

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
                startActivity(new Intent(this,DeviceScanActivity.class));
                break;
            case R.id.action_settings:
                Intent i = new Intent(this, UserSettingActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean isLE(){
        return  prefs.getBoolean("isBLE", true);
    }

    public static int getSpeed(){
        // constrain the value from 0 - 500
        return constrain(Integer.valueOf(prefs.getString("speed", "0")),0,500);
    }

    public static int getRadius(){ // getInt() Throws ClassCastException if there is a preference with this name that is not an int.
        return constrain(Integer.valueOf(prefs.getString("radius", "100")), 0, 2000);
    }
    private static int constrain(int ori, int min, int max){
        if( ori < min) ori = min;
        if( ori > max ) ori = max;
        return ori;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), BluetoothLeService.class));
    }




}
