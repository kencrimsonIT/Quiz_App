package com.example.quizzly.data.model;

import java.util.HashMap;
import java.util.Map;

public class Subject {

    private String id;
    private String name;
    private String iconName;
    private String color;
    private long createdAt;

    public Subject() {
        // Required empty constructor for Firestore deserialization
    }

    public Subject(String id, String name, String iconName, String color, long createdAt) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.color = color;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Convert to a Map for Firestore.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("iconName", iconName);
        map.put("color", color);
        map.put("createdAt", createdAt);
        return map;
    }

    /**
     * Available drawable icon resource names.
     */
    public static final String[] AVAILABLE_ICONS = {
            "biology", "chemistry", "maths", "physics", "sport"
    };

    /**
     * Predefined card background colors.
     */
    public static final String[] AVAILABLE_COLORS = {
            "#9FE5F6", "#FFB3BA", "#BAFFC9", "#FFDFBA", "#D4BAFF", "#BAE1FF"
    };
}
