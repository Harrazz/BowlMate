package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bowling_score);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load games", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    gameList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Game game = doc.toObject(Game.class);
                        game.setId(doc.getId());
                        gameList.add(game);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}