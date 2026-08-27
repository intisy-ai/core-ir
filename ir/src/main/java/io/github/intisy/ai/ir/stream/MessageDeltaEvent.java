package io.github.intisy.ai.ir.stream;

import io.github.intisy.ai.ir.IrStopReason;
import io.github.intisy.ai.ir.IrUsage;
import io.github.intisy.ai.tsemit.TsDiscriminant;
import io.github.intisy.ai.tsemit.TsInterface;
import io.github.intisy.ai.tsemit.TsNullable;
import io.github.intisy.ai.tsemit.TsOptional;
import io.github.intisy.ai.tsemit.TsVocabulary;

/** Carries the final {@code stopReason} and/or updated usage, ahead of {@link MessageStopEvent}. */
@TsDiscriminant(field = "event", value = IrEventType.MESSAGE_DELTA)
@TsInterface(data = true)
public final class MessageDeltaEvent extends IrStreamEvent {
    /** The response's final stop reason, or null when not yet known. */
    @TsOptional
    @TsNullable
    @TsVocabulary(IrStopReason.class)
    public String stopReason;
    /** Updated token usage, or null when not yet known. */
    @TsOptional
    @TsNullable
    public IrUsage usage;

    /** Creates an event with no fields set beyond the discriminator. */
    public MessageDeltaEvent() {
        super(IrEventType.MESSAGE_DELTA);
    }
}
