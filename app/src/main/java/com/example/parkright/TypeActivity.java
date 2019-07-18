package com.example.parkright;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class TypeActivity extends AppCompatActivity {
    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<Violation> mViolationData;
    private ViolationAdapter mAdapter;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violation_type);
        //Initialize the RecyclerView.
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize the ArrayList that will contain the data.
        mViolationData = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new ViolationAdapter(this, mViolationData);
        mRecyclerView.setAdapter(mAdapter);

        // Get the data.
        initializeData();
    }

    /**
     * Initialize the parking violation data from resources.
     */
    private void initializeData() {
        // Get the resources from the XML file.
        String[] violationList = getResources()
                .getStringArray(R.array.violation_types);
        TypedArray violationImageResources =
                getResources().obtainTypedArray(R.array.violation_images);
        // Clear the existing data (to avoid duplication).
        mViolationData.clear();
        // Create the ArrayList of violation objects with types and images about each violation.
        for(int i=0;i<violationList.length;i++){
            mViolationData.add(new Violation(violationList[i], violationImageResources.getResourceId(i,0)));
        }
        //Clean up the data in the typed array once you have created the Violation data ArrayList
        violationImageResources.recycle();
        // Notify the adapter of the change.
        mAdapter.notifyDataSetChanged();

    }

    /**
     * Go back to previous screen
     * @param v
     */
    public void goBack(View v){
        Intent intent = new Intent(TypeActivity.this, HowActivity.class);
        startActivity(intent);
    }









}
