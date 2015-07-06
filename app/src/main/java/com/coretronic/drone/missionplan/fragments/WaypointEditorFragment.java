package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.Mission;
import com.coretronic.drone.Mission.Builder;
import com.coretronic.drone.Mission.Type;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.ui.StatusView;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WaypointEditorFragment extends Fragment
        implements View.OnClickListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        Drone.StatusChangedListener, DroneController.MissionLoaderListener, DroneController.FollowMeStatueListener {
    private static final String TAG = WaypointEditorFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private static MissionItemListAdapter mMissionItemAdapter;
    private WebView webview_WayPoint;
    private TextView tv_droneAltitude, tv_droneSpeed, tv_droneLatLng;
    private LinearLayout layout_start_follow, layout_mavinfo, layout_editMarker, layout_deleteIcon, layout_deleteOption, layout_planControl, layout_buttomBar;
    private FrameLayout layout_waypointDetail, layout_followMe;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest mLocationRequestHighAccuracy;
    private Location nowlocation;
    final static long LOCATION_UPDATE_MIN_TIME = 1000;
    final static int REQUEST_CHECK_SETTINGS = 1000;
    public double nowLatget, nowLngget, droneLat = 0, droneLng = 0;
    public int droneHeading;
    public DroneController drone;
    public boolean isGO, canMapAddMarker, isShowMarker;
    private ProgressDialog progressDialog;
    private FragmentActivity fragmentActivity;
    private StatusView statusView;
    private FragmentManager fragmentChildManager = null;
    private WaypointDetailFragment detailFragment = null;
    private Button btn_stop_follow;
    private AbstractWheel followMeAltitudeWheel;
    private int mfollowMeAltitude = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocationService();
        fragmentActivity = getActivity();
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

        fragmentActivity = getActivity();
        fragmentChildManager = getChildFragmentManager();

        setUpWebView(view);
        setUpWaypointListAndDetail(view);
        setUpButtomBarButton(view);
        setUpTopBarButton(view);
        setUpMavInfo(view);
        statusView = (StatusView) view.findViewById(R.id.status);

        setUpFollowMe(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) fragmentActivity).registerDroneStatusChangedListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //
    // implement GoogleApiClient.ConnectionCallbacks Method
    //
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

    //
    // implement GoogleApiClient.OnConnectionFailedListener Mehtod
    //
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    //
    // implement LocationListener Method
    //
    @Override
    public void onLocationChanged(Location location) {
        nowlocation = location;
        nowLatget = nowlocation.getLatitude();
        nowLngget = nowlocation.getLongitude();
    }

    //
    // implement MissionLoaderListener Method
    //
    @Override
    public void onLoadCompleted(final List<Mission> missions) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (missions == null || missions.size() == 0) {
                    Toast.makeText(getActivity(), "There is no mission existed", Toast.LENGTH_LONG).show();
//                    mMissionItemAdapter.update(new ArrayList<Mission>());
                } else {
                    if (isGO) {
                        drone.startMission();
                        progressDialog.dismiss();
                    } else {
                        missions.remove(0);
                        mMissionItemAdapter.update(missions);
                        mMissionItemAdapter.notifyDataSetChanged();
                        writeMissionsToMap(missions);
                        progressDialog.dismiss();
                    }
                }
            }
        });
    }

    private void setUpWebView(View view) {
        webview_WayPoint = (WebView) view.findViewById(R.id.waypoint_webview);
        //設定允許 JavaScript 呼叫的對應名稱
        webview_WayPoint.addJavascriptInterface(new javascriptInterface(getActivity()), "AndroidFunction");
        //啟用 WebView 的 JavaScript 執行功能
        webview_WayPoint.getSettings().setJavaScriptEnabled(true);
        //設定 WebView開啟定位功能
        // permission to disclose the user's location to JavaScript.
        webview_WayPoint.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webview_WayPoint.getSettings().setGeolocationEnabled(true);
        webview_WayPoint.loadUrl("file:///android_asset/GoogleMap.html");
        canMapAddMarker = true;
        isShowMarker = true;

    }

    //
    // implement Drone.StatusChangedListener Method
    //
    @Override
    public void onBatteryUpdate(final int battery) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setBatteryStatus(battery);
            }
        });
    }

    @Override
    public void onAltitudeUpdate(final float altitude) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tx_alt = String.format("%d", (int) altitude);
                tv_droneAltitude.setText(tx_alt + "m");
            }
        });
    }

    @Override
    public void onRadioSignalUpdate(int rssi) {

    }

    @Override
    public void onSpeedUpdate(final float groundSpeed) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_droneSpeed.setText(groundSpeed + " km/h");
            }
        });
    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        droneLat = (double) (lat * Math.pow(10, -7));
        droneLng = (double) (lon * Math.pow(10, -7));
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_droneLatLng.setText(String.valueOf(droneLat) + ", " + String.valueOf(droneLng));
                webview_WayPoint.loadUrl("javascript:updateDroneLocation(" + droneLat + "," + droneLng + "," + droneHeading + ")");
            }
        });
    }

    @Override
    public void onHeadingUpdate(final int heading) {
        droneHeading = heading;
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webview_WayPoint.loadUrl("javascript:updateDroneLocation(" + droneLat + "," + droneLng + "," + droneHeading + ")");
            }
        });
    }

    //
    // implement DroneController.FollowMeStatueListener Method
    //
    @Override
    public void onStart(float latOffset, float longOffset) {

    }

    @Override
    public void onMissionItemUpdated(float lat, float lon) {

    }

    @Override
    public void onLocationUpdated(float lat, float lon) {

    }

    public void setUpFollowMe(View view) {
        layout_followMe = (FrameLayout)view.findViewById(R.id.layout_follow_me);
        layout_followMe.setVisibility(FrameLayout.GONE);

        layout_start_follow = (LinearLayout)view.findViewById(R.id.layout_start_follow);

        followMeAltitudeWheel= (AbstractWheel)view.findViewById(R.id.follow_me_altitude_wheel);
        followMeAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number, 0, 20, "%02d"));
        followMeAltitudeWheel.setCyclic(false);
        followMeAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mfollowMeAltitude = newValue;
            }
        });

        final RelativeLayout btn_start_follow = (RelativeLayout)view.findViewById(R.id.btn_start_follow);
        btn_start_follow.setOnClickListener(this);

        btn_stop_follow = (Button)view.findViewById(R.id.btn_stop_follow);
        btn_stop_follow.setOnClickListener(this);
        btn_stop_follow.setVisibility(Button.GONE);
    }


    /**
     * 建立給 JavaScript 呼叫的函式 *
     */
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
        public void addWaypointToList(float lat, float lng) {
            if (!canMapAddMarker) {
                return;
            }

            float altitude = 8;
            mMissionItemAdapter.add(createNewMission(lat, lng, altitude, 0, true, 0, Type.WAY_POINT));

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMissionItemAdapter.notifyDataSetChanged();
                    writeMissionsToMap(mMissionItemAdapter.getMissionList());
                }
            });
        }
    }

    private void setUpWaypointListAndDetail(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
        //如果可以確定每個item的高度是固定的，設置這個選項可以提高性能
        recyclerView.setHasFixedSize(true);
        recyclerView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.recyclerview_item_width);
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

        mMissionItemAdapter = new MissionItemListAdapter();
        recyclerView.setAdapter(mMissionItemAdapter);

        layout_waypointDetail = (FrameLayout) view.findViewById(R.id.waypoint_detail_container);

        layout_waypointDetail.setVisibility(FrameLayout.GONE);

        mMissionItemAdapter.SetOnItemClickListener(new MissionItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemDeleteClick(View v, int position) {
//                webview_WayPoint.loadUrl("javascript:deleteSelectMarker(" + position + ")");
                mMissionItemAdapter.remove(position);
                mMissionItemAdapter.notifyDataSetChanged();
                writeMissionsToMap(mMissionItemAdapter.getMissionList());
            }

            @Override
            public void onItemPlanClick(View view, int position) {
                if (mMissionItemAdapter.getFocusIndex() < 0 || position != mMissionItemAdapter.getFocusIndex()) {
                    Mission itemMission = mMissionItemAdapter.getMission(position);
                    FragmentTransaction fragmentTransaction = fragmentChildManager.beginTransaction();
                    detailFragment = WaypointDetailFragment.newInstance(position + 1, itemMission);
                    fragmentTransaction
                            .replace(R.id.waypoint_detail_container, detailFragment, "DetailFragment")
                            .commit();
                    layout_waypointDetail.setVisibility(FrameLayout.VISIBLE);
                }else{
                    layout_waypointDetail.setVisibility(FrameLayout.GONE);
                }
            }
        });
    }

    public static void setItemMissionType(Type missionType){
        mMissionItemAdapter.getMission(mMissionItemAdapter.getFocusIndex()).setType(missionType);
    }
    public static void setItemMissionAltitude(float missionAltidude){
        mMissionItemAdapter.getMission(mMissionItemAdapter.getFocusIndex()).setAltitude(missionAltidude);
    }
    public static void setItemMissionDelay(int missionDelay){
        mMissionItemAdapter.getMission(mMissionItemAdapter.getFocusIndex()).setWaitSeconds(missionDelay);
    }

    private void setUpButtomBarButton(View view) {
        layout_buttomBar = (LinearLayout) view.findViewById(R.id.bottom_bar);

        final Button myLocationButton = (Button) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(this);

        layout_planControl = (LinearLayout) view.findViewById(R.id.layout_plan_control);

        final Button goButton = (Button) view.findViewById(R.id.btn_plan_go);
        goButton.setOnClickListener(this);
        final Button stopButton = (Button) view.findViewById(R.id.btn_plan_stop);
        stopButton.setOnClickListener(this);

        final Button droneLocationButton = (Button) view.findViewById(R.id.button_drone_location);
        droneLocationButton.setOnClickListener(this);

        final Button fitMapButton = (Button) view.findViewById(R.id.button_fit_map);
        fitMapButton.setOnClickListener(this);
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
                switch (position) {
                    case 0: // PLANNING
                        canMapAddMarker = true;
                        isShowMarker = true;
                        layout_editMarker.setVisibility(LinearLayout.VISIBLE);
                        recyclerView.setVisibility(RecyclerView.VISIBLE);
                        layout_buttomBar.setVisibility(LinearLayout.VISIBLE);
                        layout_planControl.setVisibility(LinearLayout.VISIBLE);
                        layout_mavinfo.setVisibility(LinearLayout.VISIBLE);
                        layout_followMe.setVisibility(FrameLayout.GONE);
                        break;
                    case 1: // FOLLOW ME
                        canMapAddMarker = false;
                        isShowMarker = false;
                        layout_editMarker.setVisibility(LinearLayout.GONE);
                        recyclerView.setVisibility(RecyclerView.GONE);
                        layout_buttomBar.setVisibility(LinearLayout.VISIBLE);
                        layout_planControl.setVisibility(LinearLayout.GONE);
                        layout_mavinfo.setVisibility(LinearLayout.VISIBLE);
                        layout_followMe.setVisibility(FrameLayout.VISIBLE);
                        break;
                    case 2: // FLIGHT HISTORY
                        canMapAddMarker = false;
                        isShowMarker = false;
                        layout_editMarker.setVisibility(LinearLayout.GONE);
                        recyclerView.setVisibility(RecyclerView.GONE);
                        layout_buttomBar.setVisibility(LinearLayout.GONE);
                        layout_mavinfo.setVisibility(LinearLayout.GONE);
                        layout_followMe.setVisibility(FrameLayout.GONE);
                        break;
                    default:
                        break;
                }
                setDeleteOptionShow(false);
                webview_WayPoint.loadUrl("javascript:setMapClickable(" + canMapAddMarker + ")");
                webview_WayPoint.loadUrl("javascript:setMarkerShow(" + isShowMarker + ")");
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

        final Button b_action_plan_undo = (Button)view.findViewById(R.id.btn_action_plan_undo);
        b_action_plan_undo.setOnClickListener(this);

        final Button b_action_plan_delete = (Button) view.findViewById(R.id.btn_action_plan_delete);
        b_action_plan_delete.setOnClickListener(this);

        final Button b_delete_done = (Button) view.findViewById(R.id.btn_delete_done);
        b_delete_done.setOnClickListener(this);

        final Button b_delete_all = (Button) view.findViewById(R.id.btn_delete_all);
        b_delete_all.setOnClickListener(this);
    }

    private void setUpMavInfo(View view) {
        layout_mavinfo = (LinearLayout)view.findViewById(R.id.mav_info);
        tv_droneAltitude = (TextView)view.findViewById(R.id.altitude_text);
        tv_droneAltitude.setText("0m");
        tv_droneSpeed = (TextView) view.findViewById(R.id.speed_text);
        tv_droneSpeed.setText("0 km/h");
        tv_droneLatLng = (TextView) view.findViewById(R.id.location_text);
        tv_droneLatLng.setText(String.valueOf(droneLat) + ", " + String.valueOf(droneLng));
    }

    private void setUpLocationService() {
        mLocationRequestHighAccuracy = LocationRequest.create();
        // 精準度
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // 更新時間
        mLocationRequestHighAccuracy.setInterval(LOCATION_UPDATE_MIN_TIME);
        // 最快更新頻率(上限)
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
        drone = ((MainActivity) getActivity()).getDroneController();
        if (drone == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.button_back_to_main:
                getFragmentManager().popBackStack();
                break;
            case R.id.btn_action_plan_undo:
                drone.readMissions(WaypointEditorFragment.this);
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                isGO = false;
                break;
            case R.id.btn_delete_all:
                webview_WayPoint.loadUrl("javascript:clearMarkers()");
                mMissionItemAdapter.clearMission();
                mMissionItemAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_action_plan_delete:
                setDeleteOptionShow(true);
                canMapAddMarker = false;
                break;
            case R.id.btn_delete_done:
                setDeleteOptionShow(false);
                canMapAddMarker = true;
                break;
            case R.id.btn_plan_go:
                List<Mission> droneMissionList = mMissionItemAdapter.getMissionList();
                droneMissionList.add(0, createNewMission(0, 0, 0, 0, false, 0, Type.WAY_POINT));
                drone.writeMissions(droneMissionList, WaypointEditorFragment.this);
                progressDialog.setTitle("Sending");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                isGO = true;
                break;
            case R.id.btn_plan_stop:
                break;
            case R.id.button_my_location:
                webview_WayPoint.loadUrl("javascript:setMapToMyLocation()");
                break;
            case R.id.button_drone_location:
                webview_WayPoint.loadUrl("javascript:setMapTo(" + droneLat + "," + droneLng + ")");
                break;
            case R.id.button_fit_map:
                if (mMissionItemAdapter.getItemCount() > 0) {
                    webview_WayPoint.loadUrl("javascript:fitMapShowAll()");
                }
                break;
            case R.id.btn_start_follow:
                drone.startFollowMe(mfollowMeAltitude, WaypointEditorFragment.this);
                layout_start_follow.setVisibility(LinearLayout.GONE);
                btn_stop_follow.setVisibility(Button.VISIBLE);
                break;
            case R.id.btn_stop_follow:
                drone.startFollowMe(-1, WaypointEditorFragment.this);
                layout_start_follow.setVisibility(LinearLayout.VISIBLE);
                btn_stop_follow.setVisibility(Button.GONE);
                break;
        }
    }

    private void setDeleteOptionShow(boolean isShow) {
        if (isShow) {
            layout_deleteOption.setVisibility(LinearLayout.VISIBLE);
            layout_deleteIcon.setVisibility(LinearLayout.INVISIBLE);
            recyclerView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.recyclerview_deleteitem_width);
            mMissionItemAdapter.setDeleteLayoutVisible(true);
        } else {
            layout_deleteOption.setVisibility(LinearLayout.INVISIBLE);
            layout_deleteIcon.setVisibility(LinearLayout.VISIBLE);
            recyclerView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.recyclerview_item_width);
            mMissionItemAdapter.setDeleteLayoutVisible(false);
        }
        layout_waypointDetail.setVisibility(FrameLayout.GONE);
        mMissionItemAdapter.notifyDataSetChanged();
    }

    private Mission createNewMission(float latitude, float longitude, float altitude,
                                     int waitSeconds, boolean autoContinue, int radius, Type type) {
        Mission.Builder builder = new Builder();

        builder.setLatitude(latitude);
        builder.setLongitude(longitude);
        builder.setAltitude(altitude);
        builder.setWaitSeconds(waitSeconds);
        builder.setAutoContinue(autoContinue);
        builder.setRadius(radius);
        builder.setType(type);

        return builder.create();
    }

    private void writeMissionsToMap(List<Mission> missions) {
        webview_WayPoint.loadUrl("javascript:clearMarkers()");
        for (Mission mission : missions) {
            int sn = missions.indexOf(mission) + 1;
            webview_WayPoint.loadUrl("javascript:addMarker(" + mission.getLatitude() + "," + mission.getLongitude() + "," + sn + ")");
        }
    }
}