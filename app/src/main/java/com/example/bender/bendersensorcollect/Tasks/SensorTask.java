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


public class SensorTask extends AsyncTask<Void, SensorEvent, Void> {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DbAdapter mDbHelper;
    private String _id;


    public SensorTask(String id, Sensor sensor, SensorManager msm, DbAdapter db)
    {
        mSensor = sensor;
        mSensorManager = msm;
        mDbHelper = db;
        _id = id;
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            mSensorManager.unregisterListener(this);
            registerValues(event);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    };


    private void registerValues(SensorEvent event) {
        String date = DateFormat.getDateInstance().format(new Date());
        String time = DateFormat.getTimeInstance().format(new Date());
        String sensorData = "";
        for (Float value : event.values)
            sensorData += (String.valueOf(value) + ";");
        mDbHelper.insertValue(_id, date, time, mSensor.getName(), sensorData);
    }


    @Override
    protected Void doInBackground(Void... params) {
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSensorEventListener != null)
                    mSensorManager.unregisterListener(mSensorEventListener);
            }
        }, 5000);

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        mSensorManager.unregisterListener(mSensorEventListener);
    }

}
