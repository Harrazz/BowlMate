package com.example.bowlmate;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BowlingScore extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private GameAdapter adapter;
    private List<Game> gameList;
    private TextView averageScoreValue;
    private SearchView searchView;
    private static final int VOICE_SEARCH_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bowling_score);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView averageScoreLabel = findViewById(R.id.averageScoreLabel);
        averageScoreValue = findViewById(R.id.averageScoreValue); // Initialize averageScoreValue

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

        searchView = findViewById(R.id.search_view); // Initialize SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.applySearchFilter(query); // Apply filter on submit
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.applySearchFilter(newText); // Apply filter on text change
                return true;
            }
        });

        ImageButton micButton = findViewById(R.id.mic_button); // Initialize mic button
        micButton.setOnClickListener(v -> startVoiceInput());

        // Request RECORD_AUDIO permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        loadGames();

        FloatingActionButton addGameButton = findViewById(R.id.addGameButton);
        addGameButton.setOnClickListener(v -> {
            startActivity(new Intent(BowlingScore.this, AddGameActivity.class));
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a game title, score, or date...");
        try {
            startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Voice input not supported on your device.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                searchView.setQuery(result.get(0), true); // Set the query and submit it
            }
        }
    }

    private void loadGames() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("games")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by latest first
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load games: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    gameList.clear(); // Clear existing list
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
                    adapter.updateData(gameList); // Update the adapter with the full data
                    // Apply current search filter to refresh
                    adapter.applySearchFilter(searchView.getQuery().toString());


                    if (!gameList.isEmpty()) {
                        int average = totalScore / gameList.size();
                        averageScoreValue.setText(average + " / 300");
                    } else {
                        averageScoreValue.setText("- / 300");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload games to reflect any changes from AddGameActivity or EditGameActivity
        if (auth.getCurrentUser() != null) {
            loadGames();
        }
    }
}