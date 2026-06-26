package com.example.quizzly.ui.fragments;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.R;


public class SubjectListFragment extends Fragment {

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

        // --- Bottom Navigation Tab Switching ---
        view.findViewById(R.id.navHome).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_subjectListFragment_to_homeFragment);
        });

        view.findViewById(R.id.navMenu).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_subjectListFragment_to_menuFragment);
        });

        // --- Subject Cards with press animation + navigation ---
        setupCardWithAnim(view, R.id.cardBiology,   "biology");
        setupCardWithAnim(view, R.id.cardChemistry, "chemistry");
        setupCardWithAnim(view, R.id.cardMath,      "maths");
        setupCardWithAnim(view, R.id.cardPhysics,   "physics");
        setupCardWithAnim(view, R.id.cardSports,    "sports");

        // --- Staggered entrance animation for each card ---
        playStaggeredEntrance(view,
                R.id.cardBiology,
                R.id.cardChemistry,
                R.id.cardMath,
                R.id.cardPhysics,
                R.id.cardSports);
    }

    /**
     * Gắn hiệu ứng scale khi nhấn/nhả + điều hướng khi click.
     */
    private void setupCardWithAnim(View root, int cardId, String subjectId) {
        View card = root.findViewById(cardId);
        if (card == null) return;

        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Thu nhỏ khi nhấn
                    v.animate()
                            .scaleX(0.94f)
                            .scaleY(0.94f)
                            .setDuration(100)
                            .setInterpolator(new android.view.animation.AccelerateInterpolator())
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Phồng lại với bounce khi nhả
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .start();
                    break;
            }
            return false; // Cho phép onClick tiếp tục xử lý
        });

        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", subjectId);
            Navigation.findNavController(v).navigate(
                    R.id.action_subjectListFragment_to_qizzFragment, bundle);
        });
    }

    /**
     * Hiệu ứng xuất hiện lần lượt (stagger) cho các card khi fragment mở.
     * Mỗi card fade-in + slide-up với độ trễ tăng dần.
     */
    private void playStaggeredEntrance(View root, int... cardIds) {
        int delayStep = 80; // ms giữa mỗi card
        for (int i = 0; i < cardIds.length; i++) {
            View card = root.findViewById(cardIds[i]);
            if (card == null) continue;

            // Đặt trạng thái ban đầu
            card.setAlpha(0f);
            card.setTranslationY(60f);

            // Animate với độ trễ stagger
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay((long) i * delayStep)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }
    }
}
