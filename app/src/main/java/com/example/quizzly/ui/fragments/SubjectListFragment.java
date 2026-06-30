package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.example.quizzly.data.model.Subject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SubjectListFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutSubjectContainer;

    public SubjectListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        layoutSubjectContainer = view.findViewById(R.id.layoutSubjectContainer);

        // Bottom Navigation Tab Switching
        view.findViewById(R.id.navHome).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_subjectListFragment_to_homeFragment));

        view.findViewById(R.id.navMenu).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_subjectListFragment_to_menuFragment));

        loadSubjects();
    }

    private void loadSubjects() {
        db.collection("subjects")
                .orderBy("createdAt")
                .get()
                .addOnCompleteListener(task -> {
                    layoutSubjectContainer.removeAllViews();

                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        int index = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Subject subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            View subjectCard = createSubjectCard(subject, index);
                            layoutSubjectContainer.addView(subjectCard);
                            index++;
                        }
                        playStaggeredEntrance();
                    } else {
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        TextView tvEmpty = new TextView(requireContext());
        tvEmpty.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tvEmpty.setText("Chưa có chủ đề nào");
        tvEmpty.setTextColor(0xFF999999);
        tvEmpty.setTextSize(16);
        tvEmpty.setGravity(android.view.Gravity.CENTER);
        tvEmpty.setPadding(0, 80, 0, 0);
        layoutSubjectContainer.addView(tvEmpty);
    }

    private View createSubjectCard(Subject subject, int index) {
        int iconResId = getIconResource(subject.getIconName());
        int bgColor;
        try {
            bgColor = android.graphics.Color.parseColor(subject.getColor());
        } catch (Exception e) {
            bgColor = 0xFF9FE5F6;
        }

        float density = getResources().getDisplayMetrics().density;

        CardView card = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (80 * density));
        cardParams.bottomMargin = (int) (16 * density);
        card.setLayoutParams(cardParams);
        card.setRadius(20 * density);
        card.setCardElevation(0f);
        card.setCardBackgroundColor(bgColor);

        // Horizontal layout: icon | name | chevron
        LinearLayout row = new LinearLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding((int) (16 * density), 0, (int) (16 * density), 0);

        // Icon
        ImageView ivIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                (int) (60 * density), (int) (60 * density));
        iconLp.setMargins(0, 0, (int) (24 * density), 0);
        ivIcon.setLayoutParams(iconLp);
        ivIcon.setImageResource(iconResId);
        ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        row.addView(ivIcon);

        // Name
        TextView tvName = new TextView(requireContext());
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvName.setLayoutParams(nameLp);
        tvName.setText(subject.getName());
        tvName.setTextColor(0xFF222222);
        tvName.setTextSize(22);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tvName);

        // Chevron
        ImageView ivChevron = new ImageView(requireContext());
        LinearLayout.LayoutParams chevronLp = new LinearLayout.LayoutParams(
                (int) (24 * density), (int) (24 * density));
        ivChevron.setLayoutParams(chevronLp);
        ivChevron.setImageResource(R.drawable.ic_chevron_right);
        ivChevron.setColorFilter(0xFF666666);
        row.addView(ivChevron);

        card.addView(row);

        // Press animation
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.94f).scaleY(0.94f)
                            .setDuration(100)
                            .setInterpolator(new android.view.animation.AccelerateInterpolator())
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f)
                            .setDuration(200)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .start();
                    break;
            }
            return false;
        });

        // Navigate to quiz
        String subjectId = subject.getId();
        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", subjectId);
            Navigation.findNavController(v).navigate(
                    R.id.action_subjectListFragment_to_qizzFragment, bundle);
        });

        return card;
    }

    private void playStaggeredEntrance() {
        int delayStep = 80;
        int count = layoutSubjectContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View card = layoutSubjectContainer.getChildAt(i);
            card.setAlpha(0f);
            card.setTranslationY(60f);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay((long) i * delayStep)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }
    }

    private int getIconResource(String iconName) {
        if (iconName == null) return R.drawable.ic_launcher_foreground;
        switch (iconName) {
            case "biology":   return R.drawable.biology;
            case "chemistry": return R.drawable.chemistry;
            case "maths":     return R.drawable.maths;
            case "physics":   return R.drawable.physics;
            case "sport":     return R.drawable.sport;
            default:
                int resId = getResources().getIdentifier(iconName, "drawable", requireContext().getPackageName());
                return resId != 0 ? resId : R.drawable.ic_launcher_foreground;
        }
    }
}
