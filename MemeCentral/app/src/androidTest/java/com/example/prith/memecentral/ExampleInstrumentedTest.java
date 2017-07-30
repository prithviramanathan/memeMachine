package com.example.prith.memecentral;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    FirebaseDatabase mDatabase;
    DatabaseReference mTestReference;


    @Before
    public void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        mDatabase = FirebaseDatabase.getInstance();
        mTestReference = mDatabase.getReference("Tests");

    }

    @Test
    public void writeAndReadString() throws Exception {
        // Context of the app under test.

        final String testString = "Hello Testing";
        mTestReference.child("test").setValue(testString);
        Thread.sleep(3000);
        mTestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String read = "" + dataSnapshot.child("test").getValue();
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertEquals(read, testString);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Thread.sleep(3000);
    }

    @Test
    public void writeAndReadObject() throws Exception {
        // Context of the app under test.

        final Meme testMeme = new Meme("me", "hello", "meme");
        mTestReference.child("testMeme").setValue(testMeme);
        Thread.sleep(3000);
        mTestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fromDatabase = "" + dataSnapshot.child("testMeme").child("myMeme").getValue();
                assertEquals(testMeme.getMyMeme(), fromDatabase);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Thread.sleep(3000);
    }
}
