package com.coretronic.drone.album.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by james on 15/6/12.
 */
public class MediaListItem implements Serializable {
    private String mediaDate = null;
    private String mediaFileName = "";
    private String mediaSize = "";


    public MediaListItem( String mediaFileName,String mediaSize, String date)
    {
        super();
        this.mediaDate = date;
        this.mediaFileName = mediaFileName;
        this.mediaSize = mediaSize;
    }

    public String getMediaDate() {
        return mediaDate;
    }
    public void setMediaDate(String mediaDate) {
        this.mediaDate = mediaDate;
    }

    public String getMediaFileName() {
        return mediaFileName;
    }
    public void setMediaFileName(String mediaFileName) {
        this.mediaFileName = mediaFileName;
    }

    public String getMediaSize() {
        return mediaSize;
    }
    public void setMediaSize(String mediaSize) {
        this.mediaSize = mediaSize;
    }
}
