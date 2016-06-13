package com.coretronic.drone.missionplan.map;

import android.content.Context;
import android.graphics.Bitmap;
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

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Poming on 2015/9/10.
 */
public class DroneMap implements OnMapEventCallback {
    private final static int MAP_MARKER_STYLE_NORMAL = 1;
    private final static int MAP_MARKER_STYLE_SMALL = 2;

    private final Context mContext;
    private final Handler mHandler;
    private final WebView mMapWebView;
    private final Gson mGson;
    private OnMapEventCallback mOnMapEventCallback;

    private boolean mIsTapAndGoMode = true;
    private boolean mCanAddMarker = true;

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

    @Override
    @JavascriptInterface
    public void onGetMissionPlanPathDistanceAndFlightTimeCallback(final int lengthInMeters, final int timeInSeconds) {
        if (mOnMapEventCallback == null) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnMapEventCallback.onGetMissionPlanPathDistanceAndFlightTimeCallback(lengthInMeters, timeInSeconds);
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
                init(mIsTapAndGoMode, mCanAddMarker);
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
        mMapWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mMapWebView.addJavascriptInterface(this, "AndroidFunction");
        mMapWebView.getSettings().setJavaScriptEnabled(true);
        // permission to disclose the user's location to JavaScript.
        mMapWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        mMapWebView.getSettings().setGeolocationEnabled(true);
        mMapWebView.loadUrl("file:///android_asset/drone_map/GoogleMap.html");
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    }

    public void saveWebViewScreenShot(String fileName) {
        mMapWebView.buildDrawingCache();
        Bitmap cacheBitmap = mMapWebView.getDrawingCache();
        if (cacheBitmap == null) {
            return;
        }
        FileOutputStream fos = null;
        try {
//            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos = new FileOutputStream("mnt/sdcard/" + fileName + ".jpg");
            if (fos != null) {
                cacheBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            }
        } catch (Exception e) {

        }
        mMapWebView.destroyDrawingCache();
    }

    public Bitmap getWebViewScreenShot(int width, int height) {

        mMapWebView.buildDrawingCache();
        Bitmap cacheBitmap = Bitmap.createScaledBitmap(mMapWebView.getDrawingCache(), width, height, false);
        mMapWebView.destroyDrawingCache();
        return cacheBitmap;
    }

    public void getMissionPlanPathLengthAndTime(List<Integer> speed) {
        JSONArray speedJson = new JSONArray(speed);
        mMapWebView.loadUrl("javascript:getMissionPlanPathDistanceAndFlightTime(" + speedJson + ")");
    }

    public void updateMissions(List<Mission> missions) {
        mMapWebView.loadUrl("javascript:updateMissionMarkers(" + transMissionToJson(missions) + ")");
    }

    public void setMapToMyLocation() {
        mMapWebView.loadUrl("javascript:setMapToMyLocation()");
    }

    public void setMapToDroneLocation(float droneLat, float dronelon) {
        mMapWebView.loadUrl("javascript:setMapTo(" + droneLat + "," + dronelon + ")");
    }

    public void fitMapShowAll() {
        mMapWebView.loadUrl("javascript:fitMapShowAll()");
    }

    public void fitMapShowAllMissions() {
        mMapWebView.loadUrl("javascript:fitMapShowAllMissionPlanning()");
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

    public void clearTapAndGoPlan() {
        mMapWebView.loadUrl("javascript:clearTapAndGoPlan()");
    }

    public void loadHistory(List<Mission> missions, List<Float> path) {
        JSONArray pathJson = new JSONArray(path);
        mMapWebView.loadUrl("javascript:loadHistory(" + transMissionToJson(missions) + "," + pathJson + ")");
    }

    public void clearHistoryMarkerPath() {
        mMapWebView.loadUrl("javascript:clearHistoryMarkerPath()");
    }

    public void init(boolean isTapAndGo, boolean canMapAddMarker) {
        mMapWebView.loadUrl("javascript:mapClean()");
        mMapWebView.loadUrl("javascript:setMapClickable(" + canMapAddMarker + ")");
        mMapWebView.loadUrl("javascript:setTapGoMode(" + isTapAndGo + ")");
        mMapWebView.loadUrl("javascript:setMarkerStyle(" + MAP_MARKER_STYLE_NORMAL + ")");
        mIsTapAndGoMode = isTapAndGo;
        mCanAddMarker = canMapAddMarker;
    }

    public DroneMap setOnMapEventListener(OnMapEventCallback onMapEventCallback) {
        mOnMapEventCallback = onMapEventCallback;
        return this;
    }

    public void onDestroy() {
        mMapWebView.loadUrl("about:blank");
    }

    public void updateDroneLocation(float latitude, float longitude, float heading) {
        mMapWebView.loadUrl("javascript:updateDroneLocation(" + latitude + "," + longitude + "," + heading + ")");
    }

    public void updateTakeOffPoint(float takeOffLat, float takeOffLon) {
        mMapWebView.loadUrl("javascript:updateTakeOffPoint(" + takeOffLat + "," + takeOffLon + ")");
    }

    public void updateDroneHomeLocation(float homeLat, float homeLon) {
        mMapWebView.loadUrl("javascript:updateHomePoint(" + homeLat + "," + homeLon + ")");
    }

    public void setAddMarkerEnable(boolean isAddMarkerEnable) {
        mCanAddMarker = isAddMarkerEnable;
        mMapWebView.loadUrl("javascript:setMapClickable(" + isAddMarkerEnable + ")");
    }

    public void setMarkerNormal(){
        mMapWebView.loadUrl("javascript:setMarkerStyle(" + MAP_MARKER_STYLE_NORMAL + ")");
    }

    public void setMarkerSmall(){
        mMapWebView.loadUrl("javascript:setMarkerStyle(" + MAP_MARKER_STYLE_SMALL + ")");
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

    public void startUpdateFlightRoute() {
        mMapWebView.loadUrl("javascript:startUpdateFlightRoute()");
    }

    public void stopUpdateFlightRoute() {
        mMapWebView.loadUrl("javascript:stopUpdateFlightRoute()");
    }

    public void clearFlightRoute() {
        mMapWebView.loadUrl("javascript:clearFlightRoute()");
    }

}
