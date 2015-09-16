package com.coretronic.drone.missionplan.map;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Poming on 2015/9/10.
 */
public class DroneMap implements OnMapEventCallback {

    private final Context mContext;
    private final Handler mHandler;
    private final WebView mMapWebView;
    private OnMapEventCallback mOnMapEventCallback;

    private boolean mIsTapAndGoMode = true;
    private boolean mCanAddMarket = true;

    @Override
    @JavascriptInterface
    public void onClick(final float lat, final float lon) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onClick(lat, lon);
            }
        });
    }

    @Override
    @JavascriptInterface
    public void onDragEnd(final int index, final float lat, final float lon) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onDragEnd(index, lat, lon);
            }
        });
    }

    @Override
    @JavascriptInterface
    public void onDragStart() {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onDragStart();
            }
        });
    }

    @Override
    @JavascriptInterface
    public void onPolylineLengthCalculated(final int lengthInMeters) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onPolylineLengthCalculated(lengthInMeters);
            }
        });
    }

    @JavascriptInterface
    public void onWarningMessage(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void onMapLoaded() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMapWebView.loadUrl("javascript:setMapClickable(" + mCanAddMarket + ")");
                mMapWebView.loadUrl("javascript:setTapGoMode(" + mIsTapAndGoMode + ")");
            }
        });
    }

    public DroneMap(Context context, View view, Handler handler) {
        mContext = context;
        mHandler = handler;

        mMapWebView = (WebView) view.findViewById(R.id.waypoint_webview);
        mMapWebView.addJavascriptInterface(this, "AndroidFunction");
        mMapWebView.getSettings().setJavaScriptEnabled(true);
        // permission to disclose the user's location to JavaScript.
        mMapWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        mMapWebView.getSettings().setGeolocationEnabled(true);
        mMapWebView.loadUrl("file:///android_asset/GoogleMap.html");

    }

    public void updateMissions(List<Mission> missions) {
        mMapWebView.loadUrl("javascript:clearMissionPlanningMarkers()");
        for (Mission mission : missions) {
            int sn = missions.indexOf(mission) + 1;
            mMapWebView.loadUrl("javascript:addMissionMarker(" + mission.getLatitude() + "," + mission.getLongitude() + "," + sn + ")");
        }
    }

    public void setMapToMyLocation() {
        mMapWebView.loadUrl("javascript:setMapToMyLocation()");
    }

    public void setMapToDroneLocation(long droneLat, long dronelon) {
        mMapWebView.loadUrl("javascript:setMapTo(" + droneLat + "," + dronelon + ")");
    }

    public void fitMapShowAllMission() {
        mMapWebView.loadUrl("javascript:fitMapShowAllMissionPlanning()");
    }

    public void fitMapShowDroneAndMe() {
        mMapWebView.loadUrl("javascript:fitMapShowDroneAndMe()");
    }

    public void changeMapType() {
        mMapWebView.loadUrl("javascript:changeMapType()");
    }

    public void setTapGoPath() {
        mMapWebView.loadUrl("javascript:setTapGoPath()");
    }

    public void clearTapMarker() {
        mMapWebView.loadUrl("javascript:clearTapMarker()");
    }

    public void loadHistory(List<Float> markers, List<Long> path) {
        JSONArray markerJSON = new JSONArray(markers);
        JSONArray pathJson = new JSONArray(path);
        mMapWebView.loadUrl("javascript:loadHistory(" + markerJSON + "," + pathJson + ")");
    }

    public void clearMissionPlanningMarkers() {

    }

    public void clearHistoryMarkerPath() {
        mMapWebView.loadUrl("javascript:clearHistoryMarkerPath()");
    }

    public void init(boolean isTapAndGo, boolean canMapAddMarker) {
        mMapWebView.loadUrl("javascript:mapClean()");
        mMapWebView.loadUrl("javascript:setMapClickable(" + canMapAddMarker + ")");
        mMapWebView.loadUrl("javascript:setTapGoMode(" + isTapAndGo + ")");
        mIsTapAndGoMode = isTapAndGo;
        mCanAddMarket = canMapAddMarker;
    }

    public DroneMap setOnMapEventListener(OnMapEventCallback onMapEventCallback) {
        mOnMapEventCallback = onMapEventCallback;
        return this;
    }

    public void clear() {
        mMapWebView.loadUrl("about:blank");
    }

    public void updateDroneLocation(long latitude, long longitude, float yaw) {
        mMapWebView.loadUrl("javascript:updateDroneLocation(" + latitude + "," + longitude + "," + yaw + ")");
    }

    public void setAddMarkerEnable(boolean isAddMarkerEnable) {
        mCanAddMarket = isAddMarkerEnable;
        mMapWebView.loadUrl("javascript:setMapClickable(" + isAddMarkerEnable + ")");
    }

}
