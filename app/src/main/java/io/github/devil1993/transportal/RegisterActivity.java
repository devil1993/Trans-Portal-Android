package io.github.devil1993.transportal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail,etPassword, etCnfPwd ,etName;
    Button btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPwd);
        etCnfPwd = (EditText) findViewById(R.id.etCnfPwd);
        etName = (EditText) findViewById(R.id.etName);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String cnfPwd = etCnfPwd.getText().toString();
                final String name = etName.getText().toString();

                if (!password.equals(cnfPwd)){
                    Toast.makeText(RegisterActivity.this,"Password does not match.",Toast.LENGTH_LONG).show();
                }
                else {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
//                                        Log.v("dhus","dhusDhus");
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if(user == null){
                                            Toast.makeText(RegisterActivity.this, "Case kheyeche", Toast.LENGTH_SHORT).show();
                                        }
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this,
                                                                    "Registration Successful for "+name, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        RegisterActivity.this.startActivity(mainIntent);

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        String s = task.getException().getMessage();
                                        Toast.makeText(RegisterActivity.this, "Registration Failed "+s, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
