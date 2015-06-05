package com.coretronic.drone.album.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by james on 15/6/5.
 */
public class MediaObject implements Serializable {
    private static ArrayList<ImageItem> imageItems;

    public void setImageItems(ArrayList<ImageItem> imageItems) {
        MediaObject.imageItems = imageItems;
    }
    public ArrayList<ImageItem> getImageItems() {
        return imageItems;
    }
}
