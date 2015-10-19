package org.droidplanner.services.android.core.survey;

public class CameraInfo {

    private final String mName;
    private final double mSensorWidth;
    private final double mSensorHeight;
    private final double mSensorResolution;
    private final double mFocalLength;
    private final boolean mIsInLandscapeOrientation;

    public CameraInfo() {
        this("Canon SX260", 6.12, 4.22, 12.1, 5.0);
    }

    public CameraInfo(String name, double sensorWidth, double sensorHeight, double sensorResolution, double focalLength, boolean isInLandscapeOrientation) {
        this.mName = name;
        this.mSensorWidth = sensorWidth;
        this.mSensorHeight = sensorHeight;
        this.mSensorResolution = sensorResolution;
        this.mFocalLength = focalLength;
        this.mIsInLandscapeOrientation = isInLandscapeOrientation;
    }

    public CameraInfo(String name, double sensorWidth, double sensorHeight, double sensorResolution, double focalLength) {
        this(name, sensorWidth, sensorHeight, sensorResolution, focalLength, true);
    }

    public String getName() {
        return mName;
    }

    public double getSensorWidth() {
        return mSensorWidth;
    }

    public double getSensorHeight() {
        return mSensorHeight;
    }

    public double getSensorResolution() {
        return mSensorResolution;
    }

    public double getFocalLength() {
        return mFocalLength;
    }

    public boolean isInLandscapeOrientation() {
        return mIsInLandscapeOrientation;
    }

    public double getSensorLateralSize() {
        if (mIsInLandscapeOrientation) {
            return mSensorWidth;
        } else {
            return mSensorHeight;
        }
    }

    public double getSensorLongitudinalSize() {
        if (mIsInLandscapeOrientation) {
            return mSensorHeight;
        } else {
            return mSensorWidth;
        }
    }

    @Override
    public String toString() {
        return "CameraInfo{" +
                "mName='" + mName + '\'' +
                ", mSensorWidth=" + mSensorWidth +
                ", mSensorHeight=" + mSensorHeight +
                ", mSensorResolution=" + mSensorResolution +
                ", mFocalLength=" + mFocalLength +
                ", mIsInLandscapeOrientation=" + mIsInLandscapeOrientation +
                '}';
    }
}