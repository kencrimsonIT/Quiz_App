package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.databinding.FragmentResetPasswordBinding;
import com.example.quizzly.viewmodel.AuthViewModel;

public class ResetPasswordFragment extends Fragment {

    private FragmentResetPasswordBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnCreatePassword.setOnClickListener(v -> {
            String newPassword = binding.etNewPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmNewPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.resetPassword(newPassword);
        });

        // Observability
        authViewModel.getPasswordResetCompletedLiveData().observe(getViewLifecycleOwner(), completed -> {
            if (completed) {
                Toast.makeText(getContext(), "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.action_resetPasswordFragment_to_loginFragment);
            }
        });

        authViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCreatePassword.setEnabled(!isLoading);
            binding.etNewPassword.setEnabled(!isLoading);
            binding.etConfirmNewPassword.setEnabled(!isLoading);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
