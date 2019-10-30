package com.charikati.parkright;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";
    private TextInputEditText mNameField;
    private TextInputEditText mLastNameField;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private CheckBox mTermsCheck;
    private Button mRegisterBtn;
    private String first_name;
    private String last_name;
    private String email;
    private String password;
    private TextView mLogin;
    // Firebase Instance variables
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();
        //Views
        mNameField = findViewById(R.id.name_edit_txt);
        mLastNameField = findViewById(R.id.last_name_edit_txt);
        mEmailField = findViewById(R.id.email_edit_text);
        mPasswordField = findViewById(R.id.password_edit_text);
        mTermsCheck = findViewById(R.id.terms_chk_box);
        mRegisterBtn = findViewById(R.id.send_btn);
        //Handle click on register button
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_name = mNameField.getText().toString();
                last_name = mLastNameField.getText().toString();
                email = mEmailField.getText().toString();
                password = mPasswordField.getText().toString();
                createAccount();
            }
        });
        //Skip to LoginActivity
        mLogin = findViewById(R.id.login_txt);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
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
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog("Creating new user account");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(RegisterActivity.this, "createUserWithEmail:success\"", Toast.LENGTH_LONG).show();
                            //Save additional data about user in realtime database
                            User user = new User(first_name, last_name, email);
                            //Connect to realtime database and write in it
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.w(TAG, "Writing in database Succeeded");
                                                //Send Email Verification
                                                sendEmailVerification();
                                                //Go back to LoginActivity
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);

                                            }else{
                                                Log.w(TAG, "Writing in database failed");

                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "createUserWithEmail:failure", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /*private void show_verif_email_dialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        View inflator = getLayoutInflater().inflate(R.layout.email_verif_dialog, null);
        //set the layout for the AlertDialog
        builder.setView(inflator);
        final AlertDialog emailDialog = builder.create();
        //Set transparent background to the window

        TextView message = inflator.findViewById(R.id.message_txt);
        //message.setText(getString(R.string.verif_phrase_1)+ email + getString(R.string.verif_phrase_2));
        Button verifEmailButton = inflator.findViewById(R.id.verif_email_button);
        verifEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updateUI();
            }
        });
        emailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Show the tip dialog
        emailDialog.show();
    }*/

    /**
     * Sends an Email with a link to verify user's provided Email
     */
    private void sendEmailVerification() {
        // Send verification email
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to: " + user.getEmail()+ " \n Please verify your email before proceeding",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Go back to previous screen: activity_map
     */
    public void goBack(){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Up button: Go back to previous screen: activity_type
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home ) {
            goBack();
        }
        return true;
    }
}
