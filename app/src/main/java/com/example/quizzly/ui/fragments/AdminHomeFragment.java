package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.viewmodel.AuthViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeFragment extends Fragment {

    private FirebaseFirestore db;
    private AuthViewModel authViewModel;
    private TextView tvTotalQuestions, tvTotalUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        tvTotalQuestions = view.findViewById(R.id.tvTotalQuestions);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);

        loadDashboardStats();
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        view.findViewById(R.id.llLogout).setOnClickListener(v -> {
            authViewModel.logout();
            Navigation.findNavController(view).navigate(R.id.action_adminFragment_to_loginFragment);
        });
    }

    private void loadDashboardStats() {

        db.collection("questions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int count = task.getResult().size();
                        tvTotalQuestions.setText(String.valueOf(count));
                    } else {
                        tvTotalQuestions.setText("--");
                    }
                });

        db.collection("users").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int count = task.getResult().size();
                        tvTotalUsers.setText(String.valueOf(count));
                    } else {
                        tvTotalUsers.setText("--");
                    }
                });
    }
}