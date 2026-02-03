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

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ViewHolder> {

    private List<IncidentModel> incidentList;

    public IncidentAdapter(List<IncidentModel> incidentList) {
        this.incidentList = incidentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncidentModel incident = incidentList.get(position);

        holder.tvIncidentType.setText(incident.category.toUpperCase());
        holder.tvIncidentDesc.setText(incident.description);
        holder.tvUsername.setText("By: " + incident.username);

        if (incident.reportedAt != null) {
            String date = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(incident.reportedAt.toDate());
            holder.tvTime.setText(date);
        }

        // Set icon based on category
        String icon = "!";
        switch (incident.category.toLowerCase()) {
            case "accident": icon = "⚠"; break;
            case "crime": icon = "🚨"; break;
            case "facility": icon = "🔧"; break;
            default: icon = "📋";
        }
        holder.tvIcon.setText(icon);
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    public void filterList(List<IncidentModel> filteredList) {
        incidentList = filteredList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvIncidentType, tvIncidentDesc, tvTime, tvUsername;

        ViewHolder(View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvIncidentType = itemView.findViewById(R.id.tvIncidentType);
            tvIncidentDesc = itemView.findViewById(R.id.tvIncidentDesc);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUsername = itemView.findViewById(R.id.tvUsername); // Add this to item_incident.xml!
        }
    }
}