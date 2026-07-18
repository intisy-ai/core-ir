package io.github.intisy.ai.ir.json;

import io.github.intisy.ai.ir.stream.ContentBlockStartEvent;
import io.github.intisy.ai.ir.stream.ContentBlockStopEvent;
import io.github.intisy.ai.ir.stream.ErrorEvent;
import io.github.intisy.ai.ir.stream.IrEventType;
import io.github.intisy.ai.ir.stream.IrStreamEvent;
import io.github.intisy.ai.ir.stream.MessageDeltaEvent;
import io.github.intisy.ai.ir.stream.MessageStartEvent;
import io.github.intisy.ai.ir.stream.MessageStopEvent;
import io.github.intisy.ai.ir.stream.TextDeltaEvent;
import io.github.intisy.ai.ir.stream.ThinkingDeltaEvent;
import io.github.intisy.ai.ir.stream.ThinkingSignatureEvent;
import io.github.intisy.ai.ir.stream.ToolInputDeltaEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link IrStreamEvent} hierarchy <-> {@code Map} tree, dispatching on {@link IrEventType}. */
final class IrStreamEventJson {
    private IrStreamEventJson() {
    }

    static Map<String, Object> toMap(IrStreamEvent e) {
        if (e == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event", e.event);
        if (e instanceof MessageStartEvent) {
            MessageStartEvent ev = (MessageStartEvent) e;
            m.put("id", ev.id);
            m.put("model", ev.model);
            m.put("role", ev.role);
            if (ev.usage != null) m.put("usage", CommonJson.toMap(ev.usage));
        } else if (e instanceof ContentBlockStartEvent) {
            ContentBlockStartEvent ev = (ContentBlockStartEvent) e;
            m.put("index", ev.index);
            m.put("blockKind", ev.blockKind);
            if (ev.toolUseId != null) m.put("toolUseId", ev.toolUseId);
            if (ev.toolName != null) m.put("toolName", ev.toolName);
        } else if (e instanceof TextDeltaEvent) {
            TextDeltaEvent ev = (TextDeltaEvent) e;
            m.put("index", ev.index);
            m.put("text", ev.text);
        } else if (e instanceof ThinkingDeltaEvent) {
            ThinkingDeltaEvent ev = (ThinkingDeltaEvent) e;
            m.put("index", ev.index);
            m.put("text", ev.text);
        } else if (e instanceof ThinkingSignatureEvent) {
            ThinkingSignatureEvent ev = (ThinkingSignatureEvent) e;
            m.put("index", ev.index);
            m.put("signature", ev.signature);
        } else if (e instanceof ToolInputDeltaEvent) {
            ToolInputDeltaEvent ev = (ToolInputDeltaEvent) e;
            m.put("index", ev.index);
            m.put("partialJson", ev.partialJson);
        } else if (e instanceof ContentBlockStopEvent) {
            m.put("index", ((ContentBlockStopEvent) e).index);
        } else if (e instanceof MessageDeltaEvent) {
            MessageDeltaEvent ev = (MessageDeltaEvent) e;
            if (ev.stopReason != null) m.put("stopReason", ev.stopReason);
            if (ev.usage != null) m.put("usage", CommonJson.toMap(ev.usage));
        } else if (e instanceof MessageStopEvent) {
            // no fields beyond the discriminator
        } else if (e instanceof ErrorEvent) {
            ErrorEvent ev = (ErrorEvent) e;
            m.put("errorType", ev.errorType);
            m.put("message", ev.message);
        } else {
            throw new IllegalArgumentException("unsupported IrStreamEvent type: " + e.getClass());
        }
        return m;
    }

    static IrStreamEvent fromMap(Map<String, Object> m) {
        if (m == null) return null;
        String event = JsonUtil.asString(m.get("event"));
        if (IrEventType.MESSAGE_START.equals(event)) {
            MessageStartEvent ev = new MessageStartEvent();
            ev.id = JsonUtil.asString(m.get("id"));
            ev.model = JsonUtil.asString(m.get("model"));
            ev.role = JsonUtil.asString(m.get("role"));
            ev.usage = CommonJson.usageFromMap(m.get("usage"));
            return ev;
        }
        if (IrEventType.CONTENT_BLOCK_START.equals(event)) {
            ContentBlockStartEvent ev = new ContentBlockStartEvent();
            ev.index = orZero(JsonUtil.asInt(m.get("index")));
            ev.blockKind = JsonUtil.asString(m.get("blockKind"));
            ev.toolUseId = JsonUtil.asString(m.get("toolUseId"));
            ev.toolName = JsonUtil.asString(m.get("toolName"));
            return ev;
        }
        if (IrEventType.TEXT_DELTA.equals(event)) {
            TextDeltaEvent ev = new TextDeltaEvent();
            ev.index = orZero(JsonUtil.asInt(m.get("index")));
            ev.text = JsonUtil.asString(m.get("text"));
            return ev;
        }
        if (IrEventType.THINKING_DELTA.equals(event)) {
            ThinkingDeltaEvent ev = new ThinkingDeltaEvent();
            ev.index = orZero(JsonUtil.asInt(m.get("index")));
            ev.text = JsonUtil.asString(m.get("text"));
            return ev;
        }
        if (IrEventType.THINKING_SIGNATURE.equals(event)) {
            ThinkingSignatureEvent ev = new ThinkingSignatureEvent();
            ev.index = orZero(JsonUtil.asInt(m.get("index")));
            ev.signature = JsonUtil.asString(m.get("signature"));
            return ev;
        }
        if (IrEventType.TOOL_INPUT_DELTA.equals(event)) {
            ToolInputDeltaEvent ev = new ToolInputDeltaEvent();
            ev.index = orZero(JsonUtil.asInt(m.get("index")));
            ev.partialJson = JsonUtil.asString(m.get("partialJson"));
            return ev;
        }
        if (IrEventType.CONTENT_BLOCK_STOP.equals(event)) {
            ContentBlockStopEvent ev = new ContentBlockStopEvent();
            ev.index = orZero(JsonUtil.asInt(m.get("index")));
            return ev;
        }
        if (IrEventType.MESSAGE_DELTA.equals(event)) {
            MessageDeltaEvent ev = new MessageDeltaEvent();
            ev.stopReason = JsonUtil.asString(m.get("stopReason"));
            ev.usage = CommonJson.usageFromMap(m.get("usage"));
            return ev;
        }
        if (IrEventType.MESSAGE_STOP.equals(event)) {
            return new MessageStopEvent();
        }
        if (IrEventType.ERROR.equals(event)) {
            ErrorEvent ev = new ErrorEvent();
            ev.errorType = JsonUtil.asString(m.get("errorType"));
            ev.message = JsonUtil.asString(m.get("message"));
            return ev;
        }
        throw new IllegalArgumentException("unsupported stream event type: " + event);
    }

    private static int orZero(Integer i) {
        return i == null ? 0 : i;
    }
}
