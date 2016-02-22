package com.example.bender.bendersensorcollect.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DbAdapter.class.getSimpleName();

    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // Database fields
    private static final String DATABASE_TABLE = "sensor_collector";

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_DEVICE = "device";
    public static final String KEY_DATA = "data";

    public DbAdapter(Context context) {
        this.context = context;
    }

    public DbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    private ContentValues createContentValues(String _id, String date, String time, String device, String data) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, _id);
        values.put(KEY_DATE, date);
        values.put(KEY_TIME, time);
        values.put(KEY_DEVICE, device);
        values.put(KEY_DATA, data);

        return values;
    }

    //create a sensor_data
    public long createSensorData(String _id, String date, String time, String device, String data) {
        ContentValues initialValues = createContentValues(_id, date, time, device, data);
        return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
    }

    //fetch all sensor_data
    public Cursor fetchAllSensorData() {
        return database.query(DATABASE_TABLE,
                new String[]{KEY_ID, KEY_DATE, KEY_TIME, KEY_DEVICE, KEY_DATA},
                null, null, null, null, null);
    }

    public void delete()
    {
        String sqltext="DELETE FROM " + DATABASE_TABLE;
        database.execSQL(sqltext);
    }

    synchronized public void insertValue(String _id, String date, String time, String device, String data) {
        this.open();
        this.createSensorData(_id, date, time, device, data);
//        this.close();
    }
}