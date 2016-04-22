package com.coretronic.drone.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

import com.coretronic.drone.R;

/**
 * TODO: document your custom view class.
 */
public class AircraftCompassWrapView {

    private final static int ANIMATION_DURATION_IN_SECONDS = 500;   // 0.5 sec

    private final static int ANIMATION_TRANSLATE_X = 0;
    private final static int ANIMATION_TRANSLATE_X_DELTA = 0;
    private final static float ANIMATION_PILOT_X = 0.5f;
    private final static float ANIMATION_PILOT_Y = 0.5f;
    private final static int DEFAULT_RESET_VALUE = 0;
    private final static int CIRCLE_ANGLE = 360;

    private final View mLevelView;
    private final View mCircleView;
    private final View mRulerView;
    private final View mDirectionView;
    private final float mRulerHeightScale;

    private float mDroneRoll;
    private float mDronePitch;
    private float mDroneYaw;
    private float mUserDirection;

    private long mAnimationDuration = ANIMATION_DURATION_IN_SECONDS;

    public AircraftCompassWrapView(View view, int yawCircleId, int rollLevelId, int pitchRulerId, int userDirectionId) {
        mLevelView = view.findViewById(rollLevelId);
        mCircleView = view.findViewById(yawCircleId);
        mRulerView = view.findViewById(pitchRulerId);
        mDirectionView = view.findViewById(userDirectionId);
        mRulerHeightScale = view.getResources().getDimension(R.dimen.compass_ruler_height);
    }

    private Animation getYawAnimation(float yaw) {
        while (Math.abs(mDroneYaw - yaw) > CIRCLE_ANGLE / 2) {
            yaw += mDroneYaw > yaw ? CIRCLE_ANGLE : -CIRCLE_ANGLE;
        }

        Animation yawAnimation = new RotateAnimation(mDroneYaw, yaw, Animation.RELATIVE_TO_SELF, ANIMATION_PILOT_X, Animation.RELATIVE_TO_SELF, ANIMATION_PILOT_X);
        yawAnimation.setInterpolator(new LinearInterpolator());
        yawAnimation.setDuration(mAnimationDuration);
        yawAnimation.setFillAfter(true);
        mDroneYaw = yaw % CIRCLE_ANGLE;
        return yawAnimation;
    }

    private Animation getUserDirectionAnimation(float direction) {

        Animation yawAnimation = new RotateAnimation(mUserDirection, direction, Animation.RELATIVE_TO_SELF, ANIMATION_PILOT_X, Animation.RELATIVE_TO_SELF,
                ANIMATION_PILOT_X);
        yawAnimation.setInterpolator(new LinearInterpolator());
        yawAnimation.setDuration(mAnimationDuration);
        yawAnimation.setFillAfter(true);
        mUserDirection = direction;
        return yawAnimation;
    }

    private Animation getRollAnimation(float roll) {

        Animation rollAnimation = new RotateAnimation(mDroneRoll, roll, Animation.RELATIVE_TO_SELF, ANIMATION_PILOT_X, Animation.RELATIVE_TO_SELF,
                ANIMATION_PILOT_Y);
        rollAnimation.setInterpolator(new LinearInterpolator());
        rollAnimation.setDuration(mAnimationDuration);
        rollAnimation.setFillAfter(true);
        mDroneRoll = roll;
        return rollAnimation;
    }

    private Animation getPitchAnimation(float pitch) {
        Animation pitchAnimation = new TranslateAnimation(ANIMATION_TRANSLATE_X, ANIMATION_TRANSLATE_X_DELTA, mDronePitch, pitch);
        pitchAnimation.setInterpolator(new LinearInterpolator());
        pitchAnimation.setDuration(mAnimationDuration);
        pitchAnimation.setFillAfter(true);
        mDronePitch = pitch;
        return pitchAnimation;
    }

    public AircraftCompassWrapView setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
        return this;
    }

    public AircraftCompassWrapView setDroneYaw(float droneYaw) {
        droneYaw *= -1;
        mCircleView.startAnimation(getYawAnimation(droneYaw));
        return this;
    }

    public AircraftCompassWrapView setDronePitch(float dronePitch) {
        mRulerView.startAnimation(getPitchAnimation(dronePitch * mRulerHeightScale));
        return this;
    }

    public AircraftCompassWrapView setDroneRoll(float droneRoll) {
        mLevelView.startAnimation(getRollAnimation(droneRoll));
        return this;
    }

    public AircraftCompassWrapView setUserDirection(float userDirection) {
        mDirectionView.startAnimation(getUserDirectionAnimation(userDirection));
        return this;
    }

    public AircraftCompassWrapView reset() {
        setDroneYaw(DEFAULT_RESET_VALUE).setDronePitch(DEFAULT_RESET_VALUE);
        setDroneRoll(DEFAULT_RESET_VALUE).setUserDirection(DEFAULT_RESET_VALUE);
        setAnimationDuration(ANIMATION_DURATION_IN_SECONDS);
        return this;
    }
}
