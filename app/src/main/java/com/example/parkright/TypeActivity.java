package com.example.parkright;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class TypeActivity extends AppCompatActivity {
    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<Violation> mViolationData;
    private ViolationAdapter mAdapter;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);
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
        /*Use ItemTouchHelper to define what happens to RecyclerView list items when the user performs
         * various touch actions, such as swipe, or drag and drop.
         * ItemTouchHelper.SimpleCallback lets you define which directions are supported for swiping
         * and moving list items, and implement the swiping and moving behavior
         * Here, only moving behaviour is implemented
         */
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                //Get the original and target index from the second and third argument passed in
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                //Swap the items in the dataset by calling Collections.swap()
                // and pass in the dataset, and the initial and final indexes
                Collections.swap(mViolationData, from, to);
                //Notify the adapter that the item was moved, passing in the old and new indexes
                mAdapter.notifyItemMoved(from, to);
                return true;

            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        });
        // Attach the helper object to the RecyclerView
        helper.attachToRecyclerView(mRecyclerView);
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
