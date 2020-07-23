package com.example.covidfact;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AddPostActivity extends AppCompatActivity {

    private ImageButton mselectImage;
    private static final int GALLERY_REQUEST = 1;
    // Define for later access to image uri that user selected
    private Uri mimageUri = null;

    private EditText mPostTitle, mPostDesc;
    private Button mSubmitBtn;

    // Define firebase storage
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUser;

    // Define Progress Dialog when uploading to firebase storage
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // Define all use element
        mselectImage = (ImageButton) findViewById(R.id.imageSelect);
        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDesc = (EditText) findViewById(R.id.descField);
        mSubmitBtn = (Button) findViewById(R.id.submitBtn);
        mProgress = new ProgressDialog(this);


        // This redirect to root in firebase storage
        mStorage = FirebaseStorage.getInstance().getReference();
        // This redirect to child Blog in firebase realtime
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());


        // On click to get to image and return image result code
        mselectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        // Set on click to submit button
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

    }


    // Posting image to Firebase Storage
    private void startPosting() {

        mProgress.setMessage("Posting to Blog ...");


        final String title_val = mPostTitle.getText().toString().trim();
        final String desc = mPostDesc.getText().toString().trim();


        // Check is not empty before post it
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc) && mimageUri != null) {

            mProgress.show();

            // getLastPathSegment = get name of image
            StorageReference filePath = mStorage.child("Blog_Images").child(mimageUri.getLastPathSegment());

            filePath.putFile(mimageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();

                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            final Uri data_URI = uri;

                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    DatabaseReference newPost = mDatabase.push();
                                    String imageUrl = data_URI.toString();
                                    newPost.child("title").setValue(title_val);
                                    newPost.child("desc").setValue(desc);
                                    newPost.child("image").setValue(imageUrl);
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                startActivity(new Intent(AddPostActivity.this, PostPageActivity.class));
                                                finish();
                                            }else {

                                                // Log something when get name is error
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {



                                }
                            });


                        }
                    });
                    mProgress.dismiss();

                    // After already post return to postpage activity
//                    startActivity(new Intent(AddPostActivity.this, PostPageActivity.class));
                }
            });

        }

    }


    // After get the image result check the condition to know it doesn't cause any error and then set the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mimageUri = data.getData();
            mselectImage.setImageURI(mimageUri);
        }
    }
}
