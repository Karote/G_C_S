package com.coretronic.drone.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.coretronic.drone.DroneStatus;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback;
import com.coretronic.drone.model.Parameters;
import com.coretronic.drone.model.Parameters.PARAMETER_ID;
import com.coretronic.drone.model.Parameters.RC_TYPE;
import com.coretronic.drone.ui.FlightModeModel;
import com.coretronic.drone.ui.RcCalibrationBar;
import com.coretronic.ibs.drone.MavlinkLibBridge.DroneParameter;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class RCTransmitterReceiverSettingFragment extends SettingChildFragment {
    private final static String ARGUMENT_ALL_READY = "ALL_READY";
    private final static String ARGUMENT_RC_TYPE = "RC_TYPE";
    private final static String ARGUMENT_RC_ROLL_MIN = "RC_ROLL_MIN";
    private final static String ARGUMENT_RC_ROLL_MAX = "RC_ROLL_MAX";
    private final static String ARGUMENT_RC_ROLL_REV = "RC_ROLL_REV";
    private final static String ARGUMENT_RC_PITCH_MIN = "RC_PITCH_MIN";
    private final static String ARGUMENT_RC_PITCH_MAX = "RC_PITCH_MAX";
    private final static String ARGUMENT_RC_PITCH_REV = "RC_PITCH_REV";
    private final static String ARGUMENT_RC_YAW_MIN = "RC_YAW_MIN";
    private final static String ARGUMENT_RC_YAW_MAX = "RC_YAW_MAX";
    private final static String ARGUMENT_RC_YAW_REV = "RC_YAW_REV";
    private final static String ARGUMENT_RC_THROTTLE_MIN = "RC_THROTTLE_MIN";
    private final static String ARGUMENT_RC_THROTTLE_MAX = "RC_THROTTLE_MAX";
    private final static String ARGUMENT_RC_THROTTLE_REV = "RC_THROTTLE_REV";
    private final static String ARGUMENT_RC_TUNE = "RC_TUNE";
    private final static String ARGUMENT_RC_GEAR_MIN = "RC_GEAR_MIN";
    private final static String ARGUMENT_RC_GEAR_MAX = "RC_GEAR_MAX";
    private final static String ARGUMENT_RC_GEAR_REV = "RC_GEAR_REV";
    private final static String ARGUMENT_RC_CAMERA_TRIGGER_REV = "RC_CAMERA_TRIGGER_REV";
    private final static String ARGUMENT_RC_FLIGHT_MODE_ONE = "RC_FLIGHT_MODE_ONE";
    private final static String ARGUMENT_RC_FLIGHT_MODE_TWO = "RC_FLIGHT_MODE_TWO";
    private final static String ARGUMENT_RC_FLIGHT_MODE_THREE = "RC_FLIGHT_MODE_THREE";
    private final static String ARGUMENT_RC_FLIGHT_MODE_FOUR = "RC_FLIGHT_MODE_FOUR";
    private final static String ARGUMENT_RC_FLIGHT_MODE_FIVE = "RC_FLIGHT_MODE_FIVE";
    private final static String ARGUMENT_RC_FLIGHT_MODE_SIX = "RC_FLIGHT_MODE_SIX";
    private final static String ARGUMENT_RC_FLIGHT_MODE_SIMPLE = "RC_FLIGHT_MODE_SIMPLE";
    private final static String ARGUMENT_RC_FLIGHT_MODE_SUPER_SIMPLE = "RC_FLIGHT_MODE_SUPER_SIMPLE";

    private final static int FLIGHT_MODE2_PWM_THRESHOLD = 1231;
    private final static int FLIGHT_MODE3_PWM_THRESHOLD = 1361;
    private final static int FLIGHT_MODE4_PWM_THRESHOLD = 1491;
    private final static int FLIGHT_MODE5_PWM_THRESHOLD = 1621;
    private final static int FLIGHT_MODE6_PWM_THRESHOLD = 1750;

    private RadioGroup mReceiverTypeRadioGroup;
    private RadioGroup mRCTuningRadioGroup;
    private ToggleButton mGearReverseButton;
    private ToggleButton mCameraTriggerReverseButton;
    private FlightModeModel mFlightModeModel1;
    private FlightModeModel mFlightModeModel2;
    private FlightModeModel mFlightModeModel3;
    private FlightModeModel mFlightModeModel4;
    private FlightModeModel mFlightModeModel5;
    private FlightModeModel mFlightModeModel6;
    private RcCalibrationBar mRCCalibrationBarRoll;
    private RcCalibrationBar mRCCalibrationBarPitch;
    private RcCalibrationBar mRCCalibrationBarYaw;
    private RcCalibrationBar mRCCalibrationBarThrottle;
    private TextView mFlightModePWMTextView;
    private TextView mGearRetractTextView;
    private TextView mGearDeployTextView;
    private TextView mCameraTriggerOffTextView;
    private TextView mCameraTriggerOnTextView;

    private Dialog mFlightModeTypePopupDialog;

    private String mSimpleBinaryStr;
    private String mSuperSimpleBinaryStr;
    private int mFlightModeSimple;
    private int mFlightModeSuperSimple;
    private int mFlightModeTypeSettingValue;
    private int mRCType;
    private float mRCTuning;
    private float mRCCameraTriggerPWM;
    private float mRCGearPWM;
    private float mRCRollPWMMin;
    private float mRCRollPWMMax;
    private boolean mRCRollReverse;
    private float mRCPitchPWMMin;
    private float mRCPitchPWMMax;
    private boolean mRCPitchReverse;
    private float mRCYawPWMMin;
    private float mRCYawPWMMax;
    private boolean mRCYawReverse;
    private float mRCThrottlePWMMin;
    private float mRCThrottlePWMMax;
    private boolean mRCThrottleReverse;
    private boolean mRCCalibrationStart = false;
    private boolean mGearReverse;
    private boolean mCameraTriggerReverse;

    private boolean mIsAllReady = false;

    private View mView;

    public static RCTransmitterReceiverSettingFragment newInstance(DroneParameter droneParameter) {
        RCTransmitterReceiverSettingFragment fragment = new RCTransmitterReceiverSettingFragment();
        Bundle args = new Bundle();

        if (droneParameter == null) {
            return fragment;
        }

        args.putBoolean(ARGUMENT_ALL_READY, droneParameter.isAllReady());

        args.putInt(ARGUMENT_RC_TYPE, droneParameter.getRCType());
        args.putFloat(ARGUMENT_RC_ROLL_MIN, droneParameter.getRCRollMin());
        args.putFloat(ARGUMENT_RC_ROLL_MAX, droneParameter.getRCRollMax());
        args.putInt(ARGUMENT_RC_ROLL_REV, droneParameter.getRCRollRev());
        args.putFloat(ARGUMENT_RC_PITCH_MIN, droneParameter.getRCPitchMin());
        args.putFloat(ARGUMENT_RC_PITCH_MAX, droneParameter.getRCPitchMax());
        args.putInt(ARGUMENT_RC_PITCH_REV, droneParameter.getRCPitchRev());
        args.putFloat(ARGUMENT_RC_YAW_MIN, droneParameter.getRCYawMin());
        args.putFloat(ARGUMENT_RC_YAW_MAX, droneParameter.getRCYawMax());
        args.putInt(ARGUMENT_RC_YAW_REV, droneParameter.getRCYawRev());
        args.putFloat(ARGUMENT_RC_THROTTLE_MIN, droneParameter.getRCThrottleMin());
        args.putFloat(ARGUMENT_RC_THROTTLE_MAX, droneParameter.getRCThrottleMax());
        args.putInt(ARGUMENT_RC_THROTTLE_REV, droneParameter.getRCThrottleRev());
        args.putFloat(ARGUMENT_RC_TUNE, droneParameter.getTune());
        args.putFloat(ARGUMENT_RC_GEAR_MIN, droneParameter.getRCGearMin());
        args.putFloat(ARGUMENT_RC_GEAR_MAX, droneParameter.getRCGearMax());
        args.putInt(ARGUMENT_RC_GEAR_REV, droneParameter.getRCGearRev());
        args.putInt(ARGUMENT_RC_CAMERA_TRIGGER_REV, droneParameter.getRCCameraTriggerRev());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_ONE, droneParameter.getFlightModeOne());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_TWO, droneParameter.getFlightModeTwo());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_THREE, droneParameter.getFlightModeThree());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_FOUR, droneParameter.getFlightModeFour());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_FIVE, droneParameter.getFlightModeFive());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_SIX, droneParameter.getFlightModeSix());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_SIMPLE, droneParameter.getFlightModeSimple());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_SUPER_SIMPLE, droneParameter.getFlightModeSuperSimple());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_rc_transmitter_receiver, container, false);
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
        mReceiverTypeRadioGroup = (RadioGroup) v.findViewById(R.id.settings_receiver_type_radio_group);
        mReceiverTypeRadioGroup.setOnCheckedChangeListener(onReceiverTypeCheckedChangeListener);
        v.findViewById(R.id.receiver_type_futaba_button).setOnClickListener(onReceiverTypeRadioButtonClickListener);
        v.findViewById(R.id.receiver_type_jr_button).setOnClickListener(onReceiverTypeRadioButtonClickListener);
        v.findViewById(R.id.receiver_type_spektrum_button).setOnClickListener(onReceiverTypeRadioButtonClickListener);

        mFlightModePWMTextView = (TextView) v.findViewById(R.id.flight_mode_pwm_text);

        mFlightModeModel1 = (FlightModeModel) v.findViewById(R.id.flight_mode_1);
        mFlightModeModel2 = (FlightModeModel) v.findViewById(R.id.flight_mode_2);
        mFlightModeModel3 = (FlightModeModel) v.findViewById(R.id.flight_mode_3);
        mFlightModeModel4 = (FlightModeModel) v.findViewById(R.id.flight_mode_4);
        mFlightModeModel5 = (FlightModeModel) v.findViewById(R.id.flight_mode_5);
        mFlightModeModel6 = (FlightModeModel) v.findViewById(R.id.flight_mode_6);

        mGearRetractTextView = (TextView) v.findViewById(R.id.gear_retract_text_view);
        mGearDeployTextView = (TextView) v.findViewById(R.id.gear_deploy_text_view);

        mCameraTriggerOffTextView = (TextView) v.findViewById(R.id.camera_trigger_off_text_view);
        mCameraTriggerOnTextView = (TextView) v.findViewById(R.id.camera_trigger_on_text_view);

        mRCCalibrationBarRoll = (RcCalibrationBar) v.findViewById(R.id.rc_cali_roll);
        mRCCalibrationBarRoll.registerReverseButtonCheckedListener(new RcCalibrationBar.onReverseButtonCheckedListener() {
            @Override
            public void onReverseButtonCheckedChanged(boolean isChecked) {
                mRCRollReverse = isChecked;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC1_REV.ordinal(),
                        mRCRollReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
            }
        });
        mRCCalibrationBarPitch = (RcCalibrationBar) v.findViewById(R.id.rc_cali_pitch);
        mRCCalibrationBarPitch.registerReverseButtonCheckedListener(new RcCalibrationBar.onReverseButtonCheckedListener() {
            @Override
            public void onReverseButtonCheckedChanged(boolean isChecked) {
                mRCPitchReverse = isChecked;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC2_REV.ordinal(),
                        mRCPitchReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
            }
        });
        mRCCalibrationBarYaw = (RcCalibrationBar) v.findViewById(R.id.rc_cali_yaw);
        mRCCalibrationBarYaw.registerReverseButtonCheckedListener(new RcCalibrationBar.onReverseButtonCheckedListener() {
            @Override
            public void onReverseButtonCheckedChanged(boolean isChecked) {
                mRCYawReverse = isChecked;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC4_REV.ordinal(),
                        mRCYawReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
            }
        });
        mRCCalibrationBarThrottle = (RcCalibrationBar) v.findViewById(R.id.rc_cali_throttle);
        mRCCalibrationBarThrottle.registerReverseButtonCheckedListener(new RcCalibrationBar.onReverseButtonCheckedListener() {
            @Override
            public void onReverseButtonCheckedChanged(boolean isChecked) {
                mRCThrottleReverse = isChecked;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC3_REV.ordinal(),
                        mRCThrottleReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
            }
        });

        mFlightModeModel1.registerFlightModeModelButtonClickListener(R.id.flight_mode_1, flightModeModelButtonClickListener);
        mFlightModeModel2.registerFlightModeModelButtonClickListener(R.id.flight_mode_2, flightModeModelButtonClickListener);
        mFlightModeModel3.registerFlightModeModelButtonClickListener(R.id.flight_mode_3, flightModeModelButtonClickListener);
        mFlightModeModel4.registerFlightModeModelButtonClickListener(R.id.flight_mode_4, flightModeModelButtonClickListener);
        mFlightModeModel5.registerFlightModeModelButtonClickListener(R.id.flight_mode_5, flightModeModelButtonClickListener);
        mFlightModeModel6.registerFlightModeModelButtonClickListener(R.id.flight_mode_6, flightModeModelButtonClickListener);

        mRCTuningRadioGroup = (RadioGroup) v.findViewById(R.id.settings_tuning_radio_group);
        mRCTuningRadioGroup.setOnCheckedChangeListener(onTuningCheckedChangeListener);
        v.findViewById(R.id.tuning_throttle_button).setOnClickListener(onTuningRadioButtonClickListener);
        v.findViewById(R.id.tuning_basic_button).setOnClickListener(onTuningRadioButtonClickListener);
        v.findViewById(R.id.tuning_none_button).setOnClickListener(onTuningRadioButtonClickListener);


        mGearReverseButton = (ToggleButton) v.findViewById(R.id.gear_reverse_button);
        mGearReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGearReverse = !mGearReverse;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC7_REV.ordinal(),
                        mGearReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
                setGearView(mRCGearPWM);
            }
        });

        mCameraTriggerReverseButton = (ToggleButton) v.findViewById(R.id.camera_trigger_reverse_button);
        mCameraTriggerReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraTriggerReverse = !mCameraTriggerReverse;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC8_REV.ordinal(),
                        mCameraTriggerReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
                setCameraTriggerView(mRCCameraTriggerPWM);
            }
        });

        ((ToggleButton) v.findViewById(R.id.rc_calibration_start_button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRCCalibrationStart = true;
                    mRCRollPWMMin = mRCPitchPWMMin = mRCYawPWMMin = mRCThrottlePWMMin = 2000;
                    mRCRollPWMMax = mRCPitchPWMMax = mRCYawPWMMax = mRCThrottlePWMMax = 0;
                    mDroneController.startCalibration();
                } else {
                    mRCCalibrationStart = false;
                    mDroneController.stopCalibration(mRCRollPWMMin, mRCRollPWMMax, mRCPitchPWMMin, mRCPitchPWMMax, mRCYawPWMMin, mRCYawPWMMax, mRCThrottlePWMMin, mRCThrottlePWMMax);
                }
            }
        });

    }

    private RadioGroup.OnCheckedChangeListener onReceiverTypeCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.receiver_type_futaba_button:
                    mRCType = RC_TYPE.RC_TYPE_FUTABA.ordinal();
                    break;
                case R.id.receiver_type_jr_button:
                    mRCType = RC_TYPE.RC_TYPE_JR.ordinal();
                    break;
                case R.id.receiver_type_spektrum_button:
                    mRCType = RC_TYPE.RC_TYPE_SPEKTRUM.ordinal();
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onReceiverTypeRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.receiver_type_futaba_button:
                    mRCType = RC_TYPE.RC_TYPE_FUTABA.ordinal();
                    break;
                case R.id.receiver_type_jr_button:
                    mRCType = RC_TYPE.RC_TYPE_JR.ordinal();
                    break;
                case R.id.receiver_type_spektrum_button:
                    mRCType = RC_TYPE.RC_TYPE_SPEKTRUM.ordinal();
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC_TYPE.ordinal(), mRCType);
        }
    };

    private FlightModeModel.FlightModeModelButtonClickListener flightModeModelButtonClickListener = new FlightModeModel.FlightModeModelButtonClickListener() {
        @Override
        public void onModeTypeButtonClick(int resourceId, int[] viewLocation) {
            showFlightModeTypePopupDialog(resourceId, viewLocation);
        }

        @Override
        public void onFlightModeLockTypeCheck(int resourceId, int type) {
            StringBuilder simpleSettingString = new StringBuilder(mSimpleBinaryStr);
            StringBuilder superSimpleSettingString = new StringBuilder(mSuperSimpleBinaryStr);

            switch (resourceId) {
                case R.id.flight_mode_1:
                    simpleSettingString.setCharAt(5, flightModeSimpleTypeToBinaryString(type).charAt(0));
                    superSimpleSettingString.setCharAt(5, flightModeSimpleTypeToBinaryString(type).charAt(1));
                    break;
                case R.id.flight_mode_2:
                    simpleSettingString.setCharAt(4, flightModeSimpleTypeToBinaryString(type).charAt(0));
                    superSimpleSettingString.setCharAt(4, flightModeSimpleTypeToBinaryString(type).charAt(1));
                    break;
                case R.id.flight_mode_3:
                    simpleSettingString.setCharAt(3, flightModeSimpleTypeToBinaryString(type).charAt(0));
                    superSimpleSettingString.setCharAt(3, flightModeSimpleTypeToBinaryString(type).charAt(1));
                    break;
                case R.id.flight_mode_4:
                    simpleSettingString.setCharAt(2, flightModeSimpleTypeToBinaryString(type).charAt(0));
                    superSimpleSettingString.setCharAt(2, flightModeSimpleTypeToBinaryString(type).charAt(1));
                    break;
                case R.id.flight_mode_5:
                    simpleSettingString.setCharAt(1, flightModeSimpleTypeToBinaryString(type).charAt(0));
                    superSimpleSettingString.setCharAt(1, flightModeSimpleTypeToBinaryString(type).charAt(1));
                    break;
                case R.id.flight_mode_6:
                    simpleSettingString.setCharAt(0, flightModeSimpleTypeToBinaryString(type).charAt(0));
                    superSimpleSettingString.setCharAt(0, flightModeSimpleTypeToBinaryString(type).charAt(1));
                    break;
            }
            mFlightModeSimple = Integer.parseInt(simpleSettingString.toString(), 2);
            mFlightModeSuperSimple = Integer.parseInt(superSimpleSettingString.toString(), 2);
            mDroneController.setSimpleAndSuperSimpleMode(mFlightModeSimple, mFlightModeSuperSimple);
        }
    };

    private String flightModeSimpleTypeToBinaryString(int settingType) {
        String binaryString = "00";
        switch (settingType) {
            case FlightModeModel.FLIGHT_MODE_LOCK_TYPE_NONE:
                binaryString = "00";
                break;
            case FlightModeModel.FLIGHT_MODE_LOCK_TYPE_HEADING:
                binaryString = "10";
                break;
            case FlightModeModel.FLIGHT_MODE_LOCK_TYPE_HOME:
                binaryString = "01";
                break;
        }
        return binaryString;
    }

    private void showFlightModeTypePopupDialog(final int resourceId, int[] viewLocation) {
        mFlightModeTypePopupDialog = new Dialog(getActivity());
        mFlightModeTypePopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mFlightModeTypePopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFlightModeTypePopupDialog.setContentView(R.layout.popwindow_flight_mode_type);
        WindowManager.LayoutParams wmlp = mFlightModeTypePopupDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = viewLocation[0] - getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_width);
        wmlp.y = viewLocation[1];
        mFlightModeTypePopupDialog.getWindow().setAttributes(wmlp);
        mFlightModeTypePopupDialog.show();

        mFlightModeTypePopupDialog.findViewById(R.id.altitude_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.manual_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.auto_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.gps_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.rtl_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.land_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.optical_flow_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);

        mFlightModeTypePopupDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mFlightModeTypeSettingValue < 0) {
                    return;
                }
                setFlightModeView(resourceId, mFlightModeTypeSettingValue);
                setFlightModeParameter(resourceId, mFlightModeTypeSettingValue);
                mFlightModeTypeSettingValue = -1;
            }
        });
    }

    private View.OnClickListener onFlightModeTypePopDialogButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.altitude_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_STABILIZE;
                    break;
                case R.id.manual_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_ACRO;
                    break;
                case R.id.auto_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_AUTO;
                    break;
                case R.id.gps_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_LOITER;
                    break;
                case R.id.rtl_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_RTL;
                    break;
                case R.id.land_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_LAND;
                    break;
                case R.id.optical_flow_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_OF_LOITER;
                    break;
            }
            mFlightModeTypePopupDialog.dismiss();
        }
    };

    private RadioGroup.OnCheckedChangeListener onTuningCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.tuning_throttle_button:
                    mRCTuning = Parameters.TUNE_THROTTLE_POSITION;
                    break;
                case R.id.tuning_basic_button:
                    mRCTuning = Parameters.TUNE_PGAIN;
                    break;
                case R.id.tuning_none_button:
                    mRCTuning = Parameters.TUNE_NONE;
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onTuningRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tuning_throttle_button:
                    mRCTuning = Parameters.TUNE_THROTTLE_POSITION;
                    break;
                case R.id.tuning_basic_button:
                    mRCTuning = Parameters.TUNE_PGAIN;
                    break;
                case R.id.tuning_none_button:
                    mRCTuning = Parameters.TUNE_NONE;
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_TUNE.ordinal(), mRCTuning);
        }
    };

    private void disableAllView() {
        mView.findViewById(R.id.receiver_type_futaba_button).setEnabled(false);
        mView.findViewById(R.id.receiver_type_jr_button).setEnabled(false);
        mView.findViewById(R.id.receiver_type_spektrum_button).setEnabled(false);
        mView.findViewById(R.id.receiver_type_spektrum_button).setEnabled(false);
        mFlightModePWMTextView.setText("PWM-1100");
        mFlightModeModel1.setViewEnable(false);
        mFlightModeModel2.setViewEnable(false);
        mFlightModeModel3.setViewEnable(false);
        mFlightModeModel4.setViewEnable(false);
        mFlightModeModel5.setViewEnable(false);
        mFlightModeModel6.setViewEnable(false);
        mView.findViewById(R.id.gear_retract_text_view).setEnabled(false);
        mView.findViewById(R.id.gear_deploy_text_view).setEnabled(false);
        mView.findViewById(R.id.camera_trigger_off_text_view).setEnabled(false);
        mView.findViewById(R.id.camera_trigger_on_text_view).setEnabled(false);
        mView.findViewById(R.id.tuning_throttle_button).setEnabled(false);
        mView.findViewById(R.id.tuning_basic_button).setEnabled(false);
        mView.findViewById(R.id.tuning_none_button).setEnabled(false);
        mView.findViewById(R.id.gear_reverse_button).setEnabled(false);
        mView.findViewById(R.id.camera_trigger_reverse_button).setEnabled(false);
        mView.findViewById(R.id.rc_calibration_start_button).setEnabled(false);
        mRCCalibrationBarRoll.setViewDisable();
        mRCCalibrationBarPitch.setViewDisable();
        mRCCalibrationBarYaw.setViewDisable();
        mRCCalibrationBarThrottle.setViewDisable();
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

            mRCType = arguments.getInt(ARGUMENT_RC_TYPE);
            if (mRCType == RC_TYPE.RC_TYPE_FUTABA.ordinal()) {
                mReceiverTypeRadioGroup.check(R.id.receiver_type_futaba_button);
            } else if (mRCType == RC_TYPE.RC_TYPE_JR.ordinal()) {
                mReceiverTypeRadioGroup.check(R.id.receiver_type_jr_button);
            } else if (mRCType == RC_TYPE.RC_TYPE_SPEKTRUM.ordinal()) {
                mReceiverTypeRadioGroup.check(R.id.receiver_type_spektrum_button);
            }

            mRCRollPWMMin = arguments.getFloat(ARGUMENT_RC_ROLL_MIN);
            mRCRollPWMMax = arguments.getFloat(ARGUMENT_RC_ROLL_MAX);
            mRCRollReverse = arguments.getInt(ARGUMENT_RC_ROLL_REV) == Parameters.RC_REVERSE_REVERSED;
            mRCCalibrationBarRoll.setCalibrationConfig(mRCRollPWMMin, mRCRollPWMMax, mRCRollReverse);

            mRCPitchPWMMin = arguments.getFloat(ARGUMENT_RC_PITCH_MIN);
            mRCPitchPWMMax = arguments.getFloat(ARGUMENT_RC_PITCH_MAX);
            mRCPitchReverse = arguments.getInt(ARGUMENT_RC_PITCH_REV) == Parameters.RC_REVERSE_REVERSED;
            mRCCalibrationBarPitch.setCalibrationConfig(mRCPitchPWMMin, mRCPitchPWMMax, mRCPitchReverse);

            mRCYawPWMMin = arguments.getFloat(ARGUMENT_RC_YAW_MIN);
            mRCYawPWMMax = arguments.getFloat(ARGUMENT_RC_YAW_MAX);
            mRCYawReverse = arguments.getInt(ARGUMENT_RC_YAW_REV) == Parameters.RC_REVERSE_REVERSED;
            mRCCalibrationBarYaw.setCalibrationConfig(mRCYawPWMMin, mRCYawPWMMax, mRCYawReverse);

            mRCThrottlePWMMin = arguments.getFloat(ARGUMENT_RC_THROTTLE_MIN);
            mRCThrottlePWMMax = arguments.getFloat(ARGUMENT_RC_THROTTLE_MAX);
            mRCThrottleReverse = arguments.getInt(ARGUMENT_RC_THROTTLE_REV) == Parameters.RC_REVERSE_REVERSED;
            mRCCalibrationBarThrottle.setCalibrationConfig(mRCThrottlePWMMin, mRCThrottlePWMMax, mRCThrottleReverse);

            mRCTuning = arguments.getFloat(ARGUMENT_RC_TUNE);
            if (mRCTuning == Parameters.TUNE_THROTTLE_POSITION) {
                mRCTuningRadioGroup.check(R.id.tuning_throttle_button);
            } else if (mRCTuning == Parameters.TUNE_PGAIN) {
                mRCTuningRadioGroup.check(R.id.tuning_basic_button);
            } else if (mRCTuning == Parameters.TUNE_NONE) {
                mRCTuningRadioGroup.check(R.id.tuning_none_button);
            }

            arguments.getFloat(ARGUMENT_RC_GEAR_MIN);
            arguments.getFloat(ARGUMENT_RC_GEAR_MAX);


            mGearReverse = arguments.getInt(ARGUMENT_RC_GEAR_REV) < 0;
            mGearReverseButton.setChecked(mGearReverse);

            mCameraTriggerReverse = arguments.getInt(ARGUMENT_RC_CAMERA_TRIGGER_REV) < 0;
            mCameraTriggerReverseButton.setChecked(mCameraTriggerReverse);

            setFlightModeView(R.id.flight_mode_1, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_ONE));
            setFlightModeView(R.id.flight_mode_2, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_TWO));
            setFlightModeView(R.id.flight_mode_3, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_THREE));
            setFlightModeView(R.id.flight_mode_4, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_FOUR));
            setFlightModeView(R.id.flight_mode_5, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_FIVE));
            setFlightModeView(R.id.flight_mode_6, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_SIX));

            mFlightModeSimple = arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_SIMPLE);
            mFlightModeSuperSimple = arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_SUPER_SIMPLE);
            setFlightModeSimplesToViews(mFlightModeSimple, mFlightModeSuperSimple);
        }
    }

    private void setFlightModeSimplesToViews(int flightModeSimple, int flightModeSuperSimple) {
        mSimpleBinaryStr = String.format("%6s", Integer.toBinaryString(flightModeSimple)).replace(' ', '0');
        mSuperSimpleBinaryStr = String.format("%6s", Integer.toBinaryString(flightModeSuperSimple)).replace(' ', '0');

        for (int i = 0, j = 6; i < 6; i++, j--) {
            if (mSimpleBinaryStr.charAt(i) == '0' && mSuperSimpleBinaryStr.charAt(i) == '0') {
                getFlightModeViewByIndex(j).setCheckStatus(FlightModeModel.FLIGHT_MODE_LOCK_TYPE_NONE);
            } else if (mSimpleBinaryStr.charAt(i) == '1' && mSuperSimpleBinaryStr.charAt(i) == '0') {
                getFlightModeViewByIndex(j).setCheckStatus(FlightModeModel.FLIGHT_MODE_LOCK_TYPE_HEADING);
            } else if (mSimpleBinaryStr.charAt(i) == '0' && mSuperSimpleBinaryStr.charAt(i) == '1') {
                getFlightModeViewByIndex(j).setCheckStatus(FlightModeModel.FLIGHT_MODE_LOCK_TYPE_HOME);
            }
        }
    }

    private FlightModeModel getFlightModeViewByIndex(int i) {
        switch (i) {
            case 1:
                return mFlightModeModel1;
            case 2:
                return mFlightModeModel2;
            case 3:
                return mFlightModeModel3;
            case 4:
                return mFlightModeModel4;
            case 5:
                return mFlightModeModel5;
        }
        return mFlightModeModel6;
    }

    private void setFlightModeView(int flightModeViewId, int value) {
        String typeText = "";
        switch (value) {
            case Parameters.FLTMODE_STABILIZE:
                typeText = getResources().getString(R.string.flight_altitude);
                break;
            case Parameters.FLTMODE_ACRO:
                typeText = getResources().getString(R.string.manual);
                break;
            case Parameters.FLTMODE_AUTO:
                typeText = getResources().getString(R.string.auto);
                break;
            case Parameters.FLTMODE_LOITER:
                typeText = getResources().getString(R.string.gps);
                break;
            case Parameters.FLTMODE_RTL:
                typeText = getResources().getString(R.string.rtl);
                break;
            case Parameters.FLTMODE_LAND:
                typeText = getResources().getString(R.string.land);
                break;
            case Parameters.FLTMODE_OF_LOITER:
                typeText = getResources().getString(R.string.optical_flow);
                break;
        }

        switch (flightModeViewId) {
            case R.id.flight_mode_1:
                mFlightModeModel1.setModeTypeButtonText(typeText);
                break;
            case R.id.flight_mode_2:
                mFlightModeModel2.setModeTypeButtonText(typeText);
                break;
            case R.id.flight_mode_3:
                mFlightModeModel3.setModeTypeButtonText(typeText);
                break;
            case R.id.flight_mode_4:
                mFlightModeModel4.setModeTypeButtonText(typeText);
                break;
            case R.id.flight_mode_5:
                mFlightModeModel5.setModeTypeButtonText(typeText);
                break;
            case R.id.flight_mode_6:
            default:
                mFlightModeModel6.setModeTypeButtonText(typeText);
                break;
        }
    }

    private void setFlightModeParameter(int flightModeViewId, int value) {

        int paramID;
        switch (flightModeViewId) {
            case R.id.flight_mode_1:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE1.ordinal();
                break;
            case R.id.flight_mode_2:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE2.ordinal();
                break;
            case R.id.flight_mode_3:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE3.ordinal();
                break;
            case R.id.flight_mode_4:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE4.ordinal();
                break;
            case R.id.flight_mode_5:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE5.ordinal();
                break;
            case R.id.flight_mode_6:
            default:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE6.ordinal();
                break;
        }

        mDroneController.setParameters(paramID, value);
    }

    @Override
    public void onStatusUpdate(Callback.Event event, DroneStatus droneStatus) {
        super.onStatusUpdate(event, droneStatus);
        switch (event) {
            case ON_RC_FLIGHT_MODE_PWM_UPDATE:
                mFlightModePWMTextView.setText(String.format("PWM-%d", (int) droneStatus.getRCFlightModePWM()));
                setFlightModeOnFocus(droneStatus.getRCFlightModePWM());
                break;
            case ON_RC_TUNING_PWM_UPDATE:
                if (droneStatus.getRCTuningPWM() == Parameters.TUNE_THROTTLE_POSITION) {
                    mRCTuningRadioGroup.check(R.id.tuning_throttle_button);
                } else if (droneStatus.getRCTuningPWM() == Parameters.TUNE_PGAIN) {
                    mRCTuningRadioGroup.check(R.id.tuning_basic_button);
                } else if (droneStatus.getRCTuningPWM() == Parameters.TUNE_NONE) {
                    mRCTuningRadioGroup.check(R.id.tuning_none_button);
                }
                break;
            case ON_RC_GEAR_PWM_UPDATE:
                mRCGearPWM = droneStatus.getRCGearPWM();
                setGearView(mRCGearPWM);
                break;
            case ON_RC_CAMERA_TRIGGER_PWM_UPDATE:
                mRCCameraTriggerPWM = droneStatus.getRCCameraTriggerPWM();
                setCameraTriggerView(mRCCameraTriggerPWM);
                break;
            case ON_RC_ROLL_PWM_UPDATE:
                mRCCalibrationBarRoll.setPWMValue(droneStatus.getRCRollPWM());
                if (mRCCalibrationStart) {
                    if (droneStatus.getRCRollPWM() < mRCRollPWMMin) {
                        mRCRollPWMMin = droneStatus.getRCRollPWM();
                    }
                    if (droneStatus.getRCRollPWM() > mRCRollPWMMax) {
                        mRCRollPWMMax = droneStatus.getRCRollPWM();
                    }
                }
                break;
            case ON_RC_PITCH_PWM_UPDATE:
                mRCCalibrationBarPitch.setPWMValue(droneStatus.getRCPitchPWM());
                if (mRCCalibrationStart) {
                    if (droneStatus.getRCPitchPWM() < mRCPitchPWMMin) {
                        mRCPitchPWMMin = droneStatus.getRCPitchPWM();
                    }
                    if (droneStatus.getRCPitchPWM() > mRCPitchPWMMax) {
                        mRCPitchPWMMax = droneStatus.getRCPitchPWM();
                    }
                }
                break;
            case ON_RC_YAW_PWM_UPDATE:
                mRCCalibrationBarYaw.setPWMValue(droneStatus.getRCYawPWM());
                if (mRCCalibrationStart) {
                    if (droneStatus.getRCYawPWM() < mRCYawPWMMin) {
                        mRCYawPWMMin = droneStatus.getRCYawPWM();
                    }
                    if (droneStatus.getRCYawPWM() > mRCYawPWMMax) {
                        mRCYawPWMMax = droneStatus.getRCYawPWM();
                    }
                }
                break;
            case ON_RC_THROTTLE_PWM_UPDATE:
                mRCCalibrationBarThrottle.setPWMValue(droneStatus.getRCThrottlePWM());
                if (mRCCalibrationStart) {
                    if (droneStatus.getRCThrottlePWM() < mRCThrottlePWMMin) {
                        mRCThrottlePWMMin = droneStatus.getRCThrottlePWM();
                    }
                    if (droneStatus.getRCThrottlePWM() > mRCThrottlePWMMax) {
                        mRCThrottlePWMMax = droneStatus.getRCThrottlePWM();
                    }
                }
                break;
        }
    }

    private void setFlightModeOnFocus(float flightModePWM) {
        mFlightModeModel1.setModeTypeButtonOnFocus(false);
        mFlightModeModel2.setModeTypeButtonOnFocus(false);
        mFlightModeModel3.setModeTypeButtonOnFocus(false);
        mFlightModeModel4.setModeTypeButtonOnFocus(false);
        mFlightModeModel5.setModeTypeButtonOnFocus(false);
        mFlightModeModel6.setModeTypeButtonOnFocus(false);

        if (flightModePWM < FLIGHT_MODE2_PWM_THRESHOLD) {
            mFlightModeModel1.setModeTypeButtonOnFocus(true);
        } else if (flightModePWM < FLIGHT_MODE3_PWM_THRESHOLD) {
            mFlightModeModel2.setModeTypeButtonOnFocus(true);
        } else if (flightModePWM < FLIGHT_MODE4_PWM_THRESHOLD) {
            mFlightModeModel3.setModeTypeButtonOnFocus(true);
        } else if (flightModePWM < FLIGHT_MODE5_PWM_THRESHOLD) {
            mFlightModeModel4.setModeTypeButtonOnFocus(true);
        } else if (flightModePWM < FLIGHT_MODE6_PWM_THRESHOLD) {
            mFlightModeModel5.setModeTypeButtonOnFocus(true);
        } else {
            mFlightModeModel6.setModeTypeButtonOnFocus(true);
        }
    }

    private void setGearView(float gearPWM) {
        if (mGearReverse && gearPWM > 1490) {
            mGearRetractTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mGearDeployTextView.setBackgroundResource(R.color.transparent);
        } else if (mGearReverse && gearPWM < 1490) {
            mGearRetractTextView.setBackgroundResource(R.color.transparent);
            mGearDeployTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        } else if (!mGearReverse && gearPWM < 1490) {
            mGearRetractTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mGearDeployTextView.setBackgroundResource(R.color.transparent);
        } else {
            mGearRetractTextView.setBackgroundResource(R.color.transparent);
            mGearDeployTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        }
    }

    private void setCameraTriggerView(float cameraTriggerPWM) {
        if (mCameraTriggerReverse && cameraTriggerPWM > 1490) {
            mCameraTriggerOffTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mCameraTriggerOnTextView.setBackgroundResource(R.color.transparent);
        } else if (mCameraTriggerReverse && cameraTriggerPWM < 1490) {
            mCameraTriggerOffTextView.setBackgroundResource(R.color.transparent);
            mCameraTriggerOnTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        } else if (!mCameraTriggerReverse && cameraTriggerPWM < 1490) {
            mCameraTriggerOffTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mCameraTriggerOnTextView.setBackgroundResource(R.color.transparent);
        } else {
            mCameraTriggerOffTextView.setBackgroundResource(R.color.transparent);
            mCameraTriggerOnTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        }
    }
}
