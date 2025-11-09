package com.example.firestorewriteread;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/*
 A minimal Activity demonstrating:
 - saving a User document (auto-generated doc id when no auth)
 - loading the User document by id
 - callbacks: addOnSuccessListener/addOnFailureListener
 This keeps the UI simple to focus on Firestore write/read & callbacks.
*/
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS = "firestore_prefs";
    private static final String KEY_DOC_ID = "doc_id";

    private FirebaseFirestore db;
    private EditText etName;
    private EditText etEmail;
    private EditText etScore;
    private Button btnSave;
    private Button btnLoad;
    private TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etScore = findViewById(R.id.etScore);
        btnSave = findViewById(R.id.btnSave);
        btnLoad = findViewById(R.id.btnLoad);
        tvResult = findViewById(R.id.tvResult);

        btnSave.setOnClickListener(v -> saveUserFlow());

        btnLoad.setOnClickListener(v -> {
            String docId = getSavedDocId();
            if (docId != null && !docId.isEmpty()) {
                loadUserOnce(docId);
            } else {
                tvResult.setText("No stored document id. Save first.");
            }
        });
    }

    // Save user; if there's no saved doc id, create an auto-id doc and save the id locally.
    private void saveUserFlow() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        int score = 0;
        try {
            String s = etScore.getText().toString().trim();
            if (!s.isEmpty()) score = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            score = 0;
        }

        String docId = getSavedDocId();
        if (docId == null || docId.isEmpty()) {
            // no existing id, create a document with auto id
            DocumentReference ref = db.collection("users").document();
            docId = ref.getId();
            User user = new User(docId, name.isEmpty() ? "Guest" : name,
                    email.isEmpty() ? "guest@example.com" : email, score);
            ref.set(user)
               .addOnSuccessListener(aVoid -> {
                   Log.d(TAG, "Saved guest user with id: " + docId);
                   saveDocId(docId);
                   tvResult.setText("Saved user with id: " + docId);
               })
               .addOnFailureListener(e -> {
                   Log.e(TAG, "Save failed", e);
                   tvResult.setText("Save failed: " + e.getMessage());
               });
        } else {
            // id exists (guest from before or later auth); update that doc
            User user = new User(docId, name.isEmpty() ? "Guest" : name,
                    email.isEmpty() ? "guest@example.com" : email, score);
            db.collection("users").document(docId)
              .set(user)
              .addOnSuccessListener(aVoid -> {
                  Log.d(TAG, "Updated user " + docId);
                  tvResult.setText("Updated user " + docId);
              })
              .addOnFailureListener(e -> {
                  Log.e(TAG, "Update failed", e);
                  tvResult.setText("Update failed: " + e.getMessage());
              });
        }
    }

    private void loadUserOnce(String docId) {
        db.collection("users").document(docId)
          .get()
          .addOnSuccessListener(documentSnapshot -> {
              if (documentSnapshot.exists()) {
                  User u = documentSnapshot.toObject(User.class);
                  if (u != null) {
                      tvResult.setText(String.format("User: %s\nEmail: %s\nScore: %d",
                              u.getName(), u.getEmail(), u.getScore()));
                  } else {
                      tvResult.setText("Document exists but could not convert to User.");
                  }
              } else {
                  tvResult.setText("Document not found.");
              }
          })
          .addOnFailureListener(e -> {
              Log.e(TAG, "Read failed", e);
              tvResult.setText("Read failed: " + e.getMessage());
          });
    }

    private void saveDocId(String docId) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_DOC_ID, docId).apply();
    }

    private String getSavedDocId() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_DOC_ID, null);
    }
}