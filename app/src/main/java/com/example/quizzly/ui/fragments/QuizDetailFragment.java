package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.quizzly.R;

public class QuizDetailFragment extends Fragment {

    public QuizDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý nút Back
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Quay lại màn hình trước đó
                NavController navController = Navigation.findNavController(v);
                navController.navigateUp();
            });
        }

        // Xử lý nút "Bắt đầu"
        View btnStart = view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                // Chuyển sang màn hình làm bài Quiz (QizzFragment)
                // Lưu ý: ID action này có thể cần được thêm vào nav_graph.xml nếu bạn muốn truyền tham số
                NavController navController = Navigation.findNavController(v);
                
                // Mặc định chuyển thẳng đến màn hình làm bài bằng ID của destination
                try {
                    navController.navigate(R.id.qizzFragment);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}