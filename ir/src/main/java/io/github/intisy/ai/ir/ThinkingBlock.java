package io.github.intisy.ai.ir;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/**
 * Extended/reasoning-thinking content. {@code signature} is the vendor's opaque verification
 * token (e.g. a thinking signature some vendors attach) -- carried verbatim so a translator can
 * restore it on re-encode without needing to understand it.
 */
@TsDiscriminant(field = "kind", value = BlockKind.THINKING)
@TsInterface(data = true)
public final class ThinkingBlock extends Block {
    /** The reasoning content. */
    public String text;
    /** The vendor's opaque verification token, carried verbatim, or null when the vendor issues none. */
    @TsOptional
    @TsNullable
    public String signature;

    /** Creates an empty thinking block; the caller sets {@link #text} and, if the vendor issues one, {@link #signature}. */
    public ThinkingBlock() {
        super(BlockKind.THINKING);
    }
}
