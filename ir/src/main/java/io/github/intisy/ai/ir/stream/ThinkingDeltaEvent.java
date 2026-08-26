package io.github.intisy.ai.ir.stream;

/** An incremental thinking-text chunk for the content block at {@code index}. */
public final class ThinkingDeltaEvent extends IrStreamEvent {
    /** Position of the content block this chunk belongs to. */
    public int index;
    /** The incremental thinking-text chunk. */
    public String text;

    /** Creates an event with no fields set beyond the discriminator. */
    public ThinkingDeltaEvent() {
        super(IrEventType.THINKING_DELTA);
    }
}
