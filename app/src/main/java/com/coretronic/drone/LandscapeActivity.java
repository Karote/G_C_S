package com.coretronic.drone;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.coretronic.drone.ui.ViewManager;
import com.coretronic.dronecontrol.activity.MiniDronesActivity;
import com.coretronic.dronecontrol.service.DroneDevice;

/**
 * Created by jiaLian on 15/4/1.
 */
public class LandscapeActivity extends MiniDronesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewManager.unbindDrawables(findViewById(android.R.id.content));
        System.gc();
    }

    @Override
    public void onDeviceAdded(DroneDevice droneDevice) {

    }

    @Override
    public void onDeviceRemoved(DroneDevice droneDevice) {

    }

    @Override
    public void OnBatteryUpdate(int battery) {

    }
}
