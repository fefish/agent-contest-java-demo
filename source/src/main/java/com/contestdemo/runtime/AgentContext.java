package com.contestdemo.runtime;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class AgentContext {
    public final Map<String, Object> question;
    public final Path questionDir;
    public final List<Path> allowedFilePaths;
    public final LocalMcpClient mcp;
    public final TraceRecorder trace;

    public AgentContext(
            Map<String, Object> question,
            Path questionDir,
            List<Path> allowedFilePaths,
            LocalMcpClient mcp,
            TraceRecorder trace
    ) {
        this.question = question;
        this.questionDir = questionDir;
        this.allowedFilePaths = allowedFilePaths;
        this.mcp = mcp;
        this.trace = trace;
    }

    public Object callTool(String name, Map<String, Object> args) throws Exception {
        trace.add("tool_call", "name", name, "args", args);
        Object result = mcp.callTool(name, args, this);
        trace.add("tool_result", "name", name, "content", result == null ? "" : String.valueOf(result));
        return result;
    }

    public List<Map<String, Object>> availableSkills() {
        return mcp.skillSummaries();
    }
}
