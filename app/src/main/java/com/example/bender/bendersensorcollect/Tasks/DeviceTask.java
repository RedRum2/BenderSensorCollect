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

    private ConnectivityManager mConnectivityManager;
    private LocationManager mLocationManager;
    private DbAdapter mDbHelper;

    private Context context;
    private String _id;


    public DeviceTask(String id, ConnectivityManager cm, LocationManager lm, DbAdapter db, Context c) {
        mConnectivityManager = cm;
        mLocationManager = lm;
        mDbHelper = db;
        context = c;
        _id = id;
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            registerValues(true, "GPS");
        }

        @Override
        public void onProviderDisabled(String provider) {
            registerValues(false, "GPS");
        }

        @Override
        public void onLocationChanged(Location location) {
        }

    };


    @Override
    protected Void doInBackground(Void... params) {

        Boolean bt, wifi, mobile, networkState;
        bt = wifi = mobile = false;

        if (MainActivity.isConnectedToInternet()) {
            Network[] networks = mConnectivityManager.getAllNetworks();

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

            String providerId = LocationManager.GPS_PROVIDER;

            mLocationManager.requestLocationUpdates(providerId, 0, 0, mLocationListener, Looper.getMainLooper());

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        mLocationManager.removeUpdates(mLocationListener);
                }
            }, 5000);

        }

        return null;
    }



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

}
