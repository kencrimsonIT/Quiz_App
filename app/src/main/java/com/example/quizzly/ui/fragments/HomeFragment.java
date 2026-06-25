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
import com.example.quizzly.databinding.FragmentHomeBinding;
import com.example.quizzly.viewmodel.AuthViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUserLiveData().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                String username = firebaseUser.getDisplayName();
                binding.tvUsername.setText(username);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_loginFragment);
            }
        });

        binding.navMenu.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_menuFragment);
        });
        binding.cardBiology.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", "biology");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_qizzFragment, bundle);
        });

        binding.cardChemistry.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", "chemistry");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_qizzFragment, bundle);
        });

        binding.cardMath.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", "maths");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_qizzFragment, bundle);
        });

        binding.cardPhysics.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", "physics");
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_qizzFragment, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void navigateToQuiz(View view, String subjectId) {
        Bundle bundle = new Bundle();
        bundle.putString("SUBJECT_ID", subjectId);
        Navigation.findNavController(view).navigate(R.id.qizzFragment, bundle);
    }
}
