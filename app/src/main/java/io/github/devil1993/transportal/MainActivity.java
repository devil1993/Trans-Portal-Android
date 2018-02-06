package io.github.devil1993.transportal;

import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    protected void onCreate(Bundle savedInstanceState) {
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
                Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
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
                int selected = travelMode.getCheckedRadioButtonId();
                rb = findViewById(selected);
                String mode = rb.getText().toString();
                if(mode.equals(((RadioButton)findViewById(R.id.rbOther)).getText().toString())){
                    mode = newMode.getText().toString();
                }
                Toast.makeText(MainActivity.this,"Selected mode " + mode + " starting...",Toast.LENGTH_SHORT).show();

                Intent loggerIntent = new Intent(MainActivity.this, LoggerActivity.class);
                loggerIntent.putExtra("MODE", mode);
                startActivity(loggerIntent);
            }

        });
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    String path = Environment.getExternalStorageDirectory().getPath()+"/TransPortal/ToUpload/";
                    File folder = new File(path);
                    File[] listOfFiles = folder.listFiles();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    for(File f : listOfFiles){
                        final File F = f;
                        Uri file = Uri.fromFile(f);
                        StorageReference fileref = storageRef.child(file.getLastPathSegment());
                        UploadTask ut = fileref.putFile(file);
                        ut.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                F.delete();
                            }
                        });
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
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
}
