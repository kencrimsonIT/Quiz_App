package com.example.quizzly.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzly.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class UserManagementFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutUserContainer;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        layoutUserContainer = view.findViewById(R.id.layoutUserContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> userData = document.getData();
                            View userCard = createUserCard(userData, document.getId());
                            // Add bottom margin for spacing between cards
                            LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) userCard.getLayoutParams();
                            cardParams.bottomMargin = 12;
                            userCard.setLayoutParams(cardParams);
                            layoutUserContainer.addView(userCard);
                        }
                    } else {
                        tvEmptyState.setText("Không thể tải danh sách người dùng");
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                });
    }

    private View createUserCard(Map<String, Object> userData, String uid) {
        CardView card = new CardView(requireContext());
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        card.setCardElevation(2f);
        card.setRadius(20f);
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setUseCompatPadding(true);

        // Inflate the card content
        LinearLayout cardContent = new LinearLayout(requireContext());
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(20, 20, 20, 20);

        // --- Header row: avatar + name + role badge ---
        LinearLayout headerRow = new LinearLayout(requireContext());
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Avatar icon
        androidx.appcompat.widget.AppCompatImageView avatar = new androidx.appcompat.widget.AppCompatImageView(requireContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(48, 48);
        avatarParams.setMargins(0, 0, 12, 0);
        avatar.setLayoutParams(avatarParams);
        avatar.setImageResource(R.drawable.ic_person);
        avatar.setColorFilter(0xFF555555);
        avatar.setBackgroundResource(R.drawable.bg_profile_card);
        avatar.setPadding(8, 8, 8, 8);
        headerRow.addView(avatar);

        // Name + email column
        LinearLayout nameCol = new LinearLayout(requireContext());
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Safely get fields — getOrDefault returns null if key exists with null value
        String displayName = safeString(userData, "displayName", "Chưa có tên");
        String email = safeString(userData, "email", null);

        TextView tvName = new TextView(requireContext());
        tvName.setText(displayName);
        tvName.setTextColor(0xFF111111);
        tvName.setTextSize(16);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        nameCol.addView(tvName);

        if (email != null && !email.isEmpty()) {
            TextView tvEmail = new TextView(requireContext());
            tvEmail.setText(email);
            tvEmail.setTextColor(0xFF666666);
            tvEmail.setTextSize(13);
            nameCol.addView(tvEmail);
        }

        headerRow.addView(nameCol);

        // Role badge
        String role = safeString(userData, "role", "user");
        TextView tvRole = new TextView(requireContext());
        LinearLayout.LayoutParams roleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        roleParams.setMargins(8, 0, 0, 0);
        tvRole.setLayoutParams(roleParams);
        tvRole.setPadding(12, 4, 12, 4);
        tvRole.setTextSize(12);
        tvRole.setTypeface(null, android.graphics.Typeface.BOLD);

        if ("admin".equals(role)) {
            tvRole.setText("Admin");
            tvRole.setTextColor(0xFF0039B6);
            tvRole.setBackgroundResource(R.drawable.bg_button_blue);
        } else {
            tvRole.setText("User");
            tvRole.setTextColor(0xFF555555);
            tvRole.setBackgroundResource(R.drawable.bg_button);
        }
        headerRow.addView(tvRole);

        cardContent.addView(headerRow);

        // --- Divider ---
        View divider = new View(requireContext());
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        divParams.setMargins(0, 14, 0, 14);
        divider.setLayoutParams(divParams);
        divider.setBackgroundColor(0xFFE0E0E0);
        cardContent.addView(divider);

        // --- Details row: UID ---
        if (uid != null) {
            LinearLayout detailRowUid = createDetailRow("ID", uid, false);
            cardContent.addView(detailRowUid);
        }

        // --- Created date ---
        Object createdAt = userData.get("createdAt");
        if (createdAt != null) {
            String dateStr;
            if (createdAt instanceof com.google.firebase.Timestamp) {
                com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) createdAt;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                dateStr = sdf.format(new Date(ts.getSeconds() * 1000));
            } else {
                dateStr = createdAt.toString();
            }
            LinearLayout detailRowDate = createDetailRow("Ngày tham gia", dateStr, true);
            cardContent.addView(detailRowDate);
        }

        card.addView(cardContent);
        return card;
    }

    /** Helper: safely extract a String from the map, returning defaultValue if null/missing */
    private String safeString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        return value instanceof String ? (String) value : defaultValue;
    }

    private LinearLayout createDetailRow(String label, String value, boolean isLast) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (!isLast) {
            rowParams.setMargins(0, 0, 0, 6);
        }
        row.setLayoutParams(rowParams);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText(label + ": ");
        tvLabel.setTextColor(0xFF999999);
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tvLabel);

        TextView tvValue = new TextView(requireContext());
        tvValue.setText(value);
        tvValue.setTextColor(0xFF333333);
        tvValue.setTextSize(13);
        row.addView(tvValue);

        return row;
    }
}
