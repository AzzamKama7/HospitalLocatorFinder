package com.example.hospitallocatorfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_home extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeTextView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar; // 🔥 Added Toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI Elements
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        toolbar = findViewById(R.id.toolbar); // 🔥 Find Toolbar

        // Set the toolbar as the action bar
        setSupportActionBar(toolbar); // ✅ Fixes getSupportActionBar() NullPointerException

        // Set up Navigation Drawer
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(activity_home.this, MainActivity.class));
            finish();
            return;
        }

        // Fetch user's name from Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        welcomeTextView.setText("Welcome, " + (name != null ? name : "User") + "!");
                    }
                });

        // Handle Navigation Item Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_map) {
                startActivity(new Intent(activity_home.this, MapsActivity.class));
            } else if (itemId == R.id.nav_about) {
                // Navigate to the About Us Activity
                startActivity(new Intent(activity_home.this, AboutUs.class));
            } else if (itemId == R.id.nav_logout) {
                mAuth.signOut();
                Intent intent = new Intent(activity_home.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    // Handle menu icon click to open navigation drawer
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Add this method for "Find Hospital" button
    public void onGoToMapClicked(View view) {
        // Navigate to MapsActivity when the button is clicked
        startActivity(new Intent(activity_home.this, MapsActivity.class));
    }
}
