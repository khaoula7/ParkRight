package com.charikati.parkright;

import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.FileProvider;

import android.os.Bundle;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
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

    private Bundle mExtras;
    private String mViolationType;

    private String mCurrentPhotoPath;


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
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.step_2);

        mExtras = getIntent().getExtras();

        //Get the violation type from the incoming Intent
        mViolationType = mExtras.getString("VIOLATION_TYPE");


        //Click on show button will open tips dialog
        Button showBtn = (Button)findViewById(R.id.show_btn);
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTipsDialog();
            }
        });

        //Click on first camera button will send a camera intent
        mFirstCameraButton = (ImageButton) findViewById(R.id.camera_1_btn);
        mFirstCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_1);
            }
        });

        //Click on second camera button will send a camera intent
       mSecondCameraButton = (ImageButton) findViewById(R.id.camera_2_btn);
       mSecondCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_2);
            }
       });

        //Click on third camera button will send a camera intent
        mThirdCameraButton = (ImageButton) findViewById(R.id.camera_3_btn);
        mThirdCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_3);
            }
        });

        // Checkboxes
        final CheckBox plateCheckBox = (CheckBox) findViewById(R.id.plate_chk_box);
        final CheckBox contextCheckBox = (CheckBox) findViewById(R.id.context_check_box);

        //Click on map_continue_btn button will open MapActivity
        Button photoContinueBtn = findViewById(R.id.photo_continue_btn);
        photoContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(!firstPhotoCaptured || !secondPhotoCaptured || !thirdPhotoCaptured) {
                    Toast.makeText(PhotoActivity.this, R.string.photos_warning, Toast.LENGTH_LONG).show();
                }
                else if (!plateCheckBox.isChecked() || !contextCheckBox.isChecked()) {
                    Toast.makeText(PhotoActivity.this, R.string.checkboxes_warning, Toast.LENGTH_LONG).show();
                }
                else{
                    //Open MapActivity screen (Locate the violating Car)
                    Intent intent = new Intent(PhotoActivity.this, MapActivity.class);
                    startActivity(intent);
                }*/
                //Open MapActivity screen (to locate the violating Car)
                Intent intent = new Intent(PhotoActivity.this, MapsActivity.class);
                //attach the bundle to the Intent object
                intent.putExtras(mExtras);
                //finally start the activity
                startActivity(intent);
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
            //setPic(mFirstCameraButton);
            //Add photo path to intent bundle
            mExtras.putString("FILE_NAME_1", mCurrentPhotoPath);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE_2 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mSecondPhotoCaptured = true;
            //Display image in its appropriate ImageView
            displayImages(mSecondCameraButton, mCurrentPhotoPath);
            //Add photo path to intent bundle
            mExtras.putString("FILE_NAME_2", mCurrentPhotoPath);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE_3 && resultCode == RESULT_OK) {
            //User has captured the first photo
            mThirdPhotoCaptured = true;
            //Display image in its appropriate ImageView
            displayImages(mThirdCameraButton, mCurrentPhotoPath);
            //Add photo path to intent bundle
            mExtras.putString("FILE_NAME_3", mCurrentPhotoPath);

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

    /**
     * Go back to previous screen: activity_type
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home ) {
            finish();
        }
        return true;
    }









}
