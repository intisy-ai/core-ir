package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.spi.StreamDecoder;
import io.github.intisy.ai.ir.stream.ContentBlockKind;
import io.github.intisy.ai.ir.stream.ContentBlockStartEvent;
import io.github.intisy.ai.ir.stream.ContentBlockStopEvent;
import io.github.intisy.ai.ir.stream.ErrorEvent;
import io.github.intisy.ai.ir.stream.IrStreamEvent;
import io.github.intisy.ai.ir.stream.MessageDeltaEvent;
import io.github.intisy.ai.ir.stream.MessageStartEvent;
import io.github.intisy.ai.ir.stream.MessageStopEvent;
import io.github.intisy.ai.ir.stream.TextDeltaEvent;
import io.github.intisy.ai.ir.stream.ThinkingDeltaEvent;
import io.github.intisy.ai.ir.stream.ThinkingSignatureEvent;
import io.github.intisy.ai.ir.stream.ToolInputDeltaEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stateful Anthropic SSE decoder. Buffers partial lines/frames across {@link #decode} calls (a
 * chunk may split mid-line or mid-frame) and emits one {@link IrStreamEvent} per completed frame,
 * dispatching on the frame's own {@code type} field ({@code event:} lines are not required).
 * {@code ping} frames carry no semantic payload and are dropped -- every other Anthropic SSE
 * event type maps 1:1 onto the canonical stream event model.
 */
final class AnthropicStreamDecoder implements StreamDecoder {
    private final JsonCodec json;
    private final StringBuilder lineBuffer = new StringBuilder();
    private final StringBuilder dataBuffer = new StringBuilder();
    private boolean sawDataLine = false;

    AnthropicStreamDecoder(JsonCodec json) {
        this.json = json;
    }

    @Override
    public List<IrStreamEvent> decode(String chunk) {
        List<IrStreamEvent> out = new ArrayList<>();
        if (chunk == null || chunk.isEmpty()) return out;
        lineBuffer.append(chunk);
        int newlineIndex;
        while ((newlineIndex = lineBuffer.indexOf("\n")) >= 0) {
            String line = lineBuffer.substring(0, newlineIndex);
            lineBuffer.delete(0, newlineIndex + 1);
            if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
            processLine(line, out);
        }
        return out;
    }

    private void processLine(String line, List<IrStreamEvent> out) {
        if (line.isEmpty()) {
            flushFrame(out);
            return;
        }
        if (line.startsWith(":")) return; // SSE comment/keepalive
        if (line.startsWith("data:")) {
            String data = line.substring(5);
            if (data.startsWith(" ")) data = data.substring(1);
            if (sawDataLine) dataBuffer.append('\n');
            dataBuffer.append(data);
            sawDataLine = true;
        }
        // "event:"/"id:"/"retry:" carry nothing beyond what the data payload's own "type"
        // field already duplicates for every Anthropic SSE frame.
    }

    private void flushFrame(List<IrStreamEvent> out) {
        if (!sawDataLine) return;
        String data = dataBuffer.toString();
        dataBuffer.setLength(0);
        sawDataLine = false;
        if (data.isEmpty()) return;
        Map<String, Object> frame = AnthropicJsonUtil.asMap(json.parse(data));
        if (frame == null) return;
        IrStreamEvent event = decodeFrame(frame);
        if (event != null) out.add(event);
    }

    private IrStreamEvent decodeFrame(Map<String, Object> frame) {
        String type = AnthropicJsonUtil.asString(frame.get("type"));
        if (type == null) return null;
        if ("message_start".equals(type)) return decodeMessageStart(frame);
        if ("content_block_start".equals(type)) return decodeContentBlockStart(frame);
        if ("content_block_delta".equals(type)) return decodeContentBlockDelta(frame);
        if ("content_block_stop".equals(type)) return decodeContentBlockStop(frame);
        if ("message_delta".equals(type)) return decodeMessageDelta(frame);
        if ("message_stop".equals(type)) return new MessageStopEvent();
        if ("error".equals(type)) return decodeError(frame);
        // "ping" (and anything future) carries no payload we translate -- silently dropped.
        return null;
    }

    private IrStreamEvent decodeMessageStart(Map<String, Object> frame) {
        Map<String, Object> message = AnthropicJsonUtil.asMap(frame.get("message"));
        if (message == null) return null;
        MessageStartEvent ev = new MessageStartEvent();
        ev.id = AnthropicJsonUtil.asString(message.get("id"));
        ev.model = AnthropicJsonUtil.asString(message.get("model"));
        ev.role = AnthropicJsonUtil.asString(message.get("role"));
        ev.usage = AnthropicUsageCodec.decode(message.get("usage"));
        return ev;
    }

    private IrStreamEvent decodeContentBlockStart(Map<String, Object> frame) {
        ContentBlockStartEvent ev = new ContentBlockStartEvent();
        ev.index = orZero(AnthropicJsonUtil.asInt(frame.get("index")));
        Map<String, Object> block = AnthropicJsonUtil.asMap(frame.get("content_block"));
        String blockType = block == null ? null : AnthropicJsonUtil.asString(block.get("type"));
        if ("tool_use".equals(blockType)) {
            ev.blockKind = ContentBlockKind.TOOL_USE;
            ev.toolUseId = AnthropicJsonUtil.asString(block.get("id"));
            ev.toolName = AnthropicJsonUtil.asString(block.get("name"));
        } else if ("thinking".equals(blockType) || "redacted_thinking".equals(blockType)) {
            ev.blockKind = ContentBlockKind.THINKING;
        } else {
            ev.blockKind = ContentBlockKind.TEXT;
        }
        return ev;
    }

    private IrStreamEvent decodeContentBlockDelta(Map<String, Object> frame) {
        int index = orZero(AnthropicJsonUtil.asInt(frame.get("index")));
        Map<String, Object> delta = AnthropicJsonUtil.asMap(frame.get("delta"));
        String deltaType = delta == null ? null : AnthropicJsonUtil.asString(delta.get("type"));
        if ("text_delta".equals(deltaType)) {
            TextDeltaEvent ev = new TextDeltaEvent();
            ev.index = index;
            ev.text = AnthropicJsonUtil.asString(delta.get("text"));
            return ev;
        }
        if ("input_json_delta".equals(deltaType)) {
            ToolInputDeltaEvent ev = new ToolInputDeltaEvent();
            ev.index = index;
            ev.partialJson = AnthropicJsonUtil.asString(delta.get("partial_json"));
            return ev;
        }
        if ("thinking_delta".equals(deltaType)) {
            ThinkingDeltaEvent ev = new ThinkingDeltaEvent();
            ev.index = index;
            ev.text = AnthropicJsonUtil.asString(delta.get("thinking"));
            return ev;
        }
        if ("signature_delta".equals(deltaType)) {
            ThinkingSignatureEvent ev = new ThinkingSignatureEvent();
            ev.index = index;
            ev.signature = AnthropicJsonUtil.asString(delta.get("signature"));
            return ev;
        }
        return null;
    }

    private IrStreamEvent decodeContentBlockStop(Map<String, Object> frame) {
        ContentBlockStopEvent ev = new ContentBlockStopEvent();
        ev.index = orZero(AnthropicJsonUtil.asInt(frame.get("index")));
        return ev;
    }

    private IrStreamEvent decodeMessageDelta(Map<String, Object> frame) {
        MessageDeltaEvent ev = new MessageDeltaEvent();
        Map<String, Object> delta = AnthropicJsonUtil.asMap(frame.get("delta"));
        if (delta != null) {
            ev.stopReason = AnthropicStopReason.toIr(AnthropicJsonUtil.asString(delta.get("stop_reason")));
        }
        ev.usage = AnthropicUsageCodec.decode(frame.get("usage"));
        return ev;
    }

    private IrStreamEvent decodeError(Map<String, Object> frame) {
        ErrorEvent ev = new ErrorEvent();
        Map<String, Object> error = AnthropicJsonUtil.asMap(frame.get("error"));
        if (error != null) {
            ev.errorType = AnthropicJsonUtil.asString(error.get("type"));
            ev.message = AnthropicJsonUtil.asString(error.get("message"));
        }
        return ev;
    }

    private static int orZero(Integer i) {
        return i == null ? 0 : i;
    }
}
