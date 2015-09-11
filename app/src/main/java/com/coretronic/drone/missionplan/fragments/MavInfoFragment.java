package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.map.OnMapEventCallback;
import com.coretronic.drone.ui.AircraftCompassWrapView;

/**
 * Created by karot.chuang on 2015/7/23.
 */
public class MavInfoFragment extends Fragment implements OnMapEventCallback {

    private final static int LOCATION_NORMALIZE = 10000000;

    protected MapViewFragment mMapViewFragment;

    private TextView tv_droneAltitude;
    private TextView tv_droneLat;
    private TextView tv_droneSpeed;
    private TextView tv_droneFlightTime;
    private TextView tv_droneLng;
    private AircraftCompassWrapView mAircraftCompassWrapView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
    }

    protected void initMavInfoView(View view, int altitudeViewID, int speedViewId, int flightTimeViewId, int latitudeViewId, int
            longitudeViewId) {
        tv_droneAltitude = (TextView) view.findViewById(altitudeViewID);
        tv_droneSpeed = (TextView) view.findViewById(speedViewId);
        tv_droneLat = (TextView) view.findViewById(flightTimeViewId);
        tv_droneLng = (TextView) view.findViewById(latitudeViewId);
        tv_droneFlightTime = (TextView) view.findViewById(longitudeViewId);
        mAircraftCompassWrapView = new AircraftCompassWrapView(view, R.id.compass_circle, R.id.compass_level, R.id.compass_ruler, R.id
                .compass_direction);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MAV Info
        onAltitdueUpdate(0);
        onSpeedUpdate(0);
        onLocationUpdate(0, 0);
        onFlightTimeUpdate(0);

    }

    final public void onAltitdueUpdate(float altitude) {
        if (tv_droneAltitude == null)
            return;

        tv_droneAltitude.setText(String.format("%.1f m", altitude));
    }

    final public void onSpeedUpdate(float groundSpeed) {
        if (tv_droneSpeed == null)
            return;
        tv_droneSpeed.setText(String.format("%.1f km/h", groundSpeed));
    }

    final public void onLocationUpdate(long droneLat, long droneLng) {
        if (tv_droneLat == null || tv_droneLng == null)
            return;
        tv_droneLat.setText(String.format("%d.%07d,", droneLat / LOCATION_NORMALIZE, droneLat % LOCATION_NORMALIZE));
        tv_droneLng.setText(String.format("%d.%07d", droneLng / LOCATION_NORMALIZE, droneLat % LOCATION_NORMALIZE));
    }

    final public void onFlightTimeUpdate(int flightTime) {
        if (tv_droneFlightTime == null || flightTime < 0)
            return;

        int minutes = Math.min(flightTime / 60, 99);
        int seconds = flightTime % 60;
        tv_droneFlightTime.setText(String.format("%02d:%02d", minutes, seconds));
    }

    final public void onAttitudeUpdate(float yaw, float roll, float pitch) {
        if (mAircraftCompassWrapView == null) {
            return;
        }
        mAircraftCompassWrapView.setDroneYaw(yaw);
        mAircraftCompassWrapView.setDronePitch(pitch);
        mAircraftCompassWrapView.setDroneRoll(roll);
    }

    public void onClick(View v) {
    }

    @Override
    public void onClick(float lat, float lon) {

    }

    @Override
    public void onDragEnd(int index, float lat, float lon) {

    }

    @Override
    public void onPolylineLengthCalculated(int lengthInMeters) {

    }

    @Override
    public void onDragStart() {

    }
}
