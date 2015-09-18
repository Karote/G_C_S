package com.coretronic.drone.utility;

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

    public static String getMediaFolderPosition() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() + ALBUM_PATH;
    }
}
