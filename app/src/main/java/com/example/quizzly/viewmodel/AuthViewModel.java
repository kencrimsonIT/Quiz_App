package com.example.quizzly.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizzly.data.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<String> userRoleLiveData = new MutableLiveData<>();
    public LiveData<String> getUserRoleLiveData() { return userRoleLiveData; }

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetSentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpVerifiedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpResentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetCompletedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Object>> userProfileLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> displayNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> birthdateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> phoneLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    public LiveData<String> getDisplayNameLiveData() { return displayNameLiveData; }
    public LiveData<String> getBirthdateLiveData() { return birthdateLiveData; }
    public LiveData<String> getPhoneLiveData() { return phoneLiveData; }
    public LiveData<String> getEmailLiveData() { return emailLiveData; }
    private final MutableLiveData<Boolean> profileUpdatedLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = new AuthRepository();
        userLiveData.setValue(authRepository.getCurrentUser());
    }

    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<Boolean> getPasswordResetSentLiveData() { return passwordResetSentLiveData; }
    public LiveData<Boolean> getOtpVerifiedLiveData() { return otpVerifiedLiveData; }
    public LiveData<Boolean> getOtpResentLiveData() { return otpResentLiveData; }
    public LiveData<Boolean> getPasswordResetCompletedLiveData() { return passwordResetCompletedLiveData; }
    public LiveData<Map<String, Object>> getUserProfileLiveData() { return userProfileLiveData; }
    public LiveData<Boolean> getProfileUpdatedLiveData() { return profileUpdatedLiveData; }

    public void login(String email, String password) {
        loadingLiveData.setValue(true);
        authRepository.login(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
                checkUserRole(authRepository.getCurrentUser().getUid()); // Lấy tiếp role
            } else {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage() : "Đăng nhập thất bại");
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        loadingLiveData.setValue(true);
        authRepository.firebaseAuthWithGoogle(idToken).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
                checkUserRole(authRepository.getCurrentUser().getUid());
            } else {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage() : "Đăng nhập Google thất bại");
            }
        });
    }

    public void loginWithFacebook(com.facebook.AccessToken token) {
        loadingLiveData.setValue(true);
        authRepository.firebaseAuthWithFacebook(token).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
                checkUserRole(authRepository.getCurrentUser().getUid());
            } else {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage() : "Đăng nhập Facebook thất bại");
            }
        });
    }

    public void register(String email, String password, String displayName) {
        loadingLiveData.setValue(true);
        authRepository.register(email, password, displayName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
                checkUserRole(authRepository.getCurrentUser().getUid());
            } else {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage() : "Đăng ký thất bại");
            }
        });
    }

    public void checkUserRole(String uid) {
        authRepository.getUserProfile(uid).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false); // Chốt tắt loading ở bước cuối cùng này
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                if (role == null || role.isEmpty()) {
                    role = "user";
                }
                userRoleLiveData.setValue(role);
            } else {
                errorLiveData.setValue("Không thể tải thông tin phân quyền");
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
                        ? task.getException().getMessage() : "Gửi email thất bại");
            }
        });
    }

    public void logout() {
        authRepository.logout();
        userLiveData.setValue(null);
        resetRoleState();
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
                        ? task.getException().getMessage() : "Không thể gửi lại OTP");
            }
        });
    }

    public void resetPassword(String email, String otp, String newPassword) {
        loadingLiveData.setValue(true);
        authRepository.changePassword(email, otp, newPassword).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                passwordResetCompletedLiveData.setValue(true);
            } else {
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage() : "Đặt lại mật khẩu thất bại");
            }
        });
    }

    public void loadUserProfile(String uid) {
        loadingLiveData.setValue(true);
        authRepository.getUserProfile(uid).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Object> data = task.getResult().getData();
                userProfileLiveData.setValue(data);
                if (data != null) {
                    displayNameLiveData.setValue((String) data.getOrDefault("displayName", ""));
                    birthdateLiveData.setValue((String) data.getOrDefault("birthdate", ""));
                    phoneLiveData.setValue((String) data.getOrDefault("phone", ""));
                    emailLiveData.setValue((String) data.getOrDefault("email", ""));
                }
            } else {
                errorLiveData.setValue("Không thể tải thông tin người dùng");
            }
        });
    }

    public void loadCurrentUserProfile() {
        String uid = getCurrentUserUid();
        if (uid != null) {
            loadUserProfile(uid);
        } else {
            errorLiveData.setValue("User not logged in");
        }
    }

    public void updateCurrentUserProfile(String displayName, String birthdate, String phone) {
        String uid = getCurrentUserUid();
        if (uid != null) {
            updateUserProfile(uid, displayName, birthdate, phone);
        } else {
            errorLiveData.setValue("User not logged in");
        }
    }

    public void updateUserProfile(String uid, String displayName, String birthdate, String phone) {
        loadingLiveData.setValue(true);
        authRepository.updateUserProfile(uid, displayName, birthdate, phone).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()) {
                profileUpdatedLiveData.setValue(true);
                loadUserProfile(uid);
            } else {
                errorLiveData.setValue(task.getException() != null
                        ? task.getException().getMessage() : "Cập nhật thông tin thất bại");
            }
        });
    }

    public String getCurrentUserUid() {
        FirebaseUser user = userLiveData.getValue();
        return user != null ? user.getUid() : null;
    }

    public void resetRoleState() {
        userRoleLiveData.setValue(null);
    }
}