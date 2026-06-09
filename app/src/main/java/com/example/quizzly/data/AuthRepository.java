package com.example.quizzly.data;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Đăng ký tài khoản mới, sau đó:
     * 1. Cập nhật displayName vào Firebase Auth profile
     * 2. Tạo document trong Firestore collection "users"
     */
    public Task<Void> register(String email, String password, String displayName) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    // Bước 1: Cập nhật displayName vào Auth profile
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build();

                    return user.updateProfile(profileUpdate);
                })
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    // Bước 2: Tạo document trong Firestore collection "users"
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", user.getUid());
                    userData.put("displayName", displayName);
                    userData.put("email", email);
                    userData.put("createdAt", com.google.firebase.Timestamp.now());

                    return db.collection("users")
                            .document(user.getUid())
                            .set(userData);
                });
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    public Task<Void> changePassword(String newPassword) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            return user.updatePassword(newPassword);
        }
        return Tasks.forException(new Exception("No user is currently logged in"));
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}