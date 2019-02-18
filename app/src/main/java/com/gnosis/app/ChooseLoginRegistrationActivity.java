package com.gnosis.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.net.ConnectivityManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChooseLoginRegistrationActivity extends AppCompatActivity {

    private Button mLogin;
    private TextView mRegister;
    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        mRegister = (TextView) findViewById(R.id.register);
        mLogin = (Button) findViewById(R.id.login);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        final ProgressDialog progressDialog = new ProgressDialog(ChooseLoginRegistrationActivity.this,
                R.style.AppTheme_Dark_Dialog1);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");


        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user !=null){
                    Intent intent = new Intent(ChooseLoginRegistrationActivity.this, MainActivity.class);
                    progressDialog.dismiss();
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (haveNetworkConnection(true)) {
                    if (!validate()) {
                        onLoginFailed();
                        return;
                    }
                }
                else{
                    Toast.makeText(ChooseLoginRegistrationActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }


                    mLogin.setEnabled(false);

                progressDialog.show();

                    final String email = mEmail.getText().toString();
                    final String password = mPassword.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(ChooseLoginRegistrationActivity.this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (!task.isSuccessful()) {
                                                mLogin.setEnabled(true);
                                                progressDialog.dismiss();

                                Toast.makeText(ChooseLoginRegistrationActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                mLogin.setEnabled(true);
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check internet connection
                if (haveNetworkConnection(true)) {
                    Intent intent = new Intent(ChooseLoginRegistrationActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                    return;
                }
                else{
                    Toast.makeText(ChooseLoginRegistrationActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //check internet connection
    private boolean haveNetworkConnection(boolean b) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

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

    public void onLoginSuccess() {
        mLogin.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mLogin.setEnabled(true);
    }
    public boolean validate() {
        boolean valid = true;

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            mPassword.setError("Enter between 4 and 20 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }


        return valid;
    }
}
