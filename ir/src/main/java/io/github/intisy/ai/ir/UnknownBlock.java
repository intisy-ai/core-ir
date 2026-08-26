package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * A content block whose {@code type} discriminator a translator does not recognize (e.g. a
 * vendor content type added after this codec was written, such as a {@code document} block some
 * vendor introduces). Rather than fail the whole decode, a translator stashes the ENTIRE raw block
 * verbatim in {@link #raw} so {@code decode(wire)->IR->encode(wire)} stays lossless for content this
 * codec has no typed model for yet -- the same "extensions bag" philosophy as {@link
 * Block#extensions}, just for a whole block instead of one unknown field.
 */
public final class UnknownBlock extends Block {
    /** The entire original wire block, preserved verbatim so a translator can re-emit it unchanged. */
    public Map<String, Object> raw;

    /** Creates an empty unknown block; the caller sets {@link #raw}. */
    public UnknownBlock() {
        super(BlockKind.UNKNOWN);
    }
}
