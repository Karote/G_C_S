package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneDevice;
import com.coretronic.drone.DroneDevice.OnDeviceChangedListener;
import com.coretronic.drone.DroneStatus;
import com.coretronic.drone.DroneStatus.DroneMode;
import com.coretronic.drone.DroneStatus.MissionStatus;
import com.coretronic.drone.DroneStatus.StatusChangedListener;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback.Event;
import com.coretronic.drone.missionplan.adapter.NotificationCenterListAdapter;
import com.coretronic.drone.missionplan.map.DroneMap;
import com.coretronic.drone.missionplan.model.Notification;
import com.coretronic.drone.model.FlightHistory;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.RecordItem;
import com.coretronic.drone.survey.SurveyRouter;
import com.coretronic.drone.ui.ControlBarView;
import com.coretronic.drone.ui.MavInfoView;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.uvc.USBCameraMonitor;
import com.coretronic.ttslib.Speaker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;

import java.util.List;

public class MapViewFragment extends Fragment implements OnClickListener, LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener, StatusChangedListener, OnDeviceChangedListener {

    private static final String ARGUMENT_INDEX_KEY = "fragment_index";

    private static final String NOTIFICATION_TEXT_GPS_NO_SIGNAL = "GPS no Signal";
    private static final String NOTIFICATION_TEXT_DRONE_BATTERY_LOW = "Drone Battery Low - %d%% remaining";
    private static final String NOTIFICATION_TEXT_REMOTE_CONTROLLER_UNCONNECTED = "Remote Controller Unconnected";
    private static final String NOTIFICATION_TEXT_DRONE_UNCONNECTED = "Drone unconnected";
    private static final String NOTIFICATION_TEXT_DRONE_DISCONNECTED = "Drone disconnected due to signal lost";

    public static final int FRAGMENT_TYPE_PLANNING = 0;
    public static final int FRAGMENT_TYPE_HISTORY = 1;
    private static final int FRAGMENT_TYPE_ACTIVATE = 2;
    private static final int FRAGMENT_TYPE_TAP_AND_GO = 3;
    private static final int FRAGMENT_TYPE_AERIAL_SURVEY = 4;

    public static final int GOOGLE_LOCATION_REQUEST_CODE = 1000;
    private static final long LOCATION_UPDATE_MIN_TIME = 1000;
    private static int RECORD_PERIOD_IN_MILLION_SECOND = 1000;

    private int mCurrentFragmentType = 0;
    private StatusView mStatusView = null;
    private MavInfoView mMavInfoView;
    private ControlBarView mControlBarView;

    private View mMissionModeControlPanel = null;
    private View mEditOptionPanel = null;
    private View mUndoButton = null;
    private View mMoreButton = null;
    private View mEditDoneButton = null;

    private RadioGroup mMissionPlanTypeRadioGroup = null;
    private Spinner mSpinnerView = null;

    private GoogleApiClient mGoogleApiClient = null;
    private FusedLocationProviderApi mFusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest mLocationRequestHighAccuracy = null;

    private FlightHistory mFlightHistory;
    private RecordItem.Builder mRecordItemBuilder;

    private Handler mHandler;
    private boolean mSaveFlag = false;

    private Speaker mTtsSpeaker;
    private Runnable mFlightRecordRunnable = null;
    private DroneMap mDroneMap;

    private List<Mission> mCurrentMissionList;
    private MapChildFragment mCurrentFragment = null;
    private FirstPersonVisionFragment mFPVFragment;
    private float mDroneLat;
    private float mDroneLon;
    private boolean mIsSpinnerTriggerByUser = true;
    private View mFPVContainer;

    private View mWebViewContainer;
    private View mMissionPlanContainer;

    private NotificationCenterListAdapter mNotificationCenterListAdapter;
    private Button mNotificationCenterButton;
    private Dialog mNotificationPopDialog;
    private View mNotificationToastView;
    private View mNotificationCenterPopWindowView;
    private int mGPSCounts;
    private int mGPSLockType;
    private boolean mGPSLock;
    private View mPopdialogContainer;
    private ReturnToHomePopupDialogFragment mRTLPopupDialogFragment;
    private TakeOffPopupDialogFragment mTakeOffPopupDialogFragment;

    public static Fragment newInstance(int fragmentTypePlanning) {

        MapViewFragment mapViewFragment = new MapViewFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_INDEX_KEY, fragmentTypePlanning);
        mapViewFragment.setArguments(arguments);
        return mapViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocationService();
        mTtsSpeaker = new Speaker(getActivity());
        mHandler = new Handler();
        mRecordItemBuilder = new RecordItem.Builder();
        USBCameraMonitor.init(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).registerDeviceChangedListener(this);
        ((MainActivity) getActivity()).registerDroneStatusChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).unregisterDroneStatusChangedListener(this);
        ((MainActivity) getActivity()).unregisterDeviceChangedListener(this);
        mStatusView.onDisconnect();
        mDroneMap.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission_plan, container, false);

        mNotificationToastView = inflater.inflate(R.layout.toast_notification, null);
        mNotificationCenterPopWindowView = inflater.inflate(R.layout.popwindow_notification_center, null);
        mNotificationCenterListAdapter = new NotificationCenterListAdapter(inflater);
        mNotificationCenterListAdapter.setNotificationCenterListChangedListener(mNotificationCenterListChangedListener);

        initStatusBar(view);
        initSpinner(view);

        mMavInfoView = new MavInfoView(view, R.id.mav_info_panel);
        mControlBarView = new ControlBarView(view, R.id.control_button_bar, this);
        mFPVContainer = view.findViewById(R.id.fpv_container);
        mWebViewContainer = view.findViewById(R.id.webview_container);
        mMissionPlanContainer = view.findViewById(R.id.mission_plan_container);

        mPopdialogContainer = view.findViewById(R.id.popdialog_container);
        mPopdialogContainer.setVisibility(View.GONE);

        mRTLPopupDialogFragment = new ReturnToHomePopupDialogFragment();

        mRTLPopupDialogFragment.setPopDialogCallbackListener(new ReturnToHomePopupDialogFragment.PopupDialogCallback() {
            @Override
            public void onCancelButtonClick() {
                mPopdialogContainer.setVisibility(View.GONE);
            }

            @Override
            public void onConfirmButtonClick(float altitude, float stay, float speed) {
                mPopdialogContainer.setVisibility(View.GONE);
                if (mDroneController != null) {
                    mDroneController.returnToLaunch(altitude, stay, speed);
                }
                mDroneMap.clearTapAndGoPlan();
            }
        });

        mTakeOffPopupDialogFragment = new TakeOffPopupDialogFragment();
        mTakeOffPopupDialogFragment.setPopDialogCallbackListener(new TakeOffPopupDialogFragment.PopupDialogCallback() {
            @Override
            public void onCancelButtonClick() {
                mPopdialogContainer.setVisibility(View.GONE);
            }

            @Override
            public void onOKButtonClick(int takeOffAltitude) {
                mPopdialogContainer.setVisibility(View.GONE);
                mControlBarView.showLandingButton();
                if (mDroneController != null) {
                    mDroneController.takeOff(mDroneLat, mDroneLon, takeOffAltitude);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDroneMap = new DroneMap(getActivity(), view, mHandler);
        mMavInfoView.onViewCreated(view, savedInstanceState);

        mFPVFragment = new FirstPersonVisionFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.fpv_container, mFPVFragment, null).commitAllowingStateLoss();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mSaveFlag = true;
        mDroneMap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mFusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        mDroneMap.onStop();
        mSaveFlag = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTtsSpeaker != null) {
            mTtsSpeaker.destroy();
        }
        mSaveFlag = false;
        if (mFlightRecordRunnable != null) {
            mHandler.removeCallbacks(mFlightRecordRunnable);
            mFlightRecordRunnable = null;
        }
        USBCameraMonitor.onDestroy();
    }

    // Implement GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Location location = mFusedLocationProviderApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            mDroneMap.setMapToMyLocation();
        } else {
            mFusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusUpdate(Event event, DroneStatus droneStatus) {

        if (mMavInfoView != null) {
            mMavInfoView.onStatusUpdate(event, droneStatus);
        }

        switch (event) {
            case ON_BATTERY_UPDATE:
                mStatusView.setBatteryStatus(droneStatus.getBattery());
                mRecordItemBuilder.setBattery(droneStatus.getBattery());
                break;
            case ON_ALTITUDE_UPDATE:
                mRecordItemBuilder.setAltitude(droneStatus.getAltitude());
                break;
            case ON_LOCATION_UPDATE:
                mDroneLat = droneStatus.getLatitude();
                mDroneLon = droneStatus.getLongitude();
                updateOnMapDrone(droneStatus);
                mRecordItemBuilder.setLatitude(droneStatus.getLatitude());
                mRecordItemBuilder.setLongitude(droneStatus.getLongitude());
                if (isMultiMissionPlanMode()) {
                    mCurrentFragment.updateDroneLocation(mDroneLat, mDroneLon);
                }
                break;
            case ON_SATELLITE_UPDATE:
                mGPSCounts = droneStatus.getSatellites();
                mStatusView.setGpsStatus(mGPSCounts);
                mRecordItemBuilder.setSatellites(mGPSCounts);
                break;
            case ON_GPS_LOCK_TYPE_UPDATE:
                mGPSLockType = droneStatus.getGPSLockType();
                break;
            case ON_ATTITUDE_UPDATE:
                updateOnMapDrone(droneStatus);
                mRecordItemBuilder.setHeading((int) droneStatus.getHeading());
                break;
            case ON_GROUND_SPEED_UPDATE:
                mRecordItemBuilder.setGroundSpeed(droneStatus.getGroundSpeed());
                break;
            case ON_FLIGHT_DURATION_UPDATE:
                break;
            case ON_RADIO_SIGNAL_UPDATE:
                mStatusView.setRFStatus(droneStatus.getRadioSignal());
                break;
            case ON_MISSION_STATE_UPDATE:
                notificationWithTTS(droneStatus.getMissionPlanState());
                if (MissionStatus.START == droneStatus.getMissionPlanState()) {
                    mControlBarView.showStopButton();
                }
                if (!isMultiMissionPlanMode()) {
                    return;
                }
                triggerMissionRecord(droneStatus.getMissionPlanState());
                break;
            case ON_MODE_UPDATE:
                notificationWithTTS(droneStatus.getMode());
                break;
            case ON_HEARTBEAT:
                mStatusView.updateCommunicateLight(droneStatus.getLastHeartbeatTime());
                break;
        }

    }

    private StatusView.StatusAlarmListener mStatusAlarmListener = new StatusView.StatusAlarmListener() {
        @Override
        public void onGpsNoSignalAlarm() {
            Notification newAlarm = new Notification(NotificationCenterListAdapter.NOTIFICATION_TYPE_GPS, NOTIFICATION_TEXT_GPS_NO_SIGNAL, System.currentTimeMillis());
            mNotificationCenterListAdapter.updateList(newAlarm);
            showNotificationPopDialog(NOTIFICATION_TEXT_GPS_NO_SIGNAL);
        }

        @Override
        public void onGpsSignalRecover() {
            mNotificationCenterListAdapter.removeItem(NotificationCenterListAdapter.NOTIFICATION_TYPE_GPS);
        }

        @Override
        public void onBatteryLowAlarm(int batteryRemainging) {
            String notificationText = String.format(NOTIFICATION_TEXT_DRONE_BATTERY_LOW, batteryRemainging);
            Notification newAlarm = new Notification(NotificationCenterListAdapter.NOTIFICATION_TYPE_DRONE_BATTERY, notificationText, System.currentTimeMillis());
            mNotificationCenterListAdapter.updateList(newAlarm);
            showNotificationPopDialog(notificationText);
        }

        @Override
        public void onRemoteControllerDisconnect() {

        }

        @Override
        public void onDroneDisconnect() {
            Notification newAlarm = new Notification(NotificationCenterListAdapter.NOTIFICATION_TYPE_DRONE, NOTIFICATION_TEXT_DRONE_DISCONNECTED, System.currentTimeMillis());
            mNotificationCenterListAdapter.updateList(newAlarm);
            showNotificationToast(R.drawable.icon_noti_drone, NOTIFICATION_TEXT_DRONE_DISCONNECTED);
        }
    };

    private NotificationCenterListAdapter.NotificationCenterListChangedListener mNotificationCenterListChangedListener = new NotificationCenterListAdapter.NotificationCenterListChangedListener() {
        @Override
        public void onNotificationCenterListGetOne() {
            mNotificationCenterButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void onNotificationCenterListEmpty() {
            mNotificationCenterButton.setVisibility(View.GONE);
        }
    };

    private void showNotificationPopDialog(String notificationText) {
        mNotificationPopDialog = new Dialog(getActivity());
        mNotificationPopDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mNotificationPopDialog.setCanceledOnTouchOutside(false);
        mNotificationPopDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mNotificationPopDialog.setContentView(R.layout.popdialog_notification);
        ((TextView) mNotificationPopDialog.findViewById(R.id.notification_dialog_title_text)).setText(notificationText);
        mNotificationPopDialog.show();

        mNotificationPopDialog.findViewById(R.id.notification_dialog_dismiss_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotificationPopDialog.dismiss();
            }
        });
    }

    private void showNotificationToast(int iconResourceId, String notificationText) {
        ((ImageView) mNotificationToastView.findViewById(R.id.notification_toast_icon)).setImageResource(iconResourceId);
        ((TextView) mNotificationToastView.findViewById(R.id.notification_toast_text)).setText(notificationText);

        Toast notificationToast = new Toast(getActivity().getBaseContext());
        notificationToast.setGravity(Gravity.TOP | Gravity.START, getResources().getDimensionPixelOffset(R.dimen.notification_toast_position_x), getResources().getDimensionPixelOffset(R.dimen.notification_toast_position_y));
        notificationToast.setView(mNotificationToastView);
        notificationToast.setDuration(Toast.LENGTH_LONG);
        notificationToast.show();

    }

    private void showNotificationCenterPopwindow() {
        PopupWindow mNotificationCenterPopupWindow = new PopupWindow(mNotificationCenterPopWindowView, getResources().getDimensionPixelOffset(R.dimen.notification_center_popwindow_width), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mNotificationCenterPopupWindow.setTouchable(true);
        mNotificationCenterPopupWindow.setOutsideTouchable(true);
        mNotificationCenterPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        ListView notificationCenterListView = (ListView) mNotificationCenterPopWindowView.findViewById(R.id.notification_center_listview);
        notificationCenterListView.setEnabled(false);

        notificationCenterListView.setAdapter(mNotificationCenterListAdapter);

        mNotificationCenterPopupWindow.showAsDropDown(mNotificationCenterButton);
    }

    private void notificationWithTTS(DroneMode droneMode) {
        if (mTtsSpeaker == null) {
            return;
        }
        String ttsStr = null;

        switch (droneMode) {
            case MANUAL:
                ttsStr = "MANUAL";
                break;
            case ATTITUDE:
                ttsStr = "ATTITUDE";
                break;
            case GPS:
                ttsStr = "GPS";
                break;
            case LAND:
                ttsStr = "LAND";
                break;
            case RTL:
                ttsStr = "RTL";
                break;
            case ALT_HOLD:
                ttsStr = "ALT HOLD";
                break;
            case AUTO:
                ttsStr = "AUTO";
                break;
            case GUIDED:
                ttsStr = "GUIDED";
                break;
            case OPTICAL_FLOW:
                ttsStr = "OPTICAL FLOW";
                break;
        }
        mTtsSpeaker.speak(ttsStr + " Mode");

    }

    private void notificationWithTTS(MissionStatus missionPlanState) {
        if (mTtsSpeaker == null) {
            return;
        }
        switch (missionPlanState) {

            case START:
                if (mFlightRecordRunnable != null) {
                    return;
                }
                mTtsSpeaker.speak("Mission Plan Start!");
                break;
            case PAUSE:
                mTtsSpeaker.speak("Mission Plan Pause!");
                break;
            case FINISHED:
                mTtsSpeaker.speak("Mission Plan finish!");
                break;
        }
    }

    private void triggerMissionRecord(MissionStatus missionStatus) {

        switch (missionStatus) {
            case START:
                if (isTapAndGoMode()) {
                    return;
                }
                // save flight history log
                startRecord();
                break;
            case PAUSE:
                pauseRecord();
                break;
            case FINISHED:
                finishRecord();
                break;
        }
    }

    private void finishRecord() {
        pauseRecord();
        if (mFlightRecordRunnable != null) {
            mHandler.removeCallbacks(mFlightRecordRunnable);
            mFlightRecordRunnable = null;
        }
    }

    private void pauseRecord() {
        mSaveFlag = false;
    }

    private void startRecord() {
        if (mFlightRecordRunnable != null || mCurrentMissionList == null) {
            return;
        }

        mFlightHistory = ((MainActivity) getActivity()).createFlightHistory(mCurrentMissionList);
        mSaveFlag = true;
        mFlightRecordRunnable = new Runnable() {
            @Override
            public void run() {
                if (mSaveFlag) {
                    mRecordItemBuilder.setCurrentTimeStamp(System.currentTimeMillis());
                    mFlightHistory.addRecord(mRecordItemBuilder.create());
                }
                mHandler.postDelayed(mFlightRecordRunnable, RECORD_PERIOD_IN_MILLION_SECOND);
            }
        };
        mHandler.post(mFlightRecordRunnable);
    }

    public List<Mission> getMissionList() {
        return mCurrentMissionList;
    }

    private void clearMissionList() {
        mCurrentMissionList = null;
    }

    private void initSpinner(View view) {

        mSpinnerView = (Spinner) view.findViewById(R.id.mission_plan_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.mission_plan_menu, R.layout.spinner_style);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        mSpinnerView.setAdapter(spinnerAdapter);

        Bundle bundle = this.getArguments();
        int spinnerIndex = FRAGMENT_TYPE_PLANNING;
        if (bundle != null) {
            spinnerIndex = bundle.getInt(ARGUMENT_INDEX_KEY, FRAGMENT_TYPE_PLANNING);
        }
        mSpinnerView.setSelection(spinnerIndex);
        mSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mIsSpinnerTriggerByUser) {
                    mIsSpinnerTriggerByUser = true;
                    return;
                }
                switch (position) {
                    case FRAGMENT_TYPE_PLANNING:
                        mMissionPlanTypeRadioGroup.check(R.id.tap_and_go_button);
                        setFragmentTransaction(FRAGMENT_TYPE_TAP_AND_GO);
                        break;
                    case FRAGMENT_TYPE_HISTORY:
                        setFragmentTransaction(FRAGMENT_TYPE_HISTORY);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initStatusBar(View view) {

        mStatusView = (StatusView) view.findViewById(R.id.status);
        mStatusView.setStatusAlarmListener(mStatusAlarmListener);
        mNotificationCenterButton = (Button) view.findViewById(R.id.notification_center_button);
        mNotificationCenterButton.setVisibility(View.GONE);
        mNotificationCenterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotificationCenterPopwindow();
            }
        });
        mMissionModeControlPanel = view.findViewById(R.id.marker_editor_control_panel);
        mEditOptionPanel = view.findViewById(R.id.edit_option_layout);
        mMissionPlanTypeRadioGroup = (RadioGroup) view.findViewById(R.id.multi_or_single_radioGroup);

        mUndoButton = view.findViewById(R.id.undo_button);
        mUndoButton.setOnClickListener(this);
        mMoreButton = view.findViewById(R.id.more_button);
        mMoreButton.setOnClickListener(this);
        mEditDoneButton = view.findViewById(R.id.edit_done_button);
        mEditDoneButton.setEnabled(false);
        mEditDoneButton.setOnClickListener(this);
        view.findViewById(R.id.edit_cancel_button).setOnClickListener(this);
        view.findViewById(R.id.multi_way_point_button).setOnClickListener(this);
        view.findViewById(R.id.tap_and_go_button).setOnClickListener(this);
        view.findViewById(R.id.back_to_main_button).setOnClickListener(this);

        View aerialSurveyButton = view.findViewById(R.id.aerial_survey_button);
        if (aerialSurveyButton != null) {
            aerialSurveyButton.setOnClickListener(this);
        }

    }

    private void setFragmentTransaction(int fragmentType) {
        if (mCurrentFragmentType == fragmentType) {
            return;
        }
        hideFPVFragment();
        mCurrentFragmentType = fragmentType;
        switch (fragmentType) {
            case FRAGMENT_TYPE_ACTIVATE:
                mCurrentFragment = PlanningFragment.newInstance(true);
                break;
            case FRAGMENT_TYPE_PLANNING:
                mCurrentFragment = PlanningFragment.newInstance(false);
                break;
            case FRAGMENT_TYPE_HISTORY:
                mCurrentFragment = new HistoryFragment();
                break;
            case FRAGMENT_TYPE_TAP_AND_GO:
                mCurrentFragment = new TapAndGoFragment();
                break;
            case FRAGMENT_TYPE_AERIAL_SURVEY:
                mCurrentFragment = AerialSurveyFragment.newInstance();
                break;
        }

        clearMissionList();

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mission_plan_container, mCurrentFragment, null).commit();
        onChildFragmentChanged(fragmentType);
        mDroneMap.setOnMapEventListener(mCurrentFragment);
    }

    private boolean isTapAndGoMode() {
        return mCurrentFragment instanceof TapAndGoFragment;
    }

    private boolean isMultiMissionPlanMode() {
        return mCurrentFragment instanceof PlanningFragment;
    }

    private void onChildFragmentChanged(int fragmentType) {
        if (fragmentType == FRAGMENT_TYPE_ACTIVATE) {
            fragmentType = FRAGMENT_TYPE_PLANNING;
        }

        boolean isTapAndGoMode = fragmentType == FRAGMENT_TYPE_TAP_AND_GO;
        boolean canAddMarker = fragmentType != FRAGMENT_TYPE_HISTORY;
        int undoAndMoreButtonVisibility = fragmentType == FRAGMENT_TYPE_PLANNING ? View.VISIBLE : View.GONE;
        int modeControlPanelVisibility = fragmentType != FRAGMENT_TYPE_HISTORY ? View.VISIBLE : View.GONE;
        int mavInfoPanelVisibility = fragmentType != FRAGMENT_TYPE_HISTORY ? View.VISIBLE : View.GONE;
        int controlButtonBarVisibility = fragmentType != FRAGMENT_TYPE_HISTORY ? View.VISIBLE : View.GONE;
        int droneControlButtonBarVisibility = fragmentType != FRAGMENT_TYPE_PLANNING ? View.GONE : View.VISIBLE;
        mDroneMap.init(isTapAndGoMode, canAddMarker);
        setEditOptionShow(false);
        setUndoAndMoreButtonVisibility(undoAndMoreButtonVisibility);
        mMissionModeControlPanel.setVisibility(modeControlPanelVisibility);
        mMavInfoView.setVisibility(mavInfoPanelVisibility);
        mControlBarView.setVisibility(controlButtonBarVisibility);
        mControlBarView.setDroneControlBarVisibility(droneControlButtonBarVisibility);
    }

    public void setUndoAndMoreButtonVisibility(int visibility) {
        mUndoButton.setVisibility(visibility);
        mMoreButton.setVisibility(visibility);
        mUndoButton.setEnabled(false);
    }

    public void setUndoButtonEnable(boolean enable) {
        mUndoButton.setEnabled(enable);
    }


    public void getMissionPlanPathLengthAndTime(List<Integer> speedList) {
        mDroneMap.getMissionPlanPathLengthAndTime(speedList);
    }

    public void saveWebViewScreenShot(String fileName) {
        mDroneMap.saveWebViewScreenShot(fileName);
    }

    public Bitmap getWebViewScreenShot(int width, int height) {
        return mDroneMap.getWebViewScreenShot(width, height);
    }

    private void setUpLocationService() {
        mLocationRequestHighAccuracy = LocationRequest.create();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(LOCATION_UPDATE_MIN_TIME);
        mLocationRequestHighAccuracy.setFastestInterval(16);

        // The main entry point for Google Play services integration
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here.
                        mFusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, MapViewFragment.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), GOOGLE_LOCATION_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getActivity(), "Can not Enable GPS", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GOOGLE_LOCATION_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        mFusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, this);
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.back_to_main_button:
                getActivity().onBackPressed();
                return;
            case R.id.edit_cancel_button:
                setEditOptionShow(false);
                mDroneMap.setAddMarkerEnable(true);
                break;
            case R.id.edit_done_button:
                setEditOptionShow(false);
                mDroneMap.setAddMarkerEnable(true);
                break;
            case R.id.multi_way_point_button:
                setFragmentTransaction(FRAGMENT_TYPE_PLANNING);
                return;
            case R.id.tap_and_go_button:
                setFragmentTransaction(FRAGMENT_TYPE_TAP_AND_GO);
                return;
            case R.id.aerial_survey_button:
                setFragmentTransaction(FRAGMENT_TYPE_AERIAL_SURVEY);
                return;
            case R.id.plan_go_button:
                if (getDroneController() == null) {
                    return;
                }
                mControlBarView.showStopButton();
                break;
            case R.id.plan_stop_button:
                if (getDroneController() == null) {
                    return;
                }
                mControlBarView.showGoButton();
                mDroneMap.clearTapAndGoPlan();
                break;
            case R.id.drone_takeoff_button:
                showTakeOffPopupDialog();
                return;
            case R.id.drone_landing_button:
                if (getDroneController() != null) {
                    getDroneController().land();
                }
                mControlBarView.showTakeoffButton();
                mDroneMap.clearTapAndGoPlan();
                return;
            case R.id.drone_rtl_button:
                showRTLPopupDialog();
                return;
            case R.id.plan_play_button:
//                if (getDroneController() != null) {
//                    getDroneController().resumeMission();
//                }
                mControlBarView.showPauseButton();
            case R.id.plan_pause_button:
                if (getDroneController() != null) {
                    getDroneController().pauseMission();
                }
                mControlBarView.showPlayButton();
                return;
            case R.id.my_location_button:
                setMapToMyLocation();
                return;
            case R.id.drone_location_button:
                setMapToDrone();
                return;
            case R.id.fit_map_button:
                fitMapShowAll();
                return;
            case R.id.map_type_button:
                changeMapType();
                return;
            case R.id.map_fpv_switch_btn:
                if (v.isSelected()) {
                    hideFPVFragment();
                } else {
                    showFPVFragment();
                }
        }

        if (mCurrentFragment == null) {
            return;
        }
        mCurrentFragment.onClick(v);

    }

    private void showTakeOffPopupDialog() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.popdialog_container, mTakeOffPopupDialogFragment, null).commit();

        mPopdialogContainer.setVisibility(View.VISIBLE);
    }

    private void showRTLPopupDialog() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.popdialog_container, mRTLPopupDialogFragment, null).commit();

        mPopdialogContainer.setVisibility(View.VISIBLE);
    }
    public void hideFPVFragment() {
        mUndoButton.setEnabled(true);
        mMoreButton.setEnabled(true);
        scaleUpWebViewContainer();
        scaleDownFPVContainer();
        mFPVContainer.bringToFront();
        mControlBarView.onFPVHided();
        mFPVFragment.onFPVHide();
        mMissionPlanContainer.setVisibility(View.VISIBLE);
        mDroneMap.setMarkerNormal();
    }

    public void showFPVFragment() {
        mUndoButton.setEnabled(false);
        mMoreButton.setEnabled(false);
        scaleDownWebViewContainer();
        scaleUpFPVContainer();
        mWebViewContainer.bringToFront();
        mControlBarView.onFPVShowed();
        mFPVFragment.onFPVShow();
        mMissionPlanContainer.setVisibility(View.INVISIBLE);
        mDroneMap.setMarkerSmall();
    }

    private void scaleDownWebViewContainer() {
        int width = getResources().getDimensionPixelOffset(R.dimen.fpv_window_width);
        int height = getResources().getDimensionPixelOffset(R.dimen.fpv_window_height);
        int topMargin = getResources().getDimensionPixelOffset(R.dimen.fpv_window_top_margin);
        int leftMargin = getResources().getDimensionPixelOffset(R.dimen.fpv_window_left_margin);
        mWebViewContainer.getLayoutParams().width = width;
        mWebViewContainer.getLayoutParams().height = height;
        ((ViewGroup.MarginLayoutParams) mWebViewContainer.getLayoutParams()).setMargins(leftMargin, topMargin, 0, 0);
        mWebViewContainer.requestLayout();
    }

    private void scaleUpWebViewContainer() {
        mWebViewContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        mWebViewContainer.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        ((ViewGroup.MarginLayoutParams) mWebViewContainer.getLayoutParams()).setMargins(0, 0, 0, 0);
        mWebViewContainer.requestLayout();
    }

    private void scaleDownFPVContainer() {
        int width = getResources().getDimensionPixelOffset(R.dimen.fpv_window_width);
        int height = getResources().getDimensionPixelOffset(R.dimen.fpv_window_height);
        int topMargin = getResources().getDimensionPixelOffset(R.dimen.fpv_window_top_margin);
        int leftMargin = getResources().getDimensionPixelOffset(R.dimen.fpv_window_left_margin);
        mFPVContainer.getLayoutParams().width = width;
        mFPVContainer.getLayoutParams().height = height;
        ((ViewGroup.MarginLayoutParams) mFPVContainer.getLayoutParams()).setMargins(leftMargin, topMargin, 0, 0);
        mFPVContainer.requestLayout();
    }

    private void scaleUpFPVContainer() {
        mFPVContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        mFPVContainer.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
        ((ViewGroup.MarginLayoutParams) mFPVContainer.getLayoutParams()).setMargins(0, 0, 0, 0);
        mFPVContainer.requestLayout();

    }

    public DroneController getDroneController() {
        return ((MainActivity) getActivity()).getDroneController();
    }

    public void setEditOptionShow(boolean isShow) {
        if (isShow) {
            mEditOptionPanel.setVisibility(View.VISIBLE);
            mMissionPlanTypeRadioGroup.setVisibility(View.GONE);
            mMoreButton.setVisibility(View.GONE);
            mUndoButton.setVisibility(View.GONE);
            mEditDoneButton.setEnabled(false);
        } else {
            mEditOptionPanel.setVisibility(View.GONE);
            mMissionPlanTypeRadioGroup.setVisibility(View.VISIBLE);
            mMoreButton.setVisibility(View.VISIBLE);
            mUndoButton.setVisibility(View.VISIBLE);
            mEditDoneButton.setEnabled(false);
        }
        mDroneMap.setAddMarkerEnable(!isShow);
    }

    public void setEditDoneEnable() {
        mEditDoneButton.setEnabled(true);
    }

    public void activateWithExistedMission(List<Mission> missionList) {
        mCurrentMissionList = missionList;
        mIsSpinnerTriggerByUser = false;
        mSpinnerView.setSelection(FRAGMENT_TYPE_PLANNING);
        mMissionPlanTypeRadioGroup.check(R.id.multi_way_point_button);
        setFragmentTransaction(FRAGMENT_TYPE_ACTIVATE);
    }

    public void updateMissions(List<Mission> missions) {
        mCurrentMissionList = missions;
        mDroneMap.updateMissions(missions);
    }

    public void fitMapShowAll() {
        mDroneMap.fitMapShowAll();
    }

    public void fitMapShowAllMissions() {
        mDroneMap.fitMapShowAllMissions();
    }

    public void setMapToMyLocation() {
        mDroneMap.setMapToMyLocation();
    }

    private void setMapToDrone() {
        mDroneMap.setMapToDroneLocation(mDroneLat, mDroneLon);
    }

    public void changeMapType() {
        mDroneMap.changeMapType();
    }

    public void clearTapMarker() {
        mDroneMap.clearTapMarker();
    }

    public void setTapGoPath() {
        mDroneMap.setTapGoPath();
    }

    public void setMapCanAddMarker(boolean enable) {
        mDroneMap.setAddMarkerEnable(enable);
    }

    public void loadHistory(List<Mission> missions, List<Float> flightPath) {
        mDroneMap.loadHistory(missions, flightPath);
    }

    public void clearHistoryMarkerPath() {
        mDroneMap.clearHistoryMarkerPath();
    }

    private void updateOnMapDrone(DroneStatus droneStatus) {
        mDroneMap.updateDroneLocation(droneStatus.getLatitude(), droneStatus.getLongitude(), droneStatus.getHeading());
    }

    @Override
    public void onDeviceAdded(DroneDevice droneDevice) {

    }

    @Override
    public void onDeviceRemoved(DroneDevice droneDevice) {

    }

    @Override
    public void onConnectingDeviceRemoved(DroneDevice droneDevice) {
        mStatusView.onDisconnect();
    }

    public void clearSurvey() {
        mDroneMap.clearSurvey();
    }

    public void clearFootprint() {
        mDroneMap.clearFootprint();
    }

    public void updateFootprints(SurveyRouter routerGrid) {
        mDroneMap.updateFootprints(routerGrid);
    }

    public void clearMap() {
        mDroneMap.clearMap();
    }

    public void updatePolygon(List<Coord2D> polygonPoints) {
        mDroneMap.updatePolygon(polygonPoints);
    }

    void setMavInfoViewVisibility(int mavInfoViewVisibility) {
        mMavInfoView.setVisibility(mavInfoViewVisibility);
    }

    void setDroneControlBarVisibility(int visibility) {
        mControlBarView.setDroneControlBarVisibility(visibility);
    }

    public boolean isGPSLock() {
        mGPSLock = mGPSCounts >= 6 && mGPSLockType > 2;
        return mGPSLock;
    }
}