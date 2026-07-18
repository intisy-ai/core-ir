package io.github.intisy.ai.ir;

/**
 * Extended/reasoning-thinking content. {@code signature} is the vendor's opaque verification
 * token (e.g. Anthropic's thinking signature) -- carried verbatim so a translator can restore it
 * on re-encode without needing to understand it.
 */
public final class ThinkingBlock extends Block {
    public String text;
    public String signature;

    public ThinkingBlock() {
        super(BlockKind.THINKING);
    }
}
