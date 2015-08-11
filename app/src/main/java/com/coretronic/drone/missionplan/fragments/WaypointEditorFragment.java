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
import android.util.Log;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.Mission;
import com.coretronic.drone.Mission.Type;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.fragments.module.DroneInfo;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.utility.AppConfig;
import com.coretronic.drone.utility.FileHelper;
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

import org.json.JSONArray;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WaypointEditorFragment extends Fragment
        implements View.OnClickListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        Drone.StatusChangedListener, DroneController.MissionLoaderListener,
        FollowMeFragment.OnFollowMeClickListener, HistoryFragment.HistoryAdapterListener {

    private static final String TAG = WaypointEditorFragment.class.getSimpleName();

    private StatusView statusView = null;
    private WebView webview_Map = null;

    private LinearLayout layout_editMarker = null;
    private LinearLayout layout_deleteIcon = null;
    private LinearLayout layout_deleteOption = null;

    private GoogleApiClient mGoogleApiClient = null;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest mLocationRequestHighAccuracy = null;
    private Location nowlocation = null;

    final static long LOCATION_UPDATE_MIN_TIME = 1000;
    final static int REQUEST_CHECK_SETTINGS = 1000;

    private double nowLatget, nowLngget;

    private long droneLat = 0, droneLng = 0;
    private int droneHeading = 0;

    public boolean canMapAddMarker, isShowMarker;

    private ProgressDialog progressDialog = null;
    private FragmentActivity fragmentActivity = null;
    private FragmentManager fragmentChildManager = null;
    private FragmentTransaction fragmentTransaction = null;

    private MavInfoFragment currentFragment = null;

    // Save Drone Info
    private DroneInfo currentDroneInfo;
    private DroneInfo tmpDroneInfo;
    // File Helper
    private FileHelper fileHelper;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Handler handler;
    private boolean saveFlag = false;
    private int saveDelayTime = 2000;
    private String saveFileName;
    private DroneController.MissionStatus droneMissionState = DroneController.MissionStatus.FINISHED;
    private DroneController.DroneMode currentDroneMode = null;

    // TTS
    private Speaker ttsSpeaker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocationService();

        fragmentActivity = getActivity();
        fragmentChildManager = getChildFragmentManager();

        // Drone info init
        currentDroneInfo = new DroneInfo();
        tmpDroneInfo = new DroneInfo();

        // File Helper init
        fileHelper = new FileHelper(getActivity());
        sharedPreferences = getActivity().getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, 0);
        gson = new Gson();
        handler = new Handler();
        // tts init
        ttsSpeaker = new Speaker(getActivity());

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        ((MainActivity) fragmentActivity).registerDroneStatusChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

        ((MainActivity) fragmentActivity).unregisterDroneStatusChangedListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ttsSpeaker != null) {
            ttsSpeaker.destroy();
        }
        saveFlag = false;
    }

    // Implement GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Location location = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);

        if (location != null && location.getTime() > 20000) {
            nowlocation = location;
            nowLatget = nowlocation.getLatitude();
            nowLngget = nowlocation.getLongitude();
        } else {
            fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequestHighAccuracy, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, WaypointEditorFragment.this);
                }
            }, 60000, TimeUnit.MILLISECONDS);
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
                if (missions == null || missions.size() == 0) {
                    Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
                } else {
                    ((PlanningFragment) currentFragment).missionAdapterSetData(missions);
                    writeMissionsToMap(missions);
                    progressDialog.dismiss();
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
        canMapAddMarker = true;
        isShowMarker = true;
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
        currentDroneInfo.setBatter(battery);
    }

    @Override
    public void onAltitudeUpdate(final float altitude) {
        if (currentFragment == null)
            return;

        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentFragment.setMavInfoAltitude(altitude);
            }
        });
        // save info
        currentDroneInfo.setAltitude(altitude);
    }

    @Override
    public void onRadioSignalUpdate(int rssi) {
        // save info
        currentDroneInfo.setRssi(rssi);
    }

    @Override
    public void onSpeedUpdate(final float groundSpeed) {
        if (currentFragment == null)
            return;

        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentFragment.setMavInfoSpeed(groundSpeed);
            }
        });
        // save info
        currentDroneInfo.setGroundSpeed(groundSpeed);
    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        if (currentFragment == null)
            return;

        droneLat = lat;
        droneLng = lon;
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentFragment.setMavInfoLocation(droneLat, droneLng);
                webview_Map.loadUrl("javascript:updateDroneLocation(" + droneLat + "," + droneLng + "," + droneHeading + ")");
                // GPS status
                statusView.setGpsVisibility(((MainActivity)getActivity()).hasGPSSignal(eph) ? View.VISIBLE : View.GONE);
            }
        });
        // save info
        currentDroneInfo.setLat(lat);
        currentDroneInfo.setLon(lon);
        currentDroneInfo.setEph(eph);
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
        currentDroneInfo.setHeading(heading);
    }

    @Override
    public void onDroneStateUpdate(DroneController.DroneMode droneMode, DroneController.MissionStatus missionStatus, int duration) {
        Log.d("morris", "droneMissionState:" + droneMissionState + "/" + "missionState:" + missionStatus);
        if (droneMissionState != missionStatus) {

            droneMissionState = missionStatus;

            switch (droneMissionState) {
                case START:
                    // tts to start
                    if (ttsSpeaker != null) {
                        ttsSpeaker.speak("Mission Plan Start!");
                    }
                    saveFlag = true;
                    handler.post(saveFileRunnable);
                    break;
                case PAUSE:
                    if (ttsSpeaker != null) {
                        ttsSpeaker.speak("Mission Plan Pause!");
                    }
                    saveFlag = false;
                    break;
                case FINISHED:
                    if (ttsSpeaker != null) {
                        ttsSpeaker.speak("Mission Plan Stop!");
                    }
                    saveFlag = false;
                    break;
            }
        }

        // Drone Mode
        Log.d("morris", "currentMode:" + currentDroneMode + "/" + "droneMode:" + droneMode);

        if(currentDroneMode != droneMode) {
            currentDroneMode = droneMode;
            if (ttsSpeaker != null) {
                ttsSpeaker.speak(currentDroneMode.toString() + " Mode");
                ttsSpeaker.pause(1000);
            }
        }
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
        public void addWaypointToList(final float lat, final float lng) {
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

        @JavascriptInterface
        public void setPolylineLengthText(final int lengthInMeters) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((HistoryFragment) currentFragment).setFlightDistance(lengthInMeters);
                }
            });
        }
    }

    private void setUpTopBarButton(View view) {
        setUpEditMarkerLayout(view);
        final Button backToMainButton = (Button) view.findViewById(R.id.button_back_to_main);
        backToMainButton.setOnClickListener(this);

        Spinner spinnerView = (Spinner) view.findViewById(R.id.mission_plan_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.mission_plan_menu, R.layout.spinner_style);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        spinnerView.setAdapter(spinnerAdapter);
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fragmentTransaction = fragmentChildManager.beginTransaction();
                switch (position) {
                    case 0: // PLANNING
                        canMapAddMarker = true;
                        isShowMarker = true;
                        layout_editMarker.setVisibility(View.VISIBLE);
                        currentFragment = new PlanningFragment();
                        break;
                    case 1: // FLIGHT HISTORY
                        canMapAddMarker = false;
                        isShowMarker = false;
                        layout_editMarker.setVisibility(View.GONE);
                        currentFragment = new HistoryFragment();
                        break;
                    default:
                        break;
                }
                fragmentTransaction.replace(R.id.mission_plan_container, currentFragment, null).commit();
                setDeleteOptionShow(false);
                webview_Map.loadUrl("javascript:setMapClickable(" + canMapAddMarker + ")");
                webview_Map.loadUrl("javascript:clearMarkers()");
                ClearPath();
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

        final Button b_action_plan_undo = (Button) view.findViewById(R.id.btn_action_plan_undo);
        b_action_plan_undo.setOnClickListener(this);

        final Button b_action_plan_delete = (Button) view.findViewById(R.id.btn_action_plan_delete);
        b_action_plan_delete.setOnClickListener(this);

        final Button b_delete_done = (Button) view.findViewById(R.id.btn_delete_done);
        b_delete_done.setOnClickListener(this);

        final Button b_delete_all = (Button) view.findViewById(R.id.btn_delete_all);
        b_delete_all.setOnClickListener(this);
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
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
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
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
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
                webview_Map.loadUrl("javascript:clearMarkers()");
                ((PlanningFragment) currentFragment).missionAdapterClearData();
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
        } else {
            layout_deleteOption.setVisibility(View.INVISIBLE);
            layout_deleteIcon.setVisibility(View.VISIBLE);
        }
    }

    // Implement FollowMeFragment.OnFollowMeClickListener
    @Override
    public void writeMissionsToMap(List<Mission> missions) {
        webview_Map.loadUrl("javascript:clearMarkers()");
        for (Mission mission : missions) {
            int sn = missions.indexOf(mission) + 1;
            webview_Map.loadUrl("javascript:addMarker(" + mission.getLatitude() + "," + mission.getLongitude() + "," + sn + ")");
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
        webview_Map.loadUrl("javascript:fitMapShowAll()");
    }

    @Override
    public void fitMapShowDroneAndMe() {
        webview_Map.loadUrl("javascript:fitMapShowDroneAndMe()");
    }
    // End FollowMeFragment.OnFollowMeClickListener


    Runnable saveFileRunnable = new Runnable() {
        @Override
        public void run() {
            if (saveFlag) {
                saveFileName = sharedPreferences.getString(AppConfig.PREF_LOGFILE_NAME, null);
                Log.d("morris", "saveFileRunnable");
                if (saveFileName != null) {
                    tmpDroneInfo = currentDroneInfo;
                    tmpDroneInfo.setTimeStamp(System.currentTimeMillis());
                    Log.d("morris", "tmpDroneInfo: " + gson.toJson(tmpDroneInfo));
                    fileHelper.writeToFile(gson.toJson(tmpDroneInfo), saveFileName);
                    handler.postDelayed(saveFileRunnable, saveDelayTime);
                }
            }
        }
    };

    // Implement HistoryFragment.HistoryAdapterListener
    @Override
    public void LoadPathLog(List<Double> path) {
        JSONArray mJSONArray = new JSONArray(path);
        Log.d(TAG, "pathArray(Json):" + mJSONArray);
        webview_Map.loadUrl("javascript:LoadPathLog(" + mJSONArray + ")");
    }

    @Override
    public void ClearPath() {
        webview_Map.loadUrl("javascript:ClearPath()");
    }
    // End HistoryFragment.HistoryAdapterListener
}