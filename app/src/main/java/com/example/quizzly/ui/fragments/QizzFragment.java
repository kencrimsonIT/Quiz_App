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

import com.example.quizzly.databinding.FragmentQizBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.quizzly.R;

public class QizzFragment extends Fragment {

    private FragmentQizBinding binding;
    private FirebaseFirestore db;

    private List<QueryDocumentSnapshot> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswerIndex = -1;
    private int selectedIndex = -1;
    private int score = 0;
    private int wrongCount = 0;
    private boolean answered = false;

    private static final int MAX_QUESTIONS = 15;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQizBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        binding.btnExit.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        binding.btnResultExit.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        binding.btnOptionA.setOnClickListener(v -> selectOption(0));
        binding.btnOptionB.setOnClickListener(v -> selectOption(1));
        binding.btnOptionC.setOnClickListener(v -> selectOption(2));
        binding.btnOptionD.setOnClickListener(v -> selectOption(3));

        binding.btnCheck.setOnClickListener(v -> checkSelectedAnswer());
        binding.btnPrevious.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        binding.btnNextAnswer.setOnClickListener(v -> goNextOrResult());
        binding.btnReviewAnswers.setOnClickListener(v -> restartQuiz());

        String subjectId = "";
        if (getArguments() != null) {
            subjectId = getArguments().getString("SUBJECT_ID", "");
        }

        showLoading(true);
        loadQuestionsFromFirestore(subjectId);
    }

    private void loadQuestionsFromFirestore(String subjectId) {
        db.collection("questions")
                .whereEqualTo("subid", subjectId)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful() && task.getResult() != null) {
                        questionList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            questionList.add(document);
                        }

                        randomQuestions();

                        if (!questionList.isEmpty()) {
                            currentQuestionIndex = 0;
                            score = 0;
                            wrongCount = 0;
                            showQuestion(currentQuestionIndex);
                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "Không tìm thấy câu hỏi cho môn: " + subjectId,
                                    Toast.LENGTH_LONG
                            ).show();
                            if (getView() != null) {
                                Navigation.findNavController(getView()).popBackStack();
                            }
                        }
                    } else {
                        Toast.makeText(
                                getContext(),
                                "Lỗi kết nối dữ liệu Firestore!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showQuestion(int index) {
        if (binding == null || questionList == null || index >= questionList.size()) return;

        answered = false;
        selectedIndex = -1;
        correctAnswerIndex = -1;

        binding.rootQiz.setBackgroundColor(Color.parseColor("#9ADCF2"));
        binding.quizContent.setVisibility(View.VISIBLE);
        binding.resultContent.setVisibility(View.GONE);
        binding.btnCheck.setVisibility(View.VISIBLE);
        binding.answerNavigation.setVisibility(View.GONE);

        enableOptions(true);
        resetOptionBackgrounds();

        binding.tvQuestionCount.setText((index + 1) + "/" + questionList.size());

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
        correctAnswerIndex = answer != null ? answer.intValue() : 0;
    }

    private void selectOption(int index) {
        if (answered) return;

        selectedIndex = index;
        resetOptionBackgrounds();
        getOptionButton(index).setBackgroundResource(com.example.quizzly.R.drawable.bg_qizz_option_selected);
        getOptionButton(index).setBackgroundTintList(null);
        getOptionButton(index).setTextColor(Color.WHITE);
    }

    private void checkSelectedAnswer() {
        if (selectedIndex < 0) {
            Toast.makeText(getContext(), "Bạn hãy chọn một đáp án", Toast.LENGTH_SHORT).show();
            return;
        }

        if (answered) return;
        answered = true;
        enableOptions(false);

        binding.btnCheck.setVisibility(View.GONE);
        binding.answerNavigation.setVisibility(View.VISIBLE);

        if (selectedIndex == correctAnswerIndex) {
            score++;
            binding.rootQiz.setBackgroundColor(Color.parseColor("#43F47A"));
            getOptionButton(correctAnswerIndex).setBackgroundResource(com.example.quizzly.R.drawable.bg_qizz_option_correct);
            getOptionButton(correctAnswerIndex).setBackgroundTintList(null);
            getOptionButton(correctAnswerIndex).setTextColor(Color.parseColor("#263238"));
        } else {
            wrongCount++;
            binding.rootQiz.setBackgroundColor(Color.parseColor("#FF5B61"));
            getOptionButton(selectedIndex).setBackgroundResource(com.example.quizzly.R.drawable.bg_qizz_option_wrong);
            getOptionButton(selectedIndex).setBackgroundTintList(null);
            getOptionButton(selectedIndex).setTextColor(Color.WHITE);
            getOptionButton(correctAnswerIndex).setBackgroundResource(com.example.quizzly.R.drawable.bg_qizz_option_correct);
            getOptionButton(correctAnswerIndex).setBackgroundTintList(null);
            getOptionButton(correctAnswerIndex).setTextColor(Color.parseColor("#263238"));
        }
    }

    private void goNextOrResult() {
        if (currentQuestionIndex < questionList.size() - 1) {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        } else {
            showFinalResult();
        }
    }

    private void showFinalResult() {
        binding.quizContent.setVisibility(View.GONE);
        binding.resultContent.setVisibility(View.VISIBLE);
        binding.rootQiz.setBackgroundColor(Color.parseColor("#0C4F8F"));

        int total = questionList.size();
        int wrong = total - score;
        wrongCount = wrong;

        boolean success = score >= Math.ceil(total * 0.6);

        binding.ivResultImage.setImageResource(
                success ? R.drawable.meme_success : R.drawable.meme_failed
        );

        binding.tvResultTitle.setText(success ? "Bạn đã Tây 🤩" : "Bạn chưa Tây đâu 👊");
        binding.tvCorrectCount.setText(score + "/" + total);
        binding.tvWrongCount.setText(wrongCount + "/" + total);
        binding.tvResultTime.setText("20:00");
    }

    private void restartQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        wrongCount = 0;
        randomQuestions();
        showQuestion(currentQuestionIndex);
    }



    private void showLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.quizContent.setVisibility(loading ? View.GONE : View.VISIBLE);
        binding.resultContent.setVisibility(View.GONE);
    }

    private void randomQuestions() {
        Collections.shuffle(questionList);
        if (questionList.size() > MAX_QUESTIONS) {
            questionList = new ArrayList<>(questionList.subList(0, MAX_QUESTIONS));
        }
    }

    private void resetOptionBackgrounds() {
        Button[] buttons = {
                binding.btnOptionA,
                binding.btnOptionB,
                binding.btnOptionC,
                binding.btnOptionD
        };

        for (Button button : buttons) {
            button.setBackgroundResource(com.example.quizzly.R.drawable.bg_qizz_option_default);
            button.setBackgroundTintList(null);
            button.setTextColor(Color.parseColor("#263238"));
        }
    }

    private void enableOptions(boolean enabled) {
        binding.btnOptionA.setEnabled(enabled);
        binding.btnOptionB.setEnabled(enabled);
        binding.btnOptionC.setEnabled(enabled);
        binding.btnOptionD.setEnabled(enabled);
    }

    private Button getOptionButton(int index) {
        switch (index) {
            case 0:
                return binding.btnOptionA;
            case 1:
                return binding.btnOptionB;
            case 2:
                return binding.btnOptionC;
            case 3:
                return binding.btnOptionD;
            default:
                return binding.btnOptionA;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
