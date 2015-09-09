package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class TapAndGoFragment extends MavInfoFragment {
    private TextView tv_droneAltitude = null;
    private TextView tv_droneSpeed = null;
    private TextView tv_droneLat = null;
    private TextView tv_droneLng = null;
    private TextView tv_droneFlightTime = null;

    private FragmentManager fragmentChildManager = null;
    private DroneController drone = null;

    private FrameLayout layout_tapAndGoDialog = null;

    public interface TapAndGoInterface extends PlanningFragment.PlanningInterface {
        void setTapGoPath();

        void clearTapMarker();

        void changeMapType();
    }

    private TapAndGoInterface callMainFragmentInterface = null;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callMainFragmentInterface = (TapAndGoInterface) getParentFragment();
        drone = ((MainActivity) getActivity()).getDroneController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_tap_and_go, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentChildManager = getChildFragmentManager();

        // Go & Stop & Location Button Panel
        final Button landButton = (Button) view.findViewById(R.id.btn_plan_land);
        landButton.setOnClickListener(onPlanningBtnClickListener);

        final Button rtlButton = (Button) view.findViewById(R.id.btn_plan_rtl);
        rtlButton.setOnClickListener(onPlanningBtnClickListener);

        final Button myLocationButton = (Button) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(onPlanningBtnClickListener);

        final Button droneLocationButton = (Button) view.findViewById(R.id.button_drone_location);
        droneLocationButton.setOnClickListener(onPlanningBtnClickListener);

        final Button fitMapButton = (Button) view.findViewById(R.id.button_fit_map);
        fitMapButton.setOnClickListener(onPlanningBtnClickListener);

        final Button mapTypeButton = (Button) view.findViewById(R.id.btn_map_type);
        mapTypeButton.setOnClickListener(onPlanningBtnClickListener);

        // MAV Info
        tv_droneAltitude = (TextView) view.findViewById(R.id.altitude_text);
        tv_droneAltitude.setText("0m");
        tv_droneSpeed = (TextView) view.findViewById(R.id.speed_text);
        tv_droneSpeed.setText("0 km/h");
        tv_droneLat = (TextView) view.findViewById(R.id.location_lat_text);
        tv_droneLat.setText("0.0000000,");
        tv_droneLng = (TextView) view.findViewById(R.id.location_lng_text);
        tv_droneLng.setText("0.0000000");
        tv_droneFlightTime = (TextView) view.findViewById(R.id.flight_time_text);
        tv_droneFlightTime.setText("00:00");

        // Tap and Go
        layout_tapAndGoDialog = (FrameLayout) view.findViewById(R.id.tap_and_go_container);
        layout_tapAndGoDialog.setVisibility(View.GONE);
    }

    private View.OnClickListener onPlanningBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_plan_land:
                    if (drone != null) {
                        drone.land();
                    }
                    break;
                case R.id.btn_plan_rtl:
                    if (drone != null) {
                        drone.returnToLaunch();
                    }
                    break;
                case R.id.button_my_location:
                    callMainFragmentInterface.setMapToMyLocation();
                    break;
                case R.id.button_drone_location:
                    callMainFragmentInterface.setMapToDrone();
                    break;
                case R.id.button_fit_map:
                    // TO-DO: fit map to target and drone
                    break;
                case R.id.btn_map_type:
                    callMainFragmentInterface.changeMapType();
                    break;
            }
        }
    };


    private Mission createNewMission(float latitude, float longitude, float altitude,
                                     int waitSeconds, boolean autoContinue, int radius, Mission.Type type) {
        Mission.Builder builder = new Mission.Builder();

        builder.setLatitude(latitude);
        builder.setLongitude(longitude);
        builder.setAltitude(altitude);
        builder.setWaitSeconds(waitSeconds);
        builder.setAutoContinue(autoContinue);
        builder.setRadius(radius);
        builder.setType(type);

        return builder.create();
    }

    // Public Method
    @Override
    public void setMavInfoAltitude(float altitude) {
        if (tv_droneAltitude == null)
            return;

        tv_droneAltitude.setText(String.format("%.1f", altitude) + "m");
    }

    @Override
    public void setMavInfoSpeed(float groundSpeed) {
        if (tv_droneSpeed == null)
            return;

        tv_droneSpeed.setText(String.format("%.1f", groundSpeed) + " km/h");
    }

    @Override
    public void setMavInfoLocation(long droneLat, long droneLng) {
        if (tv_droneLat == null)
            return;
        String latLongStr = String.valueOf(droneLat);
        String lngLongStr = String.valueOf(droneLng);
        int latDecimalPos = latLongStr.length() - 7;
        int lngDecimalPos = lngLongStr.length() - 7;

        String lat_output, lng_output;
        if (latDecimalPos <= 0) {
            lat_output = String.format("0.%07d", droneLat);
        } else {
            lat_output = latLongStr.substring(0, latDecimalPos) + "." + latLongStr.substring(latDecimalPos);
        }

        if (lngDecimalPos <= 0) {
            lng_output = String.format("0.%07d", droneLng);
        } else {
            lng_output = lngLongStr.substring(0, lngDecimalPos) + "." + lngLongStr.substring(lngDecimalPos);
        }

        tv_droneLat.setText(lat_output + ",");
        tv_droneLng.setText(lng_output);
    }

    @Override
    public void setMavInfoFlightTime(int flightTime) {
        if (tv_droneFlightTime == null || flightTime < 1)
            return;
        String showTime = "";
        if (flightTime >= 6000) {
            showTime = "99:99";
            tv_droneFlightTime.setText(showTime);
            return;
        }
        int min = flightTime / 60;
        if (min < 10) {
            showTime += "0";
        }
        showTime += min;
        showTime += ":";

        int sec = flightTime % 60;
        if (sec < 10) {
            showTime += "0";
        }
        showTime += sec;
        tv_droneFlightTime.setText(showTime);
    }

    public void showTapAndGoDialogFragment(int altitude, float latitude, float longitude) {
        FragmentTransaction fragmentTransaction = fragmentChildManager.beginTransaction();
        TapAndGoDialogFragment tapAndGoDialogFragment = TapAndGoDialogFragment.newInstance(altitude, latitude, longitude);
        fragmentTransaction
                .replace(R.id.tap_and_go_container, tapAndGoDialogFragment, "TapAndGoFragment")
                .commit();
        layout_tapAndGoDialog.setVisibility(View.VISIBLE);
    }

    public void hideTapAndGoDialogFragment(boolean isGo, int alt, float lat, float lng) {
        layout_tapAndGoDialog.setVisibility(View.GONE);
        if (!isGo) {
            callMainFragmentInterface.clearTapMarker();
            return;
        }

        if (drone != null) {
            drone.moveToLocation(createNewMission(lat, lng, alt, 0, false, 0, Mission.Type.WAY_POINT));
            callMainFragmentInterface.setTapGoPath();
        }
    }
}
