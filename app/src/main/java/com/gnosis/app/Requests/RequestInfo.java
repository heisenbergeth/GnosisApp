package com.gnosis.app.Requests;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gnosis.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class RequestInfo extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private String currentUId, name, profileImageUrl, school, course,about, interest, userId;

    private TextView textView, mNameField, mSchool, mCourse, mAbout, mAboutTitle, mInterest, mInterestTitle;

    private ImageView mProfileImage;

    private DatabaseReference usersDb, mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = getIntent().getExtras().getString("matchId");
        showAlertbox(userId);
    }

    public void showAlertbox(String userId) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup);
        dialog.setCanceledOnTouchOutside(false);
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
                finish();
            }
        });
        dialog.show();

    }
}

