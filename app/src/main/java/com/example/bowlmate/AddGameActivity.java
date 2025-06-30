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
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class AddGameActivity extends AppCompatActivity {

    private EditText scoreInput, strikeInput, spareInput, noteInput;
    private Button saveButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);

        scoreInput = findViewById(R.id.scoreInput);
        strikeInput = findViewById(R.id.strikeInput);
        spareInput = findViewById(R.id.spareInput);
        noteInput = findViewById(R.id.noteInput);
        saveButton = findViewById(R.id.saveButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveGame());

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            startActivity(new Intent(AddGameActivity.this, AboutApp.class));
        });
    }

    private void saveGame() {
        String uid = auth.getCurrentUser().getUid();
        String scoreStr = scoreInput.getText().toString().trim();
        String strikeStr = strikeInput.getText().toString().trim();
        String spareStr = spareInput.getText().toString().trim();
        String notes = noteInput.getText().toString().trim();

        if (scoreStr.isEmpty() || strikeStr.isEmpty() || spareStr.isEmpty()) {
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

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("score", score);
        gameData.put("totalStrikes", strikes);
        gameData.put("totalSpares", spares);
        gameData.put("notes", notes);
        gameData.put("timestamp", Timestamp.now());

        db.collection("users")
                .document(uid)
                .collection("games")
                .add(gameData)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Game saved", Toast.LENGTH_SHORT).show();
                    finish(); // close this activity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show());
    }
}