package com.example.bowlmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class RegisterActivity extends AppCompatActivity {

    EditText e1, e2, nameInput, phoneInput;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, AboutApp.class);
            startActivity(intent);
        });

        nameInput = findViewById(R.id.editTextName);
        phoneInput = findViewById(R.id.editTextPhone);
        e1 = findViewById(R.id.editText);
        e2 = findViewById(R.id.editText2);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // initialize Firestore

        findViewById(R.id.textView7).setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    public void createUser(View v) {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔍 Step 1: Check if phone number already exists
        db.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (!result.isEmpty()) {
                            // Phone number already in use
                            Toast.makeText(getApplicationContext(), "Phone number already used", Toast.LENGTH_SHORT).show();
                        } else {
                            // 🔐 Step 2: Proceed with registration
                            registerWithEmail(name, phone, email, password);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to check phone number", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerWithEmail(String name, String phone, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            currentUser.sendEmailVerification().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Save name and phone temporarily using SharedPreferences
                                    SharedPreferences prefs = getSharedPreferences("UserTempData", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("name", name);
                                    editor.putString("phone", phone);
                                    editor.apply();

                                    Toast.makeText(getApplicationContext(),
                                            "Verification email sent. Please verify and login.",
                                            Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    finish();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Failed to send verification email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getApplicationContext(), "Email already exists", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to create user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}