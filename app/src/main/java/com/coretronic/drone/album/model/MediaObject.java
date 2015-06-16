package com.coretronic.drone.album.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by james on 15/6/5.
 * using pass custom class to other page(bundle)
 */
public class MediaObject implements Serializable {
    private static ArrayList<MediaItem> mediaItems;

    public void setImageItems(ArrayList<MediaItem> mediaItems) {
        MediaObject.mediaItems = mediaItems;
    }
    public ArrayList<MediaItem> getImageItems() {
        return mediaItems;
    }
}
