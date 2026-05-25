package com.contestdemo.runtime;

public final class ModelConfig {
    private final String chatCompletionsUrl;
    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final double temperature;
    private final int maxTokens;
    private final boolean stream;
    private final String packageId;

    public ModelConfig(
            String chatCompletionsUrl,
            String apiKey,
            String model,
            int timeoutSeconds,
            double temperature,
            int maxTokens,
            boolean stream,
            String packageId
    ) {
        this.chatCompletionsUrl = chatCompletionsUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.stream = stream;
        this.packageId = packageId;
    }

    public static ModelConfig fromEnv(EnvConfig env) {
        String chatUrl = env.get("MODEL_CHAT_COMPLETIONS_URL", "");
        String baseUrl = env.get("MODEL_BASE_URL", "");
        if (chatUrl.isBlank() && !baseUrl.isBlank()) {
            chatUrl = stripSlash(baseUrl) + "/chat/completions";
        }
        if (!chatUrl.isBlank() && !stripSlash(chatUrl).endsWith("/chat/completions")) {
            chatUrl = stripSlash(chatUrl) + "/chat/completions";
        }
        return new ModelConfig(
                chatUrl,
                env.get("MODEL_API_KEY", ""),
                env.get("MODEL_NAME", ""),
                env.integer("AGENT_DEMO_TIMEOUT_SECONDS", 60),
                env.decimal("AGENT_DEMO_TEMPERATURE", 0.2),
                env.integer("AGENT_DEMO_MAX_TOKENS", 0),
                env.bool("AGENT_DEMO_STREAM", false),
                packageId(env)
        );
    }

    public String chatCompletionsUrl() {
        return chatCompletionsUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public String model() {
        return model;
    }

    public int timeoutSeconds() {
        return timeoutSeconds;
    }

    public double temperature() {
        return temperature;
    }

    public int maxTokens() {
        return maxTokens;
    }

    public boolean stream() {
        return stream;
    }

    public String packageId() {
        return packageId;
    }

    public boolean configured() {
        return !chatCompletionsUrl.isBlank() && !apiKey.isBlank() && !model.isBlank();
    }

    private static String stripSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String packageId(EnvConfig env) {
        String value = env.get("PACKAGE_ID", "");
        if (value.isBlank()) {
            value = env.get("packageId", "");
        }
        return value;
    }
}
