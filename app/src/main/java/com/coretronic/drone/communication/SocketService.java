package com.coretronic.drone.communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by jiaLian on 15/4/22.
 */
public class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();
    public static final String SERVER_ADDRESS = "10.100.1.15";
    public static final String ACTION_MODE = "action_mode";
    public static final int ACTION_MODE_SOCKET_STATUS = 1;
    public static final int ACTION_MODE_SOCKET_WRITE = 2;

    public static final int PORT_1 = 7878;
    public static final int PORT_2 = 8787;
    private FlightControlSocketThread flightControlSocketThread;
    private long time;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        flightControlSocketThread = new FlightControlSocketThread(getBaseContext(),SERVER_ADDRESS, PORT_1);
        flightControlSocketThread.start();
        time = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        Log.d(TAG, "isConnected: " + flightControlSocketThread.isConnected());
        if (flightControlSocketThread.isConnected()) {
            int actionMode = intent.getIntExtra(ACTION_MODE, -1);
            switch (actionMode) {
                case ACTION_MODE_SOCKET_WRITE:
                    flightControlSocketThread.write(new byte[]{'1', '2', '3', '4', '5', 'J', 'G'});
                    break;
                case ACTION_MODE_SOCKET_STATUS:
                    flightControlSocketThread.sendBroadcast();
                    break;
            }
        } else if (System.currentTimeMillis() - time > 3000) {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flightControlSocketThread.cancel();
        Log.d(TAG, "onDestroy");
    }


}
