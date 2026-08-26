package io.github.intisy.ai.ir.stream;

/** An incremental text chunk for the content block at {@code index}. */
public final class TextDeltaEvent extends IrStreamEvent {
    /** Position of the content block this chunk belongs to. */
    public int index;
    /** The incremental text chunk. */
    public String text;

    /** Creates an event with no fields set beyond the discriminator. */
    public TextDeltaEvent() {
        super(IrEventType.TEXT_DELTA);
    }
}
