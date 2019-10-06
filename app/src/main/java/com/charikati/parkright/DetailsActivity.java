package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";
    private TextView typeTextView;
    private TextView statusTextView;
    private TextView reasonTextView;
    private TextView dateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Display Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.details_name);

        String jsonMyObject;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("report");
            //Convert JSON object into a ViolationReport object
            ViolationReport violationReport = new Gson().fromJson(jsonMyObject, ViolationReport.class);
            Log.d(TAG, "Type: " + violationReport.getType());
            typeTextView = findViewById(R.id.type_textView);
            typeTextView.setText(violationReport.getType());
            statusTextView = findViewById(R.id.status_textView);
            statusTextView.setText(violationReport.getStatus());
            if(violationReport.getStatus() == "Declined"){
                reasonTextView = findViewById(R.id.reason_textView);
                reasonTextView.setVisibility(View.VISIBLE);
                reasonTextView.setText(violationReport.getDeclineReason());
            }
            dateTextView = findViewById(R.id.date_textView);
            String date = violationReport.getSendingTime();
            date = date.substring(0, date.length()-3);
            dateTextView.setText(date);




        }

    }
}
