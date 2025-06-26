package com.example.bowlmate;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText nameEdit, phoneEdit;
    private Button saveBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEdit = findViewById(R.id.editTextName);
        phoneEdit = findViewById(R.id.editTextPhone);
        saveBtn = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        loadUserData();

        saveBtn.setOnClickListener(v -> updateUserInfo());
    }

    private void loadUserData() {
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameEdit.setText(doc.getString("name"));
                        phoneEdit.setText(doc.getString("phone"));
                    }
                });
    }

    private void updateUserInfo() {
        String name = nameEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("phone", phone).get().addOnSuccessListener(snapshot -> {
            boolean phoneExists = snapshot.getDocuments().stream()
                    .anyMatch(doc -> !doc.getId().equals(user.getUid()));

            if (phoneExists) {
                Toast.makeText(this, "Phone number already in use", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", name);
                updates.put("phone", phone);

                db.collection("users").document(user.getUid())
                        .update(updates)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}