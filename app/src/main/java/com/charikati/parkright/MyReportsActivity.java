package com.charikati.parkright;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyReportsActivity extends AppCompatActivity {
    private static final String TAG = "MyReportsActivity";
    private ListView mViolationList;
    private ReportAdapter mAdapter;

    /*Firebase instance variables*/
    //Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mViolationDatabaseReference;
    private ChildEventListener mUserEventListener;
    private ChildEventListener mViolationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        // Create a list of violations
        final ArrayList<ViolationReport> myViolations = new ArrayList<>();

        // Create an {@link ReportAdapter}, whose data source is a list of {@link ViolationAdapter}s. The
        // adapter knows how to create list items for each item in the list.
        mAdapter = new ReportAdapter(this, R.layout.report_item, myViolations );

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // word_list.xml layout file.
        mViolationList = findViewById(R.id.list_view);

        // Make the {@link ListView} use the {@link ReportAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link ViolationReport} in the list.
        mViolationList.setAdapter(mAdapter);

        //Entry point for app to access the database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //Get authenticated user id
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //References sent_violations node of the authenticated user
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("Users").child(uid).child("sent_violations");
        mViolationDatabaseReference = mFirebaseDatabase.getReference().child("Violations");
        attachUserDatabaseReadListener();

        //Click on report item will open violation_details screen
        mViolationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyReportsActivity.this, ViolationDetails.class);
                startActivity(intent);
            }
        });
    }

    private void attachUserDatabaseReadListener() {
        if(mUserEventListener == null) {
            /*
             *Read from database
             * We need to attach a ChildEventListener object to our database reference
             * This will allow us to listen and have our code triggered whenever changes
             * occur on the messages node
             */
            mUserEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    /**
                     * 1- Called whenever a new violation key is inserted in the database
                     * 2- Called for every violation key that already exists in the database.
                     * dataSnapshot contains data from the database at the specified location
                     * at the exact time the listener is triggered(adding a new message)
                     * --> We can use it to get the data from the new message
                     */
                    //Deserialize the message from database into a FriendlyMessage object (having the same fields as in database
                    String violationId = dataSnapshot.getKey();
                    mViolationDatabaseReference.orderByKey().equalTo(violationId).limitToFirst(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                //Deserialize the violation from database into a ViolationReport object
                                ViolationReport violationReport = ds.getValue(ViolationReport.class);
                                //Add ViolationReport object into adapter
                                mAdapter.add(violationReport);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //attachViolationDatabaseReadListener(violationId);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            /**
             * mMessagesDatabaseReference defines what Im listening to
             * mChildEventListener object defines what exactly will happen to the data
             */
            mUserDatabaseReference.addChildEventListener(mUserEventListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Attach AuthStateListener object to FirebaseAuth object

    }





    private void detachDatabaseReadListener() {
        if(mUserEventListener != null) {
            mUserDatabaseReference.removeEventListener(mUserEventListener);
            mUserEventListener = null;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //If the activity is destroyed due to a reason other than logout
        //(screen rotation or other configuration change), detach databaseReadListener and clean adapter
        //detachDatabaseReadListener();
       // mAdapter.clear();
    }

}
