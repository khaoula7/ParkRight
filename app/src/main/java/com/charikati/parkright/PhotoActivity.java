package com.charikati.parkright;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
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

    private ImageButton firstCameraButton;
    private ImageButton secondCameraButton;
    private ImageButton thirdCameraButton;

    private boolean firstPhotoCaptured = false;
    private boolean secondPhotoCaptured = false;
    private boolean thirdPhotoCaptured = false;

    private Bundle mExtras;
    private String violationType;

    private String currentPhotoPath;


    /* Firebase instance variables */
    private FirebaseAuth mAuth;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

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
        toolbarTitle.setText(R.string.step_2);

        mExtras = getIntent().getExtras();

        //Get the violation type from the incoming Intent
        violationType = mExtras.getString("VIOLATION_TYPE");


        //Click on show button will open tips dialog
        Button showBtn = (Button)findViewById(R.id.show_btn);
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTipsDialog();
            }
        });

        //Click on first camera button will send a camera intent
        firstCameraButton = (ImageButton) findViewById(R.id.camera_1_btn);
        firstCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_1);
            }
        });

        //Click on second camera button will send a camera intent
       secondCameraButton = (ImageButton) findViewById(R.id.camera_2_btn);
        secondCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_2);
            }
        });

        //Click on third camera button will send a camera intent
        thirdCameraButton = (ImageButton) findViewById(R.id.camera_3_btn);
        thirdCameraButton.setOnClickListener(new View.OnClickListener() {
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
     * Sends a camera intent to take a picture of violating car
     */
    /*private void invokeCamera(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, requestCode);
        }
    }*/

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
     * Gets the result of the camera Intent: a thumbnail and displays it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Get Bitmap image from photo intent
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");

        if (requestCode == REQUEST_IMAGE_CAPTURE_1 && resultCode == RESULT_OK) {
            //User has captured the first photo
            firstPhotoCaptured = true;
            //Display photo thumbnail
            displayThumbnail(imageBitmap, firstCameraButton);
            //Save photo on disk in private mode
            savePhoto("photo1.jpg", imageBitmap);
            //Add photo filename to intent bundle
            mExtras.putString("FILE_NAME_1", "photo1.jpg");

            /*Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference photoRef = mPhotosStorageReference.child("photo1.jpg");
            UploadTask uploadTask = photoRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(PhotoActivity.this, "upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(PhotoActivity.this, "Upload Succeeded", Toast.LENGTH_SHORT).show();
                }
            });*/



        } else if (requestCode == REQUEST_IMAGE_CAPTURE_2 && resultCode == RESULT_OK) {
            //User has captured the second photo
            secondPhotoCaptured = true;
            //Display photo thumbnail
            displayThumbnail(imageBitmap, secondCameraButton);
            //Save photo on disk in private mode
            savePhoto("photo2.jpg", imageBitmap);
            //Add photo filename to intent bundle
            mExtras.putString("FILE_NAME_2", "photo2.jpg");

        } else if (requestCode == REQUEST_IMAGE_CAPTURE_3 && resultCode == RESULT_OK) {
            //User has captured the third photo
            thirdPhotoCaptured = true;
            //Display photo thumbnail
            displayThumbnail(imageBitmap, thirdCameraButton);
            //Save photo on disk in private mode
            savePhoto("photo3.jpg", imageBitmap);
            //Add photo filename to intent bundle
            mExtras.putString("FILE_NAME_3", "photo3.jpg");

        }

        //Test Send to Firebase Storage

    }

    /**
     * Display the thumbnail in its appropriate ImageButton.
     * @param imageBitmap the Bitmap
     * @param cameraButton the ImageButton to display the thumbnail
     */
    private void displayThumbnail(Bitmap imageBitmap, ImageButton cameraButton) {

        //Get the width and height in px of the ImageButton
        int targetWidth = cameraButton.getWidth();
        int targetHeight = cameraButton.getHeight();
        //Resize the thumbnail to the ImageButton dimensions
        Bitmap resizedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, targetWidth, targetHeight);
        //Set the resized thumbnail to the ImageButton
        Glide.with(this).load(resizedBitmap).into(cameraButton);
        //cameraButton.setImageBitmap(resizedBitmap);
    }

    private void savePhoto(String filename, Bitmap bitmap){
        try {
            //Write file
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            //Cleanup
            stream.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        violationTxt.setText(violationType);
        //Close tip dialog
        TextView closeTxt = inflater.findViewById(R.id.close_txt);
        closeTxt.setOnClickListener(new View.OnClickListener() {
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }


}
