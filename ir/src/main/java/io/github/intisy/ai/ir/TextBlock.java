package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;

/** Plain text content block. */
@TsDiscriminant(field = "kind", value = BlockKind.TEXT)
@TsInterface(data = true)
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
