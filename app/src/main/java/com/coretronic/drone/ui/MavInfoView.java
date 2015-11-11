package com.coretronic.drone.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coretronic.drone.DroneController.DroneStatus;
import com.coretronic.drone.DroneController.StatusChangedListener;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback.Event;

/**
 * Created by Poming on 2015/10/26.
 */
public class MavInfoView implements StatusChangedListener {

    private final static int LOCATION_NORMALIZE = 10000000;

    private ProgressWithTextWrap mDroneAltitudeTextWrap;
    private ProgressWithTextWrap mDroneGroundSpeedTextWrap;

    private TextView mDroneLatitudeTextView;
    private TextView mDroneLongitudeTextView;
    private TextView mDroneFlightTimeTextView;
    private AircraftCompassWrapView mAircraftCompassWrapView;
    final private View mMavInfoView;

    public MavInfoView(View view, int mavInfoPanelId) {
        mMavInfoView = view.findViewById(mavInfoPanelId);
        initAltitudeView(R.id.altitude_text, R.id.altitude_progress_bar);
        initGroundSpeedView(R.id.ground_speed_text, R.id.ground_speed_progress_bar);
        initMavInfoView(R.id.location_lat_text, R.id.location_lng_text, R.id.flight_time_text);
    }

    private void initMavInfoView(int flightTimeViewId, int latitudeViewId, int longitudeViewId) {
        mDroneLatitudeTextView = (TextView) mMavInfoView.findViewById(flightTimeViewId);
        mDroneLongitudeTextView = (TextView) mMavInfoView.findViewById(latitudeViewId);
        mDroneFlightTimeTextView = (TextView) mMavInfoView.findViewById(longitudeViewId);
        mAircraftCompassWrapView = new AircraftCompassWrapView(mMavInfoView, R.id.compass_circle, R.id.compass_level, R.id.compass_ruler,
                R.id.compass_direction);

    }

    private void initAltitudeView(int altitudeTextViewId, int altitudeProgressId) {

        TextView textView = (TextView) mMavInfoView.findViewById(altitudeTextViewId);
        SeekArc seekArc = (SeekArc) mMavInfoView.findViewById(altitudeProgressId);
        mDroneAltitudeTextWrap = new ProgressWithTextWrap(textView, seekArc);
    }

    private void initGroundSpeedView(int verticalSpeedTextViewId, int verticalSpeedProgressId) {
        TextView textView = (TextView) mMavInfoView.findViewById(verticalSpeedTextViewId);
        SeekArc seekArc = (SeekArc) mMavInfoView.findViewById(verticalSpeedProgressId);
        mDroneGroundSpeedTextWrap = new ProgressWithTextWrap(textView, seekArc);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        onAltitudeUpdate(0);
        onGroundSpeedUpdate(0);
        onLocationUpdate(0, 0);
        onFlightTimeUpdate(0);
    }

    final private void onAltitudeUpdate(float altitude) {
        if (mDroneAltitudeTextWrap == null) {
            return;
        }
        mDroneAltitudeTextWrap.setValue(altitude);
    }

    final private void onGroundSpeedUpdate(float groundSpeed) {
        if (mDroneGroundSpeedTextWrap == null) {
            return;
        }
        mDroneGroundSpeedTextWrap.setValue(groundSpeed);
    }

    final private void onLocationUpdate(long droneLat, long droneLng) {
        if (mDroneLatitudeTextView == null || mDroneLongitudeTextView == null) {
            return;
        }
        mDroneLatitudeTextView.setText(String.format("%d.%07d,", droneLat / LOCATION_NORMALIZE, droneLat % LOCATION_NORMALIZE));
        mDroneLongitudeTextView.setText(String.format("%d.%07d", droneLng / LOCATION_NORMALIZE, droneLat % LOCATION_NORMALIZE));
    }

    final private void onFlightTimeUpdate(int flightTime) {
        if (mDroneFlightTimeTextView == null || flightTime < 0) {
            return;
        }
        int minutes = Math.min(flightTime / 60, 99);
        int seconds = flightTime % 60;
        mDroneFlightTimeTextView.setText(String.format("%02d:%02d", minutes, seconds));
    }

    final private void onAttitudeUpdate(float yaw, float roll, float pitch) {
        if (mAircraftCompassWrapView == null) {
            return;
        }
        mAircraftCompassWrapView.setDroneYaw(yaw);
        mAircraftCompassWrapView.setDronePitch(pitch);
        mAircraftCompassWrapView.setDroneRoll(roll);
    }

    @Override
    public void onStatusUpdate(Event event, DroneStatus droneStatus) {

        switch (event) {
            case ON_ALTITUDE_UPDATE:
                onAltitudeUpdate(droneStatus.getAltitude());
                break;
            case ON_LOCATION_UPDATE:
                onLocationUpdate(droneStatus.getLatitude(), droneStatus.getLongitude());
                break;
            case ON_ATTITUDE_UPDATE:
                onAttitudeUpdate(droneStatus.getYaw(), droneStatus.getRoll(), droneStatus.getPitch());
                break;
            case ON_GROUND_SPEED_UPDATE:
                onGroundSpeedUpdate(droneStatus.getGroundSpeed());
                break;
            case ON_FLIGHT_DURATION_UPDATE:
                onFlightTimeUpdate(droneStatus.getDuration());
                break;
        }
    }

    public void setVisibility(int visibility) {
        mMavInfoView.setVisibility(visibility);
    }

    private class ProgressWithTextWrap {

        private TextView mTextView;
        private SeekArc mSeekArc;

        public ProgressWithTextWrap(TextView textView, SeekArc seekArc) {
            mTextView = textView;
            mSeekArc = seekArc;
        }

        private void setValue(float value) {

            if (mTextView != null) {
                mTextView.setText(String.format("%.1f", value));
            }
            if (mSeekArc != null) {
                mSeekArc.updateProgress((int) (value * 10));
            }
        }
    }

}
