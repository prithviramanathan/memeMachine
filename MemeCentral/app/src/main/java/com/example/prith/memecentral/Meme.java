package com.example.prith.memecentral;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by prith on 3/31/2017.
 */
@IgnoreExtraProperties
public class Meme {


    private String creator;
    private int likes;
    private int dislikes;
    private String description;
    private int score;
    private String myMeme;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getLikes() {
        return likes;
    }

    public Meme() {
        //do nothing
    }

    public String getMyMeme() {
        return myMeme;
    }

    public String getDescription() {
        return description;
    }

    public Meme(String creator, String description, String myMeme) {
        this.creator = creator;
        this.description = description;
        this.myMeme = myMeme;
        likes = 0;
        dislikes = 0;
    }




    public int getDislikes() {
        return dislikes;
    }


    public int getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Meme meme = (Meme) o;

        if (!creator.equals(meme.creator)) return false;
        if (!description.equals(meme.description)) return false;
        return myMeme.equals(meme.myMeme);

    }

    @Override
    public int hashCode() {
        int result = creator.hashCode();
        result = 31 * result + likes;
        result = 31 * result + dislikes;
        result = 31 * result + description.hashCode();
        result = 31 * result + score;
        result = 31 * result + myMeme.hashCode();
        return result;
    }
}
