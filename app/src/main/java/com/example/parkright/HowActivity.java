package com.example.parkright;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class HowActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.how);
        //Click on Report Violation button
        Button skipBtn = findViewById(R.id.skip_btn);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open HowActivity screen (Tips and instructions on how to use the app)
                Intent intent = new Intent(HowActivity.this, TypeActivity.class);
                startActivity(intent);
            }
        });
    }
}
