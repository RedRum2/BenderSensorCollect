package com.example.bender.bendersensorcollect.Tasks;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.example.bender.bendersensorcollect.MainActivity;
import com.example.bender.bendersensorcollect.database.DbAdapter;

import java.text.DateFormat;
import java.util.Date;

public class DeviceTask extends AsyncTask<Void, NetworkInfo, Void> {

    private LocationManager mLocationManager;
    private DbAdapter mDbHelper;

    private Context context;
    private String _id;
    private long period;


    //the class constructor
    public DeviceTask(String id, long p, DbAdapter db, Context c) {

        mDbHelper = db;
        context = c;
        _id = id;
        period = p;
    }

    //listener which allows you to check whether the GPS is on or off
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        //method that recognizes if the GPS is active
        @Override
        public void onProviderEnabled(String provider) {
            registerValues(true, "GPS");
        }

        //method that recognizes if the GPS is not active
        @Override
        public void onProviderDisabled(String provider) {
            registerValues(false, "GPS");
        }

        @Override
        public void onLocationChanged(Location location) {
        }

    };


    //in doInBackground take the states of sensors and inserts them into the database
    @Override
    protected Void doInBackground(Void... params) {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Boolean bt, wifi, mobile, networkState;
        bt = wifi = mobile = false;

        //you see if your device is connected to the Internet
        if (MainActivity.isConnectedToInternet()) {
            //List of network sensors in the device
            Network[] networks = mConnectivityManager.getAllNetworks();

            //loop that scans all networks that are on the list to see if they are active or not
            for (Network network : networks) {
                NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(network);
                networkState = networkInfo.isAvailable() && networkInfo.isConnected();
                switch (networkInfo.getType()) {
                    case (ConnectivityManager.TYPE_BLUETOOTH):
                        bt = networkState;
                    case (ConnectivityManager.TYPE_WIFI):
                        wifi = networkState;
                    case (ConnectivityManager.TYPE_MOBILE):
                        mobile = networkState;
                        break;
                    default:
                        continue;
                }

                if (isCancelled())
                    return null;
            }

        }

        registerValues(bt, "BLUETOOTH");
        registerValues(wifi, "WIFI");
        registerValues(mobile, "MOBILE");


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            String providerId = LocationManager.GPS_PROVIDER;

            mLocationManager.requestLocationUpdates(providerId, 0, 0, mLocationListener, Looper.getMainLooper());

            //remove the listener that is active yet after [period] seconds
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        mLocationManager.removeUpdates(mLocationListener);
                }
            }, period);

        }

        return null;
    }


    //Prepare record fields and insert it into the database
    private void registerValues(Boolean deviceConnected, String deviceName) {
        String date = DateFormat.getDateInstance().format(new Date());
        String time = DateFormat.getTimeInstance().format(new Date());
        String DeviceData;


        if (deviceConnected)
            DeviceData = "1;";
        else
            DeviceData = "0;";

        mDbHelper.insertValue(_id, date, time, deviceName, DeviceData);

    }

    //closes the GPS listener
    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLocationManager.removeUpdates(mLocationListener);
    }
}
