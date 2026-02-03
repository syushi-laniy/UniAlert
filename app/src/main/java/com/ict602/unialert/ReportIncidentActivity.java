package com.ict602.unialert;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReportIncidentActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Spinner spCategory;
    private EditText etDesc;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        spCategory = findViewById(R.id.spCategory);
        etDesc = findViewById(R.id.etDesc);
        btnSubmit = findViewById(R.id.btnSubmit);

        String[] categories = new String[]{"accident", "crime", "facility", "other"};

        // ✅ FIX: custom spinner text color (hitam) + dropdown background cream
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_black,
                categories
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
        spCategory.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        if (desc.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter description", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (MapActivity.lastLat == null || MapActivity.lastLng == null) {
            Snackbar.make(findViewById(android.R.id.content),
                    "No GPS yet. Go back to map and wait a moment.", Snackbar.LENGTH_LONG).show();
            return;
        }

        String category = spCategory.getSelectedItem().toString();
        String uid = auth.getCurrentUser().getUid();
        String username = auth.getCurrentUser().getEmail() != null ? auth.getCurrentUser().getEmail() : "user";

        String userAgent = Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")";

        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("username", username);
        data.put("category", category);
        data.put("description", desc);
        data.put("lat", MapActivity.lastLat);
        data.put("lng", MapActivity.lastLng);
        data.put("reportedAt", FieldValue.serverTimestamp());
        data.put("userAgent", userAgent);

        db.collection("incidents").add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Incident submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Submit failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
