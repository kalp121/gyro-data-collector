package com.example.kalp.pothhole;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEventListener gyroscopeEventListener;
    private float[] gyroData;
    private double[] locationData;
    private Handler handler;
    private Runnable runnable;
    private TextView gyro_x;
    private TextView gyro_y;
    private TextView gyro_z;
    private TextView is_pothole;
    private TextView longitude;
    private TextView lattitude;
    private TextView speed;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean temp;
    private static  int pothole=0;
    private Button mark;

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        boolean create=FileHelper.createFile();
        temp = checkPermissions();


        mark=(Button)findViewById(R.id.mark);
        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pothole=1;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pothole=0;
                    }
                }, 500);

            }
        });

        if (gyroscopeSensor == null) {
            Toast.makeText(this, "device has no gyro", Toast.LENGTH_SHORT).show();
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationData[0] = location.getLongitude();
                locationData[1] = location.getLatitude();
                locationData[2] = location.getSpeed();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        locationManager.requestLocationUpdates("gps", 100, 0, locationListener);

        gyroscopeEventListener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                gyroData=sensorEvent.values;





                if(sensorEvent.values[2]>0.5f){
                    getWindow().getDecorView().setBackgroundColor(Color.GREEN);

                } else if (sensorEvent.values[2] <-0.5f){
                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        gyro_x = (TextView)findViewById(R.id.gyroX);
        gyro_y = (TextView)findViewById(R.id.gyroY);
        gyro_z = (TextView)findViewById(R.id.gyroZ);
        longitude=(TextView)findViewById(R.id.lng);
        lattitude=(TextView)findViewById(R.id.latt);
        speed=(TextView)findViewById(R.id.speed);
        is_pothole=(TextView)findViewById(R.id.pothole);

        gyroData = new float[3];
        locationData= new double[3];

        handler = new Handler();


        runnable = new Runnable()
        {
            @Override
            public void run() {
                handler.postDelayed(this, 500);
                //Toast.makeText(MainActivity.this,"SAVEDDDD",Toast.LENGTH_SHORT).show();
                updateGyroText();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroscopeEventListener,gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroscopeEventListener);
        handler.removeCallbacks(runnable);
    }


    protected void updateGyroText(){
        // Update the gyroscope data
        gyro_x.setText(String.format("%.2f", gyroData[0]));
        gyro_y.setText(String.format("%.2f", gyroData[1]));
        gyro_z.setText(String.format("%.2f", gyroData[2]));
        longitude.setText(String.format("%.5f",locationData[0]));
        lattitude.setText(String.format("%.5f",locationData[1]));
        speed.setText(String.format("%.3f",locationData[2]));
        is_pothole.setText(String.format("%1d",pothole));



        if(FileHelper.saveToFile(gyroData[0]+","+gyroData[1]+","+gyroData[2]+","+locationData[0]+","+locationData[1]+","+locationData[2]+","+pothole)){ }

    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

}
