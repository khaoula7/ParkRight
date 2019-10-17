package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";
    private TextView typeTextView;
    private TextView statusTextView;
    private TextView reasonTextView;
    private TextView dateTextView;
    private ImageView firstImage;
    private ImageView secondImage;
    private ImageView thirdImage;



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
            if(violationReport.getStatus().equals("Declined")){
                reasonTextView = findViewById(R.id.reason_textView);
                reasonTextView.setVisibility(View.VISIBLE);
                reasonTextView.setText(violationReport.getDeclineReason());
            }
            dateTextView = findViewById(R.id.date_textView);
            String date = violationReport.getSendingDate() + ",   " + violationReport.getSendingTime();
            dateTextView.setText(date);

            firstImage = findViewById(R.id.first_image);
            loadImage(firstImage, violationReport.getFirstImageUrl());

            secondImage = findViewById(R.id.second_image);
            loadImage(secondImage, violationReport.getSecondImageUrl());

            thirdImage = findViewById(R.id.third_image);
            loadImage(thirdImage, violationReport.getThirdImageUrl());
        }

    }

    /**
     * Load image from Firebase storage into correspondent ImageView
     * @param imageView
     * @param imageUrl
     */
    void loadImage(ImageView imageView, String imageUrl){
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .into(imageView);
    }

    /**
     * Implement menu icons (up button and sign out) behaviour
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case android.R.id.home:
                intent = new Intent(DetailsActivity.this, MyReportsActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
