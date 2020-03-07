package com.charikati.parkright;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private DrawerLayout drawer;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Use toolbar as the ActionBar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = findViewById(R.id.toolbar_title);
        //Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
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
        //If we open the app for the first time or we leave it by back button then come back HomeFragment will be displayed.
        //Otherwise, in case of rotating device or other configuration changes,selected fragment will be automatically saved and retrieved.
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    public void setActionBarTitle(String title){
        toolbarTitle.setText(R.string.step_1);
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
                startActivity( new Intent(MainActivity.this, TypeActivity.class));
                break;
            case R.id.nav_account:
                Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_reports:
                if(mFirebaseAuth != null){
                    startActivity(new Intent(MainActivity.this, MyReportsActivity.class));
                }else {
                    Toast.makeText(this, "You are not logged In", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
                break;
            case R.id.nav_status:
                Toast.makeText(this, "Status", Toast.LENGTH_SHORT).show();
                break;
//            case R.id.nav_login:
//                if(mFirebaseAuth == null){
//                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                }else {
//                    Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
//                }
//                break;
            case R.id.nav_logout:
                if(mFirebaseAuth != null) {
                    mFirebaseAuth.signOut();
                    Toast.makeText(this, "Logout "+ mFirebaseAuth, Toast.LENGTH_LONG).show();
                    mFirebaseAuth = null;
                } else {
                    Toast.makeText(this, "Already Logged out "+mFirebaseAuth, Toast.LENGTH_SHORT).show();
                }
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
}

