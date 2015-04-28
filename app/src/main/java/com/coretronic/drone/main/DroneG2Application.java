package com.coretronic.drone.main;

import android.app.Application;

/**
 * Created by jiaLian on 15/4/1.
 */
public class DroneG2Application extends Application {
    public static enum SettingType {JOYPAD_MODE, LEFT_HANDED, LENGTH}

    public static boolean[] isSettings = new boolean[SettingType.LENGTH.ordinal()];
    public static boolean isSettingsMode = false;
    public static boolean isJoypadMode = true;

    @Override
    public void onCreate() {
        super.onCreate();
//        startService(new Intent(this, SocketService.class));
    }
}
