package com.example.bowlmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class EditGameActivity extends AppCompatActivity {

    private EditText scoreInput, strikeInput, spareInput, noteInput;
    private Button updateButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);

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
    }

    private void loadGameData() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("games")
                .document(gameId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
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

        int score = Integer.parseInt(scoreInput.getText().toString().trim());
        int strikes = Integer.parseInt(strikeInput.getText().toString().trim());
        int spares = Integer.parseInt(spareInput.getText().toString().trim());
        String notes = noteInput.getText().toString().trim();

        db.collection("users").document(uid).collection("games")
                .document(gameId)
                .update("score", score,
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
