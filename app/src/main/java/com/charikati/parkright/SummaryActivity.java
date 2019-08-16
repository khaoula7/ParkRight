package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity {
    private Button mLogOutButton;
    private FirebaseAuth mAuth;
    private Spinner mSpinner;
    private ArrayList mViolationData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mSpinner = findViewById(R.id.spinner);
        String[] violations = getResources().getStringArray(R.array.violation_types);
        ArrayAdapter<String> violationAdapter=new ArrayAdapter<String>(this,R.layout.spinner_item, violations);
        mSpinner.setAdapter(violationAdapter);





    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //if(account != null) {
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //if(currentUser == null){
          //  updateUI();
        //}


    }

    private void updateUI() {
        //Toast.makeText(SummaryActivity.this, "You are Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SummaryActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
