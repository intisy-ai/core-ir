package io.github.intisy.ai.ir;

import java.util.Map;

/**
 * A content block whose {@code type} discriminator a translator does not recognize (e.g. a
 * vendor content type added after this codec was written, such as Anthropic's {@code document}
 * blocks). Rather than fail the whole decode, a translator stashes the ENTIRE raw block verbatim
 * in {@link #raw} so {@code decode(wire)->IR->encode(wire)} stays lossless for content this
 * codec has no typed model for yet -- the same "extensions bag" philosophy as {@link
 * Block#extensions}, just for a whole block instead of one unknown field.
 */
public final class UnknownBlock extends Block {
    public Map<String, Object> raw;

    public UnknownBlock() {
        super(BlockKind.UNKNOWN);
    }
}
