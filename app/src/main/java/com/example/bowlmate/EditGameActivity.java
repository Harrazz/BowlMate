package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditGameActivity extends AppCompatActivity {

    private EditText titleInput, scoreInput, strikeInput, spareInput, noteInput; // Added titleInput
    private Button updateButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);

        titleInput = findViewById(R.id.titleInput);
        scoreInput = findViewById(R.id.scoreInput);
        strikeInput = findViewById(R.id.strikeInput);
        spareInput = findViewById(R.id.spareInput);
        noteInput = findViewById(R.id.noteInput);
        updateButton = findViewById(R.id.updateButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        gameId = getIntent().getStringExtra("gameId");

        if (gameId != null) {
            loadGameData();
        } else {
            Toast.makeText(this, "Game not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        updateButton.setOnClickListener(v -> updateGame());

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            startActivity(new Intent(EditGameActivity.this, AboutApp.class));
        });
    }

    private void loadGameData() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("games")
                .document(gameId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        titleInput.setText(doc.getString("title")); // Set title
                        scoreInput.setText(String.valueOf(doc.getLong("score")));
                        strikeInput.setText(String.valueOf(doc.getLong("totalStrikes")));
                        spareInput.setText(String.valueOf(doc.getLong("totalSpares")));
                        noteInput.setText(doc.getString("notes"));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    private void updateGame() {
        String uid = auth.getCurrentUser().getUid();

        String title = titleInput.getText().toString().trim(); // Get title
        String scoreStr = scoreInput.getText().toString().trim();
        String strikeStr = strikeInput.getText().toString().trim();
        String spareStr = spareInput.getText().toString().trim();
        String notes = noteInput.getText().toString().trim();

        // Validate title
        if (title.isEmpty() || scoreStr.isEmpty() || strikeStr.isEmpty() || spareStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int score = Integer.parseInt(scoreStr);
        int strikes = Integer.parseInt(strikeStr);
        int spares = Integer.parseInt(spareStr);

        if (score > 300) {
            Toast.makeText(this, "Score cannot exceed 300", Toast.LENGTH_SHORT).show();
            return;
        }

        if (strikes > 12) {
            Toast.makeText(this, "Strikes cannot exceed 12", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spares > 11) {
            Toast.makeText(this, "Spares cannot exceed 11", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).collection("games")
                .document(gameId)
                .update("title", title, // Update title
                        "score", score,
                        "totalStrikes", strikes,
                        "totalSpares", spares,
                        "notes", notes)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Game updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}