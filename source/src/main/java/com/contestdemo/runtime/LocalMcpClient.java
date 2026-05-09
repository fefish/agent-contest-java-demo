package com.contestdemo.runtime;

import com.contestdemo.toolkits.MainMcp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LocalMcpClient {
    private final MainMcp mcp = new MainMcp();

    public List<String> toolNames() {
        return mcp.toolNames();
    }

    public List<String> agentNames() {
        return mcp.agentNames();
    }

    public List<String> skillNames() {
        return mcp.skillNames();
    }

    public List<Map<String, Object>> skillSummaries() {
        return mcp.skillSummaries();
    }

    public List<Map<String, Object>> listOpenAiTools() {
        return mcp.tools().stream().map(McpTool::toOpenAiTool).collect(Collectors.toList());
    }

    public Object callTool(String name, Map<String, Object> args, AgentContext context) throws Exception {
        return mcp.call(name, args, context);
    }
}
