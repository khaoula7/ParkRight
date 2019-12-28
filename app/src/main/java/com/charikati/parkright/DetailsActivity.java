package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.charikati.parkright.model.ViolationReport;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "DetailsActivity";
    private TextView typeTextView;
    private TextView statusTextView;
    private TextView reasonTextView;
    private TextView dateTextView;
    private ImageView firstImage;
    private ImageView secondImage;
    private ImageView thirdImage;
    private double mLatitude;
    private double mLongitude;
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String sending_time = sdf.format(violationReport.getSendingTime());



            dateTextView.setText(sending_time);
            //Load images
            firstImage = findViewById(R.id.first_image);
            loadImage(firstImage, violationReport.getFirstImageUrl());
            secondImage = findViewById(R.id.second_image);
            loadImage(secondImage, violationReport.getSecondImageUrl());
            thirdImage = findViewById(R.id.third_image);
            loadImage(thirdImage, violationReport.getThirdImageUrl());
            //Load Map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            //Get location latitude and longitude
            mLatitude = violationReport.getLatitude();
            mLongitude = violationReport.getLongitude();
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
     *Display current location marker in google maps
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Frankfurt and move the camera
        LatLng location = new LatLng(mLatitude, mLongitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in violation location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
