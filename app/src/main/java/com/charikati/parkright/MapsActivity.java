package com.charikati.parkright;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    // A default location (Frankfurt, Germany) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(50.1109, 8.6821);
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private FloatingActionButton mLocationFAB;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.charikati.parkright";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        toolbarTitle.setText(R.string.step_3);
        //SharedPrefs file
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //location floating action button to get the current place
        mLocationFAB = findViewById(R.id.location_fab);
        mLocationFAB.setOnClickListener(v -> pickCurrentPlace());
        //Click on map_continue_btn Button will open Login Activity
        Button mapContinueBtn = findViewById(R.id.login_button);
        mapContinueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Frankfurt, Germany.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Frankfurt and move the camera
        //Get location latitude and longitude
        mCurrentLatitude = getDouble(mPreferences, "LATITUDE", mDefaultLocation.latitude);
        mCurrentLongitude = getDouble(mPreferences, "LONGITUDE", mDefaultLocation.longitude);
        LatLng frankfurt = new LatLng(mCurrentLatitude, mCurrentLongitude);
        mMap.addMarker(new MarkerOptions().position(frankfurt).title("Marker in Frankfurt"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(frankfurt));
        // Enable the zoom controls for the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Prompt the user for permission.
        getLocationPermission();
    }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     * When the user responds to the request permission dialog,
     * this callback will be called by Android.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * If the user grants permission, the app fetches the user's latest location
     * and moves the camera to center around that location.
     * If the user denies permission,
     * the app simply moves the camera to the default location
     * defined among the constants at the beginning of this page (in the sample code, it is Frankfurt, Germany).
     */
    private void getDeviceLocation() {
         //Get the best and most recent location of the device, which may be null in rare
         //cases when a location is not available.
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mCurrentLatitude = mLastKnownLocation.getLatitude();
                            mCurrentLongitude = mLastKnownLocation.getLongitude();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mCurrentLatitude, mCurrentLongitude), DEFAULT_ZOOM));
                            //Add a marker on the new location
                            LatLng myLocation = new LatLng(mCurrentLatitude, mCurrentLongitude);
                            mMap.addMarker(new MarkerOptions().position(myLocation).title("Car Location"));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /* When the user taps the Pick Place button,
     *  this method checks for location permissions
     *  and re-prompts for permission if the user has not yet granted permission.
     *  If the user has granted permission,
     *  then the method calls getDeviceLocation to initiate the process of getting the current likely places.
     */
    private void pickCurrentPlace() {
        if (mMap == null) {
            return;
        }
        if (mLocationPermissionGranted) {
            getDeviceLocation();
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));
            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        putDouble(preferencesEditor, "LATITUDE", mCurrentLatitude);
        putDouble(preferencesEditor, "LONGITUDE", mCurrentLongitude);
        preferencesEditor.apply();
    }

    /**
     * Setter method
     * Convert the double to its 'raw long bits' equivalent and store that long
     * in order to be able to use double with sharedPrefs for latitude and longitude
     */
    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    /**
     * Getter method
     * Get longitude and latitude from sharedPrefs file by converting 'raw long bits' into its double equivalent
     */
    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if (!prefs.contains(key))
            return defaultValue;
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

}
