package com.coretronic.drone;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.ui.ViewManager;

import java.util.List;

/**
 * Created by jiaLian on 15/4/1.
 */
public class LandscapeFragmentActivity extends MiniDronesActivity {
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
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragList = fm.getFragments();
        if (fragList != null && fragList.size() > 0) {
            for (Fragment frag : fm.getFragments()) {
                if (frag == null) {
                    continue;
                }
                if (frag.isVisible()) {
                    FragmentManager childFm = frag.getChildFragmentManager();
                    if (childFm.getBackStackEntryCount() > 0) {
                        childFm.popBackStack();
                        return;
                    }
                }
            }
        }
        super.onBackPressed();
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
