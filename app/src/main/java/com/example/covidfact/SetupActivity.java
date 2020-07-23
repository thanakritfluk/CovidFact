package com.example.covidfact;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetupActivity extends AppCompatActivity {


    private EditText displayName;
    private Button finishSetupBtn;

    private DatabaseReference mDtabaseUsers;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        displayName = (EditText) findViewById(R.id.displayNameField);
        finishSetupBtn = (Button) findViewById(R.id.finishBtn);

        mProgress = new ProgressDialog(this);

        mDtabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        finishSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });


    }

    private void startSetupAccount() {
        String name = displayName.getText().toString().trim();
        String user_id = mAuth.getCurrentUser().getUid();
        if(!TextUtils.isEmpty(name)){
            mProgress.setMessage("Setting up account .. ");
            mProgress.show();
            mDtabaseUsers.child(user_id).child("name").setValue(name);

            mProgress.dismiss();
            Intent postPageIntent = new Intent(SetupActivity.this,PostPageActivity.class);
            postPageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(postPageIntent);
        }else {
            Toast.makeText(SetupActivity.this,"Please fill up Display Name",Toast.LENGTH_LONG).show();
        }
    }
}
