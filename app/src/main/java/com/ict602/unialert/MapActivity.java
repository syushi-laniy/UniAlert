package com.ict602.unialert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static Double lastLat = null;
    public static Double lastLng = null;

    private static final int REQ_LOC = 101;

    private GoogleMap map;
    private FusedLocationProviderClient fused;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvGreeting;
    private Button btnReport, btnAbout, btnLogout;

    private Snackbar noInternetSnack;
    private final Handler netHandler = new Handler();
    private final Runnable netChecker = new Runnable() {
        @Override
        public void run() {
            showInternetStatus();
            netHandler.postDelayed(this, 1500);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        netHandler.post(netChecker);
    }

    @Override
    protected void onStop() {
        super.onStop();
        netHandler.removeCallbacks(netChecker);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        tvGreeting = findViewById(R.id.tvGreeting);
        btnReport = findViewById(R.id.btnReport);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("fullName");
                    if (name == null) name = "User";
                    tvGreeting.setText("Hi, " + name + " 👋");
                });

        btnReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportIncidentActivity.class)));

        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        fused = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        requestLocation();
        loadMarkers();
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
            return;
        }

        map.setMyLocationEnabled(true);

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .build();

        fused.requestLocationUpdates(request, location -> {
            lastLat = location.getLatitude();
            lastLng = location.getLongitude();
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(lastLat, lastLng), 16f));
        }, getMainLooper());
    }

    private void loadMarkers() {
        db.collection("locations").get().addOnSuccessListener(snap -> {
            for (var doc : snap.getDocuments()) {
                Double lat = doc.getDouble("lat");
                Double lng = doc.getDouble("lng");
                if (lat == null || lng == null) continue;

                map.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(doc.getString("name")));
            }
        });

        db.collection("incidents")
                .orderBy("reportedAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> {
                    for (var doc : snap.getDocuments()) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat == null || lng == null) continue;

                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title("INCIDENT")
                                .snippet(doc.getString("description")));
                    }
                });
    }

    private void showInternetStatus() {
        boolean online = NetworkUtil.isOnline(this);

        if (!online) {
            if (noInternetSnack == null) {
                noInternetSnack = Snackbar
                        .make(findViewById(android.R.id.content),
                                "No internet connection",
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", v -> showInternetStatus());
            }
            if (!noInternetSnack.isShown()) noInternetSnack.show();
        } else {
            if (noInternetSnack != null && noInternetSnack.isShown()) {
                noInternetSnack.dismiss();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC) requestLocation();
    }
}
