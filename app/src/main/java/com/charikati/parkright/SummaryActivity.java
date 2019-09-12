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
import android.widget.CheckBox;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class SummaryActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = "SummaryActivity";

    private Spinner mSpinner;
    private Bundle mExtras;
    private String[] fileNameArray;
    private ImageView[] imageViewArray;
    private String[] downloadUrlArray;
    private CheckBox termsCheckBox;

    private Button mSendButton;

    /* Firebase instance variables */
    private FirebaseAuth mAuth;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mViolationsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
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
        /*Initialize Firebase components  */
        mAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        //Set Reference  to the violation_photos folder location
        mPhotosStorageReference = mFirebaseStorage.getReference().child("violation_photos");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mViolationsDatabaseReference = mFirebaseDatabase.getReference().child("Violations");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");


        //Retrieve data from intent
        mExtras = getIntent().getExtras();
        //Array of image files names
        fileNameArray = new String[]{mExtras.getString("FILE_NAME_1"),
                mExtras.getString("FILE_NAME_2"),
                mExtras.getString("FILE_NAME_3")};

        //Array of ImageViews references
        imageViewArray = new ImageView[]{findViewById(R.id.image_1),
                findViewById(R.id.image_2),
                findViewById(R.id.image_3)};

        //Array of images downloadUrl
        downloadUrlArray = new String[3];

        //Fill the Spinner with values from string-array resource
        mSpinner = findViewById(R.id.spinner);
        String[] violations = getResources().getStringArray(R.array.violation_types);
        ArrayAdapter<String> violationAdapter=new ArrayAdapter<String>(this,R.layout.spinner_item, violations);
        mSpinner.setAdapter(violationAdapter);
        //Get violation index from bundle and set it as selected item in the spinner
        int spinnerPosition = mExtras.getInt("VIOLATION_INDEX");
        mSpinner.setSelection(spinnerPosition);
        
        termsCheckBox = findViewById(R.id.terms_chk_box);

        //Display images in layout
        displayImages();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Implement Send Button
        mSendButton = findViewById(R.id.send_btn);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!termsCheckBox.isChecked()){
                    Toast.makeText(SummaryActivity.this, R.string.checkbox_warning, Toast.LENGTH_SHORT).show();
                }else {
                    //Upload images to Firebase Storage and write violation data in database
                    //uploadImagetoFirebase();
                    Intent intent = new Intent(SummaryActivity.this, ThankyouActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Display image in ImageView using Glide image library
     */
    private void displayImages() {
        for(int i = 0; i<3; i++)
            Glide.with(this).load(fileNameArray[i]).centerCrop().into(imageViewArray[i]);
    }

    /**
     *Display current location marker in google maps
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Get location latitude and longitude
        double latitude = mExtras.getDouble("LATITUDE");
        double longitude = mExtras.getDouble("LONGITUDE");
        // Add a marker in Frankfurt and move the camera
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in violation location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    /**
     * upload the 3 images to Firebase storage under violations_photos folder
     */
    private void uploadImagetoFirebase(){
        Uri fileUri;
        showProgressDialog("Uploading photos and sending report");
        for(int i = 0; i<3; i++) {
            fileUri = Uri.fromFile(new File(fileNameArray[i]));
            StorageReference photoRef = mPhotosStorageReference.child(fileUri.getLastPathSegment());
            UploadTask uploadTask = photoRef.putFile(fileUri);

            // Register observers to listen for when the download is done or if it fails
            final int finalI = i;
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle unsuccessful uploads
                    Toast.makeText(SummaryActivity.this, "upload failed " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(SummaryActivity.this, "Upload Succeeded", Toast.LENGTH_SHORT).show();
                    //Get downloadUrl
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // Continue with the task to get the download URL

                            return photoRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                //Get the download URL of the uploaded image
                                //downloadUrlArray[finalI] = task.getResult().toString();
                                Log.d(TAG, task.getResult().toString());
                                //Get download url
                                downloadUrlArray[finalI] = task.getResult().toString();
                                //if all images has been successfully uploaded
                                if(finalI == 2){
                                    hideProgressDialog();
                                    storeViolation();

                                }

                            } else {
                                Log.d(TAG, "No download URI");
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Send Violation information to database
     */
    private void storeViolation(){
        String type = mExtras.getString("VIOLATION_TYPE");
        String status = "PENDING";
        double latitude = mExtras.getDouble("LATITUDE");
        double longitude = mExtras.getDouble("LONGITUDE");
        Calendar calendar = Calendar.getInstance();
        //Get current Date
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-YYYY");
        String currentDate = currentDateFormat.format(calendar.getTime());
        //Get current Time
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = currentTimeFormat.format(calendar.getTime());
        String sending_time = currentDate + " " + currentTime;
        //Generate unique Firebase key
        String violationId = mViolationsDatabaseReference.push().getKey();
        //Create ViolationReport object
        ViolationReport violationReport = new ViolationReport(
                type, status, downloadUrlArray[0], downloadUrlArray[1], downloadUrlArray[2], latitude, longitude, sending_time);
        //Send to Violations segment in database
        mViolationsDatabaseReference.child(violationId).setValue(violationReport)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(SummaryActivity.this, "Writing Violation in database Succeeded", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Writing in database Violations segment Succeeded");
                    //Add violation Id to user's sent violations
                    mUsersDatabaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("sent_violations").child(violationId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d(TAG, "Writing in database Users segment Succeeded");
                                //Go to Thankyou page
                                Intent intent = new Intent(SummaryActivity.this, ThankyouActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

                }else{
                    Log.w(TAG, "Writing in database failed");
                }
            }
        });
    }

    /**
     * Inflate menu layout
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    /**
     * Implement menu icons (up button and sign out) behaviour
     */
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
                //Sign out from google
                //mGoogleSignInClient.signOut();
                //Sign out from Facebook
                //LoginManager.getInstance().logout()
                intent = new Intent(SummaryActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Go back to previous screen: activity_map
     */
    public void goBack(){
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
        //goBack();
    }


}
