package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.databinding.FragmentMenuBinding;
import com.example.quizzly.utils.ThemeManager;
import com.example.quizzly.viewmodel.AuthViewModel;

public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Dark mode switch
        // Initialize switch state based on current theme
        binding.switchDarkMode.setChecked(ThemeManager.isDarkMode(requireContext()));

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(requireContext(), isChecked);
            // Recreate the activity to apply the new theme
            if (getActivity() != null) {
                getActivity().recreate();
            }
        });

        binding.llLogout.setOnClickListener(v -> {
            authViewModel.logout();
            Navigation.findNavController(view).navigate(R.id.action_menuFragment_to_loginFragment);
        });

        binding.navHome.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_menuFragment_to_homeFragment);
        });

        binding.navQuiz.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_menuFragment_to_subjectListFragment);
        });

        binding.llLanguage.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_menuFragment_to_languageSetting);
        });

        binding.llProfile.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_menuFragment_to_profileFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
