package com.coretronic.drone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.missionplan.fragments.MapViewFragment;
import com.coretronic.drone.util.AppConfig;

import java.util.List;

public class MainActivity extends MiniDronesActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SETTING_NAME_2015 = "setting_2015";
    private static final String SETTING_NAME_G2 = "setting_g2";
    private static final String SETTINGS_VALUE = "settings_value";
    private static final char DEGREE_SYMBOL = 0x00B0;   //º
//    private final static DroneDevice DISCONNECTED_FAKE_DEVICE = new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, null, 0);

    private DroneDevice.OnDeviceChangedListener mDeviceChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (isUserLoginCorrect()) {
            switchToMainFragment();
        } else {
            switchToLoginFragment();
        }
        initialSetting(DroneDevice.FAKE_DRONE_DEVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragList = fm.getFragments();
        if (fragList != null && fragList.size() > 0) {
            for (Fragment frag : fragList) {
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
    public void onDeviceAdded(final DroneDevice droneDevice) {
        if (mDeviceChangedListener != null) {
            mDeviceChangedListener.onDeviceAdded(droneDevice);
        }
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        if (mDeviceChangedListener != null) {
            mDeviceChangedListener.onDeviceRemoved(droneDevice);
        }
    }

    @Override
    public void onConnectingDeviceRemoved(DroneDevice droneDevice) {
        Toast.makeText(this, droneDevice.getName() + " Disconnected", Toast.LENGTH_LONG).show();
        initialSetting(DroneDevice.FAKE_DRONE_DEVICE);
    }

    void switchToLoginFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_view, new LoginFragment(), LoginFragment.class.getSimpleName()).commit();
    }

    void switchToMainFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_view, new MainFragment(), MainFragment.class.getSimpleName()).commit();
    }

    public void registerDeviceChangedListener(DroneDevice.OnDeviceChangedListener deviceChangedListener) {
        this.mDeviceChangedListener = deviceChangedListener;
        for (DroneDevice device : getDroneDevices()) {
            deviceChangedListener.onDeviceAdded(device);
        }
    }

    public void unregisterDeviceChangedListener(DroneDevice.OnDeviceChangedListener deviceChangedListener) {
        if (this.mDeviceChangedListener == deviceChangedListener) {
            this.mDeviceChangedListener = null;
        }
    }

    public void initialSetting(DroneDevice droneDevice) {
        switch (droneDevice.getDroneType()) {
            case DroneDevice.DRONE_TYPE_FAKE:
                loadDefaultSettings();
                break;
            default:
                loadDefaultSettings();
                break;
        }
    }

    private void loadDefaultSettings() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MapViewFragment.GOOGLE_LOCATION_REQUEST_CODE) {
            getSupportFragmentManager().findFragmentByTag(MapViewFragment.class.getSimpleName()).onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean isUserLoginCorrect() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        if (!sharedPreferences.getBoolean(AppConfig.SHARED_PREFERENCE_USER_STAY_LOGIN_KEY, false)) {
            return false;
        }

        if (sharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, "").trim().length() == 0) {
            return false;
        }

        return true;
    }
}
