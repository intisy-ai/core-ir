package io.github.intisy.ai.ir.stream;

/** Closes the content block at {@code index}. */
public final class ContentBlockStopEvent extends IrStreamEvent {
    /** Position of the content block that is closing. */
    public int index;

    /** Creates an event with no fields set beyond the discriminator. */
    public ContentBlockStopEvent() {
        super(IrEventType.CONTENT_BLOCK_STOP);
    }
}
