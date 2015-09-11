package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.DroneController.DroneMode;
import com.coretronic.drone.DroneController.DroneStatus;
import com.coretronic.drone.DroneController.MissionStatus;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback.Event;
import com.coretronic.drone.missionplan.map.DroneMap;
import com.coretronic.drone.model.FlightHistory;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.RecordItem;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.ttslib.Speaker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import java.util.List;

public class MapViewFragment extends Fragment implements View.OnClickListener, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Drone.StatusChangedListener {

    private static final String ARGUMENT_INDEX_KEY = "fragment_index_index";

    public static final int FRAGMENT_TYPE_PLANNING = 0;
    public static final int FRAGMENT_TYPE_HISTORY = 1;
    private static final int FRAGMENT_TYPE_ACTIVATE = 2;
    private static final int FRAGMENT_TYPE_TAP_AND_GO = 3;

    public static final int GOOGLE_LOCATION_REQUEST_CODE = 1000;
    private static final long LOCATION_UPDATE_MIN_TIME = 1000;
    private static int RECORD_PERIOD_IN_MILLION_SECOND = 1000;

    private int mCurrentFragmentType = 0;
    private StatusView mStatusView = null;

    private LinearLayout layout_editMarker = null;
    private LinearLayout layout_deleteIcon = null;
    private LinearLayout layout_deleteOption = null;

    private View mUndoButton = null;
    private View mDeleteButton = null;
    private RadioGroup mMissionPlanTypeRadioGroup = null;
    private Spinner mSpinnerView = null;
    private List<Mission> mCurrentMissionList;
    private MavInfoFragment mCurrentFragment = null;

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

    private long mDroneLat;
    private long mDroneLon;
    private boolean mIsSpinnerTriggerByUser = true;

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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).registerDroneStatusChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).unregisterDroneStatusChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpEditMarkerLayout(view);
        setUpTopBarButton(view);
        mDroneMap = new DroneMap(getActivity(), view, mHandler);
        mStatusView = (StatusView) view.findViewById(R.id.status);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mSaveFlag = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mFusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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

        switch (event) {
            case ON_BATTERY_UPDATE:
                mStatusView.setBatteryStatus(droneStatus.getBattery());
                mRecordItemBuilder.setBattery(droneStatus.getBattery());
                break;
            case ON_ALTITUDE_UPDATE:
                if (mCurrentFragment != null) {
                    mCurrentFragment.onAltitdueUpdate(droneStatus.getAltitude());
                }
                mRecordItemBuilder.setAltitude(droneStatus.getAltitude());
                break;
            case ON_LOCATION_UPDATE:
                if (mCurrentFragment != null) {
                    mCurrentFragment.onLocationUpdate(droneStatus.getLatitude(), droneStatus.getLongitude());
                }
                mDroneLat = droneStatus.getLatitude();
                mDroneLon = droneStatus.getLongitude();
                updateOnMapDrone(droneStatus);
                mRecordItemBuilder.setLatitude(droneStatus.getLatitude());
                mRecordItemBuilder.setLongitude(droneStatus.getLongitude());
                break;
            case ON_SATELLITE_UPDATE:
                mStatusView.setGpsStatus(droneStatus.getSatellites());
                mRecordItemBuilder.setSatellites(droneStatus.getSatellites());
                break;
            case ON_ATTITUDE_UPDATE:
                if (mCurrentFragment != null) {
                    mCurrentFragment.onAttitudeUpdate(droneStatus.getYaw(), droneStatus.getRoll(), droneStatus.getPitch());
                }
                updateOnMapDrone(droneStatus);
                mRecordItemBuilder.setHeading((int) droneStatus.getYaw());
                break;
            case ON_SPEED_UPDATE:
                if (mCurrentFragment != null) {
                    mCurrentFragment.onSpeedUpdate(droneStatus.getSpeed());
                }
                mRecordItemBuilder.setSpeed(droneStatus.getSpeed());
                break;
            case ON_FLIGHT_DURATION_UPDATE:
                if (mCurrentFragment != null) {
                    mCurrentFragment.onFlightTimeUpdate(droneStatus.getDuration());
                }
                break;
            case ON_RADIO_SIGNAL_UPDATE:
                mStatusView.setGpsStatus(droneStatus.getRadioSignal());
                break;
            case ON_MISSION_STATE_UPDATE:
                notificationWithTTS(droneStatus.getMissionPlanState());
                if (!isMultiMissionPlanMode()) {
                    return;
                }
                triggerMissionRecord(droneStatus.getMissionPlanState());
                break;
            case ON_MODE_UPDATE:
                notificationWithTTS(droneStatus.getMode());
                break;
        }

    }

    private void notificationWithTTS(DroneMode droneMode) {
        if (mTtsSpeaker != null) {
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
            case IOC:
                ttsStr = "IOC";
                break;
            case LAND:
                ttsStr = "LAND";
                break;
            case RTL:
                ttsStr = "RTL";
                break;
            case POI:
                ttsStr = "POI";
                break;
            case HEADING_LOCK:
                ttsStr = "HEADING LOCK";
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

    private void setUpTopBarButton(View view) {
        view.findViewById(R.id.button_back_to_main).setOnClickListener(this);

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
                        mMissionPlanTypeRadioGroup.check(R.id.btn_action_tap_and_go);
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

    private void setUpEditMarkerLayout(View view) {
        layout_editMarker = (LinearLayout) view.findViewById(R.id.custom_layout_edit_marker);
        layout_deleteIcon = (LinearLayout) view.findViewById(R.id.layout_delete_icon);
        layout_deleteOption = (LinearLayout) view.findViewById(R.id.layout_delete_option);
        layout_deleteOption.setVisibility(LinearLayout.INVISIBLE);

        mUndoButton = view.findViewById(R.id.btn_action_plan_undo);
        mUndoButton.setOnClickListener(this);
        mDeleteButton = view.findViewById(R.id.btn_action_plan_delete);
        mDeleteButton.setOnClickListener(this);
        view.findViewById(R.id.btn_delete_done).setOnClickListener(this);
        view.findViewById(R.id.btn_delete_all).setOnClickListener(this);

        mMissionPlanTypeRadioGroup = (RadioGroup) view.findViewById(R.id.radiogroup_multi_or_single);
        mMissionPlanTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_action_multi_point:
                        setFragmentTransaction(FRAGMENT_TYPE_PLANNING);
                        break;
                    case R.id.btn_action_tap_and_go: // Tap & GO
                        setFragmentTransaction(FRAGMENT_TYPE_TAP_AND_GO);
                        break;
                }
            }
        });
    }

    private void setFragmentTransaction(int fragmentType) {
        if (mCurrentFragmentType == fragmentType) {
            return;
        }
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
        }

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
        switch (fragmentType) {
            case FRAGMENT_TYPE_ACTIVATE:
            case FRAGMENT_TYPE_PLANNING:
                mDroneMap.init(false, true);
                layout_editMarker.setVisibility(View.VISIBLE);
                mUndoButton.setVisibility(View.VISIBLE);
                mDeleteButton.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_TYPE_HISTORY:
                mDroneMap.init(false, false);
                layout_editMarker.setVisibility(View.GONE);
                setDeleteOptionShow(false);
                break;
            default:
            case FRAGMENT_TYPE_TAP_AND_GO:
                mDroneMap.init(true, true);
                layout_editMarker.setVisibility(View.VISIBLE);
                mUndoButton.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.GONE);
                break;
        }
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
//                    final LocationSettingsStates state = result.getLocationSettingsStates();
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
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Toast.makeText(getActivity(), "Can not Enable GPS", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
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
            case R.id.button_back_to_main:
                getActivity().onBackPressed();
                return;
            case R.id.btn_delete_all:
                setDeleteOptionShow(false);
                mDroneMap.clearMissionPlanningMarkers();
                mDroneMap.setAddMarkerEnable(true);
                break;
            case R.id.btn_action_plan_delete:
                setDeleteOptionShow(true);
                break;
            case R.id.btn_delete_done:
                setDeleteOptionShow(false);
                mDroneMap.setAddMarkerEnable(true);
                break;
        }

        if (mCurrentFragment == null) {
            return;
        }
        mCurrentFragment.onClick(v);

    }

    public DroneController getDroneController() {
        return ((MainActivity) getActivity()).getDroneController();
    }

    private void setDeleteOptionShow(boolean isShow) {
        if (isShow) {
            layout_deleteOption.setVisibility(View.VISIBLE);
            layout_deleteIcon.setVisibility(View.INVISIBLE);
            mUndoButton.setVisibility(View.GONE);
        } else {
            layout_deleteOption.setVisibility(View.INVISIBLE);
            layout_deleteIcon.setVisibility(View.VISIBLE);
            mUndoButton.setVisibility(View.VISIBLE);
        }
    }

    public void activateWithMission(List<Mission> missionList) {
        mCurrentMissionList = missionList;
        mIsSpinnerTriggerByUser = false;
        mSpinnerView.setSelection(FRAGMENT_TYPE_PLANNING);
        mMissionPlanTypeRadioGroup.check(R.id.btn_action_multi_point);
        setFragmentTransaction(FRAGMENT_TYPE_ACTIVATE);
    }

    public void updateMissions(List<Mission> missions) {
        mCurrentMissionList = missions;
        mDroneMap.updateMissions(missions);
    }

    public void fitMapShowAllMission() {
        mDroneMap.fitMapShowAllMission();
    }

    public void setMapToMyLocation() {
        mDroneMap.setMapToMyLocation();
    }

    public void setMapToDrone() {
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

    public void loadHistory(List<Float> markerList, List<Long> flightPath) {
        mDroneMap.loadHistory(markerList, flightPath);
    }

    public void clearHistoryMarkerPath() {
        mDroneMap.clearHistoryMarkerPath();
    }

    public void fitMapShowDroneAndMe() {
        mDroneMap.fitMapShowDroneAndMe();
    }

    private void updateOnMapDrone(DroneStatus droneStatus) {
        mDroneMap.updateDroneLocation(droneStatus.getLatitude(), droneStatus.getLongitude(), droneStatus.getYaw());
    }

    public static Mission createNewMission(float latitude, float longitude, float altitude,
                                           int waitSeconds, boolean autoContinue, int radius, Mission.Type type) {
        Mission.Builder builder = new Mission.Builder();

        builder.setLatitude(latitude);
        builder.setLongitude(longitude);
        builder.setAltitude(altitude);
        builder.setWaitSeconds(waitSeconds);
        builder.setAutoContinue(autoContinue);
        builder.setRadius(radius);
        builder.setType(type);

        return builder.create();
    }

}