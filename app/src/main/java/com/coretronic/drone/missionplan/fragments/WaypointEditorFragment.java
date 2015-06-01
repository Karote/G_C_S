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

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.Waypoint;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WaypointEditorFragment extends Fragment  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private RecyclerView recyclerView;
    private static List<Waypoint> waypointList;
    private static MissionItemListAdapter adapter;
    private WebView webview_WayPoint;
    private LinearLayout deleteIconLayout, deleteOptionLayout;
    private Button b_deleteIcon, b_deleteDone, b_deleteAll;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private LocationRequest mLocationRequestHighAccuracy;
    private Location nowlocation;
    final static long LOCATION_UPDATE_MIN_TIME = 1000;
    final static int REQUEST_CHECK_SETTINGS = 1000;
    public double nowLatget, nowLngget;

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
//        setUpWaypointListFragment(view);
        setUpWaypointListRecycleView(view);
        setUpButtomBarButton(view);
        setUpDeleteLayout(view);
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

    private void setUpWebView(View view) {
        webview_WayPoint = (WebView) view.findViewById(R.id.waypoint_webview);
        //設定允語 JavaScript 呼叫的對應名稱
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
        public void addWaypointToList(double lat, double lng) {

            double altitude = 10;
            altitude += waypointList.size();
            waypointList.add(new Waypoint(lat, lng, altitude));
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setUpButtomBarButton(View view) {
        ImageButton myLocationButton = (ImageButton) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview_WayPoint.loadUrl("javascript:setMapToMyLocation()");
            }
        });
    }

    private void setUpWaypointListRecycleView(View view) {
        waypointList = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);
//        recyclerView.setHasFixedSize(true);
        recyclerView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.recyclerview_item_width);
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(recyclerLayoutMgr);
        adapter = new MissionItemListAdapter(waypointList);
        recyclerView.setAdapter(adapter);
    }

    private void setUpDeleteLayout(View view) {
        deleteIconLayout = (LinearLayout) view.findViewById(R.id.delete_icon_layout);
        deleteOptionLayout = (LinearLayout) view.findViewById(R.id.delete_option_layout);
        deleteOptionLayout.setVisibility(LinearLayout.INVISIBLE);

        b_deleteIcon = (Button) view.findViewById(R.id.button_delete_marker);
        b_deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOptionLayout.setVisibility(LinearLayout.VISIBLE);
                deleteIconLayout.setVisibility(LinearLayout.INVISIBLE);
                recyclerView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.recyclerview_deleteitem_width);
                adapter.isVisible = true;
                adapter.notifyDataSetChanged();
            }
        });

        b_deleteDone = (Button) view.findViewById(R.id.button_delete_done);
        b_deleteDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOptionLayout.setVisibility(LinearLayout.INVISIBLE);
                deleteIconLayout.setVisibility(LinearLayout.VISIBLE);
                recyclerView.getLayoutParams().width = (int)getResources().getDimension(R.dimen.recyclerview_item_width);
                adapter.isVisible = false;
                adapter.notifyDataSetChanged();

            }
        });

        b_deleteAll = (Button) view.findViewById(R.id.button_delete_all);
        b_deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview_WayPoint.loadUrl("javascript:clearMarkers()");
                waypointList.clear();
                adapter.notifyDataSetChanged();
            }
        });
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
    } @Override
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
    public void onLocationChanged(Location location) {
        nowlocation = location;
        nowLatget = nowlocation.getLatitude();
        nowLngget = nowlocation.getLongitude();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}