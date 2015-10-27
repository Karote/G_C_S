package com.coretronic.drone.uvc;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.SparseArray;

/**
 * Created by Poming on 2015/10/26.
 */
public final class UsbCameraWrap {

    private final UsbDevice mUsbDevice;
    private final SparseArray<UsbInterface> mInterfaces;
    private UsbDeviceConnection mConnection;

    public UsbCameraWrap(final UsbDevice usbDevice, UsbManager usbManager) {
        mUsbDevice = usbDevice;
        mInterfaces = new SparseArray<>();
        mConnection = usbManager.openDevice(mUsbDevice);
    }

    public UsbDevice getDevice() {
        return mUsbDevice;
    }

    public String getDeviceName() {
        return mUsbDevice.getDeviceName();
    }

    public UsbDeviceConnection getUsbDeviceConnection() {
        return mConnection;
    }

    public synchronized int getFileDescriptor() {
        return mConnection != null ? mConnection.getFileDescriptor() : -1;
    }

    public byte[] getRawDescriptors() {
        return mConnection != null ? mConnection.getRawDescriptors() : null;
    }

    public int getVendorId() {
        return mUsbDevice.getVendorId();
    }

    public int getProductId() {
        return mUsbDevice.getProductId();
    }

    public synchronized String getSerial() {
        return mConnection != null ? mConnection.getSerial() : null;
    }

    public synchronized UsbInterface open(final int interfaceIndex) {
        UsbInterface usbInterface = mInterfaces.get(interfaceIndex);
        if (usbInterface == null) {
            usbInterface = mUsbDevice.getInterface(interfaceIndex);
            if (usbInterface != null) {
                synchronized (mInterfaces) {
                    mInterfaces.append(interfaceIndex, usbInterface);
                }
            }
        }
        return usbInterface;
    }

    public synchronized boolean close() {
        if (mConnection == null) {
            return false;
        }
        final int n = mInterfaces.size();
        for (int i = 0; i < n; i++) {
            int key = mInterfaces.keyAt(i);
            mConnection.releaseInterface(mInterfaces.get(key));
        }

        mConnection.close();
        mConnection = null;
        return true;
    }

}