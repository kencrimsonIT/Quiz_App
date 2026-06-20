package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.databinding.FragmentOtpVerifyBinding;
import com.example.quizzly.viewmodel.AuthViewModel;

public class OtpVerifyFragment extends Fragment {

    private FragmentOtpVerifyBinding binding;
    private AuthViewModel authViewModel;
    private String email = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        if (!email.isEmpty()) {
            binding.tvSubtitle.setText("Hãy nhập mã OTP chúng tôi vừa gửi qua email của bạn.");
        }

        setupOtpInputFields();

        binding.btnVerify.setOnClickListener(v -> {
            String otpCode = getOtpCode();
            if (otpCode.length() < 6) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.verifyOtp(email, otpCode);
        });

        binding.tvResendLink.setOnClickListener(v -> {
            authViewModel.resendOtp(email);
        });

        // LiveData Observability
        authViewModel.getOtpVerifiedLiveData().observe(getViewLifecycleOwner(), verified -> {
            if (verified) {
                Toast.makeText(getContext(), "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
                Bundle args = new Bundle();
                args.putString("email", email);
                args.putString("otp", getOtpCode());
                Navigation.findNavController(view).navigate(R.id.action_otpVerifyFragment_to_resetPasswordFragment, args);
            }
        });

        authViewModel.getOtpResentLiveData().observe(getViewLifecycleOwner(), resent -> {
            if (resent) {
                Toast.makeText(getContext(), "Mã OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
                clearOtpFields();
            }
        });

        authViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnVerify.setEnabled(!isLoading);
            enableOtpFields(!isLoading);
        });
    }

    private void setupOtpInputFields() {
        EditText[] editTexts = {
                binding.etDigit1,
                binding.etDigit2,
                binding.etDigit3,
                binding.etDigit4,
                binding.etDigit5,
                binding.etDigit6,
        };

        binding.etDigit1.requestFocus();

        for (int i = 0; i < editTexts.length; i++) {
            final int index = i;

            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String text = s.toString();
                    editTexts[index].setActivated(!text.isEmpty());
                    if (text.length() == 1) {
                        if (index < editTexts.length - 1) {
                            editTexts[index + 1].requestFocus();
                        }
                    }
                }
            });

            editTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (editTexts[index].getText().toString().isEmpty()) {
                        if (index > 0) {
                            editTexts[index - 1].requestFocus();
                            editTexts[index - 1].setText("");
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }

    private String getOtpCode() {
        return binding.etDigit1.getText().toString().trim() +
                binding.etDigit2.getText().toString().trim() +
                binding.etDigit3.getText().toString().trim() +
                binding.etDigit4.getText().toString().trim() +
                binding.etDigit5.getText().toString().trim() +
                binding.etDigit6.getText().toString().trim();
    }

    private void clearOtpFields() {
        binding.etDigit1.setText("");
        binding.etDigit2.setText("");
        binding.etDigit3.setText("");
        binding.etDigit4.setText("");
        binding.etDigit5.setText("");
        binding.etDigit6.setText("");
        binding.etDigit1.requestFocus();
    }

    private void enableOtpFields(boolean enabled) {
        binding.etDigit1.setEnabled(enabled);
        binding.etDigit2.setEnabled(enabled);
        binding.etDigit3.setEnabled(enabled);
        binding.etDigit4.setEnabled(enabled);
        binding.etDigit5.setEnabled(enabled);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
