package com.example.covidfact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostPageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private boolean mProcessLike = false;


    // Auth Firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;

    private FirebaseRecyclerAdapter<Blog, ShowDataViewHolder> mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Check is login or not
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(PostPageActivity.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");


        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");

        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        databaseReference.keepSynced(true);


        recyclerView = (RecyclerView) findViewById(R.id.blog_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        checkUserExist();

    }

    @Override
    public void onStart() {
        super.onStart();


        mAuth.addAuthStateListener(mAuthListener);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Blog, ShowDataViewHolder>
                (Blog.class, R.layout.blog_row, ShowDataViewHolder.class, databaseReference) {

            @Override
            protected void populateViewHolder(ShowDataViewHolder showDataViewHolder, Blog blog, int i) {

                final String postKey = getRef(i).getKey();

                showDataViewHolder.Image(blog.getImage());
                showDataViewHolder.Title(blog.getTitle());
                showDataViewHolder.Desc(blog.getDesc());
                showDataViewHolder.Username("Publisher: " + blog.getUsername());
                showDataViewHolder.setLikeBtn(postKey);


//                showDataViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(PostPageActivity.this, postKey, Toast.LENGTH_LONG).show();
//
//                    }
//                });

                showDataViewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (mProcessLike) {
                                    if (dataSnapshot.child(postKey).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseLike.child(postKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        mDatabaseLike.child(postKey).child(mAuth.getCurrentUser().getUid()).setValue("Liked");
                                        mProcessLike = false;
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });


            }

        };
        recyclerView.setAdapter(mFirebaseAdapter);


    }

    // Class to set Image, Desc and Title to View
    public static class ShowDataViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView desc;
        private final ImageView image;
        private final TextView username;
        private final ImageButton mLikeBtn;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;


        public ShowDataViewHolder(final View itemView) {
            super(itemView);
            desc = (TextView) itemView.findViewById(R.id.post_desc);
            title = (TextView) itemView.findViewById(R.id.post_title);
            image = (ImageView) itemView.findViewById(R.id.post_image);
            username = (TextView) itemView.findViewById(R.id.usernameField);
            mLikeBtn = (ImageButton) itemView.findViewById(R.id.likeBtn);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Like");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);

        }

        private void Title(String title) {
            this.title.setText(title);
        }

        private void Desc(String desc) {
            this.desc.setText(desc);
        }

        private void Username(String username) {
            this.username.setText(username);
        }

        private void Image(String img) {
            Glide.with(itemView.getContext())
                    .load(img)
                    .crossFade()
                    .placeholder(R.drawable.googleg_standard_color_18)
                    .thumbnail(01.f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image);
        }

        private void setLikeBtn(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mLikeBtn.setImageResource(R.drawable.baseline_thumb_up_black_24_liked);
                    }else{
                        mLikeBtn.setImageResource(R.drawable.baseline_thumb_up_black_24_dislike);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent setupUserIntent = new Intent(PostPageActivity.this, SetupActivity.class);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(PostPageActivity.this, AddPostActivity.class));
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }
}
