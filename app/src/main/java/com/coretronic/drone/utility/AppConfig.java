package com.coretronic.drone.utility;

import android.os.Environment;

/**
 * Created by james on 15/6/23.
 */
public class AppConfig {
    public static String ALBUM_PATH = "/CoretronicDrone/";
    public static String SHAREDPREFERENCE_ID = "CoretronicDrone";
    public static String PREF_LOGFILE_NAME = "logFileName";
    public static String PREF_MISSION_LIST = "missionList";
    public static String MISSION_LOG_START = "missionLogStart";
    public static String MISSION_LOG_STOP = "missionLogStop";

    public static String getMediaFolderPosition() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() + ALBUM_PATH;
    }
}
