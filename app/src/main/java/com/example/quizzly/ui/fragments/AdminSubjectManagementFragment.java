package com.example.quizzly.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;

public class AdminSubjectManagementFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutSubjectContainer;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private View btnAddSubject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_subject_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        layoutSubjectContainer = view.findViewById(R.id.layoutSubjectContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        btnAddSubject = view.findViewById(R.id.btnAddSubject);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        btnAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        loadSubjects();
    }

    private void loadSubjects() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        layoutSubjectContainer.removeAllViews();

        db.collection("subjects")
                .orderBy("createdAt")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Subject subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            View subjectCard = createSubjectCard(subject);
                            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) subjectCard.getLayoutParams();
                            cardParams.bottomMargin = 12;
                            subjectCard.setLayoutParams(cardParams);
                            layoutSubjectContainer.addView(subjectCard);
                        }
                    } else {
                        tvEmptyState.setText("Không thể tải danh sách chủ đề");
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                });
    }

    private View createSubjectCard(Subject subject) {
        CardView card = new CardView(requireContext());
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        card.setCardElevation(2f);
        card.setRadius(20f);
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setUseCompatPadding(true);

        LinearLayout cardContent = new LinearLayout(requireContext());
        cardContent.setOrientation(LinearLayout.HORIZONTAL);
        cardContent.setPadding(16, 12, 12, 12);

        // Icon
        ImageView ivIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(52, 52);
        iconParams.setMargins(0, 0, 16, 0);
        iconParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        ivIcon.setLayoutParams(iconParams);
        int iconResId = getIconResource(subject.getIconName());
        ivIcon.setImageResource(iconResId);
        ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Apply background color
        CardView iconBg = new CardView(requireContext());
        LinearLayout.LayoutParams bgParams = new LinearLayout.LayoutParams(52, 52);
        bgParams.setMargins(0, 0, 16, 0);
        bgParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        iconBg.setLayoutParams(bgParams);
        iconBg.setRadius(16f);
        iconBg.setCardElevation(0f);
        try {
            iconBg.setCardBackgroundColor(android.graphics.Color.parseColor(subject.getColor()));
        } catch (Exception e) {
            iconBg.setCardBackgroundColor(0xFF9FE5F6);
        }
        iconBg.addView(ivIcon);

        cardContent.addView(iconBg);

        // Name column
        LinearLayout nameCol = new LinearLayout(requireContext());
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nameCol.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvName = new TextView(requireContext());
        tvName.setText(subject.getName());
        tvName.setTextColor(0xFF111111);
        tvName.setTextSize(17);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        nameCol.addView(tvName);

        TextView tvIconName = new TextView(requireContext());
        tvIconName.setText("Icon: " + subject.getIconName());
        tvIconName.setTextColor(0xFF888888);
        tvIconName.setTextSize(12);
        nameCol.addView(tvIconName);

        cardContent.addView(nameCol);

        // Edit button
        android.widget.ImageButton btnEdit = new android.widget.ImageButton(requireContext());
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(44, 44);
        editParams.setMargins(4, 0, 4, 0);
        editParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        btnEdit.setLayoutParams(editParams);
        btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
        btnEdit.setColorFilter(0xFF555555);
        btnEdit.setBackgroundResource(R.drawable.bg_button);
        btnEdit.setPadding(10, 10, 10, 10);
        btnEdit.setOnClickListener(v -> showEditSubjectDialog(subject));
        cardContent.addView(btnEdit);

        // Delete button
        android.widget.ImageButton btnDelete = new android.widget.ImageButton(requireContext());
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(44, 44);
        deleteParams.setMargins(4, 0, 0, 0);
        deleteParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        btnDelete.setLayoutParams(deleteParams);
        btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
        btnDelete.setColorFilter(0xFFCC0000);
        btnDelete.setBackgroundResource(R.drawable.bg_button);
        btnDelete.setPadding(10, 10, 10, 10);
        btnDelete.setOnClickListener(v -> showDeleteConfirmation(subject));
        cardContent.addView(btnDelete);

        card.addView(cardContent);
        return card;
    }

    private void showAddSubjectDialog() {
        showSubjectDialog(null);
    }

    private void showEditSubjectDialog(Subject subject) {
        showSubjectDialog(subject);
    }

    private void showSubjectDialog(@Nullable Subject existingSubject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_Dialog);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_subject_form, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etSubjectName);
        LinearLayout layoutIcons = dialogView.findViewById(R.id.layoutIconPicker);
        LinearLayout layoutColors = dialogView.findViewById(R.id.layoutColorPicker);
        TextView tvSelectedIcon = dialogView.findViewById(R.id.tvSelectedIcon);
        ImageView selectedIconPreview = dialogView.findViewById(R.id.selectedIconPreview);

        final String[] selectedIcon = {Subject.AVAILABLE_ICONS[0]};
        final String[] selectedColor = {Subject.AVAILABLE_COLORS[0]};

        boolean isEditing = existingSubject != null;
        if (isEditing) {
            builder.setTitle("Sửa chủ đề");
            etName.setText(existingSubject.getName());
            selectedIcon[0] = existingSubject.getIconName();
            selectedColor[0] = existingSubject.getColor();
        } else {
            builder.setTitle("Thêm chủ đề mới");
        }

        // Setup icon picker
        final java.util.List<View> iconOptions = new java.util.ArrayList<>();
        for (String iconName : Subject.AVAILABLE_ICONS) {
            View iconOption = inflater.inflate(R.layout.item_icon_option, layoutIcons, false);
            ImageView ivIcon = iconOption.findViewById(R.id.ivIconOption);
            int resId = getIconResource(iconName);
            ivIcon.setImageResource(resId);

            // Highlight selected
            updateIconHighlight(iconOption, iconName.equals(selectedIcon[0]));

            final String currentIcon = iconName;
            iconOption.setOnClickListener(v -> {
                selectedIcon[0] = currentIcon;
                tvSelectedIcon.setText("Icon: " + currentIcon);
                selectedIconPreview.setImageResource(getIconResource(currentIcon));
                // Update highlights for all icons
                for (View opt : iconOptions) {
                    String tag = (String) opt.getTag();
                    updateIconHighlight(opt, currentIcon.equals(tag));
                }
            });
            iconOption.setTag(iconName);
            iconOptions.add(iconOption);
            layoutIcons.addView(iconOption);
        }

        // Setup color picker
        final java.util.List<View> colorOptions = new java.util.ArrayList<>();
        for (String colorHex : Subject.AVAILABLE_COLORS) {
            View colorOption = new View(requireContext());
            LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams((int) (40 * getResources().getDisplayMetrics().density), (int) (40 * getResources().getDisplayMetrics().density));
            colorParams.setMargins((int) (4 * getResources().getDisplayMetrics().density), (int) (4 * getResources().getDisplayMetrics().density), (int) (4 * getResources().getDisplayMetrics().density), (int) (4 * getResources().getDisplayMetrics().density));
            colorOption.setLayoutParams(colorParams);
            int parsedColor;
            try {
                parsedColor = android.graphics.Color.parseColor(colorHex);
            } catch (Exception ignored) {
                parsedColor = 0xFF9FE5F6;
            }
            colorOption.setBackgroundColor(parsedColor);

            // Update highlight based on selection
            updateColorHighlight(colorOption, colorHex.equals(selectedColor[0]), parsedColor);

            final String hex = colorHex;
            final int finalParsedColor = parsedColor;
            colorOption.setOnClickListener(v -> {
                selectedColor[0] = hex;
                for (View opt : colorOptions) {
                    String tag = (String) opt.getTag();
                    int col;
                    try {
                        col = android.graphics.Color.parseColor(tag);
                    } catch (Exception e) {
                        col = 0xFF9FE5F6;
                    }
                    updateColorHighlight(opt, hex.equals(tag), col);
                }
            });
            colorOption.setTag(colorHex);
            colorOptions.add(colorOption);
            layoutColors.addView(colorOption);
        }

        // Update preview
        tvSelectedIcon.setText("Icon: " + selectedIcon[0]);
        selectedIconPreview.setImageResource(getIconResource(selectedIcon[0]));

        builder.setPositiveButton(isEditing ? "Lưu" : "Thêm", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Vui lòng nhập tên chủ đề");
                return;
            }
            if (isEditing) {
                updateSubject(existingSubject.getId(), name, selectedIcon[0], selectedColor[0]);
            } else {
                addSubject(name, selectedIcon[0], selectedColor[0]);
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateIconHighlight(View iconOption, boolean selected) {
        if (selected) {
            iconOption.setBackgroundResource(R.drawable.bg_button_blue);
            iconOption.setPadding((int) (4 * getResources().getDisplayMetrics().density),
                    (int) (4 * getResources().getDisplayMetrics().density),
                    (int) (4 * getResources().getDisplayMetrics().density),
                    (int) (4 * getResources().getDisplayMetrics().density));
        } else {
            iconOption.setBackgroundResource(R.drawable.bg_button);
            iconOption.setPadding(0, 0, 0, 0);
        }
    }

    private void updateColorHighlight(View colorOption, boolean selected, int color) {
        if (selected) {
            // Draw a border by using a CardView-like approach: set a bigger background
            // and then the color inside
            colorOption.setBackgroundColor(color);
            // Add padding to create a border effect
            colorOption.setPadding((int) (3 * getResources().getDisplayMetrics().density),
                    (int) (3 * getResources().getDisplayMetrics().density),
                    (int) (3 * getResources().getDisplayMetrics().density),
                    (int) (3 * getResources().getDisplayMetrics().density));
        } else {
            colorOption.setBackgroundColor(color);
            colorOption.setPadding(0, 0, 0, 0);
        }
    }

    private void addSubject(String name, String iconName, String color) {
        progressBar.setVisibility(View.VISIBLE);

        Subject subject = new Subject(null, name, iconName, color, System.currentTimeMillis());

        db.collection("subjects")
                .add(subject.toMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadSubjects();
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void updateSubject(String id, String name, String iconName, String color) {
        progressBar.setVisibility(View.VISIBLE);

        Subject subject = new Subject(id, name, iconName, color, System.currentTimeMillis());

        db.collection("subjects")
                .document(id)
                .set(subject.toMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadSubjects();
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void showDeleteConfirmation(Subject subject) {
        new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_Dialog)
                .setTitle("Xóa chủ đề")
                .setMessage("Bạn có chắc muốn xóa chủ đề \"" + subject.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteSubject(subject.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteSubject(String id) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("subjects")
                .document(id)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadSubjects();
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                });
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
                // Try to get by resource name
                int resId = getResources().getIdentifier(iconName, "drawable", requireContext().getPackageName());
                return resId != 0 ? resId : R.drawable.ic_launcher_foreground;
        }
    }
}
