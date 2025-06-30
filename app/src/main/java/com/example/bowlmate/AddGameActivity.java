package com.example.bowlmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("score", score);
        gameData.put("totalStrikes", strikes);
        gameData.put("totalSpares", spares);
        gameData.put("notes", notes);

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
