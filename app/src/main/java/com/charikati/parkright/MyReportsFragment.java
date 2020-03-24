package com.charikati.parkright;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.charikati.parkright.adapter.ReportAdapterNew;
import com.charikati.parkright.model.ViolationReport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Objects;

public class MyReportsFragment extends Fragment {
    private FirebaseFirestore mFirestore;
    private ProgressBar mProgressBar;
    private ArrayList<ViolationReport> mReportsData;
    private ReportAdapterNew mAdapter;

    public MyReportsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_myreports, container, false);
        TextView heading = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_title);
        heading.setText(R.string.my_reports);
        //Initialize fireBase instances
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        //Find Progress bar
        mProgressBar = v.findViewById(R.id.progressBar);
        //Initialize the RecyclerView.
        RecyclerView mRecyclerView = v.findViewById(R.id.recyclerView);
        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize the ArrayList that will contain the data.
        mReportsData = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new ReportAdapterNew(getContext(), mReportsData);
        mRecyclerView.setAdapter(mAdapter);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = v.findViewById(R.id.empty_view);

        //Get all documents in subCollection sent_violations
        mFirestore.collection("Users").document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                .collection("sent_violations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(Objects.requireNonNull(task.getResult()).isEmpty()) {
                            mProgressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                           // Log.d(TAG, document.getId() + " => " + document.getData());
                            queryViolations(document.getId());
                        }
                    } else {
                        Log.d(getTag(), "Error getting documents: ", task.getException());
                    }
                });
        return v;
    }

    /**
     * Send a query to database with violation id to get all information about it
     */
    private void queryViolations(String id) {
        mFirestore.collection("Violations").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    //Deserialize the violation from database into a ViolationReport object
                    ViolationReport violationReport = documentSnapshot.toObject(ViolationReport.class);
                    //Add ViolationReport object into  Violation list
                    mReportsData.add(violationReport);
                    // Notify the adapter of the change.
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                });
    }
}
