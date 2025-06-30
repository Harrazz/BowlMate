package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class GameDetailActivity extends AppCompatActivity {

    private TextView scoreText, strikeText, spareText, noteText;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        scoreText = findViewById(R.id.detailScoreText);
        strikeText = findViewById(R.id.detailStrikeText);
        spareText = findViewById(R.id.detailSpareText);
        noteText = findViewById(R.id.detailNoteText);

        Button editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameDetailActivity.this, EditGameActivity.class);
            intent.putExtra("gameId", getIntent().getStringExtra("gameId"));
            startActivity(intent);
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String gameId = getIntent().getStringExtra("gameId");

        if (gameId != null) {
            loadGame(gameId);
        } else {
            Toast.makeText(this, "No game selected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadGame(String gameId) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("games")
                .document(gameId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        scoreText.setText("Score: " + doc.getLong("score"));
                        strikeText.setText("Strikes: " + doc.getLong("totalStrikes"));
                        spareText.setText("Spares: " + doc.getLong("totalSpares"));
                        noteText.setText("Notes: " + doc.getString("notes"));
                    } else {
                        Toast.makeText(this, "Game not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load game", Toast.LENGTH_SHORT).show());
    }
}
