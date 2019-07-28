package com.charikati.parkright;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Click on map_continue_btn Button will open Summary Activity
        Button reportBtn = findViewById(R.id.map_continue_btn);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open HowActivity screen (Tips and instructions on activity_how to use the app)
                Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Go back to previous screen: activity_type
     * @param v View
     */
    public void goBack(View v){
        Intent intent = new Intent(MapActivity.this, PhotoActivity.class);
        startActivity(intent);
    }
}
