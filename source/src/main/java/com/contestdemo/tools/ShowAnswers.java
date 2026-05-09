package com.contestdemo.tools;

import com.contestdemo.runtime.Json;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ShowAnswers {
    public static void main(String[] args) throws Exception {
        Path resultPath = Path.of(args.length > 0 ? args[0] : "source/outputs/result.json");
        List<Object> results = Json.asList(Json.parse(Files.readString(resultPath, StandardCharsets.UTF_8)));
        System.out.println("result: " + resultPath.toAbsolutePath());
        System.out.println("items: " + results.size());
        System.out.println();
        int index = 0;
        for (Object itemObject : results) {
            index++;
            Map<String, Object> item = Json.asObject(itemObject);
            System.out.println("[" + index + "] " + item.getOrDefault("id", index)
                    + "  status=" + item.getOrDefault("status", "unknown")
                    + "  model_path=" + item.getOrDefault("model_status", "unknown")
                    + "  confidence=" + item.getOrDefault("confidence", ""));
            System.out.println("used_tools: " + join(item.get("used_tools")));
            System.out.println("used_skills: " + join(item.get("used_skills")));
            System.out.println("answer:");
            System.out.println(indent(Json.string(item.get("answer"))));
            System.out.println("-".repeat(80));
        }
    }

    private static String join(Object value) {
        List<Object> list = Json.asList(value);
        if (list.isEmpty()) {
            return "-";
        }
        return String.join(", ", list.stream().map(Json::string).collect(Collectors.toList()));
    }

    private static String indent(String text) {
        StringBuilder out = new StringBuilder();
        for (String line : text.split("\\R", -1)) {
            out.append("  ").append(line).append("\n");
        }
        return out.toString();
    }
}
