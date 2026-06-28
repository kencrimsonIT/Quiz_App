package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeFragment extends Fragment {

    private FirebaseFirestore db;
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
        tvTotalQuestions = view.findViewById(R.id.tvTotalQuestions);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);


        loadDashboardStats();
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
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