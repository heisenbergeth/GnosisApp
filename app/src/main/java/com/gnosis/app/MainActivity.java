package com.gnosis.app;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.gnosis.app.Cards.arrayAdapter;
import com.gnosis.app.Cards.cards;
import com.gnosis.app.Matches.MatchesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private com.gnosis.app.Cards.arrayAdapter arrayAdapter;
    private int i;

    private FirebaseAuth mAuth;

    private String currentUId, name, profileImageUrl, school, course,about, interest;

    private TextView textView, mNameField, mSchool, mCourse, mAbout, mAboutTitle, mInterest, mInterestTitle;

    private ImageView mProfileImage;

    private DatabaseReference usersDb, mUserDatabase;

    ListView listView;
    List<cards> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();

        checkUserType();


        textView = (TextView) findViewById(R.id.instruction);
        rowItems = new ArrayList<cards>();

        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems );

        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
                textView.setVisibility(View.GONE);

            }

            @Override
            public void onLeftCardExit(Object dataObject) {

                cards obj = (cards) dataObject;
                String userId = obj.getUserId();

                /** userID of the one in the card, malalagay dun sa nope o yep niya mismo
                pedeng matawag pag kinukuha na yung mga suggestion na match (sana) XD */

                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);
                Toast.makeText(MainActivity.this, "Match dismissed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                isConnectionMatch(userId);
                Toast.makeText(MainActivity.this, "Added to match list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }


            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                showAlertbox(userId);
            }
        });

    }
    //Dialog box pag clinick yung card
    public void showAlertbox(String userId) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        mNameField = (TextView) dialog.findViewById(R.id.name);
        mSchool = (TextView) dialog.findViewById(R.id.school);
        mCourse = (TextView) dialog.findViewById(R.id.course);
        mAbout = (TextView) dialog.findViewById(R.id.about);
        mAboutTitle = (TextView) dialog.findViewById(R.id.about_title);
        mInterest = (TextView) dialog.findViewById(R.id.interest);
        mInterestTitle = (TextView) dialog.findViewById(R.id.interest_title);

        mProfileImage = (ImageView) dialog.findViewById(R.id.profileImage);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("school")!=null){
                        school=map.get("school").toString();
                        mSchool.setText(school);
                    }
                    if(map.get("course")!=null){
                        course=map.get("course").toString();
                        mCourse.setText(course);
                    }
                    if(map.get("about")!=null){
                        about=map.get("about").toString();
                        mAbout.setText(about);
                    }
                    else{
                        mAboutTitle.setVisibility(View.GONE);
                        mAbout.setVisibility(View.GONE);
                    }
                    if(map.get("interest_string")!=null){
                        interest=map.get("interest_string").toString();
                        mInterest.setText(interest);
                    }
                    else{
                        mInterestTitle.setVisibility(View.GONE);
                        mInterest.setVisibility(View.GONE);
                    }
                    Glide.clear(mProfileImage);
                    if(map.get("profileImageUrl")!=null) {
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch (profileImageUrl) {
                            case "default":
                                Glide.with(getApplication()).load(R.mipmap.default_pic).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button close = (Button) dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }


    private void isConnectionMatch(String userId) {
        //check kung nasa yep ka din niya
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "You have been connected to someone! To view, click Matches.", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();


                    //add chatID to db
                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatId").setValue(key);
                    usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String userType;
    private String oppositeUserType;
    public void checkUserType(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("type").getValue() != null){
                        userType = dataSnapshot.child("type").getValue().toString();
                        switch (userType){
                            case "Tutor":
                                oppositeUserType = "Learner";
                                break;
                            case "Learner":
                                oppositeUserType = "Tutor";
                                break;
                        }
                        getOppositeTypeUsers();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getOppositeTypeUsers(){
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //conditions for cards
                if (dataSnapshot.child("type").getValue() != null) {
                                                    //wala ka sa nopeDb ng user                                                   //wala ka sa yepDb ng user                                                  //opposite userType ng user
                    if (dataSnapshot.exists() && !dataSnapshot.child("connections").child("nope").hasChild(currentUId) && !dataSnapshot.child("connections").child("yeps").hasChild(currentUId) && dataSnapshot.child("type").getValue().toString().equals(oppositeUserType)) {
                        String profileImageUrl = "default";
                        //kung hindi default yung userpic, irerecall sa database yung url ng profilepic na inupload niya
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }

                        //connection sa database nung cards
                        cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("school").getValue().toString(),  profileImageUrl);
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void logoutUser(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }


    public void goToProfile(View view) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        return;
    }

    public void goToMatches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchesActivity.class);
        startActivity(intent);
        return;

    }

}