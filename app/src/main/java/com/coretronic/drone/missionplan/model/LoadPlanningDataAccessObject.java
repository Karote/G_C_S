package com.coretronic.drone.missionplan.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coretronic.drone.util.LoadPlanningDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2016/1/11.
 */
public class LoadPlanningDataAccessObject {
    public static final String TABLE_NAME = "loadPlanning";

    public final static String KEY_ID = "_id";
    public final static String DATE_YEAR_COLUMN = "dateYear";
    public final static String DATE_MONTH_COLUMN = "dateMonth";
    public final static String DATE_DAY_COLUMN = "dateDay";
    public final static String DATE_HOUR_COLUMN = "dateHour";
    public final static String DATE_MINUTE_COLUMN = "dateMinute";
    public final static String DATE_SECOND_COLUMN = "dateSecond";
    public final static String DISTANCE_COLUMN = "distance";
    public final static String FLIGHT_TIME_COLUMN = "flightTime";
    public final static String PLANNING_CONTENT_COLUMN = "planningContent";
    public final static String IMAGE_CONTENT_COLUMN = "imageContent";
    private final static int INDEX_ID = 0;
    private final static int INDEX_DATE_YEAR = 1;
    private final static int INDEX_DATE_MONTH = 2;
    private final static int INDEX_DATE_DAY = 3;
    private final static int INDEX_DATE_HOUR = 4;
    private final static int INDEX_DATE_MINUTE = 5;
    private final static int INDEX_DATE_SECOND = 6;
    private final static int INDEX_DISTANCE = 7;
    private final static int INDEX_FLIGHT_TIME = 8;
    private final static int INDEX_PLANNING_CONTENT = 9;
    private final static int INDEX_IMAGE_CONTENT = 10;

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
                    PLANNING_CONTENT_COLUMN + " TEXT NOT NULL, " +
                    IMAGE_CONTENT_COLUMN + " BLOB)";

    private SQLiteDatabase db;

    public LoadPlanningDataAccessObject(Context context) {
        db = LoadPlanningDBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

    public PlanningData insert(PlanningData item) {
        ContentValues cv = new ContentValues();

        cv.put(DATE_YEAR_COLUMN, item.getDateYear());
        cv.put(DATE_MONTH_COLUMN, item.getDateMonth());
        cv.put(DATE_DAY_COLUMN, item.getDateDay());
        cv.put(DATE_HOUR_COLUMN, item.getDateHour());
        cv.put(DATE_MINUTE_COLUMN, item.getDateMinute());
        cv.put(DATE_SECOND_COLUMN, item.getDateSecond());
        cv.put(DISTANCE_COLUMN, item.getDistance());
        cv.put(FLIGHT_TIME_COLUMN, item.getFlightTime());
        cv.put(PLANNING_CONTENT_COLUMN, item.getPlanningContent());
        cv.put(IMAGE_CONTENT_COLUMN, item.getImageContent());

        long id = db.insert(TABLE_NAME, null, cv);
        item.setId(id);
        return item;
    }

    public boolean update(PlanningData item) {
        ContentValues cv = new ContentValues();

        cv.put(DATE_YEAR_COLUMN, item.getDateYear());
        cv.put(DATE_MONTH_COLUMN, item.getDateMonth());
        cv.put(DATE_DAY_COLUMN, item.getDateDay());
        cv.put(DATE_HOUR_COLUMN, item.getDateHour());
        cv.put(DATE_MINUTE_COLUMN, item.getDateMinute());
        cv.put(DATE_SECOND_COLUMN, item.getDateSecond());
        cv.put(DISTANCE_COLUMN, item.getDistance());
        cv.put(FLIGHT_TIME_COLUMN, item.getFlightTime());
        cv.put(PLANNING_CONTENT_COLUMN, item.getPlanningContent());
        cv.put(IMAGE_CONTENT_COLUMN, item.getImageContent());

        String where = KEY_ID + "=" + item.getId();

        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean delete(long id) {
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public List<PlanningData> getAll() {
        List<PlanningData> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null,
                DATE_YEAR_COLUMN + " DESC, "
                        + DATE_MONTH_COLUMN + " DESC, "
                        + DATE_DAY_COLUMN + " DESC, "
                        + DATE_HOUR_COLUMN + " DESC, "
                        + DATE_MINUTE_COLUMN + " DESC, "
                        + DATE_SECOND_COLUMN + " DESC",
                null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public PlanningData get(long id) {
        PlanningData item = null;
        String where = KEY_ID + "=" + id;

        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);

        if (result.moveToFirst()) {
            item = getRecord(result);
        }

        result.close();
        return item;
    }

    public PlanningData getRecord(Cursor cursor) {
        PlanningData.Builder resultBulider = new PlanningData.Builder();
        PlanningData result = resultBulider.create();

        result.setId(cursor.getLong(INDEX_ID));
        result.setDateYear(cursor.getInt(INDEX_DATE_YEAR));
        result.setDateMonth(cursor.getInt(INDEX_DATE_MONTH));
        result.setDateDay(cursor.getInt(INDEX_DATE_DAY));
        result.setDateHour(cursor.getInt(INDEX_DATE_HOUR));
        result.setDateMinute(cursor.getInt(INDEX_DATE_MINUTE));
        result.setDateSecond(cursor.getInt(INDEX_DATE_SECOND));
        result.setDistance(cursor.getFloat(INDEX_DISTANCE));
        result.setFlightTime(cursor.getInt(INDEX_FLIGHT_TIME));
        result.setPlanningContent(cursor.getString(INDEX_PLANNING_CONTENT));
        result.setImageContent(cursor.getBlob(INDEX_IMAGE_CONTENT));

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
