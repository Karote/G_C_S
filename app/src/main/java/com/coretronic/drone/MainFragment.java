package com.coretronic.drone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController.ParameterLoaderListener;
import com.coretronic.drone.DroneStatus.StatusChangedListener;
import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.annotation.Callback.Event;
import com.coretronic.drone.controller.SimpleDroneController;
import com.coretronic.drone.missionplan.fragments.MapViewFragment;
import com.coretronic.drone.model.Parameters;
import com.coretronic.drone.settings.PreflightCheckDialogFragment;
import com.coretronic.drone.settings.SettingsFragment;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.util.AppConfig;
import com.coretronic.ibs.drone.MavlinkLibBridge;
import com.coretronic.ibs.drone.MavlinkLibBridge.DroneParameter;
import com.coretronic.ibs.log.Logger;

import java.lang.reflect.Field;

public class MainFragment extends UnBindDrawablesFragment implements View.OnClickListener, DroneDevice.OnDeviceChangedListener, StatusChangedListener, ParameterLoaderListener {

    private final static int UPDATE_TIME_OUT = 3 * 1000;
    private final static int UPDATE_PERIOD = 1 * 1000;

    public final static int PREFLIGHT_CHECK_REQUEST_CODE = 2;
    public final static int HEARTBEAT_TIMESTAMP_RESULT_CODE = 1;
    public final static String HEARTBEAT_TIMESTAMP_BUNDLE_KEY = "heartbeat_timestamp";

    private StatusView mStatusView;
    private MainActivity mMainActivity;
    private SharedPreferences mSharedPreferences;

    private int mPreParameterCount;
    private long mGetPreParameterTime;
    private Handler mReadParamTimeOutHandler = new Handler();
    private Runnable mReadParamTimeOutRunnable;

    private ProgressDialog mReadParameterProgressDialog;

    private final int TTS_RESULT_CODE = 0x1;
    private Dialog mRetryOrAbortDialog;
    private Button mDroneConnectButton;
    private DroneDevice mDroneDevice;

    private boolean mIsConnect;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_mission_plan).setOnClickListener(this);
        view.findViewById(R.id.btn_flight_history).setOnClickListener(this);
        view.findViewById(R.id.btn_flight_setting).setOnClickListener(this);
        view.findViewById(R.id.btn_preflight).setOnClickListener(this);

        ((TextView) view.findViewById(R.id.tv_app_version)).setText("v " + BuildConfig.VERSION_NAME);
        Button logoutButton = (Button) view.findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(this);
        logoutButton.setText(mSharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, ""));

        mStatusView = (StatusView) view.findViewById(R.id.status);
        mStatusView.setStatusAlarmListener(mStatusAlarmListener);

        mDroneConnectButton = (Button) view.findViewById(R.id.drone_connect_button);
        mDroneConnectButton.setOnClickListener(onDroneConnectButtonClickListener);
        mIsConnect = ((MainActivity) getActivity()).getDroneController() != SimpleDroneController.FAKE_DRONE;
        mDroneConnectButton.setText(mIsConnect ? getResources().getText(R.string.disconnect) : getResources().getText(R.string.connect_to_drone));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
        mSharedPreferences = mMainActivity.getPreferences(Context.MODE_PRIVATE);
    }

    private boolean tryToLogin() {
        String userId = mSharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, "");
        if (userId.length() == 0) {
            return false;
        }
        mMainActivity.login(userId.replace("@", "_").replace(".", "_").toLowerCase());
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity.registerDeviceChangedListener(this);
        mMainActivity.registerDroneStatusChangedListener(this);
        mMainActivity.registerParameterLoaderListener(this);
        if (!tryToLogin()) {
            Toast.makeText(mMainActivity, "Drone Cloud login error.", Toast.LENGTH_SHORT).show();
            mMainActivity.switchToLoginFragment();
        }
        checkTTS();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMainActivity.unregisterDeviceChangedListener(this);
        mMainActivity.unregisterDroneStatusChangedListener(this);
        mMainActivity.unregisterParameterLoaderListener(this);
        mStatusView.onDisconnect();
    }

    @Override
    public void onDeviceAdded(final DroneDevice droneDevice) {
        mDroneDevice = droneDevice;
        mDroneConnectButton.setEnabled(true);
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        mDroneDevice = DroneDevice.FAKE_DRONE_DEVICE;
        mDroneConnectButton.setText(getResources().getText(R.string.connect_to_drone));
        mDroneConnectButton.setEnabled(false);
    }

    @Override
    public void onConnectingDeviceRemoved(DroneDevice droneDevice) {
        mStatusView.onDisconnect();
        mDroneDevice = DroneDevice.FAKE_DRONE_DEVICE;
        mDroneConnectButton.setText(getResources().getText(R.string.connect_to_drone));
        mDroneConnectButton.setEnabled(false);
    }

    @Override
    public void onStatusUpdate(Event event, DroneStatus droneStatus) {
        switch (event) {
            case ON_BATTERY_UPDATE:
                mStatusView.setBatteryRemainingPercentage(droneStatus.getBattery());
                break;
            case ON_SATELLITE_UPDATE:
                mStatusView.setGpsStatus(droneStatus.getSatellites());
                break;
            case ON_RADIO_SIGNAL_UPDATE:
                mStatusView.setRFStatus(droneStatus.getRadioSignal());
                break;
            case ON_HEARTBEAT:
                mStatusView.updateCommunicateLight(droneStatus.getLastHeartbeatTime());
                if (mMainActivity.getReadDroneParametersStatus() == MainActivity.READ_DRONE_PARAMETERS_NONE) {
                    mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_READING);
                    Logger.d("Parameter Reading...");
                    showParameterReadingDialog();
                    checkReadParamTimeOut(0);
                    mMainActivity.getDroneController().readAllParameters();
                }
                break;
        }
    }

    private void showParameterReadingDialog() {
        mReadParameterProgressDialog = new ProgressDialog(getActivity());
        mReadParameterProgressDialog.setTitle("Parameter Reading");
        mReadParameterProgressDialog.setMessage("Please wait......");
        mReadParameterProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mReadParameterProgressDialog.setIndeterminate(false);
        mReadParameterProgressDialog.setCancelable(false);
        mReadParameterProgressDialog.setProgress(0);
        mReadParameterProgressDialog.show();
    }

    @Override
    public void onParameterLoaded(final int parameterCount, final int totalParameterCount) {

        if (mReadParameterProgressDialog != null) {
            mReadParameterProgressDialog.setMax(totalParameterCount);
            mReadParameterProgressDialog.setProgress(parameterCount);
        }

        if (parameterCount == totalParameterCount) {
            mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_DONE);
            Logger.d("Parameter Reading DONE");

            mReadParameterProgressDialog.dismiss();

            if (mReadParamTimeOutRunnable != null) {
                mReadParamTimeOutHandler.removeCallbacks(mReadParamTimeOutRunnable);
                mReadParamTimeOutRunnable = null;
            }
            return;
        }
        checkReadParamTimeOut(parameterCount);
    }

    private void checkReadParamTimeOut(int currentCount) {

        if (currentCount == 0) {
            mGetPreParameterTime = System.currentTimeMillis();
            mPreParameterCount = currentCount;
        }

        if (mPreParameterCount != currentCount) {
            mGetPreParameterTime = System.currentTimeMillis();
            mPreParameterCount = currentCount;
        }

        if (mMainActivity.getReadDroneParametersStatus() == MainActivity.READ_DRONE_PARAMETERS_RETRY) {
            if (!checkUnsetAndRetryRead()) {
                mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_DONE);
                if (mReadParameterProgressDialog != null) {
                    mReadParameterProgressDialog.dismiss();
                }
                mReadParamTimeOutHandler.removeCallbacks(mReadParamTimeOutRunnable);
                mReadParamTimeOutRunnable = null;
            }
        }

        if (mReadParamTimeOutRunnable != null) {
            return;
        }
        mReadParamTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMainActivity.getReadDroneParametersStatus() != MainActivity.READ_DRONE_PARAMETERS_READING
                        && mMainActivity.getReadDroneParametersStatus() != MainActivity.READ_DRONE_PARAMETERS_RETRY) {
                    return;
                }

                if ((System.currentTimeMillis() - mGetPreParameterTime) > UPDATE_TIME_OUT) {
                    Logger.d("Get Parameter TIME OUT");
                    if (mMainActivity.getReadDroneParametersStatus() == MainActivity.READ_DRONE_PARAMETERS_RETRY) {
                        Thread sleepThread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mReadParameterProgressDialog != null) {
                                            mReadParameterProgressDialog.dismiss();
                                        }
                                        mDroneConnectButton.setText(getResources().getText(R.string.connect_to_drone));
                                        mIsConnect = false;
                                        performConnection();
                                        showRetryOrAbortDialog();
                                    }
                                });
                            }
                        };
                        sleepThread.start();
                        mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_FAIL);
                        mReadParamTimeOutHandler.removeCallbacks(mReadParamTimeOutRunnable);
                        mReadParamTimeOutRunnable = null;
                        return;
                    }

                    if (checkUnsetAndRetryRead()) {
                        mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_RETRY);
                        mGetPreParameterTime = System.currentTimeMillis();
                    } else {
                        mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_DONE);
                        if (mReadParameterProgressDialog != null) {
                            mReadParameterProgressDialog.dismiss();
                        }
                        mReadParamTimeOutHandler.removeCallbacks(mReadParamTimeOutRunnable);
                        mReadParamTimeOutRunnable = null;
                    }
                }
                mReadParamTimeOutHandler.postDelayed(this, UPDATE_PERIOD);
            }
        };
        mReadParamTimeOutHandler.postDelayed(mReadParamTimeOutRunnable, UPDATE_PERIOD);
    }

    private void showRetryOrAbortDialog() {
        mRetryOrAbortDialog = new Dialog(getActivity());
        mRetryOrAbortDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mRetryOrAbortDialog.setCanceledOnTouchOutside(false);
        mRetryOrAbortDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mRetryOrAbortDialog.setContentView(R.layout.popdialog_retry_or_abort_read_parameter);
        mRetryOrAbortDialog.show();

        mRetryOrAbortDialog.findViewById(R.id.abort_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRetryOrAbortDialog.dismiss();
            }
        });

        mRetryOrAbortDialog.findViewById(R.id.retry_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDroneConnectButton.setText(getResources().getText(R.string.disconnect));
                mIsConnect = true;
                performConnection();
                mRetryOrAbortDialog.dismiss();
            }
        });
    }

    private boolean checkUnsetAndRetryRead() {
        DroneParameter droneParameter = mMainActivity.getDroneController().getAllParameters();
        Field[] paramField = droneParameter.getClass().getDeclaredFields();
        for (Field field : paramField) {
            field.setAccessible(true);
            try {
                if (field.getType() != boolean.class && ((Number) field.get(droneParameter)).intValue() == MavlinkLibBridge.PARAM_UNSET) {
                    Logger.d(field.getName());
                    int paramId = getParameterID(field.getName());
                    mMainActivity.getDroneController().readParameter(paramId);
                    Logger.d("paramId:" + paramId);
                    return true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
            field.setAccessible(false);
        }
        return false;
    }

    private int getParameterID(String fieldName) {
        int paramId;
        switch (fieldName) {
            case "mRCType":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC_TYPE.ordinal();
                break;
            case "mRCRollMin":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC1_MIN.ordinal();
                break;
            case "mRCRollMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC1_MAX.ordinal();
                break;
            case "mRCRollRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC1_REV.ordinal();
                break;
            case "mRCPitchMin":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC2_MIN.ordinal();
                break;
            case "mRCPitchMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC2_MAX.ordinal();
                break;
            case "mRCPitchRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC2_REV.ordinal();
                break;
            case "mRCThrottleMin":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC3_MIN.ordinal();
                break;
            case "mRCThrottleMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC3_MAX.ordinal();
                break;
            case "mRCThrottleRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC3_REV.ordinal();
                break;
            case "mRCYawMin":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC4_MIN.ordinal();
                break;
            case "mRCYawMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC4_MAX.ordinal();
                break;
            case "mRCYawRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC4_REV.ordinal();
                break;
            case "mRCRTLRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC6_REV.ordinal();
                break;
            case "mRCGearRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC8_REV.ordinal();
                break;
            case "mRCCameraTriggerRev":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RC9_REV.ordinal();
                break;
            case "mFlightModeOne":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FLTMODE1.ordinal();
                break;
            case "mFlightModeThree":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FLTMODE3.ordinal();
                break;
            case "mFlightModeFive":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FLTMODE5.ordinal();
                break;
            case "mSmartModeOne":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_SMART_MODE1.ordinal();
                break;
            case "mSmartModeTwo":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_SMART_MODE2.ordinal();
                break;
            case "mBasicGainRoll":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RATE_RLL_P.ordinal();
                break;
            case "mBasicGainPitch":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RATE_PIT_P.ordinal();
                break;
            case "mBasicGainYaw":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RATE_YAW_P.ordinal();
                break;
            case "mAttitudeGainRoll":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_STB_RLL_P.ordinal();
                break;
            case "mAttitudeGainPitch":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_STB_PIT_P.ordinal();
                break;
            case "mAttitudeGainYaw":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_STB_YAW_P.ordinal();
                break;
            case "mBatteryCapacity":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_BATT_CAPACITY.ordinal();
                break;
            case "mBatteryRemaining":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FS_BATT_PERCT.ordinal();
                break;
            case "mBatteryOption":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FS_BATT_ENABLE.ordinal();
                break;
            case "mRCSignalLost":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FS_THR_ENABLE.ordinal();
                break;
            case "mGPSSignalLost":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FS_EKF_ACTION.ordinal();
                break;
            case "mGCSSignalLost":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FS_GCS_ENABLE.ordinal();
                break;
            case "mAltitudeMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FENCE_ALT_MAX.ordinal();
                break;
            case "mRangeMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FENCE_RADIUS.ordinal();
                break;
            case "mVerticalSpeedMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_PILOT_VELZ_MAX.ordinal();
                break;
            case "mHorizontalSpeedMax":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_WPNAV_SPEED.ordinal();
                break;
            case "mRTLAltitude":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RTL_ALT.ordinal();
                break;
            case "mRTLSpeed":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_RTL_SPEED.ordinal();
                break;
            case "mHeadingDirection":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_WP_YAW_BEHAVIOR.ordinal();
                break;
            case "mThrottlePosition":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_THR_MID.ordinal();
                break;
            case "mOpticalFlow":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_FLOW_ENABLE.ordinal();
                break;
            case "mMagneticFieldDeviation":
                paramId = Parameters.PARAMETER_ID.PARAMETER_ID_COMPASS_DEC.ordinal();
                break;
            default:
                paramId = 0;
                break;
        }
        return paramId;
    }

    private Button.OnClickListener onDroneConnectButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsConnect = !mIsConnect;
            if (mIsConnect) {
                mDroneConnectButton.setText(getResources().getText(R.string.disconnect));
            } else {
                mDroneConnectButton.setText(getResources().getText(R.string.connect_to_drone));
            }

            performConnection();
        }
    };

    private void performConnection() {
        final DroneDevice connectDevice;

        if (mIsConnect) {
            connectDevice = mDroneDevice;
            if (mMainActivity.getDroneController() != SimpleDroneController.FAKE_DRONE) {
                return;
            }
        } else {
            connectDevice = DroneDevice.FAKE_DRONE_DEVICE;
        }

        mStatusView.onDisconnect();
        mMainActivity.setReadDroneParametersStatus(MainActivity.READ_DRONE_PARAMETERS_NONE);
        mMainActivity.selectDevice(connectDevice, new MiniDronesActivity.OnDroneConnectedListener() {
            @Override
            public void onConnected() {
                Toast.makeText(getActivity(), "Init controller" + connectDevice.getName(), Toast.LENGTH_LONG).show();
                mMainActivity.initialSetting(connectDevice);
            }

            @Override
            public void onConnectFail() {
                Toast.makeText(getActivity(), "Init controller error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearUserInfo() {

        mSharedPreferences.edit()
                .putString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, "")
                .putString(AppConfig.SHARED_PREFERENCE_USER_PASSWORD_KEY, "")
                .putBoolean(AppConfig.SHARED_PREFERENCE_USER_STAY_LOGIN_KEY, false)
                .apply();
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = null;
        switch (v.getId()) {
            case R.id.btn_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
                Dialog dialog = builder.setTitle("Log Out")
                        .setMessage("Do you want to log out?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearUserInfo();
                                mMainActivity.switchToLoginFragment();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
                return;
            case R.id.btn_mission_plan:
                fragment = MapViewFragment.newInstance(MapViewFragment.FRAGMENT_TYPE_PLANNING);
                break;
            case R.id.btn_flight_history:
                fragment = MapViewFragment.newInstance(MapViewFragment.FRAGMENT_TYPE_HISTORY);
                break;
            case R.id.btn_flight_setting:
                fragment = new SettingsFragment();
                break;
            case R.id.btn_preflight:
                PreflightCheckDialogFragment preflightCheckDialog = new PreflightCheckDialogFragment();
                preflightCheckDialog.setTargetFragment(MainFragment.this, PREFLIGHT_CHECK_REQUEST_CODE);
                preflightCheckDialog.registerDialogFragmentDismissListener(new PreflightCheckDialogFragment.onDialgoFragmentDismissListener() {
                    @Override
                    public void onDialogFragmentDismiss() {
                        mMainActivity.registerDroneStatusChangedListener(MainFragment.this);
                    }
                });
                preflightCheckDialog.show(getFragmentManager().beginTransaction(), "PreflightCheckDialog");
                break;
        }
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.frame_view, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
    }

    private void checkTTS() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, TTS_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_RESULT_CODE && resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }

        if (requestCode == PREFLIGHT_CHECK_REQUEST_CODE && resultCode == HEARTBEAT_TIMESTAMP_RESULT_CODE) {
            mStatusView.updateCommunicateLight(data.getLongExtra(HEARTBEAT_TIMESTAMP_BUNDLE_KEY, 0));
        }
    }

    private StatusView.StatusAlarmListener mStatusAlarmListener = new StatusView.StatusAlarmListener() {
        @Override
        public void onGpsNoSignalAlarm() {

        }

        @Override
        public void onGpsSignalRecover() {

        }

        @Override
        public void onBatteryLowAlarm(int batteryRemaining) {

        }

        @Override
        public void onRemoteControllerDisconnect() {

        }

        @Override
        public void onDroneDisconnect() {

        }
    };
}
