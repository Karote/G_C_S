package com.coretronic.drone.uvc;

import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import com.serenegiant.usb.UVCCamera;

import java.lang.ref.WeakReference;

/**
 * Created by Poming on 2015/10/26.
 */
public final class CameraHandler extends android.os.Handler {

    private static final int PREVIEW_MODE = UVCCamera.FRAME_FORMAT_MJPEG;
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;
    private final static Size DEFAULT_SIZE = new Size(0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);

    private static final int MSG_OPEN = 0;
    private static final int MSG_CLOSE = 1;
    private static final int MSG_PREVIEW_START = 2;
    private static final int MSG_PREVIEW_STOP = 3;
    private static final int MSG_RELEASE = 4;

    private final WeakReference<CameraThread> mWeakThread;
    private int mScreenHeight = PREVIEW_HEIGHT;
    private int mScreenWidth = PREVIEW_WIDTH;
    private UVCCameraTextureView mUVCCameraTextureView;

    public static final CameraHandler createHandler() {
        final CameraThread thread = new CameraThread();
        thread.start();
        return thread.getHandler();
    }

    private CameraHandler(final CameraThread thread) {
        mWeakThread = new WeakReference<>(thread);
    }

    public CameraHandler setScreenSize(int width, int height) {
        mScreenHeight = height;
        mScreenWidth = width;
        return this;
    }

    public CameraHandler setUVCCameraTextureView(UVCCameraTextureView UVCCameraTextureView) {
        mUVCCameraTextureView = UVCCameraTextureView;
        return this;
    }

    public void closeCamera() {
        stopPreview();
        sendEmptyMessage(MSG_CLOSE);
    }

    public void startPreview(final Surface surface) {
        if (surface != null) {
            sendMessage(obtainMessage(MSG_PREVIEW_START, surface));
        }
    }

    public void stopPreview() {
        final CameraThread thread = mWeakThread.get();
        if (thread == null) return;
        synchronized (thread.mSync) {
            sendEmptyMessage(MSG_PREVIEW_STOP);
            try {
                thread.mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    @Override
    public void handleMessage(final Message msg) {
        final CameraThread thread = mWeakThread.get();
        if (thread == null) return;
        switch (msg.what) {
            case MSG_OPEN:
                thread.handleOpen((UsbCameraWrap) msg.obj);
                break;
            case MSG_CLOSE:
                thread.handleClose();
                break;
            case MSG_PREVIEW_START:
                thread.handleStartPreview((Surface) msg.obj);
                break;
            case MSG_PREVIEW_STOP:
                thread.handleStopPreview();
                break;
            case MSG_RELEASE:
                thread.handleRelease();
            default:
                throw new RuntimeException("unsupported message:what=" + msg.what);
        }
    }

    public void onCameraOpened(UsbCameraWrap usbCameraWrap) {
        sendMessage(obtainMessage(MSG_OPEN, usbCameraWrap));
    }

    public double getCameraRatio() {
        return mScreenWidth * 1.0 / mScreenHeight;
    }

    private static final class CameraThread extends Thread {
        private final Object mSync = new Object();
        private CameraHandler mHandler;
        private UVCCamera mUVCCamera;
        private int mPreviewWidth;
        private int mPreviewHeight;

        private CameraThread() {
            super("CameraThread");
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        public CameraHandler getHandler() {
            synchronized (mSync) {
                if (mHandler == null)
                    try {
                        mSync.wait();
                    } catch (final InterruptedException e) {
                    }
            }
            return mHandler;
        }

        public void handleOpen(final UsbCameraWrap uvcCamera) {
            handleClose();
            mUVCCamera = new UVCCamera();
            mUVCCamera.open(uvcCamera);
            final Size targetSize = findFitScreenSize();
            mPreviewWidth = targetSize.getWidth();
            mPreviewHeight = targetSize.getHeight();
            mHandler.mUVCCameraTextureView.post(new Runnable() {
                @Override
                public void run() {
                    mHandler.mUVCCameraTextureView.setAspectRatio(targetSize.getRatio());
                }
            });
        }

        private Size findFitScreenSize() {

            double screenRatio = mHandler.getCameraRatio();
            Size targetSize = mHandler.DEFAULT_SIZE;
            double minDiffRatio = Double.MAX_VALUE;
            double minDiffWidth = Double.MAX_VALUE;
            for (Size size : mUVCCamera.getSupportedSizeList()) {
                if (size.getWidth() > mHandler.mScreenWidth) {
                    continue;
                }
                if (Math.abs(size.getRatio() - screenRatio) <= minDiffRatio && Math.abs(size.getWidth() - mHandler.mScreenWidth) <= minDiffWidth) {
                    minDiffRatio = Math.abs(size.getRatio() - screenRatio);
                    minDiffWidth = Math.abs(size.getWidth() - mHandler.mScreenWidth);
                    targetSize = size;
                }
            }
            return targetSize;
        }

        public void handleClose() {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
        }

        public void handleStartPreview(final Surface surface) {
            if (mUVCCamera == null) {
                return;
            }
            try {
                mUVCCamera.setPreviewSize(mPreviewWidth, mPreviewHeight, PREVIEW_MODE);
            } catch (final IllegalArgumentException e) {
                try {
                    mUVCCamera.setPreviewSize(mPreviewWidth, mPreviewHeight, UVCCamera.DEFAULT_PREVIEW_MODE);
                } catch (final IllegalArgumentException e1) {
                    handleClose();
                    return;
                }
            }
            if (mUVCCamera != null) {
                mUVCCamera.setPreviewDisplay(surface);
                mUVCCamera.setAutoFocus(true);
                mUVCCamera.updateCameraParams();
                mUVCCamera.startPreview();
            }
        }

        public void handleStopPreview() {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            synchronized (mSync) {
                mSync.notifyAll();
            }
        }

        public void handleRelease() {
            handleClose();
            Looper.myLooper().quit();
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (mSync) {
                mHandler = new CameraHandler(this);
                mSync.notifyAll();
            }
            Looper.loop();
            synchronized (mSync) {
                mHandler = null;
                mSync.notifyAll();
            }
        }
    }
}