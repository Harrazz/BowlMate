package com.example.bowlmate;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BowlingBall extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BowlingBallAdapter adapter;
    private List<BowlingBallModel> ballList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private Spinner spinnerBrand, spinnerPerformance, spinnerSort;
    private String selectedBrand = "All", selectedPerformance = "All", selectedSort = "Default";
    private static final int VOICE_SEARCH_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bowling_ball);

        recyclerView = findViewById(R.id.ball_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ballList = new ArrayList<>();
        adapter = new BowlingBallAdapter(this, ballList);
        recyclerView.setAdapter(adapter);
        searchView = findViewById(R.id.search_view);

        spinnerBrand = findViewById(R.id.spinner_brand);
        spinnerPerformance = findViewById(R.id.spinner_performance);
        spinnerSort = findViewById(R.id.spinner_sort);

        db = FirebaseFirestore.getInstance();
        loadBowlingBalls();

        String[] brands = {"All", "Storm", "Brunswick", "Hammer", "Roto Grip", "Columbia 300", "Motiv", "Ebonite", "900Global", "DV8"};
        String[] performances = {"All", "Plastic / Spare Balls", "Entry Level Balls", "Mid Performance Balls", "Upper Mid-Performance Balls", "High-Performance Balls" };
        String[] sorts = {"Default", "A-Z", "Z-A", "Price Low-High", "Price High-Low"};

        spinnerBrand.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands));
        spinnerPerformance.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, performances));
        spinnerSort.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sorts));

        AdapterView.OnItemSelectedListener filterChangeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBrand = spinnerBrand.getSelectedItem().toString();
                selectedPerformance = spinnerPerformance.getSelectedItem().toString();
                selectedSort = spinnerSort.getSelectedItem().toString();
                adapter.applyFilters(selectedBrand, selectedPerformance, selectedSort, searchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerBrand.setOnItemSelectedListener(filterChangeListener);
        spinnerPerformance.setOnItemSelectedListener(filterChangeListener);
        spinnerSort.setOnItemSelectedListener(filterChangeListener);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.applyFilters(selectedBrand, selectedPerformance, selectedSort, newText);
                return true;
            }
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(BowlingBall.this, AboutApp.class);
            startActivity(intent);
        });

        ImageButton micButton = findViewById(R.id.mic_button);
        micButton.setOnClickListener(v -> startVoiceInput());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say brand or model name...");
        try {
            startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                searchView.setQuery(result.get(0), true);
            }
        }
    }

    private void loadBowlingBalls() {
        db.collection("bowling_balls").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            List<BowlingBallModel> newList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value) {
                BowlingBallModel ball = doc.toObject(BowlingBallModel.class);
                newList.add(ball);
            }

            adapter.updateData(newList);
        });
    }


    public static class BowlingBallModel {
        private String imageUrl, brand, model, coverstock, performance, description;
        private double price;

        public BowlingBallModel() {}

        public String getImageUrl() { return imageUrl; }
        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getCoverstock() { return coverstock; }
        public String getPerformance() { return performance; }
        public double getPrice() { return price; }
        public String getDescription() { return description; }
    }

    public static class BowlingBallAdapter extends RecyclerView.Adapter<BowlingBallAdapter.BallViewHolder> {

        private Context context;
        private List<BowlingBallModel> ballList;
        private List<BowlingBallModel> fullList;

        public BowlingBallAdapter(Context context, List<BowlingBallModel> ballList) {
            this.context = context;
            this.ballList = new ArrayList<>(ballList);
            this.fullList = new ArrayList<>(ballList);
        }

        @NonNull
        @Override
        public BallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_bowling_ball, parent, false);
            return new BallViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BallViewHolder holder, int position) {
            BowlingBallModel ball = ballList.get(position);
            holder.brand.setText(ball.getBrand());
            holder.model.setText(ball.getModel());
            holder.coverstock.setText("Coverstock: " + ball.getCoverstock());
            holder.performance.setText("Performance: " + ball.getPerformance());
            holder.price.setText("RM " + String.format("%.2f", ball.getPrice()));
            Glide.with(context).load(ball.getImageUrl()).into(holder.image);

            // Set OnClickListener for the entire card view
            holder.itemView.setOnClickListener(v -> showBallDetailDialog(ball));
        }

        @Override
        public int getItemCount() {
            return ballList.size();
        }

        // Method to show the detail dialog
        private void showBallDetailDialog(BowlingBallModel ball) {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_bowling_ball_detail);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }


            ImageView dialogImage = dialog.findViewById(R.id.dialog_ball_image);
            TextView dialogBrand = dialog.findViewById(R.id.dialog_ball_brand);
            TextView dialogModel = dialog.findViewById(R.id.dialog_ball_model);
            TextView dialogCoverstock = dialog.findViewById(R.id.dialog_ball_coverstock);
            TextView dialogPerformance = dialog.findViewById(R.id.dialog_ball_performance);
            TextView dialogPrice = dialog.findViewById(R.id.dialog_ball_price);
            TextView dialogDescription = dialog.findViewById(R.id.dialog_ball_description);
            Button closeButton = dialog.findViewById(R.id.dialog_close_button);

            // Populate the dialog views with ball data
            Glide.with(context).load(ball.getImageUrl()).into(dialogImage);
            dialogBrand.setText(ball.getBrand());
            dialogModel.setText(ball.getModel());
            dialogCoverstock.setText("Coverstock: " + ball.getCoverstock());
            dialogPerformance.setText("Performance: " + ball.getPerformance());
            dialogPrice.setText("RM " + String.format("%.2f", ball.getPrice()));
            dialogDescription.setText(ball.getDescription());

            closeButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }

        public void applyFilters(String brand, String performance, String sortOption, String searchQuery) {
            List<BowlingBallModel> filteredList = new ArrayList<>();

            for (BowlingBallModel ball : fullList) {
                boolean matchesBrand = brand.equals("All") || (ball.getBrand() != null && ball.getBrand().equalsIgnoreCase(brand));
                boolean matchesPerformance = performance.equals("All") || (ball.getPerformance() != null && ball.getPerformance().equalsIgnoreCase(performance));
                boolean matchesSearch = searchQuery == null || searchQuery.isEmpty()
                        || (ball.getBrand() != null && ball.getBrand().toLowerCase().contains(searchQuery.toLowerCase()))
                        || (ball.getModel() != null && ball.getModel().toLowerCase().contains(searchQuery.toLowerCase()))
                        || (ball.getPerformance() != null && ball.getPerformance().toLowerCase().contains(searchQuery.toLowerCase()))
                        || (ball.getCoverstock() != null && ball.getCoverstock().toLowerCase().contains(searchQuery.toLowerCase()));

                if (matchesBrand && matchesPerformance && matchesSearch) {
                    filteredList.add(ball);
                }
            }

            // Sorting
            if (!sortOption.equals("Default")) {
                switch (sortOption) {
                    case "A-Z":
                        filteredList.sort((a, b) -> a.getBrand().compareToIgnoreCase(b.getBrand()));
                        break;
                    case "Z-A":
                        filteredList.sort((a, b) -> b.getBrand().compareToIgnoreCase(a.getBrand()));
                        break;
                    case "Price Low-High":
                        filteredList.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                        break;
                    case "Price High-Low":
                        filteredList.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                        break;
                }
            }

            ballList.clear();
            ballList.addAll(filteredList);
            notifyDataSetChanged();
        }

        public void updateData(List<BowlingBallModel> newList) {
            ballList.clear();
            ballList.addAll(newList);
            fullList.clear();
            fullList.addAll(newList);
            notifyDataSetChanged();
        }

        public static class BallViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView brand, model, coverstock, performance, price;

            public BallViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.ball_image);
                brand = itemView.findViewById(R.id.ball_brand);
                model = itemView.findViewById(R.id.ball_model);
                coverstock = itemView.findViewById(R.id.ball_coverstock);
                performance = itemView.findViewById(R.id.ball_performance);
                price = itemView.findViewById(R.id.ball_price);
            }
        }
    }
}