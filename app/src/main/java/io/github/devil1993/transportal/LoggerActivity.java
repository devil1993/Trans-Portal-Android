package io.github.devil1993.transportal;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LoggerActivity extends AppCompatActivity{
    private TextView tvGPS, tvWifi;
    LocationManager service;
    String provider,mode,ID;
    WifiManager wifiManager;
    PrintWriter gpsWriter,wifiWriter;
    Location location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger);
        tvGPS = findViewById(R.id.tvGPS);
        tvWifi = findViewById(R.id.tvWifi);
        mode = getIntent().getStringExtra("MODE");
        ID = UUID.randomUUID().toString();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        Criteria criteria = new Criteria();
        provider = service.getBestProvider(criteria, true);

        String root = Environment.getExternalStorageDirectory().getPath();
        String appDirectory = root + "/TransPortal";

        File appDir = new File(appDirectory);

        if(!appDir.exists()){
            try{
                appDir.mkdirs();
            }
            catch (Exception e){
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT);
            }
        }

        try{
            gpsWriter = new PrintWriter(appDir+"/"+mode.toLowerCase()+"_GPS_"+ID+".txt", "UTF-8");
            wifiWriter = new PrintWriter(appDir+"/"+mode.toLowerCase()+"_WIFI_"+ID+".txt", "UTF-8");
        }
        catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gpsWriter != null){
            gpsWriter.close();
        }
        if(wifiWriter!= null){
            wifiWriter.close();
        }
        // Upload files to cloud storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri wifiFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                +"/TransPortal/"+mode.toLowerCase()+"_WIFI_"+ID+".txt"));
        Uri gpsFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                +"/TransPortal/"+mode.toLowerCase()+"_GPS_"+ID+".txt"));

        StorageReference wifiRef = storageRef.child(wifiFile.getLastPathSegment());
        StorageReference gpsRef = storageRef.child(gpsFile.getLastPathSegment());
        UploadTask wifiUploadTask = wifiRef.putFile(wifiFile);
        UploadTask gpsUploadTask = gpsRef.putFile(gpsFile);
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("files");
        mDatabase.push().setValue(wifiFile.getLastPathSegment());
        mDatabase.push().setValue(gpsFile.getLastPathSegment());

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationListener loclist = new LocationListener() {
            @Override
            public void onLocationChanged(Location location1) {
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
                Toast.makeText(LoggerActivity.this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                            String currentDateandTime = sdf.format(new Date());
                            LoggerActivity.this.GPSAccess(currentDateandTime);
                            List<ScanResult> ws  = wifiManager.getScanResults();
                            String s = "";
                            for(ScanResult wifi :ws){
                                s += wifi.SSID + " ";
                                wifiWriter.println(wifi.BSSID+","+wifi.SSID+","+wifi.level+","+currentDateandTime);
                            }
//                            System.out.println(s);
                            tvWifi.setText(s);
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
        try{
            service.requestLocationUpdates(provider,0,0,loclist);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void GPSAccess(String time){
        try {
            location = service.getLastKnownLocation(provider);
            if (location != null) {
//                System.out.println(mode+" GPS " + ID);
                tvGPS.setText(location.getLatitude() + "," + location.getLongitude());
                gpsWriter.println(location.getLatitude() + "," + location.getLongitude()+","
                        +location.getSpeed()+","+location.getSpeed()+","+time);
            } else {
                    tvGPS.setText("Location not available.");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
