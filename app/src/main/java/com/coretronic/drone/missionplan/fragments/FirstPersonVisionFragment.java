package com.coretronic.drone.missionplan.fragments;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.coretronic.drone.R;
import com.coretronic.drone.ui.Joystick;
import com.coretronic.drone.uvc.CameraHandler;
import com.coretronic.drone.uvc.USBCameraMonitor;
import com.coretronic.drone.uvc.USBCameraMonitor.OnUVCCameraStatusChangedListener;
import com.coretronic.drone.uvc.UVCCameraTextureView;
import com.coretronic.drone.uvc.UsbCameraWrap;

/**
 * Created by Poming on 2015/10/21.
 */
public class FirstPersonVisionFragment extends Fragment {

    private UVCCameraTextureView mCameraView;
    private USBCameraMonitor mUSBCameraMonitor;
    private CameraHandler mCameraHandler;
    private Surface mSurface;

    private View mShutterButton;
    private View mGimbleControl;
    private RelativeLayout mGimbleRollRotateView;
    private ImageView mGimbleRollButton;

    private Matrix matrix;

    private int[] mGimbleRollRotateViewLocation = new int[2];
    private double mCurrAngle = 0;
    private double mDownAngle = 0;
    private double mPrevAngle = 0;

    private Joystick mJoystick;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpv, container, false);
        mCameraHandler = CameraHandler.createHandler();
        mCameraView = (UVCCameraTextureView) view.findViewById(R.id.camera_view);
        mCameraHandler.setUVCCameraTextureView(mCameraView);
        mCameraView.addSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mUSBCameraMonitor = USBCameraMonitor.getInstance();
                mUSBCameraMonitor.registerOnCameraDeviceStatusChangedListener(mOnUVCCameraStatusChangedListener);
                mCameraHandler.setScreenSize(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        mShutterButton = view.findViewById(R.id.shutter_button);
        mGimbleControl = view.findViewById(R.id.layout_gimbal_control);

        mGimbleRollRotateView = (RelativeLayout) view.findViewById(R.id.btn_gimble_con_roll_rotate_view);
        mGimbleRollButton = (ImageView) view.findViewById(R.id.gimble_con_roll_button);
        mGimbleRollButton.setOnTouchListener(onGimbleRollButtonTouchListener);

        mJoystick = (Joystick) view.findViewById(R.id.joystickView);
        mJoystick.setJoystickListener(new Joystick.JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float degrees, float offset) {

            }

            @Override
            public void onUp() {

            }
        });

        matrix = new Matrix();

        return view;
    }

    private View.OnTouchListener onGimbleRollButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGimbleRollRotateView.getLocationOnScreen(mGimbleRollRotateViewLocation);
            final float rollCenterX = mGimbleRollRotateViewLocation[0] + (mGimbleRollRotateView.getWidth() / 2);
            final float rollCenterY = mGimbleRollRotateViewLocation[1] + (mGimbleRollRotateView.getHeight() / 2);
            final float touchX = event.getRawX();
            final float touchY = event.getRawY();
            final float downX, downY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mGimbleRollButton.setImageResource(R.drawable.btn_gimble_con_roll_p);
                    mGimbleRollRotateView.clearAnimation();
                    downX = touchX;
                    downY = touchY;
                    mDownAngle = Math.toDegrees(Math.atan2(downX - rollCenterX, rollCenterY - downY));
                    break;
                case MotionEvent.ACTION_MOVE:
                    mPrevAngle = mCurrAngle;
                    mCurrAngle = Math.toDegrees(Math.atan2(touchX - rollCenterX, rollCenterY - touchY)) - mDownAngle;
                    mCurrAngle = mCurrAngle < -90 ? -90 : mCurrAngle;
                    mCurrAngle = mCurrAngle > 90 ? 90 : mCurrAngle;
                    rotateAnimate(mPrevAngle, mCurrAngle, 0);
                    break;
                case MotionEvent.ACTION_UP:
                    mGimbleRollButton.setImageResource(R.drawable.btn_gimble_con_roll_n);
                    mPrevAngle = mCurrAngle = 0;
                    rotateAnimate(mPrevAngle, mCurrAngle, 1000);
                    break;
            }
            return true;
        }
    };

    private void rotateAnimate(double fromDegrees, double toDegrees, long durationMillis) {
        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillAfter(true);
        mGimbleRollRotateView.startAnimation(rotate);
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mCameraView.getSurfaceTexture();
        if (surfaceTexture == null) {
            for (int i = 0; i < 3; i++) {
                mCameraView.getSurfaceTexture();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = new Surface(surfaceTexture);
        mCameraHandler.startPreview(mSurface);
    }

    private final OnUVCCameraStatusChangedListener mOnUVCCameraStatusChangedListener = new OnUVCCameraStatusChangedListener() {
        @Override
        public void onAttach(final UsbDevice usbDevice) {
            if (usbDevice == null) {
                return;
            }
            mUSBCameraMonitor.requestPermission(usbDevice);
        }

        @Override
        public void onDetach(final UsbDevice usbDevice) {
            if (mCameraHandler != null) {
                mCameraHandler.closeCamera();
            }
        }

        @Override
        public void onPermissionGranted(UsbDevice usbDevice) {
            if (usbDevice == null) {
                return;
            }
            mUSBCameraMonitor.openCamera(usbDevice);
        }

        @Override
        public void onCameraOpened(UsbCameraWrap uvcCameraWrap) {
            mCameraHandler.onCameraOpened(uvcCameraWrap);
            startPreview();
        }

        @Override
        public void onCancel() {
            ((MapViewFragment) getParentFragment()).hideFPVFragment();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUSBCameraMonitor != null) {
            mUSBCameraMonitor.unregisterOnCameraDeviceStatusChangedListener();
            mUSBCameraMonitor = null;
        }
        if (mCameraHandler != null) {
            mCameraHandler = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mCameraHandler.closeCamera();
    }

    public void onFPVShow(){
        mShutterButton.setVisibility(View.VISIBLE);
        mGimbleControl.setVisibility(View.VISIBLE);
    }

    public void onFPVHide(){
        mShutterButton.setVisibility(View.GONE);
        mGimbleControl.setVisibility(View.GONE);
    }
}
