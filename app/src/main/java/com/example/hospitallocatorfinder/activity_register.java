package com.example.hospitallocatorfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class activity_register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText nameEditText, emailEditText, passwordEditText;
    private Button registerButton;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        nameEditText = findViewById(R.id.name);  // New field for user's name
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        // Register button click listener
        registerButton.setOnClickListener(v -> registerUser());

        // Login link click listener
        loginLink.setOnClickListener(v -> startActivity(new Intent(activity_register.this, MainActivity.class)));
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity_register.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase sign-up with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save user info to Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);

                        if (user != null) {
                            db.collection("users").document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(activity_register.this, "Registration successful. Please login.", Toast.LENGTH_SHORT).show();

                                        FirebaseAuth.getInstance().signOut(); // Logout after registration

                                        // Redirect to Login Page
                                        Intent intent = new Intent(activity_register.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(activity_register.this, "Error saving user info.", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(activity_register.this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
