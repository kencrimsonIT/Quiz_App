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
    private final MutableLiveData<Boolean> otpVerifiedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpResentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetCompletedLiveData = new MutableLiveData<>();

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

    public LiveData<Boolean> getOtpVerifiedLiveData() {
        return otpVerifiedLiveData;
    }

    public LiveData<Boolean> getOtpResentLiveData() {
        return otpResentLiveData;
    }

    public LiveData<Boolean> getPasswordResetCompletedLiveData() {
        return passwordResetCompletedLiveData;
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

    public void loginWithGoogle(String idToken) {
        loadingLiveData.setValue(true);
        authRepository.firebaseAuthWithGoogle(idToken).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
            } else {
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage()
                        : "Đăng nhập Google thất bại");
            }
        });
    }

    public void loginWithFacebook(com.facebook.AccessToken token) {
        loadingLiveData.setValue(true);
        authRepository.firebaseAuthWithFacebook(token).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
            } else {
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage()
                        : "Đăng nhập Facebook thất bại");
            }
        });
    }

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

    public void sendPasswordResetOtp(String email) {
        loadingLiveData.setValue(true);
        authRepository.sendPasswordResetOtp(email).addOnCompleteListener(task -> {
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

    public void verifyOtp(String email, String otp) {
        loadingLiveData.setValue(true);
        authRepository.verifyOtp(email, otp).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful() && task.getResult() != null && task.getResult()) {
                otpVerifiedLiveData.setValue(true);
            } else {
                errorLiveData.setValue("Mã OTP không hợp lệ");
            }
        });
    }

    public void resendOtp(String email) {
        loadingLiveData.setValue(true);
        authRepository.sendPasswordResetOtp(email).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                otpResentLiveData.setValue(true);
            } else {
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage()
                        : "Không thể gửi lại OTP");
            }
        });
    }

    public void resetPassword(String newPassword) {
        loadingLiveData.setValue(true);
        new android.os.Handler().postDelayed(() -> {
            loadingLiveData.setValue(false);
            passwordResetCompletedLiveData.setValue(true);
        }, 1500);
    }
}