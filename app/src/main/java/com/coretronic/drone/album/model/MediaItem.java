package com.coretronic.drone.album.model;

import java.util.Date;

/**
 * Created by james on 15/6/1.
 */
public class MediaItem {

    private String mediaPath = "";
    private Date mediaDate = null;
    private String mediaTitle = "";
    private long mediaId = 0;
    private int mediaType = 0;
    private String mediaDuration = "";
    private boolean isMediaSelect = false;
    private String mediaFileName = "";
    private String mediaSize = "";


    public MediaItem(String mediaPath, Date date, String mediaTitle, long imgId, int mediaType, String mediaDuration, boolean isMediaSelect) {
        super();
        this.mediaTitle = mediaTitle;
        this.mediaPath = mediaPath;
        this.mediaDate = date;
        this.mediaId = imgId;
        this.mediaType = mediaType;
        this.mediaDuration = mediaDuration;
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

    public String getMediaDuration() {
        return mediaDuration;
    }

    public void setMediaDuration(String mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public boolean getIsMediaSelect() {
        return isMediaSelect;
    }
    public void setIsMediaSelect(boolean isMediaSelect) {
        this.isMediaSelect = isMediaSelect;
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
