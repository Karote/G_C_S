package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class TapAndGoFragment extends MapChildFragment {

    private final static int DEFAULT_ALTITUDE = 8;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_tap_and_go, container, false);
    }

    public void executeTapAndGoMission(int alt, float lat, float lng) {
        DroneController droneController = mMapViewFragment.getDroneController();
        if (droneController != null) {
            droneController.moveToLocation(MapViewFragment.createNewMission(lat, lng, alt, 0, false, 0, Mission.Type.WAY_POINT));
            mMapViewFragment.setTapGoPath();
        }
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        TapAndGoDialogFragment tapAndGoDialogFragment = TapAndGoDialogFragment.newInstance(DEFAULT_ALTITUDE, lat, lon);
        fragmentTransaction
                .replace(R.id.tap_and_go_container, tapAndGoDialogFragment, TapAndGoDialogFragment.class.getSimpleName())
                .commit();
    }
}
