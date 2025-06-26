package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private TextView nameView, phoneView, emailView;
    private Button editProfileBtn, changePasswordBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameView = findViewById(R.id.textViewName);
        phoneView = findViewById(R.id.textViewPhone);
        emailView = findViewById(R.id.textViewEmail);
        editProfileBtn = findViewById(R.id.btnEditProfile);
        changePasswordBtn = findViewById(R.id.btnChangePassword);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        loadUserData();

        editProfileBtn.setOnClickListener(v ->
                startActivity(new Intent(Profile.this, EditProfile.class)));

        changePasswordBtn.setOnClickListener(v -> promptPasswordChange());
    }

    private void loadUserData() {
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String email = user.getEmail();

                        Log.d("PROFILE_DEBUG", "Fetched name: " + name + ", phone: " + phone);

                        nameView.setText(name != null ? name : "No name found");
                        phoneView.setText(phone != null ? phone : "No phone found");
                        emailView.setText(email != null ? email : "No email found");
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    Log.e("PROFILE_DEBUG", "Error: ", e);
                });
    }

    private void promptPasswordChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter new password (min 6 chars)");
        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
                return;
            }

            user.updatePassword(newPassword)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}