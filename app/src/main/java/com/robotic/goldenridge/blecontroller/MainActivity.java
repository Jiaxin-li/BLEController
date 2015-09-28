package com.robotic.goldenridge.blecontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.robotic.goldenridge.blecontroller.JoystickView.OnJoystickMoveListener;

public class MainActivity extends AppCompatActivity {
    public static JoystickView joystick;
    public static BluetoothConnection btc = null ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {
                // Scale speed
                if (angle < -90 || angle > 90) {
                    power *= -1;
                }

                // X to Y adapter
                if (angle > 90) {
                    angle = 180 - angle;
                } else if (angle < -90) {
                    angle = -(180 + angle);
                }
                if(btc != null){
                    btc.sendToManualMode(angle + ":" + power);
                }


                TextView tvspeed = (TextView)findViewById(R.id.speed_value);
                tvspeed.setText(String.valueOf(power));
                TextView tvsteer = (TextView)findViewById(R.id.steer_value);
                tvsteer.setText(String.valueOf(angle));
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
        joystick.setEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                startActivity(new Intent(this,ScanActivity.class));
                break;
            case R.id.action_settings:
                break;
            default:

        }

        return super.onOptionsItemSelected(item);
    }
}
