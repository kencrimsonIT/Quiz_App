package com.example.quizzly.data.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Question {

    private String id;
    private String text;
    private List<String> option;
    private int correctAnswer;
    private String subid;

    public Question() {
        // Required empty constructor for Firestore deserialization
    }

    public Question(String id, String text, List<String> option, int correctAnswer, String subid) {
        this.id = id;
        this.text = text;
        this.option = option;
        this.correctAnswer = correctAnswer;
        this.subid = subid;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOption() { return option; }
    public void setOption(List<String> option) { this.option = option; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getSubid() { return subid; }
    public void setSubid(String subid) { this.subid = subid; }

    /**
     * Convert to a Map for Firestore.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("option", option);
        map.put("correctAnswer", correctAnswer);
        map.put("subid", subid);
        return map;
    }
}
