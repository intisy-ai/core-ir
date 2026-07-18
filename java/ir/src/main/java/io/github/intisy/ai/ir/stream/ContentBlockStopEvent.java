package io.github.intisy.ai.ir.stream;

/** Closes the content block at {@code index}. */
public final class ContentBlockStopEvent extends IrStreamEvent {
    public int index;

    public ContentBlockStopEvent() {
        super(IrEventType.CONTENT_BLOCK_STOP);
    }
}
