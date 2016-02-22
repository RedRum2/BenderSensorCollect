package com.example.bender.bendersensorcollect.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sensor_database.db";
    private static final int DATABASE_VERSION = 1;

    // this is the SQL statement for the creation of the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE sensor_collector (" +
            "_id TEXT NOT NULL," +
            " date TEXT NOT NULL," +
            " time TEXT NOT NULL," +
            " device TEXT NOT NULL," +
            " data TEXT NOT NULL);";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This method is called during the creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // This method is called during the upgrade of the database, for example when the version's number is increment
    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {

        database.execSQL("DROP TABLE IF EXISTS sensor_collector");
        onCreate(database);

    }
}