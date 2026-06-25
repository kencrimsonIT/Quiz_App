package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subject_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: Handle clicks to navigate to SubtopicListFragment
        View cardChemistry = view.findViewById(R.id.cardChemistry);
        if (cardChemistry != null) {
            cardChemistry.setOnClickListener(v -> {
                // Navigation.findNavController(v).navigate(R.id.action_subjectListFragment_to_subtopicListFragment);
            });
        }
    }
}
