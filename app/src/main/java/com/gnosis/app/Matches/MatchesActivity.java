package com.gnosis.app.Matches;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gnosis.app.Requests.RequestsObject;
import com.gnosis.app.Requests.RequestsAdapter;
import com.gnosis.app.Matches.MatchesAdapter;
import com.gnosis.app.Matches.MatchesObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gnosis.app.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MatchesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView, mRecyclerView1;
    private RecyclerView.Adapter mMatchesAdapter, mRequestsAdapter;
    private RecyclerView.LayoutManager mMatchesLayoutManager, mRequestsLayoutManager;
    private TextView emptyMatches, emptyReq;


    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);

        mMatchesLayoutManager = new LinearLayoutManager(MatchesActivity.this);
        mRecyclerView.setLayoutManager(mMatchesLayoutManager);
        mMatchesAdapter = new MatchesAdapter(getDataSetMatches(), MatchesActivity.this);
        mRecyclerView.setAdapter(mMatchesAdapter);

        //requests
        mRecyclerView1 = (RecyclerView) findViewById(R.id.recyclerView1);
        mRecyclerView1.setNestedScrollingEnabled(false);
        mRecyclerView1.setHasFixedSize(true);

        mRequestsLayoutManager = new LinearLayoutManager(MatchesActivity.this);
        mRecyclerView1.setLayoutManager(mRequestsLayoutManager);
        mRequestsAdapter = new RequestsAdapter(getDataSetRequests(), MatchesActivity.this);
        mRecyclerView1.setAdapter(mRequestsAdapter);

        emptyMatches = (TextView) findViewById(R.id.emptyMatches);
        emptyReq = (TextView) findViewById(R.id.emptyReq);
        emptyMatches.setVisibility(View.INVISIBLE);
        emptyReq.setVisibility(View.INVISIBLE);

        getUserRequestsId();
        getUserMatchId();




    }

    private void getUserMatchId() {

        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        FetchMatchInformation(match.getKey());
                    }
                }
                else{
                    emptyMatches.setVisibility(View.VISIBLE);
                    Toast.makeText(MatchesActivity.this, "No matches found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchMatchInformation(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //&& statement needs test!
                if (dataSnapshot.exists()){
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";
                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }


                    MatchesObject obj = new MatchesObject(userId, name, profileImageUrl);
                    resultsMatches.add(obj);
                    mMatchesAdapter.notifyDataSetChanged();
                    Log.d("LISTMATCHES", "added object!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //methods for requests
    private void getUserRequestsId() {

        DatabaseReference matchDb = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("yeps");
        matchDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() ){
                    for(DataSnapshot match : dataSnapshot.getChildren()){
                        FetchRequestsInformation(match.getKey());
                    }
                }
                else{
                    emptyReq.setVisibility(View.VISIBLE);
                    Toast.makeText(MatchesActivity.this, "No request found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void FetchRequestsInformation(String key) {
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUserID) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUserID) ){
                    String userId = dataSnapshot.getKey();
                    String name = "";
                    String profileImageUrl = "";
                    if(dataSnapshot.child("name").getValue()!=null){
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                    }


                    RequestsObject obj1 = new RequestsObject(userId, name, profileImageUrl);
                    resultsRequests.add(obj1);
                    mRequestsAdapter.notifyDataSetChanged();
                    Log.d("LISTREQUESTS", "added object!");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<MatchesObject> resultsMatches = new ArrayList<MatchesObject>();
    private List<MatchesObject> getDataSetMatches() { return resultsMatches; }


    private ArrayList<RequestsObject> resultsRequests = new ArrayList<RequestsObject>();
    public List<RequestsObject> getDataSetRequests() { return resultsRequests; }
}
