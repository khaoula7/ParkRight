package com.charikati.parkright;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    //Variables for Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private SignInButton mGoogleBtn;
    //Variables for Facebook Login
    private CallbackManager mCallbackManager;
    private Button mFacebookBtn;
    //Variables for Email/password Sign In
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private Button mLoginButton;
    private TextView mRegister;
    /*Firebase realtime database*/
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private Bundle mExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mExtras = getIntent().getExtras();


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
        toolbarTitle.setText(R.string.step_4);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //Initialize firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");


        /************************* Google Sign In******************************************************************************/
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleBtn = findViewById(R.id.google_button);
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();

            }
        });

        /************************* Facebook Login******************************************************************************/
        // Initialize Facebook Login button
        //CallbackManager will create a bridge between facebook and your app to login with facebook
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookBtn = findViewById(R.id.facebook_btn);
        mFacebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mFacebookBtn.setEnabled(false);
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                    }
                });
            }
        });

        /************************* Email/Password Login******************************************************************************/
        mEmailField = findViewById(R.id.email_edittext);
        mPasswordField = findViewById(R.id.password_edit);
        mLoginButton = findViewById(R.id.login_btn);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailPasswordSignIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });

        //Click on Forget Password will open an AlertDialog
        findViewById(R.id.forgot_pswd_txt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgot_password_dialog();

            }
        });
        //Go to RegisterActivity to create an Email/Password account
        mRegister = findViewById(R.id.register_txt);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtras(mExtras);
                startActivity(intent);
            }
        });

    }

    /**
     * Shows an AlertDialog to reset forgotten password
     */
    private void forgot_password_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View inflater = getLayoutInflater().inflate(R.layout.forgot_password_dialog, null);
        //set the layout for the AlertDialog
        builder.setView(inflater);
        final AlertDialog passwordDialog = builder.create();
        //Set transparent background to the window
        passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Show the tip dialog
        passwordDialog.show();
        TextInputEditText emailField = inflater.findViewById(R.id.email_edittext);
        TextView resetButton = inflater.findViewById(R.id.reset_txt_btn);
        TextView cancelButton = inflater.findViewById(R.id.cancel_txt_btn);

        //Reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Resetting Password");
                // User clicked reset, so send reset password email
                String email = emailField.getText().toString();
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            hideProgressDialog();
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(LoginActivity.this, R.string.password_sent, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, (CharSequence) task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        //Cancel Button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close dialog
                passwordDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
        else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    /*******************************Google Sign In methods************************************************************************/

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        showProgressDialog("Loading");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //checkUidExists(account);
                            //Go to SummaryActivity
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * Adds a new user to Firebase realtime database
     */
    private void writeUserInDatabase(String first_name, String last_name, String email){
        User user = new User(first_name, last_name, email);
        //Connect to realtime database and write in Users segment
        mUsersDatabaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Writing in database Succeeded");
                }else{
                    Log.w(TAG, "Writing in database failed");
                }
            }
        });
    }

    /*******************************Facebook Sign In methods************************************************************************/
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showProgressDialog("Loading");

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            mFacebookBtn.setEnabled(true);
                            //useLoginInformation(token);
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mFacebookBtn.setEnabled(true);
                        }
                        hideProgressDialog();
                    }
                });
    }


    /*******************************Email/Password Sign In methods************************************************************************/
    private void emailPasswordSignIn(String email, String password) {
        Log.d(TAG, "EmailPassword signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog("Loading");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if((user != null) && (user.isEmailVerified())) {
                                updateUI();
                            }else {
                                Toast.makeText(LoginActivity.this,
                                        "Your Email is not verified yet. \n Please verify it then proceed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * checks if a form is valid or not before sending user account information
     * @return valid
     */
    private boolean validateForm() {
        boolean validForm = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            validForm = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            validForm = false;
        } else {
            mPasswordField.setError(null);
        }
        return validForm;
    }

    /*******************************Common methods*************************************************/

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            updateUI();
        }
    }

    private void updateUI() {
        //Toast.makeText(LoginActivity.this, "You are Logged In", Toast.LENGTH_SHORT).show();
        Intent summaryIntent = new Intent(LoginActivity.this, SummaryActivity.class);
        //attach the bundle to the Intent object
        summaryIntent.putExtras(mExtras);
        Log.d(TAG, String.valueOf(mExtras.getDouble("LATITUDE")));
        startActivity(summaryIntent);
    }




    /**
     * Go back to previous screen: activity_map
     */
    public void goBack(){
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
        //intent.putExtras(mExtras);
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

    private void useLoginInformation(AccessToken accessToken) {
        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    Log.d(TAG, "name = "+first_name + " lastname = "+ last_name + " email = "+ email);
                    writeUserInDatabase(first_name, last_name, email);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name, last_name, email");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    /**
     * Only first time users will be written in database
     * @param account
     */
    private void checkUidExists(GoogleSignInAccount account){
        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, " Uid=" + Uid );
        mUsersDatabaseReference.orderByKey().equalTo(Uid).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Log.d(TAG, Uid + " does not exist");
                    writeUserInDatabase(account.getGivenName(), account.getFamilyName(), account.getEmail());
                }else{
                    Log.d(TAG, Uid + " exists");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
