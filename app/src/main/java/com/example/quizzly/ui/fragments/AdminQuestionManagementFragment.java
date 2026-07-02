package com.example.quizzly.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.data.model.Question;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminQuestionManagementFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutQuestionContainer;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private View btnAddQuestion;
    private TextView tvFilterSubject;
    private View cardSubjectFilter;

    private List<SubjectInfo> subjects = new ArrayList<>();
    private String selectedSubjectId = null; // null = show all
    private List<QuestionEntry> allQuestions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_question_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        layoutQuestionContainer = view.findViewById(R.id.layoutQuestionContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        btnAddQuestion = view.findViewById(R.id.btnAddQuestion);
        tvFilterSubject = view.findViewById(R.id.tvFilterSubject);
        cardSubjectFilter = view.findViewById(R.id.cardSubjectFilter);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        btnAddQuestion.setOnClickListener(v -> showAddQuestionDialog());

        // If navigated from "Thêm câu hỏi mới" button, auto-open the add dialog after loading
        if (getArguments() != null && getArguments().getBoolean("openAddDialog", false)) {
            // Wait for data to load, then show dialog
            btnAddQuestion.post(this::showAddQuestionDialog);
        }

        cardSubjectFilter.setOnClickListener(v -> showSubjectFilterDialog());

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        // Load both subjects and questions in parallel
        db.collection("subjects").orderBy("createdAt").get()
                .addOnCompleteListener(subjectTask -> {
                    subjects.clear();
                    if (subjectTask.isSuccessful() && subjectTask.getResult() != null) {
                        for (QueryDocumentSnapshot doc : subjectTask.getResult()) {
                            String name = doc.getString("name");
                            subjects.add(new SubjectInfo(doc.getId(), name != null ? name : "Unknown"));
                        }
                    }
                    // After subjects loaded, load questions
                    loadQuestions();
                });
    }

    private void loadQuestions() {
        db.collection("questions").get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    allQuestions.clear();

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Question q = doc.toObject(Question.class);
                            q.setId(doc.getId());
                            String subjectName = getSubjectName(q.getSubid());
                            allQuestions.add(new QuestionEntry(q, subjectName));
                        }
                    }
                    renderQuestions();
                });
    }

    private void renderQuestions() {
        layoutQuestionContainer.removeAllViews();
        tvEmptyState.setVisibility(View.GONE);

        List<QuestionEntry> filtered = new ArrayList<>();
        for (QuestionEntry entry : allQuestions) {
            if (selectedSubjectId == null || selectedSubjectId.equals(entry.question.getSubid())) {
                filtered.add(entry);
            }
        }

        if (filtered.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        for (QuestionEntry entry : filtered) {
            View card = createQuestionCard(entry);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
            params.bottomMargin = 12;
            card.setLayoutParams(params);
            layoutQuestionContainer.addView(card);
        }
    }

    private String getSubjectName(String subid) {
        if (subid == null) return "Không có chủ đề";
        for (SubjectInfo s : subjects) {
            if (s.id.equals(subid)) return s.name;
        }
        return "Không xác định";
    }

    private View createQuestionCard(QuestionEntry entry) {
        Question q = entry.question;
        float density = getResources().getDisplayMetrics().density;

        CardView card = new CardView(requireContext());
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        card.setCardElevation(2f);
        card.setRadius(20f);
        card.setCardBackgroundColor(requireContext().getColor(R.color.card_background));
        card.setUseCompatPadding(true);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding((int) (16 * density), (int) (14 * density),
                (int) (16 * density), (int) (14 * density));

        // Subject badge + correct answer indicator
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Subject badge
        TextView tvSubject = new TextView(requireContext());
        LinearLayout.LayoutParams subjectLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvSubject.setLayoutParams(subjectLp);
        tvSubject.setText(entry.subjectName);
        tvSubject.setTextColor(requireContext().getColor(R.color.primary_blue));
        tvSubject.setTextSize(12);
        tvSubject.setTypeface(null, android.graphics.Typeface.BOLD);
        tvSubject.setPadding((int) (10 * density), (int) (2 * density),
                (int) (10 * density), (int) (2 * density));
        tvSubject.setBackgroundResource(R.drawable.bg_button_blue);
        headerRow.addView(tvSubject);

        // Correct answer badge
        TextView tvCorrect = new TextView(requireContext());
        LinearLayout.LayoutParams correctLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        correctLp.setMargins((int) (8 * density), 0, 0, 0);
        tvCorrect.setLayoutParams(correctLp);
        String[] labels = {"A", "B", "C", "D"};
        int idx = q.getCorrectAnswer();
        String correctLabel = (idx >= 0 && idx < labels.length) ? labels[idx] : "?";
        tvCorrect.setText("✓ " + correctLabel);
        tvCorrect.setTextColor(requireContext().getColor(R.color.quiz_option_correct_bg));
        tvCorrect.setTextSize(12);
        tvCorrect.setTypeface(null, android.graphics.Typeface.BOLD);
        headerRow.addView(tvCorrect);

        content.addView(headerRow);

        // Question text
        TextView tvQuestion = new TextView(requireContext());
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qLp.setMargins(0, (int) (8 * density), 0, (int) (6 * density));
        tvQuestion.setLayoutParams(qLp);
        tvQuestion.setText(q.getText());
        tvQuestion.setTextColor(requireContext().getColor(R.color.text_primary));
        tvQuestion.setTextSize(15);
        tvQuestion.setTypeface(null, android.graphics.Typeface.BOLD);
        tvQuestion.setMaxLines(2);
        tvQuestion.setEllipsize(android.text.TextUtils.TruncateAt.END);
        content.addView(tvQuestion);

        // Options preview
        if (q.getOption() != null) {
            String[] labels2 = {"A", "B", "C", "D"};
            for (int i = 0; i < q.getOption().size() && i < 4; i++) {
                String optText = q.getOption().get(i);
                if (optText == null || optText.isEmpty()) continue;

                TextView tvOpt = new TextView(requireContext());
                tvOpt.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tvOpt.setText("  " + labels2[i] + ". " + optText);
                tvOpt.setTextColor(requireContext().getColor(R.color.text_tertiary));
                tvOpt.setTextSize(13);
                if (i == q.getCorrectAnswer()) {
                    tvOpt.setTextColor(requireContext().getColor(R.color.quiz_option_correct_bg));
                    tvOpt.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                content.addView(tvOpt);
            }
        }

        // Action buttons
        LinearLayout actionRow = new LinearLayout(requireContext());
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionLp.setMargins(0, (int) (10 * density), 0, 0);
        actionRow.setLayoutParams(actionLp);
        actionRow.setGravity(android.view.Gravity.END);

        // Edit button
        ImageButton btnEdit = new ImageButton(requireContext());
        LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(
                (int) (40 * density), (int) (40 * density));
        editLp.setMargins(0, 0, (int) (8 * density), 0);
        btnEdit.setLayoutParams(editLp);
        btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
        btnEdit.setColorFilter(requireContext().getColor(R.color.text_tertiary));
        btnEdit.setBackgroundResource(R.drawable.bg_button);
        btnEdit.setPadding((int) (8 * density), (int) (8 * density),
                (int) (8 * density), (int) (8 * density));
        btnEdit.setOnClickListener(v -> showEditQuestionDialog(q));
        actionRow.addView(btnEdit);

        // Delete button
        ImageButton btnDelete = new ImageButton(requireContext());
        LinearLayout.LayoutParams deleteLp = new LinearLayout.LayoutParams(
                (int) (40 * density), (int) (40 * density));
        btnDelete.setLayoutParams(deleteLp);
        btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
        btnDelete.setColorFilter(0xFFCC0000);
        btnDelete.setBackgroundResource(R.drawable.bg_button);
        btnDelete.setPadding((int) (8 * density), (int) (8 * density),
                (int) (8 * density), (int) (8 * density));
        btnDelete.setOnClickListener(v -> showDeleteConfirmation(q));
        actionRow.addView(btnDelete);

        content.addView(actionRow);
        card.addView(content);
        return card;
    }

    // ==================== Dialog: Subject Filter ====================

    private void showSubjectFilterDialog() {
        String[] names = new String[subjects.size() + 1];
        names[0] = "Tất cả chủ đề";
        for (int i = 0; i < subjects.size(); i++) {
            names[i + 1] = subjects.get(i).name;
        }

        int checkedItem = 0;
        if (selectedSubjectId != null) {
            for (int i = 0; i < subjects.size(); i++) {
                if (subjects.get(i).id.equals(selectedSubjectId)) {
                    checkedItem = i + 1;
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Lọc theo chủ đề")
                .setSingleChoiceItems(names, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        selectedSubjectId = null;
                        tvFilterSubject.setText("Tất cả chủ đề");
                    } else {
                        selectedSubjectId = subjects.get(which - 1).id;
                        tvFilterSubject.setText(subjects.get(which - 1).name);
                    }
                    renderQuestions();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ==================== Dialogs: Add / Edit Question ====================

    private void showAddQuestionDialog() {
        showQuestionDialog(null);
    }

    private void showEditQuestionDialog(Question question) {
        showQuestionDialog(question);
    }

    private void showQuestionDialog(@Nullable Question existingQuestion) {
        if (subjects.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chưa có chủ đề")
                    .setMessage("Vui lòng thêm chủ đề trước khi tạo câu hỏi.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_question_form, null);
        builder.setView(dialogView);

        EditText etQuestionText = dialogView.findViewById(R.id.etQuestionText);
        EditText etOptionA = dialogView.findViewById(R.id.etOptionA);
        EditText etOptionB = dialogView.findViewById(R.id.etOptionB);
        EditText etOptionC = dialogView.findViewById(R.id.etOptionC);
        EditText etOptionD = dialogView.findViewById(R.id.etOptionD);
        TextView tvSelectedSubject = dialogView.findViewById(R.id.tvSelectedSubject);
        RadioButton rbCorrectA = dialogView.findViewById(R.id.rbCorrectA);
        RadioButton rbCorrectB = dialogView.findViewById(R.id.rbCorrectB);
        RadioButton rbCorrectC = dialogView.findViewById(R.id.rbCorrectC);
        RadioButton rbCorrectD = dialogView.findViewById(R.id.rbCorrectD);

        final String[] selectedSubjectId = {subjects.get(0).id};
        final int[] correctAnswer = {0};

        boolean isEditing = existingQuestion != null;
        if (isEditing) {
            builder.setTitle("Sửa câu hỏi");
            etQuestionText.setText(existingQuestion.getText());
            selectedSubjectId[0] = existingQuestion.getSubid();

            List<String> opts = existingQuestion.getOption();
            if (opts != null) {
                if (opts.size() > 0) etOptionA.setText(opts.get(0));
                if (opts.size() > 1) etOptionB.setText(opts.get(1));
                if (opts.size() > 2) etOptionC.setText(opts.get(2));
                if (opts.size() > 3) etOptionD.setText(opts.get(3));
            }
            correctAnswer[0] = existingQuestion.getCorrectAnswer();
        } else {
            builder.setTitle("Thêm câu hỏi mới");
        }

        // Set correct answer radio
        RadioButton[] radios = {rbCorrectA, rbCorrectB, rbCorrectC, rbCorrectD};
        if (correctAnswer[0] >= 0 && correctAnswer[0] < 4) {
            radios[correctAnswer[0]].setChecked(true);
        } else {
            rbCorrectA.setChecked(true);
        }

        // Subject selector
        updateSubjectText(tvSelectedSubject, selectedSubjectId[0]);
        tvSelectedSubject.setOnClickListener(v -> {
            String[] subjectNames = new String[subjects.size()];
            for (int i = 0; i < subjects.size(); i++) {
                subjectNames[i] = subjects.get(i).name;
            }
            int selectedIdx = 0;
            for (int i = 0; i < subjects.size(); i++) {
                if (subjects.get(i).id.equals(selectedSubjectId[0])) {
                    selectedIdx = i;
                    break;
                }
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn chủ đề")
                    .setSingleChoiceItems(subjectNames, selectedIdx, (dialog, which) -> {
                        selectedSubjectId[0] = subjects.get(which).id;
                        updateSubjectText(tvSelectedSubject, selectedSubjectId[0]);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Radio listeners with mutual exclusion
        RadioButton[] allRadios = {rbCorrectA, rbCorrectB, rbCorrectC, rbCorrectD};
        for (int i = 0; i < allRadios.length; i++) {
            final int index = i;
            allRadios[i].setOnClickListener(v -> {
                correctAnswer[0] = index;
                // Ensure mutual exclusion
                for (int j = 0; j < allRadios.length; j++) {
                    allRadios[j].setChecked(j == index);
                }
            });
        }

        builder.setPositiveButton(isEditing ? "Lưu" : "Thêm", (dialog, which) -> {
            String text = etQuestionText.getText().toString().trim();
            String optA = etOptionA.getText().toString().trim();
            String optB = etOptionB.getText().toString().trim();
            String optC = etOptionC.getText().toString().trim();
            String optD = etOptionD.getText().toString().trim();

            if (text.isEmpty()) {
                etQuestionText.setError("Vui lòng nhập câu hỏi");
                return;
            }
            if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {                                showToast("Vui lòng nhập đầy đủ 4 đáp án");
                return;
            }

            List<String> options = Arrays.asList(optA, optB, optC, optD);

            if (isEditing) {
                updateQuestion(existingQuestion.getId(), text, options, correctAnswer[0], selectedSubjectId[0]);
            } else {
                addQuestion(text, options, correctAnswer[0], selectedSubjectId[0]);
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateSubjectText(TextView tv, String subjectId) {
        for (SubjectInfo s : subjects) {
            if (s.id.equals(subjectId)) {
                tv.setText(s.name);
                return;
            }
        }
        tv.setText("Chọn chủ đề...");
    }

    private void addQuestion(String text, List<String> options, int correctAnswer, String subid) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("option", options);
        data.put("correctAnswer", correctAnswer);
        data.put("subid", subid);

        db.collection("questions").add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadQuestions();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showToast("Lỗi khi thêm câu hỏi");
                    }
                });
    }

    private void updateQuestion(String id, String text, List<String> options, int correctAnswer, String subid) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("option", options);
        data.put("correctAnswer", correctAnswer);
        data.put("subid", subid);

        db.collection("questions").document(id).set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadQuestions();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showToast("Lỗi khi cập nhật câu hỏi");
                    }
                });
    }

    private void showDeleteConfirmation(Question question) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa câu hỏi")
                .setMessage("Bạn có chắc muốn xóa câu hỏi này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteQuestion(question.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteQuestion(String id) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("questions").document(id).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadQuestions();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        showToast("Lỗi khi xóa câu hỏi");
                    }
                });
    }

    // ==================== Helper Classes ====================

    private static class SubjectInfo {
        String id;
        String name;
        SubjectInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static class QuestionEntry {
        Question question;
        String subjectName;
        QuestionEntry(Question question, String subjectName) {
            this.question = question;
            this.subjectName = subjectName;
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }
}
