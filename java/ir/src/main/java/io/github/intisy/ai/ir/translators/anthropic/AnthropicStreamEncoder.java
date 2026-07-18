package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.spi.JsonCodec;
import io.github.intisy.ai.ir.spi.StreamEncoder;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stateless per-event Anthropic SSE encoder: each {@link IrStreamEvent} maps to exactly one
 * {@code event:}/{@code data:} frame (no cross-event buffering needed on the encode side).
 */
final class AnthropicStreamEncoder implements StreamEncoder {
    private final JsonCodec json;

    AnthropicStreamEncoder(JsonCodec json) {
        this.json = json;
    }

    @Override
    public String encode(IrStreamEvent event) {
        if (event instanceof MessageStartEvent) {
            return frame("message_start", encodeMessageStart((MessageStartEvent) event));
        }
        if (event instanceof ContentBlockStartEvent) {
            return frame("content_block_start", encodeContentBlockStart((ContentBlockStartEvent) event));
        }
        if (event instanceof TextDeltaEvent) {
            return frame("content_block_delta", encodeTextDelta((TextDeltaEvent) event));
        }
        if (event instanceof ThinkingDeltaEvent) {
            return frame("content_block_delta", encodeThinkingDelta((ThinkingDeltaEvent) event));
        }
        if (event instanceof ThinkingSignatureEvent) {
            return frame("content_block_delta", encodeSignatureDelta((ThinkingSignatureEvent) event));
        }
        if (event instanceof ToolInputDeltaEvent) {
            return frame("content_block_delta", encodeToolInputDelta((ToolInputDeltaEvent) event));
        }
        if (event instanceof ContentBlockStopEvent) {
            return frame("content_block_stop", encodeContentBlockStop((ContentBlockStopEvent) event));
        }
        if (event instanceof MessageDeltaEvent) {
            return frame("message_delta", encodeMessageDelta((MessageDeltaEvent) event));
        }
        if (event instanceof MessageStopEvent) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("type", "message_stop");
            return frame("message_stop", data);
        }
        if (event instanceof ErrorEvent) {
            return frame("error", encodeError((ErrorEvent) event));
        }
        throw new IllegalArgumentException("unsupported IrStreamEvent type: " + event.getClass());
    }

    private Map<String, Object> encodeMessageStart(MessageStartEvent ev) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("id", ev.id);
        message.put("type", "message");
        message.put("role", ev.role);
        message.put("content", new ArrayList<Object>());
        message.put("model", ev.model);
        message.put("stop_reason", null);
        message.put("stop_sequence", null);
        message.put("usage", AnthropicUsageCodec.encode(ev.usage));
        encodeLeftoverExtensions(ev, message);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "message_start");
        data.put("message", message);
        return data;
    }

    private Map<String, Object> encodeContentBlockStart(ContentBlockStartEvent ev) {
        Map<String, Object> contentBlock = new LinkedHashMap<>();
        if (ContentBlockKind.TOOL_USE.equals(ev.blockKind)) {
            contentBlock.put("type", "tool_use");
            contentBlock.put("id", ev.toolUseId);
            contentBlock.put("name", ev.toolName);
            contentBlock.put("input", new LinkedHashMap<String, Object>());
        } else if (ContentBlockKind.THINKING.equals(ev.blockKind)) {
            contentBlock.put("type", "thinking");
            contentBlock.put("thinking", "");
        } else {
            contentBlock.put("type", "text");
            contentBlock.put("text", "");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "content_block_start");
        data.put("index", ev.index);
        data.put("content_block", contentBlock);
        return data;
    }

    private Map<String, Object> encodeTextDelta(TextDeltaEvent ev) {
        Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("type", "text_delta");
        delta.put("text", ev.text);
        return deltaFrame(ev.index, delta);
    }

    private Map<String, Object> encodeThinkingDelta(ThinkingDeltaEvent ev) {
        Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("type", "thinking_delta");
        delta.put("thinking", ev.text);
        return deltaFrame(ev.index, delta);
    }

    private Map<String, Object> encodeSignatureDelta(ThinkingSignatureEvent ev) {
        Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("type", "signature_delta");
        delta.put("signature", ev.signature);
        return deltaFrame(ev.index, delta);
    }

    private Map<String, Object> encodeToolInputDelta(ToolInputDeltaEvent ev) {
        Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("type", "input_json_delta");
        delta.put("partial_json", ev.partialJson);
        return deltaFrame(ev.index, delta);
    }

    private static Map<String, Object> deltaFrame(int index, Map<String, Object> delta) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "content_block_delta");
        data.put("index", index);
        data.put("delta", delta);
        return data;
    }

    private Map<String, Object> encodeContentBlockStop(ContentBlockStopEvent ev) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "content_block_stop");
        data.put("index", ev.index);
        return data;
    }

    private Map<String, Object> encodeMessageDelta(MessageDeltaEvent ev) {
        Map<String, Object> delta = new LinkedHashMap<>();
        delta.put("stop_reason", ev.stopReason != null ? AnthropicStopReason.toAnthropic(ev.stopReason) : null);
        Object rawStopSequence = ev.extensions == null
                ? null : ev.extensions.get(AnthropicStreamDecoder.EXT_STOP_SEQUENCE_RAW);
        delta.put("stop_sequence", rawStopSequence);
        encodeLeftoverExtensions(ev, delta);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "message_delta");
        data.put("delta", delta);
        if (ev.usage != null) data.put("usage", AnthropicUsageCodec.encode(ev.usage));
        return data;
    }

    private static void encodeLeftoverExtensions(IrStreamEvent event, Map<String, Object> m) {
        if (event.extensions == null) return;
        for (Map.Entry<String, Object> e : event.extensions.entrySet()) {
            if (!e.getKey().startsWith("$")) m.put(e.getKey(), e.getValue());
        }
    }

    private Map<String, Object> encodeError(ErrorEvent ev) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("type", ev.errorType);
        error.put("message", ev.message);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "error");
        data.put("error", error);
        return data;
    }

    private String frame(String eventName, Map<String, Object> data) {
        return "event: " + eventName + "\ndata: " + json.stringify(data) + "\n\n";
    }
}
