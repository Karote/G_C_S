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

    private TextView mDroneAltitudeTextView;
    private TextView mDroneLatitudeTextView;
    private TextView mDroneLongitudeTextView;
    private TextView mDroneSpeedTextView;
    private TextView mDroneFlightTimeTextView;
    private AircraftCompassWrapView mAircraftCompassWrapView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
    }

    protected void initMavInfoView(View view, int altitudeViewID, int speedViewId, int flightTimeViewId, int latitudeViewId, int
            longitudeViewId) {
        mDroneAltitudeTextView = (TextView) view.findViewById(altitudeViewID);
        mDroneSpeedTextView = (TextView) view.findViewById(speedViewId);
        mDroneLatitudeTextView = (TextView) view.findViewById(flightTimeViewId);
        mDroneLongitudeTextView = (TextView) view.findViewById(latitudeViewId);
        mDroneFlightTimeTextView = (TextView) view.findViewById(longitudeViewId);
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
        if (mDroneAltitudeTextView == null)
            return;

        mDroneAltitudeTextView.setText(String.format("%.1f m", altitude));
    }

    final public void onSpeedUpdate(float groundSpeed) {
        if (mDroneSpeedTextView == null)
            return;
        mDroneSpeedTextView.setText(String.format("%.1f km/h", groundSpeed));
    }

    final public void onLocationUpdate(long droneLat, long droneLng) {
        if (mDroneLatitudeTextView == null || mDroneLongitudeTextView == null)
            return;
        mDroneLatitudeTextView.setText(String.format("%d.%07d,", droneLat / LOCATION_NORMALIZE, droneLat % LOCATION_NORMALIZE));
        mDroneLongitudeTextView.setText(String.format("%d.%07d", droneLng / LOCATION_NORMALIZE, droneLat % LOCATION_NORMALIZE));
    }

    final public void onFlightTimeUpdate(int flightTime) {
        if (mDroneFlightTimeTextView == null || flightTime < 0)
            return;

        int minutes = Math.min(flightTime / 60, 99);
        int seconds = flightTime % 60;
        mDroneFlightTimeTextView.setText(String.format("%02d:%02d", minutes, seconds));
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
    public void onMapClickEvent(float lat, float lon) {

    }

    @Override
    public void onMapDragEndEvent(int index, float lat, float lon) {

    }

    @Override
    public void onMapPolylineLengthCalculated(int lengthInMeters) {

    }

    @Override
    public void onMapDragStartEvent() {

    }
}
