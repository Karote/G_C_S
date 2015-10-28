package com.coretronic.drone.missionplan.fragments;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
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

        return view;
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
}
