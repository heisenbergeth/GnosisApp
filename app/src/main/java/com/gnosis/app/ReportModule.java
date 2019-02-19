package com.gnosis.app;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gnosis.app.Chat.ChatActivity;
import com.gnosis.app.Chat.ChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ReportModule extends AppCompatActivity {

    private String mReportedUID, mReporterUID, userId, complaintDetails;
    private EditText mReportedEditText, mComplaint;
    private TextView mReporterTextView;
    private Button mReport;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mDatabaseReports = FirebaseDatabase.getInstance().getReference().child("Reports");

        mReportedUID = getIntent().getExtras().getString("reportedUID");

        mReportedEditText = findViewById(R.id.reportedUID);
        mReporterTextView = findViewById(R.id.reporterUID);
        mComplaint=findViewById(R.id.complaint);
        mReport=findViewById(R.id.reportButton);

        mReportedEditText.setText(mReportedUID);
        mReporterTextView.setText(userId);


        mReport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mComplaint.getText().toString())) {
                    Toast.makeText(ReportModule.this, "Please enter a detailed complaint on the given fields.", Toast.LENGTH_LONG).show();

                } else {
                    complaintDetails = mComplaint.getText().toString();

                    DatabaseReference newReportDb = mDatabaseReports.push();

                    Map reportInfo = new HashMap();
                    reportInfo.put("Reported UID", mReportedUID);
                    reportInfo.put("Reporter UID", userId);
                    reportInfo.put("Complaint Details", complaintDetails);

                    newReportDb.setValue(reportInfo);
                    Toast.makeText(ReportModule.this, "You have successfully reported this user. Your complaint will be reviewed by moderators. ", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });


    }



}
