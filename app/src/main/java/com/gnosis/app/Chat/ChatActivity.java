package com.gnosis.app.Chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gnosis.app.MainActivity;
import com.gnosis.app.ReportModule;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.gnosis.app.Matches.MatchesActivity;
import com.gnosis.app.Matches.MatchesAdapter;
import com.gnosis.app.Matches.MatchesObject;
import com.gnosis.app.R;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    private EditText mSendEditText;

    private NestedScrollView nestedScrollView;

    private Button mSendButton;

    private String currentUserID, matchId, chatId, mName, notif_ID, sendMessageText;

    private FirebaseAuth mAuth;

    private String currentUId, name, profileImageUrl, school, course,about, interest, current_name;

    private TextView textView, mNameField, mSchool, mCourse, mAbout, mAboutTitle, mInterest, mInterestTitle;

    private ImageView mProfileImage, info;

    private DatabaseReference usersDb, mUserDatabase;

    DatabaseReference mDatabaseUser, mDatabaseChat, mDatabaseUser1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mName = getIntent().getExtras().getString("name");
        matchId = getIntent().getExtras().getString("matchId");

        getSupportActionBar().setTitle(mName);

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new ChatActivity.ExampleNotificationReceivedHandler())
                .setNotificationOpenedHandler(new ChatActivity.ExampleNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("connections").child("matches").child(matchId).child("ChatId");
        mDatabaseChat = FirebaseDatabase.getInstance().getReference().child("Chat");
        usersDb= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mDatabaseUser1 = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId);

        getNotifID();
        getName(); //for current_name

        //ONESIGNAL SEND TAGS
        JSONObject tags = new JSONObject();
        try {
            tags.put("currentUID", currentUserID);
            tags.put("current_name", current_name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        OneSignal.sendTags(tags);
        //ONESIGNAL SEND TAGS


        getChatId();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(ChatActivity.this);

        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScroll);



        mRecyclerView.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);

        mRecyclerView.setAdapter(mChatAdapter);

        nestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },1000);

        mSendEditText = findViewById(R.id.message);
        mSendButton = findViewById(R.id.send);

        mSendEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nestedScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                },1000);
            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                nestedScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                },1000);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(mSendButton.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    private void sendMessage() {
            sendMessageText = mSendEditText.getText().toString();

        if(!sendMessageText.isEmpty()){
            DatabaseReference newMessageDb = mDatabaseChat.push();

            Map newMessage = new HashMap();
            newMessage.put("createdByUser", currentUserID);
            newMessage.put("text", sendMessageText);

            newMessageDb.setValue(newMessage);

            //ONESIGNAL NOTIFICATION
            OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
            String userID = notif_ID;
            String username = current_name;

            boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();


            if (!isSubscribed)
                return;

            try {
               OneSignal.postNotification(new JSONObject("{'contents': {'en': \""+ username + ": " +sendMessageText + "\"}, " +
                                "'include_player_ids': ['" + userID + "'], " +
                                "'headings': {'en': 'New Message'}, " +
                                "'data': {'userID': \""+ currentUserID + "\", 'name': \""+ current_name + "\"}," +
                                "'buttons': [{'id': 'id1', 'text': 'Open Gnosis Chat'}]}"),
                        new OneSignal.PostNotificationResponseHandler() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                Log.i("OneSignalExample", "postNotification Success: " + response);
                            }

                            @Override
                            public void onFailure(JSONObject response) {
                                Log.e("OneSignalExample", "postNotification Failure: " + response);
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //ONESIGNAL NOTIFICATION

        }
        mSendEditText.setText(null);
    }

    private void getChatId(){
        mDatabaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    chatId = dataSnapshot.getValue().toString();
                    mDatabaseChat = mDatabaseChat.child(chatId);
                    getChatMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getChatMessages() {
        mDatabaseChat.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    String message = null;
                    String createdByUser = null;

                    if(dataSnapshot.child("text").getValue()!=null){
                        message = dataSnapshot.child("text").getValue().toString();
                    }
                    if(dataSnapshot.child("createdByUser").getValue()!=null){
                        createdByUser = dataSnapshot.child("createdByUser").getValue().toString();
                    }

                    if(message!=null && createdByUser!=null){
                        Boolean currentUserBoolean = false;
                        if(createdByUser.equals(currentUserID)){
                            currentUserBoolean = true;
                        }
                        ChatObject newMessage = new ChatObject(message, currentUserBoolean);
                        resultsChat.add(newMessage);
                        mChatAdapter.notifyDataSetChanged();

                        nestedScrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        },1000);
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

    //back button
    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();

        if (id== android.R.id.home){
            this.finish();
        }

        //send chatId data to videoChatActivity
        if (id == R.id.mybutton) {
            //ONESIGNAL NOTIFICATION
            OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
            String userID = notif_ID;
            String username = current_name;

            try {
                OneSignal.postNotification(new JSONObject("{'contents': {'en': \""+ username + " is calling you  \"}, " +
                                "'include_player_ids': ['" + userID + "'], " +
                                "'headings': {'en': 'Video Chat'}, " +
                                "'data': {'chatId': \""+ chatId + "\", 'name': \""+ current_name + "\"}," +
                                "'buttons': [{'id': 'id2', 'text': 'Open Gnosis Video Chat'}]}"),
                        new OneSignal.PostNotificationResponseHandler() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                Log.i("OneSignalExample", "postNotification Success: " + response);
                            }

                            @Override
                            public void onFailure(JSONObject response) {
                                Log.e("OneSignalExample", "postNotification Failure: " + response);
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //ONESIGNAL NOTIFICATION

            Intent intent=new Intent(this,VideoChatViewActivity.class);
            Bundle b = new Bundle();
            b.putString("chatId", chatId);
            b.putString("name", mName);
            intent.putExtras(b);
            startActivity(intent);

        }
        //profile
        if (id == R.id.account_profile) {
            showAlertbox(matchId);

        }
        //report user
        if (id == R.id.action_report) {
            Intent intent=new Intent(this,ReportModule.class);
            Bundle c = new Bundle();
            c.putString("reportedUID", matchId);
            intent.putExtras(c);
            startActivity(intent);

        }
        //block user
        if (id == R.id.action_block) {
            new AlertDialog.Builder(this)
                    .setTitle("Block")
                    .setMessage("This action can not be undone and will delete all your conversations between each other." +
                            " Are you sure you want to permanently block this user? ")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            DatabaseReference dMatches1 = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID).child("connections").child("matches").child(matchId);
                            dMatches1.removeValue();

                            DatabaseReference dYeps1 = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID).child("connections").child("yeps").child(matchId);
                            dYeps1.removeValue();

                            DatabaseReference dMatches2 = FirebaseDatabase.getInstance().getReference("Users").child(matchId).child("connections").child("matches").child(currentUserID);
                            dMatches2.removeValue();

                            DatabaseReference dYeps2 = FirebaseDatabase.getInstance().getReference("Users").child(matchId).child("connections").child("yeps").child(currentUserID);
                            dYeps2.removeValue();
                            Toast.makeText(ChatActivity.this, "You have successfully blocked this user.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();

        }
        return super.onOptionsItemSelected(item);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void showAlertbox(String matchId) {
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

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(matchId);
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


    private ArrayList<ChatObject> resultsChat = new ArrayList<ChatObject>();
    private List<ChatObject> getDataSetChat() {
        return resultsChat;
    }

    private void getNotifID() {
        mDatabaseUser1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("notif_ID") != null) {
                        notif_ID = map.get("notif_ID").toString();
                    }
                    else{
                        //For debug
                        Toast.makeText(ChatActivity.this, "No Notification ID found for this person.", Toast.LENGTH_SHORT).show();

                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getName() {
        usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        current_name = map.get("name").toString();
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //ONESIGNAL
    private class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            String notificationID = notification.payload.notificationID;
            String title = notification.payload.title;
            String body = notification.payload.body;
            String smallIcon = notification.payload.smallIcon;
            String largeIcon = notification.payload.largeIcon;
            String bigPicture = notification.payload.bigPicture;
            String smallIconAccentColor = notification.payload.smallIconAccentColor;
            String sound = notification.payload.sound;
            String ledColor = notification.payload.ledColor;
            int lockScreenVisibility = notification.payload.lockScreenVisibility;
            String groupKey = notification.payload.groupKey;
            String groupMessage = notification.payload.groupMessage;
            String fromProjectNumber = notification.payload.fromProjectNumber;
            String rawPayload = notification.payload.rawPayload;

            String customKey;

            Log.i("OneSignalExample", "NotificationID received: " + notificationID);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }
        }
    }

    private class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;
            String launchUrl = result.notification.payload.launchURL; // update docs launchUrl

            String userID= null;
            String name= null;
            Object activityToLaunch = MainActivity.class;

            if (data != null) {
                name = data.optString("name", null);
                userID = data.optString("userID", null);

                if (name != null)
                    Log.i("OneSignalExample", "name set with value: " + name);

                if (userID != null)
                    Log.i("OneSignalExample", "userID set with value: " + userID);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken) {
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

                if (result.action.actionID.equals("id1")) {
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
                    activityToLaunch = ChatActivity.class;
                }
                else
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
            }

            Intent intent = new Intent(getApplicationContext(), (Class<?>) activityToLaunch);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("matchId", userID);
            intent.putExtra("name", name);
            Log.i("OneSignalExampleChat", "name = " + name);
            Log.i("OneSignalExample", "matchId = " + userID);
            startActivity(intent);


        }
    }
    //ONESIGNAL
}
