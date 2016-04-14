package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Builder;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.util.ConstantValue;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class TapAndGoFragment extends MapChildFragment {

    private final static boolean DEFAULT_AUTO_CONTINUE = true;
    private final static int DEFAULT_WAIT_SECONDS = 0;
    private final static int DEFAULT_RADIUS = 0;
    private final static Type DEFAULT_TYPE = Type.WAY_POINT;
    private Mission.Builder mMissionBuilder;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMissionBuilder = new Builder();
        mMissionBuilder.setAltitude(ConstantValue.ALTITUDE_DEFAULT_VALUE).setType(DEFAULT_TYPE).setAutoContinue(DEFAULT_AUTO_CONTINUE)
                .setWaitSeconds(DEFAULT_WAIT_SECONDS).setRadius(DEFAULT_RADIUS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_tap_and_go, container, false);
    }

    public void executeTapAndGoMission(int alt, float lat, float lng) {
        DroneController droneController = mMapViewFragment.getDroneController();
        if (droneController != null) {
            droneController.moveToLocation(mMissionBuilder.setAltitude(alt).setLatitude(lat).setLongitude(lng).create());
            mMapViewFragment.setTapGoPath();
            mMapViewFragment.setDroneControlBarVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        TapAndGoDialogFragment tapAndGoDialogFragment = TapAndGoDialogFragment.newInstance(ConstantValue.ALTITUDE_DEFAULT_VALUE, lat, lon);
        fragmentTransaction
                .replace(R.id.tap_and_go_container, tapAndGoDialogFragment, TapAndGoDialogFragment.class.getSimpleName())
                .commit();
    }

    public void clearTapMarker() {
        mMapViewFragment.clearTapMarker();
    }
}
