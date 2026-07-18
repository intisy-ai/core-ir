package io.github.intisy.ai.ir.translators.gemini;

import io.github.intisy.ai.ir.IrUsage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gemini {@code usageMetadata} object <-> {@link IrUsage}, shared by the response and streaming
 * codecs. {@code promptTokenCount}/{@code candidatesTokenCount}/{@code cachedContentTokenCount}
 * map cleanly onto {@code inputTokens}/{@code outputTokens}/{@code cacheReadInputTokens};
 * {@code thoughtsTokenCount} and {@code totalTokenCount} have no neutral {@link IrUsage} field
 * (IR has no reasoning/thinking token count, and no derived-total field), so the caller stashes
 * them in the owning response/event's {@code extensions} bag to keep the round trip lossless.
 * {@code cacheCreationInputTokens} has no Gemini analog (Gemini's context cache is a separate
 * resource, not a per-request write count) and is always {@code null} on decode.
 */
final class GeminiUsageCodec {
    private GeminiUsageCodec() {
    }

    static IrUsage decode(Object raw) {
        Map<String, Object> m = GeminiJsonUtil.asMap(raw);
        if (m == null) return null;
        IrUsage u = new IrUsage();
        u.inputTokens = GeminiJsonUtil.asInt(m.get("promptTokenCount"));
        u.outputTokens = GeminiJsonUtil.asInt(m.get("candidatesTokenCount"));
        u.cacheReadInputTokens = GeminiJsonUtil.asInt(m.get("cachedContentTokenCount"));
        return u;
    }

    static Map<String, Object> encode(IrUsage u) {
        if (u == null) return null;
        Map<String, Object> m = new LinkedHashMap<>();
        if (u.inputTokens != null) m.put("promptTokenCount", u.inputTokens);
        if (u.outputTokens != null) m.put("candidatesTokenCount", u.outputTokens);
        if (u.cacheReadInputTokens != null) m.put("cachedContentTokenCount", u.cacheReadInputTokens);
        return m;
    }
}
