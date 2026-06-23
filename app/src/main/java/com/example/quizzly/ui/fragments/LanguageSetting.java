package com.example.quizzly.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.MainActivity;
import com.example.quizzly.databinding.FragmentLanguageSettingBinding;
import com.example.quizzly.utils.LocaleHelper;

public class LanguageSetting extends Fragment {

    private FragmentLanguageSettingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLanguageSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Highlight the currently active language
        String currentLang = LocaleHelper.getSavedLanguage(requireContext());
        updateCheckmarks(currentLang);

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        binding.llVietnamese.setOnClickListener(v -> {
            applyLanguage(LocaleHelper.LANG_VIETNAMESE);
        });

        binding.llEnglish.setOnClickListener(v -> {
            applyLanguage(LocaleHelper.LANG_ENGLISH);
        });
    }

    private void applyLanguage(String langCode) {
        // Only restart if the language actually changed
        String current = LocaleHelper.getSavedLanguage(requireContext());
        if (current.equals(langCode)) return;

        // Persist & apply new locale
        LocaleHelper.setLocale(requireContext(), langCode);

        // Restart MainActivity so all resources reload in the new locale
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void updateCheckmarks(String langCode) {
        if (LocaleHelper.LANG_VIETNAMESE.equals(langCode)) {
            binding.ivVietnameseCheck.setVisibility(View.VISIBLE);
            binding.ivEnglishCheck.setVisibility(View.INVISIBLE);
        } else {
            binding.ivEnglishCheck.setVisibility(View.VISIBLE);
            binding.ivVietnameseCheck.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
