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
    private double mLatitude;
    private double mLongitude;
    private final float DEFAULT_ZOOM = 15;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);
        // Display Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.activity_toolbar_title);
        toolbarTitle.setText(R.string.details_name);

        String jsonMyObject;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("report");
            //Convert JSON object into a ViolationReport object
            ViolationReport violationReport = new Gson().fromJson(jsonMyObject, ViolationReport.class);
            TextView typeText = findViewById(R.id.type_txt);
            typeText.setText(violationReport.getType());
            TextView statusText = findViewById(R.id.status_txt);
            statusText.setText(violationReport.getStatus());
            if(violationReport.getStatus().equals("Declined")){
                TextView reasonLabel = findViewById(R.id.reason_label);
                reasonLabel.setVisibility(View.VISIBLE);
                TextView reasonText = findViewById(R.id.reason_txt);
                reasonText.setText(violationReport.getDeclineReason());
                reasonText.setVisibility(View.VISIBLE);
            }
            TextView dateText = findViewById(R.id.date_txt);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm a");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0400"));
//            String sending_time = sdf.format(violationReport.getSendingTime());
            String date = new java.text.SimpleDateFormat("dd-MM-yyyy, hh:mm:ss aa").format(new java.util.Date (violationReport.getSendingTime()));
            dateText.setText(date);
            //Load images
            ImageView firstImage = findViewById(R.id.image_1);
            loadImage(firstImage, violationReport.getFirstImageUrl());
            ImageView secondImage = findViewById(R.id.image_2);
            loadImage(secondImage, violationReport.getSecondImageUrl());
            ImageView thirdImage = findViewById(R.id.image_3);
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
        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in violation location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }
}
