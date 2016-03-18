package com.coretronic.drone.util;

import android.os.Environment;

/**
 * Created by james on 15/6/23.
 */
public class AppConfig {

    public final static String ALBUM_PATH = "/CoretronicDrone/";
    public final static String PREF_FLAT_TRIM_LAST_TIME = "flat_trim_time";

    public final static String SHARED_PREFERENCE_USER_MAIL_KEY = "user_id";
    public final static String SHARED_PREFERENCE_USER_PASSWORD_KEY = "user_passwd";
    public final static String SHARED_PREFERENCE_USER_STAY_LOGIN_KEY = "stay_logged";

    public final static String SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT = "altitude_default_for_waypoint";
    public final static String SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT = "horizontal_speed_default_for_waypoint";
    public final static String SHARED_PREFERENCE_SHOW_FLIGHT_ROUTE = "show_flight_route";

    public static String getMediaFolderPosition() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() + ALBUM_PATH;
    }
}
