package com.serenegiant.usb;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: UVCCamera.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
*/

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.coretronic.drone.uvc.Size;
import com.coretronic.drone.uvc.UsbCameraWrap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UVCCamera {
    private static final boolean DEBUG = true;    // TODO set false when releasing
    private static final String TAG = UVCCamera.class.getSimpleName();
    private static final String DEFAULT_USBFS = "/dev/bus/usb";

    public static final int DEFAULT_PREVIEW_WIDTH = 640;
    public static final int DEFAULT_PREVIEW_HEIGHT = 480;
    public static final int DEFAULT_PREVIEW_MODE = 0;

    public static final int FRAME_FORMAT_YUYV = 0;
    public static final int FRAME_FORMAT_MJPEG = 1;

    public static final int PIXEL_FORMAT_RAW = 0;
    public static final int PIXEL_FORMAT_YUV = 1;
    public static final int PIXEL_FORMAT_RGB565 = 2;
    public static final int PIXEL_FORMAT_RGBX = 3;
    public static final int PIXEL_FORMAT_YUV420SP = 4;
    public static final int PIXEL_FORMAT_NV21 = 5;        // = YVU420SemiPlanar

    //--------------------------------------------------------------------------------
    public static final int CTRL_SCANNING = 0x00000001;    // D0:  Scanning Mode
    public static final int CTRL_AE = 0x00000002;    // D1:  Auto-Exposure Mode
    public static final int CTRL_AE_PRIORITY = 0x00000004;    // D2:  Auto-Exposure Priority
    public static final int CTRL_AE_ABS = 0x00000008;    // D3:  Exposure Time (Absolute)
    public static final int CTRL_AR_REL = 0x00000010;    // D4:  Exposure Time (Relative)
    public static final int CTRL_FOCUS_ABS = 0x00000020;    // D5:  Focus (Absolute)
    public static final int CTRL_FOCUS_REL = 0x00000040;    // D6:  Focus (Relative)
    public static final int CTRL_IRIS_ABS = 0x00000080;    // D7:  Iris (Absolute)
    public static final int CTRL_IRIS_REL = 0x00000100;    // D8:  Iris (Relative)
    public static final int CTRL_ZOOM_ABS = 0x00000200;    // D9:  Zoom (Absolute)
    public static final int CTRL_ZOOM_REL = 0x00000400;    // D10: Zoom (Relative)
    public static final int CTRL_PANTILT_ABS = 0x00000800;    // D11: PanTilt (Absolute)
    public static final int CTRL_PANTILT_REL = 0x00001000;    // D12: PanTilt (Relative)
    public static final int CTRL_ROLL_ABS = 0x00002000;    // D13: Roll (Absolute)
    public static final int CTRL_ROLL_REL = 0x00004000;    // D14: Roll (Relative)
    public static final int CTRL_FOCUS_AUTO = 0x00020000;    // D17: Focus, Auto
    public static final int CTRL_PRIVACY = 0x00040000;    // D18: Privacy
    public static final int CTRL_FOCUS_SIMPLE = 0x00080000;    // D19: Focus, Simple
    public static final int CTRL_WINDOW = 0x00100000;    // D20: Window

    public static final int PU_BRIGHTNESS = 0x80000001;    // D0: Brightness
    public static final int PU_CONTRAST = 0x80000002;    // D1: Contrast
    public static final int PU_HUE = 0x80000004;    // D2: Hue
    public static final int PU_SATURATION = 0x80000008;    // D3: Saturation
    public static final int PU_SHARPNESS = 0x80000010;    // D4: Sharpness
    public static final int PU_GAMMA = 0x80000020;    // D5: Gamma
    public static final int PU_WB_TEMP = 0x80000040;    // D6: White Balance Temperature
    public static final int PU_WB_COMPO = 0x80000080;    // D7: White Balance Component
    public static final int PU_BACKLIGHT = 0x80000100;    // D8: Backlight Compensation
    public static final int PU_GAIN = 0x80000200;    // D9: Gain
    public static final int PU_POWER_LF = 0x80000400;    // D10: Power Line Frequency
    public static final int PU_HUE_AUTO = 0x80000800;    // D11: Hue, Auto
    public static final int PU_WB_TEMP_AUTO = 0x80001000;    // D12: White Balance Temperature, Auto
    public static final int PU_WB_COMPO_AUTO = 0x80002000;    // D13: White Balance Component, Auto
    public static final int PU_DIGITAL_MULT = 0x80004000;    // D14: Digital Multiplier
    public static final int PU_DIGITAL_LIMIT = 0x80008000;    // D15: Digital Multiplier Limit
    public static final int PU_AVIDEO_STD = 0x80010000;    // D16: Analog Video Standard
    public static final int PU_AVIDEO_LOCK = 0x80020000;    // D17: Analog Video Lock Status
    public static final int PU_CONTRAST_AUTO = 0x80040000;    // D18: Contrast, Auto

    private static boolean isLoaded;

    static {
        if (!isLoaded) {
            System.loadLibrary("usb100");
            System.loadLibrary("uvc");
            System.loadLibrary("UVCCamera");
            isLoaded = true;
        }
    }

    private UsbCameraWrap mUsbCameraWrap;
    protected long mControlSupports;            // カメラコントロールでサポートしている機能フラグ
    protected long mProcSupports;                // プロセッシングユニットでサポートしている機能フラグ
    protected int mCurrentPreviewMode = 0;
    protected String mSupportedSize;
    // these fields from here are accessed from native code and do not change name and remove
    protected long mNativePtr;
    protected int mBrightnessMin, mBrightnessMax, mBrightnessDef;
    protected int mFocusMin, mFocusMax, mFocusDef;
    protected int mContrastMin, mContrastMax, mContrastDef;
    protected int mSharpnessMin, mSharpnessMax, mSharpnessDef;
    protected int mGainMin, mGainMax, mGainDef;
    protected int mGammaMin, mGammaMax, mGammaDef;
    protected int mWhiteBlanceMin, mWhiteBlanceMax, mWhiteBlanceDef;
    protected int mSaturationMin, mSaturationMax, mSaturationDef;
    protected int mHueMin, mHueMax, mHueDef;
    protected int mZoomMin, mZoomMax, mZoomDef;
    // until here

    /**
     * the sonctructor of this class should be call within the thread that has a looper
     * (UI thread or a thread that called Looper.prepare)
     */
    public UVCCamera() {
        mNativePtr = nativeCreate();
        mSupportedSize = null;
    }

    /**
     * connect to a UVC camera
     * USB permission is necessary before this method is called
     *
     * @param usbCameraWrap
     */
    public void open(final UsbCameraWrap usbCameraWrap) {
        mUsbCameraWrap = usbCameraWrap;
        nativeConnect(mNativePtr,
                mUsbCameraWrap.getVendorId(), mUsbCameraWrap.getProductId(),
                mUsbCameraWrap.getFileDescriptor(),
                getUSBFSName(mUsbCameraWrap));
        if (mNativePtr != 0) {
            mSupportedSize = nativeGetSupportedSize(mNativePtr);
        }
        nativeSetPreviewSize(mNativePtr, DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT, DEFAULT_PREVIEW_MODE);
    }

    /**
     * close and release UVC camera
     */
    public void close() {
        stopPreview();
        if (mNativePtr != 0) {
            nativeRelease(mNativePtr);
        }
        mUsbCameraWrap = null;
        mControlSupports = mProcSupports = 0;
        mCurrentPreviewMode = -1;
    }

    public UsbDevice getDevice() {
        return mUsbCameraWrap != null ? mUsbCameraWrap.getDevice() : null;
    }

    public synchronized String getSupportedSize() {
        return !TextUtils.isEmpty(mSupportedSize) ? mSupportedSize : (mSupportedSize = nativeGetSupportedSize(mNativePtr));
    }

    /**
     * Set preview size and preview mode
     *
     * @param width
     * @param height
     * @param mode   0:yuyv, other:MJPEG
     */
    public void setPreviewSize(final int width, final int height, final int mode) {
        if ((width == 0) || (height == 0))
            throw new IllegalArgumentException("invalid preview size");
        if (mNativePtr != 0) {
            final int result = nativeSetPreviewSize(mNativePtr, width, height, mode);
            if (result != 0)
                throw new IllegalArgumentException("Failed to set preview size");
            mCurrentPreviewMode = mode;
        }
    }

    public List<Size> getSupportedSizeList() {
        final int type = (mCurrentPreviewMode > 0) ? 6 : 4;
        return getSupportedSize(type, mSupportedSize);
    }

    public static List<Size> getSupportedSize(final int type, final String supportedSize) {
        final List<Size> result = new ArrayList<Size>();
        if (!TextUtils.isEmpty(supportedSize))
            try {
                final JSONObject json = new JSONObject(supportedSize);
                final JSONArray formats = json.getJSONArray("formats");
                final int format_nums = formats.length();
                for (int i = 0; i < format_nums; i++) {
                    final JSONObject format = formats.getJSONObject(i);
                    final int format_type = format.getInt("type");
                    if ((format_type == type) || (type == -1)) {
                        addSize(format, format_type, result);
                    }
                }
            } catch (final JSONException e) {
            }
        return result;
    }

    private static final void addSize(final JSONObject format, final int type, final List<Size> size_list) throws JSONException {
        final JSONArray size = format.getJSONArray("size");
        final int size_nums = size.length();
        for (int j = 0; j < size_nums; j++) {
            final String[] sz = size.getString(j).split("x");
            try {
                size_list.add(new Size(type, j, Integer.parseInt(sz[0]), Integer.parseInt(sz[1])));
            } catch (final Exception e) {
                break;
            }
        }
    }

    /**
     * set preview surface with SurfaceHolder</br>
     * you can use SurfaceHolder came from SurfaceView/GLSurfaceView
     *
     * @param holder
     */
    public void setPreviewDisplay(final SurfaceHolder holder) {
        nativeSetPreviewDisplay(mNativePtr, holder.getSurface());
    }

    /**
     * set preview surface with SurfaceTexture.
     * this method require API >= 14
     *
     * @param texture
     */
    public void setPreviewTexture(final SurfaceTexture texture) {    // API >= 11
        final Surface surface = new Surface(texture);    // XXX API >= 14
        nativeSetPreviewDisplay(mNativePtr, surface);
    }

    /**
     * set preview surface with Surface
     *
     * @param Surface
     */
    public void setPreviewDisplay(final Surface surface) {
        nativeSetPreviewDisplay(mNativePtr, surface);
    }

    /**
     * set frame callback
     *
     * @param callback
     * @param pixelFormat
     */
    public void setFrameCallback(final IFrameCallback callback, final int pixelFormat) {
        if (mNativePtr != 0) {
            nativeSetFrameCallback(mNativePtr, callback, pixelFormat);
        }
    }

    /**
     * start preview
     */
    public void startPreview() {
        if (mUsbCameraWrap != null) {
            nativeStartPreview(mNativePtr);
        }
    }

    /**
     * stop preview
     */
    public void stopPreview() {
        setFrameCallback(null, 0);
        if (mUsbCameraWrap != null) {
            nativeStopPreview(mNativePtr);
        }
    }

    /**
     * destroy UVCCamera object
     */
    public void destroy() {
        close();
        if (mNativePtr != 0) {
            nativeDestroy(mNativePtr);
            mNativePtr = 0;
        }
    }

    // wrong result may return when you call this just after camera open.
    // it is better to wait several hundreads millseconds.
    public boolean checkSupportFlag(final long flag) {
        updateCameraParams();
        if ((flag & 0x80000000) == 0x80000000)
            return ((mProcSupports & flag) == (flag & 0x7ffffffF));
        else
            return (mControlSupports & flag) == flag;
    }

    //================================================================================
    public synchronized void setAutoFocus(final boolean autoFocus) {
        if (mNativePtr != 0) {
            nativeSetAutoFocus(mNativePtr, autoFocus);
        }
    }

    public synchronized boolean getAutoFocus() {
        boolean result = true;
        if (mNativePtr != 0) {
            result = nativeGetAutoFocus(mNativePtr) > 0;
        }
        return result;
    }
//================================================================================

    /**
     * @param focus[%]
     */
    public synchronized void setFocus(final int focus) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mFocusMax - mFocusMin);
            if (range > 0)
                nativeSetFocus(mNativePtr, (int) (focus / 100.f * range) + mFocusMin);
        }
    }

    /**
     * @param focus_abs
     * @return forcus[%]
     */
    public synchronized int getFocus(final int focus_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateFocusLimit(mNativePtr);
            final float range = Math.abs(mFocusMax - mFocusMin);
            if (range > 0) {
                result = (int) ((focus_abs - mFocusMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return focus[%]
     */
    public synchronized int getFocus() {
        return getFocus(nativeGetFocus(mNativePtr));
    }

    public synchronized void resetFocus() {
        if (mNativePtr != 0) {
            nativeSetFocus(mNativePtr, mFocusDef);
        }
    }

    //================================================================================
    public synchronized void setAutoWhiteBlance(final boolean autoWhiteBlance) {
        if (mNativePtr != 0) {
            nativeSetAutoWhiteBlance(mNativePtr, autoWhiteBlance);
        }
    }

    public synchronized boolean getAutoWhiteBlance() {
        boolean result = true;
        if (mNativePtr != 0) {
            result = nativeGetAutoWhiteBlance(mNativePtr) > 0;
        }
        return result;
    }

//================================================================================

    /**
     * @param whiteBlance[%]
     */
    public synchronized void setWhiteBlance(final int whiteBlance) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mWhiteBlanceMax - mWhiteBlanceMin);
            if (range > 0)
                nativeSetWhiteBlance(mNativePtr, (int) (whiteBlance / 100.f * range) + mWhiteBlanceMin);
        }
    }

    /**
     * @param whiteBlance_abs
     * @return whiteBlance[%]
     */
    public synchronized int getWhiteBlance(final int whiteBlance_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateWhiteBlanceLimit(mNativePtr);
            final float range = Math.abs(mWhiteBlanceMax - mWhiteBlanceMin);
            if (range > 0) {
                result = (int) ((whiteBlance_abs - mWhiteBlanceMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return white blance[%]
     */
    public synchronized int getWhiteBlance() {
        return getFocus(nativeGetWhiteBlance(mNativePtr));
    }

    public synchronized void resetWhiteBlance() {
        if (mNativePtr != 0) {
            nativeSetWhiteBlance(mNativePtr, mWhiteBlanceDef);
        }
    }
//================================================================================

    /**
     * @param brightness[%]
     */
    public synchronized void setBrightness(final int brightness) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mBrightnessMax - mBrightnessMin);
            if (range > 0)
                nativeSetBrightness(mNativePtr, (int) (brightness / 100.f * range) + mBrightnessMin);
        }
    }

    /**
     * @param brightness_abs
     * @return brightness[%]
     */
    public synchronized int getBrightness(final int brightness_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateBrightnessLimit(mNativePtr);
            final float range = Math.abs(mBrightnessMax - mBrightnessMin);
            if (range > 0) {
                result = (int) ((brightness_abs - mBrightnessMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return brightness[%]
     */
    public synchronized int getBrightness() {
        return getBrightness(nativeGetBrightness(mNativePtr));
    }

    public synchronized void resetBrightness() {
        if (mNativePtr != 0) {
            nativeSetBrightness(mNativePtr, mBrightnessDef);
        }
    }

//================================================================================

    /**
     * @param contrast[%]
     */
    public synchronized void setContrast(final int contrast) {
        if (mNativePtr != 0) {
            nativeUpdateContrastLimit(mNativePtr);
            final float range = Math.abs(mContrastMax - mContrastMin);
            if (range > 0)
                nativeSetContrast(mNativePtr, (int) (contrast / 100.f * range) + mContrastMin);
        }
    }

    /**
     * @param contrast_abs
     * @return contrast[%]
     */
    public synchronized int getContrast(final int contrast_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            final float range = Math.abs(mContrastMax - mContrastMin);
            if (range > 0) {
                result = (int) ((contrast_abs - mContrastMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return contrast[%]
     */
    public synchronized int getContrast() {
        return getContrast(nativeGetContrast(mNativePtr));
    }

    public synchronized void resetContrast() {
        if (mNativePtr != 0) {
            nativeSetContrast(mNativePtr, mContrastDef);
        }
    }

//================================================================================

    /**
     * @param sharpness[%]
     */
    public synchronized void setSharpness(final int sharpness) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mSharpnessMax - mSharpnessMin);
            if (range > 0)
                nativeSetSharpness(mNativePtr, (int) (sharpness / 100.f * range) + mSharpnessMin);
        }
    }

    /**
     * @param sharpness_abs
     * @return sharpness[%]
     */
    public synchronized int getSharpness(final int sharpness_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateSharpnessLimit(mNativePtr);
            final float range = Math.abs(mSharpnessMax - mSharpnessMin);
            if (range > 0) {
                result = (int) ((sharpness_abs - mSharpnessMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return sharpness[%]
     */
    public synchronized int getSharpness() {
        return getSharpness(nativeGetSharpness(mNativePtr));
    }

    public synchronized void resetSharpness() {
        if (mNativePtr != 0) {
            nativeSetSharpness(mNativePtr, mSharpnessDef);
        }
    }
//================================================================================

    /**
     * @param gain[%]
     */
    public synchronized void setGain(final int gain) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mGainMax - mGainMin);
            if (range > 0)
                nativeSetGain(mNativePtr, (int) (gain / 100.f * range) + mGainMin);
        }
    }

    /**
     * @param gain_abs
     * @return gain[%]
     */
    public synchronized int getGain(final int gain_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateGainLimit(mNativePtr);
            final float range = Math.abs(mGainMax - mGainMin);
            if (range > 0) {
                result = (int) ((gain_abs - mGainMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return gain[%]
     */
    public synchronized int getGain() {
        return getGain(nativeGetGain(mNativePtr));
    }

    public synchronized void resetGain() {
        if (mNativePtr != 0) {
            nativeSetGain(mNativePtr, mGainDef);
        }
    }

//================================================================================

    /**
     * @param gamma[%]
     */
    public synchronized void setGamma(final int gamma) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mGammaMax - mGammaMin);
            if (range > 0)
                nativeSetGamma(mNativePtr, (int) (gamma / 100.f * range) + mGammaMin);
        }
    }

    /**
     * @param gamma_abs
     * @return gamma[%]
     */
    public synchronized int getGamma(final int gamma_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateGammaLimit(mNativePtr);
            final float range = Math.abs(mGammaMax - mGammaMin);
            if (range > 0) {
                result = (int) ((gamma_abs - mGammaMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return gamma[%]
     */
    public synchronized int getGamma() {
        return getGamma(nativeGetGamma(mNativePtr));
    }

    public synchronized void resetGamma() {
        if (mNativePtr != 0) {
            nativeSetGamma(mNativePtr, mGammaDef);
        }
    }

//================================================================================

    /**
     * @param saturation[%]
     */
    public synchronized void setSaturation(final int saturation) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mSaturationMax - mSaturationMin);
            if (range > 0)
                nativeSetSaturation(mNativePtr, (int) (saturation / 100.f * range) + mSaturationMin);
        }
    }

    /**
     * @param saturation_abs
     * @return saturation[%]
     */
    public synchronized int getSaturation(final int saturation_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateSaturationLimit(mNativePtr);
            final float range = Math.abs(mSaturationMax - mSaturationMin);
            if (range > 0) {
                result = (int) ((saturation_abs - mSaturationMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return saturation[%]
     */
    public synchronized int getSaturation() {
        return getSaturation(nativeGetSaturation(mNativePtr));
    }

    public synchronized void resetSaturation() {
        if (mNativePtr != 0) {
            nativeSetSaturation(mNativePtr, mSaturationDef);
        }
    }
//================================================================================

    /**
     * @param hue[%]
     */
    public synchronized void setHue(final int hue) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mHueMax - mHueMin);
            if (range > 0)
                nativeSetHue(mNativePtr, (int) (hue / 100.f * range) + mHueMin);
        }
    }

    /**
     * @param hue_abs
     * @return hue[%]
     */
    public synchronized int getHue(final int hue_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateHueLimit(mNativePtr);
            final float range = Math.abs(mHueMax - mHueMin);
            if (range > 0) {
                result = (int) ((hue_abs - mHueMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return hue[%]
     */
    public synchronized int getHue() {
        return getHue(nativeGetHue(mNativePtr));
    }

    public synchronized void resetHue() {
        if (mNativePtr != 0) {
            nativeSetHue(mNativePtr, mSaturationDef);
        }
    }

    //================================================================================
    public void setPowerlineFrequency(final int frequency) {
        if (mNativePtr != 0)
            nativeSetPowerlineFrequency(mNativePtr, frequency);
    }

    public int getPowerlineFrequency() {
        return nativeGetPowerlineFrequency(mNativePtr);
    }

//================================================================================

    /**
     * this may not work well with some combination of camera and device
     *
     * @param zoom[%]
     */
    public synchronized void setZoom(final int zoom) {
        if (mNativePtr != 0) {
            final float range = Math.abs(mZoomMax - mZoomMin);
            if (range > 0) {
                final int z = (int) (zoom / 100.f * range) + mZoomMin;
// 			   Log.d(TAG, "setZoom:zoom=" + zoom + " ,value=" + z);
                nativeSetZoom(mNativePtr, z);
            }
        }
    }

    /**
     * @param zoom_abs
     * @return zoom[%]
     */
    public synchronized int getZoom(final int zoom_abs) {
        int result = 0;
        if (mNativePtr != 0) {
            nativeUpdateZoomLimit(mNativePtr);
            final float range = Math.abs(mZoomMax - mZoomMin);
            if (range > 0) {
                result = (int) ((zoom_abs - mZoomMin) * 100.f / range);
            }
        }
        return result;
    }

    /**
     * @return zoom[%]
     */
    public synchronized int getZoom() {
        return getZoom(nativeGetZoom(mNativePtr));
    }

    public synchronized void resetZoom() {
        if (mNativePtr != 0) {
            nativeSetZoom(mNativePtr, mZoomDef);
        }
    }

    //================================================================================
    public synchronized void updateCameraParams() {
        if (mNativePtr != 0) {
            if ((mControlSupports == 0) || (mProcSupports == 0)) {
                // サポートしている機能フラグを取得
                if (mControlSupports == 0)
                    mControlSupports = nativeGetCtrlSupports(mNativePtr);
                if (mProcSupports == 0)
                    mProcSupports = nativeGetProcSupports(mNativePtr);
                // 設定値を取得
                if ((mControlSupports != 0) && (mProcSupports != 0)) {
                    nativeUpdateBrightnessLimit(mNativePtr);
                    nativeUpdateContrastLimit(mNativePtr);
                    nativeUpdateSharpnessLimit(mNativePtr);
                    nativeUpdateGainLimit(mNativePtr);
                    nativeUpdateGammaLimit(mNativePtr);
                    nativeUpdateSaturationLimit(mNativePtr);
                    nativeUpdateHueLimit(mNativePtr);
                    nativeUpdateZoomLimit(mNativePtr);
                    nativeUpdateWhiteBlanceLimit(mNativePtr);
                    nativeUpdateFocusLimit(mNativePtr);
                }
                if (DEBUG) {
                    dumpControls(mControlSupports);
                    dumpProc(mProcSupports);
                    Log.v(TAG, String.format("Brightness:min=%d,max=%d,def=%d", mBrightnessMin, mBrightnessMax, mBrightnessDef));
                    Log.v(TAG, String.format("Contrast:min=%d,max=%d,def=%d", mContrastMin, mContrastMax, mContrastDef));
                    Log.v(TAG, String.format("Sharpness:min=%d,max=%d,def=%d", mSharpnessMin, mSharpnessMax, mSharpnessDef));
                    Log.v(TAG, String.format("Gain:min=%d,max=%d,def=%d", mGainMin, mGainMax, mGainDef));
                    Log.v(TAG, String.format("Gamma:min=%d,max=%d,def=%d", mGammaMin, mGammaMax, mGammaDef));
                    Log.v(TAG, String.format("Saturation:min=%d,max=%d,def=%d", mSaturationMin, mSaturationMax, mSaturationDef));
                    Log.v(TAG, String.format("Hue:min=%d,max=%d,def=%d", mHueMin, mHueMax, mHueDef));
                    Log.v(TAG, String.format("Zoom:min=%d,max=%d,def=%d", mZoomMin, mZoomMax, mZoomDef));
                    Log.v(TAG, String.format("WhiteBlance:min=%d,max=%d,def=%d", mWhiteBlanceMin, mWhiteBlanceMax, mWhiteBlanceDef));
                    Log.v(TAG, String.format("Focus:min=%d,max=%d,def=%d", mFocusMin, mFocusMax, mFocusDef));
                }
            }
        } else {
            mControlSupports = mProcSupports = 0;
        }
    }

    private static final String[] SUPPORTS_CTRL = {
            "D0:  Scanning Mode",
            "D1:  Auto-Exposure Mode",
            "D2:  Auto-Exposure Priority",
            "D3:  Exposure Time (Absolute)",
            "D4:  Exposure Time (Relative)",
            "D5:  Focus (Absolute)",
            "D6:  Focus (Relative)",
            "D7:  Iris (Absolute)",
            "D8:  Iris (Relative)",
            "D9:  Zoom (Absolute)",
            "D10: Zoom (Relative)",
            "D11: PanTilt (Absolute)",
            "D12: PanTilt (Relative)",
            "D13: Roll (Absolute)",
            "D14: Roll (Relative)",
            "D15: Reserved",
            "D16: Reserved",
            "D17: Focus, Auto",
            "D18: Privacy",
            "D19: Focus, Simple",
            "D20: Window",
            "D21: Region of Interest",
            "D22: Reserved, set to zero",
            "D23: Reserved, set to zero",
    };

    private static final String[] SUPPORTS_PROC = {
            "D0: Brightness",
            "D1: Contrast",
            "D2: Hue",
            "D3: Saturation",
            "D4: Sharpness",
            "D5: Gamma",
            "D6: White Balance Temperature",
            "D7: White Balance Component",
            "D8: Backlight Compensation",
            "D9: Gain",
            "D10: Power Line Frequency",
            "D11: Hue, Auto",
            "D12: White Balance Temperature, Auto",
            "D13: White Balance Component, Auto",
            "D14: Digital Multiplier",
            "D15: Digital Multiplier Limit",
            "D16: Analog Video Standard",
            "D17: Analog Video Lock Status",
            "D18: Contrast, Auto",
            "D19: Reserved. Set to zero",
            "D20: Reserved. Set to zero",
            "D21: Reserved. Set to zero",
            "D22: Reserved. Set to zero",
            "D23: Reserved. Set to zero",
    };

    private static final void dumpControls(final long controlSupports) {
        Log.i(TAG, String.format("controlSupports=%x", controlSupports));
        for (int i = 0; i < SUPPORTS_CTRL.length; i++) {
            Log.i(TAG, SUPPORTS_CTRL[i] + ((controlSupports & (0x1 << i)) != 0 ? "=enabled" : "=disabled"));
        }
    }

    private static final void dumpProc(final long procSupports) {
        Log.i(TAG, String.format("procSupports=%x", procSupports));
        for (int i = 0; i < SUPPORTS_PROC.length; i++) {
            Log.i(TAG, SUPPORTS_PROC[i] + ((procSupports & (0x1 << i)) != 0 ? "=enabled" : "=disabled"));
        }
    }

    private final String getUSBFSName(final UsbCameraWrap usbCameraWrap) {
        String result = null;
        final String name = usbCameraWrap.getDeviceName();
        final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
        if ((v != null) && (v.length > 2)) {
            final StringBuilder sb = new StringBuilder(v[0]);
            for (int i = 1; i < v.length - 2; i++)
                sb.append("/").append(v[i]);
            result = sb.toString();
        }
        if (TextUtils.isEmpty(result)) {
            Log.w(TAG, "failed to get USBFS path, try to use default path:" + name);
            result = DEFAULT_USBFS;
        }
        return result;
    }

    // #nativeCreate and #nativeDestroy are not static methods.
    private final native long nativeCreate();

    private final native void nativeDestroy(long id_camera);

    private static final native int nativeConnect(long id_camera, int venderId, int productId, int fileDescriptor, String usbfs);

    private static final native int nativeRelease(long id_camera);

    private static final native int nativeSetPreviewSize(long id_camera, int width, int height, int mode);

    private static final native String nativeGetSupportedSize(long id_camera);

    private static final native int nativeStartPreview(long id_camera);

    private static final native int nativeStopPreview(long id_camera);

    private static final native int nativeSetPreviewDisplay(long id_camera, Surface surface);

    private static final native int nativeSetFrameCallback(long mNativePtr, IFrameCallback callback, int pixelFormat);

//**********************************************************************

    /**
     * start movie capturing(this should call while previewing)
     *
     * @param surface
     */
    public void startCapture(final Surface surface) {
        if (mUsbCameraWrap != null && surface != null) {
            nativeSetCaptureDisplay(mNativePtr, surface);
        } else
            throw new NullPointerException("startCapture");
    }

    /**
     * stop movie capturing
     */
    public void stopCapture() {
        if (mUsbCameraWrap != null) {
            nativeSetCaptureDisplay(mNativePtr, null);
        }
    }

    private static final native int nativeSetCaptureDisplay(long id_camera, Surface surface);

    private static final native long nativeGetCtrlSupports(long id_camera);

    private static final native long nativeGetProcSupports(long id_camera);

    private static final native int nativeSetExposureMode(long id_camera, int exposureMode);

    private static final native int nativeGetExposureMode(long id_camera);

    private static final native int nativeSetAutoFocus(long id_camera, boolean autofocus);

    private static final native int nativeGetAutoFocus(long id_camera);

    private final native int nativeUpdateFocusLimit(long id_camera);

    private static final native int nativeSetFocus(long id_camera, int focus);

    private static final native int nativeGetFocus(long id_camera);

    private static final native int nativeSetAutoWhiteBlance(long id_camera, boolean autoWhiteBlance);

    private static final native int nativeGetAutoWhiteBlance(long id_camera);

    private final native int nativeUpdateWhiteBlanceLimit(long id_camera);

    private static final native int nativeSetWhiteBlance(long id_camera, int whiteBlance);

    private static final native int nativeGetWhiteBlance(long id_camera);

    private final native int nativeUpdateBrightnessLimit(long id_camera);

    private static final native int nativeSetBrightness(long id_camera, int brightness);

    private static final native int nativeGetBrightness(long id_camera);

    private final native int nativeUpdateContrastLimit(long id_camera);

    private static final native int nativeSetContrast(long id_camera, int contrast);

    private static final native int nativeGetContrast(long id_camera);

    private final native int nativeUpdateSharpnessLimit(long id_camera);

    private static final native int nativeSetSharpness(long id_camera, int sharpness);

    private static final native int nativeGetSharpness(long id_camera);

    private final native int nativeUpdateGainLimit(long id_camera);

    private static final native int nativeSetGain(long id_camera, int gain);

    private static final native int nativeGetGain(long id_camera);

    private final native int nativeUpdateGammaLimit(long id_camera);

    private static final native int nativeSetGamma(long id_camera, int gamma);

    private static final native int nativeGetGamma(long id_camera);

    private final native int nativeUpdateSaturationLimit(long id_camera);

    private static final native int nativeSetSaturation(long id_camera, int saturation);

    private static final native int nativeGetSaturation(long id_camera);

    private final native int nativeUpdateHueLimit(long id_camera);

    private static final native int nativeSetHue(long id_camera, int hue);

    private static final native int nativeGetHue(long id_camera);

    private static final native int nativeSetPowerlineFrequency(long id_camera, int frequency);

    private static final native int nativeGetPowerlineFrequency(long id_camera);

    private final native int nativeUpdateZoomLimit(long id_camera);

    private static final native int nativeSetZoom(long id_camera, int zoom);

    private static final native int nativeGetZoom(long id_camera);
}