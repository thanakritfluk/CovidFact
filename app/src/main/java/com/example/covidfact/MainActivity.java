package com.example.covidfact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Button signin;
    private SignInButton googleSignInBtn;
    private Button signup;
    private EditText email;
    private EditText password;
    private ProgressDialog mProgress;


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private String TAG = "Main Login Activity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        // Make sure it store data offline
        mDatabaseUsers.keepSynced(true);

        // Define all button
        signin = (Button) findViewById(R.id.signin);
        googleSignInBtn = (SignInButton) findViewById(R.id.googleBtn);
        signup = (Button) findViewById(R.id.signup);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        mProgress = new ProgressDialog(this);

        // Set on click when hit signin
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openPostPage();
                checkLogin();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterPage();
            }
        });

        checkAlreadyLogin();


    }

    private void checkLogin() {
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgress.setMessage("Checking Login ...");
            mProgress.show();


            // Make simple signin with email password
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mProgress.dismiss();
                        checkUserExist();
                    } else {
                        mProgress.dismiss();
                        Toast.makeText(MainActivity.this, "Error Login make sure email password correct", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please input email and password", Toast.LENGTH_LONG).show();
        }
    }

    // Check when user open application again and check is already login or not
    private void checkAlreadyLogin() {
        if (mAuth.getCurrentUser() != null) {
            finish();
//            openPostPage();
            openDailyReportPage();
        }
    }


    // Check when click signin is user has the diaplay name in realtime database or not
    private void checkUserExist() {
        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id)) {
                    finish();
                    openDailyReportPage();

                } else {
//                    Toast.makeText(MainActivity.this, "You need to setup your account.", Toast.LENGTH_LONG).show();
                    openSetupPage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void openDailyReportPage(){
        Intent intent = new Intent(MainActivity.this, DailyReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void openPostPage() {
        Intent intent = new Intent(MainActivity.this, PostPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void openSetupPage() {
        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void openRegisterPage() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
