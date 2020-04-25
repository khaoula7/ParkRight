package com.charikati.parkright;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "LocationActivity";
    // The map object
    private GoogleMap mMap;
    // Fetches the current location of the device
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 10;
    private SharedPreferences mPreferences;
    private double mCurrentLatitude;
    private double mCurrentLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
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
        toolbarTitle.setText(R.string.location);
        //Implement steppers
        TextView twoTxt = findViewById(R.id.two_txt);
        twoTxt.setTextColor(getResources().getColor(R.color.white));
        twoTxt.setBackgroundResource(R.drawable.active_text_style);
        TextView threeTxt = findViewById(R.id.three_txt);
        threeTxt.setTextColor(getResources().getColor(R.color.white));
        threeTxt.setBackgroundResource(R.drawable.active_text_style);

        //SharedPrefs file
        String sharedPrefFile = "com.charikati.parkright";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        // Initialize Fused Location  object
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocationActivity.this);
        FloatingActionButton mLocationFAB = findViewById(R.id.location_fab);
        mLocationFAB.setOnClickListener(v -> checkGPSEnabled());
        //Click on Continue Button will open Login Activity
        Button goToLoginBtn = findViewById(R.id.confirm_button);
        goToLoginBtn.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null)
                startActivity(new Intent(LocationActivity.this, LoginActivity.class));
            else
                startActivity(new Intent(LocationActivity.this, SummaryActivity.class));
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //check if gps is enabled or not and request user to enable it
       checkGPSEnabled();
    }

    /**
     * Check if gps is enabled or not and request user to enable it
     */
    private void checkGPSEnabled(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(LocationActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(LocationActivity.this, locationSettingsResponse -> LocationActivity.this.getDeviceLocation());
        task.addOnFailureListener(LocationActivity.this, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult(LocationActivity.this, 51);
                } catch (IntentSender.SendIntentException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * Get the result of implicit intent asking user to accept enabling location
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    /**
     * Fetch the device location using Fused location object
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            // Even if the task is successful, mLastKnownLocation may be null
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                mCurrentLatitude = mLastKnownLocation.getLatitude();
                                mCurrentLongitude = mLastKnownLocation.getLongitude();
                            } else {
                                // Request for an updated location
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                // Function that will be executed when an uodated location is received
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        // Remove location updates from the callback so we don't keep getting the location updates which can increase battery consumption
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        } else {
                            Toast.makeText(LocationActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        Log.d(TAG, "Latitude in onPause: " + mCurrentLatitude + "Longitude in onPause: "+ mCurrentLongitude);
        putDouble(preferencesEditor, "LATITUDE", mCurrentLatitude);
        putDouble(preferencesEditor, "LONGITUDE", mCurrentLongitude);
        preferencesEditor.apply();
    }

    /**
     * Setter method
     * Convert the double to its 'raw long bits' equivalent and store that long
     * in order to be able to use double with sharedPrefs for latitude and longitude
     */
    void putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        edit.putLong(key, Double.doubleToRawLongBits(value));
    }


}
