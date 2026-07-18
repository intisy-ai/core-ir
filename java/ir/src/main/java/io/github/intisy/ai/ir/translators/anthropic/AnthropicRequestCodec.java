package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrMessage;
import io.github.intisy.ai.ir.IrRequest;
import io.github.intisy.ai.ir.IrThinking;
import io.github.intisy.ai.ir.IrTool;
import io.github.intisy.ai.ir.IrToolChoice;
import io.github.intisy.ai.ir.TextBlock;
import io.github.intisy.ai.ir.spi.JsonCodec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Anthropic Messages API request {@code Map} tree <-> {@link IrRequest}. {@code system} and each
 * message's {@code content} may be a plain string or a block array on the wire; this codec
 * remembers which shape it saw (a {@code $}-prefixed marker in the owning object's
 * {@code extensions}) so encode reproduces the same shape rather than always widening to an
 * array. Any request/message/tool/tool_choice field with no neutral IR home (e.g. {@code
 * container}, {@code service_tier}, {@code disable_parallel_tool_use}) round-trips verbatim
 * through the corresponding {@code extensions} bag.
 */
final class AnthropicRequestCodec {
    private AnthropicRequestCodec() {
    }

    private static final String EXT_SYSTEM_IS_STRING = "$systemIsString";
    private static final String EXT_THINKING_TYPE = "$thinkingType";
    private static final String EXT_CONTENT_IS_STRING = "$contentIsString";

    private static final Set<String> TOP_LEVEL_KNOWN_KEYS = new HashSet<>(Arrays.asList(
            "model", "system", "messages", "tools", "tool_choice", "max_tokens", "temperature",
            "top_p", "top_k", "stop_sequences", "stream", "thinking", "metadata"));

    static IrRequest decodeRequest(JsonCodec json, String wireJson) {
        Map<String, Object> root = AnthropicJsonUtil.asMap(json.parse(wireJson));
        IrRequest r = new IrRequest();
        if (root == null) return r;

        r.model = AnthropicJsonUtil.asString(root.get("model"));
        r.maxTokens = AnthropicJsonUtil.asInt(root.get("max_tokens"));
        r.temperature = AnthropicJsonUtil.asDouble(root.get("temperature"));
        r.topP = AnthropicJsonUtil.asDouble(root.get("top_p"));
        r.topK = AnthropicJsonUtil.asInt(root.get("top_k"));
        r.stopSequences = decodeStringList(root.get("stop_sequences"));
        Boolean stream = AnthropicJsonUtil.asBoolean(root.get("stream"));
        r.stream = stream != null && stream;
        r.metadata = AnthropicJsonUtil.asMap(root.get("metadata"));

        decodeSystem(root.get("system"), r);
        r.messages = decodeMessages(root.get("messages"));
        r.tools = decodeTools(root.get("tools"));
        Map<String, Object> toolChoiceMap = AnthropicJsonUtil.asMap(root.get("tool_choice"));
        if (toolChoiceMap != null) r.toolChoice = decodeToolChoice(toolChoiceMap);
        decodeThinking(root.get("thinking"), r);

        for (Map.Entry<String, Object> e : root.entrySet()) {
            if (!TOP_LEVEL_KNOWN_KEYS.contains(e.getKey())) {
                putRequestExtension(r, e.getKey(), e.getValue());
            }
        }
        return r;
    }

    static String encodeRequest(JsonCodec json, IrRequest r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("model", r.model);
        encodeSystem(r, m);
        m.put("messages", encodeMessages(r.messages));
        if (r.tools != null) m.put("tools", encodeTools(r.tools));
        if (r.toolChoice != null) m.put("tool_choice", encodeToolChoice(r.toolChoice));
        if (r.maxTokens != null) m.put("max_tokens", r.maxTokens);
        if (r.temperature != null) m.put("temperature", r.temperature);
        if (r.topP != null) m.put("top_p", r.topP);
        if (r.topK != null) m.put("top_k", r.topK);
        if (r.stopSequences != null) m.put("stop_sequences", new ArrayList<Object>(r.stopSequences));
        m.put("stream", r.stream);
        if (r.thinking != null) encodeThinking(r, m);
        if (r.metadata != null) m.put("metadata", r.metadata);
        encodeLeftoverExtensions(r, m);
        return json.stringify(m);
    }

    private static void decodeSystem(Object sysRaw, IrRequest r) {
        if (sysRaw instanceof String) {
            r.system = AnthropicBlockCodec.wrapStringAsBlocks((String) sysRaw);
            putRequestExtension(r, EXT_SYSTEM_IS_STRING, Boolean.TRUE);
        } else if (sysRaw != null) {
            r.system = AnthropicBlockCodec.decodeBlockList(sysRaw);
        }
    }

    private static void encodeSystem(IrRequest r, Map<String, Object> m) {
        if (r.system == null) return;
        boolean wasString = r.extensions != null && Boolean.TRUE.equals(r.extensions.get(EXT_SYSTEM_IS_STRING));
        if (wasString && AnthropicBlockCodec.isPlainWrappedText(r.system)) {
            m.put("system", ((TextBlock) r.system.get(0)).text);
        } else {
            m.put("system", AnthropicBlockCodec.encodeBlockList(r.system));
        }
    }

    private static void decodeThinking(Object thinkingRaw, IrRequest r) {
        Map<String, Object> tm = AnthropicJsonUtil.asMap(thinkingRaw);
        if (tm == null) return;
        String thinkingType = AnthropicJsonUtil.asString(tm.get("type"));
        IrThinking thinking = new IrThinking();
        thinking.enabled = !"disabled".equals(thinkingType);
        thinking.budgetTokens = AnthropicJsonUtil.asInt(tm.get("budget_tokens"));
        r.thinking = thinking;
        putRequestExtension(r, EXT_THINKING_TYPE, thinkingType);
    }

    private static void encodeThinking(IrRequest r, Map<String, Object> m) {
        Map<String, Object> tm = new LinkedHashMap<>();
        String thinkingType = r.extensions == null ? null : AnthropicJsonUtil.asString(r.extensions.get(EXT_THINKING_TYPE));
        if (thinkingType == null) thinkingType = r.thinking.enabled ? "enabled" : "disabled";
        tm.put("type", thinkingType);
        if (r.thinking.budgetTokens != null) tm.put("budget_tokens", r.thinking.budgetTokens);
        m.put("thinking", tm);
    }

    private static List<IrMessage> decodeMessages(Object raw) {
        List<Object> list = AnthropicJsonUtil.asList(raw);
        if (list == null) return null;
        List<IrMessage> out = new ArrayList<>();
        for (Object item : list) out.add(decodeMessage(AnthropicJsonUtil.asMap(item)));
        return out;
    }

    private static IrMessage decodeMessage(Map<String, Object> mm) {
        if (mm == null) return null;
        IrMessage msg = new IrMessage();
        msg.role = AnthropicJsonUtil.asString(mm.get("role"));
        Object contentRaw = mm.get("content");
        if (contentRaw instanceof String) {
            msg.content = AnthropicBlockCodec.wrapStringAsBlocks((String) contentRaw);
            putMessageExtension(msg, EXT_CONTENT_IS_STRING, Boolean.TRUE);
        } else {
            msg.content = AnthropicBlockCodec.decodeBlockList(contentRaw);
        }
        for (Map.Entry<String, Object> e : mm.entrySet()) {
            if (!"role".equals(e.getKey()) && !"content".equals(e.getKey())) {
                putMessageExtension(msg, e.getKey(), e.getValue());
            }
        }
        return msg;
    }

    private static List<Object> encodeMessages(List<IrMessage> messages) {
        List<Object> out = new ArrayList<>();
        if (messages == null) return out;
        for (IrMessage msg : messages) out.add(encodeMessage(msg));
        return out;
    }

    private static Map<String, Object> encodeMessage(IrMessage msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", msg.role);
        boolean wasString = msg.extensions != null && Boolean.TRUE.equals(msg.extensions.get(EXT_CONTENT_IS_STRING));
        if (wasString && AnthropicBlockCodec.isPlainWrappedText(msg.content)) {
            m.put("content", ((TextBlock) msg.content.get(0)).text);
        } else {
            m.put("content", AnthropicBlockCodec.encodeBlockList(msg.content));
        }
        if (msg.extensions != null) {
            for (Map.Entry<String, Object> e : msg.extensions.entrySet()) {
                if (!e.getKey().startsWith("$")) m.put(e.getKey(), e.getValue());
            }
        }
        return m;
    }

    private static List<IrTool> decodeTools(Object raw) {
        List<Object> list = AnthropicJsonUtil.asList(raw);
        if (list == null) return null;
        List<IrTool> out = new ArrayList<>();
        for (Object item : list) out.add(decodeTool(AnthropicJsonUtil.asMap(item)));
        return out;
    }

    private static IrTool decodeTool(Map<String, Object> tm) {
        if (tm == null) return null;
        IrTool t = new IrTool();
        t.name = AnthropicJsonUtil.asString(tm.get("name"));
        t.description = AnthropicJsonUtil.asString(tm.get("description"));
        t.inputSchema = tm.get("input_schema");
        for (Map.Entry<String, Object> e : tm.entrySet()) {
            String k = e.getKey();
            if (!"name".equals(k) && !"description".equals(k) && !"input_schema".equals(k)) {
                if (t.extensions == null) t.extensions = new LinkedHashMap<>();
                t.extensions.put(k, e.getValue());
            }
        }
        return t;
    }

    private static List<Object> encodeTools(List<IrTool> tools) {
        List<Object> out = new ArrayList<>();
        for (IrTool t : tools) out.add(encodeTool(t));
        return out;
    }

    private static Map<String, Object> encodeTool(IrTool t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", t.name);
        if (t.description != null) m.put("description", t.description);
        m.put("input_schema", t.inputSchema);
        if (t.extensions != null) {
            for (Map.Entry<String, Object> e : t.extensions.entrySet()) m.put(e.getKey(), e.getValue());
        }
        return m;
    }

    private static IrToolChoice decodeToolChoice(Map<String, Object> tc) {
        IrToolChoice c = new IrToolChoice();
        c.type = AnthropicJsonUtil.asString(tc.get("type"));
        c.name = AnthropicJsonUtil.asString(tc.get("name"));
        for (Map.Entry<String, Object> e : tc.entrySet()) {
            String k = e.getKey();
            if (!"type".equals(k) && !"name".equals(k)) {
                if (c.extensions == null) c.extensions = new LinkedHashMap<>();
                c.extensions.put(k, e.getValue());
            }
        }
        return c;
    }

    private static Map<String, Object> encodeToolChoice(IrToolChoice c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", c.type);
        if (c.name != null) m.put("name", c.name);
        if (c.extensions != null) {
            for (Map.Entry<String, Object> e : c.extensions.entrySet()) m.put(e.getKey(), e.getValue());
        }
        return m;
    }

    private static List<String> decodeStringList(Object raw) {
        List<Object> list = AnthropicJsonUtil.asList(raw);
        if (list == null) return null;
        List<String> out = new ArrayList<>();
        for (Object item : list) out.add(String.valueOf(item));
        return out;
    }

    private static void putRequestExtension(IrRequest r, String key, Object value) {
        if (r.extensions == null) r.extensions = new LinkedHashMap<>();
        r.extensions.put(key, value);
    }

    private static void putMessageExtension(IrMessage msg, String key, Object value) {
        if (msg.extensions == null) msg.extensions = new LinkedHashMap<>();
        msg.extensions.put(key, value);
    }

    private static void encodeLeftoverExtensions(IrRequest r, Map<String, Object> m) {
        if (r.extensions == null) return;
        for (Map.Entry<String, Object> e : r.extensions.entrySet()) {
            if (!e.getKey().startsWith("$")) m.put(e.getKey(), e.getValue());
        }
    }
}
