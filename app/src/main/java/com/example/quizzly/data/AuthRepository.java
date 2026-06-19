package com.example.quizzly.data;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.google.firebase.firestore.FieldValue;
import com.example.quizzly.utils.EmailJSService;

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

    public Task<AuthResult> firebaseAuthWithGoogle(String idToken) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        return signInWithCredential(credential, "Google");
    }

    public Task<AuthResult> firebaseAuthWithFacebook(com.facebook.AccessToken token) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token.getToken());
        return signInWithCredential(credential, "Facebook");
    }

    private Task<AuthResult> signInWithCredential(com.google.firebase.auth.AuthCredential credential, String provider) {
        return firebaseAuth.signInWithCredential(credential)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        throw new Exception("User is null after " + provider + " Sign-In");
                    }

                    return db.collection("users").document(user.getUid()).get()
                            .continueWithTask(getTask -> {
                                if (getTask.isSuccessful() && !getTask.getResult().exists()) {
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("uid", user.getUid());
                                    userData.put("displayName", user.getDisplayName());
                                    userData.put("email", user.getEmail());
                                    userData.put("createdAt", com.google.firebase.Timestamp.now());

                                    return db.collection("users")
                                            .document(user.getUid())
                                            .set(userData)
                                            .continueWith(setTask -> task.getResult());
                                }
                                return Tasks.forResult(task.getResult());
                            });
                });
    }

    public Task<Void> register(String email, String password, String displayName) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

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

    public Task<Void> sendPasswordResetOtp(String email) {
        String otp = generateOtp();
        long expirationMillis = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes
        Timestamp expiredAt = new Timestamp(expirationMillis / 1000, 0);

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("expiredAt", expiredAt);
        otpData.put("createdAt", FieldValue.serverTimestamp());
        
        return db.collection("otps").document(email).set(otpData).continueWithTask(task -> {
            if (task.isSuccessful()) {
                EmailJSService.sendEmail(email, otp);
            }
            return task;
        });
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public Task<Boolean> verifyOtp(String email, String otp) {
        return db.collection("otps").document(email).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String savedOtp = task.getResult().getString("otp");
                if (otp.equals(savedOtp)) {
                    return true;
                }
            }
            return false;
        });
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