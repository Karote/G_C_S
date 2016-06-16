package com.coretronic.drone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.coretronic.drone.DroneStatus;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback;
import com.coretronic.drone.model.Parameters;
import com.coretronic.drone.model.Parameters.FS_BATTERY_OPTION;
import com.coretronic.drone.model.Parameters.PARAMETER_ID;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.ibs.drone.MavlinkLibBridge;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class BatteryAndFailsafeSettingFragment extends SettingChildFragment {
    private final static String ARGUMENT_ALL_READY = "ALL_READY";
    private final static String ARGUMENT_BATTERY_CAPACITY = "BATTERY_CAPACITY";
    private final static String ARGUMENT_BATTERY_REMAINING = "BATTERY_REMAINING";
    private final static String ARGUMENT_BATTERY_OPTION = "BATTERY_OPTION";
    private final static String ARGUMENT_RC_SIGNAL_LOST = "RC_SIGNAL_LOST";
    private final static String ARGUMENT_GPS_SIGNAL_LOST = "GPS_SIGNAL_LOST";
    private final static String ARGUMENT_GCS_SIGNAL_LOST = "GCS_SIGNAL_LOST";

    private final static int BATTERY_LEVEL_0_BATTERY_REMAINING = 0;
    private final static int BATTERY_LEVEL_1_BATTERY_REMAINING = 20;
    private final static int BATTERY_LEVEL_2_BATTERY_REMAINING = 40;
    private final static int BATTERY_LEVEL_3_BATTERY_REMAINING = 60;
    private final static int BATTERY_LEVEL_4_BATTERY_REMAINING = 80;

    private RadioGroup mBatteryCapacityRadioGroup;
    private RadioGroup mBatteryOptionRadioGroup;
    private RadioGroup mRCSignalLostRadioGroup;
    private RadioGroup mGPSSignalLostRadioGroup;
    private RadioGroup mGCSSignalLostRadioGroup;
    private SeekBarTextView mBatteryRemainingView;
    private ImageView mBatteryFirstImage;
    private ImageView mBatterySecondImage;
    private ImageView mBatteryThirdImage;
    private TextView mBatteryFirstText;
    private TextView mBatterySecondText;
    private TextView mBatteryThirdText;

    private int mBatteryCapacity;
    private float mBatteryRemaining;
    private int mBatteryOption;
    private int mRCSignalLost;
    private int mGPSSignalLost;
    private int mGCSSignalLost;

    private boolean mIsAllReady = false;

    private View mView;

    public static BatteryAndFailsafeSettingFragment newInstance(MavlinkLibBridge.DroneParameter droneParameter) {
        BatteryAndFailsafeSettingFragment fragment = new BatteryAndFailsafeSettingFragment();
        Bundle args = new Bundle();

        if (droneParameter == null) {
            return fragment;
        }

        args.putBoolean(ARGUMENT_ALL_READY, droneParameter.isAllReady());

        args.putInt(ARGUMENT_BATTERY_CAPACITY, droneParameter.getBatteryCapacity());
        args.putInt(ARGUMENT_BATTERY_OPTION, droneParameter.getBatteryOption());
        args.putInt(ARGUMENT_RC_SIGNAL_LOST, droneParameter.getRCSignalLost());
        args.putInt(ARGUMENT_GPS_SIGNAL_LOST, droneParameter.getGPSSignalLost());
        args.putInt(ARGUMENT_GCS_SIGNAL_LOST, droneParameter.getGCSSignalLost());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_battery_failsafe, container, false);
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
        mBatteryCapacityRadioGroup = (RadioGroup) v.findViewById(R.id.settings_battery_capacity_radio_group);
        mBatteryCapacityRadioGroup.setOnCheckedChangeListener(onBatteryCapacityCheckedChangeListener);
        v.findViewById(R.id.battery_capacity_ten_thousand_mah_button).setOnClickListener(onBatteryCapacityRadioButtonClickListener);
        v.findViewById(R.id.battery_capacity_sixteen_thousand_mah_button).setOnClickListener(onBatteryCapacityRadioButtonClickListener);
        v.findViewById(R.id.battery_capacity_twenty_two_thousand_mah_button).setOnClickListener(onBatteryCapacityRadioButtonClickListener);
        mBatteryFirstImage = (ImageView) v.findViewById(R.id.battery_1_icon);
        mBatterySecondImage = (ImageView) v.findViewById(R.id.battery_2_icon);
        mBatteryThirdImage = (ImageView) v.findViewById(R.id.battery_3_icon);
        mBatteryFirstText = (TextView) v.findViewById(R.id.battery_1_current_text);
        mBatterySecondText = (TextView) v.findViewById(R.id.battery_2_current_text);
        mBatteryThirdText = (TextView) v.findViewById(R.id.battery_3_current_text);

        mBatteryRemainingView = (SeekBarTextView) v.findViewById(R.id.battery_remaining_seekbar);
        mBatteryRemainingView.setConfig(20, 50, 1, 20, 50);
        mBatteryRemainingView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {
            @Override
            public void onStopTrackingTouch(float value) {
                mBatteryRemaining = value;
            }
        });

        mBatteryOptionRadioGroup = (RadioGroup) v.findViewById(R.id.settings_failsafe_battery_option_radio_group);
        mBatteryOptionRadioGroup.setOnCheckedChangeListener(onBatteryOptionCheckedChangeListener);
        v.findViewById(R.id.battery_option_none_button).setOnClickListener(onBatteryOptionRadioButtonClickListener);
        v.findViewById(R.id.battery_option_rtl_button).setOnClickListener(onBatteryOptionRadioButtonClickListener);
        v.findViewById(R.id.battery_option_land_button).setOnClickListener(onBatteryOptionRadioButtonClickListener);

        mRCSignalLostRadioGroup = (RadioGroup) v.findViewById(R.id.settings_rc_signal_lost_radio_group);
        mRCSignalLostRadioGroup.setOnCheckedChangeListener(onRCSignalLostCheckedChangeListener);
        v.findViewById(R.id.rc_signal_lost_none_button).setOnClickListener(onRCSignalLostRadioButtonClickListener);
        v.findViewById(R.id.rc_signal_lost_rtl_button).setOnClickListener(onRCSignalLostRadioButtonClickListener);
        v.findViewById(R.id.rc_signal_lost_land_button).setOnClickListener(onRCSignalLostRadioButtonClickListener);

        mGPSSignalLostRadioGroup = (RadioGroup) v.findViewById(R.id.settings_gps_signal_lost_radio_group);
        mGPSSignalLostRadioGroup.setOnCheckedChangeListener(onGPSSignalLostCheckedChangeListener);
        v.findViewById(R.id.gps_signal_lost_none_button).setOnClickListener(onGPSSignalLostRadioButtonClickListener);
        v.findViewById(R.id.gps_signal_lost_land_button).setOnClickListener(onGPSSignalLostRadioButtonClickListener);

        mGCSSignalLostRadioGroup = (RadioGroup) v.findViewById(R.id.settings_gcs_signal_lost_radio_group);
        mGCSSignalLostRadioGroup.setOnCheckedChangeListener(onGCSSignalLostCheckedChangeListener);
        v.findViewById(R.id.gcs_signal_lost_none_button).setOnClickListener(onGCSSignalLostRadioButtonClickListener);
        v.findViewById(R.id.gcs_signal_lost_rtl_button).setOnClickListener(onGCSSignalLostRadioButtonClickListener);
        v.findViewById(R.id.gcs_signal_lost_land_button).setOnClickListener(onGCSSignalLostRadioButtonClickListener);
    }

    private void disableAllView() {
        mView.findViewById(R.id.battery_capacity_ten_thousand_mah_button).setEnabled(false);
        mView.findViewById(R.id.battery_capacity_sixteen_thousand_mah_button).setEnabled(false);
        mView.findViewById(R.id.battery_capacity_twenty_two_thousand_mah_button).setEnabled(false);

        mView.findViewById(R.id.battery_option_none_button).setEnabled(false);
        mView.findViewById(R.id.battery_option_rtl_button).setEnabled(false);
        mView.findViewById(R.id.battery_option_land_button).setEnabled(false);

        mView.findViewById(R.id.rc_signal_lost_none_button).setEnabled(false);
        mView.findViewById(R.id.rc_signal_lost_rtl_button).setEnabled(false);
        mView.findViewById(R.id.rc_signal_lost_land_button).setEnabled(false);

        mView.findViewById(R.id.gps_signal_lost_none_button).setEnabled(false);
        mView.findViewById(R.id.gps_signal_lost_land_button).setEnabled(false);

        mView.findViewById(R.id.gcs_signal_lost_none_button).setEnabled(false);
        mView.findViewById(R.id.gcs_signal_lost_rtl_button).setEnabled(false);
        mView.findViewById(R.id.gcs_signal_lost_land_button).setEnabled(false);

        mBatteryFirstText.setTextColor(getResources().getColor(R.color.gray));
        mBatterySecondText.setTextColor(getResources().getColor(R.color.gray));
        mBatteryThirdText.setTextColor(getResources().getColor(R.color.gray));

        mBatteryRemainingView.setViewDisable(30);
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

            mBatteryCapacity = arguments.getInt(ARGUMENT_BATTERY_CAPACITY);
            if (mBatteryCapacity == Parameters.FS_BATTERY_CAPACITY_ONE) {
                mBatteryCapacityRadioGroup.check(R.id.battery_capacity_ten_thousand_mah_button);
            } else if (mBatteryCapacity == Parameters.FS_BATTERY_CAPACITY_TWO) {
                mBatteryCapacityRadioGroup.check(R.id.battery_capacity_sixteen_thousand_mah_button);
            } else if (mBatteryCapacity == Parameters.FS_BATTERY_CAPACITY_THREE) {
                mBatteryCapacityRadioGroup.check(R.id.battery_capacity_twenty_two_thousand_mah_button);
            }

            mBatteryRemainingView.setValue(30);

            mBatteryOption = arguments.getInt(ARGUMENT_BATTERY_OPTION);
            if (mBatteryOption == FS_BATTERY_OPTION.FS_BATT_DISABLE.ordinal()) {
                mBatteryOptionRadioGroup.check(R.id.battery_option_none_button);
            } else if (mBatteryOption == FS_BATTERY_OPTION.FS_BATT_RTL.ordinal()) {
                mBatteryOptionRadioGroup.check(R.id.battery_option_rtl_button);
            } else if (mBatteryOption == FS_BATTERY_OPTION.FS_BATT_LAND.ordinal()) {
                mBatteryOptionRadioGroup.check(R.id.battery_option_land_button);
            }

            mRCSignalLost = arguments.getInt(ARGUMENT_RC_SIGNAL_LOST);
            if (mRCSignalLost == Parameters.FS_RC_SIGNAL_LOST_DISABLE) {
                mRCSignalLostRadioGroup.check(R.id.rc_signal_lost_none_button);
            } else if (mRCSignalLost == Parameters.FS_RC_SIGNAL_LOST_RTL) {
                mRCSignalLostRadioGroup.check(R.id.rc_signal_lost_rtl_button);
            } else if (mRCSignalLost == Parameters.FS_RC_SIGNAL_LOST_LAND) {
                mRCSignalLostRadioGroup.check(R.id.rc_signal_lost_land_button);
            }

            mGPSSignalLost = arguments.getInt(ARGUMENT_GPS_SIGNAL_LOST);
            if (mGPSSignalLost == Parameters.FS_GPS_SIGNAL_LOST_DISABLE) {
                mGPSSignalLostRadioGroup.check(R.id.gps_signal_lost_none_button);
            } else if (mGPSSignalLost == Parameters.FS_GPS_SIGNAL_LOST_LAND) {
                mGPSSignalLostRadioGroup.check(R.id.gps_signal_lost_land_button);
            }

            mGCSSignalLost = arguments.getInt(ARGUMENT_GCS_SIGNAL_LOST);
            if (mGCSSignalLost == Parameters.FS_GCS_SIGNAL_LOST_DISABLE) {
                mGCSSignalLostRadioGroup.check(R.id.gcs_signal_lost_none_button);
            } else if (mGCSSignalLost == Parameters.FS_GCS_SIGNAL_LOST_RTL) {
                mGCSSignalLostRadioGroup.check(R.id.gcs_signal_lost_rtl_button);
            } else if (mGCSSignalLost == Parameters.FS_GCS_SIGNAL_LOST_LAND) {
                mGCSSignalLostRadioGroup.check(R.id.gcs_signal_lost_land_button);
            }
        }
    }

    private RadioGroup.OnCheckedChangeListener onBatteryCapacityCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.battery_capacity_ten_thousand_mah_button:
                    mBatteryCapacity = Parameters.FS_BATTERY_CAPACITY_ONE;
                    break;
                case R.id.battery_capacity_sixteen_thousand_mah_button:
                    mBatteryCapacity = Parameters.FS_BATTERY_CAPACITY_TWO;
                    break;
                case R.id.battery_capacity_twenty_two_thousand_mah_button:
                    mBatteryCapacity = Parameters.FS_BATTERY_CAPACITY_THREE;
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onBatteryCapacityRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.battery_capacity_ten_thousand_mah_button:
                    mBatteryCapacity = Parameters.FS_BATTERY_CAPACITY_ONE;
                    break;
                case R.id.battery_capacity_sixteen_thousand_mah_button:
                    mBatteryCapacity = Parameters.FS_BATTERY_CAPACITY_TWO;
                    break;
                case R.id.battery_capacity_twenty_two_thousand_mah_button:
                    mBatteryCapacity = Parameters.FS_BATTERY_CAPACITY_THREE;
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_BATT_CAPACITY.ordinal(), mBatteryCapacity);
        }
    };

    private RadioGroup.OnCheckedChangeListener onBatteryOptionCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.battery_option_none_button:
                    mBatteryOption = FS_BATTERY_OPTION.FS_BATT_DISABLE.ordinal();
                    break;
                case R.id.battery_option_rtl_button:
                    mBatteryOption = FS_BATTERY_OPTION.FS_BATT_RTL.ordinal();
                    break;
                case R.id.battery_option_land_button:
                    mBatteryOption = FS_BATTERY_OPTION.FS_BATT_LAND.ordinal();
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onBatteryOptionRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.battery_option_none_button:
                    mBatteryOption = FS_BATTERY_OPTION.FS_BATT_DISABLE.ordinal();
                    break;
                case R.id.battery_option_rtl_button:
                    mBatteryOption = FS_BATTERY_OPTION.FS_BATT_RTL.ordinal();
                    break;
                case R.id.battery_option_land_button:
                    mBatteryOption = FS_BATTERY_OPTION.FS_BATT_LAND.ordinal();
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FS_BATT_ENABLE.ordinal(), mBatteryOption);
        }
    };

    private RadioGroup.OnCheckedChangeListener onRCSignalLostCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rc_signal_lost_none_button:
                    mRCSignalLost = Parameters.FS_RC_SIGNAL_LOST_DISABLE;
                    break;
                case R.id.rc_signal_lost_rtl_button:
                    mRCSignalLost = Parameters.FS_RC_SIGNAL_LOST_RTL;
                    break;
                case R.id.rc_signal_lost_land_button:
                    mRCSignalLost = Parameters.FS_RC_SIGNAL_LOST_LAND;
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onRCSignalLostRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rc_signal_lost_none_button:
                    mRCSignalLost = Parameters.FS_RC_SIGNAL_LOST_DISABLE;
                    break;
                case R.id.rc_signal_lost_rtl_button:
                    mRCSignalLost = Parameters.FS_RC_SIGNAL_LOST_RTL;
                    break;
                case R.id.rc_signal_lost_land_button:
                    mRCSignalLost = Parameters.FS_RC_SIGNAL_LOST_LAND;
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FS_THR_ENABLE.ordinal(), mRCSignalLost);
        }
    };

    private RadioGroup.OnCheckedChangeListener onGPSSignalLostCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.gps_signal_lost_none_button:
                    mGPSSignalLost = Parameters.FS_GPS_SIGNAL_LOST_DISABLE;
                    break;
                case R.id.gps_signal_lost_land_button:
                    mGPSSignalLost = Parameters.FS_GPS_SIGNAL_LOST_LAND;
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onGPSSignalLostRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.gps_signal_lost_none_button:
                    mGPSSignalLost = Parameters.FS_GPS_SIGNAL_LOST_DISABLE;
                    break;
                case R.id.gps_signal_lost_land_button:
                    mGPSSignalLost = Parameters.FS_GPS_SIGNAL_LOST_LAND;
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FS_EKF_ACTION.ordinal(), mGPSSignalLost);
        }
    };

    private RadioGroup.OnCheckedChangeListener onGCSSignalLostCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.gcs_signal_lost_none_button:
                    mGCSSignalLost = Parameters.FS_GCS_SIGNAL_LOST_DISABLE;
                    break;
                case R.id.gcs_signal_lost_rtl_button:
                    mGCSSignalLost = Parameters.FS_GCS_SIGNAL_LOST_RTL;
                    break;
                case R.id.gcs_signal_lost_land_button:
                    mGCSSignalLost = Parameters.FS_GCS_SIGNAL_LOST_LAND;
                    break;
            }
        }
    };

    private RadioButton.OnClickListener onGCSSignalLostRadioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.gcs_signal_lost_none_button:
                    mGCSSignalLost = Parameters.FS_GCS_SIGNAL_LOST_DISABLE;
                    break;
                case R.id.gcs_signal_lost_rtl_button:
                    mGCSSignalLost = Parameters.FS_GCS_SIGNAL_LOST_RTL;
                    break;
                case R.id.gcs_signal_lost_land_button:
                    mGCSSignalLost = Parameters.FS_GCS_SIGNAL_LOST_LAND;
                    break;
            }
            mDroneController.setParameters(PARAMETER_ID.PARAMETER_ID_FS_GCS_ENABLE.ordinal(), mGCSSignalLost);
        }
    };

    @Override
    public void onStatusUpdate(Callback.Event event, DroneStatus droneStatus) {
        switch (event) {
            case ON_BATTERY_REMAINING_FIRST_UPDATE:
                mBatteryFirstImage.setImageLevel(calculateBatteryLevel(droneStatus.getBatteryRemainingFirst()));
                break;
            case ON_BATTERY_REMAINING_SECOND_UPDATE:
                mBatterySecondImage.setImageLevel(calculateBatteryLevel(droneStatus.getBatteryRemainingSecond()));
                break;
            case ON_BATTERY_REMAINING_THIRD_UPDATE:
                mBatteryThirdImage.setImageLevel(calculateBatteryLevel(droneStatus.getBatteryRemainingThird()));
                break;
            case ON_BATTERY_CURRENT_FIRST_UPDATE:
                mBatteryFirstText.setText(String.valueOf(droneStatus.getBatteryCurrentFirst()) + "mAh");
                break;
            case ON_BATTERY_CURRENT_SECOND_UPDATE:
                mBatterySecondText.setText(String.valueOf(droneStatus.getBatteryCurrentSecond()) + "mAh");
                break;
            case ON_BATTERY_CURRENT_THIRD_UPDATE:
                mBatteryThirdText.setText(String.valueOf(droneStatus.getBatteryCurrentThird()) + "mAh");
                break;
        }
    }

    private int calculateBatteryLevel(int batteryRemaining) {
        if (batteryRemaining < BATTERY_LEVEL_1_BATTERY_REMAINING) {
            return 0;
        }
        if (batteryRemaining < BATTERY_LEVEL_2_BATTERY_REMAINING) {
            return 1;
        }
        if (batteryRemaining < BATTERY_LEVEL_3_BATTERY_REMAINING) {
            return 2;
        }
        if (batteryRemaining < BATTERY_LEVEL_4_BATTERY_REMAINING) {
            return 3;
        }
        return 4;
    }
}
