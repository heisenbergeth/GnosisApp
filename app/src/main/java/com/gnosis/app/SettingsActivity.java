package com.gnosis.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mAbout;
    private Spinner mSchool, mCourse;

    private Button mBack, mConfirm, mInterest;

    private TextView mItemSelected;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userId, name, phone, profileImageUrl, userSex, school1, course1,school, course, about, type;

    private int index1, index2;

    private Uri resultUri;


    private ArrayList<Integer> mUserItems = new ArrayList<>();
    private ArrayList<Integer> mUserItems1 = new ArrayList<>();
    ArrayList<String> selected_categories = new ArrayList<String>();
    private String[] listItems;
    //private String[] listItems = {"BSIT", "BSCS", "BSECE"};
    boolean[] checkedItems;
    //boolean[] checkedItems ={false, false, false};
    TinyDB tinydb;
    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor sharedPrefEditor;



    final String titleTutor= "Select all applicable subject you can teach:";
    final String titleLearner= "Select all applicable subject you want to learn:";
    private String title="";
    final String learner="Learner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone2);

        mSchool = (Spinner) findViewById(R.id.school);

        mCourse = (Spinner) findViewById(R.id.course);

        mAbout = (EditText) findViewById(R.id.about);

        mInterest = (Button) findViewById(R.id.select_interest);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        listItems = getResources().getStringArray(R.array.subject_interests);
        checkedItems = new boolean[listItems.length];
        checkedItemsBoolean();
        getUserInfo();



        getUserType();

        mInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
                tinydb = new TinyDB(SettingsActivity.this);
                selected_categories = tinydb.getListString("selected");

                for(int i=0;i<checkedItems.length;i++){
                    if(selected_categories.contains((String)String.valueOf(i)))
                        checkedItems[i]=true;
                }

                mBuilder.setTitle(title);

                /*String i= mUserDatabase.child("interest").toString();
                int value=Integer.parseInt(i);
                mUserItems.add(value);*/

                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {


                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {


                        if(isChecked) {
                            if(!selected_categories.contains((String)String.valueOf(position))){
                                selected_categories.add(String.valueOf(position));
                                checkedItems[position]=true;
                            }
                            mUserItems.add(position);
                            mUserItems1.remove((Integer.valueOf(position)));
                        }
                        //unchecked
                        else if (selected_categories.contains((String)String.valueOf(position))) {
                            // Else, if the item is already in the array, remove it
                            selected_categories.remove((String)String.valueOf(position));
                            checkedItems[position]=false;

                            mUserItems.remove((Integer.valueOf(position)));
                            mUserItems1.add(position);
                        }

                    }
                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                        tinydb.putListString("selected",selected_categories);

                        String item = "";
                        String item1 = "";
                        String itemDb= "";

                        for(int i=0;i<checkedItems.length;i++){
                            if(selected_categories.contains((String)String.valueOf(i))) {
                                item = item + listItems[i];
                                itemDb=listItems[i];
                                mUserDatabase.child("interest").child(itemDb).setValue(true);
                                if (i != selected_categories.size() - 1) {

                                    item = item + ", ";
                                }
                            }

                        }

                        for(int i=0;i<checkedItems.length;i++){
                            if(!selected_categories.contains((String)String.valueOf(i))) {
                                itemDb=listItems[i];
                                mUserDatabase.child("interest").child(itemDb).setValue(false);
                            }
                        }

                        mUserDatabase.child("interest").child("string").setValue(null);
                        mUserDatabase.child("interest_string").setValue(item);

                  /*      for (int i = 0; i < selected_categories.size(); i++) {
                            item = item + listItems[mUserItems.get(i)];
                            itemDb = listItems[mUserItems.get(i)];
                            mUserDatabase.child("interest").child(itemDb).setValue(true);
                            if (i != mUserItems.size() - 1) {

                                item = item + ", ";
                            }
                        }
                        for (int i = 0; i < mUserItems1.size(); i++) {
                            item1 = item1 + listItems[mUserItems1.get(i)];
                            itemDb = listItems[mUserItems1.get(i)];
                            mUserDatabase.child("interest").child(itemDb).setValue(false);
                            if (i != mUserItems1.size() - 1) {

                                item1 = item1 + ", ";
                            }
                        }mUserDatabase.child("interest").child("string").setValue(null);
                        mItemSelected.setText(item);

                        mUserDatabase.child("interest").child("string").setValue(item); */

                    }
                });

                mBuilder.setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

//              mBuilder.setNeutralButton(R.string.clear_all_label, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        for (int i = 0; i < checkedItems.length; i++) {
//                            checkedItems[i] = false;
//                            mUserItems.clear();
//                            mItemSelected.setText("");
//                        }
//                    }
//               });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();

            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkedItemsBoolean() {
        for(int i=0; i<listItems.length; i++){
            checkedItems[i]= false;
        }
    }

    private void getUserType() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("type") != null) {
                        type = map.get("type").toString();
                        if (type.equals(learner)==true){
                            title=titleLearner;
                        }
                        else  {
                            title=titleTutor;
                        }

                    }

                }
            }
                @Override
                public void onCancelled(DatabaseError databaseError) {

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
                    if(map.get("school_pos")!=null){
                        school1=map.get("school_pos").toString();
                        // index1=Integer.valueOf(map.get("school_pos").toString());
                        index1=Integer.parseInt(school1);
                        mSchool.setSelection(index1);


                    }
                    if(map.get("course_pos")!=null){
                        course1=map.get("course_pos").toString();
                        index2=Integer.parseInt(course1);
                        mCourse.setSelection(index2);
                    }
                    if(map.get("about")!=null){
                        about=map.get("about").toString();
                        mAbout.setText(about);
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

    private void saveUserInformation() {
        name = mNameField.getText().toString();
        phone = mPhoneField.getText().toString();
        school = mSchool.getSelectedItem().toString();
        int school2=mSchool.getSelectedItemPosition();
        course = mCourse.getSelectedItem().toString();
        int course2 = mCourse.getSelectedItemPosition();
        about = mAbout.getText().toString();
        if (TextUtils.isEmpty(mNameField.getText().toString()) || TextUtils.isEmpty((mSchool.getSelectedItem().toString()))
                || TextUtils.isEmpty((mCourse.getSelectedItem().toString())) ) {
            Toast.makeText(SettingsActivity.this, "Input error. Please complete required fields.", Toast.LENGTH_LONG).show();

        }

        else {
            Map userInfo = new HashMap();
            userInfo.put("name", name);
            userInfo.put("phone", phone);
            userInfo.put("school", school);
            userInfo.put("school_pos", school2);
            userInfo.put("course", course);
            userInfo.put("course_pos", course2);
            userInfo.put("about", about);
            mUserDatabase.updateChildren(userInfo);

            if (resultUri != null) {
                StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
                Bitmap bitmap = null;

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filepath.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();
                    }
                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Map userInfo = new HashMap();
                        userInfo.put("profileImageUrl", downloadUrl.toString());
                        mUserDatabase.updateChildren(userInfo);

                        finish();
                        return;
                    }
                });

            }
            finish();
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}
