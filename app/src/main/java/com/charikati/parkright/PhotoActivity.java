package com.charikati.parkright;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = "PhotoActivity";
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

    //The only Exras I will keep
    private Bundle mExtras;
    private String mViolationType;
    private int mViolationIndex;

    private String mFilePath1;
    private String mFilePath2;
    private String mFilePath3;
    private String mCurrentPhotoPath;

    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.charikati.parkright";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Display Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.step_2);

        //Open sharedPrefs file at the given filename (sharedPrefFile) with the mode MODE_PRIVATE.
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
        mExtras = getIntent().getExtras();
        if (mExtras != null) {
            mViolationType = mExtras.getString("VIOLATION_TYPE");
            mViolationIndex = mExtras.getInt("VIOLATION_INDEX");
        }else {
            //Display already taken photos from sharedPrefs file
            //coming from Up or back button
            String file_1 = mPreferences.getString("FILE_NAME_1", null);
            if (file_1 != null) {
                displayImages(mFirstCameraButton, file_1);
            }
            String file_2 = mPreferences.getString("FILE_NAME_2", null);
            if(file_2 != null) {
                displayImages(mSecondCameraButton, file_2);
            }
            String file_3 = mPreferences.getString("FILE_NAME_3", null);
            if(file_3 != null) {
                displayImages(mThirdCameraButton, file_3);
            }
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
        Button showBtn = (Button)findViewById(R.id.show_btn);
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTipsDialog();
            }
        });
        //Click on first camera button will send a camera intent
        mFirstCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_1);
            }
        });
        //Click on second camera button will send a camera intent
       mSecondCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_2);
            }
       });
        //Click on third camera button will send a camera intent
        mThirdCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_3);
            }
        });

        //Click on map_continue_btn button will open MapActivity
        Button photoContinueBtn = findViewById(R.id.photo_continue_btn);
        photoContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!mFirstPhotoCaptured|| !mSecondPhotoCaptured|| !mThirdPhotoCaptured) {
//                    Toast.makeText(PhotoActivity.this, R.string.photos_warning, Toast.LENGTH_LONG).show();
//                }
//                else if (!plateCheckBox.isChecked() || !contextCheckBox.isChecked()) {
//                    Toast.makeText(PhotoActivity.this, R.string.checkboxes_warning, Toast.LENGTH_LONG).show();
//                }
//                else{
                    //Open MapActivity screen (Locate the violating Car)
//                    Intent intent = new Intent(PhotoActivity.this, LocationActivity.class);
//                    startActivity(intent);
                CheckLoactionPermission();

//                }
            }
        });
    }

    /**
     * Create a File to store the taken image in app private folder
     * @return imageFile
     * @throws IOException
     */

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    /**
     *Take a picture and provide file for saving it
     * @param requestCode
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
     * Display image in its appropriate ImageView
     * Add the path to intent bundle
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE_1 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mFirstPhotoCaptured = true;
            //Display image in its appropriate ImageView
            displayImages(mFirstCameraButton, mCurrentPhotoPath);
            mFilePath1 = mCurrentPhotoPath;
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_2 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mSecondPhotoCaptured = true;
            //Display image in its appropriate ImageView
            displayImages(mSecondCameraButton, mCurrentPhotoPath);
            mFilePath2 = mCurrentPhotoPath;
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_3 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mThirdPhotoCaptured = true;
            //Display image in its appropriate ImageView
            displayImages(mThirdCameraButton, mCurrentPhotoPath);
            mFilePath3 = mCurrentPhotoPath;
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
        //Get the violation type from the incoming Intent and set it to the TextView
        TextView violationTxt = inflater.findViewById(R.id.message_txt);
        violationTxt.setText(mViolationType);
        //Close tip dialog
        ImageView closeImg = inflater.findViewById(R.id.close_img);
        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.dismiss();
            }
        });
        //Set transparent background to the window
        tipDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Show the tip dialog
        tipDialog.show();
    }

    private void CheckLoactionPermission(){
        if(ContextCompat.checkSelfPermission(PhotoActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(PhotoActivity.this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PhotoActivity.this, LocationActivity.class));
            finish();
            return;
        }else {
            // Dexter is an android library that simplifies the process of requesting permissions at runtime.
            Dexter.withActivity(PhotoActivity.this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            //Toast.makeText(PhotoActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhotoActivity.this, LocationActivity.class));
                            finish();
                        }
                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if(response.isPermanentlyDenied()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this);
                                builder.setTitle("Permission Denied")
                                        .setMessage("Permission to access device location is permanently denied. you need to go to Settings to allow the permission.")
                                        .setNegativeButton("Cancel", null)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent();
                                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                            }
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
