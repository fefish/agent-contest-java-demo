package com.contestdemo;

import com.contestdemo.runtime.BatchRunner;

import java.nio.file.Path;
import java.util.Map;

public final class Main {
    public static void main(String[] args) throws Exception {
        Map<String, String> parsed = parseArgs(args);
        Path question = Path.of(parsed.getOrDefault("--question", "source/examples/questions.json"));
        Path output = Path.of(parsed.getOrDefault("--output", "source/outputs/result.json"));
        var results = new BatchRunner().runFile(question, output);
        long ok = results.stream().filter(item -> "success".equals(item.get("status"))).count();
        System.out.println("done: " + ok + "/" + results.size() + " succeeded");
        System.out.println("result saved to: " + output.toAbsolutePath());
    }

    private static Map<String, String> parseArgs(String[] args) {
        java.util.LinkedHashMap<String, String> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) {
            result.put(args[i], args[i + 1]);
        }
        return result;
    }
}
