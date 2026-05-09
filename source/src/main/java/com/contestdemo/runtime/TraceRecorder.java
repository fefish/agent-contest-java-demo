package com.contestdemo.runtime;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TraceRecorder {
    private final List<Map<String, Object>> events = new ArrayList<>();

    public void add(String type, Object... pairs) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("time", OffsetDateTime.now(ZoneOffset.UTC).toString());
        event.put("type", type);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            event.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        events.add(event);
    }

    public List<Map<String, Object>> events() {
        return events;
    }

    public List<String> usedToolNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Map<String, Object> event : events) {
            if ("tool_call".equals(event.get("type"))) {
                names.add(Json.string(event.get("name")));
            }
        }
        return new ArrayList<>(names);
    }

    public List<String> usedSkillNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Map<String, Object> event : events) {
            if ("tool_call".equals(event.get("type"))) {
                String tool = Json.string(event.get("name"));
                if (tool.equals("skill_load") || tool.equals("skill_read_resource") || tool.equals("skill_run")) {
                    Object args = event.get("args");
                    if (args instanceof Map<?, ?> map && map.get("name") != null) {
                        names.add(Json.string(map.get("name")));
                    }
                }
            }
        }
        return new ArrayList<>(names);
    }
}
