package com.coretronic.drone.album.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by james on 15/6/1.
 */
public class ImageItem {

    private String mediaPath;
    private Date mediaDate;
    private String mediaTitle;
    private long mediaId;
    private int mediaType;
    private boolean isMediaSelect = false;

    public ImageItem(String mediaPath, Date date, String mediaTitle, long imgId, int mediaType, boolean isMediaSelect)
    {
        super();
        this.mediaTitle = mediaTitle;
        this.mediaPath = mediaPath;
        this.mediaDate = date;
        this.mediaId = imgId;
        this.mediaType = mediaType;
        this.isMediaSelect = isMediaSelect;
    }



    public String getMediaPath() {
        return mediaPath;
    }
    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public Date getMediaDate() {
        return mediaDate;
    }
    public void setMediaDate(Date mediaDate) {
        this.mediaDate = mediaDate;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }
    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public long getMediaId() {
        return mediaId;
    }
    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public int getMediaType() {
        return mediaType;
    }
    public void setMediaType(int imgType) {
        this.mediaType = imgType;
    }

    public boolean getIsMediaSelect() {
        return isMediaSelect;
    }
    public void setIsMediaSelect(boolean isMediaSelect) {
        this.isMediaSelect = isMediaSelect;
    }

}
