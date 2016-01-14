package com.coretronic.drone.util;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by karot.chuang on 2015/12/30.
 */
public class MissionLists {
    private final static String KEY_ID = "_id";
    private final static String KEY_DATE_YEAR = "dateYear";
    private final static String KEY_DATE_MONTH = "dateMonth";
    private final static String KEY_DATE_DAY = "dateDay";
    private final static String KEY_DATE_HOUR = "dateHour";
    private final static String KEY_DATE_MINUTE = "dateMinute";
    private final static String KEY_DATE_SECOND = "dateSecond";
    private final static String KEY_DISTANCE = "distance";
    private final static String KEY_FLIGHT_TIME = "flightTime";
    private final static String KEY_MISSION_CONTENT = "missionContent";
    private final static String KEY_IMAGE_CONTENT = "imageContent";

    @Expose
    @SerializedName(KEY_ID)
    private long mId;

    @Expose
    @SerializedName(KEY_DATE_YEAR)
    private int mDateYear;

    @Expose
    @SerializedName(KEY_DATE_MONTH)
    private int mDateMonth;

    @Expose
    @SerializedName(KEY_DATE_DAY)
    private int mDateDay;

    @Expose
    @SerializedName(KEY_DATE_HOUR)
    private int mDateHour;

    @Expose
    @SerializedName(KEY_DATE_MINUTE)
    private int mDateMinute;

    @Expose
    @SerializedName(KEY_DATE_SECOND)
    private int mDateSecond;

    @Expose
    @SerializedName(KEY_DISTANCE)
    private float mDistance;

    @Expose
    @SerializedName(KEY_FLIGHT_TIME)
    private int mFlightTime;

    @Expose
    @SerializedName(KEY_MISSION_CONTENT)
    private String mMissionContent;

    @Expose
    @SerializedName(KEY_IMAGE_CONTENT)
    private byte[] mImageContent;

    private MissionLists(long id, float distance, int flightTime, String missionContent, byte[] imageContent) {
        this(id, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                Calendar.getInstance().get(Calendar.SECOND),
                distance, flightTime, missionContent, imageContent);
    }

    private MissionLists(long id, int year, int month, int day, int hour, int minute, int second,
                         float distance, int flightTime, String missionContent, byte[] imageContent) {
        mId = id;
        mDateYear = year;
        mDateMonth = month;
        mDateDay = day;
        mDateHour = hour;
        mDateMinute = minute;
        mDateSecond = second;
        mDistance = distance;
        mFlightTime = flightTime;
        mMissionContent = missionContent;
        mImageContent = imageContent;
    }

    @Override
    public MissionLists clone() {
        try {
            return (MissionLists) super.clone();
        } catch (CloneNotSupportedException e) {
            return new MissionLists.Builder().create();
        }
    }

    public long getId() {
        return mId;
    }

    public int getDateYear() {
        return mDateYear;
    }

    public int getDateMonth() {
        return mDateMonth;
    }

    public int getDateDay() {
        return mDateDay;
    }

    public int getDateHour() {
        return mDateHour;
    }

    public int getDateMinute() {
        return mDateMinute;
    }

    public int getDateSecond() {
        return mDateSecond;
    }

    public float getDistance() {
        return mDistance;
    }

    public long getFlightTime() {
        return mFlightTime;
    }

    public String getMissionContent() {
        return mMissionContent;
    }

    public byte[] getImageContent() {
        return mImageContent;
    }


    public MissionLists setId(long id) {
        this.mId = id;
        return this;
    }

    public MissionLists setDateYear(int dateYear) {
        this.mDateYear = dateYear;
        return this;
    }

    public MissionLists setDateMonth(int dateMonth) {
        this.mDateMonth = dateMonth;
        return this;
    }

    public MissionLists setDateDay(int dateDay) {
        this.mDateDay = dateDay;
        return this;
    }

    public MissionLists setDateHour(int dateHour) {
        this.mDateHour = dateHour;
        return this;
    }

    public MissionLists setDateMinute(int dateMinute) {
        this.mDateMinute = dateMinute;
        return this;
    }

    public MissionLists setDateSecond(int dateSecond) {
        this.mDateSecond = dateSecond;
        return this;
    }

    public MissionLists setDistance(float distance) {
        this.mDistance = distance;
        return this;
    }

    public MissionLists setFlightTime(int flightTime) {
        this.mFlightTime = flightTime;
        return this;
    }

    public MissionLists setMissionContent(String missionContent) {
        this.mMissionContent = missionContent;
        return this;
    }

    public MissionLists setImageContent(byte[] imageContent) {
        this.mImageContent = imageContent;
        return this;
    }

    @Override
    public String toString() {
        return "MissionLists{" +
                "mId=" + mId +
                ", mDateMonth=" + mDateMonth +
                ", mDateDay=" + mDateDay +
                ", mDateHour=" + mDateHour +
                ", mDateMinute=" + mDateMinute +
                ", mDateSecond=" + mDateSecond +
                ", mDistance=" + mDistance +
                ", mFlightTime=" + mFlightTime +
                ", mMissionContent=" + mMissionContent +
                '}';
    }

    public static class Builder {
        private long mId;
        private float mDistance;
        private int mFlightTime;
        private String mMissionContent;
        private byte[] mImageContent;

        public Builder setId(long id) {
            mId = id;
            return this;
        }

        public Builder setDistance(float distance) {
            mDistance = distance;
            return this;
        }

        public Builder setFlightTime(int flightTime) {
            mFlightTime = flightTime;
            return this;
        }

        public Builder setMissionContent(String missionContent) {
            mMissionContent = missionContent;
            return this;
        }

        public Builder setImageContent(byte[] imageContent) {
            mImageContent = imageContent;
            return this;
        }

        public MissionLists create() {
            return new MissionLists(mId, mDistance, mFlightTime, mMissionContent, mImageContent);
        }
    }
}
