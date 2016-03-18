package com.coretronic.drone.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneStatus;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.annotation.Callback;

/**
 * Created by karot.chuang on 2016/3/18.
 */
public class SettingChildFragment extends Fragment  implements DroneStatus.StatusChangedListener {

    protected MainActivity mMainActivity;
    protected Fragment mMainFragment;
    protected DroneController mDroneController;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
        mMainFragment = getParentFragment();
        mDroneController = mMainActivity.getDroneController();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity.registerDroneStatusChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMainActivity.unregisterDroneStatusChangedListener(this);
    }

    @Override
    public void onStatusUpdate(Callback.Event event, DroneStatus droneStatus) {
        switch (event) {
            case ON_RC_CONNECT_UPDATE:
                ((SettingsMaingFragment) mMainFragment).setRCConnectedIndicator(droneStatus.ismArmingCheckRCConnect());
                break;
            case ON_HEARTBEAT:
                ((SettingsMaingFragment) mMainFragment).updateHeartbeatTimeStamp(droneStatus.getLastHeartbeatTime());
                break;
        }
    }
}
