package com.coretronic.drone.survey;

import android.util.Pair;

import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Builder;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.model.ShutterControl;
import com.coretronic.drone.model.ShutterControl.Mode;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.geoTools.PolylineTools;
import org.droidplanner.services.android.core.survey.SurveyData;
import org.droidplanner.services.android.core.survey.grid.EndpointSorter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poming on 2015/10/15.
 */
public class SurveyRouter {

    private final Builder mMissionBuilder;

    private List<Coord2D> mWayPoints;
    private List<Coord2D> mCameraShutterPoints;
    private SurveyData mSurveyData;

    public SurveyRouter(List<Coord2D> wayPoints, List<Coord2D> cameraShutterPoints, SurveyData surveyData) {
        mWayPoints = wayPoints;
        mCameraShutterPoints = cameraShutterPoints;
        mSurveyData = surveyData;
        mMissionBuilder = new Builder();
    }

    public SurveyRouter(EndpointSorter endpointSorter, SurveyData surveyData) {
        this(endpointSorter.getSortedGrid(), endpointSorter.getCameraLocations(), surveyData);
    }

    public List<Coord2D> getCameraShutterPoints() {
        return mCameraShutterPoints;
    }

    public SurveyData getSurveyData() {
        return mSurveyData;
    }

    public int getCameraShutterCount() {
        return mCameraShutterPoints.size();
    }

    public Pair<Coord2D, Coord2D> getCameraOffset() {
        return mSurveyData.getCameraOffset();
    }

    public double getLength() {
        return PolylineTools.getPolylineLength(mWayPoints);
    }

    public int getNumberOfLines() {
        return mWayPoints.size() / 2;
    }

    public List<Mission> toMissions(float droneLat, float droneLon) {

        List<Mission> missions = new ArrayList<>();

        mMissionBuilder.setAltitude((float) mSurveyData.getAltitude()).setAutoContinue(true).setWaitSeconds(0).setRadius(0);
        Coord2D firstPoint = mWayPoints.get(0);
        missions.add(mMissionBuilder.setType(Type.TAKEOFF).setLatitude(droneLat).setLongitude(droneLon).create());
        missions.add(mMissionBuilder.setType(Type.CAMERA_TRIGGER_DISTANCE).setLatitude((float) firstPoint.getLatitude()).setLongitude((float) firstPoint
                .getLongitude()).setShutterControl(new ShutterControl(Mode.DISTANCE, (int) (mSurveyData.getLongitudinalPictureDistance() * 10))).create());

        mMissionBuilder.setType(Type.WAY_POINT);
        for (int index = 1; index < mWayPoints.size() - 1; index++) {
            Coord2D wayPoint = mWayPoints.get(index);
            missions.add(mMissionBuilder.setLatitude((float) wayPoint.getLatitude()).setLongitude((float) wayPoint.getLongitude()).create());
        }

        Coord2D rtlPoint = mWayPoints.get(mWayPoints.size() - 1);
        missions.add(mMissionBuilder.setType(Type.CAMERA_TRIGGER_DISTANCE).setLatitude((float) rtlPoint.getLatitude()).setLongitude((float) rtlPoint
                .getLongitude()).setShutterControl(new ShutterControl(Mode.DISABLE, 0)).create());
        missions.add(mMissionBuilder.setType(Type.RTL).setLatitude((float) rtlPoint.getLatitude()).setLongitude((float) rtlPoint
                .getLongitude()).create());

        return missions;

    }

}
