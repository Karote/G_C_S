package com.coretronic.drone.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coretronic.drone.missionplan.model.LoadPlanningDataAccessObject;

/**
 * Created by karot.chuang on 2016/1/11.
 */
public class LoadPlanningDBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "loadPlanning.db";
    private final static int VERSION = 1;
    private static SQLiteDatabase database;

    public LoadPlanningDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new LoadPlanningDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LoadPlanningDataAccessObject.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LoadPlanningDataAccessObject.TABLE_NAME);
        onCreate(db);
    }
}
