package com.coretronic.drone.communication;

import android.content.Context;
import android.util.Log;

/**
 * Created by jiaLian on 15/4/22.
 */
public class FlightControlSocketThread extends SocketThread {
    private static final String TAG = FlightControlSocketThread.class.getSimpleName();

    public FlightControlSocketThread(Context context, String dstAddress, int dstPort) {
        super(context, dstAddress, dstPort);
    }

//    public FlightControlSocketThread(String dstAddress, int dstPort) {
//        super(dstAddress, dstPort);
//    }

    @Override
    public void onReceiver(byte[] readBuffer) {
        Log.v(TAG, "onReceiver: " + readBuffer);
    }
}
