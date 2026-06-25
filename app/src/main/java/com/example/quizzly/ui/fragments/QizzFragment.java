package com.example.quizzly.ui.fragments;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.databinding.FragmentQizBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QizzFragment extends Fragment {

    private FragmentQizBinding binding;
    private FirebaseFirestore db;
    private List<QueryDocumentSnapshot> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswerIndex = -1;
    private int score = 0;
    private boolean answered = false;
    private boolean hasWrongAnswer = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        binding.btnExit.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        String subjectId = "";
        if (getArguments() != null) {
            subjectId = getArguments().getString("SUBJECT_ID", "");
        }

        loadQuestionsFromFirestore(subjectId);

        binding.btnOptionA.setOnClickListener(v -> checkAnswer(0, binding.btnOptionA));
        binding.btnOptionB.setOnClickListener(v -> checkAnswer(1, binding.btnOptionB));
        binding.btnOptionC.setOnClickListener(v -> checkAnswer(2, binding.btnOptionC));
        binding.btnOptionD.setOnClickListener(v -> checkAnswer(3, binding.btnOptionD));

        binding.btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex);
            } else {
                Toast.makeText(getContext(),
                        "Bạn đạt " + score + "/" + questionList.size() + " điểm.",
                        Toast.LENGTH_LONG).show();

                Navigation.findNavController(view).popBackStack();
            }
        });
    }

    private void loadQuestionsFromFirestore(String subjectId) {

        db.collection("questions")
                .whereEqualTo("subid", subjectId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {

                        questionList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            questionList.add(document);
                        }
                        randomQuestions();
                        if (!questionList.isEmpty()) {
                            currentQuestionIndex = 0;
                            showQuestion(currentQuestionIndex);
                        } else {
                            Toast.makeText(getContext(), "Không tìm thấy câu hỏi cho môn: " + subjectId, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getContext(), "Lỗi kết nối dữ liệu Firestore!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showQuestion(int index) {

        if (questionList == null || index >= questionList.size()) return;

        answered = false;
        hasWrongAnswer = false;
        binding.btnNext.setEnabled(false);
        binding.btnNext.setAlpha(0.5f);

        binding.btnOptionA.setEnabled(true);
        binding.btnOptionB.setEnabled(true);
        binding.btnOptionC.setEnabled(true);
        binding.btnOptionD.setEnabled(true);

        binding.quizProgressBar.setProgress(index + 1);

        QueryDocumentSnapshot currentDoc = questionList.get(index);

        binding.tvQuestion.setText(currentDoc.getString("text"));

        List<String> options = (List<String>) currentDoc.get("option");

        if (options != null && options.size() >= 4) {
            binding.btnOptionA.setText(options.get(0));
            binding.btnOptionB.setText(options.get(1));
            binding.btnOptionC.setText(options.get(2));
            binding.btnOptionD.setText(options.get(3));
        }

        Long answer = currentDoc.getLong("correctAnswer");

        if (answer != null) {
            correctAnswerIndex = answer.intValue();
        } else {
            correctAnswerIndex = 0;
        }

        resetButtonBackgrounds();
    }

    private void checkAnswer(int selectedIndex, Button selectedButton) {

        if (answered) return;

        if (selectedIndex == correctAnswerIndex) {

            selectedButton.setBackgroundColor(getResources().getColor(R.color.correct_green));
            if (!hasWrongAnswer) {
                score++;
            }

            answered = true;
            binding.btnNext.setEnabled(true);
            binding.btnNext.setAlpha(1f);

            binding.btnOptionA.setEnabled(false);
            binding.btnOptionB.setEnabled(false);
            binding.btnOptionC.setEnabled(false);
            binding.btnOptionD.setEnabled(false);

        } else {
            hasWrongAnswer = true;
            selectedButton.setBackgroundColor(getResources().getColor(R.color.wrong_red));
        }
    }

    private void resetButtonBackgrounds() {

        int normalColor = Color.WHITE;

        binding.btnOptionA.setBackgroundColor(normalColor);
        binding.btnOptionB.setBackgroundColor(normalColor);
        binding.btnOptionC.setBackgroundColor(normalColor);
        binding.btnOptionD.setBackgroundColor(normalColor);
    }
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void randomQuestions() {

        Random random = new Random();

        for (int i = 0; i < questionList.size(); i++) {
            int j = random.nextInt(questionList.size());

            QueryDocumentSnapshot temp = questionList.get(i);
            questionList.set(i, questionList.get(j));
            questionList.set(j, temp);
        }

        if (questionList.size() > 10) {
            questionList = new ArrayList<>(questionList.subList(0, 10));
        }
    }
}