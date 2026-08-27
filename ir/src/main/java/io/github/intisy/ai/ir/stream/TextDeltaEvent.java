package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/** An incremental text chunk for the content block at {@code index}. */
@TsDiscriminant(field = "event", value = IrEventType.TEXT_DELTA)
@TsInterface(data = true)
public final class TextDeltaEvent extends IrStreamEvent {
    /** Position of the content block this chunk belongs to. */
    public int index;
    /** The incremental text chunk. */
    @TsOptional
    @TsNullable
    public String text;

    /** Creates an event with no fields set beyond the discriminator. */
    public TextDeltaEvent() {
        super(IrEventType.TEXT_DELTA);
    }
}
