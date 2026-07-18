package io.github.intisy.ai.ir.translators.anthropic;

import io.github.intisy.ai.ir.IrResponse;
import io.github.intisy.ai.ir.spi.JsonCodec;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Anthropic Messages API (non-streaming) response {@code Map} tree <-> {@link IrResponse}.
 *
 * <p>{@code stop_reason} and {@code stop_sequence} are stashed verbatim in
 * {@link IrResponse#extensions} alongside the best-effort {@code IrStopReason} mapping: Anthropic
 * has two stop reasons ({@code pause_turn}, {@code refusal}) with no IR equivalent yet, and
 * {@code stop_sequence} carries free text IR has no field for. Restoring the raw value on encode
 * keeps the round trip lossless regardless of that gap. Likewise {@code type}/{@code role} (always
 * {@code "message"}/{@code "assistant"} in practice) are preserved verbatim rather than assumed.
 */
final class AnthropicResponseCodec {
    private AnthropicResponseCodec() {
    }

    private static final String EXT_STOP_REASON_RAW = "$stopReasonRaw";
    private static final String EXT_STOP_SEQUENCE_RAW = "$stopSequenceRaw";
    private static final String EXT_TYPE_RAW = "$typeRaw";
    private static final String EXT_ROLE_RAW = "$roleRaw";

    private static final Set<String> TOP_LEVEL_KNOWN_KEYS = new HashSet<>(Arrays.asList(
            "id", "type", "role", "content", "model", "stop_reason", "stop_sequence", "usage"));

    static IrResponse decodeResponse(JsonCodec json, String wireJson) {
        Map<String, Object> root = AnthropicJsonUtil.asMap(json.parse(wireJson));
        IrResponse r = new IrResponse();
        if (root == null) return r;

        r.id = AnthropicJsonUtil.asString(root.get("id"));
        r.model = AnthropicJsonUtil.asString(root.get("model"));
        r.content = AnthropicBlockCodec.decodeBlockList(root.get("content"));
        r.usage = AnthropicUsageCodec.decode(root.get("usage"));

        String stopReasonRaw = AnthropicJsonUtil.asString(root.get("stop_reason"));
        r.stopReason = AnthropicStopReason.toIr(stopReasonRaw);
        putExtension(r, EXT_STOP_REASON_RAW, stopReasonRaw);
        putExtension(r, EXT_STOP_SEQUENCE_RAW, root.get("stop_sequence"));
        putExtension(r, EXT_TYPE_RAW, root.get("type"));
        putExtension(r, EXT_ROLE_RAW, root.get("role"));

        for (Map.Entry<String, Object> e : root.entrySet()) {
            if (!TOP_LEVEL_KNOWN_KEYS.contains(e.getKey())) {
                putExtension(r, e.getKey(), e.getValue());
            }
        }
        return r;
    }

    static String encodeResponse(JsonCodec json, IrResponse r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.id);
        m.put("type", extensionOrDefault(r, EXT_TYPE_RAW, "message"));
        m.put("role", extensionOrDefault(r, EXT_ROLE_RAW, "assistant"));
        m.put("content", AnthropicBlockCodec.encodeBlockList(r.content));
        m.put("model", r.model);
        Object rawStopReason = r.extensions == null ? null : r.extensions.get(EXT_STOP_REASON_RAW);
        m.put("stop_reason", rawStopReason != null ? rawStopReason : r.stopReason);
        m.put("stop_sequence", r.extensions == null ? null : r.extensions.get(EXT_STOP_SEQUENCE_RAW));
        if (r.usage != null) m.put("usage", AnthropicUsageCodec.encode(r.usage));
        encodeLeftoverExtensions(r, m);
        return json.stringify(m);
    }

    private static Object extensionOrDefault(IrResponse r, String key, Object defaultValue) {
        Object v = r.extensions == null ? null : r.extensions.get(key);
        return v != null ? v : defaultValue;
    }

    private static void putExtension(IrResponse r, String key, Object value) {
        if (r.extensions == null) r.extensions = new LinkedHashMap<>();
        r.extensions.put(key, value);
    }

    private static void encodeLeftoverExtensions(IrResponse r, Map<String, Object> m) {
        if (r.extensions == null) return;
        for (Map.Entry<String, Object> e : r.extensions.entrySet()) {
            if (!e.getKey().startsWith("$")) m.put(e.getKey(), e.getValue());
        }
    }
}
