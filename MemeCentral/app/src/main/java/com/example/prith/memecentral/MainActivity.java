package com.example.prith.memecentral;

import android.app.Dialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    List<AuthUI.IdpConfig> signInOptions;
    FirebaseAuth authentication;
    Button logout;
    FloatingActionButton fab;
    final int SIGN_IN_REQUEST_CODE = 1;
    final int FIND_IMAGE_REQUEST_CODE = 2;
    private static final int REQUEST_READ_PERMISSION = 100;
    private Intent i;

    private FirebaseDatabase mDatabase;

    private DatabaseReference mLikeReference;
    private DatabaseReference mMemeCollection;
    private DatabaseReference mUserReference;


    //now for the recycler view
    private RecyclerView mMemeCollectionRecyclerView;
    private FirebaseRecyclerAdapter<Meme, MemeCollectionViewHolder> mAdapter;


    private boolean mProcessLike = false;

    private Bitmap memeImage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //start authentication process
        authentication = FirebaseAuth.getInstance();





        //create all possible sign in options
        signInOptions = (List<AuthUI.IdpConfig>) new ArrayList<>();
        signInOptions.add(new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());
        signInOptions.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        signInOptions.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        //check if already signed in
        if(authentication.getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setProviders(signInOptions).build(), SIGN_IN_REQUEST_CODE);
        }

        //set up firebase database
        mDatabase = FirebaseDatabase.getInstance();
        mMemeCollection = mDatabase.getReference("memeCollection");
        mMemeCollectionRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMemeCollectionRecyclerView.setLayoutManager(mLayoutManager);

        LinearSnapHelper mSnapHelper = new LinearSnapHelper();
        mSnapHelper.attachToRecyclerView(mMemeCollectionRecyclerView);

        //set likes reference
        mLikeReference = mDatabase.getReference("likes");
        mUserReference = mDatabase.getReference("Users");
        mMemeCollection.keepSynced(true);
        mLikeReference.keepSynced(true);
        mUserReference.keepSynced(true);

        //button to upload memes
        fab = (FloatingActionButton) findViewById(R.id.uploadButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(view.getContext());
                dialog.setContentView(R.layout.uploadbox);
                //dialog box to upload meme
                final EditText descriptionView = (EditText) dialog.findViewById(R.id.giveDescription);
                final Button save = (Button) dialog.findViewById(R.id.finishUpload);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //create the object
                        if(memeImage == null){
                            Toast.makeText(getApplicationContext(), "Please Select An Image", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        String creator = authentication.getCurrentUser().getUid();

                        String description = descriptionView.getText().toString();

                        //code here from stack overflow to convert bitmap to string
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        memeImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                        memeImage.recycle();
                        byte[] byteArray = bytes.toByteArray();
                        String imageBytes = Base64.encodeToString(byteArray, Base64.DEFAULT);



                        Meme myMeme = new Meme(creator, description, imageBytes);
                        mMemeCollection.push().setValue(myMeme);
                        dialog.dismiss();
                    }
                });

                final Button findImage = (Button) dialog.findViewById(R.id.findImage);
                findImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //pull up gallery
                        i = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        requestPermission();
                    }
                });



                dialog.show();
            }
        });





    }






    //log out button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflator = getMenuInflater();
        menuInflator.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logoutButton){
            authentication.signOut();
            Intent restart = getIntent();
            finish();
            startActivity(restart);
        }
        if(item.getItemId() == R.id.username){

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.usernamebox);

            Log.d("dialog", "got here");
            //dialog box to upload meme
            final EditText usernameView = (EditText) dialog.findViewById(R.id.giveUsername);
            final Button set = (Button) dialog.findViewById(R.id.setUser);
            set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String user = usernameView.getText().toString();
                    if(user.length() == 0){
                        user = "";
                    }
                    mUserReference.child(authentication.getCurrentUser().getUid()).setValue(user);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        if(item.getItemId() == R.id.LeaderboardButton){
            Intent i = new Intent(this, LeaderboardActivity.class);
            startActivity(i);
        }
        return true;
    }

    //all the activity results possible for this activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //sign in request
        if(requestCode == SIGN_IN_REQUEST_CODE){
            //we signed in
            if(resultCode == RESULT_OK)
                Toast.makeText(getApplicationContext(), "Signed in", Toast.LENGTH_SHORT).show();
            else{
                //could not sign in
                i = getIntent();
                finish();
                startActivity(i);
                Toast.makeText(getApplicationContext(), "Could not sign in", Toast.LENGTH_SHORT).show();
            }
        }
        //code from stack overflow to get an image
        if(requestCode == FIND_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri select = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(select, filePath, null, null, null);
            cursor.moveToFirst();

            int colIndex = cursor.getColumnIndex(filePath[0]);
            String imagePath = cursor.getString(colIndex);
            cursor.close();

            memeImage = BitmapFactory.decodeFile(imagePath);
            Toast.makeText(getApplicationContext(), "Selected: " + imagePath, Toast.LENGTH_SHORT).show();
        }
    }


    //request permission code from stack overflow
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
        } else {
            startActivityForResult(i, FIND_IMAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_READ_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(i, FIND_IMAGE_REQUEST_CODE);
        }
    }


    //code from zilles to load the recycler view
    @Override
    protected void onStart() {
        super.onStart();

        mAdapter = new FirebaseRecyclerAdapter<Meme, MemeCollectionViewHolder>(Meme.class,
                R.layout.meme_list_item, MemeCollectionViewHolder.class, mMemeCollection) {
            @Override
            protected void populateViewHolder(final MemeCollectionViewHolder viewHolder,
                                              final Meme model, int position) {

                //id of the object
                final String memeID = getRef(position).getKey();

                Log.d("Main Activity", model.getCreator() );
                Log.d("Main Activity", "" + model.getScore() );
                viewHolder.mDescriptionView.setText(model.getDescription());
                //get the image from the string
                byte[] imageBytes = Base64.decode(model.getMyMeme().getBytes(), Base64.DEFAULT);
                final Bitmap meme = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                final Meme memeModel = model;
                viewHolder.mMemeView.setImageBitmap(meme);

                viewHolder.setLikeButton(memeID);

                //now like and dislike button
                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mProcessLike = true;

                        mLikeReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(memeID).hasChild(authentication.getCurrentUser().getUid())) {
                                        //do nothing already liked
                                        mLikeReference.child(memeID).child(authentication.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                        viewHolder.mLikeButton.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                                        Toast.makeText(getApplicationContext(), "Removed Like", Toast.LENGTH_SHORT).show();
                                    } else {
                                        viewHolder.mScoreView.setText("" + dataSnapshot.child(memeID).getChildrenCount());
                                        mLikeReference.child(memeID).child(authentication.getCurrentUser().getUid()).setValue(memeModel.getCreator());
                                        mProcessLike = false;
                                        Toast.makeText(getApplicationContext(), "Liked Post", Toast.LENGTH_SHORT).show();
                                        viewHolder.mLikeButton.setImageResource(R.drawable.ic_thumb_up_grey_24dp);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                });



                //set score
                mLikeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mScoreView.setText("" + (dataSnapshot.child(memeID).getChildrenCount()) + " Likes");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //now the creator
                mUserReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(model.getCreator())){
                            if(dataSnapshot.child(model.getCreator()).getValue().equals("")){
                                viewHolder.mCreator.setText("");
                            }
                            else{
                                String creator = "Creator: " + dataSnapshot.child(model.getCreator()).getValue();
                                viewHolder.mCreator.setText(creator);
                            }

                        }
                        else{
                            viewHolder.mCreator.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                //now for comment button

                viewHolder.mCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Actual Meme ID", memeID);
                        Intent commentIntent = new Intent(getApplicationContext(), CommentActivity.class);
                        commentIntent.putExtra(CommentActivity.userUID, authentication.getCurrentUser().getUid() + "," + memeID);
                        startActivity(commentIntent);
                    }
                });
            };

            @Override
            protected Meme parseSnapshot(DataSnapshot snapshot) {
                return super.parseSnapshot(snapshot);
            }
        };

        mMemeCollectionRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public static class MemeCollectionViewHolder extends RecyclerView.ViewHolder {
        TextView mDescriptionView;
        ImageView mMemeView;
        TextView mCreator;
        TextView mScoreView;
        ImageButton mLikeButton;
        ImageButton mCommentButton;

        FirebaseAuth authentication;

        private DatabaseReference mLikeReference;

        public MemeCollectionViewHolder(View view) {
            super(view);
            mDescriptionView = (TextView) view.findViewById(R.id.description);
            mMemeView = (ImageView) view.findViewById(R.id.imageView);
            mCreator = (TextView) view.findViewById(R.id.CreatorView);
            mScoreView = (TextView) view.findViewById(R.id.scoreView);
            mLikeButton = (ImageButton) view.findViewById(R.id.likebutton);
            mCommentButton = (ImageButton) view.findViewById(R.id.commentButton);

            //for firebase
            authentication = FirebaseAuth.getInstance();
            mLikeReference = FirebaseDatabase.getInstance().getReference().child("likes");
        }

        public void setLikeButton(final String memeID){
            mLikeReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(memeID).hasChild(authentication.getCurrentUser().getUid())) {
                        mLikeButton.setImageResource(R.drawable.ic_thumb_up_grey_24dp);


                    } else {
                        mLikeButton.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }






}
