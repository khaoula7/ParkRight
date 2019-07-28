package com.charikati.parkright;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

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
                invokeCamera(REQUEST_IMAGE_CAPTURE_1);
            }
        });

        //Click on second camera button will send a camera intent
       secondCameraButton = (ImageButton) findViewById(R.id.camera_2_btn);
        secondCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeCamera(REQUEST_IMAGE_CAPTURE_2);
            }
        });

        //Click on third camera button will send a camera intent
        thirdCameraButton = (ImageButton) findViewById(R.id.camera_3_btn);
        thirdCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeCamera(REQUEST_IMAGE_CAPTURE_3);
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
                //Open MapActivity screen (Locate the violating Car)
                Intent intent = new Intent(PhotoActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });


    }
    /**
     * Sends a camera intent to take a picture of violating car
     */
    private void invokeCamera(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, requestCode);
        }
    }
    /**
     * Gets the result of the camera Intent: a thumbnail and displays it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_1 && resultCode == RESULT_OK) {
            displayThumbnail(data, firstCameraButton);
            //User has captured the first photo
            firstPhotoCaptured = true;
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE_2 && resultCode == RESULT_OK) {
            displayThumbnail(data, secondCameraButton);
            //User has captured the second photo
            secondPhotoCaptured = true;
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE_3 && resultCode == RESULT_OK){
            displayThumbnail(data, thirdCameraButton);
            //User has captured the third photo
            thirdPhotoCaptured=true;
        }
    }

    /**
     * Display the thumbnail in its appropriate ImageButton.
     * @param data the intent
     * @param cameraButton the ImageButton to display the thumbnail
     */
    private void displayThumbnail(Intent data, ImageButton cameraButton) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        //Get the width and height in px of the ImageButton
        int targetWidth = cameraButton.getWidth();
        int targetHeight = cameraButton.getHeight();
        //Resize the thumbnail to the ImageButton dimensions
        Bitmap resizedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, targetWidth, targetHeight);
        //Set the resized thumbnail to the ImageButton
        cameraButton.setImageBitmap(resizedBitmap);
    }
    /**
     * Builds the tips_dialog to show tips on how to take photos
     */
    private void showTipsDialog() {
        //Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Inflate the layout
        View inflator = getLayoutInflater().inflate(R.layout.tips_dialog, null);
        //set the layout for the AlertDialog
        builder.setView(inflator);
        //Create the Tip Dialog
        final AlertDialog tipDialog = builder.create();
        //Get the violation type from the incoming Intent and set it to the TextView
        TextView violationTxt = inflator.findViewById(R.id.violation_txt);
        violationTxt.setText(getIntent().getStringExtra("activity_type"));
        //Close tip dialog
        TextView closeTxt = inflator.findViewById(R.id.close_txt);
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
     * @param v View
     */
    public void goBack(View v){
        Intent intent = new Intent(PhotoActivity.this, TypeActivity.class);
        startActivity(intent);
    }

}
