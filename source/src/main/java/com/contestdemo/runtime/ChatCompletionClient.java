package com.contestdemo.runtime;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChatCompletionClient {
    private final ModelConfig config;
    private final HttpClient client;

    public ChatCompletionClient(ModelConfig config) {
        this.config = config;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeoutSeconds()))
                .build();
    }

    public String create(List<Map<String, Object>> messages) throws Exception {
        if (!config.configured()) {
            throw new IllegalStateException("Model gateway is not configured. Check .env.");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", config.model());
        payload.put("messages", messages);
        payload.put("temperature", config.temperature());
        payload.put("stream", config.stream());
        if (config.maxTokens() > 0) {
            payload.put("max_tokens", config.maxTokens());
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.chatCompletionsUrl()))
                .timeout(Duration.ofSeconds(config.timeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(Json.stringify(payload), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Model gateway HTTP " + response.statusCode() + ": " + response.body());
        }
        return config.stream() ? parseSse(response.body()) : parseMessage(response.body());
    }

    private static String parseMessage(String body) {
        Map<String, Object> data = Json.asObject(Json.parse(body));
        List<Object> choices = Json.asList(data.get("choices"));
        if (choices.isEmpty()) {
            return "";
        }
        Map<String, Object> choice = Json.asObject(choices.get(0));
        Map<String, Object> message = Json.asObject(choice.get("message"));
        return Json.string(message.get("content"));
    }

    private static String parseSse(String body) {
        StringBuilder out = new StringBuilder();
        for (String line : body.split("\\R")) {
            line = line.trim();
            if (!line.startsWith("data:")) {
                continue;
            }
            String data = line.substring("data:".length()).trim();
            if (data.equals("[DONE]") || data.isBlank()) {
                continue;
            }
            try {
                Map<String, Object> payload = Json.asObject(Json.parse(data));
                List<Object> choices = Json.asList(payload.get("choices"));
                if (choices.isEmpty()) {
                    continue;
                }
                Map<String, Object> choice = Json.asObject(choices.get(0));
                Map<String, Object> delta = Json.asObject(choice.get("delta"));
                out.append(Json.string(delta.get("content")));
            } catch (Exception ignored) {
            }
        }
        return out.toString();
    }

    public static List<Map<String, Object>> messages(String system, String user) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", system));
        messages.add(Map.of("role", "user", "content", user));
        return messages;
    }
}
