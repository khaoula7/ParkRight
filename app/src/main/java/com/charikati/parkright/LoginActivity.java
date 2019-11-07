package com.charikati.parkright;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.charikati.parkright.model.User;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    // Firebase Instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore mFireDb;
    //Variables for Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1;
    private SignInButton mGoogleBtn;
    //Variables for Facebook Login
    private CallbackManager mCallbackManager;
    private Button mFacebookBtn;
    //Variables for Email/password Sign In
    private TextInputEditText mEmailField;
    private TextInputEditText mPasswordField;
    private Button mLoginButton;
    private TextView mRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        // Initialize Firebase Components
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFireDb = FirebaseFirestore.getInstance();
        //AuthStateListener reacts to auth state changes (sign in, sign out)
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Check if user is logged in or not and act accordingly
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null && currentUser.isEmailVerified()){
                    Log.d(TAG, "email verified:  "+currentUser.isEmailVerified());
                    //User is signed in
                    updateUI();
                }else {
                    //User is signed out, start sign in flow
                    googleSignIn();
                    facebookLogIn();
                    emailSignIn();
                }
            }
        };
    }

    /**
     * Launch google sign in process
     */
    private void googleSignIn(){
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleBtn = findViewById(R.id.google_button);
        mGoogleBtn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }
    /**
     * Launch facebook login process
     */
    private void facebookLogIn() {
        //CallbackManager will create a bridge between facebook and your app to login with facebook
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookBtn = findViewById(R.id.facebook_button);
        mFacebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance()
                        .logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
        else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    /**
     * Authenticate google user with Firebase
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showProgressDialog("Signing In");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            checkUidExists(account);
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
     * Authenticate facebook user with Firebase
     */
    private void handleFacebookAccessToken(AccessToken token) {
        showProgressDialog("Loading");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            mFacebookBtn.setEnabled(true);
                            useLoginInformation(token);
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mFacebookBtn.setEnabled(true);
                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * Launch email login process
     */
    private void emailSignIn(){
        mEmailField = findViewById(R.id.email_edittext);
        mPasswordField = findViewById(R.id.password_edit);
        mLoginButton = findViewById(R.id.login_button);
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
                startActivity(intent);
            }
        });
    }

    /**
     * Authenticate Email/Password user with Firebase
     */
    private void emailPasswordSignIn(String email, String password) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog("Signing In");
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            if((user != null) && (user.isEmailVerified())) {
                                updateUI();
                            }else {
                                Toast.makeText(LoginActivity.this,
                                        "Your Email is not verified yet. Check your " + user.getEmail() + " before proceeding",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
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

    /**
     * App is started and visible, Attach AuthStateListener
     */
    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    /**
     * App is no longer visible, Detach AuthStateListener
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /**
     * If user is signed in, go to summary activity
     */
    private void updateUI() {
        Intent summaryIntent = new Intent(LoginActivity.this, SummaryActivity.class);
        startActivity(summaryIntent);
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
                mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                passwordDialog.dismiss();
            }
        });
    }

    /**
     * Only first time users will be written in database( Google Sign In and Facebook Login)
     */
    private void checkUidExists(GoogleSignInAccount account){
        String Uid = mFirebaseAuth.getCurrentUser().getUid();
        DocumentReference docRef = mFireDb.collection("Users").document(Uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Log.d(TAG, "No such document");
                        writeUserInDatabase(account.getGivenName(), account.getFamilyName(), account.getEmail());
                    } else {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Adds a new user to FireStore database
     */
    private void writeUserInDatabase(String first_name, String last_name, String email){
        User user = new User(first_name, last_name, email);
        //Connect to Firestore and write in Users collection
        mFireDb.collection("Users").document(mFirebaseAuth.getCurrentUser().getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Writing in database Succeeded");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error writing user", e);
                    }
                });
    }

    /**
     * Creating the GraphRequest to fetch user details
     */
    private void useLoginInformation(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
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



}
