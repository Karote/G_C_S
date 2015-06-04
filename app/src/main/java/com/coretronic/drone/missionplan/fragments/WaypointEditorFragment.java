package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.Drone.MissionLoaderListener;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.Mission;
import com.coretronic.drone.Mission.Builder;
import com.coretronic.drone.Mission.Type;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.adapter.MissionItemListAdapter;
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
        implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, MissionLoaderListener {
    private static final String TAG=WaypointEditorFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private static MissionItemListAdapter mMissionItemAdapter;
    private WebView webview_WayPoint;
    private LinearLayout deleteIconLayout, deleteOptionLayout;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest mLocationRequestHighAccuracy;
    private Location nowlocation;
    final static long LOCATION_UPDATE_MIN_TIME = 1000;
    final static int REQUEST_CHECK_SETTINGS = 1000;
    public double nowLatget, nowLngget;
    public Drone drone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLocationService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waypoint, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpWebView(view);
        setUpWaypointListRecycleView(view);
        setUpButtomBarButton(view);
        setUpTopBarButton(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
//                    mMissionItemAdapter.update(missions);
                    drone.startMission();
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
            float altitude = 10;
//            altitude += mMissionItemAdapter.getItemCount();
            mMissionItemAdapter.add(createNewMission(lat, lng, altitude, 0, true, 0, Type.WAY_POINT));

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMissionItemAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setUpWaypointListRecycleView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
        //如果可以確定每個item的高度是固定的，設置這個選項可以提高性能
        recyclerView.setHasFixedSize(true);
        recyclerView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.recyclerview_item_width);
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

        mMissionItemAdapter = new MissionItemListAdapter();
        recyclerView.setAdapter(mMissionItemAdapter);

        mMissionItemAdapter.SetOnItemClickListener(new MissionItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemDeleteClick(View v, int position) {
                webview_WayPoint.loadUrl("javascript:deleteSelectMarker(" + position + ")");
            }

            @Override
            public void onItemPlanClick(View view, int position) {
                Log.d(TAG, "onItemPlanClick");
            }
        });
    }

    private void setUpButtomBarButton(View view) {
        final ImageButton myLocationButton = (ImageButton) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(this);

        final Button goButton = (Button)view.findViewById(R.id.button_go);
        goButton.setOnClickListener(this);
    }

    private void setUpTopBarButton(View view){
        setUpDeleteLayout(view);
        final Button backToMainButton = (Button)view.findViewById(R.id.button_back_to_main);
        backToMainButton.setOnClickListener(this);
    }

    private void setUpDeleteLayout(View view) {
        deleteIconLayout = (LinearLayout)view.findViewById(R.id.delete_icon_layout);
        deleteOptionLayout = (LinearLayout)view.findViewById(R.id.delete_option_layout);
        deleteOptionLayout.setVisibility(LinearLayout.INVISIBLE);

        final Button b_deleteIcon = (Button) view.findViewById(R.id.button_delete_marker);
        b_deleteIcon.setOnClickListener(this);

        final Button b_deleteDone = (Button) view.findViewById(R.id.button_delete_done);
        b_deleteDone.setOnClickListener(this);

        final Button b_deleteAll = (Button) view.findViewById(R.id.button_delete_all);
        b_deleteAll.setOnClickListener(this);
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
        switch (v.getId()) {
            case R.id.button_back_to_main:
                getFragmentManager().popBackStack();
                break;
            case R.id.button_delete_all:
                webview_WayPoint.loadUrl("javascript:clearMarkers()");
                mMissionItemAdapter.clearMission();
                mMissionItemAdapter.notifyDataSetChanged();
                break;
            case R.id.button_delete_marker:
                deleteOptionLayout.setVisibility(LinearLayout.VISIBLE);
                deleteIconLayout.setVisibility(LinearLayout.INVISIBLE);
                recyclerView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.recyclerview_deleteitem_width);
                mMissionItemAdapter.isVisible = true;
                mMissionItemAdapter.notifyDataSetChanged();
                break;
            case R.id.button_delete_done:
                deleteOptionLayout.setVisibility(LinearLayout.INVISIBLE);
                deleteIconLayout.setVisibility(LinearLayout.VISIBLE);
                recyclerView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.recyclerview_item_width);
                mMissionItemAdapter.isVisible = false;
                mMissionItemAdapter.notifyDataSetChanged();
                break;
            case R.id.button_go:
                drone = ((MainActivity) getActivity()).getDroneController();
                if (drone == null) {
                    return;
                }
                MissionItemListAdapter droneMissionAdapter = new MissionItemListAdapter();
                droneMissionAdapter.update(mMissionItemAdapter.getMissionList());
                droneMissionAdapter.addAt(createNewMission(0, 0, 0, 0, false, 0, Type.WAY_POINT), 0);
                drone.writeMissions(droneMissionAdapter.getMissionList(), WaypointEditorFragment.this);
                break;
            case R.id.button_my_location:
                webview_WayPoint.loadUrl("javascript:setMapToMyLocation()");
                break;
        }
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


}