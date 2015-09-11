package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class TapAndGoFragment extends MavInfoFragment {

    private final static int DEFAULT_ALTITUDE = 8;
    private FrameLayout layout_tapAndGoDialog = null;

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
        view.findViewById(R.id.btn_plan_land).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.btn_plan_rtl).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.button_my_location).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.button_drone_location).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.button_fit_map).setOnClickListener(onPlanningBtnClickListener);
        view.findViewById(R.id.btn_map_type).setOnClickListener(onPlanningBtnClickListener);
        // Tap and Go
        layout_tapAndGoDialog = (FrameLayout) view.findViewById(R.id.tap_and_go_container);
         layout_tapAndGoDialog.setVisibility(View.GONE);
    }

    private View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DroneController droneController = mMapViewFragment.getDroneController();
            switch (v.getId()) {
                case R.id.btn_plan_land:
                    if (droneController != null) {
                        droneController.land();
                    }
                    break;
                case R.id.btn_plan_rtl:
                    if (droneController != null) {
                        droneController.returnToLaunch();
                    }
                    break;
                case R.id.button_my_location:
                    mMapViewFragment.setMapToMyLocation();
                    break;
                case R.id.button_drone_location:
                    mMapViewFragment.setMapToDrone();
                    break;
                case R.id.button_fit_map:
                    // TO-DO: fit map to target and drone
                    break;
                case R.id.btn_map_type:
                    mMapViewFragment.changeMapType();
                    break;
            }
        }
    };

    public void hideTapAndGoDialogFragment(boolean isGo, int alt, float lat, float lng) {
        layout_tapAndGoDialog.setVisibility(View.GONE);
        if (!isGo) {
            mMapViewFragment.clearTapMarker();
            return;
        }
        DroneController droneController = mMapViewFragment.getDroneController();

        if (droneController != null) {
            droneController.moveToLocation(MapViewFragment.createNewMission(lat, lng, alt, 0, false, 0, Mission.Type.WAY_POINT));
            mMapViewFragment.setTapGoPath();
        }
    }

    @Override
    public void onClick(float lat, float lon) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        TapAndGoDialogFragment tapAndGoDialogFragment = TapAndGoDialogFragment.newInstance(DEFAULT_ALTITUDE, lat, lon);
        fragmentTransaction
                .replace(R.id.tap_and_go_container, tapAndGoDialogFragment, TapAndGoDialogFragment.class.getSimpleName())
                .commit();
        layout_tapAndGoDialog.setVisibility(View.VISIBLE);

    }
}
