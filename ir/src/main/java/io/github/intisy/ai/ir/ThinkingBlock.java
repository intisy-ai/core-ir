package io.github.intisy.ai.ir;

/**
 * Extended/reasoning-thinking content. {@code signature} is the vendor's opaque verification
 * token (e.g. a thinking signature some vendors attach) -- carried verbatim so a translator can
 * restore it on re-encode without needing to understand it.
 */
public final class ThinkingBlock extends Block {
    /** The reasoning content. */
    public String text;
    /** The vendor's opaque verification token, carried verbatim, or null when the vendor issues none. */
    public String signature;

    /** Creates an empty thinking block; the caller sets {@link #text} and, if the vendor issues one, {@link #signature}. */
    public ThinkingBlock() {
        super(BlockKind.THINKING);
    }
}
