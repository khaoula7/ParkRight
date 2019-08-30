package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileInputStream;
import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button mLogOutButton;
    private FirebaseAuth mAuth;
    private Spinner mSpinner;
    private ArrayList mViolationData;
    private Bundle mExtras;
    private ImageView mFirstImage;
    private ImageView mSecondImage;
    private ImageView mThirdImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mFirstImage = findViewById(R.id.image_1);
        mSecondImage = findViewById(R.id.image_2);
        mThirdImage = findViewById(R.id.image_3);

        //Get the bundle from the intent
        mExtras = getIntent().getExtras();

        //Fill the Spinner with values from string-array resource
        mSpinner = findViewById(R.id.spinner);
        String[] violations = getResources().getStringArray(R.array.violation_types);
        ArrayAdapter<String> violationAdapter=new ArrayAdapter<String>(this,R.layout.spinner_item, violations);
        mSpinner.setAdapter(violationAdapter);

        //Get violation index from bundle and set it as selected item in the spinner
        int spinnerPosition = mExtras.getInt("VIOLATION_INDEX");
        mSpinner.setSelection(spinnerPosition);

        //Get images filename from bundle and display it
        displayImages(mFirstImage, mExtras.getString("FILE_NAME_1"));
        displayImages(mSecondImage, mExtras.getString("FILE_NAME_2"));
        displayImages(mThirdImage, mExtras.getString("FILE_NAME_3"));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Decode bitmap image from file stream and display it in its ImageView
     * @param imageView where to display the image in layout
     * @param fileName of the image stored privately on disk
     */
    private void displayImages(ImageView imageView, String fileName) {
        Bitmap bmp = null;
        try {

            FileInputStream is = this.openFileInput(fileName);
            bmp = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bmp);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Get location latitude and longitude
        double latitude = mExtras.getDouble("LATITUDE");
        double longitude = mExtras.getDouble("LONGITUDE");
        //Toast.makeText(this, "Latitude= "+latitude + " Longitude= "+ longitude, Toast.LENGTH_LONG).show();

        // Add a marker in Frankfurt and move the camera
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in violation location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    /**
     * Go back to previous screen: activity_map
     * @param v View
     */
    public void goBack(View v){
        Intent intent = new Intent(SummaryActivity.this, MapsActivity.class);
        intent.putExtras(mExtras);
        startActivity(intent);
    }

    /**
     * Override back button behaviour to point towards MapsActivity
     * and not LoginActivity which is the normal behaviour
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SummaryActivity.this, MapsActivity.class);
        intent.putExtras(mExtras);
        startActivity(intent);
        return;
    }


}
