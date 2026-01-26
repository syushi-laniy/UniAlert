package com.ict602.unialert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private final List<NewsModel> list;

    public NewsAdapter(List<NewsModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        NewsModel n = list.get(i);

        h.tvTitle.setText(n.title);
        h.tvContent.setText(n.content);

        if (n.createdAt != null) {
            String date = new SimpleDateFormat(
                    "dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(n.createdAt.toDate());
            h.tvDate.setText(date);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvNewsTitle);
            tvContent = v.findViewById(R.id.tvNewsContent);
            tvDate = v.findViewById(R.id.tvNewsDate);
        }
    }
}
