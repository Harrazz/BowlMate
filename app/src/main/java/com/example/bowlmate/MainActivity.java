package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView; // Import TextView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.DocumentReference; // Import DocumentReference
import com.google.firebase.firestore.FirebaseFirestore; // Import FirebaseFirestore

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;     // Declare TextView for "Welcome"
    private TextView userNameTextView; // Declare TextView for username
    private FirebaseAuth mAuth;       // Declare FirebaseAuth
    private FirebaseFirestore db;     // Declare FirebaseFirestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize the TextViews
        welcomeText = findViewById(R.id.welcomeText);
        userNameTextView = findViewById(R.id.userNameTextView);

        // Set the static "Welcome" text
        welcomeText.setText("Welcome");

        // Get current user and display name
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");
                    if (userName != null && !userName.isEmpty()) {
                        userNameTextView.setText(userName); // Set the retrieved name
                    } else {
                        userNameTextView.setText("User"); // Fallback if name is empty/null
                    }
                } else {
                    userNameTextView.setText("User"); // Fallback if document doesn't exist
                }
            }).addOnFailureListener(e -> {
                userNameTextView.setText("User"); // Fallback on error
                // Log the error for debugging: Log.e("MainActivity", "Error fetching user data", e);
            });
        } else {
            // If somehow currentUser is null, redirect to Login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutApp.class);
            startActivity(intent);
        });

        ImageButton btnBowlingBall = findViewById(R.id.btnBowlingBall);
        btnBowlingBall.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BowlingBall.class));
        });

        ImageButton btnShopLocation = findViewById(R.id.btnShopLocation);
        btnShopLocation.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BowlingLocation.class));
        });

        ImageButton btnYourScore = findViewById(R.id.btnYourScore);
        btnYourScore.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BowlingScore.class));
        });

        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Profile.class));
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}