package io.github.intisy.ai.ir;

/** Plain text content block. */
public final class TextBlock extends Block {
    public String text;

    public TextBlock() {
        super(BlockKind.TEXT);
    }

    public TextBlock(String text) {
        this();
        this.text = text;
    }
}
