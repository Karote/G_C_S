package com.coretronic.drone.missionplan.map;

import android.content.Context;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.survey.SurveyRouter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.json.JSONArray;

import java.util.List;

/**
 * Created by Poming on 2015/9/10.
 */
public class DroneMap implements OnMapEventCallback {

    private final Context mContext;
    private final Handler mHandler;
    private final WebView mMapWebView;
    private final Gson mGson;
    private OnMapEventCallback mOnMapEventCallback;

    private boolean mIsTapAndGoMode = true;
    private boolean mCanAddMarket = true;

    @Override
    @JavascriptInterface
    public void onMapClickEvent(final float lat, final float lon) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onMapClickEvent(lat, lon);
            }
        });
    }

    @Override
    @JavascriptInterface
    public void onMapDragEndEvent(final int index, final float lat, final float lon) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onMapDragEndEvent(index, lat, lon);
            }
        });
    }

    @Override
    @JavascriptInterface
    public void onMapDragStartEvent() {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onMapDragStartEvent();
            }
        });
    }

    @Override
    @JavascriptInterface
    public void onMapPolylineLengthCalculated(final int lengthInMeters) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onMapPolylineLengthCalculated(lengthInMeters);
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
                init(mIsTapAndGoMode, mCanAddMarket);
            }
        });
    }

    @JavascriptInterface
    public void onMapDeleteMarker(final int index) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onMapDeleteMarker(index);
            }
        });
    }

    public DroneMap(Context context, View view, Handler handler) {
        mContext = context;
        mHandler = handler;

        mMapWebView = (WebView) view.findViewById(R.id.map_webview);
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
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    }

    public void updateMissions(List<Mission> missions) {
        mMapWebView.loadUrl("javascript:updateMissionMarkers(" + transMissionToJson(missions) + ")");
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

    public void onDestroy() {
        mMapWebView.loadUrl("about:blank");
    }

    public void updateDroneLocation(long latitude, long longitude, float yaw) {
        mMapWebView.loadUrl("javascript:updateDroneLocation(" + latitude + "," + longitude + "," + yaw + ")");
    }

    public void setAddMarkerEnable(boolean isAddMarkerEnable) {
        mCanAddMarket = isAddMarkerEnable;
        mMapWebView.loadUrl("javascript:setMapClickable(" + isAddMarkerEnable + ")");
    }

    public void onStart() {
        mMapWebView.loadUrl("javascript:enableGeoLocation(true)");
    }

    public void onStop() {
        mMapWebView.loadUrl("javascript:enableGeoLocation(false)");
    }

    private String transMissionToJson(List missions) {
        return mGson.toJson(missions);
    }

    public void updateFootprints(SurveyRouter surveyRouter) {
        Pair<Coord2D, Coord2D> cameraOffset = surveyRouter.getCameraOffset();
        mMapWebView.loadUrl(String.format("javascript:initFootprintProperties(%f , %f, %f, %f)",
                cameraOffset.first.getX(), cameraOffset.first.getY(), cameraOffset.second.getX(), cameraOffset.second.getY()));
        mMapWebView.loadUrl("javascript:updateFootprint(" + transMissionToJson(surveyRouter.getCameraShutterPoints()) + ")");
    }

    public void clearSurvey() {
        mMapWebView.loadUrl("javascript:clearSurvey()");
    }

    public void clearFootprint() {
        mMapWebView.loadUrl("javascript:clearFootprint()");
    }

    public void clearMap() {
        mMapWebView.loadUrl("javascript:mapClean()");
    }

    public void updatePolygon(List<Coord2D> polygonPoints) {
        mMapWebView.loadUrl("javascript:updatePolygon(" + transMissionToJson(polygonPoints) + ")");
    }

}
