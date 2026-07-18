package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrThinking;
import io.github.intisy.ai.ir.IrTool;
import io.github.intisy.ai.ir.IrToolChoice;
import io.github.intisy.ai.ir.IrUsage;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@code Map} conversions for the small leaf types shared by request/response/streaming. */
final class CommonJson {
    private CommonJson() {
    }

    static Map<String, Object> toMap(IrUsage u) {
        if (u == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("inputTokens", u.inputTokens);
        m.put("outputTokens", u.outputTokens);
        if (u.cacheReadInputTokens != null) m.put("cacheReadInputTokens", u.cacheReadInputTokens);
        if (u.cacheCreationInputTokens != null) m.put("cacheCreationInputTokens", u.cacheCreationInputTokens);
        if (u.reasoningTokens != null) m.put("reasoningTokens", u.reasoningTokens);
        if (u.totalTokens != null) m.put("totalTokens", u.totalTokens);
        return m;
    }

    static IrUsage usageFromMap(Object o) {
        Map<String, Object> m = JsonUtil.asMap(o);
        if (m == null) return null;
        IrUsage u = new IrUsage();
        u.inputTokens = JsonUtil.asInt(m.get("inputTokens"));
        u.outputTokens = JsonUtil.asInt(m.get("outputTokens"));
        u.cacheReadInputTokens = JsonUtil.asInt(m.get("cacheReadInputTokens"));
        u.cacheCreationInputTokens = JsonUtil.asInt(m.get("cacheCreationInputTokens"));
        u.reasoningTokens = JsonUtil.asInt(m.get("reasoningTokens"));
        u.totalTokens = JsonUtil.asInt(m.get("totalTokens"));
        return u;
    }

    static Map<String, Object> toMap(IrThinking t) {
        if (t == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("enabled", t.enabled);
        if (t.budgetTokens != null) m.put("budgetTokens", t.budgetTokens);
        return m;
    }

    static IrThinking thinkingFromMap(Object o) {
        Map<String, Object> m = JsonUtil.asMap(o);
        if (m == null) return null;
        IrThinking t = new IrThinking();
        Boolean enabled = JsonUtil.asBoolean(m.get("enabled"));
        t.enabled = enabled != null && enabled;
        t.budgetTokens = JsonUtil.asInt(m.get("budgetTokens"));
        return t;
    }

    static Map<String, Object> toMap(IrTool t) {
        if (t == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", t.name);
        if (t.description != null) m.put("description", t.description);
        m.put("inputSchema", t.inputSchema);
        if (t.extensions != null) m.put("extensions", t.extensions);
        return m;
    }

    @SuppressWarnings("unchecked")
    static IrTool toolFromMap(Object o) {
        Map<String, Object> m = JsonUtil.asMap(o);
        if (m == null) return null;
        IrTool t = new IrTool();
        t.name = JsonUtil.asString(m.get("name"));
        t.description = JsonUtil.asString(m.get("description"));
        t.inputSchema = m.get("inputSchema");
        t.extensions = (Map<String, Object>) m.get("extensions");
        return t;
    }

    static Map<String, Object> toMap(IrToolChoice c) {
        if (c == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", c.type);
        if (c.name != null) m.put("name", c.name);
        if (c.extensions != null) m.put("extensions", c.extensions);
        return m;
    }

    @SuppressWarnings("unchecked")
    static IrToolChoice toolChoiceFromMap(Object o) {
        Map<String, Object> m = JsonUtil.asMap(o);
        if (m == null) return null;
        IrToolChoice c = new IrToolChoice();
        c.type = JsonUtil.asString(m.get("type"));
        c.name = JsonUtil.asString(m.get("name"));
        c.extensions = (Map<String, Object>) m.get("extensions");
        return c;
    }
}
