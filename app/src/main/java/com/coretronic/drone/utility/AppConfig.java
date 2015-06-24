package com.coretronic.drone.utility;

import android.content.Context;
import android.os.Environment;

/**
 * Created by james on 15/6/23.
 */
public class AppConfig {
    public static String SERVER_IP ="192.168.42.1";
    public static String COMMAND_PORT = "7878";
    public static String DATA_PORT = "8787";

    public static String ALBUM_PATH_SD_CARD = "/CoretronicDrone/";

    public static String getMediaFolderPosition(Context context)
    {

//       return context.getExternalCacheDir()+ ALBUM_PATH_SD_CARD;
       return  Environment.getExternalStoragePublicDirectory(
               Environment.DIRECTORY_DCIM).getAbsolutePath()+ ALBUM_PATH_SD_CARD;
    }
}
