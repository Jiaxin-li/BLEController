package com.robotic.goldenridge.blecontroller;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by jiaxin on 4/7/2016.
 */
public class PathPlanActivity extends AppCompatActivity {
    private static final String TAG = PathPlanActivity.class.getSimpleName();
    Context pContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiti_path);
        pContext= this.getApplicationContext();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"on destroy");
    }

}
