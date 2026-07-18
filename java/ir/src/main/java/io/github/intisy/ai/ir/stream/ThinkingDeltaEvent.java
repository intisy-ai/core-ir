package io.github.intisy.ai.ir.stream;

/** An incremental thinking-text chunk for the content block at {@code index}. */
public final class ThinkingDeltaEvent extends IrStreamEvent {
    public int index;
    public String text;

    public ThinkingDeltaEvent() {
        super(IrEventType.THINKING_DELTA);
    }
}
