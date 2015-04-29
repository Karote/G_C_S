package com.coretronic.drone.main;

import android.app.Application;

/**
 * Created by jiaLian on 15/4/1.
 */
public class DroneG2Application extends Application {
    public enum SettingType {JOYPAD_MODE, LEFT_HANDED, LENGTH}

    public static boolean[] isSettings = new boolean[SettingType.LENGTH.ordinal()];
    public static boolean isSettingsMode = false;
    public static boolean isJoypadMode = true;
    public static int joyStickRadius = 0;

    @Override
    public void onCreate() {
        super.onCreate();
//        startService(new Intent(this, SocketService.class));
        isSettings[SettingType.JOYPAD_MODE.ordinal()] = true;
        isSettings[SettingType.LEFT_HANDED.ordinal()] = false;
    }


}
