package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * Base of the content-block hierarchy: {@link TextBlock}, {@link ImageBlock},
 * {@link ToolUseBlock}, {@link ToolResultBlock}, {@link ThinkingBlock}. Chosen as the most
 * expressive superset across Anthropic/Gemini/OpenAI content models -- this is a purpose-built
 * neutral shape, not "adopting Anthropic".
 *
 * <p>{@code kind} is the JSON discriminator ({@link BlockKind}). {@code cacheControl} and
 * {@code extensions} carry vendor-specific passthrough with no neutral home (e.g. Anthropic's
 * {@code cache_control}), so a translator's {@code decode(wire)->IR->encode(wire)} round trip
 * stays semantically lossless.
 */
public abstract class Block {
    public String kind;
    public String cacheControl;
    public Map<String, Object> extensions;

    protected Block(String kind) {
        this.kind = kind;
    }
}
