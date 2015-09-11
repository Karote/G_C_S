package com.coretronic.drone.utility;

import android.os.Environment;

/**
 * Created by james on 15/6/23.
 */
public class AppConfig {

    public static String ALBUM_PATH = "/CoretronicDrone/";
    public static String SHAREDPREFERENCE_ID = "coretronic_drone";
    public static String PREF_FLAT_TRIM_LAST_TIME = "flat_trim_time";

    public static String getMediaFolderPosition() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() + ALBUM_PATH;
    }
}
