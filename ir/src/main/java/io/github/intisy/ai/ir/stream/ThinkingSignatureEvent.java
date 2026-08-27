package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;

/** Delivers the vendor's opaque thinking-verification signature for the block at {@code index}. */
@TsDiscriminant(field = "event", value = IrEventType.THINKING_SIGNATURE)
@TsInterface(data = true)
public final class ThinkingSignatureEvent extends IrStreamEvent {
    /** Position of the content block this signature verifies. */
    public int index;
    /** The vendor's opaque signature value. */
    @TsOptional
    @TsNullable
    public String signature;

    /** Creates an event with no fields set beyond the discriminator. */
    public ThinkingSignatureEvent() {
        super(IrEventType.THINKING_SIGNATURE);
    }
}
