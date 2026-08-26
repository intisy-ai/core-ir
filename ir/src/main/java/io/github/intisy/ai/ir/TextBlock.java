package io.github.intisy.ai.ir;

/** Plain text content block. */
public final class TextBlock extends Block {
    /** The block's text. */
    public String text;

    /** Creates an empty text block; the caller sets {@link #text}. */
    public TextBlock() {
        super(BlockKind.TEXT);
    }

    /**
     * @param text the block's text.
     */
    public TextBlock(String text) {
        this();
        this.text = text;
    }
}
