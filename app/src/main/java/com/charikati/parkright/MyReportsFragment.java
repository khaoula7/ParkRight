package com.charikati.parkright;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.charikati.parkright.adapter.ReportAdapter;
import com.charikati.parkright.model.ViolationReport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MyReportsFragment extends Fragment {
    private FirebaseFirestore mFirestore;
    private ProgressBar mProgressBar;
    private TextView emptyView;
    private ArrayList<ViolationReport> mReportsData;
    private ReportAdapter mAdapter;
    private FirebaseAuth mFirebaseAuth;
    private ListenerRegistration reportListener;

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
        mFirebaseAuth = FirebaseAuth.getInstance();
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
        mAdapter = new ReportAdapter(getContext(), mReportsData);
        mRecyclerView.setAdapter(mAdapter);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = v.findViewById(R.id.empty_view);

        //Get all documents once in subCollection sent_violations
        /*final CollectionReference collection = mFirestore.collection("Users").document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid()).collection("sent_violations");
        collection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
//                        Toast.makeText(getContext(), "results length: "+ task.getResult().size(), Toast.LENGTH_SHORT);
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
                });*/
        return v;
    }

    /**
     * Send a query to database with violation id to get all information about it
     */
    private void queryViolations(String id) {
        //final DocumentReference reportRef = mFirestore.collection("Violations").document(id);
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

    @Override
    public void onStart() {
        super.onStart();
        // Clear the existing data in reports list (to avoid duplication).
        mReportsData.clear();
        final CollectionReference collection = mFirestore.collection("Users").document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid()).collection("sent_violations");
        reportListener = collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(getTag(), "Listen failed.", e);
                    Toast.makeText(getContext(), "Listen Failed", Toast.LENGTH_LONG).show();
                    return;
                }
                assert value != null;
                if (value.isEmpty()) {
                    mProgressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }

                for (QueryDocumentSnapshot doc : value) {
                    queryViolations(doc.getId());
                }
            }
        });
        /*reportListener = collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(getTag(), "listen:error", e);
                    Toast.makeText(getContext(), "Listen Failed", Toast.LENGTH_LONG).show();
                    return;
                }
                assert snapshots != null;
                if (snapshots.isEmpty()) {
                    mProgressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    DocumentSnapshot documentSnapshot = dc.getDocument();
                    String id = documentSnapshot.getId();
                    switch (dc.getType()) {
                        case ADDED:
//                            Log.d(TAG, "New city: " + dc.getDocument().getData());
                            //Toast.makeText(getContext(), "ID: "+ dc.getDocument().getId(), Toast.LENGTH_LONG).show();
                            queryViolations(id);
//                            notification();
                            break;
                        case MODIFIED:
                            Log.d(getTag(), "Modified city: " + dc.getDocument().getData());
                            String status = (String) documentSnapshot.get("status");
                            Toast.makeText(getContext(), "Status: "+ status, Toast.LENGTH_LONG).show();
                            //notification(status);
                            break;
                        case REMOVED:
//                            Toast.makeText(getContext(), "Removed: id "+ id , Toast.LENGTH_LONG).show();
//                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                            break;
                    }
                }

            }
        });*/
    }

    private void notification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "n")
                    .setContentTitle("Code Sphere")
                    .setSmallIcon(R.drawable.ic_adb_black_24dp)
                    .setAutoCancel(true)
                    .setContentText("New Data Added");
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getActivity());
            managerCompat.notify(999, builder.build());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        reportListener.remove();
    }
}
