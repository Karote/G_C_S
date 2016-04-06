package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
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

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.model.Parameters;
import com.coretronic.drone.ui.Joystick;
import com.coretronic.drone.uvc.CameraHandler;
import com.coretronic.drone.uvc.USBCameraMonitor;
import com.coretronic.drone.uvc.USBCameraMonitor.OnUVCCameraStatusChangedListener;
import com.coretronic.drone.uvc.UVCCameraTextureView;
import com.coretronic.drone.uvc.UsbCameraWrap;

import java.util.Timer;

/**
 * Created by Poming on 2015/10/21.
 */
public class FirstPersonVisionFragment extends Fragment {
    private final static int PWM_MAX = 2000;
    private final static int PWM_MIN = 1000;

    private UVCCameraTextureView mCameraView;
    private USBCameraMonitor mUSBCameraMonitor;
    private CameraHandler mCameraHandler;
    private Surface mSurface;
    private DroneController mDroneController;

    private View mShutterButton;
    private View mGimbleControl;
    private RelativeLayout mGimbleRollRotateView;
    private ImageView mGimbleRollButton;

    private int[] mGimbleRollRotateViewLocation = new int[2];
    private float mCurrAngle = 0;
    private float mDownAngle = 0;
    private float mPrevAngle = 0;

    private Handler mSendPWMHandler;
    private Runnable mSendPWMRunnable;
    private Timer timer;
    private float mGimbleRollPWM;
    private float mGimbleYawPWM;
    private float mGimblePitchPWM;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDroneController = ((MainActivity) activity).getDroneController();
    }

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

        mSendPWMHandler = new Handler();

        mShutterButton = view.findViewById(R.id.shutter_button);
        mGimbleControl = view.findViewById(R.id.layout_gimbal_control);

        mGimbleRollRotateView = (RelativeLayout) view.findViewById(R.id.btn_gimble_con_roll_rotate_view);
        mGimbleRollButton = (ImageView) view.findViewById(R.id.gimble_con_roll_button);
        mGimbleRollButton.setOnTouchListener(onGimbleRollButtonTouchListener);

        ((Joystick) view.findViewById(R.id.joystickView)).setJoystickListener(new Joystick.JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(final float xOffset, final float yOffset) {
                mGimbleYawPWM = pwmTransfer(xOffset);
                mGimblePitchPWM = pwmTransfer(yOffset);

                if (mSendPWMRunnable != null) {
                    return;
                }
                mSendPWMRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mDroneController.gimbalControl(Parameters.GIMBAL_CONTROL_YAW, mGimbleYawPWM);
                        mDroneController.gimbalControl(Parameters.GIMBAL_CONTROL_PITCH, mGimblePitchPWM);
                        mSendPWMHandler.postDelayed(mSendPWMRunnable, 100);
                    }
                };
                mSendPWMHandler.post(mSendPWMRunnable);
            }

            @Override
            public void onUp() {
                mGimbleYawPWM = pwmTransfer(0);
                mGimblePitchPWM = pwmTransfer(0);
                mDroneController.gimbalControl(Parameters.GIMBAL_CONTROL_YAW, mGimbleYawPWM);
                mDroneController.gimbalControl(Parameters.GIMBAL_CONTROL_PITCH, mGimblePitchPWM);
                if (mSendPWMRunnable != null) {
                    mSendPWMHandler.removeCallbacks(mSendPWMRunnable);
                    mSendPWMRunnable = null;
                }
            }
        });

        return view;
    }

    private float pwmTransfer(float offset) {
        float pwmDiff = (PWM_MAX - PWM_MIN) / 2;
        float pwmBase = (PWM_MAX + PWM_MIN) / 2;
        int posCoe = offset < 0 ? -1 : 1;
        return posCoe * offset * offset * pwmDiff + pwmBase;
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
                    mDownAngle = (float) Math.toDegrees(Math.atan2(downX - rollCenterX, rollCenterY - downY));
                    break;
                case MotionEvent.ACTION_MOVE:
                    mCurrAngle = (float) Math.toDegrees(Math.atan2(touchX - rollCenterX, rollCenterY - touchY)) - mDownAngle;
                    mCurrAngle = mCurrAngle < -90 ? -90 : mCurrAngle;
                    mCurrAngle = mCurrAngle > 90 ? 90 : mCurrAngle;
                    rotateAnimate(mPrevAngle, mCurrAngle, 0);
                    mGimbleRollPWM = pwmTransfer(mCurrAngle / 90);
                    mPrevAngle = mCurrAngle;
                    if (mSendPWMRunnable != null) {
                        break;
                    }
                    mSendPWMRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mDroneController.gimbalControl(Parameters.GIMBAL_CONTROL_ROLL, mGimbleRollPWM);
                            mSendPWMHandler.postDelayed(mSendPWMRunnable, 100);
                        }
                    };
                    mSendPWMHandler.post(mSendPWMRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                    mGimbleRollButton.setImageResource(R.drawable.btn_gimble_con_roll_n);
                    mPrevAngle = mCurrAngle = 0;
                    rotateAnimate(mPrevAngle, mCurrAngle, 1000);

                    mGimbleRollPWM = pwmTransfer(mCurrAngle);
                    mDroneController.gimbalControl(Parameters.GIMBAL_CONTROL_ROLL, mGimbleRollPWM);
                    if (mSendPWMRunnable != null) {
                        mSendPWMHandler.removeCallbacks(mSendPWMRunnable);
                        mSendPWMRunnable = null;
                    }
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
        if (mSendPWMHandler != null) {
            mSendPWMHandler = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mCameraHandler.closeCamera();
    }

    public void onFPVShow() {
        mShutterButton.setVisibility(View.VISIBLE);
        mGimbleControl.setVisibility(View.VISIBLE);
    }

    public void onFPVHide() {
        mShutterButton.setVisibility(View.GONE);
        mGimbleControl.setVisibility(View.GONE);
    }
}
