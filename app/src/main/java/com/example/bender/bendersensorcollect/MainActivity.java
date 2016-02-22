package com.example.bender.bendersensorcollect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bender.bendersensorcollect.Tasks.DeviceTask;
import com.example.bender.bendersensorcollect.Tasks.SenderTask;
import com.example.bender.bendersensorcollect.Tasks.SensorTask;
import com.example.bender.bendersensorcollect.database.DbAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private SensorManager mSensorManager;
    private List<Sensor> deviceSensor;
    private TextView tvOutput;
    private ArrayList<AsyncTask> taskList;
    private Boolean tasksCancellable;
    private TimerTask doAsynchronousTask;
    private DbAdapter dbHelper;
    private static ConnectivityManager connectivityManager;
    private SenderTask senderTask;
    private Spinner mSpinner;
    private String[] spinner_list;
    private EditText etName;
    private ProgressBar progressBar;

    private static final int REQUEST_LOCATION_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        deviceSensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        dbHelper = new DbAdapter(this);

        tasksCancellable = false;

        tvOutput = (TextView) findViewById(R.id.textView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        etName = (EditText) findViewById(R.id.et_name);
        Button lista = (Button) findViewById(R.id.Lista);
        Button startI = (Button) findViewById(R.id.Start);
        Button stopI = (Button) findViewById(R.id.Stop);
        Button cleanI = (Button) findViewById(R.id.Clean);

        spinner_list = new String[]{"1", "5", "10", "15", "30", "60"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinner_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        lista.setOnClickListener(this);
        startI.setOnClickListener(this);
        stopI.setOnClickListener(this);
        cleanI.setOnClickListener(this);


        //send the data have not been sent yet
        connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (isConnectedToInternet()) {
            senderTask = new SenderTask(dbHelper, getApplicationContext());
            senderTask.execute();
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (connectivityManager == null)
//            connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//    }

    public void startScanning()
    {
        String _id = etName.getText().toString();
        taskList = new ArrayList<>();

        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        DeviceTask mDeviceTask = new DeviceTask(_id, connectivityManager, locManager, dbHelper, this);
        mDeviceTask.execute();
        taskList.add(mDeviceTask);

        for (Sensor sensor : deviceSensor)
        {
            if (sensor.getType() == Sensor.TYPE_SIGNIFICANT_MOTION) {
                continue;
            }
            SensorTask mAsyncTask = new SensorTask(_id, sensor, mSensorManager, dbHelper);
            mAsyncTask.execute();
            taskList.add(mAsyncTask);
        }

    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        long period = Long.parseLong(spinner_list[mSpinner.getSelectedItemPosition()]) * 1000;

        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (tasksCancellable)
                            cancelTasks();

                        startScanning();
                        tasksCancellable = true;

                        if (isConnectedToInternet()) {
                            senderTask = new SenderTask(dbHelper, getApplicationContext());
                            senderTask.execute();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, period);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Start:
                if (etName.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Insert your name", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    requestLocationPermissions();

                progressBar.setIndeterminate(true);
                callAsynchronousTask();
                break;
            case R.id.Lista:
                for(Sensor sensor : deviceSensor)
                    tvOutput.append(sensor.getName() + "\n");
                break;
            case R.id.Stop:
                progressBar.setIndeterminate(false);
                if (taskList != null && doAsynchronousTask != null) {
                    cancelTasks();
                    doAsynchronousTask.cancel();
                    tasksCancellable = false;
                    dbHelper.close();
                }
                break;
            case R.id.Clean:
                tvOutput.setText("");
                break;
        }
    }

    private void cancelTasks() {
        for (AsyncTask task: taskList) {
            if (task != null)
                task.cancel(true);
        }
    }

    void requestLocationPermissions() {
        String locationPermissions[] =
                {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
            ActivityCompat.requestPermissions(this, locationPermissions, REQUEST_LOCATION_PERMISSION);
    }

    public static boolean isConnectedToInternet() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null)
            return (info.isAvailable() && info.isConnected());
        //Info is null if no default network is currently active
        return false;
    }

}
