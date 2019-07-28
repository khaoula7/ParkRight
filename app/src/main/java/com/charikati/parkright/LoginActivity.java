package com.charikati.parkright;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /**
     * Go back to previous screen: activity_map
     * @param v View
     */
    public void goBack(View v){
        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
        startActivity(intent);
    }
}
