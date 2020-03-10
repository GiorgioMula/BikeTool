package com.giorgiomula.biketools;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MainActivity extends Activity {

    TextView speedTextView;
    TextView distanceTextView;
    TextView accellerometerTextView;
    double distance;
    private final String DISTANCE_BUNDLE_KEY = "com.giorgiomula.biketool.distance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            distance = savedInstanceState.getDouble(DISTANCE_BUNDLE_KEY, 0.0);
        }
        setContentView(R.layout.activity_main);

        speedTextView = findViewById(R.id.speedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        accellerometerTextView = findViewById(R.id.accellerometerSpeedTextView);

        setDistanceText(distance);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                Location previousLocation;

                @Override
                public void onLocationChanged(Location location) {
                    if (previousLocation!= null && (!location.hasSpeed() || location.getSpeed() != 0)) {
                        distance += location.distanceTo(previousLocation);
                    }
                    previousLocation = location;
                    setSpeedText(location.getSpeed(), location.hasSpeed());
                    setDistanceText(distance);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    if (provider.equals(LocationManager.GPS_PROVIDER)) {
                        // TODO: if GPS location not available avoid any update !
                    }
                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } catch (SecurityException ex) {
            Toast.makeText(this, "Need to grant access location", Toast.LENGTH_LONG);
        }

        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(new SensorEventListener() {
            LinearSpeed x_speed;
            long mPrevTime;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mPrevTime > 5) {
                    x_speed.setAccelleration(event.values[0], event.timestamp);
                    setSpeedAccellerometerText(x_speed.getSpeed());
                } else {
                  x_speed = new LinearSpeed(event.values[0], event.timestamp, event.accuracy);
                }
                mPrevTime++;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, mSensor, 1000);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(DISTANCE_BUNDLE_KEY, distance);
    }

    public void buttonClearOnClick(View view) {
        distance = 0;
        setDistanceText(distance);
    }

    private void setDistanceText(double dist) {
        distanceTextView.setText(String.format("%06.0f m", dist));
    }

    private void setSpeedText(double speed_ms, boolean available) {
        String text = available? String.format("%04.2f\t Km/h", speed_ms * 3.6f) : getString(R.string.not_available);
        speedTextView.setText(text);
    }

    private void setSpeedAccellerometerText(double speed_ms) {
        String text = String.format("%04.2f\t Km/h", speed_ms * 3.6f);
        accellerometerTextView.setText(text);
    }


}
