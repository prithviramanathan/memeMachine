package com.example.prith.memecentral;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder>{
    List<Map.Entry<String, Integer>> ranks;
    FirebaseDatabase mDatabase;
    DatabaseReference mUserbase;

    public RankAdapter(List<Map.Entry<String, Integer>> userRanks) {
        this.ranks = userRanks;
        Log.d("rank size: ", "" + userRanks.size());
        mDatabase = FirebaseDatabase.getInstance();
        mUserbase = mDatabase.getReference("Users");
    }


    /**
     * covers screen with ranks
     * @param parent the recyclerView
     * @param viewType not used
     * @return the view holder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // a LayoutInflater turns a layout XML resource into a View object.
        final View rankListItem = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(rankListItem);
    }

    /**
     * loads the specific leader at that position
     * @param holder the view holder to display leaders
     * @param position the position in the list
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Map.Entry<String, Integer> thisRank = ranks.get(position);
        String mRank = "" + (int)(position + 1);
        holder.rankView.setText(mRank);
        holder.userView.setText(thisRank.getKey());
        String score = "" + thisRank.getValue();
        holder.scoreView.setText(score);



        //now the creator
        mUserbase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(thisRank.getKey())){
                    if(dataSnapshot.child(thisRank.getKey()).getValue().equals("")){
                        holder.userView.setText("");
                    }
                    else{
                        String creator = "" + dataSnapshot.child(thisRank.getKey()).getValue();
                        holder.userView.setText(creator);
                    }

                }
                else{
                    holder.userView.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * gets the number of movies
     * @return returns number of users
     */
    @Override
    public int getItemCount(){
        return ranks.size();
    }







    /**
     * Viewholder class that stores the data for each movie
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View view;
        public TextView rankView;
        public TextView userView;
        public TextView scoreView;
        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            rankView = (TextView) itemView.findViewById(R.id.rank);
            userView = (TextView) itemView.findViewById(R.id.userRank);
            scoreView = (TextView) itemView.findViewById(R.id.score);
        }
    }
}
