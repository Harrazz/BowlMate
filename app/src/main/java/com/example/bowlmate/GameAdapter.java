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

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context;
    private List<Game> gameList;

    public GameAdapter(Context context, List<Game> gameList) {
        this.context = context;
        this.gameList = gameList;
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

        holder.scoreText.setText("Score: " + game.getScore());
        holder.strikeText.setText("Strikes: " + game.getTotalStrikes());
        holder.spareText.setText("Spares: " + game.getTotalSpares());

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

    public static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView scoreText, strikeText, spareText;
        ImageButton deleteButton;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            scoreText = itemView.findViewById(R.id.scoreText);
            strikeText = itemView.findViewById(R.id.strikeText);
            spareText = itemView.findViewById(R.id.spareText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
