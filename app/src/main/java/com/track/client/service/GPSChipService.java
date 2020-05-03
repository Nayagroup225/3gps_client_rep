package com.track.client.service;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.track.client.model.BaseRes;
import com.track.client.preferences.AppSharedPreference;
import com.track.client.retrofit.ApiCall;
import com.track.client.retrofit.IApiCallback;

import retrofit2.Response;

public class GPSChipService extends Service implements IApiCallback<BaseRes>, LocationListener {

    protected LocationManager locationManager;
//    GoogleApiClient mGoogleApiClient;
//    LocationRequest mLocationRequest;
    public Handler mHandler;
    public boolean isWaitingResponse = false;
    AppSharedPreference preferences;

    long stepTimeForSendPosition = 30000;//ms
    long lastSentPositionTime = 0;


    FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();
        preferences = AppSharedPreference.getInstance(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        buildGoogleApiClient();
        startTrackGps();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification());
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Runnable r = () -> {
            while (true) {
                if (!isWaitingResponse) {
                    ApiCall.getInstance().checkCurrentState(preferences.getDeviceId(), GPSChipService.this);
                    isWaitingResponse = true;
//                    findCurrentLocation();
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    Log.e("Error", e.toString());
                }
            }
        };
        new Thread(r).start();
        return START_STICKY;
    }

    void startTrackGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        }
    }

    @Override
    public void onSuccess(String type, Response<BaseRes> response) {
        if (response.isSuccessful()) {
            if (type.equals("state")) {
                if (!response.body().getErrorCode().equals("1")) {
                    String[] stateArray = response.body().getErrorMsg().split(",");
                    Log.e("state", response.body().getErrorMsg());
                    AppSharedPreference.getInstance(this).setGpsState(stateArray[0]);
                    AppSharedPreference.getInstance(this).setSmsState(stateArray[1]);
                    AppSharedPreference.getInstance(this).setRecordState(stateArray[2]);
                }
            }
        }
        isWaitingResponse = false;
    }

    private void findCurrentLocation() {
        if (lastSentPositionTime == 0 || (System.currentTimeMillis() - lastSentPositionTime) >= stepTimeForSendPosition) {

            try {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                if (AppSharedPreference.getInstance(getApplicationContext()).getGpsState().equals("1")) {
                                    lastSentPositionTime = System.currentTimeMillis();
                                    Toast.makeText(getApplicationContext(), mLastKnownLocation.getLatitude() + " : " + mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                                    ApiCall.getInstance().sendCurrentPosition(preferences.getDeviceId(), String.valueOf(mLastKnownLocation.getLatitude()), String.valueOf(mLastKnownLocation.getLongitude()), GPSChipService.this);
                                }
                            }
                        } else {
                            Log.e("location", "failed");
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    }

//    protected synchronized void buildGoogleApiClient(){
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        mGoogleApiClient.connect();
//    }

    @Override
    public void onLocationChanged(Location location) {
        if (AppSharedPreference.getInstance(this).getGpsState().equals("1")) {
//            if (lastSentPositionTime == 0 || (System.currentTimeMillis() - lastSentPositionTime) >= stepTimeForSendPosition) {
//                lastSentPositionTime = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), location.getLatitude() + " : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                ApiCall.getInstance().sendCurrentPosition(preferences.getDeviceId(), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), GPSChipService.this);
//            }
        }
    }

//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(10000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Toast.makeText(getApplicationContext(), "connection susupended.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Toast.makeText(getApplicationContext(), "connection failed.", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onFailure(Object data) {
        isWaitingResponse = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Do not forget to unregister the receiver!!!
    }

    class CheckStateThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (!isWaitingResponse) {
                    ApiCall.getInstance().checkCurrentState(preferences.getDeviceId(), GPSChipService.this);
                    isWaitingResponse = true;
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    Log.e("Error", e.toString());
                }
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}
