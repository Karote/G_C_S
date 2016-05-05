package com.coretronic.drone.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Parameters.GS_HEADING_DIRECTION;
import com.coretronic.drone.model.Parameters.GS_OPTICAL_FLOW;
import com.coretronic.drone.model.Parameters.PARAMETER_ID;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.drone.util.AppConfig;
import com.coretronic.ibs.drone.MavlinkLibBridge;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class GeneralSettingFragment extends SettingChildFragment {
    private final static String ARGUMENT_ALL_READY = "ALL_READY";
    private final static String ARGUMENT_ALTITUDE_MAX = "ALTITUDE_MAX";
    private final static String ARGUMENT_RANGE_MAX = "RANGE_MAX";
    private final static String ARGUMENT_VERTICAL_SPEED_MAX = "VERTICAL_SPEED_MAX";
    private final static String ARGUMENT_HORIZONTAL_SPEED_MAX = "HORIZONTAL_SPEED_MAX";
    private final static String ARGUMENT_RTL_ALTITUDE = "RTL_ALTITUDE";
    private final static String ARGUMENT_RTL_SPEED = "RTL_SPEED";
    private final static String ARGUMENT_HEADING_DIRECTION = "HEADING_DIRECTION";
    private final static String ARGUMENT_THROTTLE_POSITION = "THROTTLE_POSITION";
    private final static String ARGUMENT_OPTICAL_FLOW = "OPTICAL_FLOW";

    private final static float ALTITUDE_MAX_UI_MIN = 10;
    private final static float ALTITUDE_MAX_UI_MAX = 1000;
    private final static float ALTITUDE_MAX_UI_GAP = 1;
    private final static float ALTITUDE_MAX_VALUE_MIN = 10;
    private final static float ALTITUDE_MAX_VALUE_MAX = 1000;
    private final static float RANGE_MAX_UI_MIN = 30;
    private final static float RANGE_MAX_UI_MAX = 10000;
    private final static float RANGE_MAX_UI_GAP = 1;
    private final static float RANGE_MAX_VALUE_MIN = 30;
    private final static float RANGE_MAX_VALUE_MAX = 10000;
    private final static float VERTICAL_SPEED_MAX_UI_MIN = 0.5f;
    private final static float VERTICAL_SPEED_MAX_UI_MAX = 5;
    private final static float VERTICAL_SPEED_MAX_UI_GAP = 0.5f;
    private final static float VERTICAL_SPEED_MAX_VALUE_MIN = 50;
    private final static float VERTICAL_SPEED_MAX_VALUE_MAX = 500;
    private final static float HORIZONTAL_SPEED_MAX_UI_MIN = 0;
    private final static float HORIZONTAL_SPEED_MAX_UI_MAX = 20;
    private final static float HORIZONTAL_SPEED_MAX_UI_GAP = 0.5f;
    private final static float HORIZONTAL_SPEED_MAX_VALUE_MIN = 0;
    private final static float HORIZONTAL_SPEED_MAX_VALUE_MAX = 2000;
    private final static float RTL_SPEED_UI_MIN = 0;
    private final static float RTL_SPEED_UI_MAX = 20;
    private final static float RTL_SPEED_UI_GAP = 0.5f;
    private final static float RTL_SPEED_VALUE_MIN = 0;
    private final static float RTL_SPEED_VALUE_MAX = 2000;
    private final static float RTL_ALTITUDE_UI_MIN = 0;
    private final static float RTL_ALTITUDE_UI_MAX = 80;
    private final static float RTL_ALTITUDE_UI_GAP = 0.5f;
    private final static float RTL_ALTITUDE_VALUE_MIN = 0;
    private final static float RTL_ALTITUDE_VALUE_MAX = 8000;
    private final static float THROTTLE_POSITION_UI_MIN = 30;
    private final static float THROTTLE_POSITION_UI_MAX = 70;
    private final static float THROTTLE_POSITION_UI_GAP = 1;
    private final static float THROTTLE_POSITION_VALUE_MIN = 300;
    private final static float THROTTLE_POSITION_VALUE_MAX = 700;
    private final static float HORIZONTAL_SPEED_DEFAULT_UI_MIN = 0;
    private final static float HORIZONTAL_SPEED_DEFAULT_UI_MAX = 20;
    private final static float HORIZONTAL_SPEED_DEFAULT_UI_GAP = 1;
    private final static float HORIZONTAL_SPEED_DEFAULT_VALUE_MIN = 0;
    private final static float HORIZONTAL_SPEED_DEFAULT_VALUE_MAX = 20;

    private SeekBarTextView mAltitudeMaxView;
    private SeekBarTextView mRangeMaxView;
    private SeekBarTextView mVerticalSpeedMaxView;
    private SeekBarTextView mHorizontalSpeedMaxView;
    private SeekBarTextView mRTLSpeedView;
    private SeekBarTextView mRTLAltitudeView;
    private SeekBarTextView mThrottlePositionView;
    private SeekBarTextView mAltitudeDefaultView;
    private SeekBarTextView mHorizontalSpeedDefaultView;
    private RadioGroup mHeadingDirectionRadioGroup;
    private RadioGroup mRTLHeadingDirectionRadioGroup;
    private RadioGroup mOpticalFlowRadioGroup;
    private RadioGroup mShowFlightRouteRadioGroup;
    private RadioButton mRTLHeadingDirectionFrontButton;
    private RadioButton mRTLHeadingDirectionRearButton;

    private SharedPreferences mSharedPreferences;

    private float mAltitudeMax;
    private float mRangeMax;
    private float mVerticalSpeedMax;
    private float mHorizontalSpeedMax;
    private float mRTLAltitude;
    private float mRTLSpeed;
    private int mHeadingDirection;
    private int mThrottlePosition;
    private int mAltitudeDefault;
    private int mHorizontalSpeedDefault;
    private int mOpticalFlow;
    private boolean mShowFlightRoute;

    private boolean mIsAllReady = false;

    private View mView;

    public static GeneralSettingFragment newInstance(MavlinkLibBridge.DroneParameter droneParameter) {
        GeneralSettingFragment fragment = new GeneralSettingFragment();
        Bundle args = new Bundle();

        if (droneParameter == null) {
            return fragment;
        }

        args.putBoolean(ARGUMENT_ALL_READY, droneParameter.isAllReady());

        args.putFloat(ARGUMENT_ALTITUDE_MAX, droneParameter.getAltitudeMax());
        args.putFloat(ARGUMENT_RANGE_MAX, droneParameter.getRangeMax());
        args.putFloat(ARGUMENT_VERTICAL_SPEED_MAX, droneParameter.getVerticalSpeedMax());
        args.putFloat(ARGUMENT_HORIZONTAL_SPEED_MAX, droneParameter.getHorizontalSpeedMax());
        args.putFloat(ARGUMENT_RTL_ALTITUDE, droneParameter.getRTLAltitude());
        args.putFloat(ARGUMENT_RTL_SPEED, droneParameter.getRTLSpeed());
        args.putInt(ARGUMENT_HEADING_DIRECTION, droneParameter.getHeadingDirection());
        args.putInt(ARGUMENT_THROTTLE_POSITION, droneParameter.getThrottlePosition());
        args.putInt(ARGUMENT_OPTICAL_FLOW, droneParameter.getOpticalFlow());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = mMainActivity.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        initView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViewValue();
    }

    private void initView(View v) {
        mAltitudeMaxView = (SeekBarTextView) v.findViewById(R.id.settings_altitude_max);
        mAltitudeMaxView.setConfig(
                ALTITUDE_MAX_UI_MIN,
                ALTITUDE_MAX_UI_MAX,
                ALTITUDE_MAX_UI_GAP,
                ALTITUDE_MAX_VALUE_MIN,
                ALTITUDE_MAX_VALUE_MAX);
        mAltitudeMaxView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mAltitudeMax = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FENCE_ALT_MAX.ordinal(), mAltitudeMax);
            }
        });

        mRangeMaxView = (SeekBarTextView) v.findViewById(R.id.settings_range_max);
        mRangeMaxView.setConfig(
                RANGE_MAX_UI_MIN,
                RANGE_MAX_UI_MAX,
                RANGE_MAX_UI_GAP,
                RANGE_MAX_VALUE_MIN,
                RANGE_MAX_VALUE_MAX);
        mRangeMaxView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mRangeMax = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FENCE_RADIUS.ordinal(), mRangeMax);
            }
        });

        mVerticalSpeedMaxView = (SeekBarTextView) v.findViewById(R.id.settings_vertical_speed_max);
        mVerticalSpeedMaxView.setConfig(
                VERTICAL_SPEED_MAX_UI_MIN,
                VERTICAL_SPEED_MAX_UI_MAX,
                VERTICAL_SPEED_MAX_UI_GAP,
                VERTICAL_SPEED_MAX_VALUE_MIN,
                VERTICAL_SPEED_MAX_VALUE_MAX);
        mVerticalSpeedMaxView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mVerticalSpeedMax = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_PILOT_VELZ_MAX.ordinal(), mVerticalSpeedMax);
            }
        });

        mHorizontalSpeedMaxView = (SeekBarTextView) v.findViewById(R.id.settings_horizontal_speed_max);
        mHorizontalSpeedMaxView.setConfig(
                HORIZONTAL_SPEED_MAX_UI_MIN,
                HORIZONTAL_SPEED_MAX_UI_MAX,
                HORIZONTAL_SPEED_MAX_UI_GAP,
                HORIZONTAL_SPEED_MAX_VALUE_MIN,
                HORIZONTAL_SPEED_MAX_VALUE_MAX);
        mHorizontalSpeedMaxView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mHorizontalSpeedMax = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_WPNAV_SPEED.ordinal(), mHorizontalSpeedMax);
            }
        });

        mRTLSpeedView = (SeekBarTextView) v.findViewById(R.id.settings_rtl_speed);
        mRTLSpeedView.setConfig(
                RTL_SPEED_UI_MIN,
                RTL_SPEED_UI_MAX,
                RTL_SPEED_UI_GAP,
                RTL_SPEED_VALUE_MIN,
                RTL_SPEED_VALUE_MAX);
        mRTLSpeedView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mRTLSpeed = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RTL_SPEED.ordinal(), mRTLSpeed);
            }
        });

        mRTLAltitudeView = (SeekBarTextView) v.findViewById(R.id.settings_rtl_altitude);
        mRTLAltitudeView.setConfig(
                RTL_ALTITUDE_UI_MIN,
                RTL_ALTITUDE_UI_MAX,
                RTL_ALTITUDE_UI_GAP,
                RTL_ALTITUDE_VALUE_MIN,
                RTL_ALTITUDE_VALUE_MAX);
        mRTLAltitudeView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mRTLAltitude = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RTL_ALT.ordinal(), mRTLAltitude);
            }
        });

        mHeadingDirectionRadioGroup = (RadioGroup) v.findViewById(R.id.settings_heading_direction_radio_group);
        mHeadingDirectionRadioGroup.setOnCheckedChangeListener(onHeadingDirectionCheckedChangeListener);
        v.findViewById(R.id.heading_direction_never_change_button).setOnClickListener(onHeadingDirectionRadioButtonClickListener);
        v.findViewById(R.id.heading_direction_next_waypoint_button).setOnClickListener(onHeadingDirectionRadioButtonClickListener);

        mRTLHeadingDirectionRadioGroup = (RadioGroup) v.findViewById(R.id.settings_rtl_heading_direction_radio_group);
        mRTLHeadingDirectionRadioGroup.setOnCheckedChangeListener(onRTLHeadingDirectionCheckedChangeListener);
        mRTLHeadingDirectionFrontButton = (RadioButton) v.findViewById(R.id.rtl_heading_direction_front_button);
        mRTLHeadingDirectionFrontButton.setOnClickListener(onRTLHeadingDirectionRadioButtonClickListener);
        mRTLHeadingDirectionRearButton = (RadioButton) v.findViewById(R.id.rtl_heading_direction_rear_button);
        mRTLHeadingDirectionRearButton.setOnClickListener(onRTLHeadingDirectionRadioButtonClickListener);

        mThrottlePositionView = (SeekBarTextView) v.findViewById(R.id.settings_throttle_position);
        mThrottlePositionView.setConfig(
                THROTTLE_POSITION_UI_MIN,
                THROTTLE_POSITION_UI_MAX,
                THROTTLE_POSITION_UI_GAP,
                THROTTLE_POSITION_VALUE_MIN,
                THROTTLE_POSITION_VALUE_MAX);
        mThrottlePositionView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mThrottlePosition = (int) value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_THR_MID.ordinal(), mThrottlePosition);
            }
        });

        mAltitudeDefaultView = (SeekBarTextView) v.findViewById(R.id.settings_altitude_default);
        mAltitudeDefaultView.setConfig(5, 200, 1, 5, 200);
        mAltitudeDefaultView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mAltitudeDefault = (int) value;
                mSharedPreferences.edit().putInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, mAltitudeDefault).apply();
            }
        });

        mHorizontalSpeedDefaultView = (SeekBarTextView) v.findViewById(R.id.settings_horizontal_speed_default);
        mHorizontalSpeedDefaultView.setConfig(
                HORIZONTAL_SPEED_DEFAULT_UI_MIN,
                HORIZONTAL_SPEED_DEFAULT_UI_MAX,
                HORIZONTAL_SPEED_DEFAULT_UI_GAP,
                HORIZONTAL_SPEED_DEFAULT_VALUE_MIN,
                HORIZONTAL_SPEED_DEFAULT_VALUE_MAX);
        mHorizontalSpeedDefaultView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mHorizontalSpeedDefault = (int) value;
                mSharedPreferences.edit().putInt(AppConfig.SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT, mHorizontalSpeedDefault).apply();
            }
        });

        mOpticalFlowRadioGroup = (RadioGroup) v.findViewById(R.id.setting_optical_flow_radio_group);
        mOpticalFlowRadioGroup.setOnCheckedChangeListener(onOpticalFlowCheckedChangeListener);
        v.findViewById(R.id.optical_flow_off_button).setOnClickListener(onOpticalFlowRadioButtonClickListener);
        v.findViewById(R.id.optical_flow_on_button).setOnClickListener(onOpticalFlowRadioButtonClickListener);

        mShowFlightRouteRadioGroup = (RadioGroup) v.findViewById(R.id.settings_show_flight_route_radio_group);
        mShowFlightRouteRadioGroup.setOnCheckedChangeListener(onShowFlightRouteCheckedChanged);

        v.findViewById(R.id.settings_magnetic_field_deviation_auto_adjust_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TBD
            }
        });
        v.findViewById(R.id.settings_flat_trim_execute_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TBD
            }
        });
        v.findViewById(R.id.settings_reset_to_default_reset_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TBD
            }
        });
        v.findViewById(R.id.settings_firmware_update_check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TBD
            }
        });
    }

    private void disableAllView() {
        mAltitudeMaxView.setViewEnabled(false);
        mRangeMaxView.setViewEnabled(false);
        mVerticalSpeedMaxView.setViewEnabled(false);
        mHorizontalSpeedMaxView.setViewEnabled(false);
        mRTLSpeedView.setViewEnabled(false);
        mRTLAltitudeView.setViewEnabled(false);
        mView.findViewById(R.id.heading_direction_never_change_button).setEnabled(false);
        mView.findViewById(R.id.heading_direction_next_waypoint_button).setEnabled(false);
        mView.findViewById(R.id.rtl_heading_direction_front_button).setEnabled(false);
        mView.findViewById(R.id.rtl_heading_direction_rear_button).setEnabled(false);
        mThrottlePositionView.setViewEnabled(false);
        mView.findViewById(R.id.optical_flow_on_button).setEnabled(false);
        mView.findViewById(R.id.optical_flow_off_button).setEnabled(false);
    }


    private void initViewValue() {
        Bundle arguments = getArguments();

        if (arguments == null) {
            disableAllView();
        } else {
            mIsAllReady = arguments.getBoolean(ARGUMENT_ALL_READY);

            if (!mIsAllReady) {
                disableAllView();
                return;
            }
            mAltitudeMax = arguments.getFloat(ARGUMENT_ALTITUDE_MAX);
            mAltitudeMaxView.setValue(mAltitudeMax);

            mRangeMax = arguments.getFloat(ARGUMENT_RANGE_MAX);
            mRangeMaxView.setValue(mRangeMax);

            mVerticalSpeedMax = arguments.getFloat(ARGUMENT_VERTICAL_SPEED_MAX);
            mVerticalSpeedMaxView.setValue(mVerticalSpeedMax);

            mHorizontalSpeedMax = arguments.getFloat(ARGUMENT_HORIZONTAL_SPEED_MAX);
            mHorizontalSpeedMaxView.setValue(mHorizontalSpeedMax);

            mRTLAltitude = arguments.getFloat(ARGUMENT_RTL_ALTITUDE);
            mRTLAltitudeView.setValue(mRTLAltitude);

            mRTLSpeed = arguments.getFloat(ARGUMENT_RTL_SPEED);
            mRTLSpeedView.setValue(mRTLSpeed);

            mHeadingDirection = arguments.getInt(ARGUMENT_HEADING_DIRECTION);
            if (mHeadingDirection == GS_HEADING_DIRECTION.GS_NEVER_CHANGE.ordinal()) {
                mHeadingDirectionRadioGroup.check(R.id.heading_direction_never_change_button);
                mRTLHeadingDirectionRadioGroup.clearCheck();
                mRTLHeadingDirectionRadioGroup.setEnabled(false);
            } else if (mHeadingDirection == GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_FRONT.ordinal()) {
                mHeadingDirectionRadioGroup.check(R.id.heading_direction_next_waypoint_button);
                mRTLHeadingDirectionRadioGroup.check(R.id.rtl_heading_direction_front_button);
            } else if (mHeadingDirection == GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_REAR.ordinal()) {
                mHeadingDirectionRadioGroup.check(R.id.heading_direction_next_waypoint_button);
                mRTLHeadingDirectionRadioGroup.check(R.id.rtl_heading_direction_rear_button);
            }

            mThrottlePosition = arguments.getInt(ARGUMENT_THROTTLE_POSITION);
            mThrottlePositionView.setValue(mThrottlePosition);

            mOpticalFlow = arguments.getInt(ARGUMENT_OPTICAL_FLOW);
            if (mOpticalFlow == GS_OPTICAL_FLOW.GS_OF_DISABLE.ordinal()) {
                mOpticalFlowRadioGroup.check(R.id.optical_flow_off_button);
            } else if (mOpticalFlow == GS_OPTICAL_FLOW.GS_OF_ENABLE.ordinal()) {
                mOpticalFlowRadioGroup.check(R.id.optical_flow_on_button);
            }
        }
        mAltitudeDefault = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, 30);
        mAltitudeDefaultView.setValue(mAltitudeDefault);

        mHorizontalSpeedDefault = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT, 4);
        mHorizontalSpeedDefaultView.setValue(mHorizontalSpeedDefault);

        mShowFlightRoute = mSharedPreferences.getBoolean(AppConfig.SHARED_PREFERENCE_SHOW_FLIGHT_ROUTE, true);
        if (mShowFlightRoute) {
            mShowFlightRouteRadioGroup.check(R.id.show_flight_route_on_button);
        } else {
            mShowFlightRouteRadioGroup.check(R.id.show_flight_route_off_button);
        }
    }

    private RadioGroup.OnCheckedChangeListener onHeadingDirectionCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.heading_direction_never_change_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEVER_CHANGE.ordinal();
                    mRTLHeadingDirectionRadioGroup.clearCheck();
                    mRTLHeadingDirectionFrontButton.setEnabled(false);
                    mRTLHeadingDirectionRearButton.setEnabled(false);
                    break;
                case R.id.heading_direction_next_waypoint_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_FRONT.ordinal();
                    mRTLHeadingDirectionFrontButton.setEnabled(true);
                    mRTLHeadingDirectionRearButton.setEnabled(true);
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onHeadingDirectionRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.heading_direction_never_change_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEVER_CHANGE.ordinal();
                    mRTLHeadingDirectionRadioGroup.clearCheck();
                    mRTLHeadingDirectionFrontButton.setEnabled(false);
                    mRTLHeadingDirectionRearButton.setEnabled(false);
                    break;
                case R.id.heading_direction_next_waypoint_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_FRONT.ordinal();
                    mRTLHeadingDirectionRadioGroup.check(R.id.rtl_heading_direction_front_button);
                    mRTLHeadingDirectionFrontButton.setEnabled(true);
                    mRTLHeadingDirectionRearButton.setEnabled(true);
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_WP_YAW_BEHAVIOR.ordinal(), mHeadingDirection);
        }
    };

    private RadioGroup.OnCheckedChangeListener onRTLHeadingDirectionCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rtl_heading_direction_front_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_FRONT.ordinal();
                    break;
                case R.id.rtl_heading_direction_rear_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_REAR.ordinal();
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onRTLHeadingDirectionRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rtl_heading_direction_front_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_FRONT.ordinal();
                    break;
                case R.id.rtl_heading_direction_rear_button:
                    mHeadingDirection = GS_HEADING_DIRECTION.GS_NEXT_WAYPOINT_REAR.ordinal();
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_WP_YAW_BEHAVIOR.ordinal(), mHeadingDirection);
        }
    };

    private RadioGroup.OnCheckedChangeListener onOpticalFlowCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.optical_flow_on_button:
                    mOpticalFlow = GS_OPTICAL_FLOW.GS_OF_ENABLE.ordinal();
                    break;
                case R.id.optical_flow_off_button:
                    mOpticalFlow = GS_OPTICAL_FLOW.GS_OF_DISABLE.ordinal();
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onOpticalFlowRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.optical_flow_on_button:
                    mOpticalFlow = GS_OPTICAL_FLOW.GS_OF_ENABLE.ordinal();
                    break;
                case R.id.optical_flow_off_button:
                    mOpticalFlow = GS_OPTICAL_FLOW.GS_OF_DISABLE.ordinal();
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FLOW_ENABLE.ordinal(), mOpticalFlow);
        }
    };

    private RadioGroup.OnCheckedChangeListener onShowFlightRouteCheckedChanged = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.show_flight_route_on_button:
                    mShowFlightRoute = true;
                    break;
                case R.id.show_flight_route_off_button:
                    mShowFlightRoute = false;
                    break;
            }
            mSharedPreferences.edit().putBoolean(AppConfig.SHARED_PREFERENCE_SHOW_FLIGHT_ROUTE, mShowFlightRoute).apply();
        }
    };
}
