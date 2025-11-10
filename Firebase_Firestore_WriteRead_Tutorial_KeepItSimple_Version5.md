# Firestore — Write and Read Data (Keep It Simple)

Goal
- Learn Cloud Firestore in a simple and clear way.
- Explain the move from SQL to NoSQL (document database).
- Show how a Java POJO maps to a Firestore document.
- Emphasize writing data (set) and reading data (get) and why operations are asynchronous (use callbacks).
- Do NOT include createdAt in main examples — timestamps and realtime listeners are optional bonus topics.

Audience
- Students who know SQL basics (tables, rows, joins) and are starting with Firestore.

---

## 1. SQL vs NoSQL — short and simple

SQL (relational)
- Tables, rows, columns.
- Relations via JOIN.
- Rigid schema.

NoSQL (document / Firestore)
- Collections contain documents.
- Each document is a set of key/value pairs (a JSON-like map).
- No server-side JOINs; design data for the reads your app needs.
- A SQL row becomes a JSON document inside a collection.

Summary: in a document database each SQL row becomes a document (a JSON object) inside a collection.

---

## 2. Object → JSON / Map / POJO

Example SQL row in `users` table:
| id  | name  | email           | score |
|-----|-------|-----------------|-------|
| u1  | Alice | alice@mail.com  | 42    |

The same row as a NoSQL JSON document:
```json
{
  "uid": "u1",
  "name": "Alice",
  "email": "alice@mail.com",
  "score": 42
}
```

In Java you can use `Map<String,Object>` or (recommended) a POJO:
- POJO = simple class with a public no-arg constructor and getters/setters.
- Firestore automatically serializes a POJO on `.set()` and deserializes on `.toObject(YourClass.class)`.

---

## 3. Several examples — SQL row → JSON document

Example A — `users` table row → `/users/u1` document

SQL row:
| id  | name  | email           | score |
|-----|-------|-----------------|-------|
| u1  | Alice | alice@mail.com  | 42    |

Firestore document (`/users/u1`):
```json
{
  "uid": "u1",
  "name": "Alice",
  "email": "alice@mail.com",
  "score": 42
}
```

Example B — `orders` table row → `/orders/o1001` document

SQL row:
| order_id | user_id | total | status   |
|----------|---------|-------|----------|
| o1001    | u1      | 59.99 | shipped  |

Firestore document (`/orders/o1001`):
```json
{
  "order_id": "o1001",
  "user_id": "u1",
  "total": 59.99,
  "status": "shipped"
}
```

Example C — `products` table row → `/products/p200` document

SQL row:
| sku   | name        | price |
|-------|-------------|-------|
| p200  | Water Bottle| 12.5  |

Firestore document (`/products/p200`):
```json
{
  "sku": "p200",
  "name": "Water Bottle",
  "price": 12.5
}
```

Fields can be strings, numbers, booleans, arrays, or nested maps.

---

## 4. What is a Collection, Document and DocumentReference — simple

- Collection = a group of documents (like a Java collection: List or ArrayList).
- Document = one object inside the collection (an item in the list).
- DocumentReference = the address or pointer to that document.

Analogy:
- A Java `List<User>` holds many `User` objects. Firestore’s `Collection` holds many documents.
- A DocumentReference is like the address or variable that points to a specific object — e.g. `/users/u1`.

Why DocumentReference matters:
- You need it to read, write, update or delete a specific document.
- It gives you the document ID (the last segment of the path) so you can refer to the same document later.
- If you don't have an auth uid, you can create a document with an auto-generated ID and keep the DocumentReference to find the same document later.

Create a DocumentReference:
```java
// Generates an automatic id
DocumentReference ref = db.collection("users").document();
String id = ref.getId(); // save this id if you want to refer later
```

Or specify an ID:
```java
DocumentReference ref = db.collection("users").document("u1");
```

Use add() to get a generated DocumentReference in the callback:
```java
db.collection("users").add(user)
  .addOnSuccessListener(docRef -> {
      String generatedId = docRef.getId();
  });
```

---

## 5. Minimal POJO — no createdAt (Keep It Simple)

```java
// User.java
package com.example.app.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private int score;

    // Required public no-arg constructor
    public User() {}

    public User(String uid, String name, String email, int score) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.score = score;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
```

---

## 6. Write — saving a document in Firestore

Principle: operations do not block the UI. They are asynchronous — a callback runs when the write succeeds or fails.

Example: save a user with an auto-generated DocumentReference (guest flow)
```java
FirebaseFirestore db = FirebaseFirestore.getInstance();

public void saveGuestUser() {
    DocumentReference ref = db.collection("users").document(); // auto id
    String docId = ref.getId();

    User user = new User(docId, "Guest", "guest@example.com", 0);

    ref.set(user)
       .addOnSuccessListener(aVoid -> {
           // Write succeeded
           Log.d("Firestore", "Saved guest user with id: " + docId);
           // Optionally save docId locally to use later
       })
       .addOnFailureListener(e -> {
           // Write failed
           Log.e("Firestore", "Save failed", e);
       });
}
```

Example: save a user under FirebaseAuth uid (if authenticated)
```java
FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
if (current != null) {
    String uid = current.getUid();
    User user = new User(uid, "Alice", "alice@mail.com", 42);
    db.collection("users").document(uid).set(user)
      .addOnSuccessListener(aVoid -> Log.d("Firestore","Saved user " + uid))
      .addOnFailureListener(e -> Log.e("Firestore","Save failed", e));
}
```

---

## 7. Read — get a single document

Read once (single snapshot):
```java
public void loadUserOnce(String docId) {
    db.collection("users").document(docId)
      .get()
      .addOnSuccessListener(documentSnapshot -> {
          if (documentSnapshot.exists()) {
              User u = documentSnapshot.toObject(User.class);
              // Use u.getName(), u.getEmail(), u.getScore()
          } else {
              // Document not found
          }
      })
      .addOnFailureListener(e -> {
          // Read failed (network, permissions, ...)
      });
}
```

Important:
- `.get()` returns a `Task`; do not block the UI waiting for it.
- Update UI inside the callback.

---

## 8. What to do if there is no Authentication (no uid)

- If not authenticated, `FirebaseAuth.getInstance().getCurrentUser()` returns `null` — there is no auth uid.
- Simple options:
  1. Use an auto-generated DocumentReference (`document()` or `add()`), then save the generated id locally (SharedPreferences) if you want to refer to the same document later.
  2. Generate your own id (for example `UUID.randomUUID().toString()`).
  3. Use Anonymous Authentication if you want a stable uid provided by Firebase without user sign-up.

Example of creating an auto-id and storing it locally:
```java
DocumentReference ref = db.collection("users").document();
String docId = ref.getId();
// Save docId to SharedPreferences if you want to keep it across app restarts
ref.set(user);
```

---

## 9. Callbacks — what they are and why they are required (detailed)

What is a callback?
- A callback is a function or method you pass to another function so it can be called later when an asynchronous operation finishes.
- In Firestore, network and disk operations are performed in the background. You register callbacks to handle the result once the operation completes.

Why do we need callbacks?
- Network and I/O can take time. Blocking the UI thread during these operations would freeze the app.
- Callbacks let the app stay responsive and react when the operation succeeds or fails.

Common listeners (callbacks) in Firestore SDK:
- `addOnSuccessListener(...)` — invoked when the Task completes successfully.
- `addOnFailureListener(...)` — invoked when the Task fails with an exception.
- `addOnCompleteListener(...)` — invoked when the Task finishes (either success or failure).

Do callbacks run on the UI thread?
- By default in Android the listeners (`addOnSuccessListener`, `addOnFailureListener`) run on the main (UI) thread. That makes it safe to update UI components inside the callbacks.

Example: write then read after success
```java
db.collection("users").document(uid).set(user)
  .addOnSuccessListener(aVoid -> {
      // The write completed (locally and queued for server sync)
      db.collection("users").document(uid).get()
        .addOnSuccessListener(doc -> {
            // Use result
        })
        .addOnFailureListener(e -> { /* handle read error */ });
  })
  .addOnFailureListener(e -> {
      // Handle write error
  });
```

Avoiding deeply nested callbacks ("callback hell")
- Prefer returning the `Task` from helper methods and attach listeners at the call site.
- Example helper that returns the Task:
```java
public Task<Void> saveUserTask(User user) {
    return db.collection("users").document(user.getUid()).set(user);
}

// In UI code:
saveUserTask(user)
  .addOnSuccessListener(aVoid -> { /* success */ })
  .addOnFailureListener(e -> { /* failure */ });
```

Callback interface pattern (decouple data layer from UI)
```java
public interface FirestoreCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}

public void loadUserOnce(String uid, FirestoreCallback<User> cb) {
    db.collection("users").document(uid).get()
      .addOnSuccessListener(doc -> {
          if (doc.exists()) cb.onSuccess(doc.toObject(User.class));
          else cb.onFailure(new Exception("Not found"));
      })
      .addOnFailureListener(cb::onFailure);
}
```

Offline behavior and callbacks
- Firestore mobile SDK caches data locally. A write may return success locally even if not yet persisted to the server.
- If your logic needs confirmation the server accepted the write, that is an advanced topic; for most lessons, treat `addOnSuccessListener` as confirmation the write was accepted locally and queued for sync.

Cancelling and listeners
- Most simple `get()`/`set()` Tasks are not cancellable. For realtime snapshot listeners use `ListenerRegistration.remove()` to stop listening.

Practical tips
- Always update UI inside callbacks.
- Check that the Activity/Fragment is still alive before updating UI from a callback.
- Handle failures: show error messages or allow retry.
- Return `Task` objects from data layer methods where sensible to allow chaining.

---

## 10. Short summary — Keep It Simple

- SQL row → Firestore document: each table row becomes a JSON document in a collection.
- Use a POJO (no-arg constructor + getters/setters) — Firestore serializes/deserializes automatically.
- DocumentReference = the address of a document; use it to get/set/update/delete a specific document.
- If there is no auth uid, use auto-generated document IDs or create your own ID; store it locally if you need to access the same document later.
- Firestore calls are asynchronous. Use `addOnSuccessListener`, `addOnFailureListener` or `addOnCompleteListener` to handle results. Do not block the UI thread.
- Timestamps and realtime listeners are optional bonus topics.

---

### Quick cheat sheet
- Create document with auto id: `DocumentReference ref = db.collection("users").document(); String id = ref.getId();`
- Write: `ref.set(obj).addOnSuccessListener(...).addOnFailureListener(...)`
- Read: `db.collection("users").document(id).get().addOnSuccessListener(...)`
- Return a `Task` from helper methods to allow chaining and simpler code structure.
- If no auth uid: use auto id, `add()`, or anonymous auth.

---

If you want, I can now prepare a minimal Android sample project (Java + XML + Gradle) that implements Save and Load using the POJO approach and auto-generated document IDs, ready to copy into Android Studio.