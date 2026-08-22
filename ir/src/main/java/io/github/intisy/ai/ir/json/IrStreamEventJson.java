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
        if (e.extensions != null) m.put("extensions", e.extensions);
        return m;
    }

    @SuppressWarnings("unchecked")
    static IrStreamEvent fromMap(Map<String, Object> m) {
        if (m == null) return null;
        String event = JsonUtil.asString(m.get("event"));
        IrStreamEvent ev;
        if (IrEventType.MESSAGE_START.equals(event)) {
            MessageStartEvent e = new MessageStartEvent();
            e.id = JsonUtil.asString(m.get("id"));
            e.model = JsonUtil.asString(m.get("model"));
            e.role = JsonUtil.asString(m.get("role"));
            e.usage = CommonJson.usageFromMap(m.get("usage"));
            ev = e;
        } else if (IrEventType.CONTENT_BLOCK_START.equals(event)) {
            ContentBlockStartEvent e = new ContentBlockStartEvent();
            e.index = orZero(JsonUtil.asInt(m.get("index")));
            e.blockKind = JsonUtil.asString(m.get("blockKind"));
            e.toolUseId = JsonUtil.asString(m.get("toolUseId"));
            e.toolName = JsonUtil.asString(m.get("toolName"));
            ev = e;
        } else if (IrEventType.TEXT_DELTA.equals(event)) {
            TextDeltaEvent e = new TextDeltaEvent();
            e.index = orZero(JsonUtil.asInt(m.get("index")));
            e.text = JsonUtil.asString(m.get("text"));
            ev = e;
        } else if (IrEventType.THINKING_DELTA.equals(event)) {
            ThinkingDeltaEvent e = new ThinkingDeltaEvent();
            e.index = orZero(JsonUtil.asInt(m.get("index")));
            e.text = JsonUtil.asString(m.get("text"));
            ev = e;
        } else if (IrEventType.THINKING_SIGNATURE.equals(event)) {
            ThinkingSignatureEvent e = new ThinkingSignatureEvent();
            e.index = orZero(JsonUtil.asInt(m.get("index")));
            e.signature = JsonUtil.asString(m.get("signature"));
            ev = e;
        } else if (IrEventType.TOOL_INPUT_DELTA.equals(event)) {
            ToolInputDeltaEvent e = new ToolInputDeltaEvent();
            e.index = orZero(JsonUtil.asInt(m.get("index")));
            e.partialJson = JsonUtil.asString(m.get("partialJson"));
            ev = e;
        } else if (IrEventType.CONTENT_BLOCK_STOP.equals(event)) {
            ContentBlockStopEvent e = new ContentBlockStopEvent();
            e.index = orZero(JsonUtil.asInt(m.get("index")));
            ev = e;
        } else if (IrEventType.MESSAGE_DELTA.equals(event)) {
            MessageDeltaEvent e = new MessageDeltaEvent();
            e.stopReason = JsonUtil.asString(m.get("stopReason"));
            e.usage = CommonJson.usageFromMap(m.get("usage"));
            ev = e;
        } else if (IrEventType.MESSAGE_STOP.equals(event)) {
            ev = new MessageStopEvent();
        } else if (IrEventType.ERROR.equals(event)) {
            ErrorEvent e = new ErrorEvent();
            e.errorType = JsonUtil.asString(m.get("errorType"));
            e.message = JsonUtil.asString(m.get("message"));
            ev = e;
        } else {
            throw new IllegalArgumentException("unsupported stream event type: " + event);
        }
        ev.extensions = (Map<String, Object>) m.get("extensions");
        return ev;
    }

    private static int orZero(Integer i) {
        return i == null ? 0 : i;
    }
}
