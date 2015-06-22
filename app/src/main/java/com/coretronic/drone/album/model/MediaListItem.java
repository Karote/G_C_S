package com.coretronic.drone.album.model;

import android.util.Log;
import com.coretronic.drone.ambarlla.message.FileItem;
import com.coretronic.drone.utility.AppUtils;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
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


    public MediaListItem(FileItem fileItem)
    {

        SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat toFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            Date date = fromFormat.parse(fileItem.getMediaDate());
            this.mediaDate =  toFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        this.mediaDate =  fileItem.getMediaDate() + "";
        this.mediaFileName = fileItem.getMediaFileName();
        this.mediaSize = AppUtils.readableFileSize(fileItem.getMediaSize());

    }




}
