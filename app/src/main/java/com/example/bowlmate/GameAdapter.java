package com.example.bowlmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context;
    private List<Game> gameList;
    private List<Game> fullGameList; // To hold the complete list of games

    public GameAdapter(Context context, List<Game> gameList) {
        this.context = context;
        this.gameList = new ArrayList<>(gameList); // Initialize with a copy
        this.fullGameList = new ArrayList<>(gameList); // Initialize full list
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = gameList.get(position);

        holder.titleText.setText(game.getTitle()); // Set the game title
        holder.scoreText.setText("Score: " + game.getScore());

        // Format and set the date
        if (game.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(game.getTimestamp().toDate());
            holder.dateText.setText(formattedDate);
        } else {
            holder.dateText.setText("No Date"); // Handle cases where timestamp might be missing
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GameDetailActivity.class);
            intent.putExtra("gameId", game.getId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Game")
                    .setMessage("Are you sure you want to delete this game?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteGame(game.getId()))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteGame(String gameId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("games")
                .document(gameId)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Game deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete game", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    // Method to update the full list of data
    public void updateData(List<Game> newList) {
        fullGameList.clear();
        fullGameList.addAll(newList);
        // Do not call notifyDataSetChanged() here, it will be called by applySearchFilter
    }

    // New method for applying search filter
    public void applySearchFilter(String query) {
        gameList.clear();
        if (query.isEmpty()) {
            gameList.addAll(fullGameList); // If query is empty, show all games
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            for (Game game : fullGameList) {
                // Search by title
                boolean matchesTitle = game.getTitle() != null && game.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);

                // Search by score (exact match or partial string match if query is numeric)
                boolean matchesScore = false;
                try {
                    int scoreQuery = Integer.parseInt(query);
                    if (game.getScore() == scoreQuery) {
                        matchesScore = true;
                    }
                } catch (NumberFormatException e) {
                    // Not a number, so check if score string contains query
                    if (String.valueOf(game.getScore()).contains(query)) {
                        matchesScore = true;
                    }
                }


                // Search by date (formatted date string)
                boolean matchesDate = false;
                if (game.getTimestamp() != null) {
                    String formattedDate = sdf.format(game.getTimestamp().toDate()).toLowerCase(Locale.getDefault());
                    matchesDate = formattedDate.contains(lowerCaseQuery);
                }

                if (matchesTitle || matchesScore || matchesDate) {
                    gameList.add(game);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, scoreText, dateText;
        ImageButton deleteButton;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            scoreText = itemView.findViewById(R.id.scoreText);
            dateText = itemView.findViewById(R.id.dateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}