package com.gnosis.app;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.gnosis.app.Chat.ChatActivity;
import com.gnosis.app.Chat.VideoChatViewActivity;
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
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private cards cards_data[];
    private com.gnosis.app.Cards.arrayAdapter arrayAdapter;
    private boolean showingFirst = false;

    private FirebaseAuth mAuth;

    private String currentUId, name, profileImageUrl, school, course,about, interest, notif_userId, notif_ID;

    private TextView textView, mNameField, mSchool, mCourse, mAbout, mAboutTitle, mInterest, mInterestTitle;

    private ImageView mProfileImage, infologo;

    private DatabaseReference usersDb, mUserDatabase, mUserDatabase1, mDatabaseUser1;

    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;

    ListView listView;
    List<cards> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        mUserDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUId);



        checkUserType();


        textView = (TextView) findViewById(R.id.instruction);
        infologo= (ImageView) findViewById(R.id.instruction1);
        //textView.setVisibility(View.GONE);


        textView.startAnimation(fadeIn);
        textView.startAnimation(fadeOut);

        fadeIn.setDuration(900);
        fadeIn.setFillAfter(true);
        fadeOut.setDuration(1200);
        fadeOut.setFillAfter(true);
        fadeOut.setStartOffset(4200+fadeIn.getStartOffset());


        //ONESIGNAL SAVE USER_ID TO DATABASE
        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new MainActivity.ExampleNotificationReceivedHandler())
                .setNotificationOpenedHandler(new MainActivity.ExampleNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        notif_userId = status.getSubscriptionStatus().getUserId();
        mUserDatabase1.child("notif_ID").setValue(notif_userId);
        //ONESIGNAL SAVE USER_ID TO DATABASE



        infologo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    infologo.setClickable(false);
                    textView.setVisibility(View.VISIBLE);
                    textView.startAnimation(fadeIn);

                    Timer t = new Timer(false);
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textView.startAnimation(fadeOut);
                                    infologo.setClickable(true);
                                }
                            });
                        }
                    }, 5000);

                }

        });
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


    private void isConnectionMatch(final String userId) {
        //check kung nasa yep ka din niya
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        mDatabaseUser1 = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
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
                        Toast.makeText(MainActivity.this, "No Notification ID found for this person.", Toast.LENGTH_SHORT).show();

                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "You have been connected to someone! To view, click Matches.", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    //ONESIGNAL NOTIFICATION
                    OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
                    String userID = notif_userId;
                    String matchID = notif_ID;

                    boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();


                    if (!isSubscribed)
                        return;

                    try {
                        OneSignal.postNotification(new JSONObject("{'contents': {'en': 'You have been connected to someone! To view, click Matches.'}, " +
                                        "'include_player_ids': ['" + userID + "', '" + matchID +"'], " +
                                        "'headings': {'en': 'New Match'}, " +
                                        "'data': {'userID': \""+ notif_userId + "\"}," +
                                        "'buttons': [{'id': 'id3', 'text': 'Open Matches'}]}"),
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
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
            //    .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, ChooseLoginRegistrationActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                })
                .setNegativeButton("No", null)
                .show();

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
    public void goToKhanViewer(View view) {
        Intent intent = new Intent(MainActivity.this, KhanViewer.class);
        startActivity(intent);
        return;

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
            String chatId= null;
            Object activityToLaunch = MainActivity.class;

            if (data != null) {
                name = data.optString("name", null);
                userID = data.optString("userID", null);
                chatId = data.optString("chatId", null);

                if (name != null)
                    Log.i("OneSignalExample", "name set with value: " + name);

                if (userID != null)
                    Log.i("OneSignalExample", "userID set with value: " + userID);

                if (chatId != null)
                    Log.i("OneSignalExample", "chatId set with value: " + chatId);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken) {
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

                if (result.action.actionID.equals("id1")) {
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
                    activityToLaunch = ChatActivity.class;
                }
                if (result.action.actionID.equals("id2")) {
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
                    activityToLaunch = VideoChatViewActivity.class;
                }
                if (result.action.actionID.equals("id3")) {
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
                    activityToLaunch = MatchesActivity.class;
                }
                else
                    Log.i("OneSignalExample", "button id called: " + result.action.actionID);
            }

            Intent intent1 = new Intent(getApplicationContext(), (Class<?>) MainActivity.class);
            startActivity(intent1);

            Intent intent = new Intent(getApplicationContext(), (Class<?>) activityToLaunch);
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            intent.putExtra("matchId", userID);
            intent.putExtra("chatId", chatId);
            intent.putExtra("name", name);
            Log.i("OneSignalExampleMain", "name = " + name);
            Log.i("OneSignalExampleMain", "chatId = " + chatId);
            Log.i("OneSignalExample", "matchId = " + userID);
            startActivity(intent);


        }
    }
    //ONESIGNAL


}