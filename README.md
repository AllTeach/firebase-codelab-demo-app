# Firestore Write/Read Sample (Keep It Simple)

This repository contains a minimal Android sample demonstrating Firestore write and read operations
with a simple POJO (no createdAt by default). It is designed for teaching students who are familiar
with SQL and shows how SQL rows map to Firestore documents.

What is included
- Simple Android app (Java) with Save / Load UI
- Uses Firestore: writes a User POJO and reads it back using document id
- Stores generated document id locally for guest flow
- Two student-facing Markdown tutorials:
  - Firebase_Firestore_WriteRead_Tutorial_KeepItSimple_EN.md
  - Firebase_Firestore_WriteRead_Tutorial_KeepItSimple_HE.md

Important setup steps
1. Create a Firebase project at https://console.firebase.google.com/
2. Add an Android app in the Firebase console and download the `google-services.json` file.
   Put `google-services.json` into `app/`.
3. In the Firebase console, enable Firestore in test mode for learning (do NOT leave test mode in production).
4. Open the project in Android Studio and sync Gradle.
5. Run the app on a device or emulator.

If you want, you can change the code to use `FirebaseAuth` for an auth-based `/users/{uid}` flow.

License
- Use for teaching and examples.