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

import com.example.quizzly.R;
import com.example.quizzly.databinding.FragmentProfileBinding;
import com.example.quizzly.viewmodel.AuthViewModel;

public class ProfileFragment extends Fragment {

    private AuthViewModel authViewModel;

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        binding.btnEdit.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_profileEditingFragment);
        });

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        // Load current user profile
        authViewModel.loadCurrentUserProfile();
        // Observe LiveData fields and bind to UI
        authViewModel.getDisplayNameLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvUsername.setText(s);
        });
        authViewModel.getBirthdateLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvBirthday.setText(s);
        });
        authViewModel.getEmailLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvEmail.setText(s);
        });
        authViewModel.getPhoneLiveData().observe(getViewLifecycleOwner(), s -> {
            if (s != null) binding.tvPhone.setText(s);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
