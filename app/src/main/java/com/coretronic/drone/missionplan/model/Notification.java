package com.coretronic.drone.missionplan.model;

/**
 * Created by karot.chuang on 2016/3/2.
 */
public class Notification {
    private int mType;
    private String mContent;
    private long mTime;

    public Notification(int type, String content, long time) {
        mType = type;
        mContent = content;
        mTime = time;
    }

    public int getType() {
        return mType;
    }

    public String getContent() {
        return mContent;
    }

    public long getTime() {
        return mTime;
    }
}
