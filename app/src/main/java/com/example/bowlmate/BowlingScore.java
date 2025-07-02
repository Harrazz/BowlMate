package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class BowlingScore extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private GameAdapter adapter;
    private List<Game> gameList;
    private TextView averageScoreText; // This variable is not used, can be removed if not needed.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bowling_score);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView averageScoreLabel = findViewById(R.id.averageScoreLabel);
        TextView averageScoreValue = findViewById(R.id.averageScoreValue);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            startActivity(new Intent(BowlingScore.this, AboutApp.class));
        });

        recyclerView = findViewById(R.id.gamesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gameList = new ArrayList<>();
        adapter = new GameAdapter(this, gameList);
        recyclerView.setAdapter(adapter);

        loadGames();

        FloatingActionButton addGameButton = findViewById(R.id.addGameButton);
        addGameButton.setOnClickListener(v -> {
            startActivity(new Intent(BowlingScore.this, AddGameActivity.class));
        });
    }

    private void loadGames() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("games")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by latest first
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load games", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    gameList.clear();
                    int totalScore = 0;

                    for (DocumentSnapshot doc : snapshots) {
                        Game game = doc.toObject(Game.class);
                        if (game != null) {
                            game.setId(doc.getId());
                            // Timestamp might be null for older entries, handle it
                            if (doc.getTimestamp("timestamp") != null) {
                                game.setTimestamp(doc.getTimestamp("timestamp"));
                            }
                            gameList.add(game);
                            totalScore += game.getScore();
                        }
                    }

                    adapter.notifyDataSetChanged();

                    TextView averageScoreValue = findViewById(R.id.averageScoreValue);
                    if (!gameList.isEmpty()) {
                        int average = totalScore / gameList.size();
                        averageScoreValue.setText(average + " / 300");
                    } else {
                        averageScoreValue.setText("- / 300");
                    }
                });
    }
}