package com.contestdemo.tools;

import com.contestdemo.runtime.ChatCompletionClient;
import com.contestdemo.runtime.EnvConfig;
import com.contestdemo.runtime.ModelConfig;

public final class TestModel {
    public static void main(String[] args) throws Exception {
        EnvConfig env = EnvConfig.load();
        ModelConfig config = ModelConfig.fromEnv(env);
        System.out.println("model: " + (config.model().isBlank() ? "not configured" : config.model()));
        System.out.println("url: " + (config.chatCompletionsUrl().isBlank() ? "not configured" : config.chatCompletionsUrl()));
        System.out.println("api key configured: " + (!config.apiKey().isBlank()));
        if (!config.configured()) {
            System.out.println("response: model gateway is not configured");
            return;
        }
        String response = new ChatCompletionClient(config).create(ChatCompletionClient.messages(
                "You are a concise test assistant.",
                "请只回答 OK。"
        ));
        System.out.println("response:");
        System.out.println(response);
    }
}
