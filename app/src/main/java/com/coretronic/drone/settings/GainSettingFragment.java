package com.coretronic.drone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Parameters.PARAMETER_ID;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.ibs.drone.MavlinkLibBridge;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class GainSettingFragment extends SettingChildFragment {
    private final static String ARGUMENT_ALL_READY = "ALL_READY";
    private final static String ARGUMENT_BASIC_GAIN_ROLL = "BASIC_GAIN_ROLL";
    private final static String ARGUMENT_BASIC_GAIN_PITCH = "BASIC_GAIN_PITCH";
    private final static String ARGUMENT_BASIC_GAIN_YAW = "BASIC_GAIN_YAW";
    private final static String ARGUMENT_ATTITUDE_GAIN_ROLL = "ATTITUDE_GAIN_ROLL";
    private final static String ARGUMENT_ATTITUDE_GAIN_PITCH = "ATTITUDE_GAIN_PITCH";
    private final static String ARGUMENT_ATTITUDE_GAIN_YAW = "ATTITUDE_GAIN_YAW";

    private final static int SETTING_GAIN_UI_MIN = 1;
    private final static int SETTING_GAIN_UI_MAX = 100;
    private final static int SETTING_GAIN_UI_GAP = 1;
    private final static float SETTING_GAIN_BASIC_ROLL_MIN = 0.08f;
    private final static float SETTING_GAIN_BASIC_ROLL_MAX = 0.3f;
    private final static float SETTING_GAIN_BASIC_PITCH_MIN = 0.08f;
    private final static float SETTING_GAIN_BASIC_PITCH_MAX = 0.3f;
    private final static float SETTING_GAIN_BASIC_YAW_MIN = 0.15f;
    private final static float SETTING_GAIN_BASIC_YAW_MAX = 0.5f;
    private final static float SETTING_GAIN_ATTITUDE_ROLL_MIN = 3.0f;
    private final static float SETTING_GAIN_ATTITUDE_ROLL_MAX = 12.0f;
    private final static float SETTING_GAIN_ATTITUDE_PITCH_MIN = 3.0f;
    private final static float SETTING_GAIN_ATTITUDE_PITCH_MAX = 12.0f;
    private final static float SETTING_GAIN_ATTITUDE_YAW_MIN = 3.0f;
    private final static float SETTING_GAIN_ATTITUDE_YAW_MAX = 6.0f;

    private SeekBarTextView mBasicGainRollView;
    private SeekBarTextView mBasicGainPitchView;
    private SeekBarTextView mBasicGainYawView;
    private SeekBarTextView mAttitudeGainRollView;
    private SeekBarTextView mAttitudeGainPitchView;
    private SeekBarTextView mAttitudeGainYawView;

    private float mBasicGainRoll;
    private float mBasicGainPitch;
    private float mBasicGainYaw;
    private float mAttitudeGainRoll;
    private float mAttitudeGainPitch;
    private float mAttitudeGainYaw;

    private boolean mIsAllReady = false;

    public static GainSettingFragment newInstance(MavlinkLibBridge.DroneParameter droneParameter) {
        GainSettingFragment fragment = new GainSettingFragment();
        Bundle args = new Bundle();

        if (droneParameter == null) {
            return fragment;
        }

        args.putBoolean(ARGUMENT_ALL_READY, droneParameter.isAllReady());

        args.putFloat(ARGUMENT_BASIC_GAIN_ROLL, droneParameter.getBasicGainRoll());
        args.putFloat(ARGUMENT_BASIC_GAIN_PITCH, droneParameter.getBasicGainPitch());
        args.putFloat(ARGUMENT_BASIC_GAIN_YAW, droneParameter.getBasicGainYaw());
        args.putFloat(ARGUMENT_ATTITUDE_GAIN_ROLL, droneParameter.getAttitudeGainRoll());
        args.putFloat(ARGUMENT_ATTITUDE_GAIN_PITCH, droneParameter.getAttitudeGainPitch());
        args.putFloat(ARGUMENT_ATTITUDE_GAIN_YAW, droneParameter.getAttitudeGainYaw());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_gain, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViewValue();
    }

    private void initView(View v) {
        mBasicGainRollView = (SeekBarTextView) v.findViewById(R.id.basic_gain_roll);
        mBasicGainPitchView = (SeekBarTextView) v.findViewById(R.id.basic_gain_pitch);
        mBasicGainYawView = (SeekBarTextView) v.findViewById(R.id.basic_gain_yaw);
        mAttitudeGainRollView = (SeekBarTextView) v.findViewById(R.id.attitude_gain_roll);
        mAttitudeGainPitchView = (SeekBarTextView) v.findViewById(R.id.attitude_gain_pitch);
        mAttitudeGainYawView = (SeekBarTextView) v.findViewById(R.id.attitude_gain_yaw);

        mBasicGainRollView.setConfig(
                SETTING_GAIN_UI_MIN,
                SETTING_GAIN_UI_MAX,
                SETTING_GAIN_UI_GAP,
                SETTING_GAIN_BASIC_ROLL_MIN,
                SETTING_GAIN_BASIC_ROLL_MAX);

        mBasicGainPitchView.setConfig(
                SETTING_GAIN_UI_MIN,
                SETTING_GAIN_UI_MAX,
                SETTING_GAIN_UI_GAP,
                SETTING_GAIN_BASIC_PITCH_MIN,
                SETTING_GAIN_BASIC_PITCH_MAX);

        mBasicGainYawView.setConfig(
                SETTING_GAIN_UI_MIN,
                SETTING_GAIN_UI_MAX,
                SETTING_GAIN_UI_GAP,
                SETTING_GAIN_BASIC_YAW_MIN,
                SETTING_GAIN_BASIC_YAW_MAX);

        mAttitudeGainRollView.setConfig(
                SETTING_GAIN_UI_MIN,
                SETTING_GAIN_UI_MAX,
                SETTING_GAIN_UI_GAP,
                SETTING_GAIN_ATTITUDE_ROLL_MIN,
                SETTING_GAIN_ATTITUDE_ROLL_MAX);

        mAttitudeGainPitchView.setConfig(
                SETTING_GAIN_UI_MIN,
                SETTING_GAIN_UI_MAX,
                SETTING_GAIN_UI_GAP,
                SETTING_GAIN_ATTITUDE_PITCH_MIN,
                SETTING_GAIN_ATTITUDE_PITCH_MAX);

        mAttitudeGainYawView.setConfig(
                SETTING_GAIN_UI_MIN,
                SETTING_GAIN_UI_MAX,
                SETTING_GAIN_UI_GAP,
                SETTING_GAIN_ATTITUDE_YAW_MIN,
                SETTING_GAIN_ATTITUDE_YAW_MAX);

        mBasicGainRollView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mBasicGainRoll = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RATE_RLL_P.ordinal(), mBasicGainRoll);
            }
        });
        mBasicGainPitchView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mBasicGainPitch = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RATE_PIT_P.ordinal(), mBasicGainPitch);
            }
        });
        mBasicGainYawView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mBasicGainYaw = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RATE_YAW_P.ordinal(), mBasicGainYaw);
            }
        });
        mAttitudeGainRollView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mAttitudeGainRoll = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_STB_RLL_P.ordinal(), mAttitudeGainRoll);
            }
        });
        mAttitudeGainPitchView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mAttitudeGainPitch = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_STB_PIT_P.ordinal(), mAttitudeGainPitch);
            }
        });
        mAttitudeGainYawView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mAttitudeGainYaw = value;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_STB_YAW_P.ordinal(), mAttitudeGainYaw);
            }
        });
    }

    private void disableAllView() {
        mBasicGainRollView.setViewEnabled(false);
        mBasicGainPitchView.setViewEnabled(false);
        mBasicGainYawView.setViewEnabled(false);
        mAttitudeGainRollView.setViewEnabled(false);
        mAttitudeGainPitchView.setViewEnabled(false);
        mAttitudeGainYawView.setViewEnabled(false);
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

            mBasicGainRoll = arguments.getFloat(ARGUMENT_BASIC_GAIN_ROLL);
            mBasicGainPitch = arguments.getFloat(ARGUMENT_BASIC_GAIN_PITCH);
            mBasicGainYaw = arguments.getFloat(ARGUMENT_BASIC_GAIN_YAW);
            mAttitudeGainRoll = arguments.getFloat(ARGUMENT_ATTITUDE_GAIN_ROLL);
            mAttitudeGainPitch = arguments.getFloat(ARGUMENT_ATTITUDE_GAIN_PITCH);
            mAttitudeGainYaw = arguments.getFloat(ARGUMENT_ATTITUDE_GAIN_YAW);

            mBasicGainRollView.setValue(mBasicGainRoll);
            mBasicGainPitchView.setValue(mBasicGainPitch);
            mBasicGainYawView.setValue(mBasicGainYaw);
            mAttitudeGainRollView.setValue(mAttitudeGainRoll);
            mAttitudeGainPitchView.setValue(mAttitudeGainPitch);
            mAttitudeGainYawView.setValue(mAttitudeGainYaw);
        }
    }
}
