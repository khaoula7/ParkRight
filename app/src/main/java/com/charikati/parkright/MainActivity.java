package com.charikati.parkright;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final String TAG = "MainActivity";
    private DrawerLayout drawer;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize FireBase Auth
        mFireBaseAuth = FirebaseAuth.getInstance();

        //Use drawer_toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // A reference to the NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        //To listen to click events on navigation drawer items, Implement NavigationView interface
        navigationView.setNavigationItemSelectedListener(this);
        // A reference to the DrawerLayout
        drawer = findViewById(R.id.drawer);
        //Button to show/hide navigation drawer placed in toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Change the navigation drawer icon
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        //Display user name amd email
        View headerView =  navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.name_txt);
        TextView email = headerView.findViewById(R.id.email_txt);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mFireBaseAuth.getCurrentUser() != null) {
                    //Set user name and email and change their visibility
                    userName.setText(mFireBaseAuth.getCurrentUser().getDisplayName());
                    userName.setVisibility(View.VISIBLE);
                    email.setText(mFireBaseAuth.getCurrentUser().getEmail());
                    email.setVisibility(View.VISIBLE);

                }else {
                    //Hide username and email
                    userName.setVisibility(View.GONE);
                    email.setVisibility(View.GONE);
                    // for getting menu from navigationView
                    Menu menu = navigationView.getMenu();
                    // finding menuItem that you want to change
                    MenuItem nav_logout= menu.findItem(R.id.nav_logout);
                    // Hide logout menu item
                    nav_logout.setVisible(false);
                }
            }
        };

        //If we open the app for the first time or we leave it by back button then come back HomeFragment will be displayed.
        //Otherwise, in case of rotating device or other configuration changes,selected fragment will be automatically saved and retrieved.
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    /**
     * Implement NavigationView method to specify the behaviour of each item in Navigation drawer
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_new:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TypeFragment()).commit();
                //startActivity( new Intent(MainActivity.this, TypeActivity.class));
                break;
            case R.id.nav_account:
                if(mFireBaseAuth.getCurrentUser() != null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MyAccountFragment()).commit();
                }else {
                    Toast.makeText(this, "You are not logged In", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                break;

            case R.id.nav_reports:
                if(mFireBaseAuth.getCurrentUser() != null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MyReportsFragment()).commit();
                }else {
                    Toast.makeText(this, "You are not logged In", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                break;

            case R.id.nav_status:
                if(mFireBaseAuth.getCurrentUser() != null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MyStatusFragment()).commit();
                }else {
                    Toast.makeText(this, "Login to your account first!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                break;
            case R.id.nav_logout:
                if(mFireBaseAuth.getCurrentUser() != null) {
                    mFireBaseAuth.signOut();
                    // Google sign out
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
                    googleSignInClient.signOut().addOnCompleteListener(this,
                            task -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

                    LoginManager.getInstance().logOut();
                    //Toast.makeText(this, "Logout "+ mFireBaseAuth, Toast.LENGTH_LONG).show();
                } else
                     Toast.makeText(this, "Already Logged out " + mFireBaseAuth, Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_contact:
                Toast.makeText(this, "Contact US", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_terms:
                Toast.makeText(this, "Terms and Conditions", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_privacy:
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
                break;
        }
        //Close drawer once item is selected
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Override back button behaviour to close the Drawer navigation before closing the activity
     */
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                Toast.makeText(this, "Share", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFireBaseAuth.addAuthStateListener(mAuthListener);
    }
}

