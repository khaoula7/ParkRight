package com.charikati.parkright;

import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.charikati.parkright.model.ViolationReport;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.*;

public class SummaryActivity extends BaseActivity implements OnMapReadyCallback {
    private static final String TAG = "SummaryActivity";
    //Screen widget variables
    private String[] fileNameArray;
    private ImageView[] imageViewArray;
    private String[] downloadUrlArray;
    private CheckBox termsCheckBox;
    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private StorageReference mPhotosStorageReference;
    private FirebaseFirestore mFirestore;
    //Shared Preferences variables
    private SharedPreferences mPreferences;
    private Double mLatitude;
    private Double mLongitude;
    private String mViolationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);
        // Display Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Remove default title text
        requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.activity_toolbar_title);
        toolbarTitle.setText(R.string.report);
        //Implement steppers
        TextView twoTxt = findViewById(R.id.two_txt);
        twoTxt.setTextColor(getResources().getColor(R.color.white));
        twoTxt.setBackgroundResource(R.drawable.active_text_style);
        TextView threeTxt = findViewById(R.id.three_txt);
        threeTxt.setTextColor(getResources().getColor(R.color.white));
        threeTxt.setBackgroundResource(R.drawable.active_text_style);
        TextView fourTxt = findViewById(R.id.four_txt);
        fourTxt.setTextColor(getResources().getColor(R.color.white));
        fourTxt.setBackgroundResource(R.drawable.active_text_style);
        /*Initialize Firebase components  */
        mFirebaseAuth = FirebaseAuth.getInstance();
       FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        //Set Reference  to the violation_photos folder location
        mPhotosStorageReference = firebaseStorage.getReference().child("violation_photos");
        mFirestore = FirebaseFirestore.getInstance();
        //Open sharedPrefs file at the given filename (sharedPrefFile) with the mode MODE_PRIVATE.
        String sharedPrefFile = "com.charikati.parkright";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        //Array of image files names
        fileNameArray = new String[]{mPreferences.getString("FILE_NAME_1", null),
                mPreferences.getString("FILE_NAME_2", null),
                mPreferences.getString("FILE_NAME_3", null)};
        //Array of ImageViews references
        imageViewArray = new ImageView[]{findViewById(R.id.image_1),
                findViewById(R.id.image_2),
                findViewById(R.id.image_3)};
        //Array of images downloadUrl
        downloadUrlArray = new String[3];
        //Fill the Spinner with values from string-array resource
        Spinner spinner = findViewById(R.id.spinner);
        String[] violations = getResources().getStringArray(R.array.violation_types);
        ArrayAdapter<String> violationAdapter=new ArrayAdapter<>(this,R.layout.spinner_item, violations);
        spinner.setAdapter(violationAdapter);
        //Get violation index and set it as selected item in the spinner
        mViolationType = mPreferences.getString("VIOLATION_TYPE", null);
        int spinnerPosition = mPreferences.getInt("VIOLATION_INDEX", 0);
        spinner.setSelection(spinnerPosition);
        termsCheckBox = findViewById(R.id.terms_chk_box);
        //Display images in layout
        displayImages();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        //Get location latitude and longitude
        mLatitude = getDouble(mPreferences, "LATITUDE");
        mLongitude = getDouble(mPreferences, "LONGITUDE");
        Log.d(TAG, "Latitude: "+ mLatitude + " Longitude: "+ mLongitude );
        //Implement Send Button
        Button sendButton = findViewById(R.id.send_btn);
        sendButton.setOnClickListener(v -> {
            if(!termsCheckBox.isChecked()){
                Toast.makeText(SummaryActivity.this, R.string.checkbox_warning, Toast.LENGTH_SHORT).show();
            }else {
                //Upload images to Firebase Storage and write violation data in database
                sendReport();

                //startActivity(new Intent(SummaryActivity.this, ThankyouActivity.class));
                //Delete all sharedPreferences
//                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
//                preferencesEditor.clear();
//                preferencesEditor.apply();

            }
        });
    }

    /**
     * Display image in Layout using Glide image library
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
        // Add a marker in Frankfurt and move the camera
        LatLng location = new LatLng(mLatitude, mLongitude);
        // Enable the zoom controls for the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in violation location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));

    }

    /**
     * upload the 3 images to Firebase storage under violations_photos folder
     */
    private void sendReport(){
        Uri fileUri;
        showProgressDialog("Please wait while we send your report");
        for(int i = 0; i<3; i++) {
            fileUri = Uri.fromFile(new File(fileNameArray[i]));
            StorageReference photoRef = mPhotosStorageReference.child(requireNonNull(fileUri.getLastPathSegment()));
            UploadTask uploadTask = photoRef.putFile(fileUri);
            // Register observers to listen for when the download is done or if it fails
            final int finalI = i;
            uploadTask.addOnFailureListener(e -> {
                // Handle unsuccessful uploads
                Toast.makeText(SummaryActivity.this, "upload failed " + e.toString(), Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return photoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    //Get download url
                    downloadUrlArray[finalI] = requireNonNull(task.getResult()).toString();
                    //if all images has been successfully uploaded
                    if(finalI == 2){
                        hideProgressDialog();
                        storeViolation();
                    }
                } else {
                    Log.d(TAG, "Uploading Failed");
                    hideProgressDialog();
                }
            }));
        }
    }

    /**
     * Send Violation information to database
     */
    private void storeViolation(){
        String status = "Pending";
        //Get the current timestamp
        long sendingTime = System.currentTimeMillis();
        Log.d(TAG, "sendingTime "+ sendingTime);
        //Create ViolationReport object
        ViolationReport violationReport = new ViolationReport(
                mViolationType, status, downloadUrlArray[0], downloadUrlArray[1], downloadUrlArray[2],
                mLatitude, mLongitude, sendingTime, 0, null, null, null);
        mFirestore.collection("Violations")
                .add(violationReport)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    //Add violation Id to user's sent violations
                    Map<String, Object> violationId = new HashMap<>();
                    mFirestore.collection("Users")
                            .document(requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                            .collection("sent_violations").document(documentReference.getId()).set(violationId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Writing in database Succeeded");
                                //hideProgressDialog();
                                //Delete all sharedPreferences
                                SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                                preferencesEditor.clear();
                                preferencesEditor.apply();
                                //Go to ThankYou screen
                                startActivity(new Intent(SummaryActivity.this, ThankyouActivity.class));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error writing user", e);
                               // hideProgressDialog();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    //hideProgressDialog();
                });
    }

//    /**
//     * Inflate menu layout
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.logout_menu, menu);
//        return true;
//    }
//
//    /**
//     * Implement menu icons (up button and sign out) behaviour
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent intent;
//        if (item.getItemId() == R.id.sign_out_menu) {//Firebase Signout
//            mFirebaseAuth.signOut();
//            //Sign out from google
//            //mGoogleSignInClient.signOut();
//            //Sign out from Facebook
//            //LoginManager.getInstance().logout();
//            intent = new Intent(SummaryActivity.this, LoginActivity.class);
//            startActivity(intent);
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * Override back button behaviour to point towards MapsActivity
     * and not LoginActivity which is the normal behaviour*/
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SummaryActivity.this, LocationActivity.class));
    }

    /**
     * Get longitude and latitude from sharedPrefs file as doubles
     */
    double getDouble(final SharedPreferences prefs, final String key) {
        if (!prefs.contains(key))
            return 0.0;
        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }


}
