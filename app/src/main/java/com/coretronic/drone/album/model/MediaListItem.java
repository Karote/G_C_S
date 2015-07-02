package com.coretronic.drone.album.model;

import com.coretronic.drone.DroneController.MediaCommandListener.MediaContent;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by james on 15/6/12.
 */
public class MediaListItem implements Serializable {
    private String mediaDate = null;
    private String mediaFileName = "";
    private String mediaSize = "";


    public MediaListItem(String mediaFileName, String mediaSize, String date) {
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

    public MediaListItem(MediaContent mediaContent) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat toFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            Date date = fromFormat.parse(mediaContent.getCreateData());
            this.mediaDate = toFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.mediaFileName = mediaContent.getName();
        this.mediaSize = String.valueOf(mediaContent.size());

    }


}
