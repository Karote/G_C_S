package com.coretronic.drone.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coretronic.ibs.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2016/1/11.
 */
public class LoadMissionDataAccessObject {
    public static final String TABLE_NAME = "loadmission";

    public final static String KEY_ID = "_id";
    public final static String DATE_YEAR_COLUMN = "dateYear";
    public final static String DATE_MONTH_COLUMN = "dateMonth";
    public final static String DATE_DAY_COLUMN = "dateDay";
    public final static String DATE_HOUR_COLUMN = "dateHour";
    public final static String DATE_MINUTE_COLUMN = "dateMinute";
    public final static String DATE_SECOND_COLUMN = "dateSecond";
    public final static String DISTANCE_COLUMN = "distance";
    public final static String FLIGHT_TIME_COLUMN = "flightTime";
    public final static String MISSION_CONTENT_COLUMN = "missionContent";
    public final static String IMAGE_CONTENT_COLUMN = "imageContent";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATE_YEAR_COLUMN + " INTEGER NOT NULL, " +
                    DATE_MONTH_COLUMN + " INTEGER NOT NULL, " +
                    DATE_DAY_COLUMN + " INTEGER NOT NULL, " +
                    DATE_HOUR_COLUMN + " INTEGER NOT NULL, " +
                    DATE_MINUTE_COLUMN + " INTEGER NOT NULL, " +
                    DATE_SECOND_COLUMN + " INTEGER NOT NULL, " +
                    DISTANCE_COLUMN + " REAL NOT NULL, " +
                    FLIGHT_TIME_COLUMN + " INTEGER NOT NULL, " +
                    MISSION_CONTENT_COLUMN + " TEXT NOT NULL, " +
                    IMAGE_CONTENT_COLUMN + " BLOB)";

    private SQLiteDatabase db;

    public LoadMissionDataAccessObject(Context context) {
        db = LoadMissionDBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

    public MissionLists insert(MissionLists item) {
        ContentValues cv = new ContentValues();

        cv.put(DATE_YEAR_COLUMN, item.getDateYear());
        cv.put(DATE_MONTH_COLUMN, item.getDateMonth());
        cv.put(DATE_DAY_COLUMN, item.getDateDay());
        cv.put(DATE_HOUR_COLUMN, item.getDateHour());
        cv.put(DATE_MINUTE_COLUMN, item.getDateMinute());
        cv.put(DATE_SECOND_COLUMN, item.getDateSecond());
        cv.put(DISTANCE_COLUMN, item.getDistance());
        cv.put(FLIGHT_TIME_COLUMN, item.getFlightTime());
        cv.put(MISSION_CONTENT_COLUMN, item.getMissionContent());
        cv.put(IMAGE_CONTENT_COLUMN, item.getImageContent());

        long id = db.insert(TABLE_NAME, null, cv);
        item.setId(id);
        return item;
    }

    public boolean update(MissionLists item) {
        ContentValues cv = new ContentValues();

        cv.put(DATE_YEAR_COLUMN, item.getDateYear());
        cv.put(DATE_MONTH_COLUMN, item.getDateMonth());
        cv.put(DATE_DAY_COLUMN, item.getDateDay());
        cv.put(DATE_HOUR_COLUMN, item.getDateHour());
        cv.put(DATE_MINUTE_COLUMN, item.getDateMinute());
        cv.put(DATE_SECOND_COLUMN, item.getDateSecond());
        cv.put(DISTANCE_COLUMN, item.getDistance());
        cv.put(FLIGHT_TIME_COLUMN, item.getFlightTime());
        cv.put(MISSION_CONTENT_COLUMN, item.getMissionContent());
        cv.put(IMAGE_CONTENT_COLUMN, item.getImageContent());

        String where = KEY_ID + "=" + item.getId();

        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean delete(long id) {
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public List<MissionLists> getAll() {
        List<MissionLists> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, DATE_YEAR_COLUMN + " DESC, " + DATE_MONTH_COLUMN + " DESC", null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public MissionLists get(long id) {
        MissionLists item = null;
        String where = KEY_ID + "=" + id;

        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);

        if (result.moveToFirst()) {
            item = getRecord(result);
        }

        result.close();
        return item;
    }

    public MissionLists getRecord(Cursor cursor) {
        MissionLists.Builder resultBulider = new MissionLists.Builder();
        MissionLists result = resultBulider.create();

        result.setId(cursor.getLong(0));
        result.setDateYear(cursor.getInt(1));
        result.setDateMonth(cursor.getInt(2));
        result.setDateDay(cursor.getInt(3));
        result.setDateHour(cursor.getInt(4));
        result.setDateMinute(cursor.getInt(5));
        result.setDateSecond(cursor.getInt(6));
        result.setDistance(cursor.getFloat(7));
        result.setFlightTime(cursor.getInt(8));
        result.setMissionContent(cursor.getString(9));
        result.setImageContent(cursor.getBlob(10));

        return result;
    }

    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

}
