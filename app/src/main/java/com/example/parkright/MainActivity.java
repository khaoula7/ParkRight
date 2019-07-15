package com.example.parkright;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Click on Report Violation button
        Button reportBtn = findViewById(R.id.report_btn);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open HowTo screen (Tips and instructions on how to use the app)
                Intent intent = new Intent(MainActivity.this, HowTo.class);
                startActivity(intent);
            }
        });
    }


}

