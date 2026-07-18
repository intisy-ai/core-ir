package io.github.intisy.ai.ir.stream;

/** Delivers the vendor's opaque thinking-verification signature for the block at {@code index}. */
public final class ThinkingSignatureEvent extends IrStreamEvent {
    public int index;
    public String signature;

    public ThinkingSignatureEvent() {
        super(IrEventType.THINKING_SIGNATURE);
    }
}
