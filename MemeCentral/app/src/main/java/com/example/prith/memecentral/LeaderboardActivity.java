package com.example.prith.memecentral;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mLikeReference;

    private RecyclerView mRecyclerView;
    private RankAdapter mAdapter;
    List<String> leaders;

    Map<String, Integer> userScorePair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Dank Boards");
        setSupportActionBar(toolbar);


        mDatabase = FirebaseDatabase.getInstance();
        mLikeReference = mDatabase.getReference("likes");

        mRecyclerView = (RecyclerView) findViewById(R.id.leaderboardView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflator = getMenuInflater();
        menuInflator.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.RecentButton){
            Intent recent = new Intent(this, MainActivity.class);
            startActivity(recent);
        }
        return true;
    }



    @Override
    protected void onStart() {
        super.onStart();
        userScorePair = new HashMap<>();
        //get the leaderboard data
        mLikeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot memes: dataSnapshot.getChildren()){
                    for(DataSnapshot likes: memes.getChildren()){
                        String key = "" + likes.getValue();
                        if(userScorePair.containsKey("" + likes.getValue())){

                            userScorePair.put(key, userScorePair.get(key) + 1);
                        }
                        else{
                            userScorePair.put(key, 1);
                        }

                    }
                }
                Log.d("Get Scores", "" + userScorePair.size());
                List<Map.Entry<String, Integer>> list = sortMap(userScorePair);
                Log.d("Sorted", "" + list.size());
                mAdapter = new RankAdapter(list);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public List<Map.Entry<String, Integer>> sortMap(Map<String, Integer> pairs) {
        //stack overflow code to sort
        List<Map.Entry<String, Integer>> mlist = new LinkedList<>(pairs.entrySet());
        Collections.sort(mlist, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> stringIntegerEntry, Map.Entry<String, Integer> t1) {
                return t1.getValue().compareTo(stringIntegerEntry.getValue());
            }
        });
        return mlist;

    }
}


