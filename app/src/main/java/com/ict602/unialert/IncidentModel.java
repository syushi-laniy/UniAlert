package com.ict602.unialert;

import com.google.firebase.Timestamp;

public class IncidentModel {
    public String category;
    public String description;
    public String username;
    public Timestamp reportedAt;
    public Double lat;
    public Double lng;

    public IncidentModel(String category, String description, String username,
                         Timestamp reportedAt, Double lat, Double lng) {
        this.category = category;
        this.description = description;
        this.username = username;
        this.reportedAt = reportedAt;
        this.lat = lat;
        this.lng = lng;
    }
}