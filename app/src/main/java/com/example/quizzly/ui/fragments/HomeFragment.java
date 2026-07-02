package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateInterpolator;

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

        // --- Bottom nav tab switching ---
        binding.navMenu.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_menuFragment);
        });

        binding.navQuiz.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_subjectListFragment);
        });

        // --- Subject cards với scale bounce animation ---
        setupCardWithAnim(binding.cardBiology,   "NTMYDkr2XeZTEYWbmme6", view);
        setupCardWithAnim(binding.cardChemistry, "BfSFQSkUdeJ660KtlTnM", view);
        setupCardWithAnim(binding.cardMath,      "nA84Q4UnyIMhLToo8EZa", view);
        setupCardWithAnim(binding.cardPhysics,   "GQsUeRmni6PtUIJ4C6be", view);

        // --- Staggered entrance animation ---
        playStaggeredEntrance(
                binding.cardBiology,
                binding.cardChemistry,
                binding.cardMath,
                binding.cardPhysics);
    }

    /**
     * Gắn hiệu ứng scale bounce khi nhấn/nhả + navigate khi click.
     */
    private void setupCardWithAnim(View card, String subjectId, View navView) {
        if (card == null) return;

        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.93f).scaleY(0.93f)
                            .setDuration(100)
                            .setInterpolator(new AccelerateInterpolator())
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1.0f).scaleY(1.0f)
                            .setDuration(200)
                            .setInterpolator(new OvershootInterpolator(2.5f))
                            .start();
                    break;
            }
            return false;
        });

        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("SUBJECT_ID", subjectId);
            Navigation.findNavController(navView).navigate(R.id.action_homeFragment_to_qizzFragment, bundle);
        });
    }

    /**
     * Hiệu ứng xuất hiện lần lượt (stagger) cho các card khi vào Home.
     */
    private void playStaggeredEntrance(View... cards) {
        int delayStep = 90;
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card == null) continue;
            card.setAlpha(0f);
            card.setTranslationY(50f);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(380)
                    .setStartDelay((long) i * delayStep)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
