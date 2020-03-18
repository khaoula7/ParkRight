package com.charikati.parkright;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.charikati.parkright.adapter.ViolationAdapter;
import com.charikati.parkright.model.ViolationPreview;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class TypeFragment extends Fragment {
    // Member variables.
    private ArrayList<ViolationPreview> mViolationData;
    private ViolationAdapter mAdapter;
    //Shared Preferences variable
    private String sharedPrefFile = "com.charikati.parkright";

    public TypeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_type, container, false);
        //Change toolbar title
        TextView heading = getActivity().findViewById(R.id.toolbar_title);
        heading.setText(R.string.type_label);
        //Open sharedPrefs file at the given filename (sharedPrefFile) with the mode MODE_PRIVATE.
        SharedPreferences mPreferences = getActivity().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        //Delete all sharedPreferences
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.apply();
        //Initialize the RecyclerView.
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        // Set the Layout Manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize the ArrayList that will contain the data.
        mViolationData = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new ViolationAdapter(getContext(), mViolationData);
        recyclerView.setAdapter(mAdapter);
        // Get the data.
        initializeData();
        /*Use ItemTouchHelper to define what happens to RecyclerView list items when the user performs
         * various touch actions, such as swipe, or drag and drop.*/
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                //Get the original and target index from the second and third argument passed in
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                //Swap the items in the dataSet by calling Collections.swap()
                // and pass in the dataSet, and the initial and final indexes
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
        helper.attachToRecyclerView(recyclerView);
        return v;
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
