package com.coretronic.drone;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.ui.ViewManager;

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
    public void onBatteryUpdate(int battery) {

    }

    @Override
    public void onAltitudeUpdate(float altitude) {

    }

    @Override
    public void onRadioSignalUpdate(int rssi) {

    }

    @Override
    public void onSpeedUpdate(float groundSpeed) {

    }
}
