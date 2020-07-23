package com.example.covidfact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DailyReportActivity extends AppCompatActivity {


    private Button postpageBtn;
    private Button report_signoutBtn;

    private TextView infectedText;
    private TextView remediedText;
    private TextView decesedText;
    private TextView hospitalizedText;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseReport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);

        postpageBtn = (Button) findViewById(R.id.postpageBtn);
        report_signoutBtn = (Button) findViewById(R.id.report_signoutBtn);

        infectedText = (TextView) findViewById(R.id.infectedText);
        remediedText = (TextView) findViewById(R.id.remediedText);
        decesedText = (TextView) findViewById(R.id.decesedText);
        hospitalizedText = (TextView) findViewById(R.id.hospitalizedText);




        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReport = FirebaseDatabase.getInstance().getReference().child("Report");


        OkHttpClient client = new OkHttpClient();
        String url = "https://covid19.th-stat.com/api/open/today";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    final String myRespond = response.body().string();





                    DailyReportActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(myRespond);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                jsonObject=jsonObject.getJSONObject("0");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                String infected=jsonObject.getString("Confirmed");
                                String remedied = jsonObject.getString("Recovered");
                                String decesed = jsonObject.getString("Deaths");
                                String hospitalized = jsonObject.getString("Hospitalized");
//                                testRequest.setText(lat);
                                infectedText.setText(infected);
                                remediedText.setText(remedied);
                                decesedText.setText(decesed);
                                hospitalizedText.setText(hospitalized);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });




//        mDatabaseReport.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String infected = dataSnapshot.child("Infected").getValue().toString();
//                String remedied = dataSnapshot.child("Remedied").getValue().toString();
//                String hospitalized = dataSnapshot.child("Hospitalized").getValue().toString();
//                String decesed = dataSnapshot.child("Decesed").getValue().toString();
//
//                infectedText.setText(infected);
//                remediedText.setText(remedied);
//                decesedText.setText(decesed);
//                hospitalizedText.setText(hospitalized);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        postpageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPostPage();
            }
        });


        report_signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
                openSignInPage();

            }
        });

        checkUserExist();


    }


    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupUserIntent = new Intent(DailyReportActivity.this, SetupActivity.class);
                        setupUserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupUserIntent);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    public void openPostPage() {
        Intent intent = new Intent(DailyReportActivity.this, PostPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public void openSignInPage(){
        Intent intent = new Intent(DailyReportActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


}
