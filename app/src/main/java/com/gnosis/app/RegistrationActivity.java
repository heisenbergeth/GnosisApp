package com.gnosis.app;

import android.accessibilityservice.AccessibilityService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import android.widget.TextView.OnEditorActionListener;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import android.text.TextUtils;
import android.widget.Spinner;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private Button mRegister;
    private EditText mEmail, mPassword, mName, mRePassword;
    private TextView mLogin;

    private RadioGroup mRadioGroup;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private Spinner mSpinner1, mSpinner2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //getSupportActionBar().setTitle("Register");

        //back button
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);



        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user !=null){
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");


        mRegister = (Button) findViewById(R.id.register);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mRePassword = (EditText) findViewById(R.id.rePW);
        mName = (EditText) findViewById(R.id.name);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        mSpinner1 = (Spinner) findViewById(R.id.spinner1);
        mSpinner2 = (Spinner) findViewById(R.id.spinner2);
        mLogin = (TextView) findViewById(R.id.login);

        //Dismiss keyboard after enter key on password
        mRePassword.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(mRePassword.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(RegistrationActivity.this, ChooseLoginRegistrationActivity.class);
                    progressDialog.dismiss();
                    startActivity(intent);

            }
        });


        //Register button click event
        mRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String repassword = mRePassword.getText().toString();
                final String name = mName.getText().toString();
                final String school= mSpinner1.getSelectedItem().toString();
                final int school1= mSpinner1.getSelectedItemPosition();
                final String schoolpos = String.valueOf(school1);
                final String course= mSpinner2.getSelectedItem().toString();
                final int course1= mSpinner2.getSelectedItemPosition();
                final String coursepos = String.valueOf(course1);

                //if fields are empty
                if (!validate()) {
                    onSignupFailed();
                    return;
                }
                else {
                    mRegister.setEnabled(false);
                    progressDialog.show();

                    int selectId = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton = (RadioButton) findViewById(selectId);

                    if (radioButton.getText() == null) {
                        return;
                    }



                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(RegistrationActivity.this, "User already exists.", Toast.LENGTH_SHORT).show();
                            } else {

                                String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                Map userInfo = new HashMap<>();
                                userInfo.put("name", name);
                                userInfo.put("type", radioButton.getText().toString());
                                userInfo.put("profileImageUrl", "default");
                                userInfo.put("school", school);
                                userInfo.put("school_pos", schoolpos);
                                userInfo.put("course", course);
                                userInfo.put("course_pos", coursepos);

                                currentUserDb.updateChildren(userInfo);

                                Toast.makeText(RegistrationActivity.this,
                                        "Registration Successful.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    mRegister.setEnabled(true);
                }
            }
        });
    }


    /*back button
    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();

        if (id== android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();

        mRegister.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String repassword = mRePassword.getText().toString();
        final String name = mName.getText().toString();
        final String school= mSpinner1.getSelectedItem().toString();
        final int school1= mSpinner1.getSelectedItemPosition();
        final String schoolpos = String.valueOf(school1);
        final String course= mSpinner2.getSelectedItem().toString();
        final int course1= mSpinner2.getSelectedItemPosition();
        final String coursepos = String.valueOf(course1);

        if (name.isEmpty() || name.length() < 3) {
            mName.setError("at least 3 characters");
            valid = false;
        } else {
            mName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }


        if (password.isEmpty() || password.length() < 4 || password.length() > 6) {
            mPassword.setError("Enter between 4 and 6 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (repassword.isEmpty() || repassword.length() < 4 || repassword.length() > 6 || !(repassword.equals(password))) {
            mRePassword.setError("Password do not match");
            valid = false;
        } else {
            mRePassword.setError(null);
        }

        if (TextUtils.isEmpty((mSpinner1.getSelectedItem().toString()))
                || TextUtils.isEmpty((mSpinner2.getSelectedItem().toString())) ||mRadioGroup.getCheckedRadioButtonId()==-1) {
            Toast.makeText(RegistrationActivity.this, "Input error. Please complete all the fields.", Toast.LENGTH_LONG).show();
            valid=false;
        }

        return valid;
    }
}
