package io.github.devil1993.transportal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser fbu = FirebaseAuth.getInstance().getCurrentUser();
        String msg = "Signed in as " + fbu.getEmail();
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }
}
