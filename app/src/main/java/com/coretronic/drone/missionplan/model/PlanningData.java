package com.coretronic.drone.missionplan.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by karot.chuang on 2015/12/30.
 */
public class PlanningData {
    private final static String KEY_ID = "_id";
    private final static String KEY_DATE_YEAR = "dateYear";
    private final static String KEY_DATE_MONTH = "dateMonth";
    private final static String KEY_DATE_DAY = "dateDay";
    private final static String KEY_DATE_HOUR = "dateHour";
    private final static String KEY_DATE_MINUTE = "dateMinute";
    private final static String KEY_DATE_SECOND = "dateSecond";
    private final static String KEY_DISTANCE = "distance";
    private final static String KEY_FLIGHT_TIME = "flightTime";
    private final static String KEY_PLANNING_CONTENT = "planningContent";
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
    @SerializedName(KEY_PLANNING_CONTENT)
    private String mPlanningContent;

    @Expose
    @SerializedName(KEY_IMAGE_CONTENT)
    private byte[] mImageContent;

    private PlanningData(long id, float distance, int flightTime, String planningContent, byte[] imageContent) {
        this(id, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                Calendar.getInstance().get(Calendar.SECOND),
                distance, flightTime, planningContent, imageContent);
    }

    private PlanningData(long id, int year, int month, int day, int hour, int minute, int second,
                         float distance, int flightTime, String planningContent, byte[] imageContent) {
        mId = id;
        mDateYear = year;
        mDateMonth = month;
        mDateDay = day;
        mDateHour = hour;
        mDateMinute = minute;
        mDateSecond = second;
        mDistance = distance;
        mFlightTime = flightTime;
        mPlanningContent = planningContent;
        mImageContent = imageContent;
    }

    @Override
    public PlanningData clone() {
        try {
            return (PlanningData) super.clone();
        } catch (CloneNotSupportedException e) {
            return new PlanningData.Builder().create();
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

    public String getPlanningContent() {
        return mPlanningContent;
    }

    public byte[] getImageContent() {
        return mImageContent;
    }


    public PlanningData setId(long id) {
        this.mId = id;
        return this;
    }

    public PlanningData setDateYear(int dateYear) {
        this.mDateYear = dateYear;
        return this;
    }

    public PlanningData setDateMonth(int dateMonth) {
        this.mDateMonth = dateMonth;
        return this;
    }

    public PlanningData setDateDay(int dateDay) {
        this.mDateDay = dateDay;
        return this;
    }

    public PlanningData setDateHour(int dateHour) {
        this.mDateHour = dateHour;
        return this;
    }

    public PlanningData setDateMinute(int dateMinute) {
        this.mDateMinute = dateMinute;
        return this;
    }

    public PlanningData setDateSecond(int dateSecond) {
        this.mDateSecond = dateSecond;
        return this;
    }

    public PlanningData setDistance(float distance) {
        this.mDistance = distance;
        return this;
    }

    public PlanningData setFlightTime(int flightTime) {
        this.mFlightTime = flightTime;
        return this;
    }

    public PlanningData setPlanningContent(String planningContent) {
        this.mPlanningContent = planningContent;
        return this;
    }

    public PlanningData setImageContent(byte[] imageContent) {
        this.mImageContent = imageContent;
        return this;
    }

    @Override
    public String toString() {
        return "PlanningData{" +
                "mId=" + mId +
                ", mDateMonth=" + mDateMonth +
                ", mDateDay=" + mDateDay +
                ", mDateHour=" + mDateHour +
                ", mDateMinute=" + mDateMinute +
                ", mDateSecond=" + mDateSecond +
                ", mDistance=" + mDistance +
                ", mFlightTime=" + mFlightTime +
                ", mPlanningContent=" + mPlanningContent +
                '}';
    }

    public static class Builder {
        private long mId;
        private float mDistance;
        private int mFlightTime;
        private String mPlanningContent;
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

        public Builder setPlanningContent(String planningContent) {
            mPlanningContent = planningContent;
            return this;
        }

        public Builder setImageContent(byte[] imageContent) {
            mImageContent = imageContent;
            return this;
        }

        public PlanningData create() {
            return new PlanningData(mId, mDistance, mFlightTime, mPlanningContent, mImageContent);
        }
    }
}
