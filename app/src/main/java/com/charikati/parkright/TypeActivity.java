package com.charikati.parkright;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.widget.TextView;

import com.charikati.parkright.adapter.ViolationAdapter;
import com.charikati.parkright.model.ViolationPreview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class TypeActivity extends AppCompatActivity {
    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<ViolationPreview> mViolationData;
    private ViolationAdapter mAdapter;
    //Shared Preferences variables
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.charikati.parkright";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.step_1);
        //Open sharedPrefs file at the given filename (sharedPrefFile) with the mode MODE_PRIVATE.
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        //Delete all sharedPreferences
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.apply();

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
            mViolationData.add(new ViolationPreview(violationList[i], violationImageResources.getResourceId(i,0)));
        }
        //Clean up the data in the typed array once you have created the Violation data ArrayList
        violationImageResources.recycle();
        // Notify the adapter of the change.
        mAdapter.notifyDataSetChanged();
    }
}
