package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * Base of the content-block hierarchy: {@link TextBlock}, {@link ImageBlock},
 * {@link ToolUseBlock}, {@link ToolResultBlock}, {@link ThinkingBlock}. Chosen as the most
 * expressive superset across vendor content models -- this is a purpose-built neutral shape, not
 * adopting any single vendor's shape.
 *
 * <p>{@code kind} is the JSON discriminator ({@link BlockKind}). {@code cacheControl} and
 * {@code extensions} carry vendor-specific passthrough with no neutral home (e.g. a vendor's own
 * {@code cache_control} field), so a translator's {@code decode(wire)->IR->encode(wire)} round trip
 * stays semantically lossless.
 */
public abstract class Block {
    /** The JSON discriminator identifying the concrete subclass; one of the {@link BlockKind} constants. */
    public String kind;
    /** Vendor-specific cache-control hint carried verbatim, or null when the vendor sets none. */
    public String cacheControl;
    /** Vendor-specific fields with no neutral equivalent, or null when none apply. */
    public Map<String, Object> extensions;

    /**
     * @param kind the {@link BlockKind} constant a concrete subclass fixes itself to.
     */
    protected Block(String kind) {
        this.kind = kind;
    }
}
