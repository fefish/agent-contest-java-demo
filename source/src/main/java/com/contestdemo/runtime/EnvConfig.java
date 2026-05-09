package com.contestdemo.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class EnvConfig {
    private final Map<String, String> values = new HashMap<>();

    private EnvConfig() {
        values.putAll(System.getenv());
        Path envPath = Path.of(".env");
        if (Files.exists(envPath)) {
            try {
                for (String rawLine : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
                    String line = rawLine.trim();
                    if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                        continue;
                    }
                    String[] parts = line.split("=", 2);
                    values.putIfAbsent(parts[0].trim(), stripQuotes(parts[1].trim()));
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static EnvConfig load() {
        return new EnvConfig();
    }

    public String get(String name, String defaultValue) {
        String value = values.get(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public boolean bool(String name, boolean defaultValue) {
        String value = values.get(name);
        if (value == null) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("1")
                || normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("y")
                || normalized.equals("on");
    }

    public int integer(String name, int defaultValue) {
        try {
            return Integer.parseInt(get(name, String.valueOf(defaultValue)));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    public double decimal(String name, double defaultValue) {
        try {
            return Double.parseDouble(get(name, String.valueOf(defaultValue)));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
