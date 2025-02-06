package com.example.hospitallocatorfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.firebase.auth.FirebaseAuth;

public class AboutUs extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_us);

        // Initialize UI elements for the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.my_toolbar);

        // Set the toolbar as the action bar
        setSupportActionBar(toolbar);

        // Set up the navigation drawer toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handling navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Home logic
                Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AboutUs.this, activity_home.class));
            } else if (itemId == R.id.nav_map) {
                // Map logic
                startActivity(new Intent(AboutUs.this, MapsActivity.class));
            } else if (itemId == R.id.nav_about) {
                // Already on AboutUs page
                Toast.makeText(this, "About Us Selected", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_logout) {
                // Logout logic
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AboutUs.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawers(); // Close the drawer after selecting an option
            return true;
        });

        // Apply window insets to allow content to be displayed edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setting up the GitHub button functionality
        Button gitHubButton = findViewById(R.id.gitHubButton);
        gitHubButton.setOnClickListener(v -> {
            // Open the GitHub repository when the button is clicked
            String url = "https://github.com/your-repository"; // Replace with your actual GitHub URL
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
    }

    // Handle menu icon click to open navigation drawer
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
