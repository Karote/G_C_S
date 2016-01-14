package com.coretronic.drone.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by karot.chuang on 2016/1/11.
 */
public class LoadMissionDBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "loadmission.db";
    private final static int VERSION = 1;
    private static SQLiteDatabase database;

    public LoadMissionDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getDatabase(Context context) {
        if (database == null || !database.isOpen()) {
            database = new LoadMissionDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
        }

        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LoadMissionDataAccessObject.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LoadMissionDataAccessObject.TABLE_NAME);
        onCreate(db);
    }
}
