package io.github.intisy.ai.ir.stream;

/** Delivers the vendor's opaque thinking-verification signature for the block at {@code index}. */
public final class ThinkingSignatureEvent extends IrStreamEvent {
    /** Position of the content block this signature verifies. */
    public int index;
    /** The vendor's opaque signature value. */
    public String signature;

    /** Creates an event with no fields set beyond the discriminator. */
    public ThinkingSignatureEvent() {
        super(IrEventType.THINKING_SIGNATURE);
    }
}
