package com.ict602.unialert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static Double lastLat = null;
    public static Double lastLng = null;

    private static final int REQ_LOC = 101;

    private GoogleMap map;
    private FusedLocationProviderClient fused;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private MaterialToolbar topAppBar;

    private Snackbar noInternetSnack;
    private final Handler netHandler = new Handler(Looper.getMainLooper());
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

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setSubtitle("Hi, User 👋");

        // Add Logout without new XML menu file
        setupToolbarMenu(topAppBar);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Auth check
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Greeting
        db.collection("users").document(auth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("fullName");
                    if (name == null || name.trim().isEmpty()) name = "User";
                    topAppBar.setSubtitle("Hi, " + name + " 👋");
                });

        // FAB - Report
        FloatingActionButton fabReport = findViewById(R.id.fabReport);
        fabReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportIncidentActivity.class))
        );

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_map);
        // In MapActivity.onCreate()
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_map) {
                return true;
            } else if (id == R.id.nav_incidents) {
                Intent i = new Intent(this, IncidentListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            } else if (id == R.id.nav_news) {
                Intent i = new Intent(this, NewsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            } else if (id == R.id.nav_about) {
                Intent i = new Intent(this, AboutActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                return true;
            }

            return false;
        });

        // Map
        fused = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void setupToolbarMenu(MaterialToolbar toolbar) {
        Menu menu = toolbar.getMenu();
        menu.clear();

        MenuItem logoutItem = menu.add("Logout");
        logoutItem.setIcon(android.R.drawable.ic_lock_power_off);
        logoutItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        toolbar.setOnMenuItemClickListener(item -> {
            if ("Logout".contentEquals(item.getTitle())) {
                doLogout();
                return true;
            }
            return false;
        });
    }

    private void doLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        requestLocation();
        loadMarkers();
    }

    private void requestLocation() {
        if (map == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC
            );
            return;
        }

        map.setMyLocationEnabled(true);

        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    lastLat = location.getLatitude();
                    lastLng = location.getLongitude();

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lastLat, lastLng), 16f
                    ));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadMarkers() {
        if (map == null) return;

        // 1) locations collection (admin-added)
        db.collection("locations")
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat == null || lng == null) continue;

                        String name = doc.getString("name");
                        if (name == null) name = "Location";

                        String type = doc.getString("type"); // security / clinic / emergency / other
                        if (type == null) type = "other";

                        float hue = BitmapDescriptorFactory.HUE_AZURE; // default
                        switch (type) {
                            case "security":
                                hue = BitmapDescriptorFactory.HUE_BLUE;
                                break;
                            case "clinic":
                                hue = BitmapDescriptorFactory.HUE_GREEN;
                                break;
                            case "emergency":
                                hue = BitmapDescriptorFactory.HUE_VIOLET;
                                break;
                            default:
                                hue = BitmapDescriptorFactory.HUE_AZURE;
                                break;
                        }

                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title(name)
                                .snippet(type.toUpperCase())
                                .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                    }
                });

        // 2) incidents collection (user-reported)
        db.collection("incidents")
                .orderBy("reportedAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        if (lat == null || lng == null) continue;

                        String category = doc.getString("category"); // accident/crime/facility/other
                        if (category == null) category = "incident";

                        String desc = doc.getString("description");
                        if (desc == null) desc = "";

                        // Incident marker color (ikut category)
                        float hue = BitmapDescriptorFactory.HUE_RED;
                        switch (category) {
                            case "crime":
                                hue = BitmapDescriptorFactory.HUE_RED;
                                break;
                            case "accident":
                                hue = BitmapDescriptorFactory.HUE_ORANGE;
                                break;
                            case "facility":
                                hue = BitmapDescriptorFactory.HUE_YELLOW;
                                break;
                            default:
                                hue = BitmapDescriptorFactory.HUE_ROSE;
                                break;
                        }

                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title(category.toUpperCase())
                                .snippet(desc)
                                .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                    }
                });
    }


    private void showInternetStatus() {
        // Pastikan NetworkUtil class wujud
        boolean online = NetworkUtil.isOnline(this);

        if (!online) {
            if (noInternetSnack == null) {
                noInternetSnack = Snackbar.make(
                        findViewById(android.R.id.content),
                        "No internet connection",
                        Snackbar.LENGTH_INDEFINITE
                ).setAction("Retry", v -> showInternetStatus());
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

        if (requestCode == REQ_LOC) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Location permission denied", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}