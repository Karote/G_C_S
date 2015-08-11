package com.coretronic.drone.missionplan.fragments;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class FollowMeFragment extends MavInfoFragment implements DroneController.FollowMeStateListener {
    private static final String TAG = FollowMeFragment.class.getSimpleName();

    private static final int DEFAULT_ALTITUDE = 8;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 20;

    private TextView tvAltitude = null;
    private TextView tvSpeed = null;
    private TextView tvLat = null;
    private TextView tvLng = null;
    private TextView tv_droneFlightTime = null;


    OnFollowMeClickListener mCallback;

    private AbstractWheel altitudeWheel = null;
    private NumericWheelAdapter altitudeAdapter = null;
    private RelativeLayout startFollowMe = null;
    private Button stopFollowMe = null;

    public interface OnFollowMeClickListener extends PlanningFragment.MissionAdapterListener {
        void fitMapShowDroneAndMe();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (OnFollowMeClickListener) getActivity().getSupportFragmentManager().findFragmentByTag("fragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_follow_me, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FollowMe Control Panel
        startFollowMe = (RelativeLayout) view.findViewById(R.id.rl_start_follow);
        startFollowMe.setOnClickListener(onFollowBtnClickListener);

        stopFollowMe = (Button) view.findViewById(R.id.btn_stop_follow);
        stopFollowMe.setOnClickListener(onFollowBtnClickListener);
        stopFollowMe.setVisibility(View.GONE);

        altitudeWheel = (AbstractWheel) view.findViewById(R.id.follow_me_altitude_wheel);
        altitudeAdapter = new NumericWheelAdapter(getActivity().getBaseContext(), MIN_VALUE, MAX_VALUE, "%01d");
        altitudeAdapter.setItemResource(R.layout.text_wheel_number);
        altitudeWheel.setViewAdapter(altitudeAdapter);
        altitudeWheel.setCyclic(false);
        altitudeWheel.setCurrentItem(DEFAULT_ALTITUDE - MIN_VALUE);
        altitudeWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                if (startFollowMe.getVisibility() == View.GONE) {
                    Log.d(TAG, "onScrollingFinished: " + (altitudeWheel.getCurrentItem() + MIN_VALUE));
                    ((MainActivity) getActivity()).getDroneController().startFollowMe(altitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                }
            }
        });

        // Location Button Panel
        final Button myLocationButton = (Button) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(onFollowBtnClickListener);

        final Button droneLocationButton = (Button) view.findViewById(R.id.button_drone_location);
        droneLocationButton.setOnClickListener(onFollowBtnClickListener);

        final Button fitMapButton = (Button) view.findViewById(R.id.button_fit_map);
        fitMapButton.setOnClickListener(onFollowBtnClickListener);

        // MAV Info
        tvAltitude = (TextView) view.findViewById(R.id.altitude_text);
        tvAltitude.setText("0m");
        tvSpeed = (TextView) view.findViewById(R.id.speed_text);
        tvSpeed.setText("0 km/h");
        tvLat = (TextView) view.findViewById(R.id.location_lat_text);
        tvLat.setText("0.000000,");
        tvLng = (TextView) view.findViewById(R.id.location_lng_text);
        tvLng.setText("0.000000");
        tv_droneFlightTime = (TextView) view.findViewById(R.id.flight_time_text);
        tv_droneFlightTime.setText("00:00");
    }

    @Override
    public void setMavInfoAltitude(float altitude) {
        if (tvAltitude == null)
            return;

        String tx_alt = String.format("%d", (int) altitude);
        tvAltitude.setText(tx_alt + "m");
    }

    @Override
    public void setMavInfoSpeed(float groundSpeed) {
        if (tvSpeed == null)
            return;

        tvSpeed.setText(groundSpeed + " km/h");
    }

    @Override
    public void setMavInfoLocation(long droneLat, long droneLng) {
        if (tvLat == null)
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

        tvLat.setText(lat_output + ",");
        tvLng.setText(lng_output);
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

    View.OnClickListener onFollowBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final DroneController drone = ((MainActivity) getActivity()).getDroneController();
//            if (drone == null) {
//                return;
//            }
            switch (v.getId()) {
                case R.id.rl_start_follow:
                    drone.startFollowMe(altitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                    startFollowMe.setVisibility(View.GONE);
                    stopFollowMe.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_stop_follow:
                    drone.startFollowMe(0, FollowMeFragment.this);
                    startFollowMe.setVisibility(View.VISIBLE);
                    stopFollowMe.setVisibility(View.GONE);
                    break;
                case R.id.button_my_location:
                    mCallback.setMapToMyLocation();
                    break;
                case R.id.button_drone_location:
                    mCallback.setMapToDrone();
                    break;
                case R.id.button_fit_map:
                    mCallback.fitMapShowDroneAndMe();
                    break;
            }
        }
    };

    // Implement DroneController.FollowMeStateListener
    @Override
    public void onStart(float latOffset, float longOffset) {
        Toast.makeText(getActivity(), "Start Follow Me", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMissionItemUpdated(float lat, float lon, float yaw, int clock) {

    }

    @Override
    public void onLocationUpdated(float lat, float lon) {

    }

    @Override
    public void onLocationUpdated(Location location) {

    }
    // End DroneController.FollowMeStateListener
}
