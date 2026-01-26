package com.ict602.unialert;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NewsAdapter adapter;
    private List<NewsModel> newsList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        recyclerView = findViewById(R.id.recyclerNews);
        progressBar = findViewById(R.id.progressNews);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadNews();
    }

    private void loadNews() {

        if (!NetworkUtil.isOnline(this)) {
            Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection", Snackbar.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("news")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    newsList.clear();

                    if (snapshot.isEmpty()) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "No latest news available", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    snapshot.forEach(doc -> {
                        String title = doc.getString("title");
                        String content = doc.getString("content");

                        newsList.add(new NewsModel(title, content,
                                doc.getTimestamp("createdAt")));
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Failed to load news", Snackbar.LENGTH_LONG).show();
                });
    }
}
