package com.coretronic.drone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by jiaLian on 15/4/9.
 */
public class WifiRssiReceiver extends BroadcastReceiver {
    private static final String TAG = WifiRssiReceiver.class.getSimpleName();
    private static final int NUMBER_OF_LEVELS = 5;
    private ImageView imageView;

    public WifiRssiReceiver(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final WifiManager wifiManager = (WifiManager) imageView.getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), NUMBER_OF_LEVELS);
        Log.d(TAG, ssid + " RSSI: " + level);
    }
}
