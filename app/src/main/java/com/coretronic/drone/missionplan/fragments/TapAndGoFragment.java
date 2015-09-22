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
public class TapAndGoFragment extends MavInfoFragment {

    private final static int DEFAULT_ALTITUDE = 8;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_plan_tap_and_go, container, false);
        initMavInfoView(view, R.id.altitude_text, R.id.speed_text, R.id.location_lat_text, R.id.location_lng_text, R.id.flight_time_text);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Go & Stop & Location Button Panel
        view.findViewById(R.id.plan_stop_button).setVisibility(View.GONE);
        view.findViewById(R.id.drone_landing_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.drone_rtl_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.my_location_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.drone_location_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.fit_map_button).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.map_type_button).setOnClickListener(onPlanningBtnClickListener);
        // Tap and Go
    }

    private View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DroneController droneController = mMapViewFragment.getDroneController();
            switch (v.getId()) {
                case R.id.drone_landing_button:
                    if (droneController != null) {
                        droneController.land();
                    }
                    break;
                case R.id.drone_rtl_button:
                    if (droneController != null) {
                        droneController.returnToLaunch();
                    }
                    break;
                case R.id.my_location_button:
                    mMapViewFragment.setMapToMyLocation();
                    break;
                case R.id.drone_location_button:
                    mMapViewFragment.setMapToDrone();
                    break;
                case R.id.fit_map_button:
                    // TO-DO: fit map to target and drone
                    break;
                case R.id.map_type_button:
                    mMapViewFragment.changeMapType();
                    break;
            }
        }
    };

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
