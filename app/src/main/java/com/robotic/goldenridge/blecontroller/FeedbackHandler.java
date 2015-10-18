package com.robotic.goldenridge.blecontroller;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jiaxin on 10/18/2015.
 */
public class FeedbackHandler {
    private Activity act;
    CarControlProtos.FeedBack fb = null;
    public FeedbackHandler(Activity act){
        this.act = act;
    }

    public void handleFeedback(InputStream in){
        try {
           // Log.d("DEC","handleFeedback");
            fb = CarControlProtos.FeedBack.parseDelimitedFrom(in);
            //Log.d("DEC", "parse feedback");
            processFeedback(fb);
            //Log.d("DEC", "sent feedback toprocess ");

        } catch (IOException e) {
            Log.d("DEC","IOException");
            e.printStackTrace();
        }

    }

    public void handleFeedback(byte[] in){

        try {

            fb = CarControlProtos.FeedBack.parseFrom(in);
        } catch (InvalidProtocolBufferException e) {
            Log.d("DEC","InvalidProtocolBufferException");
            e.printStackTrace();
        }
        processFeedback(fb);

    }

    private void processFeedback(CarControlProtos.FeedBack fb){
       // Log.d("DEC","process feedback");
        if(fb != null){

        if(fb.hasDecode()){
            if(fb.getDecode()){

                updateText(act, R.id.decode_value, act.getString(R.string.decode_success)); //getString(R.string.decode_success)
            }
            else{
                updateText(act,R.id.decode_value,act.getString(R.string.decode_error));
            }
        }
        else{
            updateText(act,R.id.decode_value,act.getString(R.string.init_value));
        }

        if(fb.hasFrontDist()){
            updateText(act, R.id.front_value, Integer.toString(fb.getFrontDist()));

        }
        else{
            updateText(act,R.id.front_value,act.getString(R.string.init_value));
        }

        if(fb.hasRearDist()){
            updateText(act, R.id.rear_value, Integer.toString(fb.getRearDist()));

        }
        else{
            updateText(act, R.id.rear_value, act.getString(R.string.init_value));
        }
        }
        else{
            Log.d("DEC","fb Null");
        }

    }

    public static void updateText(Activity act,int ID, final String text)
    {

        final TextView loadingText = (TextView) act.findViewById(ID);
        act.runOnUiThread(new Runnable()
        {

            public void run()
            {
                loadingText.setText(text);

            }

        });
    }
}
