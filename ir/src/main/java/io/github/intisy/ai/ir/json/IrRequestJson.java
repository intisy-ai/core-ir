package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.IrMessage;
import io.github.intisy.ai.ir.IrRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link IrRequest} (and its {@link IrMessage} children) <-> {@code Map} tree. */
final class IrRequestJson {
    private IrRequestJson() {
    }

    static Map<String, Object> toMap(IrRequest r) {
        if (r == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("model", r.model);
        if (r.system != null) m.put("system", BlockJson.toMapList(r.system));
        m.put("messages", messagesToMapList(r.messages));
        if (r.tools != null) m.put("tools", toolsToMapList(r.tools));
        if (r.toolChoice != null) m.put("toolChoice", CommonJson.toMap(r.toolChoice));
        if (r.maxTokens != null) m.put("maxTokens", r.maxTokens);
        if (r.temperature != null) m.put("temperature", r.temperature);
        if (r.topP != null) m.put("topP", r.topP);
        if (r.topK != null) m.put("topK", r.topK);
        if (r.stopSequences != null) m.put("stopSequences", new ArrayList<Object>(r.stopSequences));
        m.put("stream", r.stream);
        if (r.thinking != null) m.put("thinking", CommonJson.toMap(r.thinking));
        if (r.metadata != null) m.put("metadata", r.metadata);
        if (r.extensions != null) m.put("extensions", r.extensions);
        return m;
    }

    @SuppressWarnings("unchecked")
    static IrRequest fromMap(Map<String, Object> m) {
        if (m == null) return null;
        IrRequest r = new IrRequest();
        r.model = JsonUtil.asString(m.get("model"));
        r.system = BlockJson.fromMapList(m.get("system"));
        r.messages = messagesFromMapList(m.get("messages"));
        r.tools = toolsFromMapList(m.get("tools"));
        r.toolChoice = CommonJson.toolChoiceFromMap(m.get("toolChoice"));
        r.maxTokens = JsonUtil.asInt(m.get("maxTokens"));
        r.temperature = JsonUtil.asDouble(m.get("temperature"));
        r.topP = JsonUtil.asDouble(m.get("topP"));
        r.topK = JsonUtil.asInt(m.get("topK"));
        r.stopSequences = stringListFromMap(m.get("stopSequences"));
        Boolean stream = JsonUtil.asBoolean(m.get("stream"));
        r.stream = stream != null && stream;
        r.thinking = CommonJson.thinkingFromMap(m.get("thinking"));
        r.metadata = (Map<String, Object>) m.get("metadata");
        r.extensions = (Map<String, Object>) m.get("extensions");
        return r;
    }

    private static List<Object> messagesToMapList(List<IrMessage> messages) {
        List<Object> out = new ArrayList<>();
        if (messages == null) return out;
        for (IrMessage msg : messages) out.add(messageToMap(msg));
        return out;
    }

    private static Map<String, Object> messageToMap(IrMessage msg) {
        if (msg == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", msg.role);
        m.put("content", BlockJson.toMapList(msg.content));
        if (msg.extensions != null) m.put("extensions", msg.extensions);
        return m;
    }

    @SuppressWarnings("unchecked")
    private static List<IrMessage> messagesFromMapList(Object o) {
        List<Object> list = JsonUtil.asList(o);
        if (list == null) return null;
        List<IrMessage> out = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = JsonUtil.asMap(item);
            if (m == null) continue;
            IrMessage msg = new IrMessage();
            msg.role = JsonUtil.asString(m.get("role"));
            msg.content = BlockJson.fromMapList(m.get("content"));
            msg.extensions = (Map<String, Object>) m.get("extensions");
            out.add(msg);
        }
        return out;
    }

    private static List<Object> toolsToMapList(List<io.github.intisy.ai.ir.IrTool> tools) {
        List<Object> out = new ArrayList<>();
        for (io.github.intisy.ai.ir.IrTool t : tools) out.add(CommonJson.toMap(t));
        return out;
    }

    private static List<io.github.intisy.ai.ir.IrTool> toolsFromMapList(Object o) {
        List<Object> list = JsonUtil.asList(o);
        if (list == null) return null;
        List<io.github.intisy.ai.ir.IrTool> out = new ArrayList<>();
        for (Object item : list) out.add(CommonJson.toolFromMap(item));
        return out;
    }

    private static List<String> stringListFromMap(Object o) {
        List<Object> list = JsonUtil.asList(o);
        if (list == null) return null;
        List<String> out = new ArrayList<>();
        for (Object item : list) out.add(String.valueOf(item));
        return out;
    }
}
