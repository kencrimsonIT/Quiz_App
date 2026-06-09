package com.example.quizzly.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizzly.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetSentLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        userLiveData.setValue(authRepository.getCurrentUser());
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getPasswordResetSentLiveData() {
        return passwordResetSentLiveData;
    }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
            } else {
                errorLiveData.setValue(task.getException() != null ? task.getException().getMessage() : "Login failed");
            }
        });
    }

    public void register(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.register(email, password).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
            } else {
                errorLiveData.setValue(task.getException() != null ? task.getException().getMessage() : "Registration failed");
            }
        });
    }

    public void sendPasswordResetEmail(String email) {
        loadingLiveData.setValue(true);
        authRepository.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                passwordResetSentLiveData.setValue(true);
            } else {
                errorLiveData.setValue(task.getException() != null ? task.getException().getMessage() : "Failed to send reset email");
            }
        });
    }

    public void logout() {
        authRepository.logout();
        userLiveData.setValue(null);
    }
}
