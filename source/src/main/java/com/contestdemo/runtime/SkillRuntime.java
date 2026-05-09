package com.contestdemo.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SkillRuntime {
    private static final Path SKILLS_DIR = Path.of("source", "solution", "skills");
    private final Map<String, SkillPackage> skills = new LinkedHashMap<>();

    public SkillRuntime() {
        reload();
    }

    public void reload() {
        skills.clear();
        if (!Files.isDirectory(SKILLS_DIR)) {
            return;
        }
        try {
            try (Stream<Path> stream = Files.list(SKILLS_DIR)) {
                List<Path> skillDirs = stream.filter(Files::isDirectory).sorted().collect(Collectors.toList());
                for (Path dir : skillDirs) {
                    Path skillMd = dir.resolve("SKILL.md");
                    if (!Files.exists(skillMd)) {
                        continue;
                    }
                    String raw = Files.readString(skillMd, StandardCharsets.UTF_8);
                    Map<String, String> frontmatter = frontmatter(raw);
                    Map<String, Object> metadata = readJsonObject(dir.resolve("skill.json"));
                    String name = firstNonBlank(frontmatter.get("name"), Json.string(metadata.get("name")), dir.getFileName().toString());
                    String description = firstNonBlank(frontmatter.get("description"), Json.string(metadata.get("description")), name);
                    String entrypoint = Json.string(metadata.get("entrypoint"));
                    int timeout = (int) Json.number(metadata.get("timeout_seconds"), 20);
                    skills.put(name, new SkillPackage(name, description, dir, raw, metadata, entrypoint, timeout, resources(dir)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to load skills", e);
        }
    }

    public List<String> skillNames() {
        return new ArrayList<>(skills.keySet());
    }

    public List<Map<String, Object>> summaries() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SkillPackage skill : skills.values()) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("name", skill.name);
            summary.put("description", skill.description);
            summary.put("has_executable", !skill.entrypoint.isBlank());
            summary.put("resources", skill.resources);
            result.add(summary);
        }
        return result;
    }

    public String loadSkill(String name, int maxChars) {
        SkillPackage skill = resolve(name);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", skill.name);
        payload.put("description", skill.description);
        payload.put("skill_dir", skill.dir.toString());
        payload.put("resources", skill.resources);
        payload.put("has_executable", !skill.entrypoint.isBlank());
        payload.put("entrypoint", skill.entrypoint);
        payload.put("input_schema", skill.metadata.getOrDefault("input_schema", Map.of()));
        payload.put("usage", "Follow SKILL.md. If execution is needed, call skill_run with arguments matching input_schema.");
        payload.put("SKILL.md", truncate(skill.rawMarkdown, maxChars));
        return Json.stringify(payload);
    }

    public String readResource(String name, String path, int maxChars) throws IOException {
        SkillPackage skill = resolve(name);
        Path relative = Path.of(path);
        if (relative.isAbsolute() || path.contains("..")) {
            throw new IllegalArgumentException("resource path must stay inside the skill package");
        }
        if (relative.getNameCount() == 0 || !(relative.getName(0).toString().equals("references") || relative.getName(0).toString().equals("assets"))) {
            throw new IllegalArgumentException("resource path must be under references/ or assets/");
        }
        Path target = skill.dir.resolve(relative).normalize();
        if (!target.startsWith(skill.dir.normalize())) {
            throw new IllegalArgumentException("resource path escapes skill package");
        }
        return truncate(Files.readString(target, StandardCharsets.UTF_8), maxChars);
    }

    public String runSkill(String name, Map<String, Object> arguments) throws Exception {
        SkillPackage skill = resolve(name);
        if (skill.entrypoint.isBlank()) {
            throw new IllegalStateException("skill has no executable entrypoint: " + skill.name);
        }
        Path script = skill.dir.resolve(skill.entrypoint).normalize();
        if (!script.startsWith(skill.dir.normalize())) {
            throw new IllegalArgumentException("skill entrypoint escapes skill package");
        }
        Process process = new ProcessBuilder(javaCommand(), script.toAbsolutePath().toString())
                .redirectErrorStream(false)
                .start();
        process.getOutputStream().write(Json.stringify(arguments).getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().close();
        boolean finished = process.waitFor(Duration.ofSeconds(skill.timeoutSeconds).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("skill timed out: " + skill.name);
        }
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (process.exitValue() != 0) {
            throw new RuntimeException("skill failed: " + stderr);
        }
        return stdout;
    }

    private SkillPackage resolve(String name) {
        if (skills.containsKey(name)) {
            return skills.get(name);
        }
        for (Map.Entry<String, SkillPackage> entry : skills.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name) || entry.getValue().dir.getFileName().toString().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("unknown skill: " + name);
    }

    private static Map<String, Object> readJsonObject(Path path) {
        if (!Files.exists(path)) {
            return new LinkedHashMap<>();
        }
        try {
            return Json.asObject(Json.parse(Files.readString(path, StandardCharsets.UTF_8)));
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private static Map<String, String> frontmatter(String markdown) {
        Map<String, String> result = new LinkedHashMap<>();
        if (!markdown.startsWith("---\n")) {
            return result;
        }
        int end = markdown.indexOf("\n---\n", 4);
        if (end < 0) {
            return result;
        }
        for (String line : markdown.substring(4, end).split("\n")) {
            int sep = line.indexOf(':');
            if (sep > 0) {
                result.put(line.substring(0, sep).trim(), line.substring(sep + 1).trim().replace("\"", ""));
            }
        }
        return result;
    }

    private static List<String> resources(Path dir) {
        List<String> result = new ArrayList<>();
        for (String root : List.of("references", "assets")) {
            Path base = dir.resolve(root);
            if (!Files.isDirectory(base)) {
                continue;
            }
            try (Stream<Path> stream = Files.walk(base)) {
                stream.filter(Files::isRegularFile)
                        .sorted(Comparator.naturalOrder())
                        .forEach(path -> result.add(dir.relativize(path).toString().replace('\\', '/')));
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    private static String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "\n[truncated]";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String javaCommand() {
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        return Files.isExecutable(java) ? java.toString() : "java";
    }

    private static final class SkillPackage {
        private final String name;
        private final String description;
        private final Path dir;
        private final String rawMarkdown;
        private final Map<String, Object> metadata;
        private final String entrypoint;
        private final int timeoutSeconds;
        private final List<String> resources;

        private SkillPackage(
                String name,
                String description,
                Path dir,
                String rawMarkdown,
                Map<String, Object> metadata,
                String entrypoint,
                int timeoutSeconds,
                List<String> resources
        ) {
            this.name = name;
            this.description = description;
            this.dir = dir;
            this.rawMarkdown = rawMarkdown;
            this.metadata = metadata;
            this.entrypoint = entrypoint;
            this.timeoutSeconds = timeoutSeconds;
            this.resources = resources;
        }
    }
}
