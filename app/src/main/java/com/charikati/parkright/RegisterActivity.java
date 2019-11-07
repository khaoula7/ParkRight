package com.charikati.parkright;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.charikati.parkright.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputEditText mNameField;
    private TextInputEditText mLastNameField;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private CheckBox mTermsCheck;
    private String first_name;
    private String last_name;
    private String email;
    private String password;
    // Firebase Instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFireDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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
        toolbarTitle.setText(R.string.step_4_1);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Views
        mNameField = findViewById(R.id.name_edit_txt);
        mLastNameField = findViewById(R.id.last_name_edit_txt);
        mEmailField = findViewById(R.id.email_edit_text);
        mPasswordField = findViewById(R.id.password_edit_text);
        mTermsCheck = findViewById(R.id.terms_chk_box);
        // Access a Cloud Firestore instance
        mFireDb = FirebaseFirestore.getInstance();

        Button mRegisterBtn = findViewById(R.id.send_btn);
        //Handle click on register button
        mRegisterBtn.setOnClickListener(v -> {
            first_name = mNameField.getText().toString();
            last_name = mLastNameField.getText().toString();
            email = mEmailField.getText().toString();
            password = mPasswordField.getText().toString();
            createAccount();
        });
        //Skip to LoginActivity
        TextView mLogin = findViewById(R.id.login_txt);
        mLogin.setOnClickListener(v -> {
            goToLogin();
        });
    }

    /**
     * checks if the form data is valid before sending user account information
     */
    private boolean validateForm() {
        boolean validForm = true;
        //first name
        if (TextUtils.isEmpty(first_name)) {
            mNameField.setError("Required.");
            validForm = false;
        } else {
            mNameField.setError(null);
        }
        //last name
        if (TextUtils.isEmpty(last_name)) {
            mLastNameField.setError("Required.");
            validForm = false;
        } else {
            mLastNameField.setError(null);
        }
        //email
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            validForm = false;
        } else {
            mEmailField.setError(null);
        }
        //password
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            validForm = false;
        } else {
            mPasswordField.setError(null);
        }
        //Terms checkbox
        if(!mTermsCheck.isChecked()) {
            mTermsCheck.setError("Required.");
            validForm = false;
        }
        return validForm;
    }

    /**
     * Create a new account for user
     */
    private void createAccount() {
        if (!validateForm()) {
            return;
        }
        showProgressDialog("Creating new user account");
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Save additional data about user in realtime database
                            User user = new User(first_name, last_name, email);
                            mFireDb.collection("Users").add(user);
                            // Add a new document with a user ID as a custom id
                            mFireDb.collection("Users").document(mFirebaseAuth.getCurrentUser().getUid())
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "User successfully written!");
                                            sendEmailVerification();
                                            goToLogin();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error writing user", e);
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "createUserWithEmail:failure", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * Sends an Email with a link to verify user's provided Email
     */
    private void sendEmailVerification() {
        // Send verification email
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            hideProgressDialog();
//                            Toast.makeText(RegisterActivity.this,
//                                    "Verification email sent to " + user.getEmail(),
//                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Verification email sent to " + user.getEmail());

                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
//                            Toast.makeText(RegisterActivity.this,
//                                    "Failed to send verification email.",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Go back to Login Screen
     */
    public void goToLogin(){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}
