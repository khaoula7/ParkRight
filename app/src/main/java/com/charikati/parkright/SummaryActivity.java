package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SummaryActivity extends AppCompatActivity {
    private Button mLogOutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /*mLogOutButton = findViewById(R.id.logout_btn);
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logout from Firebase
                mAuth.signOut();
                //Logout from Facebook
                LoginManager.getInstance().logOut();
                updateUI();
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //if(account != null) {
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            updateUI();
        }


    }

    private void updateUI() {
        //Toast.makeText(SummaryActivity.this, "You are Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SummaryActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}
