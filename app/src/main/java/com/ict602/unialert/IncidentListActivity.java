package com.ict602.unialert;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class IncidentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private View emptyState;
    private TextInputEditText etSearch;

    private FirebaseFirestore db;
    private List<IncidentModel> incidentList = new ArrayList<>();
    private IncidentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_list);

        recyclerView = findViewById(R.id.rvIncidents);
        progressBar = findViewById(R.id.progressIncidents); // Add this to XML!
        emptyState = findViewById(R.id.emptyState);
        etSearch = findViewById(R.id.etSearch);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidentAdapter(incidentList);
        recyclerView.setAdapter(adapter);

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterIncidents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadIncidents();
    }

    private void loadIncidents() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        db.collection("incidents")
                .orderBy("reportedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    incidentList.clear();

                    if (querySnapshot.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        return;
                    }

                    recyclerView.setVisibility(View.VISIBLE);

                    for (var doc : querySnapshot.getDocuments()) {
                        IncidentModel incident = new IncidentModel(
                                doc.getString("category"),
                                doc.getString("description"),
                                doc.getString("username"),
                                doc.getTimestamp("reportedAt"),
                                doc.getDouble("lat"),
                                doc.getDouble("lng")
                        );
                        incidentList.add(incident);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    // Show error
                });
    }

    private void filterIncidents(String query) {
        List<IncidentModel> filtered = new ArrayList<>();

        if (query.isEmpty()) {
            filtered.addAll(incidentList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (IncidentModel incident : incidentList) {
                if (incident.category.toLowerCase().contains(lowerQuery) ||
                        incident.description.toLowerCase().contains(lowerQuery)) {
                    filtered.add(incident);
                }
            }
        }

        adapter.filterList(filtered);

        if (filtered.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}