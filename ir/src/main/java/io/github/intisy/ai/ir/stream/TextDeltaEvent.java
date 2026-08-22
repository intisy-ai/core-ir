package io.github.intisy.ai.ir.stream;

/** An incremental text chunk for the content block at {@code index}. */
public final class TextDeltaEvent extends IrStreamEvent {
    public int index;
    public String text;

    public TextDeltaEvent() {
        super(IrEventType.TEXT_DELTA);
    }
}
