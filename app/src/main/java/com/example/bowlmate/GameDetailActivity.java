package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GameDetailActivity extends AppCompatActivity {

    private TextView titleText, dateText, scoreText, strikeText, spareText, noteText; // Added titleText and dateText

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        titleText = findViewById(R.id.detailTitleText); // Initialize titleText
        dateText = findViewById(R.id.detailDateText);   // Initialize dateText
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

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            startActivity(new Intent(GameDetailActivity.this, AboutApp.class));
        });
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
                        titleText.setText("Game: " + doc.getString("title")); // Set title
                        scoreText.setText("Score: " + doc.getLong("score"));
                        strikeText.setText("Strikes: " + doc.getLong("totalStrikes"));
                        spareText.setText("Spares: " + doc.getLong("totalSpares"));
                        noteText.setText("Notes: " + doc.getString("notes"));

                        // Format and set the date
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                            String formattedDate = sdf.format(timestamp.toDate());
                            dateText.setText("Date: " + formattedDate);
                        } else {
                            dateText.setText("Date: N/A");
                        }

                    } else {
                        Toast.makeText(this, "Game not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load game", Toast.LENGTH_SHORT).show());
    }
}
