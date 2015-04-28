package com.coretronic.drone.communication;

import android.content.Context;
import android.util.Log;

/**
 * Created by jiaLian on 15/4/22.
 */
public class MediaSocketThread extends SocketThread {
    private static final String TAG = MediaSocketThread.class.getSimpleName();

    public MediaSocketThread(Context context, String dstAddress, int dstPort) {
        super(context, dstAddress, dstPort);
    }


    @Override
    public void onReceiver(byte[] readBuffer) {
        Log.v(TAG, "onReceiver: " + readBuffer);
    }
}
