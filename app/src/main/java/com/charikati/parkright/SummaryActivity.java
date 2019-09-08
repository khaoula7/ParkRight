package com.charikati.parkright;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class SummaryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SummaryActivity";

    private Spinner mSpinner;
    private Bundle mExtras;
    private ImageView mFirstImage;
    private ImageView mSecondImage;
    private ImageView mThirdImage;
    private Button mSendButton;

    /* Firebase instance variables */
    private FirebaseAuth mAuth;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        /*Initialize Firebase components  */
        mAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        //Set Reference  to the violation_photos folder location
        mPhotosStorageReference = mFirebaseStorage.getReference().child("violation_photos");




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
        toolbarTitle.setText(R.string.step_4_2);

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

        //Implement Send Button
        mSendButton = findViewById(R.id.send_btn);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri file = Uri.fromFile(new File("/data/user/0/com.charikati.parkright/files/photo1.jpg"));
                StorageReference photoRef = mPhotosStorageReference.child("photo1.jpg");
                UploadTask uploadTask = photoRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(SummaryActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Toast.makeText(SummaryActivity.this, "Upload Succeeded", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
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
            Glide.with(this).load(bmp).into(imageView);
            //imageView.setImageBitmap(bmp);
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
        Toast.makeText(this, "Latitude= "+latitude + " Longitude= "+ longitude, Toast.LENGTH_LONG).show();

        // Add a marker in Frankfurt and move the camera
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in violation location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    /**
     * Go back to previous screen: activity_map
     */
    public void goBack(){
        Intent intent = new Intent(SummaryActivity.this, MapsActivity.class);
        intent.putExtras(mExtras);
        Toast.makeText(this, "Latitude = "+ mExtras.getDouble("Latitude"), Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    /**
     * Override back button behaviour to point towards MapsActivity
     * and not LoginActivity which is the normal behaviour
     */
    @Override
    public void onBackPressed() {
        //goBack();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case android.R.id.home:
                intent = new Intent(SummaryActivity.this, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            case R.id.sign_out_menu:
                mAuth.signOut();
                intent = new Intent(SummaryActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

}
