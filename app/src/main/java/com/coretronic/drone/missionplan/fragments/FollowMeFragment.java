package com.coretronic.drone.missionplan.fragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class FollowMeFragment extends MavInfoFragment implements DroneController.FollowMeStateListener {
    private static final String TAG = FollowMeFragment.class.getSimpleName();

    public static final int DEFAULT_ALTITUDE = 8;

    private LinearLayout layout_start_follow = null;
    private Button btn_stop_follow = null;

    private TextView tv_droneAltitude, tv_droneSpeed, tv_droneLatLng;

    OnFollowMeClickListener mCallback;
    private AbstractWheel followMeAltitudeWheel;

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
        layout_start_follow = (LinearLayout) view.findViewById(R.id.layout_start_follow);

        followMeAltitudeWheel = (AbstractWheel) view.findViewById(R.id.follow_me_altitude_wheel);
        followMeAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 20, "%02d"));
        followMeAltitudeWheel.setCyclic(false);
        followMeAltitudeWheel.setCurrentItem(DEFAULT_ALTITUDE);

        final RelativeLayout btn_start_follow = (RelativeLayout) view.findViewById(R.id.btn_start_follow);
        btn_start_follow.setOnClickListener(onFollowBtnClickListener);

        btn_stop_follow = (Button) view.findViewById(R.id.btn_stop_follow);
        btn_stop_follow.setOnClickListener(onFollowBtnClickListener);
        btn_stop_follow.setVisibility(View.GONE);

        // Location Button Panel
        final Button myLocationButton = (Button) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(onFollowBtnClickListener);

        final Button droneLocationButton = (Button) view.findViewById(R.id.button_drone_location);
        droneLocationButton.setOnClickListener(onFollowBtnClickListener);

        final Button fitMapButton = (Button) view.findViewById(R.id.button_fit_map);
        fitMapButton.setOnClickListener(onFollowBtnClickListener);

        // MAV Info
        tv_droneAltitude = (TextView) view.findViewById(R.id.altitude_text);
        tv_droneAltitude.setText("0m");
        tv_droneSpeed = (TextView) view.findViewById(R.id.speed_text);
        tv_droneSpeed.setText("0 km/h");
        tv_droneLatLng = (TextView) view.findViewById(R.id.location_text);
        tv_droneLatLng.setText("0.000000, 0.000000");
    }

    @Override
    public void setMavInfoAltitude(float altitude) {
        String tx_alt = String.format("%d", (int) altitude);
        tv_droneAltitude.setText(tx_alt + "m");
    }

    @Override
    public void setMavInfoSpeed(float groundSpeed) {
        tv_droneSpeed.setText(groundSpeed + " km/h");
    }

    @Override
    public void setMavInfoLocation(double droneLat, double droneLng) {
        tv_droneLatLng.setText(String.valueOf(droneLat) + ", " + String.valueOf(droneLng));
    }

    View.OnClickListener onFollowBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final DroneController drone = ((MainActivity) getActivity()).getDroneController();
            if (drone == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.btn_start_follow:
                    drone.startFollowMe(followMeAltitudeWheel.getCurrentItem(), FollowMeFragment.this);
                    layout_start_follow.setVisibility(View.GONE);
                    btn_stop_follow.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_stop_follow:
                    drone.startFollowMe(-1, FollowMeFragment.this);
                    layout_start_follow.setVisibility(View.VISIBLE);
                    btn_stop_follow.setVisibility(View.GONE);
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
