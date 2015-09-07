package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.model.FlightHistory;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.model.RecordItem;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.utility.AppConfig;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.List;

public class WaypointEditorFragment extends Fragment
        implements View.OnClickListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        Drone.StatusChangedListener, DroneController.MissionLoaderListener,
        FollowMeFragment.FollowMeInterface, HistoryFragment.HistoryInterface,
        TapAndGoFragment.TapAndGoInterface {

    public static final int FRAGMENTTYPE_PLANNING = 0;
    public static final int FRAGMENTTYPE_HISTORY = 1;
    public static final int FRAGMENTTYPE_ACTIVATE = 2;
    public static final int FRAGMENTTYPE_TAPANDGO = 3;
    public static final int REQUEST_LOCATION = 1000;

    private static final long LOCATION_UPDATE_MIN_TIME = 1000;

    private boolean canMapAddMarker = true;
    private boolean isTapAndGo = true;
    private boolean isSwitchFromHistoryFile = false;

    private int pre_fragmentType = 0;
    private int spinnerIndex = FRAGMENTTYPE_PLANNING;
    private int droneHeading = 0;
    private double nowLatget = 0;
    private double nowLngget = 0;
    private long droneLat = 0;
    private long droneLng = 0;

    private StatusView statusView = null;
    private WebView webview_Map = null;

    private LinearLayout layout_editMarker = null;
    private LinearLayout layout_deleteIcon = null;
    private LinearLayout layout_deleteOption = null;

    private Button b_action_plan_undo = null;
    private Button b_action_plan_delete = null;
    private RadioGroup radioGroup_multi_or_single = null;

    private Spinner spinnerView = null;
    private List<Mission> currentMissionList;

    private GoogleApiClient mGoogleApiClient = null;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest mLocationRequestHighAccuracy = null;
    private Location nowlocation = null;

    private ProgressDialog progressDialog = null;
    private FragmentActivity fragmentActivity = null;
    private FragmentManager fragmentChildManager = null;

    private MavInfoFragment currentFragment = null;

    // Save Drone Info
//    private DroneInfo currentDroneInfo;
//    private DroneInfo tmpDroneInfo;
    private FlightHistory flightHistory;
    private RecordItem.Builder recordItemBuilder;

    // File Helper
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Handler handler;
    private boolean saveFlag = false;
    private int saveDelayTime = 1000;
    private DroneController.MissionStatus droneMissionState = DroneController.MissionStatus.FINISHED;
    private DroneController.DroneMode currentDroneMode = null;

    // TTS
    private Speaker ttsSpeaker;
    private Runnable saveFileRunnable = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocationService();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            spinnerIndex = bundle.getInt(AppConfig.MAIN_FRAG_ARGUMENT, 0);
        }

        fragmentActivity = getActivity();
        fragmentChildManager = getChildFragmentManager();

        // flight history init
        sharedPreferences = getActivity().getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, 0);
        gson = new Gson();
        handler = new Handler();
        // tts init
        ttsSpeaker = new Speaker(getActivity());

        // Drone status change
        ((MainActivity) fragmentActivity).registerDroneStatusChangedListener(this);

        // record builder
        recordItemBuilder = new RecordItem.Builder();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(fragmentActivity) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
                dismiss();
            }
        };
        progressDialog.setCancelable(false);
        return inflater.inflate(R.layout.fragment_mission_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpWebView(view);
        setUpTopBarButton(view);
        statusView = (StatusView) view.findViewById(R.id.status);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        saveFlag = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        webview_Map.loadUrl("about:blank");
        saveFlag = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ttsSpeaker != null) {
            ttsSpeaker.destroy();
        }
        // log record stop
        ((MainActivity) fragmentActivity).unregisterDroneStatusChangedListener(this);
        saveFlag = false;
        if (saveFileRunnable != null) {
            handler.removeCallbacks(saveFileRunnable);
            saveFileRunnable = null;
        }
    }

    // Implement GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Location location = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            nowlocation = location;
            nowLatget = nowlocation.getLatitude();
            nowLngget = nowlocation.getLongitude();
        } else {
            fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    // End GoogleApiClient.ConnectionCallbacks


    // Implement GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
    // End GoogleApiClient.OnConnectionFailedListener


    // Implement LocationListener
    @Override
    public void onLocationChanged(Location location) {
        nowlocation = location;
        nowLatget = nowlocation.getLatitude();
        nowLngget = nowlocation.getLongitude();
    }
    // End LocationListener


    // Implement MissionLoaderListener
    @Override
    public void onLoadCompleted(final List<Mission> missions) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null)
                    progressDialog.dismiss();
                if (missions == null || missions.size() == 0) {
                    Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
                } else {
                    ((PlanningFragment) currentFragment).missionAdapterSetData(missions);
                    writeMissionsToMap(missions);
                }
            }
        });
    }
    // End MissionLoaderListener

    private void setUpWebView(View view) {
        webview_Map = (WebView) view.findViewById(R.id.waypoint_webview);
        webview_Map.addJavascriptInterface(new javascriptInterface(getActivity()), "AndroidFunction");
        webview_Map.getSettings().setJavaScriptEnabled(true);
        // permission to disclose the user's location to JavaScript.
        webview_Map.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webview_Map.getSettings().setGeolocationEnabled(true);
        webview_Map.loadUrl("file:///android_asset/GoogleMap.html");
    }


    // Implement Drone.StatusChangedListener
    @Override
    public void onBatteryUpdate(final int battery) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setBatteryStatus(battery);
            }
        });

        // save info
        recordItemBuilder.setBattery(battery);
    }

    @Override
    public void onAltitudeUpdate(final float altitude) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentFragment != null) {
                    currentFragment.setMavInfoAltitude(altitude);
                }
            }
        });
        // save info
        recordItemBuilder.setAltitude(altitude);
    }

    @Override
    public void onRadioSignalUpdate(int rssi) {
        // save info
    }

    @Override
    public void onSpeedUpdate(final float groundSpeed) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentFragment != null) {
                    currentFragment.setMavInfoSpeed(groundSpeed);
                }
            }
        });
        // save info
        recordItemBuilder.setSpeed(groundSpeed);
    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        droneLat = lat;
        droneLng = lon;
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (currentFragment != null) {
                        currentFragment.setMavInfoLocation(droneLat, droneLng);
                    }
                    webview_Map.loadUrl("javascript:updateDroneLocation(" + droneLat + "," + droneLng + "," + droneHeading + ")");
                    // GPS status
                    statusView.setGpsVisibility(((MainActivity) getActivity()).hasGPSSignal(eph) ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // save info
        recordItemBuilder.setLatitude(lat);
        recordItemBuilder.setLongitude(lon);
        recordItemBuilder.setSatellites(eph);
    }

    @Override
    public void onHeadingUpdate(final int heading) {
        droneHeading = heading;
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webview_Map.loadUrl("javascript:updateDroneLocation(" + droneLat + "," + droneLng + "," + droneHeading + ")");
            }
        });
        // save info
        recordItemBuilder.setHeading(heading);
    }

    @Override
    public void onDroneStateUpdate(DroneController.DroneMode droneMode, DroneController.MissionStatus missionStatus, final int duration) {
        if (spinnerIndex != FRAGMENTTYPE_PLANNING) {
            return;
        }

        if (droneMissionState != missionStatus) {
            droneMissionState = missionStatus;
            switch (droneMissionState) {
                case START:
                    if (saveFileRunnable != null) {
                        return;
                    }
                    // tts to start
                    if (ttsSpeaker != null) {
                        ttsSpeaker.speak("Mission Plan Start!");
                    }
                    // save flight history log
                    if (!isTapAndGo) {
                        saveFlag = true;
                        createHistory();
                    }
                    break;
                case PAUSE:
                    saveFlag = false;
                    if (ttsSpeaker != null) {
                        ttsSpeaker.speak("Mission Plan Pause!");
                    }
                    break;
                case FINISHED:
                    saveFlag = false;
                    if (ttsSpeaker != null) {
                        ttsSpeaker.speak("Mission Plan finish!");
                    }
                    if (saveFileRunnable != null) {
                        handler.removeCallbacks(saveFileRunnable);
                        saveFileRunnable = null;
                    }
                    break;
            }
        }

        // Drone Mode
        if (currentDroneMode != droneMode) {
            currentDroneMode = droneMode;
            String ttsStr = null;
            if (ttsSpeaker != null) {
                switch (currentDroneMode) {
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
                if (ttsStr != null) {
                    ttsSpeaker.speak(ttsStr + " Mode");
                }
            }
        }
        if (currentFragment != null) {
            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentFragment.setMavInfoFlightTime(duration);
                }
            });
        }
    }

    private void createHistory() {
        // save flight history log
        List<Mission> missionList = gson.fromJson(sharedPreferences.getString(AppConfig.PREF_MISSION_LIST, null), new TypeToken<List<Mission>>() {
        }.getType());

        flightHistory = ((MainActivity) getActivity()).createFlightHistory(missionList);
        saveFileRunnable = new Runnable() {
            @Override
            public void run() {
                if (saveFlag) {
                    recordItemBuilder.setCurrentTimeStamp(System.currentTimeMillis());
                    flightHistory.addRecord(recordItemBuilder.create());
                }
                handler.postDelayed(saveFileRunnable, saveDelayTime);
            }
        };
        handler.post(saveFileRunnable);
    }

    public List<Mission> getMissionList() {
        return currentMissionList;
    }
    // End Drone.StatusChangedListener

    public class javascriptInterface {
        Context mContext;

        javascriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void mapPointToAndroid(final float lat, final float lng) {
            if (isTapAndGo) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int tapGo_Altitude = 8;
                        ((TapAndGoFragment) currentFragment).showTapAndGoDialogFragment(tapGo_Altitude, lat, lng);
                    }
                });
            } else {
                if (!canMapAddMarker) {
                    return;
                }

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float altitude = 8;
                        ((PlanningFragment) currentFragment).missionAdapterAddData(lat, lng, altitude, 0, true, 0, Type.WAY_POINT);
                        writeMissionsToMap(
                                ((PlanningFragment) currentFragment).missionAdapterGetList()
                        );
                    }
                });
            }
        }

        @JavascriptInterface
        public void setPolylineLengthText(final int lengthInMeters) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((HistoryFragment) currentFragment).setFlightDistance(lengthInMeters);
                }
            });
        }

        @JavascriptInterface
        public void markerUpdateLocation(final int index, final float lat, final float lng) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((PlanningFragment) currentFragment).setItemMissionLocation(index, lat, lng);
                }
            });
        }

        @JavascriptInterface
        public void hideDetailFragment() {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((PlanningFragment) currentFragment).missionAdapterUnselect();
                }
            });
        }
    }

    private void setUpTopBarButton(View view) {
        setUpEditMarkerLayout(view);
        final Button backToMainButton = (Button) view.findViewById(R.id.button_back_to_main);
        backToMainButton.setOnClickListener(this);

        spinnerView = (Spinner) view.findViewById(R.id.mission_plan_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.mission_plan_menu, R.layout.spinner_style);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        spinnerView.setAdapter(spinnerAdapter);
        spinnerView.setSelection(spinnerIndex);
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case FRAGMENTTYPE_PLANNING:
                        if (isSwitchFromHistoryFile) {
                            radioGroup_multi_or_single.check(R.id.btn_action_multi_point);
                            setFragmentTransaction(FRAGMENTTYPE_ACTIVATE);
                        } else {
                            radioGroup_multi_or_single.check(R.id.btn_action_tap_and_go);
                            setFragmentTransaction(FRAGMENTTYPE_TAPANDGO);
                        }
                        break;
                    case FRAGMENTTYPE_HISTORY:
                        setFragmentTransaction(FRAGMENTTYPE_HISTORY);
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

        b_action_plan_undo = (Button) view.findViewById(R.id.btn_action_plan_undo);
        b_action_plan_undo.setOnClickListener(this);

        b_action_plan_delete = (Button) view.findViewById(R.id.btn_action_plan_delete);
        b_action_plan_delete.setOnClickListener(this);

        final Button b_delete_done = (Button) view.findViewById(R.id.btn_delete_done);
        b_delete_done.setOnClickListener(this);

        final Button b_delete_all = (Button) view.findViewById(R.id.btn_delete_all);
        b_delete_all.setOnClickListener(this);

        radioGroup_multi_or_single = (RadioGroup) view.findViewById(R.id.radiogroup_multi_or_single);
        radioGroup_multi_or_single.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_action_multi_point:
                        if (isSwitchFromHistoryFile) {
                            setFragmentTransaction(FRAGMENTTYPE_ACTIVATE);
                        } else {
                            setFragmentTransaction(FRAGMENTTYPE_PLANNING);
                        }
                        break;
                    case R.id.btn_action_tap_and_go: // Tap & GO
                        setFragmentTransaction(FRAGMENTTYPE_TAPANDGO);
                        break;
                }
            }
        });
    }

    private void setFragmentTransaction(int fragmentType) {
        if (pre_fragmentType == fragmentType) {
            return;
        }
        pre_fragmentType = fragmentType;
        switch (fragmentType) {
            case FRAGMENTTYPE_ACTIVATE:
                canMapAddMarker = true;
                isTapAndGo = false;
                isSwitchFromHistoryFile = true;
                if (getDroneController() != null) {
                    getDroneController().stopTapAndGo();
                }
                currentFragment = PlanningFragment.newInstance(isSwitchFromHistoryFile);
                layout_editMarker.setVisibility(View.VISIBLE);
                b_action_plan_undo.setVisibility(View.VISIBLE);
                b_action_plan_delete.setVisibility(View.VISIBLE);
                break;
            case FRAGMENTTYPE_PLANNING:
                canMapAddMarker = true;
                isTapAndGo = false;
                isSwitchFromHistoryFile = false;
                if (getDroneController() != null) {
                    getDroneController().stopTapAndGo();
                }
                currentFragment = PlanningFragment.newInstance(isSwitchFromHistoryFile);
                layout_editMarker.setVisibility(View.VISIBLE);
                b_action_plan_undo.setVisibility(View.VISIBLE);
                b_action_plan_delete.setVisibility(View.VISIBLE);
                break;
            case FRAGMENTTYPE_HISTORY:
                canMapAddMarker = false;
                isTapAndGo = false;
                isSwitchFromHistoryFile = false;
                if (getDroneController() != null) {
                    getDroneController().stopTapAndGo();
                }
                currentFragment = new HistoryFragment();
                layout_editMarker.setVisibility(View.GONE);
                setDeleteOptionShow(false);
                break;
            case FRAGMENTTYPE_TAPANDGO:
                canMapAddMarker = true;
                isTapAndGo = true;
                isSwitchFromHistoryFile = false;
                if (getDroneController() != null) {
                    getDroneController().startTapAndGo();
                }
                currentFragment = new TapAndGoFragment();
                layout_editMarker.setVisibility(View.VISIBLE);
                b_action_plan_undo.setVisibility(View.INVISIBLE);
                b_action_plan_delete.setVisibility(View.INVISIBLE);
                break;
        }

        FragmentTransaction fragmentTransaction = fragmentChildManager.beginTransaction();
        fragmentTransaction.replace(R.id.mission_plan_container, currentFragment, null).commit();

        webview_Map.loadUrl("javascript:mapClean()");
        webview_Map.loadUrl("javascript:setMapClickable(" + canMapAddMarker + ")");
        webview_Map.loadUrl("javascript:setTapGoMode(" + isTapAndGo + ")");
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
                        fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, WaypointEditorFragment.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
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
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, this);
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
                getFragmentManager().popBackStack();
                break;
            case R.id.btn_action_plan_undo:
                if (getDroneController() != null) {
                    getDroneController().readMissions(WaypointEditorFragment.this);
                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                }
                break;
            case R.id.btn_delete_all:
                webview_Map.loadUrl("javascript:clearMissionPlanningMarkers()");
                ((PlanningFragment) currentFragment).missionAdapterShowDelete(false);
                ((PlanningFragment) currentFragment).missionAdapterClearData();
                setDeleteOptionShow(false);
                canMapAddMarker = true;
                break;
            case R.id.btn_action_plan_delete:
                setDeleteOptionShow(true);
                ((PlanningFragment) currentFragment).missionAdapterShowDelete(true);
                canMapAddMarker = false;
                break;
            case R.id.btn_delete_done:
                setDeleteOptionShow(false);
                ((PlanningFragment) currentFragment).missionAdapterShowDelete(false);
                canMapAddMarker = true;
                break;
        }
    }

    private DroneController getDroneController() {
        return ((MainActivity) getActivity()).getDroneController();
    }

    private void setDeleteOptionShow(boolean isShow) {
        if (isShow) {
            layout_deleteOption.setVisibility(View.VISIBLE);
            layout_deleteIcon.setVisibility(View.INVISIBLE);
            b_action_plan_undo.setVisibility(View.GONE);
        } else {
            layout_deleteOption.setVisibility(View.INVISIBLE);
            layout_deleteIcon.setVisibility(View.VISIBLE);
            b_action_plan_undo.setVisibility(View.VISIBLE);
        }
    }

    // Implement FollowMeFragment.FollowMeInterface
    @Override
    public void writeMissionsToMap(List<Mission> missions) {
        webview_Map.loadUrl("javascript:clearMissionPlanningMarkers()");
        for (Mission mission : missions) {
            int sn = missions.indexOf(mission) + 1;
            webview_Map.loadUrl("javascript:addMissionMarker(" + mission.getLatitude() + "," + mission.getLongitude() + "," + sn + ")");
        }
    }

    @Override
    public void setMapToMyLocation() {
        webview_Map.loadUrl("javascript:setMapToMyLocation()");
    }

    @Override
    public void setMapToDrone() {
        webview_Map.loadUrl("javascript:setMapTo(" + droneLat + "," + droneLng + ")");
    }

    @Override
    public void fitMapShowAllMission() {
        webview_Map.loadUrl("javascript:fitMapShowAllMissionPlanning()");
    }

    @Override
    public void fitMapShowDroneAndMe() {
        webview_Map.loadUrl("javascript:fitMapShowDroneAndMe()");
    }
    // End FollowMeFragment.FollowMeInterface

    @Override
    public void changeMapType() {
        webview_Map.loadUrl("javascript:changeMapType()");
    }

    @Override
    public void setTapGoPath() {
        webview_Map.loadUrl("javascript:setTapGoPath()");
    }

    @Override
    public void clearTapMarker() {
        webview_Map.loadUrl("javascript:clearTapMarker()");
    }

    // Implement HistoryFragment.HistoryInterface
    @Override
    public void loadHistory(List<Float> markers, List<Long> path) {
        JSONArray markerJSON = new JSONArray(markers);
        JSONArray pathJson = new JSONArray(path);
        webview_Map.loadUrl("javascript:loadHistory(" + markerJSON + "," + pathJson + ")");
    }

    @Override
    public void clearHistoryMarkerPath() {
        webview_Map.loadUrl("javascript:clearHistoryMarkerPath()");
    }

    @Override
    public void spinnerSetToPlanning(List<Mission> missionList, boolean isHistory) {
        currentMissionList = missionList;
        isSwitchFromHistoryFile = isHistory;
        spinnerView.setSelection(FRAGMENTTYPE_PLANNING);
    }
    // End HistoryFragment.HistoryInterface

}