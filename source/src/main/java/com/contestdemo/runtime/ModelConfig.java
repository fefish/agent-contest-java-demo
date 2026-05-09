package com.contestdemo.runtime;

public record ModelConfig(
        String chatCompletionsUrl,
        String apiKey,
        String model,
        int timeoutSeconds,
        double temperature,
        int maxTokens,
        boolean stream
) {
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
                env.bool("AGENT_DEMO_STREAM", false)
        );
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
}
