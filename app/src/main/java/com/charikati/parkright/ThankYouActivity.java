package com.charikati.parkright;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class ThankYouActivity extends AppCompatActivity {
    public static final String TAG = "ThankyouActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thankyou);
        //Get the first name of the user
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fireDb = FirebaseFirestore.getInstance();
        TextView userName = findViewById(R.id.user_name_txt);
        String Uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        DocumentReference docRef = fireDb.collection("Users").document(Uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {
                    String title = getResources().getString(R.string.thank_you) + " " + Objects.requireNonNull(document.getData()).get("firstName");
                    userName.setText(title);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
        //Use toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.activity_toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        //Click on New Violation Button
        findViewById(R.id.new_violation_btn).setOnClickListener(v -> {
            //Send an intent for main activity to open type fragment
            Intent openFragTypeIntent = new Intent(ThankYouActivity.this, MainActivity.class);
            openFragTypeIntent.putExtra("OPEN_FRAG_TYPE", 1000);
            startActivity(openFragTypeIntent);
        });
        //Click on My Reports Button
        findViewById(R.id.my_reports_btn).setOnClickListener(v -> {
            //Send an intent for main activity to open my reports fragment
            Intent openFragReportsIntent = new Intent(ThankYouActivity.this, MainActivity.class);
            openFragReportsIntent.putExtra("OPEN_FRAG_REPORTS", 2000);
            startActivity(openFragReportsIntent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {
            Toast.makeText(this, "Share", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}
