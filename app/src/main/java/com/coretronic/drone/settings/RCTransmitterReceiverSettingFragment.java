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
import android.widget.Button;
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
    private final static String ARGUMENT_RC_RTL_REV = "RC_RTL_REV";
    private final static String ARGUMENT_RC_GEAR_REV = "RC_GEAR_REV";
    private final static String ARGUMENT_RC_CAMERA_TRIGGER_REV = "RC_CAMERA_TRIGGER_REV";
    private final static String ARGUMENT_RC_FLIGHT_MODE_ONE = "RC_FLIGHT_MODE_ONE";
    private final static String ARGUMENT_RC_FLIGHT_MODE_THREE = "RC_FLIGHT_MODE_THREE";
    private final static String ARGUMENT_RC_FLIGHT_MODE_FIVE = "RC_FLIGHT_MODE_FIVE";
    private final static String ARGUMENT_RC_SMART_MODE_ONE = "RC_FLIGHT_MODE_ONE";
    private final static String ARGUMENT_RC_SMART_MODE_TWO = "RC_FLIGHT_MODE_TWO";

    private final static int FLIGHT_MODE2_PWM_THRESHOLD = 1367;
    private final static int FLIGHT_MODE3_PWM_THRESHOLD = 1634;

    private final static int SMART_MODE2_PWM_THRESHOLD = 1367;
    private final static int SMART_MODE3_PWM_THRESHOLD = 1634;

    private RadioGroup mReceiverTypeRadioGroup;
    private ToggleButton mReturnHomeReverseButton;
    private ToggleButton mLandingGearReverseButton;
    private ToggleButton mCameraTriggerReverseButton;
    private FlightModeModel mFlightModeModel1;
    private FlightModeModel mFlightModeModel2;
    private FlightModeModel mFlightModeModel3;
    private RcCalibrationBar mRCCalibrationBarRoll;
    private RcCalibrationBar mRCCalibrationBarPitch;
    private RcCalibrationBar mRCCalibrationBarYaw;
    private RcCalibrationBar mRCCalibrationBarThrottle;
    private TextView mFlightModePWMTextView;
    private TextView mReturnHomeOffTextView;
    private TextView mReturnHomeOnTextView;
    private TextView mSmartModePWMTextView;
    private TextView mLandingGearCloseTextView;
    private TextView mLandingGearOpenTextView;
    private TextView mCameraTriggerOffTextView;
    private TextView mCameraTriggerOnTextView;
    private TextView mSmartMode1TypeOffText;
    private Button mSmartMode2TypeButton;
    private Button mSmartMode3TypeButton;


    private Dialog mFlightModeTypePopupDialog;
    private Dialog mSmartModeTypePopupDialog;

    private int mFlightModeTypeSettingValue;
    private int mSmartModeTypeSettingValue;
    private int mRCType;
    private float mRCReturnHomePWM;
    private float mRCCameraTriggerPWM;
    private float mRCLandingGearPWM;
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
    private boolean mRCReturnHomeReverse;
    private boolean mLandingGearReverse;
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
        args.putInt(ARGUMENT_RC_RTL_REV, droneParameter.getRCRTLRev());
        args.putInt(ARGUMENT_RC_GEAR_REV, droneParameter.getRCGearRev());
        args.putInt(ARGUMENT_RC_CAMERA_TRIGGER_REV, droneParameter.getRCCameraTriggerRev());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_ONE, droneParameter.getFlightModeOne());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_THREE, droneParameter.getFlightModeThree());
        args.putInt(ARGUMENT_RC_FLIGHT_MODE_FIVE, droneParameter.getFlightModeFive());
        args.putInt(ARGUMENT_RC_SMART_MODE_ONE, droneParameter.getSmartModeOne());
        args.putInt(ARGUMENT_RC_SMART_MODE_TWO, droneParameter.getSmartModeTwo());

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

        mReturnHomeOffTextView = (TextView) v.findViewById(R.id.return_to_home_off_text_view);
        mReturnHomeOnTextView = (TextView) v.findViewById(R.id.return_to_home_on_text_view);

        mSmartModePWMTextView = (TextView) v.findViewById(R.id.smart_mode_pwm_text);

        mSmartMode1TypeOffText = (TextView) v.findViewById(R.id.smart_mode_1_off_text);
        mSmartMode2TypeButton = (Button) v.findViewById(R.id.smart_mode_2_type_button);
        mSmartMode3TypeButton = (Button) v.findViewById(R.id.smart_mode_3_type_button);

        mLandingGearCloseTextView = (TextView) v.findViewById(R.id.landing_gear_close_text_view);
        mLandingGearOpenTextView = (TextView) v.findViewById(R.id.landing_gear_open_text_view);

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

        mReturnHomeReverseButton = (ToggleButton) v.findViewById(R.id.return_to_home_reverse_button);
        mReturnHomeReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRCReturnHomeReverse = !mRCReturnHomeReverse;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC6_REV.ordinal(),
                        mRCReturnHomeReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
                setReturnHomeView(mRCReturnHomePWM);
            }
        });

        mSmartMode2TypeButton.setOnClickListener(smartModeButtonClickListener);
        mSmartMode3TypeButton.setOnClickListener(smartModeButtonClickListener);

        mLandingGearReverseButton = (ToggleButton) v.findViewById(R.id.landing_gear_reverse_button);
        mLandingGearReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLandingGearReverse = !mLandingGearReverse;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC8_REV.ordinal(),
                        mLandingGearReverse ? Parameters.RC_REVERSE_REVERSED : Parameters.RC_REVERSE_NORMAL);
                setLandingGearView(mRCLandingGearPWM);
            }
        });

        mCameraTriggerReverseButton = (ToggleButton) v.findViewById(R.id.camera_trigger_reverse_button);
        mCameraTriggerReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraTriggerReverse = !mCameraTriggerReverse;
                mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_RC9_REV.ordinal(),
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
    };

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
        mFlightModeTypeSettingValue = -1;
        mFlightModeTypePopupDialog.show();

        mFlightModeTypePopupDialog.findViewById(R.id.altitude_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.manual_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.gps_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
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
                case R.id.gps_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_LOITER;
                    break;
                case R.id.optical_flow_button:
                    mFlightModeTypeSettingValue = Parameters.FLTMODE_OF_LOITER;
                    break;
            }
            mFlightModeTypePopupDialog.dismiss();
        }
    };

    private View.OnClickListener smartModeButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int viewLocation[] = new int[2];
            v.getLocationOnScreen(viewLocation);
            showSmartModeTypePopupDialog(v.getId(), viewLocation);
        }
    };

    private void showSmartModeTypePopupDialog(final int resourceId, int[] viewLocation) {
        mSmartModeTypePopupDialog = new Dialog(getActivity());
        mSmartModeTypePopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSmartModeTypePopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSmartModeTypePopupDialog.setContentView(R.layout.popwindow_smart_mode_type);
        WindowManager.LayoutParams wmlp = mSmartModeTypePopupDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = viewLocation[0] - getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_width);
        wmlp.y = viewLocation[1];
        mSmartModeTypePopupDialog.getWindow().setAttributes(wmlp);
        mSmartModeTypeSettingValue = -1;
        mSmartModeTypePopupDialog.show();

        mSmartModeTypePopupDialog.findViewById(R.id.smart_mode_off_button).setOnClickListener(onSmartModeTypePopDialogButtonClickListener);
        mSmartModeTypePopupDialog.findViewById(R.id.smart_mode_cl_button).setOnClickListener(onSmartModeTypePopDialogButtonClickListener);
        mSmartModeTypePopupDialog.findViewById(R.id.smart_mode_hl_button).setOnClickListener(onSmartModeTypePopDialogButtonClickListener);
        mSmartModeTypePopupDialog.findViewById(R.id.smart_mode_poi_button).setOnClickListener(onSmartModeTypePopDialogButtonClickListener);

        mSmartModeTypePopupDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mSmartModeTypeSettingValue < 0) {
                    return;
                }
                setSmartModeView(resourceId, mSmartModeTypeSettingValue);
                setSmartModeParameter(resourceId, mSmartModeTypeSettingValue);
                mSmartModeTypeSettingValue = -1;
            }
        });
    }

    private View.OnClickListener onSmartModeTypePopDialogButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.smart_mode_off_button:
                    mSmartModeTypeSettingValue = Parameters.SMART_MODE.SMART_MODE_OFF.ordinal();
                    break;
                case R.id.smart_mode_cl_button:
                    mSmartModeTypeSettingValue = Parameters.SMART_MODE.SMART_MODE_CL.ordinal();
                    break;
                case R.id.smart_mode_hl_button:
                    mSmartModeTypeSettingValue = Parameters.SMART_MODE.SMART_MODE_HL.ordinal();
                    break;
                case R.id.smart_mode_poi_button:
                    mSmartModeTypeSettingValue = Parameters.SMART_MODE.SMART_MODE_POI.ordinal();
                    break;
            }
            mSmartModeTypePopupDialog.dismiss();
        }
    };

    private void disableAllView() {
        mView.findViewById(R.id.receiver_type_futaba_button).setEnabled(false);
        mView.findViewById(R.id.receiver_type_jr_button).setEnabled(false);
        mView.findViewById(R.id.receiver_type_spektrum_button).setEnabled(false);
        mView.findViewById(R.id.receiver_type_spektrum_button).setEnabled(false);
        mFlightModePWMTextView.setText("PWM-1100");
        mFlightModeModel1.setViewDisable();
        mFlightModeModel2.setViewDisable();
        mFlightModeModel3.setViewDisable();
        mSmartModePWMTextView.setText("PWM-1100");
        mSmartMode1TypeOffText.setTextColor(getResources().getColor(R.color.white_transparent_35));
        mSmartMode2TypeButton.setEnabled(false);
        mSmartMode2TypeButton.setText("");
        mSmartMode3TypeButton.setEnabled(false);
        mSmartMode3TypeButton.setText("");
        mView.findViewById(R.id.return_to_home_off_text_view).setEnabled(false);
        mView.findViewById(R.id.return_to_home_on_text_view).setEnabled(false);
        mView.findViewById(R.id.landing_gear_close_text_view).setEnabled(false);
        mView.findViewById(R.id.landing_gear_open_text_view).setEnabled(false);
        mView.findViewById(R.id.camera_trigger_off_text_view).setEnabled(false);
        mView.findViewById(R.id.camera_trigger_on_text_view).setEnabled(false);
        mView.findViewById(R.id.return_to_home_reverse_button).setEnabled(false);
        mView.findViewById(R.id.landing_gear_reverse_button).setEnabled(false);
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

            mRCReturnHomeReverse = arguments.getInt(ARGUMENT_RC_RTL_REV) < 0;
            mReturnHomeReverseButton.setChecked(mRCReturnHomeReverse);

            mLandingGearReverse = arguments.getInt(ARGUMENT_RC_GEAR_REV) < 0;
            mLandingGearReverseButton.setChecked(mLandingGearReverse);

            mCameraTriggerReverse = arguments.getInt(ARGUMENT_RC_CAMERA_TRIGGER_REV) < 0;
            mCameraTriggerReverseButton.setChecked(mCameraTriggerReverse);

            setFlightModeView(R.id.flight_mode_1, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_ONE));
            setFlightModeView(R.id.flight_mode_2, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_THREE));
            setFlightModeView(R.id.flight_mode_3, arguments.getInt(ARGUMENT_RC_FLIGHT_MODE_FIVE));

            setSmartModeView(R.id.smart_mode_2_type_button, arguments.getInt(ARGUMENT_RC_SMART_MODE_ONE));
            setSmartModeView(R.id.smart_mode_3_type_button, arguments.getInt(ARGUMENT_RC_SMART_MODE_TWO));
        }
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
            case Parameters.FLTMODE_LOITER:
                typeText = getResources().getString(R.string.gps);
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
            default:
                mFlightModeModel3.setModeTypeButtonText(typeText);
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
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE3.ordinal();
                break;
            case R.id.flight_mode_3:
            default:
                paramID = PARAMETER_ID.PARAMETER_ID_FLTMODE5.ordinal();
                break;
        }

        mDroneController.setParameters(paramID, value);
    }

    private void setSmartModeView(int smartModeViewId, int value) {
        String typeText = "";
        if (value == Parameters.SMART_MODE.SMART_MODE_OFF.ordinal()) {
            typeText = getResources().getString(R.string.smart_mode_off);
        } else if (value == Parameters.SMART_MODE.SMART_MODE_CL.ordinal()) {
            typeText = getResources().getString(R.string.smart_mode_cl);
        } else if (value == Parameters.SMART_MODE.SMART_MODE_HL.ordinal()) {
            typeText = getResources().getString(R.string.smart_mode_hl);
        } else if (value == Parameters.SMART_MODE.SMART_MODE_POI.ordinal()) {
            typeText = getResources().getString(R.string.smart_mode_poi);
        }

        if (smartModeViewId == R.id.smart_mode_2_type_button) {
            mSmartMode2TypeButton.setText(typeText);
        } else {
            mSmartMode3TypeButton.setText(typeText);
        }
    }

    private void setSmartModeParameter(int smartModeViewId, int value) {
        int paramID;
        if (smartModeViewId == R.id.smart_mode_2_type_button) {
            paramID = PARAMETER_ID.PARAMETER_ID_SMART_MODE1.ordinal();
        } else {
            paramID = PARAMETER_ID.PARAMETER_ID_SMART_MODE2.ordinal();
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
            case ON_RC_RTL_PWM_UPDATE:
                mRCReturnHomePWM = droneStatus.getRCRTLPWM();
                setReturnHomeView(mRCReturnHomePWM);
                break;
            case ON_RC_SMART_MODE_PWM_UPDATE:
                mSmartModePWMTextView.setText(String.format("PWM-%d", (int) droneStatus.getRCSmartModePWM()));
                setSmartModeOnFocus(droneStatus.getRCSmartModePWM());
                break;
            case ON_RC_GEAR_PWM_UPDATE:
                mRCLandingGearPWM = droneStatus.getRCGearPWM();
                setLandingGearView(mRCLandingGearPWM);
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

        if (flightModePWM < FLIGHT_MODE2_PWM_THRESHOLD) {
            mFlightModeModel1.setModeTypeButtonOnFocus(true);
        } else if (flightModePWM < FLIGHT_MODE3_PWM_THRESHOLD) {
            mFlightModeModel2.setModeTypeButtonOnFocus(true);
        } else {
            mFlightModeModel3.setModeTypeButtonOnFocus(true);
        }
    }

    private void setSmartModeOnFocus(float smartModePWM) {
        mSmartMode1TypeOffText.setTextColor(getResources().getColor(R.color.white));
        mSmartMode2TypeButton.setBackgroundResource(R.drawable.settings_button_bg);
        mSmartMode3TypeButton.setBackgroundResource(R.drawable.settings_button_bg);

        if (smartModePWM < SMART_MODE2_PWM_THRESHOLD) {
            mSmartMode1TypeOffText.setTextColor(getResources().getColor(R.color.primary_color_normal));
        } else if (smartModePWM < SMART_MODE3_PWM_THRESHOLD) {
            mSmartMode2TypeButton.setBackgroundResource(R.color.primary_color_normal);
        } else {
            mSmartMode3TypeButton.setBackgroundResource(R.color.primary_color_normal);
        }
    }

    private void setReturnHomeView(float gearPWM) {
        if (mRCReturnHomeReverse && gearPWM > 1490) {
            mReturnHomeOffTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mReturnHomeOnTextView.setBackgroundResource(R.color.transparent);
        } else if (mRCReturnHomeReverse && gearPWM < 1490) {
            mReturnHomeOffTextView.setBackgroundResource(R.color.transparent);
            mReturnHomeOnTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        } else if (!mRCReturnHomeReverse && gearPWM < 1490) {
            mReturnHomeOffTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mReturnHomeOnTextView.setBackgroundResource(R.color.transparent);
        } else {
            mReturnHomeOffTextView.setBackgroundResource(R.color.transparent);
            mReturnHomeOnTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        }
    }

    private void setLandingGearView(float gearPWM) {
        if (mLandingGearReverse && gearPWM > 1490) {
            mLandingGearCloseTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mLandingGearOpenTextView.setBackgroundResource(R.color.transparent);
        } else if (mLandingGearReverse && gearPWM < 1490) {
            mLandingGearCloseTextView.setBackgroundResource(R.color.transparent);
            mLandingGearOpenTextView.setBackgroundResource(R.drawable.settings_focus_frame);
        } else if (!mLandingGearReverse && gearPWM < 1490) {
            mLandingGearCloseTextView.setBackgroundResource(R.drawable.settings_focus_frame);
            mLandingGearOpenTextView.setBackgroundResource(R.color.transparent);
        } else {
            mLandingGearCloseTextView.setBackgroundResource(R.color.transparent);
            mLandingGearOpenTextView.setBackgroundResource(R.drawable.settings_focus_frame);
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
