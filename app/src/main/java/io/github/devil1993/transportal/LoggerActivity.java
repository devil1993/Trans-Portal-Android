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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LoggerActivity extends AppCompatActivity{
    private TextView tvGPS, tvWifi,tvSpeed,tvOpenWifi,tvCount;
    LocationManager service;
    String provider,mode,ID;
    WifiManager wifiManager;
    PrintWriter gpsWriter,wifiWriter, hariDesai;
    Location location;
    SeekBar seekBar;
    RatingBar ratingBar;
    float progress;
    boolean isActive;
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_logger);
            ratingBar = findViewById(R.id.ratingBar);
            seekBar = findViewById(R.id.seekBar);
            tvGPS = findViewById(R.id.tvGPS);
            tvWifi = findViewById(R.id.tvWifi);
            tvSpeed = findViewById(R.id.tvSpeed);
            tvOpenWifi = findViewById(R.id.tvFreeWiFi);
            tvCount = findViewById(R.id.tvWiFiCount);
            mode = getIntent().getStringExtra("MODE");
            ID = UUID.randomUUID().toString();
            isActive = false;
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                progress = (float)i/100;
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    progress = v / 5;
                    seekBar.setProgress((int) v * 20);
                }
            });

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
            String backlog = appDirectory + "/ToUpload";

            File appDir = new File(backlog);

            if (!appDir.exists()) {
                try {
                    appDir.mkdirs();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
                }
            }
            appDir = new File(appDirectory);
            try {
                hariDesai = new PrintWriter(appDir + "/" + mode.toLowerCase() + "_MARKING_" + ID + ".txt", "UTF-8");
                gpsWriter = new PrintWriter(appDir + "/" + mode.toLowerCase() + "_GPS_" + ID + ".txt", "UTF-8");
                wifiWriter = new PrintWriter(appDir + "/" + mode.toLowerCase() + "_WIFI_" + ID + ".txt", "UTF-8");
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
                finish();
            }
            isActive = true;
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
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                String currentDateandTime = sdf.format(new Date());
                                LoggerActivity.this.GPSAccess(currentDateandTime);
                                List<ScanResult> ws = wifiManager.getScanResults();
                                String s = "";
                                String open = "";
                                tvCount.setText(ws.size() + "");
                                for (ScanResult wifi : ws) {
                                    s += wifi.SSID + ", ";
                                    wifiWriter.println(wifi.BSSID + "," + wifi.SSID + "," + wifi.level + "," + currentDateandTime);
//                                System.out.println(wifi.SSID+":"+wifi.capabilities);
                                    if (!(wifi.capabilities.contains("WPA") || wifi.capabilities.contains("WPA2"))) {
                                        open += wifi.SSID + ",";
                                    }
                                }
                                tvOpenWifi.setText(open);
//                            System.out.println(s);
                                tvWifi.setText(s);
                                if (isActive) {
                                    hariDesai.println(progress + "," + currentDateandTime);
                                }
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
            try {
                service.requestLocationUpdates(provider, 0, 0, loclist);
            } catch (SecurityException e) {
                TransLogger.appendLog(e,TransLogger.WARN);
                throw e;
            }
//            throw new Exception("Testing");
        }
        catch (Exception e){
            TransLogger.appendLog(e,TransLogger.ERROR);
            throw e;
        }
    }

    @Override
    protected void onDestroy() {
//        Toast.makeText(this,"OnDestroy called",Toast.LENGTH_SHORT).show();
        try {
            super.onDestroy();
            if (gpsWriter != null) {
                gpsWriter.close();
            }
            if (wifiWriter != null) {
                wifiWriter.close();
            }
            if (hariDesai != null) {
                hariDesai.close();
            }
            // Upload files to cloud storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            final Uri wifiFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                    + "/TransPortal/" + mode.toLowerCase() + "_WIFI_" + ID + ".txt"));
            final Uri gpsFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                    + "/TransPortal/" + mode.toLowerCase() + "_GPS_" + ID + ".txt"));
            final Uri markFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                    + "/TransPortal/" + mode.toLowerCase() + "_MARKING_" + ID + ".txt"));

            StorageReference wifiRef = storageRef.child(wifiFile.getLastPathSegment());
            StorageReference gpsRef = storageRef.child(gpsFile.getLastPathSegment());
            StorageReference markRef = storageRef.child(markFile.getLastPathSegment());
            UploadTask wifiUploadTask = wifiRef.putFile(wifiFile);
            UploadTask gpsUploadTask = gpsRef.putFile(gpsFile);
            UploadTask markUploadTask = markRef.putFile(markFile);

            wifiUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Uri destFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                            + "/TransPortal/ToUpload/" + mode.toLowerCase() + "_WIFI_" + ID + ".txt"));
                    try {
                        copyFileUsingStream(new File(wifiFile.getPath()), new File(destFile.getPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            wifiUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("files");
                    mDatabase.push().setValue(wifiFile.getLastPathSegment());
                }
            });
            gpsUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Uri destFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                            + "/TransPortal/ToUpload/" + mode.toLowerCase() + "_GPS_" + ID + ".txt"));
                    try {
                        copyFileUsingStream(new File(gpsFile.getPath()), new File(destFile.getPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            gpsUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("files");
                    mDatabase.push().setValue(gpsFile.getLastPathSegment());
                }
            });

            markUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Uri destFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                            + "/TransPortal/ToUpload/" + mode.toLowerCase() + "_MARKING_" + ID + ".txt"));
                    try {
                        copyFileUsingStream(new File(markFile.getPath()), new File(destFile.getPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            markUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("files");
                    mDatabase.push().setValue(markFile.getLastPathSegment());
                }
            });
        }
        catch(Exception e){
            TransLogger.appendLog(e,TransLogger.ERROR);
            throw e;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(this,"On Start called",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Toast.makeText(this,"On Stop called",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
//        Toast.makeText(this,"On Pause called",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
//        Toast.makeText(this,"On Resume called",Toast.LENGTH_SHORT).show();

    }

    private void GPSAccess(String time){
        try {
            location = service.getLastKnownLocation(provider);
            if (location != null) {
//                System.out.println(mode+" GPS " + ID);
                tvGPS.setText(location.getLatitude() + "," + location.getLongitude());
                tvSpeed.setText((location.getSpeed()*3.6)+" Km/h");
                gpsWriter.println(location.getLatitude() + "," + location.getLongitude()+","
                        +location.getSpeed()+","+location.getAltitude()+","+time);
            } else {
                    tvSpeed.setText("Speed not available");
                    tvGPS.setText("Location not available.");
            }
        } catch (SecurityException e) {
            TransLogger.appendLog(e.getMessage(),TransLogger.LOG);
        }
    }
}
