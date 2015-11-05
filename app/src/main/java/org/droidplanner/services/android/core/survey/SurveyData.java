package org.droidplanner.services.android.core.survey;

import android.util.Pair;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.polygon.Polygon;

import java.util.List;
import java.util.Locale;

public class SurveyData {

    private final static int DEFAULT_OVERLAP = 50;
    private final static int DEFAULT_SIDELAP = 60;
    private final static int DEFAULT_ALTITUDE = 50;
    private final static int DEFAULT_ANGLE = 0;

    private CameraInfo mCameraInfo;
    private double mAltitude;
    private double mAngle;
    private double mOverlap;
    private double mSidelap;
    private Footprint mFootprint;
    private Polygon mPolygon;

    public SurveyData() {
        mCameraInfo = new CameraInfo();
        update(DEFAULT_ANGLE, DEFAULT_ALTITUDE, DEFAULT_OVERLAP, DEFAULT_SIDELAP);
    }

    public void update(double angle, double altitude, double overlap, double sidelap) {
        mAngle = angle;
        mOverlap = overlap;
        mSidelap = sidelap;
        mAltitude = altitude;
        updateFootprint();
    }

    public SurveyData setOverlap(double overlap) {
        mOverlap = overlap;
        return this;
    }

    public SurveyData setSidelap(double sidelap) {
        mSidelap = sidelap;
        return this;
    }

    public SurveyData setAltitude(double altitude) {
        mAltitude = altitude;
        updateFootprint();
        return this;
    }

    public SurveyData setCameraInfo(CameraInfo info) {
        mCameraInfo = info;
        updateFootprint();
        return this;
    }

    public SurveyData setAngle(double angle) {
        mAngle = angle;
        updateFootprint();
        return this;
    }

    public SurveyData setPolygon(Polygon polygon) {
        mPolygon = polygon;
        return this;
    }

    private void updateFootprint() {
        mFootprint = new Footprint(mCameraInfo, this.mAltitude, mAngle);
    }

    public CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    public double getLongitudinalPictureDistance() {
        return getLongitudinalFootPrint() * (1 - mOverlap * .01);
    }

    public double getLateralPictureDistance() {
        return getLateralFootPrint() * (1 - mSidelap * .01);
    }

    public double getAltitude() {
        return mAltitude;
    }

    public double getAngle() {
        return mAngle;
    }

    public double getSidelap() {
        return mSidelap;
    }

    public double getOverlap() {
        return mOverlap;
    }

    public double getLateralFootPrint() {
        return mAltitude * mCameraInfo.getSensorLateralSize() / mCameraInfo.getFocalLength();
    }

    public double getLongitudinalFootPrint() {
        return mAltitude * mCameraInfo.getSensorLongitudinalSize() / mCameraInfo.getFocalLength();
    }

    public List<Coord2D> getFootPrintFrame() {
        return mFootprint.getVertexInGlobalFrame();
    }

    public double getGroundResolution() {
        return getLongitudinalFootPrint() * 1000 * getLateralFootPrint() * 1000 / (mCameraInfo.getSensorResolution() * 1000000);
    }

    public double getArea(){
        return mPolygon.getArea().valueInSqMeters();
    }

    public Pair<Coord2D, Coord2D> getCameraOffset() {
        return new Pair<>(mFootprint.getVertexInGlobalFrame().get(0), mFootprint.getVertexInGlobalFrame().get(1));
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Altitude: %f Angle %f Overlap: %f Sidelap: %f Footprint %s", mAltitude,
                mAngle, mOverlap, mSidelap, mFootprint.toString());
    }

}