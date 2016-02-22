package com.example.bender.bendersensorcollect.Tasks;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.example.bender.bendersensorcollect.database.DbAdapter;

import java.text.DateFormat;
import java.util.Date;

//create the AsyncTask to register value from sensor and prepare record for database
public class SensorTask extends AsyncTask<Void, SensorEvent, Void> {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DbAdapter mDbHelper;
    private String _id;
    private long period;


    public SensorTask(String id, long p, Sensor sensor, SensorManager msm, DbAdapter db)
    {
        mSensor = sensor;
        mSensorManager = msm;
        mDbHelper = db;
        _id = id;
        period = p;
    }

    //this is the method used to register a new value from sensor
    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            mSensorManager.unregisterListener(this);    //unregister the Listener to stop new values capture
            registerValues(event);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };

    //Prepare record fields and insert it into the database
    private void registerValues(SensorEvent event) {
        String date = DateFormat.getDateInstance().format(new Date());
        String time = DateFormat.getTimeInstance().format(new Date());
        String sensorData = "";
        for (Float value : event.values)
            sensorData += (String.valueOf(value) + ";");
        mDbHelper.insertValue(_id, date, time, mSensor.getName(), sensorData);  //insert the record into the database
    }

    //a new thread use this method to register the listener to a sensor
    @Override
    protected Void doInBackground(Void... params) {
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //remove the listener that is active yet after [period] seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSensorEventListener != null)
                    mSensorManager.unregisterListener(mSensorEventListener);
            }
        }, period);

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        //this method unregister the listener
        mSensorManager.unregisterListener(mSensorEventListener);
    }

}
