package com.coretronic.drone.uvc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.coretronic.uvc.R;
import com.serenegiant.usb.DeviceFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Poming on 2015/10/23.
 */
public class USBCameraMonitor {

    private final static String ACTION_USB_PERMISSION_BASE = "com.serenegiant.USB_PERMISSION.";
    private final static String ACTION_USB_PERMISSION = ACTION_USB_PERMISSION_BASE + USBCameraMonitor.class.getSimpleName();

    private static USBCameraMonitor sInstance;

    private final Activity mActivity;
    private final ConcurrentHashMap<UsbDevice, UsbCameraWrap> mCameraWrapMap = new ConcurrentHashMap<>();
    private final UsbManager mUsbManager;
    private OnUVCCameraStatusChangedListener mOnUVCCameraStatusChangedListener;
    private BroadcastReceiver mUSBBroadcastReceiver;

    public static USBCameraMonitor init(Activity activity) {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (USBCameraMonitor.class) {
            if (sInstance != null) {
                return sInstance;
            }
            sInstance = new USBCameraMonitor(activity);
        }
        return sInstance;
    }

    public USBCameraMonitor(final Activity activity) {
        mActivity = activity;
        mUsbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
    }

    public static USBCameraMonitor getInstance() {
        return sInstance;
    }

    public synchronized void requestPermission(final UsbDevice device) {

        if (mOnUVCCameraStatusChangedListener == null) {
            return;
        }

        if (mUSBBroadcastReceiver == null || device == null) {
            onCancel();
            return;
        }
        if (mUsbManager.hasPermission(device)) {
            onPermissionGranted(device);
            return;
        }
        mUsbManager.requestPermission(device, PendingIntent.getBroadcast(mActivity, 0, new Intent(ACTION_USB_PERMISSION), 0));
    }

    private synchronized void registerUSBAction() {
        if (mUSBBroadcastReceiver != null) {
            return;
        }
        mUSBBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mOnUVCCameraStatusChangedListener == null) {
                    return;
                }
                final String action = intent.getAction();
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (ACTION_USB_PERMISSION.equals(action)) {
                    if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        onCancel();
                        return;
                    }
                    onPermissionGranted(device);
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    onAttach(device);
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    onDetach(device);
                }
            }
        };
        final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mActivity.registerReceiver(mUSBBroadcastReceiver, filter);
    }

    private void unregisterUSBAction() {
        if (mUSBBroadcastReceiver == null) {
            return;
        }
        mActivity.unregisterReceiver(mUSBBroadcastReceiver);
        mUSBBroadcastReceiver = null;
    }

    public void openCamera(UsbDevice usbDevice) {

        UsbCameraWrap usbCameraWrap = mCameraWrapMap.get(usbDevice);
        if (usbCameraWrap == null) {
            usbCameraWrap = new UsbCameraWrap(usbDevice, mUsbManager);
            mCameraWrapMap.put(usbDevice, usbCameraWrap);
        }
        onCameraOpened(usbCameraWrap);
    }

    private void onCameraOpened(UsbCameraWrap usbCameraWrap) {
        if (mOnUVCCameraStatusChangedListener != null) {
            mOnUVCCameraStatusChangedListener.onCameraOpened(usbCameraWrap);
        }
    }

    private void onPermissionGranted(UsbDevice usbDevice) {
        if (mOnUVCCameraStatusChangedListener != null) {
            mOnUVCCameraStatusChangedListener.onPermissionGranted(usbDevice);
        }
    }

    private void onDetach(UsbDevice usbDevice) {
        if (mOnUVCCameraStatusChangedListener != null) {
            mOnUVCCameraStatusChangedListener.onDetach(usbDevice);
        }
    }

    private void onCancel() {
        if (mOnUVCCameraStatusChangedListener != null) {
            mOnUVCCameraStatusChangedListener.onCancel();
        }
    }

    private void onAttach(UsbDevice usbDevice) {
        if (mOnUVCCameraStatusChangedListener != null) {
            mOnUVCCameraStatusChangedListener.onAttach(usbDevice);
        }
    }

    public void registerOnCameraDeviceStatusChangedListener(OnUVCCameraStatusChangedListener OnUVCCameraStatusChangedListener) {
        mOnUVCCameraStatusChangedListener = OnUVCCameraStatusChangedListener;
        registerUSBAction();
        List<UsbDevice> attachedUsbDevices = getUVCCamera();
        if (attachedUsbDevices != null && attachedUsbDevices.size() > 0) {
            mOnUVCCameraStatusChangedListener.onAttach(attachedUsbDevices.get(0));
        }
    }

    public void unregisterOnCameraDeviceStatusChangedListener() {
        mOnUVCCameraStatusChangedListener = null;
        unregisterUSBAction();
    }

    public List<UsbDevice> getUVCCamera() {
        return getDeviceList(DeviceFilter.getDeviceFilters(mActivity, R.xml.device_filter).get(0));
    }

    private List<UsbDevice> getDeviceList(final DeviceFilter filter) {
        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<>();
        if (deviceList != null) {
            final Iterator<UsbDevice> iterator = deviceList.values().iterator();
            UsbDevice device;
            while (iterator.hasNext()) {
                device = iterator.next();
                if ((filter == null) || (filter.matches(device))) {
                    result.add(device);
                }
            }
        }
        return result;
    }

    public static void onDestroy() {
        if (sInstance == null) {
            return;
        }
        sInstance.unregisterOnCameraDeviceStatusChangedListener();
        sInstance.destroy();
    }

    private void destroy() {
        final Set<UsbDevice> keys = mCameraWrapMap.keySet();
        if (keys == null) {
            return;
        }
        UsbCameraWrap usbCamera;
        for (final UsbDevice key : keys) {
            usbCamera = mCameraWrapMap.remove(key);
            usbCamera.close();
        }
        mCameraWrapMap.clear();
    }

    public interface OnUVCCameraStatusChangedListener {

        void onAttach(UsbDevice device);

        void onDetach(UsbDevice device);

        void onPermissionGranted(UsbDevice usbDevice);

        void onCameraOpened(UsbCameraWrap uvcCameraWrap);

        void onCancel();
    }

}

