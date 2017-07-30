package com.example.prith.memecentral;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CommentActivity extends AppCompatActivity {
    public static final String userUID = "";
    private String memeKey;
    private String userKey;


    private FirebaseDatabase mDatabase;

    private DatabaseReference mCommentReference;
    private DatabaseReference mUserReference;
    //comment section for this specific meme
    private DatabaseReference mCommentSection;


    private RecyclerView mRecyclerView;
    private EditText mCommentText;
    private FirebaseRecyclerAdapter<CommentMessage, CommentViewHolder> mCommentViewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commentboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Comments");
        setSupportActionBar(toolbar);

        //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        userKey = getIntent().getStringExtra(userUID);
        memeKey = userKey.split(",")[1];
        userKey = userKey.split(",")[0];
        Log.d("MemeID: ", memeKey);
        Log.d("UserID: ", userKey);
        mDatabase = FirebaseDatabase.getInstance();
        mUserReference = mDatabase.getReference("Users");
        mCommentReference = mDatabase.getReference("Comments");
        mCommentSection = mCommentReference.child(memeKey);
        mRecyclerView = (RecyclerView) findViewById(R.id.commentRecycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentSection.keepSynced(true);
        mCommentText = (EditText) findViewById(R.id.newComment);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mCommentViewAdapter = new FirebaseRecyclerAdapter<CommentMessage,
                CommentViewHolder>(CommentMessage.class, R.layout.comment, CommentViewHolder.class, mCommentSection){
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder,
                                              CommentMessage model, int position) {
                viewHolder.bind(model);
            }
        };
        mRecyclerView.setAdapter(mCommentViewAdapter);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView mAuthorView;
        private final TextView mCommentView;
        private FirebaseDatabase mDatabase;
        private DatabaseReference mUserReference;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mAuthorView = (TextView) itemView.findViewById(R.id.commenter);
            mCommentView = (TextView) itemView.findViewById(R.id.comment);
            mDatabase = FirebaseDatabase.getInstance();
            mUserReference = mDatabase.getReference("Users");
        }

        public void bind(final CommentMessage message) {
            mCommentView.setText(message.getMessage());
            mAuthorView.setText(message.getAuthor());

            //now the creator
            mUserReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(message.getAuthor())){
                        if(dataSnapshot.child(message.getAuthor()).getValue().equals("")){
                            mAuthorView.setText("");
                        }
                        else{
                            String creator = "" + dataSnapshot.child(message.getAuthor()).getValue();
                            mAuthorView.setText(creator);
                        }

                    }
                    else{
                        mAuthorView.setText("");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }



    public void newCommentPost(View view) {
        String newCommentString = mCommentText.getText().toString();
        mCommentText.setText("");
        if(newCommentString.length() == 0){
            return;
        }
        if (mCommentSection != null) {
//            ChatMessage chatMessage = new ChatMessage(newPostString, "George");
            CommentMessage comment = new CommentMessage(userKey, newCommentString);
            mCommentReference.push().setValue(comment);
            mCommentSection.push().setValue(comment);
        } else {
            Toast.makeText(view.getContext(), "Database not available", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    //gotten from stack overflow. For back button
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
