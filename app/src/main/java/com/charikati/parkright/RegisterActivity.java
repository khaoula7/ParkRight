package com.charikati.parkright;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
        Toolbar toolbar = findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.activity_toolbar_title);
        toolbarTitle.setText(R.string.sign_up);
        // Display Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Views
        mNameField = findViewById(R.id.name_edit_text);
        mLastNameField = findViewById(R.id.last_edit_text);
        mEmailField = findViewById(R.id.email_edit_text);
        mPasswordField = findViewById(R.id.password_edit_text);
        mTermsCheck = findViewById(R.id.terms_check_box);
        // Access a Cloud FireStore instance
        mFireDb = FirebaseFirestore.getInstance();

        Button mRegisterBtn = findViewById(R.id.sign_button);
        //Handle click on register button
        mRegisterBtn.setOnClickListener(v -> {
            first_name = Objects.requireNonNull(mNameField.getText()).toString();
            last_name = Objects.requireNonNull(mLastNameField.getText()).toString();
            email = Objects.requireNonNull(mEmailField.getText()).toString();
            password = Objects.requireNonNull(mPasswordField.getText()).toString();
            createAccount();
        });
        //Go to LoginActivity
        findViewById(R.id.login_txt).setOnClickListener(v -> goToLogin());
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
        } else
            mNameField.setError(null);
        //last name
        if (TextUtils.isEmpty(last_name)) {
            mLastNameField.setError("Required.");
            validForm = false;
        } else
            mLastNameField.setError(null);
        //email
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            validForm = false;
        } else
            mEmailField.setError(null);
        //password
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            validForm = false;
        } else
            mPasswordField.setError(null);
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
        if (!validateForm())
            return;
        showProgressDialog("Creating new user account");
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //In order to get the full name with mFirebaseAuth.getCurrentUser.getDisplayName(), we need to set it up when creating the new account with email and password
                        String displayName = first_name + " " + last_name;
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).updateProfile(profileUpdates);
                        //Save additional data about user in fireStore database
                        User user = new User(first_name, last_name, email);
                        mFireDb.collection("Users").add(user);
                        // Add a new document with a user ID as a custom id
                        mFireDb.collection("Users").document(mFirebaseAuth.getCurrentUser().getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User successfully written!");
                                    sendEmailVerification();
                                    goToLogin();
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error writing user", e));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "createUserWithEmail:failure", Toast.LENGTH_SHORT).show();
                    }
                    hideProgressDialog();
                });
    }

    /**
     * Sends an Email with a link to verify user's provided Email
     */
    private void sendEmailVerification() {
        // Send verification email
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        hideProgressDialog();
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Verification email sent to " + user.getEmail());

                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
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
