package com.contestdemo.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestionIO {
    private QuestionIO() {}

    public static List<Map<String, Object>> loadTasks(Path path) throws IOException {
        Object data = Json.parse(Files.readString(path, StandardCharsets.UTF_8));
        List<Object> raw;
        if (data instanceof List<?>) {
            raw = Json.asList(data);
        } else {
            Map<String, Object> object = Json.asObject(data);
            if (object.containsKey("questions")) {
                raw = Json.asList(object.get("questions"));
            } else if (object.containsKey("tasks")) {
                raw = Json.asList(object.get("tasks"));
            } else {
                raw = Json.asList(object.get("items"));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : raw) {
            result.add(Json.asObject(item));
        }
        return result;
    }

    public static Map<String, Object> publicQuestion(Map<String, Object> task) {
        String question = Json.string(task.getOrDefault("question", task.get("description")));
        if (question.isBlank()) {
            throw new IllegalArgumentException("task is missing question: " + task.get("id"));
        }
        Map<String, Object> publicQuestion = new LinkedHashMap<>();
        if (task.containsKey("id")) {
            publicQuestion.put("id", task.get("id"));
        }
        publicQuestion.put("question", question);
        if (task.containsKey("files")) {
            publicQuestion.put("files", task.get("files"));
        }
        return publicQuestion;
    }

    public static void writeJson(Path path, Object data) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, Json.stringify(data) + "\n", StandardCharsets.UTF_8);
    }
}
