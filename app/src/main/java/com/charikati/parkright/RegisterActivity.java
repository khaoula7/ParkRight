package com.charikati.parkright;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputEditText mNameField;
    private TextInputEditText mLastNameField;
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private CheckBox mTermsCheck;
    private Button mRegisterBtn;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private Bundle mExtras;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Views
        mNameField = findViewById(R.id.name_edit_txt);
        mLastNameField = findViewById(R.id.last_name_edit_txt);
        mEmailField = findViewById(R.id.email_edit_text);
        mPasswordField = findViewById(R.id.password_edit_text);
        mTermsCheck = findViewById(R.id.terms_chk_box);
        mRegisterBtn = findViewById(R.id.send_btn);

        mExtras = getIntent().getExtras();


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mNameField.getText().toString();
                lastName = mLastNameField.getText().toString();
                email = mEmailField.getText().toString();
                password = mPasswordField.getText().toString();
                createAccount();
            }
        });

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI();
    }
    // [END on_start_check_user]

    private void updateUI() {
        //hideProgressDialog();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent summaryIntent = new Intent(RegisterActivity.this, SummaryActivity.class);
            startActivity(summaryIntent);
            finish();
        }
        else{
            Toast.makeText(this, "In updateUI", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * checks if a form is valid or not before sending user account information
     * @return valid
     */
    private boolean validateForm() {
        boolean validForm = true;

        if (TextUtils.isEmpty(name)) {
            mNameField.setError("Required.");
            validForm = false;
        } else {
            mNameField.setError(null);
        }

        if (TextUtils.isEmpty(lastName)) {
            mLastNameField.setError("Required.");
            validForm = false;
        } else {
            mLastNameField.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            validForm = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            validForm = false;
        } else {
            mPasswordField.setError(null);
        }

        if(!mTermsCheck.isChecked()) {
            mTermsCheck.setError("Required.");
            validForm = false;
        }
        return validForm;
    }


    private void createAccount() {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(RegisterActivity.this, "createUserWithEmail:success\"", Toast.LENGTH_LONG).show();

                            //Save additional data about user in realtime database
                            User user = new User(name, lastName, email);
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
                                                intent.putExtras(mExtras);
                                                startActivity(intent);

                                            }else{
                                                Log.w(TAG, "Writing in database failed");

                                            }
                                        }
                                    });
                            //FirebaseUser currentUser = mAuth.getCurrentUser();
                            //updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "createUserWithEmail:failure", Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
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
     * Go to Login screen: LoginActivity
     * @param v View
     */
    public void login(View v){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtras(mExtras);
        startActivity(intent);
    }
}
