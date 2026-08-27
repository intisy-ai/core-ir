package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;

/** Closes the content block at {@code index}. */
@TsDiscriminant(field = "event", value = IrEventType.CONTENT_BLOCK_STOP)
@TsInterface(data = true)
public final class ContentBlockStopEvent extends IrStreamEvent {
    /** Position of the content block that is closing. */
    public int index;

    /** Creates an event with no fields set beyond the discriminator. */
    public ContentBlockStopEvent() {
        super(IrEventType.CONTENT_BLOCK_STOP);
    }
}
