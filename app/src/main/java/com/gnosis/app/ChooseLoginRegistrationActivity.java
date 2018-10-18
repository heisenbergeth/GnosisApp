package com.gnosis.app;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {

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


    private Button mLogin, mRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        mLogin = (Button) findViewById(R.id.login);
        mRegister = (Button) findViewById(R.id.register);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check internet connection
                if (haveNetworkConnection(true)) {


                    Intent intent = new Intent(ChooseLoginRegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);

                    return;
                }
                else{
                    Toast.makeText(ChooseLoginRegistrationActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();

                }
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
}
