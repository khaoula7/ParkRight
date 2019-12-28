package com.charikati.parkright;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.charikati.parkright.adapter.ReportAdapter;
import com.charikati.parkright.model.ViolationReport;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MyReportsActivity extends BaseActivity {
    private static final String TAG = "MyReportsActivity";
    private ListView mViolationList;
    private ReportAdapter mAdapter;
    private ProgressBar progressBar;
    /*Firebase instance variables*/
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.reports_name);
        //Initialize firbase instances
        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progress_bar);
        // Create a list of violations
        final ArrayList<ViolationReport> myViolations = new ArrayList<>();
        // Create a ReportAdapter which knows how to create list items for each item in the list.
        mAdapter = new ReportAdapter(this, R.layout.report_item, myViolations );
        //Find ListView in activity_my_reports.xml
        mViolationList = findViewById(R.id.list_view);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = findViewById(R.id.empty);
//        mViolationList .setEmptyView(emptyView);

        // Apply adapter to the listView
        mViolationList.setAdapter(mAdapter);
        //Get a realtime snapshot of all documents in subcollection sent_violations
        mFirestore.collection("Users").document(mFirebaseAuth.getCurrentUser().getUid())
                .collection("sent_violations")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        if(value.isEmpty()){
                            Log.d(TAG, "No Reports Yet !");
                            progressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getId() != null) {
                               queryViolations(doc.getId());
                            }
                        }

                    }
                });

        //Click on report item will open violation_details screen
        mViolationList.setOnItemClickListener((parent, view, position, id) -> {
            ViolationReport violationReport = (ViolationReport) parent.getItemAtPosition(position);
            Intent intent = new Intent(MyReportsActivity.this, DetailsActivity.class);
            //Convert ViolationReport object into a JSON object
            intent.putExtra("report", new Gson().toJson(violationReport));
            startActivity(intent);
        });
    }

    /**
     * Send a query to database with violation id to get all information about it
     */
    private void queryViolations(String id) {
        mFirestore.collection("Violations").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //Deserialize the violation from database into a ViolationReport object
                        ViolationReport violationReport = documentSnapshot.toObject(ViolationReport.class);
                        //Add ViolationReport object into adapter
                        mAdapter.add(violationReport);
                        progressBar.setVisibility(View.GONE);
                    }
        });
    }
}
