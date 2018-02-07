package io.github.devil1993.transportal;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RadioButton rb;
    RadioGroup travelMode;
    Button btnGo;
    TextView tvLogOut,tvExit;
    EditText newMode;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Please press the start button again to start logging.",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please provide the necessary permissions.",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            tvExit = findViewById(R.id.tvExit);
            tvLogOut = findViewById(R.id.tvLogOut);
            travelMode = findViewById(R.id.travelMode);
            newMode = findViewById(R.id.etVehicle);
            btnGo = findViewById(R.id.btnStart);
            tvLogOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    MainActivity.this.startActivity(loginIntent);
                }
            });

            tvExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finishAffinity();
                }
            });
            btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                            alertBuilder.setCancelable(true);
                            alertBuilder.setTitle("Permission necessary");
                            alertBuilder.setMessage("Write external storage permission is necessary to write log file!!!");
                            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_CALENDAR}, 1);
                                }
                            });
                            AlertDialog alert = alertBuilder.create();
                            alert.show();
                        } else {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_CALENDAR}, 1);

                        }
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {

                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                            alertBuilder.setCancelable(true);
                            alertBuilder.setTitle("Permission necessary");
                            alertBuilder.setMessage("GPS access permission is necessary to log the locations!!!");
                            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                                }
                            });
                            AlertDialog alert = alertBuilder.create();
                            alert.show();
                        } else {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

                        }
                    } else {

                        int selected = travelMode.getCheckedRadioButtonId();
                        rb = findViewById(selected);
                        String mode = rb.getText().toString();
                        if (mode.equals(((RadioButton) findViewById(R.id.rbOther)).getText().toString())) {
                            mode = newMode.getText().toString();
                        }
                        Toast.makeText(MainActivity.this, "Selected mode " + mode + " starting...", Toast.LENGTH_SHORT).show();

                        Intent loggerIntent = new Intent(MainActivity.this, LoggerActivity.class);
                        loggerIntent.putExtra("MODE", mode);
                        startActivity(loggerIntent);
                    }
                }

            });
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        String path = Environment.getExternalStorageDirectory().getPath() + "/TransPortal/ToUpload/";
                        File folder = new File(path);
                        File[] listOfFiles = folder.listFiles();
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        for (File f : listOfFiles) {
                            final File F = f;
                            final Uri file = Uri.fromFile(f);
                            StorageReference fileref = storageRef.child(file.getLastPathSegment());
                            UploadTask ut = fileref.putFile(file);
                            ut.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    DatabaseReference mDatabase;
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("files");
                                    mDatabase.push().setValue(file.getLastPathSegment());
                                    F.delete();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        catch (Exception e){
            TransLogger.appendLog(e.getMessage()+"\n"+e.getStackTrace(),TransLogger.ERROR);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Toast.makeText(this,"On pause of main",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Toast.makeText(this,"On stop of main",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        try{
            super.onStart();
            FirebaseUser fbu = FirebaseAuth.getInstance().getCurrentUser();
            String msg = "Signed in as " + fbu.getEmail();
            Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    //        Thread t = new Thread() {
    //            @Override
    //            public void run() {
    //                String path = Environment.getExternalStorageDirectory().getPath()+"/TransPortal/ToUpload/";
    //                File folder = new File(path);
    //                File[] listOfFiles = folder.listFiles();
    //                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    //                for(File f : listOfFiles){
    //                    final File F = f;
    //                    Uri file = Uri.fromFile(f);
    //                    StorageReference fileref = storageRef.child(file.getLastPathSegment());
    //                    UploadTask ut = fileref.putFile(file);
    //                    ut.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
    //                        @Override
    //                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
    //                            F.delete();
    //                        }
    //                    });
    //                }
    //            }
    //        };
    //        t.start();
        }
        catch (Exception e){
            TransLogger.appendLog(e.getMessage()+"\n"+e.getStackTrace(),TransLogger.ERROR);
        }
    }
}
