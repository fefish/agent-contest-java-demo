package com.contestdemo.runtime;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AgentRegistry {
    private static final Path AGENTS_DIR = Path.of("source", "solution", "agents");
    private final Map<String, AgentPackage> agents = new LinkedHashMap<>();

    public AgentRegistry() {
        reload();
    }

    public void reload() {
        agents.clear();
        if (!Files.isDirectory(AGENTS_DIR)) {
            return;
        }
        try (var stream = Files.list(AGENTS_DIR)) {
            for (Path dir : stream.filter(Files::isDirectory).sorted(Comparator.naturalOrder()).toList()) {
                Map<String, Object> metadata = readJson(dir.resolve("agent.json"));
                String name = Json.string(metadata.getOrDefault("name", dir.getFileName().toString()));
                String entrypoint = Json.string(metadata.getOrDefault("entrypoint", "scripts/Run.java"));
                int timeout = (int) Json.number(metadata.get("timeout_seconds"), 20);
                agents.put(name, new AgentPackage(name, dir, entrypoint, timeout));
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to load agents", e);
        }
    }

    public List<String> names() {
        return new ArrayList<>(agents.keySet());
    }

    public String run(String agentName, String task, String contextText) throws Exception {
        AgentPackage agent = agents.get(agentName);
        if (agent == null) {
            throw new IllegalArgumentException("unknown agent: " + agentName);
        }
        Path script = agent.dir.resolve(agent.entrypoint).normalize();
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("task", task);
        input.put("context_text", contextText);
        Process process = new ProcessBuilder(javaCommand(), script.toAbsolutePath().toString())
                .start();
        process.getOutputStream().write(Json.stringify(input).getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().close();
        boolean finished = process.waitFor(Duration.ofSeconds(agent.timeoutSeconds).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("agent timed out: " + agent.name);
        }
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (process.exitValue() != 0) {
            throw new RuntimeException("agent failed: " + stderr);
        }
        return stdout;
    }

    private static Map<String, Object> readJson(Path path) {
        if (!Files.exists(path)) {
            return new LinkedHashMap<>();
        }
        try {
            return Json.asObject(Json.parse(Files.readString(path, StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private static String javaCommand() {
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        return Files.isExecutable(java) ? java.toString() : "java";
    }

    private record AgentPackage(String name, Path dir, String entrypoint, int timeoutSeconds) {
    }
}
