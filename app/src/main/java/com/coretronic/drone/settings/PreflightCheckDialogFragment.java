package com.coretronic.drone.settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneStatus;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.MainFragment;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback;
import com.coretronic.drone.model.Parameters;

/**
 * Created by karot.chuang on 2016/2/24.
 */
public class PreflightCheckDialogFragment extends DialogFragment implements DroneStatus.StatusChangedListener {
    private final static int BATTERY_LEVEL_0_BATTERY_REMAINING = 0;
    private final static int BATTERY_LEVEL_1_BATTERY_REMAINING = 20;
    private final static int BATTERY_LEVEL_2_BATTERY_REMAINING = 40;
    private final static int BATTERY_LEVEL_3_BATTERY_REMAINING = 60;
    private final static int BATTERY_LEVEL_4_BATTERY_REMAINING = 80;

    private final static int GPS_LEVEL_0_SATELLITE_COUNT = 0;
    private final static int GPS_LEVEL_1_SATELLITE_COUNT = 4;
    private final static int GPS_LEVEL_2_SATELLITE_COUNT = 6;
    private final static int GPS_LEVEL_3_SATELLITE_COUNT = 8;
    private final static int GPS_LEVEL_4_SATELLITE_COUNT = 10;
    private final static int GPS_LEVEL_5_SATELLITE_COUNT = 12;

    private final static int GPS_UPDATE_GAP_NO_SIGNAL = 5 * 1000;
    private final static int GPS_UPDATE_PERIOD = 1 * 1000;

    private ImageView mBatteryFirstImage;
    private ImageView mBatterySecondImage;
    private ImageView mBatteryThirdImage;
    private ImageView mBatteryCapacityStatusImage;
    private ImageView mRemoteControllerStatusImage;
    private ImageView mCompassStatusImage;
    private ImageView mAccelerometersStatusImage;
    private ImageView mGPSStatusImage;
    private ImageView mBarometerStatusImage;
    private ImageView mSonarStatusImage;


    private TextView mBatteryFirstText;
    private TextView mBatterySecondText;
    private TextView mBatteryThirdText;
    private TextView mBatteryCapacityStatusText;
    private TextView mRemoteControllerStatusText;
    private TextView mCompassStatusText;
    private TextView mAccelerometersStatusText;
    private TextView mGPSStatusText;
    private TextView mBarometerStatusText;
    private TextView mSonarStatusText;

    private MainActivity mMainActivity;
    private DroneController mDroneController;

    private int mGpsCurrentLevel;
    private Handler mGpsCheckHandler;
    private Runnable mGpsCheckRunnable;
    private long mGpsCheckTimestamp;

    private onDialgoFragmentDismissListener mOnDialgoFragmentDismissListener;

    private View mMotorTestButton1;
    private View mMotorTestButton2;
    private View mMotorTestButton3;
    private View mMotorTestButton4;
    private View mMotorTestButton5;
    private View mMotorTestButton6;
    private View mMotorTestButton7;
    private View mMotorTestButton8;
    private View mMotorTestButton9;
    private View mMotorTestButton10;
    private View mMotorTestButton11;
    private View mMotorTestButton12;
    private int mRotorType;

    public interface onDialgoFragmentDismissListener {
        void onDialogFragmentDismiss();
    }

    public void registerDialogFragmentDismissListener(onDialgoFragmentDismissListener listener) {
        this.mOnDialgoFragmentDismissListener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
        mDroneController = mMainActivity.getDroneController();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity.registerDroneStatusChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGpsCheckRunnable != null) {
            mGpsCheckHandler.removeCallbacks(mGpsCheckRunnable);
            mGpsCheckRunnable = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogfragment_preflight_check, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(false);

        mGpsCheckHandler = new Handler();
        mGpsCurrentLevel = 0;
        mGpsCheckTimestamp = System.currentTimeMillis();

        initView(view);
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mMainActivity.unregisterDroneStatusChangedListener(this);
        mOnDialgoFragmentDismissListener.onDialogFragmentDismiss();
    }

    private void initView(View v) {
        v.findViewById(R.id.preflight_check_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mMotorTestButton1 = v.findViewById(R.id.motor_test_button_1);
        mMotorTestButton2 = v.findViewById(R.id.motor_test_button_2);
        mMotorTestButton3 = v.findViewById(R.id.motor_test_button_3);
        mMotorTestButton4 = v.findViewById(R.id.motor_test_button_4);
        mMotorTestButton5 = v.findViewById(R.id.motor_test_button_5);
        mMotorTestButton6 = v.findViewById(R.id.motor_test_button_6);
        mMotorTestButton7 = v.findViewById(R.id.motor_test_button_7);
        mMotorTestButton8 = v.findViewById(R.id.motor_test_button_8);
        mMotorTestButton9 = v.findViewById(R.id.motor_test_button_9);
        mMotorTestButton10 = v.findViewById(R.id.motor_test_button_10);
        mMotorTestButton11 = v.findViewById(R.id.motor_test_button_11);
        mMotorTestButton12 = v.findViewById(R.id.motor_test_button_12);

        setAllMotorTestButtonEnable(false);

        mMotorTestButton1.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton2.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton3.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton4.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton5.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton6.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton7.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton8.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton9.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton10.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton11.setOnClickListener(onMotorTestButtonClickListener);
        mMotorTestButton12.setOnClickListener(onMotorTestButtonClickListener);

        mBatteryFirstImage = (ImageView) v.findViewById(R.id.battery_check_icon_1);
        mBatterySecondImage = (ImageView) v.findViewById(R.id.battery_check_icon_2);
        mBatteryThirdImage = (ImageView) v.findViewById(R.id.battery_check_icon_3);

        mBatteryFirstText = (TextView) v.findViewById(R.id.battery1_check_current_text);
        mBatterySecondText = (TextView) v.findViewById(R.id.battery2_check_current_text);
        mBatteryThirdText = (TextView) v.findViewById(R.id.battery3_check_current_text);

        mBatteryCapacityStatusImage = (ImageView) v.findViewById(R.id.battery_capacity_check_icon);
        mBatteryCapacityStatusText = (TextView) v.findViewById(R.id.battery_capacity_check_text);

        mRemoteControllerStatusImage = (ImageView) v.findViewById(R.id.rc_check_icon);
        mRemoteControllerStatusText = (TextView) v.findViewById(R.id.rc_check_text);

        mCompassStatusImage = (ImageView) v.findViewById(R.id.compass_check_icon);
        mCompassStatusText = (TextView) v.findViewById(R.id.compass_check_text);

        mAccelerometersStatusImage = (ImageView) v.findViewById(R.id.accelerometers_check_icon);
        mAccelerometersStatusText = (TextView) v.findViewById(R.id.accelerometers_check_text);

        mGPSStatusImage = (ImageView) v.findViewById(R.id.gps_check_icon);
        mGPSStatusText = (TextView) v.findViewById(R.id.gps_check_text);

        mBarometerStatusImage = (ImageView) v.findViewById(R.id.barometer_check_icon);
        mBarometerStatusText = (TextView) v.findViewById(R.id.barometer_check_text);

        mSonarStatusImage = (ImageView) v.findViewById(R.id.sonar_check_icon);
        mSonarStatusText = (TextView) v.findViewById(R.id.sonar_check_text);
    }

    private void setAllMotorTestButtonEnable(boolean enable) {
        mMotorTestButton1.setEnabled(enable);
        mMotorTestButton2.setEnabled(enable);
        mMotorTestButton3.setEnabled(enable);
        mMotorTestButton4.setEnabled(enable);
        mMotorTestButton5.setEnabled(enable);
        mMotorTestButton6.setEnabled(enable);
        mMotorTestButton7.setEnabled(enable);
        mMotorTestButton8.setEnabled(enable);
        mMotorTestButton9.setEnabled(enable);
        mMotorTestButton10.setEnabled(enable);
        mMotorTestButton11.setEnabled(enable);
        mMotorTestButton12.setEnabled(enable);
    }

    private void initMotorTestButton(int aircraftType) {
        int motorNum;
        switch (aircraftType) {
            case Parameters.ROTOR_TYPE_QUADROTOR_I:
            case Parameters.ROTOR_TYPE_QUADROTOR:
                motorNum = 4;
                break;
            case Parameters.ROTOR_TYPE_HEXAROTOR_I:
            case Parameters.ROTOR_TYPE_HEXAROTOR:
                motorNum = 6;
                break;
            case Parameters.ROTOR_TYPE_OCTOROTOR_I:
            case Parameters.ROTOR_TYPE_OCTOROTOR:
            case Parameters.ROTOR_TYPE_DUO_QUADROTOR:
                motorNum = 8;
                break;
            case Parameters.ROTOR_TYPE_DUO_HEXAROTOR_I:
            case Parameters.ROTOR_TYPE_DUO_HEXAROTOR:
                motorNum = 12;
                break;
            default:
                motorNum = 0;
                break;
        }

        if (motorNum > 0) {
            mMotorTestButton1.setEnabled(true);
            mMotorTestButton2.setEnabled(true);
            mMotorTestButton3.setEnabled(true);
            mMotorTestButton4.setEnabled(true);
            mMotorTestButton1.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
            mMotorTestButton2.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
            mMotorTestButton3.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
            mMotorTestButton4.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
            if (motorNum > 4) {
                mMotorTestButton5.setEnabled(true);
                mMotorTestButton6.setEnabled(true);
                mMotorTestButton5.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                mMotorTestButton6.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                if (motorNum > 6) {
                    mMotorTestButton7.setEnabled(true);
                    mMotorTestButton8.setEnabled(true);
                    mMotorTestButton7.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                    mMotorTestButton8.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                    if (motorNum > 8) {
                        mMotorTestButton9.setEnabled(true);
                        mMotorTestButton10.setEnabled(true);
                        mMotorTestButton11.setEnabled(true);
                        mMotorTestButton12.setEnabled(true);
                        mMotorTestButton9.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                        mMotorTestButton10.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                        mMotorTestButton11.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                        mMotorTestButton12.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_normal_color));
                    }
                }
            }
        }
    }

    @Override
    public void onStatusUpdate(Callback.Event event, DroneStatus droneStatus) {
        String textSetString;
        int textColorId;
        int imageResourceId;
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
            case ON_BATTERY_UPDATE:
                mBatteryCapacityStatusText.setText(String.valueOf(droneStatus.getBattery()) + "%");
                if (droneStatus.getBattery() < 40) {
                    mBatteryCapacityStatusText.setTextColor(getResources().getColor(R.color.preflight_check_status_text_alart_color));
                    mBatteryCapacityStatusImage.setImageResource(R.drawable.icon_circle_check_alart);
                } else {
                    mBatteryCapacityStatusText.setTextColor(getResources().getColor(R.color.preflight_check_status_text_normal_color));
                    mBatteryCapacityStatusImage.setImageResource(R.drawable.icon_circle_check_ok);
                }
                break;
            case ON_SATELLITE_UPDATE:
                updateGpsStatus(droneStatus.getSatellites());
                break;
            case ON_RC_CONNECT_UPDATE:
                textSetString = droneStatus.ismArmingCheckRCConnect() ? getString(R.string.connected) : getString(R.string.unconnected);
                textColorId = droneStatus.ismArmingCheckRCConnect() ? R.color.preflight_check_status_text_normal_color : R.color.preflight_check_status_text_alart_color;
                imageResourceId = droneStatus.ismArmingCheckRCConnect() ? R.drawable.icon_circle_check_ok : R.drawable.icon_circle_check_alart;
                mRemoteControllerStatusText.setText(textSetString);
                mRemoteControllerStatusText.setTextColor(getResources().getColor(textColorId));
                mRemoteControllerStatusImage.setImageResource(imageResourceId);
                break;
            case ON_COMPASS_READY_UPDATE:
                textSetString = droneStatus.isArmingCheckCompassReady() ? getString(R.string.ready) : getString(R.string.not_ready);
                textColorId = droneStatus.isArmingCheckCompassReady() ? R.color.preflight_check_status_text_normal_color : R.color.preflight_check_status_text_alart_color;
                imageResourceId = droneStatus.isArmingCheckCompassReady() ? R.drawable.icon_circle_check_ok : R.drawable.icon_circle_check_alart;
                mCompassStatusText.setText(textSetString);
                mCompassStatusText.setTextColor(getResources().getColor(textColorId));
                mCompassStatusImage.setImageResource(imageResourceId);
                break;
            case ON_ACCEL_READY_UPDATE:
                textSetString = droneStatus.isArmingCheckAccelReady() ? getString(R.string.ready) : getString(R.string.not_ready);
                textColorId = droneStatus.isArmingCheckAccelReady() ? R.color.preflight_check_status_text_normal_color : R.color.preflight_check_status_text_alart_color;
                imageResourceId = droneStatus.isArmingCheckAccelReady() ? R.drawable.icon_circle_check_ok : R.drawable.icon_circle_check_alart;
                mAccelerometersStatusText.setText(textSetString);
                mAccelerometersStatusText.setTextColor(getResources().getColor(textColorId));
                mAccelerometersStatusImage.setImageResource(imageResourceId);
                break;
            case ON_BAROMETER_READY_UPDATE:
                textSetString = droneStatus.isArmingCheckBarometerReady() ? getString(R.string.ready) : getString(R.string.not_ready);
                textColorId = droneStatus.isArmingCheckBarometerReady() ? R.color.preflight_check_status_text_normal_color : R.color.preflight_check_status_text_alart_color;
                imageResourceId = droneStatus.isArmingCheckBarometerReady() ? R.drawable.icon_circle_check_ok : R.drawable.icon_circle_check_alart;
                mBarometerStatusText.setText(textSetString);
                mBarometerStatusText.setTextColor(getResources().getColor(textColorId));
                mBarometerStatusImage.setImageResource(imageResourceId);
                break;
            case ON_SONAR_READY_UPDATE:
                textSetString = droneStatus.isArmingCheckSonarReady() ? getString(R.string.ready) : getString(R.string.not_ready);
                textColorId = droneStatus.isArmingCheckSonarReady() ? R.color.preflight_check_status_text_normal_color : R.color.preflight_check_status_text_alart_color;
                imageResourceId = droneStatus.isArmingCheckSonarReady() ? R.drawable.icon_circle_check_ok : R.drawable.icon_circle_check_alart;
                mSonarStatusText.setText(textSetString);
                mSonarStatusText.setTextColor(getResources().getColor(textColorId));
                mSonarStatusImage.setImageResource(imageResourceId);
                break;
            case ON_HEARTBEAT:
                Intent intent = new Intent();
                intent.putExtra(MainFragment.HEARTBEAT_TIMESTAMP_BUNDLE_KEY, droneStatus.getLastHeartbeatTime());
                getTargetFragment().onActivityResult(getTargetRequestCode(), MainFragment.HEARTBEAT_TIMESTAMP_RESULT_CODE, intent);
                break;
            case ON_ROTO_TYPE_UPDATE:
                mRotorType = droneStatus.getRotoType();
                if (!droneStatus.isFlying()) {
                    initMotorTestButton(mRotorType);
                }
                break;
            case ON_DRONE_IS_FLYING_UPDATE:
                if (droneStatus.isFlying()) {
                    setAllMotorTestButtonEnable(false);
                } else {
                    initMotorTestButton(mRotorType);
                }
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

    private void updateGpsStatus(int satellites) {
        final int gpsNewLevel = calculateGpsLevel(satellites);

        if (mGpsCheckRunnable != null) {
            return;
        }
        mGpsCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (mGpsCurrentLevel == gpsNewLevel) {
                    mGpsCheckTimestamp = System.currentTimeMillis();
                    return;
                }

                long gap = System.currentTimeMillis() - mGpsCheckTimestamp;
                if (gap > GPS_UPDATE_GAP_NO_SIGNAL) {
                    if (gpsNewLevel > 0) {
                        mGPSStatusText.setText(getResources().getString(R.string.ready));
                        mGPSStatusText.setTextColor(getResources().getColor(R.color.preflight_check_status_text_normal_color));
                        mGPSStatusImage.setImageResource(R.drawable.icon_circle_check_ok);
                    } else {
                        mGPSStatusText.setText(getResources().getString(R.string.no_signal));
                        mGPSStatusText.setTextColor(getResources().getColor(R.color.preflight_check_status_text_alart_color));
                        mGPSStatusImage.setImageResource(R.drawable.icon_circle_check_alart);
                    }
                    mGpsCurrentLevel = gpsNewLevel;
                    mGpsCheckTimestamp = System.currentTimeMillis();
                }
                mGpsCheckHandler.postDelayed(this, GPS_UPDATE_PERIOD);
            }
        };
        mGpsCheckHandler.postDelayed(mGpsCheckRunnable, GPS_UPDATE_PERIOD);
    }

    private int calculateGpsLevel(int satellites) {

        if (satellites < GPS_LEVEL_1_SATELLITE_COUNT) {
            return 0;
        }
        if (satellites < GPS_LEVEL_2_SATELLITE_COUNT) {
            return 1;
        }
        if (satellites < GPS_LEVEL_3_SATELLITE_COUNT) {
            return 2;
        }
        if (satellites < GPS_LEVEL_4_SATELLITE_COUNT) {
            return 3;
        }
        if (satellites < GPS_LEVEL_5_SATELLITE_COUNT) {
            return 4;
        }
        return 5;
    }

    private View.OnClickListener onMotorTestButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            setAllMotorTestButtonEnable(false);

            int motorNum = 0;
            switch (v.getId()) {
                case R.id.motor_test_button_1:
                    motorNum = 1;
                    mMotorTestButton1.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_2:
                    motorNum = 2;
                    mMotorTestButton2.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_3:
                    motorNum = 3;
                    mMotorTestButton3.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_4:
                    motorNum = 4;
                    mMotorTestButton4.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_5:
                    motorNum = 5;
                    mMotorTestButton5.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_6:
                    motorNum = 6;
                    mMotorTestButton6.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_7:
                    motorNum = 7;
                    mMotorTestButton7.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_8:
                    motorNum = 8;
                    mMotorTestButton8.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_9:
                    motorNum = 9;
                    mMotorTestButton9.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_10:
                    motorNum = 10;
                    mMotorTestButton10.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_11:
                    motorNum = 11;
                    mMotorTestButton11.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
                case R.id.motor_test_button_12:
                    motorNum = 12;
                    mMotorTestButton12.setBackgroundColor(getResources().getColor(R.color.preflight_check_motor_test_button_pressed_color));
                    break;
            }

            mDroneController.motorTest(motorNum);

            Thread sleepThread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initMotorTestButton(mRotorType);
                        }
                    });
                }
            };
            sleepThread.start();
        }
    };
}
