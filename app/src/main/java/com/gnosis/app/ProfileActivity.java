package com.gnosis.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mNameField, mPhoneField, mSchool, mCourse, mAbout, mInterest;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, name, phone, profileImageUrl, userSex, school, course,about, interest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mNameField = (TextView) findViewById(R.id.name);
        mPhoneField = (TextView) findViewById(R.id.phone2);

        mSchool = (TextView) findViewById(R.id.school);
        mCourse = (TextView) findViewById(R.id.course);
        mAbout = (TextView) findViewById(R.id.about);
        mAbout.setMovementMethod(new ScrollingMovementMethod());

        mInterest = (TextView) findViewById(R.id.subject_interest);
        mInterest.setMovementMethod(new ScrollingMovementMethod());

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBack = (Button) findViewById(R.id.back);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserInfo();


        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("sex")!=null){
                        userSex = map.get("sex").toString();
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
                    if(map.get("interest_string") !=null){
                        interest=map.get("interest_string").toString();
                        mInterest.setText(interest);
                    }
                    Glide.clear(mProfileImage);
                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch(profileImageUrl){
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

        }

    public void goToSettings(View view) {
        Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
        startActivity(intent);
        finish();
        return;
    }
    }