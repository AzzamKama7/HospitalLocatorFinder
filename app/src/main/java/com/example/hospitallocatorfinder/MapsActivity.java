package com.example.hospitallocatorfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker userMarker;

    private double lastLatitude = Double.NaN;
    private double lastLongitude = Double.NaN;

    // Hospital Data with Name, Coordinates, and Operating Hours
    private final Map<LatLng, String> hospitalData = new HashMap<LatLng, String>() {{
        put(new LatLng(3.115694, 101.653203), "Universiti Malaya Medical Centre\nOpen: 8 AM - Close: 8 PM\n50603");
        put(new LatLng(3.068599, 101.609480), "Sunway Medical Centre\nOpen: 7 AM - Close: 10 PM\nBandar Sunway, 47500 Petaling Jaya, Selangor");
        put(new LatLng(3.139011, 101.627451), "KPJ Damansara Specialist Hospital\nOpen: 7 AM - Close: 10 PM");
        put(new LatLng(2.981560, 101.719518), "Sultan Idris Shah Hospital, Serdang\nOpen: 24 Hours");
        put(new LatLng(3.086334, 101.594401), "Subang Jaya Medical Centre\nOpen: 8 AM - Close: 8 PM");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapsActivity", "Error: Map fragment is null!");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableUserLocation();
        }

        // Add hospital markers with info window
        addHospitalMarkers();
    }

    private void enableUserLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                LatLng kl = new LatLng(3.1390, 101.6869);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kl, 12));
                setupLocationUpdates();
            }
        } catch (SecurityException e) {
            Log.e("MapsActivity", "SecurityException: " + e.getMessage());
        }
    }

    private void setupLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d("MapsActivity", "New Location: " + location.getLatitude() + ", " + location.getLongitude());

                        if (hasLocationChanged(location.getLatitude(), location.getLongitude())) {
                            updateUserLocation(location);
                            fetchUserDataAndSendLocation(location.getLatitude(), location.getLongitude());

                            lastLatitude = location.getLatitude();
                            lastLongitude = location.getLongitude();
                        } else {
                            Log.d("MapsActivity", "Location unchanged, skipping update.");
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateUserLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
        } else {
            userMarker.setPosition(userLocation);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));
    }

    private void addHospitalMarkers() {
        for (Map.Entry<LatLng, String> entry : hospitalData.entrySet()) {
            mMap.addMarker(new MarkerOptions().position(entry.getKey()).title(entry.getValue()));
        }
    }

    private boolean hasLocationChanged(double latitude, double longitude) {
        return Double.isNaN(lastLatitude) || Double.isNaN(lastLongitude) ||
                Math.abs(latitude - lastLatitude) > 0.0001 || Math.abs(longitude - lastLongitude) > 0.0001;
    }

    private void fetchUserDataAndSendLocation(double latitude, double longitude) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            String userId = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");
                    if (userName == null || userName.isEmpty()) {
                        userName = "Unknown User";
                    }

                    sendLocationToServer(latitude, longitude, userName, userEmail);
                } else {
                    Log.e("MapsActivity", "User not found in Firestore.");
                }
            }).addOnFailureListener(e -> Log.e("MapsActivity", "Firestore error: " + e.getMessage()));
        } else {
            Log.e("MapsActivity", "No authenticated user found.");
        }
    }

    private void sendLocationToServer(double latitude, double longitude, String userName, String userEmail) {
        try {
            String encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString());
            String encodedUserEmail = URLEncoder.encode(userEmail, StandardCharsets.UTF_8.toString());
            String serverUrl = "http://10.0.2.2/hospital_locator/location_api.php";

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    String data = "latitude=" + latitude + "&longitude=" + longitude +
                            "&user_name=" + encodedUserName + "&user_email=" + encodedUserEmail;

                    URL url = new URL(serverUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    try (OutputStream outputStream = urlConnection.getOutputStream()) {
                        outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("MapsActivity", "Data sent successfully to server");
                    } else {
                        Log.e("MapsActivity", "Server Error: " + responseCode);
                    }

                    urlConnection.disconnect();
                } catch (Exception e) {
                    Log.e("MapsActivity", "Error Sending Data", e);
                }
            });

        } catch (Exception e) {
            Log.e("MapsActivity", "Encoding Error", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
