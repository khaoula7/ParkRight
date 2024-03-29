package com.charikati.parkright;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import id.zelory.compressor.Compressor;

public class PhotoActivity extends AppCompatActivity {
    //private static final String TAG = "PhotoActivity";
    // Constants for camera intent
    static final int REQUEST_IMAGE_CAPTURE_1 = 1;
    static final int REQUEST_IMAGE_CAPTURE_2 = 2;
    static final int REQUEST_IMAGE_CAPTURE_3 = 3;
    private ImageButton mFirstCameraButton;
    private ImageButton mSecondCameraButton;
    private ImageButton mThirdCameraButton;
    private boolean mFirstPhotoCaptured = false;
    private boolean mSecondPhotoCaptured = false;
    private boolean mThirdPhotoCaptured = false;
    //Extras
    private String mViolationType;
    private int mViolationIndex;
    private int mViolationResource;
    private String mFilePath1;
    private String mFilePath2;
    private String mFilePath3;
    private File mPhotoFile;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.activity_toolbar_title);
        toolbarTitle.setText(R.string.photos);
        // Display Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        //Implement steppers
        TextView twoTxt = findViewById(R.id.two_txt);
        twoTxt.setTextColor(getResources().getColor(R.color.white));
        twoTxt.setBackgroundResource(R.drawable.active_text_style);
        //Open sharedPrefs file at the given filename (sharedPrefFile) with the mode MODE_PRIVATE.
        String sharedPrefFile = "com.charikati.parkright";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        //ImageButtons
        mFirstCameraButton = findViewById(R.id.camera_1_btn);
        mSecondCameraButton = findViewById(R.id.camera_2_btn);
        mThirdCameraButton = findViewById(R.id.camera_3_btn);
        // Checkboxes
        final CheckBox plateCheckBox = findViewById(R.id.plate_chk_box);
        final CheckBox contextCheckBox = findViewById(R.id.context_check_box);
        //Get information sent by TypeActivity from intent
        //This is the first time PhotoActivity is accessed
        Bundle mExtras = getIntent().getExtras();
        if (mExtras != null) {
            mViolationType = mExtras.getString("VIOLATION_TYPE");
            mViolationIndex = mExtras.getInt("VIOLATION_INDEX");
            mViolationResource = mExtras.getInt("VIOLATION_RESOURCE");

        }else {
            //Display already taken photos from sharedPrefs file
            //coming from Up or back button
            String file_1 = mPreferences.getString("FILE_NAME_1", null);
            if (file_1 != null)
                displayImages(mFirstCameraButton, file_1);
            String file_2 = mPreferences.getString("FILE_NAME_2", null);
            if(file_2 != null)
                displayImages(mSecondCameraButton, file_2);
            String file_3 = mPreferences.getString("FILE_NAME_3", null);
            if(file_3 != null)
                displayImages(mThirdCameraButton, file_3);
            //Restore show Tips dialog
            mViolationType = mPreferences.getString("VIOLATION_TYPE", null);
            //I need it to link tips to violation type
            mViolationIndex = mPreferences.getInt("VIOLATION_INDEX", 0);
            plateCheckBox.setChecked(true);
            contextCheckBox.setChecked(true);
            mFirstPhotoCaptured = true;
            mSecondPhotoCaptured = true;
            mThirdPhotoCaptured = true;
        }
        //Click on show button will open tips dialog
        Button showBtn = findViewById(R.id.show_btn);
        showBtn.setOnClickListener(v -> showTipsDialog());
        //Click on first camera button will send a camera intent
        mFirstCameraButton.setOnClickListener(v -> dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_1));
        //Click on second camera button will send a camera intent
       mSecondCameraButton.setOnClickListener(v -> dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_2));
        //Click on third camera button will send a camera intent
        mThirdCameraButton.setOnClickListener(v -> dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_3));

        //Click on map_continue_btn button will open MapActivity
        Button photoContinueBtn = findViewById(R.id.photo_continue_btn);
        photoContinueBtn.setOnClickListener(v -> {
                if(!mFirstPhotoCaptured|| !mSecondPhotoCaptured|| !mThirdPhotoCaptured)
                    Toast.makeText(PhotoActivity.this, R.string.photos_warning, Toast.LENGTH_LONG).show();
                else if (!plateCheckBox.isChecked() || !contextCheckBox.isChecked())
                    Toast.makeText(PhotoActivity.this, R.string.checkboxes_warning, Toast.LENGTH_LONG).show();
                else
                    checkLocationPermission();
        });
    }

    /**
     * Create a File to store the taken image in app private folder
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
       //mCurrentPhotoPath = imageFile.getAbsolutePath();
        mPhotoFile = imageFile;
        return imageFile;
    }

    /**
     *Take a picture and provide file for saving it
     */
    private void dispatchTakePictureIntent(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.charikati.parkright.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    /**
     * Gets the result of the photo Intent.
     * Compress image and display it in its appropriate ImageView
     * Add the path to intent bundle
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String compressedImagePath;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE_1 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mFirstPhotoCaptured = true;
            //Compress image file and display it
            compressedImagePath = compressImages(mPhotoFile);
            mFilePath1 = compressedImagePath;
            displayImages(mFirstCameraButton, compressedImagePath);
            //Display real image in its appropriate ImageView
            /*mFilePath1 = mPhotoFile.getAbsolutePath();
            displayImages(mFirstCameraButton, mFilePath1);*/
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_2 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mSecondPhotoCaptured = true;
            //Compress image file and display it
            compressedImagePath = compressImages(mPhotoFile);
            mFilePath2 = compressedImagePath;
            displayImages(mSecondCameraButton, compressedImagePath);
            //Display real image in its appropriate ImageView
            /*mFilePath2 = mPhotoFile.getAbsolutePath();
            displayImages(mSecondCameraButton, mFilePath2);*/
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_3 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mThirdPhotoCaptured = true;
            //Compress image file and display it
            compressedImagePath = compressImages(mPhotoFile);
            mFilePath3 = compressedImagePath;
            displayImages(mThirdCameraButton, compressedImagePath);
            //Display real image in its appropriate ImageView
            /*mFilePath3 = mPhotoFile.getAbsolutePath();
            displayImages(mThirdCameraButton, mFilePath3);*/
        }
    }

    /**
     * Display image in ImageView using Glide image library
     * @param imageView where to display the image in layout
     * @param fileName of the image stored privately on disk
     */
    private void displayImages(ImageView imageView, String fileName) {
        Glide.with(this)
                    .load(fileName).centerCrop().into(imageView);
    }

    /**
     * compress images using Compressor library
     */
    private String compressImages(File photoFile){
        File compressedImageFile = new Compressor.Builder(this)
                .setMaxWidth(2016)
                .setMaxHeight(2688)
                .setQuality(100)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_PICTURES)).getAbsolutePath())
                .build()
                .compressToFile(photoFile);
        return compressedImageFile.getAbsolutePath();
    }

    /**
     * Builds the tips_dialog to show tips on how to take photos
     */
    private void showTipsDialog() {
        //Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Inflate the layout
        View inflater = getLayoutInflater().inflate(R.layout.tips_dialog, null);
        //set the layout for the AlertDialog
        builder.setView(inflater);
        //Create the Tip Dialog
        final AlertDialog tipDialog = builder.create();
        //Get the violation image and type from the incoming Intent and set it to ImageView and TextView
        ImageView violationImage = inflater.findViewById(R.id.image);
        violationImage.setImageResource(mViolationResource);
        TextView violationTxt = inflater.findViewById(R.id.message_txt);
        violationTxt.setText(mViolationType);
        //Set transparent background to the window
        Objects.requireNonNull(tipDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Show the tip dialog
        tipDialog.show();
        //Close tip dialog
        ImageView closeImg = inflater.findViewById(R.id.close_img);
        closeImg.setOnClickListener(v -> tipDialog.dismiss());
    }

    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(PhotoActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(PhotoActivity.this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PhotoActivity.this, LocationActivity.class));
        }else {
            // Dexter is an android library that simplifies the process of requesting permissions at runtime.
            Dexter.withActivity(PhotoActivity.this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            //Toast.makeText(PhotoActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhotoActivity.this, LocationActivity.class));
                        }
                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if(response.isPermanentlyDenied()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this);
                                builder.setTitle("Permission Denied")
                                        .setMessage("Permission to access device location is permanently denied. you need to go to Settings to allow the permission.")
                                        .setNegativeButton("Cancel", null)
                                        .setPositiveButton("OK", (dialog, which) -> {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                                        })
                                        .show();
                            } else {
                                Toast.makeText(PhotoActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .check();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_btn) {
            showTipsDialog();
        }
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * Write image file paths to shared Preferences
     */
    @Override
    protected void onPause() {
        super.onPause();
        //shared preferences editor is required to write to the shared preferences object
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("VIOLATION_TYPE", mViolationType);
        preferencesEditor.putInt("VIOLATION_INDEX", mViolationIndex);
        if(mFilePath1 != null)
            preferencesEditor.putString("FILE_NAME_1", mFilePath1);
        if(mFilePath2 != null)
            preferencesEditor.putString("FILE_NAME_2",mFilePath2);
        if(mFilePath3 != null)
            preferencesEditor.putString("FILE_NAME_3",mFilePath3);
        preferencesEditor.apply();
    }


}
