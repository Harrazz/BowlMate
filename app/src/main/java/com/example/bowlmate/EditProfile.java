package com.example.bowlmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfile extends AppCompatActivity {

    private EditText editName, editPhone;
    private Button buttonSave;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfile.this, AboutApp.class);
            startActivity(intent);
        });

        editName = findViewById(R.id.editTextName);
        editPhone = findViewById(R.id.editTextPhone);
        buttonSave = findViewById(R.id.buttonSave);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    editName.setText(documentSnapshot.getString("name"));
                    editPhone.setText(documentSnapshot.getString("phone"));
                }
            });

            buttonSave.setOnClickListener(v -> {
                String newName = editName.getText().toString().trim();
                String newPhone = editPhone.getText().toString().trim();

                if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
                    Toast.makeText(EditProfile.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users")
                        .whereEqualTo("phone", newPhone)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            boolean isUsedByOther = queryDocumentSnapshots.getDocuments().stream()
                                    .anyMatch(doc -> !doc.getId().equals(currentUser.getUid()));

                            if (isUsedByOther) {
                                Toast.makeText(EditProfile.this, "Phone number is already used by another user", Toast.LENGTH_LONG).show();
                            } else {
                                // Step 2: Ask for confirmation
                                new AlertDialog.Builder(EditProfile.this)
                                        .setTitle("Confirm Update")
                                        .setMessage("Are you sure you want to update your profile?")
                                        .setPositiveButton("Update", (dialog, which) -> {
                                            db.collection("users").document(currentUser.getUid())
                                                    .update("name", newName, "phone", newPhone)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(EditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditProfile.this, "Error checking phone number", Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }
}