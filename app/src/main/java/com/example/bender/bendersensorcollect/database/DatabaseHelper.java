package com.example.bender.bendersensorcollect.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sensor_database.db";
    private static final int DATABASE_VERSION = 1;

    // Lo statement SQL di creazione del database
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

    // Questo metodo viene chiamato durante la creazione del database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // Questo metodo viene chiamato durante l'upgrade del database, ad esempio quando viene incrementato il numero di versione
    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {

        database.execSQL("DROP TABLE IF EXISTS sensor_collector");
        onCreate(database);

    }
}