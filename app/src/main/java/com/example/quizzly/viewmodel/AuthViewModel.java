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
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage()
                        : "Đăng nhập thất bại");
            }
        });
    }

    /**
     * Đăng ký tài khoản mới với displayName
     * AuthRepository sẽ tự động:
     * 1. Tạo tài khoản Firebase Auth
     * 2. Cập nhật displayName vào Auth profile
     * 3. Tạo document trong Firestore collection "users"
     */
    public void register(String email, String password, String displayName) {
        loadingLiveData.setValue(true);
        authRepository.register(email, password, displayName).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
            } else {
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage()
                        : "Đăng ký thất bại");
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
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage()
                        : "Gửi email thất bại");
            }
        });
    }

    public void logout() {
        authRepository.logout();
        userLiveData.setValue(null);
    }
}