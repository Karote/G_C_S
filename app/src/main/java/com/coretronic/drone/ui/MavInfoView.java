package com.coretronic.drone.ui;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coretronic.drone.DroneStatus;
import com.coretronic.drone.DroneStatus.StatusChangedListener;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback.Event;
import com.coretronic.drone.util.ConstantValue;

/**
 * Created by Poming on 2015/10/26.
 */
public class MavInfoView implements StatusChangedListener {
    private ProgressWithTextWrap mDroneAltitudeTextWrap;
    private ProgressWithTextWrap mDroneGroundSpeedTextWrap;
    private ProgressWithTextWrap mDroneClimbSpeedTextWrap;

    private TextView mDroneLatitudeTextView;
    private TextView mDroneLongitudeTextView;
    private TextView mDroneFromHomeTextView;
    private TextView mDroneNextTargetTextView;
    private AircraftCompassWrapView mAircraftCompassWrapView;
    final private View mMavInfoView;

    private Location mDroneLocation;
    private Location mHomeLocation;
    private Location mTargetLocation;

    public MavInfoView(View view, int mavInfoPanelId) {
        mMavInfoView = view.findViewById(mavInfoPanelId);
        initAltitudeView(R.id.altitude_text, R.id.altitude_progress_bar);
        initGroundSpeedView(R.id.ground_speed_text, R.id.ground_speed_progress_bar);
        initClimbSpeedView(R.id.climb_speed_text, R.id.climb_speed_progress_bar);
        initMavInfoView(R.id.location_lat_text, R.id.location_lng_text, R.id.from_home_distance_text, R.id.target_distance_text);
    }

    private void initMavInfoView(int flightTimeViewId, int latitudeViewId, int fromHomeViewId, int nextTargetViewId) {
        mDroneLatitudeTextView = (TextView) mMavInfoView.findViewById(flightTimeViewId);
        mDroneLongitudeTextView = (TextView) mMavInfoView.findViewById(latitudeViewId);
        mDroneFromHomeTextView = (TextView) mMavInfoView.findViewById(fromHomeViewId);
        mDroneNextTargetTextView = (TextView) mMavInfoView.findViewById(nextTargetViewId);
        mAircraftCompassWrapView = new AircraftCompassWrapView(mMavInfoView, R.id.compass_circle, R.id.compass_level, R.id.compass_ruler,
                R.id.compass_direction);

        mDroneLocation = new Location("drone location");
        mHomeLocation = new Location("home location");
        mTargetLocation = new Location("target location");

    }

    private void initAltitudeView(int altitudeTextViewId, int altitudeProgressId) {

        TextView textView = (TextView) mMavInfoView.findViewById(altitudeTextViewId);
        SeekArc seekArc = (SeekArc) mMavInfoView.findViewById(altitudeProgressId);
        mDroneAltitudeTextWrap = new ProgressWithTextWrap(textView, seekArc);
    }

    private void initGroundSpeedView(int horizontalSpeedTextViewId, int horizontalSpeedProgressId) {
        TextView textView = (TextView) mMavInfoView.findViewById(horizontalSpeedTextViewId);
        SeekArc seekArc = (SeekArc) mMavInfoView.findViewById(horizontalSpeedProgressId);
        mDroneGroundSpeedTextWrap = new ProgressWithTextWrap(textView, seekArc);
    }

    private void initClimbSpeedView(int verticalSpeedTextViewId, int verticalSpeedProgressId) {
        TextView textView = (TextView) mMavInfoView.findViewById(verticalSpeedTextViewId);
        SeekArc seekArc = (SeekArc) mMavInfoView.findViewById(verticalSpeedProgressId);
        mDroneClimbSpeedTextWrap = new ProgressWithTextWrap(textView, seekArc);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        onAltitudeUpdate(0);
        onGroundSpeedUpdate(0);
        onClimbSpeedUpdate(0);
        onLocationUpdate(Float.MAX_VALUE, Float.MAX_VALUE);
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

    final private void onClimbSpeedUpdate(float climbSpeed) {
        if (mDroneClimbSpeedTextWrap == null) {
            return;
        }
        mDroneClimbSpeedTextWrap.setValue(climbSpeed);
    }

    final private void onLocationUpdate(float droneLat, float droneLng) {
        if (mDroneLatitudeTextView == null || mDroneLongitudeTextView == null) {
            return;
        }
        String latStrFormat;
        String lonStrFormat;
        if (droneLat == Float.MAX_VALUE || droneLng == Float.MAX_VALUE) {
            latStrFormat = "--.-----, ";
            lonStrFormat = "--,-----";
            mDroneLatitudeTextView.setText(latStrFormat);
            mDroneLongitudeTextView.setText(lonStrFormat);
        } else {
            latStrFormat = ConstantValue.LOCATION_STRING_FORMAT + ", ";
            lonStrFormat = ConstantValue.LOCATION_STRING_FORMAT;
            mDroneLatitudeTextView.setText(String.format(latStrFormat, droneLat));
            mDroneLongitudeTextView.setText(String.format(lonStrFormat, droneLng));
        }

        mDroneLocation.setLatitude(droneLat);
        mDroneLocation.setLongitude(droneLng);

        updateFromHomeDistance();
        updateNextTargetDistance();
    }

    public void onHomePointUpdate(float homeLat, float homeLng) {
        mHomeLocation.setLatitude(homeLat);
        mHomeLocation.setLongitude(homeLng);
        mHomeLocation.setAccuracy(100.0f);
    }

    public void onTargetPointUpdate(float targetLat, float targetLng) {
        mTargetLocation.setLatitude(targetLat);
        mTargetLocation.setLongitude(targetLng);
    }

    private void updateFromHomeDistance() {
        if (mHomeLocation.hasAccuracy()) {
            float distance = mDroneLocation.distanceTo(mHomeLocation);
            mDroneFromHomeTextView.setText(String.format("%.1f", distance));
        }
    }

    private void updateNextTargetDistance() {

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
                if (droneStatus.getSatellites() >= 6 && droneStatus.getGPSLockType() > 2) {
                    onLocationUpdate(droneStatus.getLatitude(), droneStatus.getLongitude());
                }
                break;
            case ON_ATTITUDE_UPDATE:
                onAttitudeUpdate(droneStatus.getYaw(), droneStatus.getRoll(), droneStatus.getPitch());
                break;
            case ON_GROUND_SPEED_UPDATE:
                onGroundSpeedUpdate(droneStatus.getGroundSpeed());
                break;
            case ON_CLIMB_SPEED_UPDATE:
                onClimbSpeedUpdate(droneStatus.getClimbSpeed());
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
                mSeekArc.updateProgress((int) (Math.abs(value) * 10));
            }
        }
    }

}
