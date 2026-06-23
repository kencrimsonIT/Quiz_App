package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.quizzly.databinding.FragmentProfileEditingBinding;
import com.example.quizzly.viewmodel.AuthViewModel;

public class ProfileEditingFragment extends Fragment {

    private AuthViewModel authViewModel;

    private FragmentProfileEditingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileEditingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

                // Initialize ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        // Load current user profile into edit fields
        authViewModel.loadCurrentUserProfile();
        authViewModel.getDisplayNameLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.etDisplayName.setText(s);
        });
        authViewModel.getBirthdateLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.etBirthday.setText(s);
        });
        authViewModel.getPhoneLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.etPhone.setText(s);
        });
        // Email is not editable but we display it
        authViewModel.getEmailLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.etEmail.setText(s);
        });

        binding.btnSave.setOnClickListener(v -> {
            // Gather input data
            String displayName = binding.etDisplayName.getText().toString().trim();
            String birthday = binding.etBirthday.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            // Update profile via ViewModel
            authViewModel.updateCurrentUserProfile(displayName, birthday, phone);
            // Pop back after update (optional, could wait for callback)
            Navigation.findNavController(view).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
